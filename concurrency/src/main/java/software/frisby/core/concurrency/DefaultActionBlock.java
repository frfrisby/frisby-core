package software.frisby.core.concurrency;


import software.frisby.core.validation.Values;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


final class DefaultActionBlock<T> implements ActionBlock<T> {
    private final Consumer<T> action;
    private final ItemPostedManager<T> postedManager;
    private final CompletableFuture<Void> completionFuture;
    private final SyncCompletionGuard guard;

    DefaultActionBlock(Consumer<T> action,
                       ItemPostedHandler<T> itemPostedHandler) {
        Values.notNull("action", action);

        EventSource eventSource = new EventSource(ActionBlock.class.getSimpleName());
        this.action = action;

        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);

        this.completionFuture = new CompletableFuture<>();
        this.guard = new SyncCompletionGuard(() -> this.completionFuture.complete(null));
    }

    @Override
    public boolean post(T item) {
        if (this.guard.isCompleted()) {
            return false;
        }


        if (null == item) {
            return false;
        } else {
            this.guard.begin();

            try {
                this.action.accept(item);
                this.postedManager.sendOnPostedNotification(item, true);
            } finally {
                this.guard.end();
            }

            return true;
        }
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
        return this.completionFuture;
    }
}
