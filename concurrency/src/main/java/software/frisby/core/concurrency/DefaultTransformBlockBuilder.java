package software.frisby.core.concurrency;

import java.util.function.Function;

final class DefaultTransformBlockBuilder<T, R> implements TransformBlockBuilder<T, R> {
    private Function<T, R> transform;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<R> itemDeliveredHandler;

    DefaultTransformBlockBuilder() {
    }

    @Override
    public TransformBlockBuilder<T, R> transform(Function<T, R> transform) {
        this.transform = transform;
        return this;
    }

    @Override
    public TransformBlockBuilder<T, R> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public TransformBlockBuilder<T, R> itemDeliveredHandler(ItemDeliveredHandler<R> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public TransformBlock<T, R> build() {
        return new DefaultTransformBlock<>(this.transform, this.itemPostedHandler, this.itemDeliveredHandler);
    }
}
