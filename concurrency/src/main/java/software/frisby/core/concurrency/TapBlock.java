package software.frisby.core.concurrency;

import java.util.concurrent.CompletableFuture;

/**
 * A synchronous pipeline stage that invokes a caller-supplied side effect consumer for each
 * received item and then forwards the same unchanged item to the linked downstream target.
 *
 * <p>{@code TapBlock} holds no internal queue and uses no executor.  Every item is passed to
 * the consumer and forwarded on the posting thread, with no intermediate buffering.  If the
 * consumer throws, the exception propagates to the caller and the item <strong>will not</strong>
 * be forwarded downstream.</p>
 *
 * <p>Common use cases include persisting items to a database, emitting audit events, or
 * recording metrics — any side effect where the item itself must continue downstream
 * unchanged.  For slow or I/O-bound consumers, place an async {@link BufferBlock} upstream
 * to decouple the posting thread from the consumer's execution.</p>
 *
 * <p>{@link #complete()} cascades immediately to the linked downstream target — there is
 * nothing to drain.</p>
 *
 * @param <T> The type of items received and forwarded by this block.
 * @see TapBlockBuilder
 */
public interface TapBlock<T> extends Stage<T, T> {
    /**
     * Returns a new builder for constructing a {@link TapBlock}.
     *
     * @param <T> The type of items received and forwarded.
     * @return A new {@link TapBlockBuilder} instance.
     */
    static <T> TapBlockBuilder<T> builder() {
        return new DefaultTapBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link TapBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items received and forwarded.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link TapBlockBuilder} instance.
     */
    static <T> TapBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Signals that no more items will be posted to this block.  Since this block processes
     * items inline, completion cascades immediately to the linked downstream target.  The
     * {@link #completion()} future resolves once the downstream target has also completed.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when {@link #complete()} has been called and the linked
     * downstream target has also completed.
     *
     * @return A {@link CompletableFuture} that resolves when the downstream target is complete.
     */
    @Override
    CompletableFuture<Void> completion();
}


