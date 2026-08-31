package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A terminal pipeline block that invokes a {@link Consumer} for every item it receives.
 *
 * <p>{@code ActionBlock} is the canonical end-of-pipeline sink.  It accepts items on the
 * calling thread, executes the configured {@link Consumer} inline, and has no downstream
 * target.  Because there is no internal queue, {@link #complete()} resolves the
 * {@link #completion()} future immediately — there is nothing to drain.</p>
 *
 * <pre>{@code
 * ActionBlock<String> sink = ActionBlock.<String>builder()
 *         .action(item -> System.out.println("Received: " + item))
 *         .build();
 *
 * transformBlock.linkTo(sink);
 * }</pre>
 *
 * @param <T> The type of items consumed by this block.
 * @see ActionBlockBuilder
 */
public interface ActionBlock<T> extends Target<T> {
    /**
     * Returns a new builder for constructing an {@link ActionBlock}.
     *
     * @param <T> The type of items consumed by the block.
     * @return A new {@link ActionBlockBuilder} instance.
     */
    static <T> ActionBlockBuilder<T> builder() {
        return new DefaultActionBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing an {@link ActionBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items consumed by the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link ActionBlockBuilder} instance.
     */
    @SuppressWarnings("java:S1172")
    static <T> ActionBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

    /**
     * Posts an item to this block, identically to {@link #post(Object)}.
     *
     * <p>This block processes items inline on the calling thread and holds no internal queue, so
     * it never waits for capacity.  {@code timeout} is accepted only for consistency with
     * {@link Target#post(Object, Duration)} and has no effect on this block's behavior.
     *
     * @param item    The item to post.
     * @param timeout The maximum time to wait for capacity to become available; ignored, since
     *                this block never waits.
     * @return {@code true} if the item was accepted; {@code false} if it was rejected (for
     * example, because this block has already been completed).
     * @throws software.frisby.core.validation.NullValueException            if {@code timeout} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code timeout} is negative.
     */
    @Override
    boolean post(T item, Duration timeout);

    /**
     * Signals that no more items will be posted to this block.  Since this block processes
     * items inline, the {@link #completion()} future resolves immediately — there is no
     * queue to drain.
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when {@link #complete()} has been called.
     *
     * @return A {@link CompletableFuture} that resolves when this block has been completed.
     */
    @Override
    CompletableFuture<Void> completion();
}
