package software.frisby.core.concurrency;

import java.util.Comparator;
import java.util.concurrent.Executor;

/**
 * Builder for constructing a {@link PriorityBufferBlock}.  Obtain an instance via
 * {@link PriorityBufferBlock#builder()}.
 *
 * @param <T> The type of items held in the block.
 */
public interface PriorityBufferBlockBuilder<T> extends AsyncObservableBlockBuilder<T, T, PriorityBufferBlockBuilder<T>> {
    /**
     * Sets the {@link Executor} that will run the worker thread delivering items to the
     * downstream target.  Any {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     *
     * @param executor The executor that will execute the worker task.
     * @return This builder, for method chaining.
     */
    PriorityBufferBlockBuilder<T> executor(Executor executor);

    /**
     * Sets the comparator used to determine the delivery order of queued items.  The item
     * ranked first by the comparator is always delivered next.
     *
     * @param comparator The comparator that defines the priority ordering of items.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code comparator} is null.
     */
    PriorityBufferBlockBuilder<T> comparator(Comparator<T> comparator);

    /**
     * Optional. Sets the maximum number of items the internal priority queue can hold before backpressure
     * is applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    PriorityBufferBlockBuilder<T> capacity(int capacity);

    /**
     * Returns a new {@link PriorityBufferBlock} configured by this builder.
     *
     * @return A new {@link PriorityBufferBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no executor or comparator has been configured.
     */
    PriorityBufferBlock<T> build();
}
