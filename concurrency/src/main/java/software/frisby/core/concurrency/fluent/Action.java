package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.ActionBlock;
import software.frisby.core.concurrency.GenericType;
import software.frisby.core.concurrency.ItemPostedHandler;
import software.frisby.core.concurrency.Target;

import java.util.List;
import java.util.function.Consumer;

/**
 * Fluent builder for a terminal action stage.
 *
 * <p>Each item received is passed to a caller-supplied {@link java.util.function.Consumer}.
 * The stage produces no output and cannot be linked to a downstream target, making it the
 * natural terminal stage of any {@link Pipeline}.</p>
 *
 * <pre>{@code
 * Pipeline<Message> pipeline = Pipeline.<Message>builder()
 *         .from(Buffer.of(Message.class))
 *         .then(Action.of(Message.class)
 *                 .action(message -> store(message)));
 * }</pre>
 *
 * @param <T> The type of items consumed by this stage.
 * @see Branch
 * @see Broadcast
 */
public final class Action<T> implements PipelineTarget<T> {
    private Consumer<T> consumer;
    private ItemPostedHandler<T> itemPostedHandler;

    private ActionBlock<T> block;

    private Action() {
    }

    /**
     * Returns a new {@code Action} builder.
     *
     * @param <T> The type of items to consume.
     * @return A new {@code Action} instance.
     */
    public static <T> Action<T> of() {
        return new Action<>();
    }

    /**
     * Returns a new {@code Action} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to consume.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Action} instance.
     */
    public static <T> Action<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Action} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to consume.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Action} instance.
     */
    public static <T> Action<T> of(GenericType<T> itemType) {
        return of(itemType.getRawType());
    }

    /**
     * Returns a new {@code Action<List<T>>} builder for pipelines whose items are lists.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The element type of the lists to consume.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Action<List<T>>} instance.
     */
    public static <T> Action<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Sets the consumer that will be invoked for each item received by the block.
     *
     * @param consumer The consumer to invoke for each item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code action} is null.
     */
    public Action<T> action(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    /**
     * Optional. Sets the handler that will receive a notification each time an item is posted
     * to the block. If not configured, no posted-item notifications are generated.
     *
     * @param handler The handler to notify when items are posted.
     * @return This builder, for method chaining.
     */
    public Action<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Target<T> toTarget() {
        if (null == this.block) {
            this.block = toBlock();
        }

        return this.block;
    }

    private ActionBlock<T> toBlock() {
        return ActionBlock.<T>builder()
                .itemPostedHandler(itemPostedHandler)
                .action(consumer)
                .build();
    }
}
