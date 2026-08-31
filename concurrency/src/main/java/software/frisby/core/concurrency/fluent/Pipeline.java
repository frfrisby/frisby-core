package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Target;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * A fully assembled, runnable pipeline that accepts items at its head and processes them
 * through every downstream stage to the terminal block.
 *
 * <p>A {@code Pipeline<T>} is a terminal pipeline — its last stage consumes items without
 * producing output.  Use {@link OpenPipeline} for pipelines that have a tail source that can
 * be linked to additional downstream stages.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * Pipeline<Message> pipeline = Pipeline.<Message>builder()
 *         .executor(executor)
 *         .from(Buffer.of(Message.class))
 *         .then(Transform.of(Message.class).transform(...))
 *         .then(message -> process(message));
 *
 * pipeline.post(message);
 * pipeline.complete();
 * pipeline.awaitCompletion();
 * }</pre>
 *
 * @param <T> The type of items accepted at the head of this pipeline.
 * @see OpenPipeline
 * @see PipelineBuilder
 */
public interface Pipeline<T> extends Target<T> {
    /**
     * Returns a new builder for constructing a terminal {@link Pipeline}.
     *
     * @param <T> The type of items accepted at the head of the pipeline.
     * @return A new {@link PipelineBuilder} instance.
     */
    static <T> PipelineBuilder<T> builder() {
        return new DefaultPipelineBuilder<>();
    }

    /**
     * Posts an item to the head of this pipeline.
     *
     * @param item The item to post.
     * @return {@code true} if the item was accepted; {@code false} if the pipeline has
     * already been completed or the head's internal queue is full.
     */
    @Override
    boolean post(T item);

    /**
     * Posts an item to the head of this pipeline, waiting up to {@code timeout} for the head
     * stage to accept it if it cannot do so immediately.
     *
     * <p>Whether this method can ever actually wait depends on the pipeline's head stage.  If the
     * head is one of this library's asynchronous blocks ({@code Buffer}, {@code Batch}, {@code
     * Group}, {@code PriorityBuffer}, {@code Delay}), {@code timeout} bounds how long the caller
     * waits for a free queue slot.  If the head is a synchronous stage ({@code Transform}, {@code
     * Tap}, {@code Router}, {@code Branch}, {@code Broadcast}, or a bare lambda), there is nothing
     * to wait for, and this method behaves exactly like {@link #post(Object)}.  Either way, this
     * method never bounds the execution time of any user-supplied code invoked while delivering
     * the item.</p>
     *
     * @param item    The item to post.
     * @param timeout The maximum time to wait for the head stage to accept the item.
     * @return {@code true} if the item was accepted before {@code timeout} elapsed;
     * {@code false} if {@code timeout} elapsed before the item could be accepted.
     * @throws UnsupportedOperationException if the head stage does not support a bounded-wait
     *                                        {@code post} — this never happens for a pipeline
     *                                        assembled entirely from this library's own stage
     *                                        builders, only for a hand-wired custom head.
     */
    @Override
    boolean post(T item, Duration timeout);

    /**
     * Returns the number of items currently queued at the head of this pipeline.
     *
     * <p>Delegates to the head block's {@link Target#size()} implementation.  For
     * synchronous head blocks that hold no internal queue, this returns {@code 0}.</p>
     *
     * @return The number of items currently buffered at the pipeline head.
     */
    @Override
    int size();

    /**
     * Signals that no more items will be posted to this pipeline.  Completion cascades
     * automatically from the head through every downstream stage to the terminal block.
     *
     * <p>After this method returns, subsequent calls to {@link #post} return {@code false}
     * immediately.  Call {@link #awaitCompletion} to block until all queued items have been
     * processed.</p>
     */
    @Override
    void complete();

    /**
     * Returns a future that resolves when the terminal block of this pipeline has finished
     * processing all items queued before {@link #complete} was called.
     *
     * @return A {@link CompletableFuture} that resolves when the pipeline has fully drained.
     */
    @Override
    CompletableFuture<Void> completion();
}
