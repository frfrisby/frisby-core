package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An asynchronous pipeline stage that groups incoming items by key and delivers each group
 * as a {@link java.util.List} to the linked downstream target on a dedicated background thread.
 *
 * <p>Each posted item is assigned to a group by the configured grouping function.  A group is
 * flushed to the downstream target when any of the following conditions is met:</p>
 * <ul>
 *   <li>the configured {@link #timeout()} elapses since the group was first created,</li>
 *   <li>the configured {@link #idleTimeout()} elapses since the last item was added to the group,</li>
 *   <li>the group reaches the configured {@code maxGroupSize} (if set), or</li>
 *   <li>a {@link GroupObserver} returns {@link Retention#RELEASE} for the group.</li>
 * </ul>
 *
 * <p>When {@link #complete()} is called, all pending groups are flushed immediately regardless
 * of their timeout or idle-timeout state.</p>
 *
 * <pre>{@code
 * GroupBlock<Order> grouper = GroupBlock.<Order, String>builder()
 *         .groupingFunction(Order::customerId)
 *         .timeout(Duration.ofSeconds(10))
 *         .idleTimeout(Duration.ofSeconds(3))
 *         .executor(executor)
 *         .build();
 *
 * source.linkTo(grouper);
 * grouper.linkTo(batchProcessor);  // Target<List<Order>>
 * }</pre>
 *
 * @param <T> The type of items grouped by this block.
 * @see GroupBlockBuilder
 * @see GroupObserver
 * @see Retention
 */
public interface GroupBlock<T> extends Stage<T, List<T>> {
    /**
     * Returns a new builder for constructing a {@link GroupBlock}.
     *
     * @param <T> The type of items grouped by the block.
     * @param <K> The type of the key that identifies each group.
     * @return A new {@link GroupBlockBuilder} instance.
     */
    static <T, K> GroupBlockBuilder<T, K> builder() {
        return new DefaultGroupBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link GroupBlock}.
     * Both parameters are used solely for type inference at the call site; they are not stored.
     *
     * @param <T>             The type of items grouped by the block.
     * @param <K>             The type of the key that identifies each group.
     * @param ignoredItemType The item type class; used for inference only.
     * @param ignoredKeyType  The key type class; used for inference only.
     * @return A new {@link GroupBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T, K> GroupBlockBuilder<T, K> builder(Class<T> ignoredItemType, Class<K> ignoredKeyType) {
        return builder();
    }

    /**
     * Returns the maximum time any group will be held before it is flushed to the downstream
     * target, regardless of how many items have accumulated or how recently the last item
     * arrived.
     *
     * @return The configured group timeout.
     */
    Duration timeout();

    /**
     * Returns the maximum time that may elapse between successive items arriving in a group
     * before the group is flushed to the downstream target.
     *
     * @return The configured idle timeout.
     */
    Duration idleTimeout();

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
     * Returns the maximum number of items a single group may accumulate before it is flushed
     * immediately to the downstream target, bypassing the timeout and idle-timeout windows.
     * Defaults to {@code 128} if not explicitly configured.
     *
     * @return The configured maximum group size.
     */
    int maxGroupSize();

    /**
     * Returns the number of items currently buffered in the internal queue waiting to be
     * grouped and forwarded to the downstream target.
     *
     * @return The current number of items queued in this block.
     */
    @Override
    int size();

    /**
     * Returns the maximum number of items that can be buffered in the internal queue.
     *
     * @return The configured capacity of this block's internal queue.
     */
    int capacity();

    /**
     * Signals that no more items will be posted to this block.  All pending groups are flushed
     * and delivered immediately rather than waiting for any timeout or idle-timeout to expire.
     * The {@link #completion()} future resolves once the drain and all downstream targets have
     * completed.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when all items queued before {@link #complete()} was
     * called have been grouped and delivered to the downstream target, and the downstream
     * target has also completed.
     *
     * @return A {@link CompletableFuture} that resolves when the drain is complete.
     */
    @Override
    CompletableFuture<Void> completion();
}
