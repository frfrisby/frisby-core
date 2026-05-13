package software.frisby.core.concurrency;


import java.util.List;
import java.util.concurrent.CompletableFuture;


final class DefaultExpandBlock<T> implements ExpandBlock<T> {
    private final TargetManager<T> targetManager;
    private final ItemPostedManager<List<T>> postedManager;
    private final SyncCompletionGuard guard;

    DefaultExpandBlock(ItemPostedHandler<List<T>> itemPostedHandler,
                       ItemDeliveredHandler<T> itemDeliveredHandler) {
        EventSource eventSource = new EventSource("ExpandBlock");
        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler);
        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);
        this.guard = new SyncCompletionGuard(this.targetManager::complete);
    }

    @Override
    public boolean post(List<T> list) {
        if (this.guard.isCompleted()) {
            return false;
        }

        this.targetManager.awaitTargets();

        if (null == list) {
            return false;
        }

        this.postedManager.sendOnPostedNotification(list, true);

        this.guard.begin();

        try {
            for (T item : list) {
                if (null != item) {
                    this.targetManager.postToTarget(item);
                }
            }

            return true;
        } finally {
            this.guard.end();
        }
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
}
