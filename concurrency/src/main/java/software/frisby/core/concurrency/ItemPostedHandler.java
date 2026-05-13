package software.frisby.core.concurrency;

/**
 * Receives a notification each time an item is posted to a block.
 *
 * <p>This is a {@link FunctionalInterface} by design.  The method signature must not be
 * changed or augmented with additional abstract methods, as that would break lambda
 * implementations.</p>
 *
 * @param <T> The type of item posted to the block.
 */
@FunctionalInterface
public interface ItemPostedHandler<T> {
    /**
     * Called by the block immediately after an item is posted.
     *
     * @param source   The block to which the item was posted.
     * @param item     The item that was passed to the block's {@link Target#post} method.
     * @param accepted {@code true} if the block accepted the item; {@code false} if it was
     *                 rejected (for example, because the queue was full or the block was
     *                 already completed).
     */
    void onPosted(Object source, T item, boolean accepted);
}


