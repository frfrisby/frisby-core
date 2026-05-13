package software.frisby.core.concurrency;

import java.util.function.Consumer;

final class DefaultTapBlockBuilder<T> implements TapBlockBuilder<T> {
    private Consumer<T> consumer;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    DefaultTapBlockBuilder() {
    }

    @Override
    public TapBlockBuilder<T> consumer(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public TapBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public TapBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public TapBlock<T> build() {
        if (null == this.consumer) {
            throw new IllegalStateException(
                    "The 'TapBlock' block requires a consumer.  Call consumer(consumer) before calling build()."
            );
        }

        return new DefaultTapBlock<>(this.consumer, this.itemPostedHandler, this.itemDeliveredHandler);
    }
}

