package software.frisby.core.concurrency;

import java.util.function.Consumer;

/**
 * Builder for constructing a {@link TapBlock}.  Obtain an instance via
 * {@link TapBlock#builder()}.
 *
 * @param <T> The type of items received and forwarded by the block.
 */
public interface TapBlockBuilder<T> extends ObservableBlockBuilder<T, T, TapBlockBuilder<T>> {
    /**
     * Sets the consumer that will be invoked for each received item as a side effect.
     * The item is forwarded downstream unchanged after the consumer returns.  If the
     * consumer throws, the exception propagates to the caller and the item <strong>will not</strong> forwarded.
     *
     * @param consumer The side effect consumer.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code consumer} is null.
     */
    TapBlockBuilder<T> consumer(Consumer<T> consumer);

    /**
     * Returns a new {@link TapBlock} configured by this builder.
     *
     * @return A new {@link TapBlock} instance.
     * @throws IllegalStateException if no consumer has been configured.
     */
    TapBlock<T> build();
}

