package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An asynchronous pipeline stage that accumulates incoming items into {@link java.util.List}
 * batches and delivers each complete batch to the linked downstream target on a dedicated
 * background thread.
 *
 * <p>A batch is flushed to the downstream target when either:</p>
 * <ul>
 *   <li>the batch reaches the configured {@link #batchSize()}, or</li>
 *   <li>the configured {@link #timeout()} elapses since the first item was added to the batch.</li>
 * </ul>
 *
 * <p>Posting is non-blocking as long as the internal queue has capacity.  When the queue is
 * full, {@link #post(Object)} blocks the caller until capacity becomes available.  The timed
 * overload {@link #post(Object, Duration)} allows the caller to specify a maximum wait time.</p>
 *
 * <p>When {@link #complete()} is called, any partial batch is flushed immediately rather than
 * waiting for the batch size or timeout to be reached.</p>
 *
 * <pre>{@code
 * BatchBlock<Event> batcher = BatchBlock.<Event>builder()
 *         .batchSize(100)
 *         .timeout(Duration.ofSeconds(2))
 *         .executor(executor)
 *         .build();
 *
 * source.linkTo(batcher);
 * batcher.linkTo(bulkWriter);  // Target<List<Event>>
 * }</pre>
 *
 * @param <T> The type of items accumulated into batches by this block.
 * @see BatchBlockBuilder
 */
public interface BatchBlock<T> extends Stage<T, List<T>> {
    /**
     * Returns a new builder for constructing a {@link BatchBlock}.
     *
     * @param <T> The type of items held in the block.
     * @return A new {@link BatchBlockBuilder} instance.
     */
    static <T> BatchBlockBuilder<T> builder() {
        return new DefaultBatchBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link BatchBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items held in the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link BatchBlockBuilder} instance.
     */
    static <T> BatchBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Returns the maximum number of items that can be buffered in the internal queue.
     *
     * @return The configured capacity of this block's internal queue.
     */
    int capacity();

    /**
     * Returns the maximum number of items that will be accumulated into a single batch before
     * the batch is flushed to the downstream target.
     *
     * @return The configured batch size.
     */
    int batchSize();

    /**
     * Returns the maximum time a batch will be held before being flushed to the downstream
     * target, even if the batch has not yet reached the configured {@link #batchSize()}.
     *
     * @return The configured batch timeout.
     */
    Duration timeout();

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
     * Returns the number of items currently buffered in the internal queue waiting to be
     * batched and forwarded to the downstream target.
     *
     * @return The current number of items queued in this block.
     */
    @Override
    int size();

    /**
     * Signals that no more items will be posted to this block.  Any partial batch currently
     * accumulating is flushed and delivered immediately rather than waiting for the batch size
     * or timeout to be reached.  The {@link #completion()} future resolves once the drain and
     * all downstream targets have completed.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when all items queued before {@link #complete()} was
     * called have been batched and delivered to the downstream target, and the downstream
     * target has also completed.
     *
     * @return A {@link CompletableFuture} that resolves when the drain is complete.
     */
    @Override
    CompletableFuture<Void> completion();
}
