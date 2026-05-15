package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Fluent builder for an async batching stage.
 *
 * <p>Individual items posted to the block are accumulated in an internal queue and forwarded
 * downstream as a {@code List<T>} when either the configured batch size is reached or the
 * flush timeout elapses, whichever comes first.  Acts as a {@code T → List<T>} stage and is
 * commonly paired with {@link Expand} to break batches back into individual items further
 * downstream.</p>
 *
 * <pre>{@code
 * Pipeline<Message> pipeline = Pipeline.<Message>builder()
 *         .from(Buffer.of(Message.class))
 *         .then(Batch.of(Message.class)
 *                 .batchSize(50)
 *                 .timeout(Duration.ofSeconds(2)))
 *         .then(batch -> persist(batch));
 * }</pre>
 *
 * @param <T> The type of items accumulated into batches.
 * @see Expand
 * @see Group
 */
public final class Batch<T> implements PipelineStage<T, List<T>>, ExecutorAwareStage, AsyncObservableBlockBuilder<T, List<T>, Batch<T>> {
    private Integer capacity;
    private Integer batchSize;
    private Duration timeout;
    private Executor executor;
    private ErrorOccurredHandler<List<T>> errorOccurredHandler;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<List<T>> itemDeliveredHandler;

    private BatchBlock<T> block;

    private Batch() {
    }

    /**
     * Returns a new {@code Batch} builder.
     *
     * @param <T> The type of items to batch.
     * @return A new {@code Batch} instance.
     */
    public static <T> Batch<T> of() {
        return new Batch<>();
    }

    /**
     * Returns a new {@code Batch} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to batch.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Batch} instance.
     */
    public static <T> Batch<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Batch} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to batch.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Batch} instance.
     */
    public static <T> Batch<T> of(GenericType<T> itemType) {
        return of(itemType.getRawType());
    }

    /**
     * Sets the maximum number of items the internal queue can hold before backpressure is
     * applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     * @see BufferBlockBuilder#capacity(int)
     */
    public Batch<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the maximum number of items to accumulate in a single batch before the batch is
     * flushed to the downstream target.  Defaults to {@code 128} if not set.
     *
     * @param batchSize The target batch size; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code batchSize} is less than {@code 1}.
     */
    public Batch<T> batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Sets the maximum time a batch will be held before being flushed to the downstream target,
     * even if it has not yet reached the configured batch size.  Defaults to
     * {@code Duration.ofSeconds(5)} if not set.
     *
     * @param timeout The maximum batch hold time; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is not positive.
     */
    public Batch<T> timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public Batch<T> errorOccurredHandler(ErrorOccurredHandler<List<T>> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public Batch<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Batch<T> itemDeliveredHandler(ItemDeliveredHandler<List<T>> handler) {
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

    private BatchBlock<T> toBlock() {
        if (null == this.block) {
            BatchBlockBuilder<T> builder = BatchBlock.<T>builder()
                    .executor(executor)
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .errorOccurredHandler(errorOccurredHandler);

            if (null != capacity) {
                builder.capacity(capacity);
            }

            if (null != batchSize) {
                builder.batchSize(batchSize);
            }

            if (null != timeout) {
                builder.timeout(timeout);
            }

            this.block = builder.build();
        }

        return this.block;
    }
}
