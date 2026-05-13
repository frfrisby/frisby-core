package software.frisby.core.concurrency;

/**
 * Builder mixin that adds {@code itemPostedHandler} and {@code itemDeliveredHandler}
 * configuration to a block builder.  Intended for synchronous blocks whose errors are thrown
 * directly to the calling thread rather than dispatched to an error handler.
 *
 * @param <T> The type of items posted to the block.
 * @param <R> The type of items delivered to the linked downstream target.
 * @param <B> The concrete builder type; used to return {@code this} for fluent chaining.
 * @see AsyncObservableBlockBuilder
 */
public interface ObservableBlockBuilder<T, R, B> {
    /**
     * Optional. Sets the handler that will receive a notification each time an item is posted
     * to the block. If not configured, no posted-item notifications are generated.
     *
     * @param handler The handler to notify when items are posted.
     * @return This builder, for method chaining.
     */
    B itemPostedHandler(ItemPostedHandler<T> handler);

    /**
     * Optional. Sets the handler that will receive a notification after each item is
     * successfully delivered to the linked downstream target. If not configured, no
     * delivered-item notifications are generated.
     *
     * @param handler The handler to notify on successful delivery.
     * @return This builder, for method chaining.
     */
    B itemDeliveredHandler(ItemDeliveredHandler<R> handler);
}
