package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class DefaultTapBlock<T> implements TapBlock<T> {
    private final Consumer<T> consumer;
    private final TargetManager<T> targetManager;

    private final ItemPostedManager<T> postedManager;
    private final SyncCompletionGuard guard;

    DefaultTapBlock(Consumer<T> consumer,
                    ItemPostedHandler<T> itemPostedHandler,
                    ItemDeliveredHandler<T> itemDeliveredHandler) {
        Values.notNull("consumer", consumer);

        this.consumer = consumer;

        EventSource eventSource = new EventSource("TapBlock");
        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler);
        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);

        this.guard = new SyncCompletionGuard(this::signalDownstream);
    }

    @Override
    public boolean post(T item) {
        if (this.guard.isCompleted()) {
            return false;
        }

        this.targetManager.awaitTargets();

        if (null == item) {
            return false;
        }

        this.postedManager.sendOnPostedNotification(item, true);

        this.guard.begin();

        try {
            this.consumer.accept(item);
            this.targetManager.postToTarget(item);
        } finally {
            this.guard.end();
        }

        return true;
    }

    @Override
    public void linkTo(Target<T> target) {
        this.targetManager.add(target);
    }

    @Override
    public int inFlight() {
        return this.targetManager.inFlight();
    }

    @Override
    public void onLinked() {
        this.guard.onLinked();
    }

    @Override
    public void complete() {
        this.guard.complete();
    }

    @Override
    public CompletableFuture<Void> completion() {
        return this.targetManager.completion();
    }

    private void signalDownstream() {
        this.targetManager.complete();
    }
}

