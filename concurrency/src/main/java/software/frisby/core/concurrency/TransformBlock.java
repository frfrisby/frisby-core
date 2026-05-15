package software.frisby.core.concurrency;

import java.util.concurrent.CompletableFuture;

/**
 * A synchronous pipeline stage that applies a transformation function to each received item
 * and forwards the result to the linked downstream target.
 *
 * <p>{@code TransformBlock} holds no internal queue and uses no executor.  Every item is
 * transformed and forwarded on the posting thread, with no intermediate buffering.  If the
 * transform function returns {@code null}, the result is silently dropped and nothing is
 * forwarded downstream.</p>
 *
 * <p>{@link #complete()} cascades immediately to the linked downstream target — there is
 * nothing to drain.</p>
 *
 * <pre>{@code
 * TransformBlock<String, Integer> lengths = TransformBlock.<String, Integer>builder()
 *         .transform(String::length)
 *         .build();
 *
 * source.linkTo(lengths);
 * lengths.linkTo(intBuffer);
 * }</pre>
 *
 * @param <T> The type of items received by this block from an upstream source.
 * @param <R> The type of items produced by this block and forwarded to the downstream target.
 * @see TransformBlockBuilder
 */
public interface TransformBlock<T, R> extends Stage<T, R> {
    /**
     * Returns a new builder for constructing a {@link TransformBlock}.
     *
     * @param <T> The type of items received from the upstream source.
     * @param <R> The type of items forwarded to the downstream target.
     * @return A new {@link TransformBlockBuilder} instance.
     */
    static <T, R> TransformBlockBuilder<T, R> builder() {
        return new DefaultTransformBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link TransformBlock}.
     * Both parameters are used solely for type inference at the call site; they are not stored.
     *
     * @param <T>               The type of items received from the upstream source.
     * @param <R>               The type of items forwarded to the downstream target.
     * @param ignoredInputType  The input type class; used for inference only.
     * @param ignoredOutputType The output type class; used for inference only.
     * @return A new {@link TransformBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T, R> TransformBlockBuilder<T, R> builder(Class<T> ignoredInputType, Class<R> ignoredOutputType) {
        return builder();
    }

    /**
     * Signals that no more items will be posted to this block.  Since this block processes
     * items inline, completion cascades immediately to the linked downstream target.  The
     * {@link #completion()} future resolves once the downstream target has also completed.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when {@link #complete()} has been called and the linked
     * downstream target has also completed.
     *
     * @return A {@link CompletableFuture} that resolves when the downstream target is complete.
     */
    @Override
    CompletableFuture<Void> completion();
}
