package software.frisby.core.concurrency;

/**
 * Receives a notification each time an item is posted to a block.
 *
 * <p>On asynchronous blocks ({@link BufferBlock}, {@link BatchBlock}, {@link GroupBlock},
 * {@link PriorityBufferBlock}, {@link DelayBlock}), this handler fires on the posting
 * thread immediately after the item is enqueued — it reports that the item was
 * <em>accepted</em>, not that it was <em>consumed</em>.  The item is then picked up and
 * processed by a separate worker thread, running concurrently and independently from
 * that point on.  This notification and the item's downstream processing race each
 * other, with no happens-before relationship between them.  Under load, a downstream
 * consumer can complete before this handler ever fires.  Do not treat this handler's
 * firing as a signal that downstream processing has started, let alone finished, for
 * that item.  If calling code needs to know the item was accepted independent of
 * delivery, synchronize on the handler's own firing — for example, by counting down a
 * dedicated latch from inside it.  Don't infer acceptance from a signal the downstream
 * consumer raises later.</p>
 *
 * <p>On synchronous blocks, delivery happens inline on the posting thread, so this
 * handler always fires before delivery to the downstream target completes for the same
 * item.</p>
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




