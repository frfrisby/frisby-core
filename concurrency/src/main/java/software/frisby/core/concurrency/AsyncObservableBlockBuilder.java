package software.frisby.core.concurrency;

/**
 * Builder mixin that adds {@code itemPostedHandler}, {@code itemDeliveredHandler}, and
 * {@code errorOccurredHandler} configuration to a block builder.  Intended for asynchronous
 * blocks that process items on a dedicated background thread, where exceptions cannot
 * propagate to the posting thread and must be dispatched to an explicit error handler instead.
 *
 * @param <T> The type of items posted to the block.
 * @param <R> The type of items delivered to the linked downstream target.
 * @param <B> The concrete builder type; used to return {@code this} for fluent chaining.
 * @see ObservableBlockBuilder
 */
public interface AsyncObservableBlockBuilder<T, R, B> extends ObservableBlockBuilder<T, R, B> {
    /**
     * Optional. Sets the handler that will receive a programmatic callback when an error occurs
     * while delivering an item to the linked downstream target. If not configured, delivery
     * errors are still logged at {@code ERROR} level automatically — the handler is for cases
     * where the application also needs to react programmatically (for example, to increment a
     * metric or trigger a circuit-breaker).
     *
     * @param handler The handler to notify when a delivery error occurs.
     * @return This builder, for method chaining.
     */
    B errorOccurredHandler(ErrorOccurredHandler<R> handler);
}
