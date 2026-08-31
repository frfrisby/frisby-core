package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * Provides a block that accepts a single posted item and delivers it to every pre-configured
 * downstream target.  All targets are wired at build time through {@link BroadcastBlockBuilder};
 * no runtime {@code linkTo} method is exposed.
 * <p>
 * At least two targets must be configured before calling {@link BroadcastBlockBuilder#build}.
 * Broadcasting to a single target is not meaningful — wire the upstream block directly with
 * {@link Source#linkTo} instead.
 * <p>
 * This block does not perform any internal buffering.  The {@code post} operation is synchronous
 * and will block the caller until the item has been offered to every downstream target.  Callers
 * that need to decouple the posting thread from downstream latency should place a
 * {@link BufferBlock} in front of the broadcast block.
 * <p>
 * An optional cloning function may be configured via
 * {@link BroadcastBlockBuilder#cloningFunction(UnaryOperator)}.  When provided, a fresh copy of
 * the item is produced for each target before delivery, ensuring that no two targets share a
 * reference to the same mutable object.  When omitted, the original item reference is passed to
 * every target unchanged — appropriate for pipelines that carry immutable messages.
 *
 * <pre>{@code
 * // Broadcast to three consumers (immutable messages — shared reference):
 * BroadcastBlock<Event> broadcast = BroadcastBlock.<Event>builder()
 *         .target(consumerA)
 *         .target(consumerB)
 *         .target(consumerC)
 *         .build();
 *
 * // Broadcast with deep-copy cloning (mutable objects):
 * BroadcastBlock<MutableRecord> broadcast = BroadcastBlock.<MutableRecord>builder()
 *         .targets(List.of(consumerA, consumerB, consumerC))
 *         .cloningFunction(MutableRecord::copy)
 *         .build();
 * }</pre>
 *
 * @param <T> The type of elements broadcast by this block.
 * @see BroadcastBlockBuilder
 */
public interface BroadcastBlock<T> extends Target<T> {
    /**
     * Returns a new builder that will construct a new instance of {@link BroadcastBlock}.
     *
     * @param <T> The type of elements broadcast by the block.
     * @return A new {@link BroadcastBlockBuilder} instance.
     */
    static <T> BroadcastBlockBuilder<T> builder() {
        return new DefaultBroadcastBlockBuilder<>();
    }

    /**
     * Returns a new builder that will construct a new instance of {@link BroadcastBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of elements broadcast by the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link BroadcastBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> BroadcastBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Posts an item to this block, delivering a copy to every pre-configured downstream target
     * in turn.  Each target's own {@link Target#post(Object, Duration)} is called with the same
     * {@code timeout} value, independently of every other target.  If an earlier target blocks
     * for the full {@code timeout} before accepting (or rejecting) the item, later targets are
     * only attempted afterward.  The worst-case total wait for this method is therefore up to
     * {@code timeout} multiplied by the number of configured targets, not a single shared budget
     * across all of them.
     *
     * @param item    The item to post.
     * @param timeout The maximum time to wait for each individual target to accept the item.
     * @return {@code true} if every target accepted the item before its own {@code timeout}
     * window elapsed; {@code false} if this block has already been completed, {@code item} is
     * {@code null}, or at least one target failed to accept the item within {@code timeout}.
     * @throws software.frisby.core.validation.NullValueException            if {@code timeout} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is negative.
     */
    @Override
    boolean post(T item, Duration timeout);

    /**
     * Signals that no more items will be posted to this block.  All pre-configured downstream
     * targets are completed immediately; since this block holds no internal buffer there is
     * nothing to drain.  The {@link #completion()} future resolves once all downstream targets
     * have also completed.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when all pre-configured downstream targets have completed.
     *
     * @return A {@link CompletableFuture} that resolves when all downstream targets are complete.
     */
    @Override
    CompletableFuture<Void> completion();
}
