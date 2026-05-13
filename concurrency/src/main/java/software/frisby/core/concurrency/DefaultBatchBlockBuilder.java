package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

final class DefaultBatchBlockBuilder<T> implements BatchBlockBuilder<T> {

    private int capacity;
    private int batchSize;
    private Duration timeout;
    private Executor executor;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<List<T>> itemDeliveredHandler;
    private ErrorOccurredHandler<List<T>> errorOccurredHandler;

    DefaultBatchBlockBuilder() {
        this.capacity = DefaultBatchBlock.DEFAULT_CAPACITY;
        this.batchSize = DefaultBatchBlock.DEFAULT_BATCH_SIZE;
        this.timeout = DefaultBatchBlock.DEFAULT_TIMEOUT;
    }

    @Override
    public BatchBlockBuilder<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public BatchBlockBuilder<T> batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public BatchBlockBuilder<T> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public BatchBlockBuilder<T> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public BatchBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public BatchBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<List<T>> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public BatchBlockBuilder<T> errorOccurredHandler(ErrorOccurredHandler<List<T>> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public BatchBlock<T> build() {
        return new DefaultBatchBlock<>(capacity, batchSize, timeout, executor, itemPostedHandler, itemDeliveredHandler, errorOccurredHandler);
    }
}
