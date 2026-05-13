package software.frisby.core.concurrency;

import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class DefaultRouterBlock<T> implements RouterBlock<T> {
    private final List<Target<T>> targets;
    private final RoutingFunction<T> routingFunction;
    private final ItemPostedManager<T> postedManager;
    private final ItemDeliveredManager<T> deliveredManager;
    private final SyncCompletionGuard guard;

    DefaultRouterBlock(List<Target<T>> targets,
                       RoutingFunction<T> routingFunction,
                       ItemPostedHandler<T> itemPostedHandler,
                       ItemDeliveredHandler<T> itemDeliveredHandler) {
        Sequences.notEmpty("targets", targets);
        Values.notNull("routingFunction", routingFunction);

        EventSource eventSource = new EventSource(RouterBlock.class.getSimpleName());

        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);
        this.deliveredManager = new ItemDeliveredManager<>(this, eventSource, itemDeliveredHandler);

        this.targets = List.copyOf(targets);
        this.routingFunction = routingFunction;
        this.guard = new SyncCompletionGuard(this::signalDownstream);

        for (Target<T> target : this.targets) {
            target.onLinked();
        }
    }

    @Override
    public void onLinked() {
        this.guard.onLinked();
    }

    @Override
    public boolean post(T item) {
        if (this.guard.isCompleted()) {
            return false;
        }

        if (null == item) {
            return false;
        }

        int index = Math.floorMod(this.routingFunction.route(item), this.targets.size());
        Target<T> target = this.targets.get(index);


        this.guard.begin();

        try {
            boolean accepted = target.post(item);

            if (accepted) {
                this.deliveredManager.sendOnDeliveredNotification(target, item);
            }

            this.postedManager.sendOnPostedNotification(item, accepted);

            return accepted;
        } finally {
            this.guard.end();
        }
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
