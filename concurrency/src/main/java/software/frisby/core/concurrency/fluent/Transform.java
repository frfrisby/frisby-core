package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Values;

import java.util.List;
import java.util.function.Function;

/**
 * Fluent builder for a synchronous transformation stage.
 *
 * <p>Each item posted to this stage is converted by a caller-supplied function and the result
 * forwarded to the downstream target immediately on the posting thread.  A {@code null} result
 * from the transform function is silently dropped and nothing is forwarded.  Acts as a
 * {@code T → R} stage.</p>
 *
 * <pre>{@code
 * Pipeline<Order> pipeline = Pipeline.<Order>builder()
 *         .from(Buffer.of(Order.class))
 *         .then(Transform.of(Order.class, Invoice.class)
 *                 .transform(order -> invoiceService.create(order)))
 *         .then(invoice -> store(invoice));
 * }</pre>
 *
 * @param <T> The input type received by this stage.
 * @param <R> The output type produced by this stage.
 */
public final class Transform<T, R> implements PipelineStage<T, R>, ObservableBlockBuilder<T, R, Transform<T, R>> {
    private Function<T, R> transformFunction;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<R> itemDeliveredHandler;

    private TransformBlock<T, R> block;

    private Transform() {
    }

    /**
     * Returns a new {@code Transform} builder.
     *
     * @param <T> The input type.
     * @param <R> The output type.
     * @return A new {@code Transform} instance.
     */
    public static <T, R> Transform<T, R> of() {
        return new Transform<>();
    }

    /**
     * Returns a new {@code Transform} builder.  Both parameters are used solely for type
     * inference at the call site; they are not stored.
     *
     * @param <T>               The input type.
     * @param <R>               The output type.
     * @param ignoredInputType  The input type class; used for inference only.
     * @param ignoredOutputType The output type class; used for inference only.
     * @return A new {@code Transform} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T, R> Transform<T, R> of(Class<T> ignoredInputType, Class<R> ignoredOutputType) {
        return of();
    }

    /**
     * Returns a new passthrough {@code Transform} builder where input and output types are
     * the same.  {@code itemType} is used solely for type inference; it is not stored.
     *
     * @param <T>      The item type.
     * @param itemType The item type class; used for inference only.
     * @return A new {@code Transform<T, T>} instance.
     */
    public static <T> Transform<T, T> of(Class<T> itemType) {
        return of(itemType, itemType);
    }

    /**
     * Returns a new {@code Transform} builder.  Both tokens are used solely for type
     * inference at the call site; they are not stored.  Use this overload when {@code T} or
     * {@code R} is itself a generic type (e.g. {@code List<Message>}) and a {@code Class}
     * literal cannot capture the full type.
     *
     * @param <T>               The input type.
     * @param <R>               The output type.
     * @param inputGenericType  The input generic type token; used for inference only.
     * @param outputGenericType The output generic type token; used for inference only.
     * @return A new {@code Transform} instance.
     * @throws NullValueException if {@code inputGenericType} or {@code outputGenericType} are null.
     */
    public static <T, R> Transform<T, R> of(GenericType<T> inputGenericType, GenericType<R> outputGenericType) {
        Values.notNull("inputGenericType", inputGenericType);
        Values.notNull("outputGenericType", outputGenericType);
        return of(inputGenericType.rawType(), outputGenericType.rawType());
    }

    /**
     * Returns a new passthrough {@code Transform} builder where input and output types are
     * the same generic type.  {@code genericItemType} is used solely for type inference; it
     * is not stored.
     *
     * @param <T>             The item type.
     * @param genericItemType The generic type token; used for inference only.
     * @return A new {@code Transform<T, T>} instance.
     * @throws NullValueException if {@code genericItemType} is null.
     */
    public static <T> Transform<T, T> of(GenericType<T> genericItemType) {
        Values.notNull("genericItemType", genericItemType);
        return of(genericItemType.rawType(), genericItemType.rawType());
    }

    /**
     * Returns a new {@code Transform<List<T>, List<R>>} builder for stages that transform
     * lists of one element type into lists of another.  Both parameters are used solely for
     * type inference; they are not stored.
     *
     * @param <T>               The element type of the input lists.
     * @param <R>               The element type of the output lists.
     * @param ignoredInputType  The input element type class; used for inference only.
     * @param ignoredOutputType The output element type class; used for inference only.
     * @return A new {@code Transform<List<T>, List<R>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T, R> Transform<List<T>, List<R>> ofLists(Class<T> ignoredInputType, Class<R> ignoredOutputType) {
        return of(
                new GenericType<>() {
                },
                new GenericType<>() {
                }
        );
    }

    /**
     * Returns a new passthrough {@code Transform<List<T>, List<T>>} builder for stages
     * that transform lists without changing the element type.  {@code itemType} is used
     * solely for type inference; it is not stored.
     *
     * @param <T>      The element type.
     * @param itemType The element type class; used for inference only.
     * @return A new {@code Transform<List<T>, List<T>>} instance.
     */
    public static <T> Transform<List<T>, List<T>> ofLists(Class<T> itemType) {
        return ofLists(itemType, itemType);
    }

    /**
     * Sets the function that transforms each received item into the output type.  If the
     * function returns {@code null}, the result is silently dropped and nothing is forwarded
     * downstream.
     *
     * @param transform The function that transforms each received item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code transform} is null.
     */
    public Transform<T, R> transform(Function<T, R> transform) {
        this.transformFunction = transform;
        return this;
    }

    @Override
    public Transform<T, R> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Transform<T, R> itemDeliveredHandler(ItemDeliveredHandler<R> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<R> toSource() {
        return toBlock();
    }

    @Override
    public Target<T> toTarget() {
        return toBlock();
    }

    private TransformBlock<T, R> toBlock() {
        if (null == this.block) {
            this.block = TransformBlock.<T, R>builder()
                    .transform(transformFunction)
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .build();
        }

        return this.block;
    }
}
