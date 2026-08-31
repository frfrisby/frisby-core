package software.frisby.core.concurrency;

import java.time.Duration;
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
     * Posts an item to this block.  The transform function is applied inline on the calling
     * thread — this block itself never blocks.  The result is then forwarded to the linked
     * downstream target via its own {@link Target#post(Object, Duration)}.  That call waits up
     * to {@code timeout} if the target blocks — for example, an asynchronous block whose queue
     * is full.  If the transform function returns {@code null}, the result is silently dropped,
     * nothing is forwarded downstream, and this method returns {@code false}.
     *
     * @param item    The item to post.
     * @param timeout The maximum time to wait for the downstream target to accept the
     *                transformed result.
     * @return {@code true} if the downstream target accepted the transformed result before
     * {@code timeout} elapsed; {@code false} if this block has already been completed,
     * {@code item} is {@code null}, the transform function returned {@code null}, or
     * {@code timeout} elapsed before the downstream target accepted the result.
     * @throws software.frisby.core.validation.NullValueException            if {@code timeout} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is negative.
     */
    @Override
    boolean post(T item, Duration timeout);

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
