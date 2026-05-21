package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluent builder for a terminal conditional-routing stage.
 *
 * <p>Items are evaluated against a sequence of predicate-target pairs in declaration order.
 * The first matching predicate determines the {@link Pipeline} that receives the item; at most
 * one arm fires per item.  An {@code otherwise} target handles items that match no predicate.
 * Acts as a terminal, divergent stage — each arm is a self-contained pipeline and no shared
 * downstream fan-in is performed.</p>
 *
 * <pre>{@code
 * Pipeline<Order> pipeline = Pipeline.<Order>builder()
 *         .from(Buffer.of(Order.class))
 *         .to(Branch.of(Order.class)
 *                 .when(order -> order.isPriority(), priorityPipeline)
 *                 .otherwise(standardPipeline));
 * }</pre>
 *
 * @param <T> The type of items routed by this stage.
 * @see Broadcast
 * @see Router
 */
public final class Branch<T> implements PipelineTarget<T>, ObservableBlockBuilder<T, T, Branch<T>> {
    private final List<WhenCondition<T>> whenConditions;
    private Pipeline<T> otherwise;

    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private BranchBlock<T> block;

    private Branch() {
        this.whenConditions = new ArrayList<>();
    }

    /**
     * Returns a new {@code Branch} builder.
     *
     * @param <T> The type of items to route.
     * @return A new {@code Branch} instance.
     */
    public static <T> Branch<T> of() {
        return new Branch<>();
    }

    /**
     * Returns a new {@code Branch} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to route.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Branch} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Branch<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Branch} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to route.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Branch} instance.
     * @throws NullValueException if {@code itemType} is null.
     */
    public static <T> Branch<T> of(GenericType<T> itemType) {
        Values.notNull("itemType", itemType);
        return of(itemType.getRawType());
    }

    /**
     * Returns a new {@code Branch<List<T>>} builder for pipelines whose items are lists.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The element type of the lists to route.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Branch<List<T>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Branch<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Adds a conditional routing clause.  If {@code predicate} matches a posted item, that item
     * will be forwarded to {@code target}.  Clauses are evaluated in the order they are declared;
     * the first match wins.
     * <p>
     * This method may be called multiple times to add multiple conditional branches.
     *
     * @param predicate The predicate an item must satisfy to be forwarded to {@code target}.
     * @param target    The target that will receive items matching {@code predicate}.
     * @return This builder, for method chaining.
     * @throws NullValueException if {@code predicate} or
     *                            {@code target} is null.
     */
    public Branch<T> when(Predicate<T> predicate, Pipeline<T> target) {
        Values.notNull("predicate", predicate);
        Values.notNull("target", target);

        whenConditions.add(new WhenCondition<>(predicate, target));
        return this;
    }

    /**
     * Sets the default routing target.  Items that do not match any {@link #when} predicate will
     * be forwarded to {@code target}.  This method must be called exactly once before {@link software.frisby.core.concurrency.BranchBlockBuilder#build};
     * a second call throws {@link IllegalStateException}.
     *
     * @param target The default target that will receive items that do not match any when clause.
     * @return This builder, for method chaining.
     * @throws NullValueException    if {@code target} is null.
     * @throws IllegalStateException if {@code otherwise} has already been called on this builder.
     */
    public Branch<T> otherwise(Pipeline<T> target) {
        Values.notNull("target", target);

        if (null != this.otherwise) {
            throw new IllegalStateException(
                    "The 'BranchBlock' block already has an otherwise target configured.  The otherwise() method may only be called once."
            );
        }

        this.otherwise = target;
        return this;
    }

    @Override
    public Branch<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Branch<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Target<T> toTarget() {
        return toBlock();
    }

    private BranchBlock<T> toBlock() {
        if (null == this.block) {
            BranchBlockBuilder<T> builder = BranchBlock.<T>builder()
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .otherwise(otherwise);

            for (WhenCondition<T> condition : whenConditions) {
                builder.when(condition.predicate(), condition.target());
            }

            this.block = builder.build();
        }

        return this.block;
    }

    private record WhenCondition<T>(Predicate<T> predicate,
                                    Pipeline<T> target) {
    }
}
