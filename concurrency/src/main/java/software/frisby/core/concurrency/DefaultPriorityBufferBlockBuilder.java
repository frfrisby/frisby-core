package software.frisby.core.concurrency;

import java.util.Comparator;
import java.util.concurrent.Executor;

final class DefaultPriorityBufferBlockBuilder<T> implements PriorityBufferBlockBuilder<T> {
    private int capacity;
    private Comparator<T> comparator;
    private Executor executor;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;
    private ErrorOccurredHandler<T> errorOccurredHandler;

    DefaultPriorityBufferBlockBuilder() {
        this.capacity = DefaultPriorityBufferBlock.DEFAULT_CAPACITY;
    }

    @Override
    public PriorityBufferBlockBuilder<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public PriorityBufferBlockBuilder<T> comparator(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public PriorityBufferBlockBuilder<T> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public PriorityBufferBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public PriorityBufferBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public PriorityBufferBlockBuilder<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public PriorityBufferBlock<T> build() {
        return new DefaultPriorityBufferBlock<>(capacity, executor, comparator, itemPostedHandler, itemDeliveredHandler, errorOccurredHandler);
    }
}
