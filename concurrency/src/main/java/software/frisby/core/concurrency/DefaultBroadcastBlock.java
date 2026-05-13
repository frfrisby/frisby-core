package software.frisby.core.concurrency;

import software.frisby.core.validation.Sequences;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

final class DefaultBroadcastBlock<T> implements BroadcastBlock<T> {
    private final List<Target<T>> targets;
    private final UnaryOperator<T> cloningFunction;
    private final ItemPostedManager<T> postedManager;
    private final ItemDeliveredManager<T> deliveredManager;
    private final SyncCompletionGuard guard;

    DefaultBroadcastBlock(List<Target<T>> targets,
                          UnaryOperator<T> cloningFunction,
                          ItemPostedHandler<T> itemPostedHandler,
                          ItemDeliveredHandler<T> itemDeliveredHandler) {
        Sequences.notEmpty("targets", targets);

        EventSource eventSource = new EventSource(BroadcastBlock.class.getSimpleName());

        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);
        this.deliveredManager = new ItemDeliveredManager<>(this, eventSource, itemDeliveredHandler);

        this.targets = List.copyOf(targets);
        this.cloningFunction = cloningFunction;
        this.guard = new SyncCompletionGuard(this::signalDownstream);

        for (Target<T> target : this.targets) {
            target.onLinked();
        }
    }

    @Override
    public boolean post(T item) {
        if (this.guard.isCompleted()) {
            return false;
        }


        if (null == item) {
            return false;
        }

        this.guard.begin();

        try {
            boolean allAccepted = true;

            for (Target<T> target : this.targets) {
                T payload = null == this.cloningFunction ? item : this.cloningFunction.apply(item);
                boolean accepted = target.post(payload);

                if (accepted) {
                    this.deliveredManager.sendOnDeliveredNotification(target, item);
                } else {
                    allAccepted = false;
                }
            }

            this.postedManager.sendOnPostedNotification(item, allAccepted);

            return allAccepted;
        } finally {
            this.guard.end();
        }
    }

    @Override
    public void onLinked() {
        this.guard.onLinked();
    }

    @Override
    public int inFlight() {
        int sum = 0;

        for (Target<T> target : this.targets) {
            sum += target.inFlight();
        }

        return sum;
    }

    @Override
    public void complete() {
        this.guard.complete();
    }

    @Override
    public CompletableFuture<Void> completion() {
        CompletableFuture<?>[] futures = new CompletableFuture[this.targets.size()];

        for (int i = 0; i < this.targets.size(); i++) {
            futures[i] = this.targets.get(i).completion();
        }

        return CompletableFuture.allOf(futures);
    }

    private void signalDownstream() {
        for (Target<T> target : this.targets) {
            target.complete();
        }
    }
}
