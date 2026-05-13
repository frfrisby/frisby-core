package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Builder for constructing a {@link BatchBlock}.  Obtain an instance via
 * {@link BatchBlock#builder()}.
 *
 * @param <T> The type of items accumulated into batches by the block.
 */
public interface BatchBlockBuilder<T> extends AsyncObservableBlockBuilder<T, List<T>, BatchBlockBuilder<T>> {
    /**
     * Sets the {@link Executor} that will run the worker thread batching and delivering items
     * to the downstream target.  Any {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     *
     * @param executor The executor that will execute the worker task.
     * @return This builder, for method chaining.
     */
    BatchBlockBuilder<T> executor(Executor executor);

    /**
     * Optional. Sets the maximum number of items the internal queue can hold before backpressure is
     * applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    BatchBlockBuilder<T> capacity(int capacity);

    /**
     * Optional. Sets the maximum number of items to accumulate in a single batch before the batch is
     * flushed to the downstream target.  Defaults to {@code 128} if not set.
     *
     * @param batchSize The target batch size; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code batchSize} is less than {@code 1}.
     */
    BatchBlockBuilder<T> batchSize(int batchSize);

    /**
     * Optional. Sets the maximum time a batch will be held before being flushed to the downstream target,
     * even if it has not yet reached the configured batch size.  Defaults to
     * {@code Duration.ofSeconds(5)} if not set.
     *
     * @param timeout The maximum batch hold time; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is not positive.
     */
    BatchBlockBuilder<T> timeout(Duration timeout);

    /**
     * Returns a new {@link BatchBlock} configured by this builder.
     *
     * @return A new {@link BatchBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no executor has been configured.
     */
    BatchBlock<T> build();
}
