package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Source;
import software.frisby.core.concurrency.Target;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Intermediate builder for an {@link OpenPipeline}.  Each call to {@link #then(PipelineStage)}
 * appends a stage and returns a new {@code OpenChain} with updated type parameters.  Call
 * {@link #build()} at the end to assemble and return the finished {@link OpenPipeline}.  Use
 * {@link #then(OpenPipeline)} to embed a pre-assembled {@link OpenPipeline} as an intermediate
 * stage.
 *
 * <p>Obtain an {@code OpenChain} via {@link OpenPipelineBuilder#from} rather than constructing
 * one directly.</p>
 *
 * @param <H> The type of items accepted at the head of the pipeline (fixed throughout the chain).
 * @param <I> The input type of the current tail stage.
 * @param <O> The output type of the current tail stage.
 * @see OpenPipeline#builder()
 * @see Chain
 */
public final class OpenChain<H, I, O> {
    private final PipelineTarget<I> target;
    private final PipelineSource<O> source;

    private OpenChain<H, H, ?> start;
    private OpenChain<H, ?, ?> previous;
    private OpenChain<H, ?, ?> next;

    private Executor executor;
    private boolean built;

    private OpenChain(PipelineTarget<I> target, PipelineSource<O> source) {
        this.target = target;
        this.source = source;
    }

    @SuppressWarnings("unchecked")
    private static <T> Target<T> castTarget(Target<?> t) {
        return (Target<T>) t;
    }

    @SuppressWarnings("unchecked")
    private static <T> Source<T> castSource(Source<?> s) {
        return (Source<T>) s;
    }

    static <I, N> OpenChain<I, I, N> from(PipelineStage<I, N> source, Executor executor) {
        OpenChain<I, I, N> chain = new OpenChain<>(source, source);

        chain.executor = executor;
        chain.start = chain;

        return chain;
    }

    static <I, N> OpenChain<I, I, N> from(OpenPipeline<I, N> pipeline, Executor executor) {
        OpenChain<I, I, N> chain = new OpenChain<>(() -> pipeline, () -> pipeline);

        chain.executor = executor;
        chain.start = chain;

        return chain;
    }

    /**
     * Appends an intermediate stage and returns the updated open chain.
     *
     * @param <N>  The output type of the new stage.
     * @param next The stage to append.
     * @return A new {@code OpenChain<H, O, N>} with the appended stage as the current tail.
     * @throws IllegalStateException if this chain has already been finalized by a {@code build()} call.
     */
    public <N> OpenChain<H, O, N> then(PipelineStage<O, N> next) {
        throwIfBuilt();

        OpenChain<H, O, N> nextLink = new OpenChain<>(next, next);

        nextLink.start = this.start;
        nextLink.previous = this;

        this.next = nextLink;

        return nextLink;
    }

    /**
     * Appends a pre-assembled {@link OpenPipeline} as an intermediate stage and returns the
     * updated open chain.
     *
     * <p>The open pipeline's executor and internal wiring are already fixed at the time it was
     * built; this method simply splices it into the chain by linking the current tail to its
     * head and exposing its tail as the new chain output.  No executor is injected into the
     * embedded pipeline.</p>
     *
     * @param <N>      The output type of the embedded pipeline's tail.
     * @param pipeline The pre-assembled pipeline to embed as a stage.
     * @return A new {@code OpenChain<H, O, N>} with the embedded pipeline's tail as the current
     * output.
     * @throws IllegalStateException if this chain has already been finalized by a {@code build()} call.
     */
    public <N> OpenChain<H, O, N> then(OpenPipeline<O, N> pipeline) {
        throwIfBuilt();

        OpenChain<H, O, N> nextLink = new OpenChain<>(() -> pipeline, () -> pipeline);

        nextLink.start = this.start;
        nextLink.previous = this;

        this.next = nextLink;

        return nextLink;
    }

    /**
     * Assembles all stages, wires the blocks together, and returns the finished
     * {@link OpenPipeline}.
     *
     * <p>The returned pipeline exposes the tail source; call
     * {@link OpenPipeline#linkTo(Target)} to connect it to a downstream target, or supply
     * this pipeline to {@link OpenRouter#factory(Supplier)} to embed it as an arm inside a larger
     * pipeline.</p>
     *
     * @return The finished {@link OpenPipeline}{@code <H, O>}.
     * @throws software.frisby.core.validation.NullValueException if the pipeline contains any asynchronous stages and
     *                                                            no executor has been configured.
     * @throws IllegalStateException                              if this chain has already been finalized by a previous
     *                                                            {@code build()} call.
     */
    public OpenPipeline<H, O> build() {
        throwIfBuilt();

        this.start.built = true;

        OpenChain<H, ?, ?> current = this.start;
        OpenChain<H, ?, ?> last;

        do {
            if (current.target instanceof ExecutorAwareStage asyncStage) {
                asyncStage.executor(this.start.executor);
            }

            current.target.toTarget();

            OpenChain<H, ?, ?> previousChain = current.previous;

            if (null != previousChain) {
                previousChain.link();
            }

            last = current;
            current = current.next;
        } while (null != current);

        Target<H> head = castTarget(this.start.target.toTarget());
        Source<O> tail = castSource(last.source.toSource());

        return new DefaultOpenPipeline<>(head, List.of(tail));
    }

    /**
     * Overrides the shared executor for this open pipeline.
     *
     * @param executor The executor to use for all async stages in this chain.
     * @return This chain, for method chaining.
     */
    public OpenChain<H, I, O> executor(Executor executor) {
        this.start.executor = executor;
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void link() {
        // Only called from build() on non-terminal nodes; source is always non-null.
        Source src = this.source.toSource();
        Target nextTarget = this.next.target.toTarget();

        src.linkTo(nextTarget);
    }

    private void throwIfBuilt() {
        if (this.start.built) {
            throw new IllegalStateException(
                    "This open pipeline chain has already been finalized.  Call build() only once per chain."
            );
        }
    }
}
