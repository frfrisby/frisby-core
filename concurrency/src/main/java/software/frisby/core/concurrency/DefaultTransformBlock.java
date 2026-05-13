package software.frisby.core.concurrency;


import software.frisby.core.validation.Values;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


final class DefaultTransformBlock<T, R> implements TransformBlock<T, R> {
    private final Function<T, R> transform;
    private final TargetManager<R> targetManager;

    private final ItemPostedManager<T> postedManager;
    private final SyncCompletionGuard guard;

    DefaultTransformBlock(Function<T, R> transform,
                          ItemPostedHandler<T> itemPostedHandler,
                          ItemDeliveredHandler<R> itemDeliveredHandler) {
        Values.notNull("transform", transform);

        this.transform = transform;

        EventSource eventSource = new EventSource("TransformBlock");
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
        } else {
            this.postedManager.sendOnPostedNotification(item, true);

            this.guard.begin();

            try {
                this.targetManager.postToTarget(this.transform.apply(item));
            } finally {
                this.guard.end();
            }

            return true;
        }
    }

    @Override
    public void linkTo(Target<R> target) {
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
