package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for a synchronous pass-through side effect stage.
 *
 * <p>Each item posted to this stage is passed to a caller-supplied {@link Consumer} as a
 * side effect, then forwarded to the downstream target <em>unchanged</em> on the same
 * thread.  If the consumer throws, the exception propagates to the posting thread and
 * the item is not forwarded.</p>
 *
 * <p>Typical uses include persisting items to a database, emitting audit events, or
 * recording metrics.  For slow or I/O-bound consumers, place an async {@link Buffer}
 * upstream to decouple the posting thread:</p>
 *
 * <pre>{@code
 * // Synchronous — consumer is fast; Tap is the pipeline head
 * Pipeline<Order> pipeline = Pipeline.<Order>builder()
 *         .from(Tap.of(Order.class)
 *                 .consumer(order -> metrics.increment("orders.received")))
 *         .to(order -> fulfill(order));
 *
 * // Decoupled — consumer is slow (e.g. database write); Buffer dequeues on a worker thread
 * Pipeline<Order> pipeline = Pipeline.<Order>builder()
 *         .executor(executor)
 *         .from(Buffer.of(Order.class))
 *         .then(Tap.of(Order.class)
 *                 .consumer(order -> db.save(order)))
 *         .to(order -> fulfill(order));
 * }</pre>
 *
 * @param <T> The type of items passed through this stage.
 */
public final class Tap<T> implements PipelineStage<T, T>, ObservableBlockBuilder<T, T, Tap<T>> {
    private Consumer<T> consumer;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private TapBlock<T> block;

    private Tap() {
    }

    /**
     * Returns a new {@code Tap} builder.
     *
     * @param <T> The item type.
     * @return A new {@code Tap} instance.
     */
    public static <T> Tap<T> of() {
        return new Tap<>();
    }

    /**
     * Returns a new {@code Tap} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The item type.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Tap<T>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Tap<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Tap} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>     The item type.
     * @param ignored The generic type token; used for inference only.
     * @return A new {@code Tap<T>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Tap<T> of(GenericType<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Tap<List<T>>} builder for pipelines whose items are lists.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The element type of the lists to observe.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Tap<List<T>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Tap<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Sets the consumer that will be invoked for each received item as a side effect.
     * The item is forwarded downstream unchanged after the consumer returns.  If the
     * consumer throws, the exception propagates to the calling thread and the item
     * is not forwarded.
     *
     * @param consumer The side effect consumer.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code consumer} is null.
     */
    public Tap<T> consumer(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public Tap<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Tap<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<T> toSource() {
        return toBlock();
    }

    @Override
    public Target<T> toTarget() {
        return toBlock();
    }

    private TapBlock<T> toBlock() {
        if (null == this.block) {
            this.block = TapBlock.<T>builder()
                    .consumer(consumer)
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .build();
        }

        return this.block;
    }
}

