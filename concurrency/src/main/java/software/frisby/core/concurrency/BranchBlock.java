package software.frisby.core.concurrency;

/**
 * Provides a block that routes posted items to one of several configured targets based on predicate
 * matching. Routing targets and their predicates are configured at build time through the builder;
 * no runtime {@code linkTo} method is exposed.
 * <p>
 * On each {@link #post}, predicates are evaluated in the order they were declared to
 * {@link BranchBlockBuilder#when}. The first predicate that matches determines the target; evaluation
 * stops immediately. If no predicate matches, the item is forwarded to the target supplied to
 * {@link BranchBlockBuilder#otherwise}.
 * <p>
 * This block does not perform any internal buffering. The {@code post} operation is synchronous and
 * will block the caller until the posted item reaches a new async boundary in a downstream target.
 *
 * <pre>{@code
 * BranchBlock<Order> router = BranchBlock.<Order>builder()
 *         .when(order -> order.priority() == HIGH,   urgentQueue)
 *         .when(order -> order.priority() == NORMAL, normalQueue)
 *         .otherwise(bulkQueue)
 *         .build();
 * }</pre>
 *
 * @param <T> The type of elements routed by this block.
 * @see BranchBlockBuilder
 */
public interface BranchBlock<T> extends Target<T> {
    /**
     * Returns a new builder that will construct a new instance of {@link BranchBlock}.
     *
     * @param <T> The type of elements routed by the block.
     * @return A new {@link BranchBlockBuilder} instance.
     */
    static <T> BranchBlockBuilder<T> builder() {
        return new DefaultBranchBlockBuilder<>();
    }

    /**
     * Returns a new builder that will construct a new instance of {@link BranchBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of elements routed by the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link BranchBlockBuilder} instance.
     */
    static <T> BranchBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }
}
