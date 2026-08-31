package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A synchronous pipeline stage that unpacks a {@code List<T>} received from an upstream source
 * into individual {@code T} items and forwards each one to the linked downstream target.
 *
 * <p>{@code ExpandBlock} is the direct inverse of {@link BatchBlock}: where {@code BatchBlock}
 * collects individual items into lists, {@code ExpandBlock} unpacks lists back into individual
 * items.  It processes items on the calling thread with no internal buffer and no executor.</p>
 *
 * <p>Null lists are rejected ({@link #post} returns {@code false}).  Empty lists are accepted
 * ({@link #post} returns {@code true}) with nothing forwarded.  Null elements within the list
 * are silently skipped.</p>
 *
 * <p>Typical pipeline composition:</p>
 * <pre>
 * SourceBlock&lt;Message&gt; (batch supplier)
 *     &#x2192; ExpandBlock&lt;Message&gt;
 *     &#x2192; RouterBlock&lt;Message&gt;
 *     &#x2192; BufferBlock&lt;Message&gt; (&#xD7;N)
 * </pre>
 *
 * @param <T> The type of individual elements unpacked from each received list.
 * @see BatchBlock
 * @see ExpandBlockBuilder
 */
public interface ExpandBlock<T> extends Stage<List<T>, T> {
    /**
     * Returns a new builder for constructing an {@link ExpandBlock}.
     *
     * @param <T> The type of individual elements unpacked from each received list.
     * @return A new {@link ExpandBlockBuilder} instance.
     */
    static <T> ExpandBlockBuilder<T> builder() {
        return new DefaultExpandBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing an {@link ExpandBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of individual elements unpacked from each received list.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link ExpandBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> ExpandBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Posts a list to this block, unpacking it inline on the calling thread — this block itself
     * never blocks.  Each non-null element is forwarded, one at a time, to the linked downstream
     * target via its own {@link Target#post(Object, Duration)}, each call waiting up to
     * {@code timeout} if the target blocks — for example, an asynchronous block whose queue is
     * full.  Each element gets its own independent {@code timeout} window, so the worst-case
     * total wait for this method is up to {@code timeout} multiplied by the number of elements in
     * the list, not a single shared budget across the whole list.
     *
     * @param list    The list to post.
     * @param timeout The maximum time to wait for the downstream target to accept each element.
     * @return {@code true} if every non-null element was accepted by the downstream target
     * before its own {@code timeout} window elapsed; {@code false} if this block has already
     * been completed, {@code list} is {@code null}, or at least one element failed to be
     * accepted within {@code timeout}.  An empty list returns {@code true} with nothing
     * forwarded.
     * @throws software.frisby.core.validation.NullValueException            if {@code timeout} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is negative.
     */
    @Override
    boolean post(List<T> list, Duration timeout);

    /**
     * Signals that no more lists will be posted to this block.  Since this block processes
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
