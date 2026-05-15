package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * An asynchronous pipeline stage that buffers incoming items and delivers them to the linked
 * downstream target on a dedicated background thread, in the order determined by a configured
 * {@link java.util.Comparator}.
 *
 * <p>{@code PriorityBufferBlock} behaves identically to {@link BufferBlock} except that the
 * internal queue is a priority queue: the item ranked highest by the comparator is always
 * delivered next, regardless of insertion order.  Items with equal priority are delivered in
 * FIFO order.</p>
 *
 * <pre>{@code
 * PriorityBufferBlock<Task> buffer = PriorityBufferBlock.<Task>builder()
 *         .comparator(Comparator.comparing(Task::priority).reversed())
 *         .capacity(512)
 *         .executor(executor)
 *         .build();
 *
 * source.linkTo(buffer);
 * buffer.linkTo(worker);
 * }</pre>
 *
 * @param <T> The type of items buffered and forwarded by this block.
 * @see PriorityBufferBlockBuilder
 */
public interface PriorityBufferBlock<T> extends Stage<T, T> {
    /**
     * Returns a new builder for constructing a {@link PriorityBufferBlock}.
     *
     * @param <T> The type of items held in the block.
     * @return A new {@link PriorityBufferBlockBuilder} instance.
     */
    static <T> PriorityBufferBlockBuilder<T> builder() {
        return new DefaultPriorityBufferBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link PriorityBufferBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items held in the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link PriorityBufferBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> PriorityBufferBlockBuilder<T> builder(Class<T> ignored) {
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
     * Signals that no more items will be posted to this block.  The internal priority queue
     * will be fully drained — all buffered items will be delivered to the linked target in
     * priority order — before the block stops.  The {@link #completion()} future resolves
     * once the drain and all downstream targets have completed.
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
