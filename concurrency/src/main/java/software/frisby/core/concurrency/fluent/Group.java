package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Fluent builder for an async grouping stage.
 *
 * <p>Items are accumulated into named groups identified by a caller-supplied key function.
 * Each group is flushed downstream as a {@code List<T>} when a maximum timeout, an idle
 * timeout between successive items, or a maximum group size is reached.  Acts as a
 * {@code T → List<T>} stage.  Groups that share the same key are always emitted as a single
 * coherent batch.</p>
 *
 * <pre>{@code
 * Pipeline<Event> pipeline = Pipeline.<Event>builder()
 *         .from(Buffer.of(Event.class))
 *         .then(Group.of(Event.class, String.class)
 *                 .groupingFunction(Event::customerId)
 *                 .timeout(Duration.ofSeconds(10))
 *                 .maxGroupSize(100))
 *         .then(events -> processGroup(events));
 * }</pre>
 *
 * @param <T> The type of items accumulated into groups.
 * @param <K> The type of the grouping key extracted from each item.
 * @see Batch
 */
public final class Group<T, K> implements PipelineStage<T, List<T>>, ExecutorAwareStage, AsyncObservableBlockBuilder<T, List<T>, Group<T, K>> {
    private Function<T, K> groupingFunction;
    private Duration timeout;
    private Duration idleTimeout;
    private Integer maxGroupSize;
    private Integer capacity;
    private GroupObserver<T, K> observer;
    private Executor executor;
    private ErrorOccurredHandler<List<T>> errorOccurredHandler;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<List<T>> itemDeliveredHandler;

    private GroupBlock<T, K> block;

    private Group() {
    }

    /**
     * Returns a new {@code Group} builder.
     *
     * @param <T> The type of items to group.
     * @param <K> The type of the grouping key.
     * @return A new {@code Group} instance.
     */
    public static <T, K> Group<T, K> of() {
        return new Group<>();
    }

    /**
     * Returns a new {@code Group} builder.  Both parameters are used solely for type
     * inference at the call site; they are not stored.
     *
     * @param <T>             The type of items to group.
     * @param <K>             The type of the grouping key.
     * @param ignoredItemType The item type class; used for inference only.
     * @param ignoredKeyType  The key type class; used for inference only.
     * @return A new {@code Group} instance.
     */
    public static <T, K> Group<T, K> of(Class<T> ignoredItemType, Class<K> ignoredKeyType) {
        return of();
    }

    /**
     * Returns a new {@code Group} builder.  Both parameters are used solely for type
     * inference at the call site; they are not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to group.
     * @param <K>      The type of the grouping key.
     * @param itemType The item generic type token; used for inference only.
     * @param keyType  The key type class; used for inference only.
     * @return A new {@code Group} instance.
     */
    public static <T, K> Group<T, K> of(GenericType<T> itemType, Class<K> keyType) {
        return of(itemType.getRawType(), keyType);
    }

    /**
     * Returns a new {@code Group} builder.  Both tokens are used solely for type inference
     * at the call site; they are not stored.  Use this overload when both {@code T} and
     * {@code K} are themselves generic types and {@code Class} literals cannot capture their
     * full types.
     *
     * @param <T>      The type of items to group.
     * @param <K>      The type of the grouping key.
     * @param itemType The item generic type token; used for inference only.
     * @param keyType  The key generic type token; used for inference only.
     * @return A new {@code Group} instance.
     */
    public static <T, K> Group<T, K> of(GenericType<T> itemType, GenericType<K> keyType) {
        return of(itemType.getRawType(), keyType.getRawType());
    }

    /**
     * Sets the function that extracts a group key from each posted item.  Items that return
     * equal keys are accumulated into the same group.
     *
     * @param groupingFunction The function that extracts a group key from each posted item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code groupingFunction} is null.
     */
    public Group<T, K> groupingFunction(Function<T, K> groupingFunction) {
        this.groupingFunction = groupingFunction;
        return this;
    }

    /**
     * Optional. Sets the maximum time any group will be held before it is flushed to the downstream
     * target, regardless of the idle-timeout or size limit.  Defaults to
     * {@code Duration.ofSeconds(10)} if not set.
     *
     * @param timeout The maximum group hold time; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is not positive.
     */
    public Group<T, K> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Optional. Sets the maximum time that may elapse between successive items arriving in a group before
     * the group is flushed.  Defaults to {@code Duration.ofSeconds(5)} if not set.
     *
     * @param idleTimeout The maximum idle time between items within a group; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code idleTimeout} is not positive.
     */
    public Group<T, K> idleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

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
    public Group<T, K> maxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
        return this;
    }

    /**
     * Optional. Sets the maximum number of items the internal queue can hold before backpressure is
     * applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    public Group<T, K> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Optional. Sets the observer that will be notified when groups are created or modified,
     * allowing it to override the default retention policy for individual groups. If not
     * configured, all groups are held and flushed under the default timeout and idle-timeout
     * policy.
     *
     * @param observer The observer to notify when groups change.
     * @return This builder, for method chaining.
     */
    public Group<T, K> groupObserver(GroupObserver<T, K> observer) {
        this.observer = observer;
        return this;
    }

    @Override
    public Group<T, K> errorOccurredHandler(ErrorOccurredHandler<List<T>> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public Group<T, K> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Group<T, K> itemDeliveredHandler(ItemDeliveredHandler<List<T>> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<List<T>> toSource() {
        return toBlock();
    }

    @Override
    public Target<T> toTarget() {
        return toBlock();
    }

    @Override
    public void executor(Executor executor) {
        this.executor = executor;
    }

    private GroupBlock<T, K> toBlock() {
        if (null == block) {
            GroupBlockBuilder<T, K> builder = GroupBlock.<T, K>builder()
                    .groupingFunction(groupingFunction)
                    .groupObserver(observer)
                    .executor(executor)
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .errorOccurredHandler(errorOccurredHandler);

            if (null != capacity) {
                builder.capacity(capacity);
            }

            if (null != maxGroupSize) {
                builder.maxGroupSize(maxGroupSize);
            }

            if (null != timeout) {
                builder.timeout(timeout);
            }

            if (null != idleTimeout) {
                builder.idleTimeout(idleTimeout);
            }

            this.block = builder.build();
        }

        return this.block;
    }
}
