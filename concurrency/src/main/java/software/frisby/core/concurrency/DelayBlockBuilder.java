package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Builder for constructing a {@link DelayBlock}.  Obtain an instance via
 * {@link DelayBlock#builder()}.
 *
 * @param <T> The type of items held and forwarded by the block.
 */
public interface DelayBlockBuilder<T> extends AsyncObservableBlockBuilder<T, T, DelayBlockBuilder<T>> {
    /**
     * Sets the {@link Executor} that will run the worker thread delivering delayed items to the
     * downstream target.  Any {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     *
     * @param executor The executor that will execute the worker task.
     * @return This builder, for method chaining.
     */
    DelayBlockBuilder<T> executor(Executor executor);

    /**
     * Sets a fixed delay that will be applied to every item posted to the block.
     *
     * @param delay The duration each item will be held before being forwarded to the linked
     *              target; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException            if {@code delay} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code delay} is not positive.
     */
    DelayBlockBuilder<T> delay(Duration delay);

    /**
     * Sets a function that computes a per-item delay.  The function receives each posted item
     * and returns the duration for which it should be held before being forwarded.
     *
     * <pre>{@code
     * builder.delay(task -> task.scheduledDelay())
     * }</pre>
     *
     * @param delayFunction The function that computes the delay for each posted item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code delayFunction} is null.
     */
    DelayBlockBuilder<T> delay(Function<T, Duration> delayFunction);

    /**
     * Optional. Sets the maximum number of items the internal delay queue can hold before backpressure
     * is applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    DelayBlockBuilder<T> capacity(int capacity);

    /**
     * Returns a new {@link DelayBlock} configured by this builder.
     *
     * @return A new {@link DelayBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no executor or delay has been configured.
     */
    DelayBlock<T> build();
}
