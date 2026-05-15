package software.frisby.core.concurrency;

/**
 * Provides a block that routes each posted item to exactly one of its pre-configured downstream
 * targets, selected by a {@link RoutingFunction}.  All targets are wired at build time through
 * {@link RouterBlockBuilder}; no runtime {@code linkTo} method is exposed.
 * <p>
 * By default, when no {@link RouterBlockBuilder#routingFunction routingFunction} is supplied,
 * items are distributed in round-robin order across all configured targets.
 * <p>
 * At least two targets must be configured before calling {@link RouterBlockBuilder#build}.  A
 * single-target block is not meaningful routing — wire the upstream block directly with
 * {@link Source#linkTo} instead.
 * <p>
 * This block does not perform any internal buffering.  The {@code post} operation is synchronous
 * and will block the caller until the posted item reaches a new async boundary in a downstream
 * target.
 *
 * <pre>{@code
 * // Round-robin across three workers (default):
 * RouterBlock<Task> router = RouterBlock.<Task>builder()
 *         .target(workerA)
 *         .target(workerB)
 *         .target(workerC)
 *         .build();
 *
 * // Sticky routing — all events for the same customerId go to the same worker:
 * RouterBlock<Order> router = RouterBlock.<Order>builder()
 *         .targets(workers)
 *         .sticky(Order::customerId)
 *         .build();
 *
 * // Custom routing function:
 * RouterBlock<Message> router = RouterBlock.<Message>builder()
 *         .target(highPriorityQueue)
 *         .target(normalQueue)
 *         .routingFunction(msg -> msg.priority() > 5 ? 0 : 1)
 *         .build();
 * }</pre>
 *
 * @param <T> The type of elements routed by this block.
 * @see RouterBlockBuilder
 */
public interface RouterBlock<T> extends Target<T> {
    /**
     * Returns a new builder that will construct a new instance of {@link RouterBlock}.
     *
     * @param <T> The type of elements routed by the block.
     * @return A new {@link RouterBlockBuilder} instance.
     */
    static <T> RouterBlockBuilder<T> builder() {
        return new DefaultRouterBlockBuilder<>();
    }

    /**
     * Returns a new builder that will construct a new instance of {@link RouterBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of elements routed by the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link RouterBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> RouterBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }
}
