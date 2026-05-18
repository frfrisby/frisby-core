package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Builder for constructing a {@link GroupBlock}.  Obtain an instance via
 * {@link GroupBlock#builder()}.
 *
 * @param <T> The type of items grouped by the block.
 * @param <K> The type of the key that uniquely identifies each group.
 */
public interface GroupBlockBuilder<T, K> extends AsyncObservableBlockBuilder<T, List<T>, GroupBlockBuilder<T, K>> {
    /**
     * Sets the {@link Executor} that will run the worker thread grouping and delivering items
     * to the downstream target.  Any {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     *
     * @param executor The executor that will execute the worker task.
     * @return This builder, for method chaining.
     */
    GroupBlockBuilder<T, K> executor(Executor executor);

    /**
     * Sets the function that extracts a group key from each posted item.  Items that return
     * equal keys are accumulated into the same group.
     *
     * @param groupingFunction The function that extracts a group key from each posted item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code groupingFunction} is null.
     */
    GroupBlockBuilder<T, K> groupingFunction(Function<T, K> groupingFunction);

    /**
     * Optional. Sets the observer that will be notified when groups are created or modified,
     * allowing it to override the default retention policy for individual groups. If not
     * configured, all groups are held and flushed under the default timeout and idle-timeout
     * policy.
     *
     * @param observer The observer to notify when groups change.
     * @return This builder, for method chaining.
     */
    GroupBlockBuilder<T, K> groupObserver(GroupObserver<T, K> observer);

    /**
     * Optional. Sets the maximum time any group will be held before it is flushed to the downstream
     * target, regardless of the idle-timeout or size limit.  Defaults to
     * {@code Duration.ofSeconds(10)} if not set.
     *
     * @param timeout The maximum group hold time; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is not positive.
     */
    GroupBlockBuilder<T, K> timeout(Duration timeout);

    /**
     * Optional. Sets the maximum time that may elapse between successive items arriving in a group before
     * the group is flushed.  Defaults to {@code Duration.ofSeconds(5)} if not set.
     *
     * @param idleTimeout The maximum idle time between items within a group; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code idleTimeout} is not positive.
     */
    GroupBlockBuilder<T, K> idleTimeout(Duration idleTimeout);

    /**
     * Optional. Sets the maximum number of items a single group may accumulate before it is flushed to
     * the downstream target immediately, bypassing the timeout and idle-timeout windows.  When
     * a group reaches this size it is released as if a {@link GroupObserver} had returned
     * {@link Retention#RELEASE}.  Defaults to {@code 128} if not set.
     *
     * @param maxGroupSize The maximum number of items per group; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code maxGroupSize} is less than {@code 1}.
     */
    GroupBlockBuilder<T, K> maxGroupSize(int maxGroupSize);

    /**
     * Optional. Sets the maximum number of items the internal queue can hold before backpressure is
     * applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    GroupBlockBuilder<T, K> capacity(int capacity);

    /**
     * Returns a new {@link GroupBlock} configured by this builder.
     *
     * @return A new {@link GroupBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no executor or grouping function has been configured.
     */
    GroupBlock<T> build();
}
