package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * An asynchronous FIFO pipeline stage that buffers incoming items and delivers them to the
 * linked downstream target on a dedicated background thread.
 *
 * <p>Posting to a {@code BufferBlock} is non-blocking as long as the internal queue has
 * capacity.  When the queue is full, {@link #post(Object)} blocks the caller until capacity
 * becomes available or the executor is shut down.  The timed overload
 * {@link #post(Object, Duration)} allows the caller to specify a maximum wait time and
 * proceed if capacity does not free up in time.</p>
 *
 * <p>Delivery to the downstream target happens asynchronously on the block's worker thread,
 * decoupling the posting thread from any latency or blocking in the downstream target.</p>
 *
 * <pre>{@code
 * BufferBlock<String> buffer = BufferBlock.<String>builder()
 *         .capacity(512)
 *         .executor(executor)
 *         .build();
 *
 * source.linkTo(buffer);
 * buffer.linkTo(actionBlock);
 * }</pre>
 *
 * @param <T> The type of items buffered and forwarded by this block.
 * @see BufferBlockBuilder
 */
public interface BufferBlock<T> extends Stage<T, T> {
    /**
     * Returns a new builder for constructing a {@link BufferBlock}.
     *
     * @param <T> The type of items held in the block.
     * @return A new {@link BufferBlockBuilder} instance.
     */
    static <T> BufferBlockBuilder<T> builder() {
        return new DefaultBufferBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link BufferBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items held in the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link BufferBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> BufferBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Posts an item to this block, waiting up to the specified timeout for queue capacity to
     * become available if the internal buffer is full.
     *
     * @param item    The item to post.
     * @param timeout The maximum time to wait for capacity to become available.
     * @return {@code true} if the item was accepted before the timeout expired;
     * {@code false} if the timeout elapsed before capacity became available.
     * @throws software.frisby.core.validation.NullValueException            if {@code timeout} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is not positive.
     */
    boolean post(T item, Duration timeout);

    /**
     * Returns the maximum number of items that can be buffered in the internal queue.
     *
     * @return The configured capacity of this block's internal queue.
     */
    int capacity();

    /**
     * Returns the number of items currently buffered in the internal queue waiting to be
     * forwarded to the downstream target.
     *
     * @return The current number of items queued in this block.
     */
    @Override
    int size();

    /**
     * Signals that no more items will be posted to this block.  The internal queue will be
     * fully drained — all buffered items will be delivered to the linked target — before the
     * block stops.  The {@link #completion()} future resolves once the drain and all
     * downstream targets have completed.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when all items queued before {@link #complete()} was
     * called have been delivered to the downstream target and the downstream target has also
     * completed.
     *
     * @return A {@link CompletableFuture} that resolves when the drain is complete.
     */
    @Override
    CompletableFuture<Void> completion();
}
