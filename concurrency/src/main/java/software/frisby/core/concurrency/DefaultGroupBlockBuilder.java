package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

final class DefaultGroupBlockBuilder<T, K> implements GroupBlockBuilder<T, K> {
    private Function<T, K> groupingFunction;
    private Duration timeout;
    private Duration idleTimeout;
    private int capacity;
    private int maxGroupSize;
    private Executor executor;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<List<T>> itemDeliveredHandler;
    private ErrorOccurredHandler<List<T>> errorOccurredHandler;
    private GroupObserver<T, K> groupObserver;

    DefaultGroupBlockBuilder() {
        this.timeout = DefaultGroupBlock.DEFAULT_TIMEOUT;
        this.idleTimeout = DefaultGroupBlock.DEFAULT_IDLE_TIMEOUT;
        this.capacity = DefaultGroupBlock.DEFAULT_CAPACITY;
        this.maxGroupSize = DefaultGroupBlock.DEFAULT_MAX_GROUP_SIZE;
    }

    @Override
    public GroupBlockBuilder<T, K> groupingFunction(Function<T, K> groupingFunction) {
        this.groupingFunction = groupingFunction;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> idleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> maxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> itemDeliveredHandler(ItemDeliveredHandler<List<T>> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> errorOccurredHandler(ErrorOccurredHandler<List<T>> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public GroupBlockBuilder<T, K> groupObserver(GroupObserver<T, K> observer) {
        this.groupObserver = observer;
        return this;
    }

    @Override
    public GroupBlock<T> build() {
        return new DefaultGroupBlock<>(
                this.groupingFunction,
                this.timeout,
                this.idleTimeout,
                this.capacity,
                this.maxGroupSize,
                this.executor,
                this.itemPostedHandler,
                this.itemDeliveredHandler,
                this.errorOccurredHandler,
                this.groupObserver
        );
    }
}
