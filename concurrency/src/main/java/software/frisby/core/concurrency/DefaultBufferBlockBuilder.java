package software.frisby.core.concurrency;

import java.util.concurrent.Executor;

final class DefaultBufferBlockBuilder<T> implements BufferBlockBuilder<T> {
    private int capacity;
    private Executor executor;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;
    private ErrorOccurredHandler<T> errorOccurredHandler;

    DefaultBufferBlockBuilder() {
        this.capacity = DefaultBufferBlock.DEFAULT_CAPACITY;
    }

    @Override
    public BufferBlockBuilder<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public BufferBlockBuilder<T> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public BufferBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public BufferBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public BufferBlockBuilder<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public BufferBlock<T> build() {
        return new DefaultBufferBlock<>(capacity, executor, itemPostedHandler, itemDeliveredHandler, errorOccurredHandler);
    }
}
