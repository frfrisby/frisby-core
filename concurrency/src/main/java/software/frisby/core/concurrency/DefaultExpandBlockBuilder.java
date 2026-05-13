package software.frisby.core.concurrency;

import java.util.List;

final class DefaultExpandBlockBuilder<T> implements ExpandBlockBuilder<T> {
    private ItemPostedHandler<List<T>> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    DefaultExpandBlockBuilder() {
    }

    @Override
    public ExpandBlockBuilder<T> itemPostedHandler(ItemPostedHandler<List<T>> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public ExpandBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public ExpandBlock<T> build() {
        return new DefaultExpandBlock<>(this.itemPostedHandler, this.itemDeliveredHandler);
    }
}

