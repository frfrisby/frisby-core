package software.frisby.core.concurrency;

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
    static <T> ActionBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }

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
