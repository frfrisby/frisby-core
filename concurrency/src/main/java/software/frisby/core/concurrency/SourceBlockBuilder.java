package software.frisby.core.concurrency;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Builder for constructing a {@link SourceBlock}.  Obtain an instance via
 * {@link SourceBlock#builder()}.
 *
 * @param <T> The type of items produced by the block.
 */
public interface SourceBlockBuilder<T> {
    /**
     * Sets the {@link Executor} that will run the worker thread driving this block.  Any
     * {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     *
     * @param executor The executor that will execute the worker task.
     * @return This builder, for method chaining.
     */
    SourceBlockBuilder<T> executor(Executor executor);

    /**
     * Sets the single-item supplier that will be invoked on each iteration to produce one item.
     * The supplier should block internally until an item is available (for example, via
     * {@link java.util.concurrent.BlockingQueue#take()}).
     *
     * <p>If the supplier returns {@code null}, the iteration is treated as a no-op — nothing
     * is forwarded to the downstream target and the supplier is invoked again immediately on
     * the next iteration.  This matches the behavior of {@link #batchSupplier} when it returns
     * {@code null} or an empty list.</p>
     *
     * <p>Only one supplier mode may be configured per builder.  Calling this method after
     * {@link #batchSupplier} has already been called (or vice versa) throws
     * {@link IllegalStateException} immediately.</p>
     *
     * @param supplier The single-item supplier.
     * @return This builder, for method chaining.
     * @throws IllegalStateException if a supplier has already been configured on this builder.
     */
    SourceBlockBuilder<T> supplier(Supplier<T> supplier);

    /**
     * Sets the batch supplier that will be invoked on each iteration to produce zero or more
     * items.  Each item in the returned list is forwarded individually to the downstream target.
     *
     * <p>A {@code null} return value or an empty list is treated as a no-op — nothing is
     * forwarded and the supplier is invoked again immediately on the next iteration.  This is
     * the standard pattern for a supplier that needs to signal "nothing available yet" without
     * blocking (for example, a polling database query that returns an empty result set).</p>
     *
     * <p>The supplier is responsible for its own rate-limiting.  If the underlying data source
     * can be exhausted, the supplier should block internally between polls (for example, by
     * parking the thread) to avoid busy-spinning once all data has been produced.  The worker
     * thread exits cleanly when the executor is shut down.</p>
     *
     * <p>Only one supplier mode may be configured per builder.  Calling this method after
     * {@link #supplier} has already been called (or vice versa) throws
     * {@link IllegalStateException} immediately.</p>
     *
     * @param supplier The batch supplier that returns zero or more items per call.
     * @return This builder, for method chaining.
     * @throws IllegalStateException if a supplier has already been configured on this builder.
     */
    SourceBlockBuilder<T> batchSupplier(Supplier<List<T>> supplier);

    /**
     * Optional. Sets the handler that will receive a notification after each item is
     * successfully delivered to the linked downstream target. If not configured, no
     * delivered-item notifications are generated.
     *
     * @param handler The handler to notify on successful delivery.
     * @return This builder, for method chaining.
     */
    SourceBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler);

    /**
     * Optional. Sets the handler that will receive a notification when an error occurs while
     * delivering an item to the linked downstream target. If not configured, delivery errors
     * are silently swallowed and the block continues processing subsequent items.
     *
     * @param handler The handler to notify when a delivery error occurs.
     * @return This builder, for method chaining.
     */
    SourceBlockBuilder<T> errorOccurredHandler(ErrorOccurredHandler<T> handler);

    /**
     * Optional. Sets the concurrency policy that controls how many threads invoke the supplier
     * concurrently and, for adaptive policies, how the active thread count scales up and down in
     * response to supplier results.
     *
     * <p>When not configured, the block runs a single thread with no throttling — equivalent to
     * {@link SourceConcurrencyPolicy#fixed(int) SourceConcurrencyPolicy.fixed(1)}.</p>
     *
     * <p>When {@link SourceConcurrencyPolicy#maxThreads()} is greater than {@code 1} the
     * configured supplier will be called concurrently by multiple threads.  The supplier must be
     * thread-safe, and the first downstream stage should typically be an async block that owns an internal
     * queue — such as {@link BatchBlock}, {@link BufferBlock}, {@link DelayBlock}, {@link GroupBlock},
     * or {@link PriorityBufferBlock} — so that concurrent posts are absorbed safely without blocking
     * the supplier threads.</p>
     *
     * @param policy The concurrency policy to apply.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code policy} is null.
     */
    SourceBlockBuilder<T> concurrencyPolicy(SourceConcurrencyPolicy policy);

    /**
     * Returns a new {@link SourceBlock} configured by this builder.
     *
     * @return A new {@link SourceBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no executor has been configured.
     * @throws IllegalStateException                              if neither {@link #supplier} nor {@link #batchSupplier} has
     *                                                            been configured.
     */
    SourceBlock<T> build();
}
