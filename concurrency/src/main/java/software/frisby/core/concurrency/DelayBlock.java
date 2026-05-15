package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * An asynchronous pipeline stage that holds each incoming item for a configured duration
 * before forwarding it to the linked downstream target.
 *
 * <p>The delay applied to each item is determined by a {@link Function}{@code <T, Duration>}
 * provided at build time.  A fixed delay for all items can be configured using
 * {@link DelayBlockBuilder#delay(Duration)}.  A per-item delay can be configured using
 * {@link DelayBlockBuilder#delay(Function)}.</p>
 *
 * <h2>Drain semantics</h2>
 *
 * <p>When {@link #complete()} is called, all items still held in the delay queue are
 * <strong>delivered immediately</strong>, regardless of how much delay time remains on
 * each item.  This is consistent with the rest of the block family: {@code BatchBlock} does
 * not wait for a full batch to form on drain, {@code GroupBlock} does not wait for group
 * timeouts, and {@code DelayBlock} does not wait for item delays to expire.  The contract is
 * uniform — {@code complete()} means "deliver everything you have right now."</p>
 *
 * <h2>Natural-drain pattern</h2>
 *
 * <p>If items must be delivered after their full configured delay before the pipeline shuts
 * down, track deliveries with a latch and call {@link #complete()} only after the last item
 * has been naturally delivered.  This pattern suits pacing or throttling in long-lived
 * pipelines where unexpired delays must be honored.  At that point the delay queue is empty
 * and the immediate-flush path has nothing to do:</p>
 *
 * <pre>{@code
 * int itemCount = items.size();
 * CountDownLatch allDelivered = new CountDownLatch(itemCount);
 *
 * DelayBlock<Task> block = DelayBlock.<Task>builder()
 *         .delay(Duration.ofSeconds(5))
 *         .executor(executor)
 *         .build();
 *
 * block.linkTo(item -> {
 *     process(item);
 *     allDelivered.countDown();
 *     return true;
 * });
 *
 * for (Task task : items) {
 *     block.post(task);
 * }
 *
 * // Wait for every item to be delivered at its natural delay.
 * allDelivered.await();
 *
 * // The delay queue is now empty — complete() shuts down cleanly with no items to flush.
 * block.complete();
 * block.completion().get();
 * }</pre>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * // Fixed delay — every item is held for 5 seconds:
 * DelayBlock<Task> block = DelayBlock.<Task>builder()
 *         .delay(Duration.ofSeconds(5))
 *         .executor(executor)
 *         .build();
 *
 * // Variable delay — delay is computed from the item itself:
 * DelayBlock<Task> block = DelayBlock.<Task>builder()
 *         .delay(Task::scheduledDelay)
 *         .executor(executor)
 *         .build();
 * }</pre>
 *
 * @param <T> The type of items held and forwarded by this block.
 * @see DelayBlockBuilder
 */
public interface DelayBlock<T> extends Stage<T, T> {
    /**
     * Returns a new builder for constructing a {@link DelayBlock}.
     *
     * @param <T> The type of items held in the block.
     * @return A new {@link DelayBlockBuilder} instance.
     */
    static <T> DelayBlockBuilder<T> builder() {
        return new DefaultDelayBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link DelayBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items held in the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link DelayBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> DelayBlockBuilder<T> builder(Class<T> ignored) {
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
     * Returns the maximum number of items that can be buffered in the internal delay queue.
     *
     * @return The configured capacity of this block's internal delay queue.
     */
    int capacity();

    /**
     * Returns the number of items currently held in the delay queue waiting for their delay
     * to expire before being forwarded to the downstream target.
     *
     * @return The current number of items in the delay queue.
     */
    @Override
    int size();

    /**
     * Signals that no more items will be posted to this block.  Any items still held in the
     * delay queue are <strong>delivered immediately</strong>, regardless of how much delay
     * time remains — unexpired delays are not honored on drain.
     *
     * <p>If you need items to be delivered only after their full configured delay has elapsed,
     * use the natural-drain pattern: track deliveries with a {@code CountDownLatch} and call
     * {@code complete()} only after the latch reaches zero.  See the class-level documentation
     * for a full example.</p>
     *
     * <p>The {@link #completion()} future resolves once all items have been delivered and the
     * downstream target has also completed.</p>
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when all items that were in the delay queue before
     * {@link #complete()} was called have been delivered to the downstream target, and the
     * downstream target has also completed.
     *
     * @return A {@link CompletableFuture} that resolves when the drain is complete.
     */
    @Override
    CompletableFuture<Void> completion();
}
