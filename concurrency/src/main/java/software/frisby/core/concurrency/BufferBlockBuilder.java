package software.frisby.core.concurrency;

import java.util.concurrent.Executor;

/**
 * Builder for constructing a {@link BufferBlock}.  Obtain an instance via
 * {@link BufferBlock#builder()}.
 *
 * @param <T> The type of items held in the block.
 */
public interface BufferBlockBuilder<T> extends AsyncObservableBlockBuilder<T, T, BufferBlockBuilder<T>> {
    /**
     * Sets the {@link Executor} that will run the worker thread delivering items to the
     * downstream target.  Any {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     *
     * @param executor The executor that will execute the worker task.
     * @return This builder, for method chaining.
     */
    BufferBlockBuilder<T> executor(Executor executor);

    /**
     * Optional. Sets the maximum number of items the internal queue can hold before backpressure is
     * applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    BufferBlockBuilder<T> capacity(int capacity);

    /**
     * Returns a new {@link BufferBlock} configured by this builder.
     *
     * @return A new {@link BufferBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no executor has been configured.
     */
    BufferBlock<T> build();
}
