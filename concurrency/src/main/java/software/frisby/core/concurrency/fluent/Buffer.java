package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Values;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Fluent builder for an async pass-through buffering stage.
 *
 * <p>Items posted to the buffer are held in an internal bounded queue and forwarded to the
 * downstream target on a background thread, decoupling producers from consumers.  Because the
 * input and output types are the same, {@code Buffer} acts as a {@code T → T} stage and is
 * typically placed at the head of a pipeline.  Backpressure is applied to posting threads when
 * the queue is full.</p>
 *
 * <pre>{@code
 * Pipeline<Message> pipeline = Pipeline.<Message>builder()
 *         .from(Buffer.of(Message.class)
 *                 .capacity(2048))
 *         .then(message -> process(message));
 * }</pre>
 *
 * @param <T> The type of items buffered by this stage.
 * @see Batch
 * @see PriorityBuffer
 */
public final class Buffer<T> implements PipelineStage<T, T>, ExecutorAwareStage, AsyncObservableBlockBuilder<T, T, Buffer<T>> {
    private Integer capacity;
    private Executor executor;
    private ErrorOccurredHandler<T> errorOccurredHandler;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private BufferBlock<T> block;

    private Buffer() {
    }

    /**
     * Returns a new {@code Buffer} builder.
     *
     * @param <T> The type of items to buffer.
     * @return A new {@code Buffer} instance.
     */
    public static <T> Buffer<T> of() {
        return new Buffer<>();
    }

    /**
     * Returns a new {@code Buffer} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to buffer.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Buffer} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Buffer<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Buffer} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to buffer.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Buffer} instance.
     * @throws NullValueException if {@code itemType} is null.
     */
    public static <T> Buffer<T> of(GenericType<T> itemType) {
        Values.notNull("itemType", itemType);
        return of(itemType.rawType());
    }

    /**
     * Returns a new {@code Buffer<List<T>>} builder for pipelines that carry lists as their
     * item type.  {@code ignored} is used solely for type inference; it is not stored.
     *
     * @param <T>     The element type of the lists to buffer.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Buffer<List<T>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Buffer<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
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
    public Buffer<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public Buffer<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public Buffer<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Buffer<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<T> toSource() {
        return toBufferBlock();
    }

    @Override
    public Target<T> toTarget() {
        return toBufferBlock();
    }

    @Override
    public void executor(Executor executor) {
        this.executor = executor;
    }

    private BufferBlock<T> toBufferBlock() {
        if (null == this.block) {
            BufferBlockBuilder<T> builder = BufferBlock.<T>builder()
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
