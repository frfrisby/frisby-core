package software.frisby.core.concurrency;

import java.time.Duration;
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
    @SuppressWarnings("java:S1172")
    static <T> TapBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Posts an item to this block.  The consumer is invoked inline on the calling thread — this
     * block itself never blocks.  The item is then forwarded to the linked downstream target via
     * its own {@link Target#post(Object, Duration)}.  That call waits up to {@code timeout} if
     * the target blocks — for example, an asynchronous block whose queue is full.
     *
     * @param item    The item to post.
     * @param timeout The maximum time to wait for the downstream target to accept the item.
     * @return {@code true} if the downstream target accepted the item before {@code timeout}
     * elapsed; {@code false} if this block has already been completed, {@code item} is
     * {@code null}, or {@code timeout} elapsed before the downstream target accepted it.
     * @throws software.frisby.core.validation.NullValueException            if {@code timeout} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is negative.
     */
    @Override
    boolean post(T item, Duration timeout);

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


