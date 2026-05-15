package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;

import java.util.List;

/**
 * Fluent builder for a synchronous list-expansion stage.
 *
 * <p>Each {@code List<T>} posted to this block is unwrapped and its elements forwarded to
 * the downstream target one by one, in iteration order.  Acts as a {@code List<T> → T} stage
 * and is the natural inverse of {@link Batch}.</p>
 *
 * <pre>{@code
 * Pipeline<Message> pipeline = Pipeline.<Message>builder()
 *         .from(Buffer.of(Message.class))
 *         .then(Batch.of(Message.class).batchSize(50))
 *         .then(Transform.of(Message.class, ProcessedMessage.class)
 *                 .transform(batch -> enrich(batch)))   // List<Message> → List<ProcessedMessage>
 *         .then(Expand.of(ProcessedMessage.class))       // List<ProcessedMessage> → ProcessedMessage
 *         .then(msg -> store(msg));
 * }</pre>
 *
 * @param <T> The element type produced after expanding each posted list.
 * @see Batch
 */
public final class Expand<T> implements PipelineStage<List<T>, T>, ObservableBlockBuilder<List<T>, T, Expand<T>> {
    private ItemPostedHandler<List<T>> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private ExpandBlock<T> block;

    private Expand() {
    }

    /**
     * Returns a new {@code Expand} builder.
     *
     * @param <T> The element type produced after expansion.
     * @return A new {@code Expand} instance.
     */
    public static <T> Expand<T> of() {
        return new Expand<>();
    }

    /**
     * Returns a new {@code Expand} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The element type produced after expansion.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Expand} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Expand<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Expand} builder.  {@code elementType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>         The element type produced after expansion.
     * @param elementType The generic type token; used for inference only.
     * @return A new {@code Expand} instance.
     */
    public static <T> Expand<T> of(GenericType<T> elementType) {
        return of(elementType.getRawType());
    }

    @Override
    public Expand<T> itemPostedHandler(ItemPostedHandler<List<T>> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Expand<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<T> toSource() {
        if (null == this.block) {
            this.block = toBlock();
        }

        return this.block;
    }

    @Override
    public Target<List<T>> toTarget() {
        if (null == this.block) {
            this.block = toBlock();
        }

        return block;
    }

    ExpandBlock<T> toBlock() {
        return ExpandBlock.<T>builder()
                .itemPostedHandler(itemPostedHandler)
                .itemDeliveredHandler(itemDeliveredHandler)
                .build();
    }
}
