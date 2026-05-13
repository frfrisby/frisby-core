package software.frisby.core.concurrency;

import java.util.function.Function;

/**
 * Builder for constructing a {@link TransformBlock}.  Obtain an instance via
 * {@link TransformBlock#builder()}.
 *
 * @param <T> The type of items received from the upstream source.
 * @param <R> The type of items forwarded to the downstream target after transformation.
 */
public interface TransformBlockBuilder<T, R> extends ObservableBlockBuilder<T, R, TransformBlockBuilder<T, R>> {
    /**
     * Sets the function that transforms each received item into the output type.  If the
     * function returns {@code null}, the result is silently dropped and nothing is forwarded
     * downstream.
     *
     * @param transform The function that transforms each received item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code transform} is null.
     */
    TransformBlockBuilder<T, R> transform(Function<T, R> transform);

    /**
     * Returns a new {@link TransformBlock} configured by this builder.
     *
     * @return A new {@link TransformBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no transform function has been configured.
     */
    TransformBlock<T, R> build();
}
