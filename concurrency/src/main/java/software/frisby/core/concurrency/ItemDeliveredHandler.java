package software.frisby.core.concurrency;

/**
 * Receives a notification each time an item is successfully delivered from a source block to
 * a downstream target block.
 *
 * <p>This is a {@link FunctionalInterface} by design.  The method signature must not be
 * changed or augmented with additional abstract methods, as that would break lambda
 * implementations.</p>
 *
 * @param <T> The type of item delivered from the source block to the target block.
 */
@FunctionalInterface
public interface ItemDeliveredHandler<T> {
    /**
     * Called by the block after an item has been successfully delivered to the downstream target.
     *
     * @param source The block that delivered the item.
     * @param target The block that received the item.
     * @param item   The item that was delivered.
     */
    void onDelivered(Object source, Object target, T item);
}
