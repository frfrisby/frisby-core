package software.frisby.core.concurrency;

/**
 * A pipeline source block that continuously pulls items from a configured {@link java.util.function.Supplier}
 * and forwards them to the linked downstream target.
 *
 * <p>{@code SourceBlock} drives the pipeline: it runs on a dedicated background thread provided
 * by the configured {@link java.util.concurrent.Executor}, invoking the supplier in a tight loop
 * and posting each returned item downstream.  The block holds the worker thread at
 * {@link Source#linkTo} until at least one target is wired, preventing silent message loss.</p>
 *
 * <p>Two supplier modes are available:</p>
 * <ul>
 *   <li><b>Single-item mode</b> — configured with {@link SourceBlockBuilder#supplier(java.util.function.Supplier)}.
 *       The supplier is called once per iteration and should block internally (for example via
 *       {@link java.util.concurrent.BlockingQueue#take()}) when no item is available.</li>
 *   <li><b>Batch mode</b> — configured with {@link SourceBlockBuilder#batchSupplier(java.util.function.Supplier)}.
 *       The supplier returns a {@link java.util.List}; each element is posted individually.
 *       A null or empty list is treated as a no-op.</li>
 * </ul>
 *
 * <pre>{@code
 * BlockingQueue<String> queue = new LinkedBlockingQueue<>();
 *
 * SourceBlock<String> source = SourceBlock.<String>builder()
 *         .supplier(queue::take)
 *         .executor(executor)
 *         .build();
 *
 * source.linkTo(bufferBlock);
 * }</pre>
 *
 * @param <T> The type of items produced by this block and forwarded to the linked target.
 * @see SourceBlockBuilder
 */
public interface SourceBlock<T> extends Source<T> {
    /**
     * Returns a new builder for constructing a {@link SourceBlock}.
     *
     * @param <T> The type of items produced by the block.
     * @return A new {@link SourceBlockBuilder} instance.
     */
    static <T> SourceBlockBuilder<T> builder() {
        return new DefaultSourceBlockBuilder<>();
    }

    /**
     * Returns a new builder for constructing a {@link SourceBlock}.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The type of items produced by the block.
     * @param ignored The item type class; used for inference only.
     * @return A new {@link SourceBlockBuilder} instance.
     */
    static <T> SourceBlockBuilder<T> builder(Class<T> ignored) {
        return builder();
    }
}
