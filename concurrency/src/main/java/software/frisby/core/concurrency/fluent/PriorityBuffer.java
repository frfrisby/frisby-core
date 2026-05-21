package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Values;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Fluent builder for an async priority-ordered pass-through stage.
 *
 * <p>Items posted to the block are held in an internal priority queue and delivered to the
 * downstream target on a background thread in the order defined by the configured
 * {@link java.util.Comparator}.  Acts as a {@code T → T} stage; useful when items must be
 * processed in priority order rather than arrival order.</p>
 *
 * <pre>{@code
 * Pipeline<Task> pipeline = Pipeline.<Task>builder()
 *         .from(PriorityBuffer.of(Task.class)
 *                 .comparator(Comparator.comparingInt(Task::priority).reversed()))
 *         .then(task -> execute(task));
 * }</pre>
 *
 * @param <T> The type of items buffered and reordered by this stage.
 * @see Buffer
 */
public final class PriorityBuffer<T> implements PipelineStage<T, T>, ExecutorAwareStage, AsyncObservableBlockBuilder<T, T, PriorityBuffer<T>> {
    private Integer capacity;
    private Comparator<T> comparator;
    private Executor executor;
    private ErrorOccurredHandler<T> errorOccurredHandler;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private PriorityBufferBlock<T> block;

    private PriorityBuffer() {
    }

    /**
     * Returns a new {@code PriorityBuffer} builder.
     *
     * @param <T> The type of items to buffer.
     * @return A new {@code PriorityBuffer} instance.
     */
    public static <T> PriorityBuffer<T> of() {
        return new PriorityBuffer<>();
    }

    /**
     * Returns a new {@code PriorityBuffer} builder.  {@code ignored} is used solely for
     * type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to buffer.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code PriorityBuffer} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> PriorityBuffer<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code PriorityBuffer} builder.  {@code itemType} is used solely for
     * type inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to buffer.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code PriorityBuffer} instance.
     * @throws NullValueException if {@code itemType} is null.
     */
    public static <T> PriorityBuffer<T> of(GenericType<T> itemType) {
        Values.notNull("itemType", itemType);
        return of(itemType.getRawType());
    }

    /**
     * Returns a new {@code PriorityBuffer<List<T>>} builder for pipelines that carry lists
     * as their item type.  {@code ignored} is used solely for type inference; it is not
     * stored.
     *
     * @param <T>     The element type of the lists to buffer.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code PriorityBuffer<List<T>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> PriorityBuffer<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Optional. Sets the maximum number of items the internal queue can hold before backpressure is
     * applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     * @see PriorityBufferBlockBuilder#capacity(int)
     */
    public PriorityBuffer<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets the comparator used to determine the delivery order of queued items.  The item
     * ranked first by the comparator is always delivered next.
     *
     * @param comparator The comparator that defines the priority ordering of items.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code comparator} is null.
     */
    public PriorityBuffer<T> comparator(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    @Override
    public PriorityBuffer<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public PriorityBuffer<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public PriorityBuffer<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<T> toSource() {
        return toPriorityBufferBlock();
    }

    @Override
    public Target<T> toTarget() {
        return toPriorityBufferBlock();
    }

    @Override
    public void executor(Executor executor) {
        this.executor = executor;
    }

    private PriorityBufferBlock<T> toPriorityBufferBlock() {
        if (null == this.block) {
            PriorityBufferBlockBuilder<T> builder = PriorityBufferBlock.<T>builder()
                    .comparator(comparator)
                    .executor(executor)
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .errorOccurredHandler(errorOccurredHandler);

            if (null != capacity) {
                builder.capacity(capacity);
            }

            this.block = builder.build();
        }

        return this.block;
    }
}
