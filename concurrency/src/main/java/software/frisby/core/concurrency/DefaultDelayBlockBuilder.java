package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.function.Function;

final class DefaultDelayBlockBuilder<T> implements DelayBlockBuilder<T> {
    private int capacity;
    private Executor executor;
    private Function<T, Duration> delayFunction;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;
    private ErrorOccurredHandler<T> errorOccurredHandler;

    DefaultDelayBlockBuilder() {
        this.capacity = DefaultDelayBlock.DEFAULT_CAPACITY;
    }

    @Override
    public DelayBlockBuilder<T> delay(Duration delay) {
        this.delayFunction = item -> delay;
        return this;
    }

    @Override
    public DelayBlockBuilder<T> delay(Function<T, Duration> delayFunction) {
        this.delayFunction = delayFunction;
        return this;
    }

    @Override
    public DelayBlockBuilder<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public DelayBlockBuilder<T> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public DelayBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public DelayBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public DelayBlockBuilder<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public DelayBlock<T> build() {
        return new DefaultDelayBlock<>(new DelayQueue<>(), capacity, delayFunction, executor, itemPostedHandler, itemDeliveredHandler, errorOccurredHandler);
    }
}
