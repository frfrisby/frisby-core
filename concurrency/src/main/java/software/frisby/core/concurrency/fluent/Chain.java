package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Source;
import software.frisby.core.concurrency.Target;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Intermediate builder for a terminal {@link Pipeline}.  Each call to {@link #then(PipelineStage)}
 * appends a stage and returns a new {@code Chain} with the updated output type.  Call one of the
 * terminal methods — {@link #to(java.util.function.Consumer)}, {@link #to(Action)},
 * {@link #to(PipelineTarget)}, or {@link #to(Pipeline)} — to wire the final stage and receive
 * the finished {@link Pipeline}.
 *
 * <p>The naming convention is intentional: {@code then()} always returns a {@code Chain}
 * (the pipeline is still open), while {@code to()} always returns a {@link Pipeline} (the
 * pipeline is closed and ready to use).</p>
 *
 * <p>Obtain a {@code Chain} via {@link PipelineBuilder#from} rather than constructing one
 * directly.</p>
 *
 * @param <H> The type of items accepted at the head of the pipeline (fixed throughout the chain).
 * @param <O> The output type of the current tail stage.
 * @see Pipeline#builder()
 * @see OpenChain
 */
public final class Chain<H, O> {
    private final PipelineTarget<?> target;
    private final PipelineSource<O> source;

    private Chain<H, ?> start;
    private Chain<H, ?> previous;
    private Chain<H, ?> next;

    private Executor executor;
    private boolean built;

    private Chain(PipelineTarget<?> target, PipelineSource<O> source) {
        this.target = target;
        this.source = source;
    }

    @SuppressWarnings("unchecked")
    private static <T> Target<T> castTarget(Target<?> t) {
        return (Target<T>) t;
    }

    static <I, N> Chain<I, N> from(PipelineStage<I, N> source, Executor executor) {
        Chain<I, N> chain = new Chain<>(source, source);

        chain.executor = executor;
        chain.start = chain;

        return chain;
    }

    static <I, N> Chain<I, N> from(OpenPipeline<I, N> pipeline, Executor executor) {
        Chain<I, N> chain = new Chain<>((PipelineTarget<I>) () -> pipeline, () -> pipeline);

        chain.executor = executor;
        chain.start = chain;

        return chain;
    }

    /**
     * Appends an intermediate stage and returns the updated chain.
     *
     * @param <N>  The output type of the new stage.
     * @param next The stage to append.
     * @return A new {@code Chain<H, N>} with the appended stage as the current tail.
     * @throws IllegalStateException if this chain has already been finalized by a {@code to()} call.
     */
    public <N> Chain<H, N> then(PipelineStage<O, N> next) {
        throwIfBuilt();

        Chain<H, N> nextLink = new Chain<>(next, next);

        nextLink.start = this.start;
        nextLink.previous = this;

        this.next = nextLink;

        return nextLink;
    }

    /**
     * Terminates the pipeline by linking its tail source to the supplied {@link Pipeline} and
     * returns the assembled {@link Pipeline}.
     *
     * <p>The inner pipeline's completion propagates correctly: because
     * {@link Pipeline#completion()} delegates depth-first through the block graph, the outer
     * pipeline's {@link Pipeline#awaitCompletion()} will not return until the inner pipeline
     * and all of its downstream stages have fully drained.</p>
     *
     * @param pipeline The pre-assembled pipeline that will receive items from the current tail.
     * @return The finished {@link Pipeline}.
     */
    public Pipeline<H> to(Pipeline<O> pipeline) {
        return to(() -> pipeline);
    }

    /**
     * Terminates the pipeline with a {@link Consumer} action and returns the assembled
     * {@link Pipeline}.
     *
     * @param consumer The consumer invoked for each item that reaches the terminal stage.
     * @return The finished {@link Pipeline}.
     * @throws software.frisby.core.validation.NullValueException if the pipeline contains any asynchronous stages and
     *                                                            no executor has been configured.
     */
    public Pipeline<H> to(Consumer<O> consumer) {
        return to(Action.<O>of().action(consumer));
    }

    /**
     * Terminates the pipeline with an {@link Action} stage and returns the assembled
     * {@link Pipeline}.
     *
     * @param action The configured action stage that will serve as the terminal block.
     * @return The finished {@link Pipeline}.
     * @throws software.frisby.core.validation.NullValueException if the pipeline contains any asynchronous stages and
     *                                                            no executor has been configured.
     */
    public Pipeline<H> to(Action<O> action) {
        return to((PipelineTarget<O>) action);
    }

    /**
     * Terminates the pipeline by linking its tail source to the supplied
     * {@link PipelineTarget} and returns the assembled {@link Pipeline}.
     *
     * <p>The target may implement {@link software.frisby.core.concurrency.Source}{@code <R>}
     * to produce further output (e.g. {@link Branch}, {@link Broadcast}), but any downstream
     * wiring of that source is the caller's responsibility; the fluent chain does not track it.</p>
     *
     * <p>To wire to a raw {@link Target} block that is not wrapped in a fluent stage helper,
     * pass a {@link PipelineTarget} lambda: {@code .to(() -> existingBlock)}.</p>
     *
     * @param terminal The terminal target that will receive items from the current tail.
     * @return The finished {@link Pipeline}.
     * @throws software.frisby.core.validation.NullValueException if the pipeline contains any asynchronous stages and
     *                                                            no executor has been configured.
     */
    public Pipeline<H> to(PipelineTarget<O> terminal) {
        throwIfBuilt();

        Chain<H, Void> terminalLink = new Chain<>(terminal, null);

        terminalLink.start = this.start;
        terminalLink.previous = this;

        this.next = terminalLink;

        return buildPipeline();
    }

    /**
     * Overrides the shared executor for this pipeline.  Useful when the executor should
     * differ from the one supplied to {@link PipelineBuilder#executor}.
     *
     * @param executor The executor to use for all async stages in this chain.
     * @return This chain, for method chaining.
     */
    public Chain<H, O> executor(Executor executor) {
        this.start.executor = executor;
        return this;
    }

    private Pipeline<H> buildPipeline() {
        this.start.built = true;

        Chain<H, ?> current = this.start;

        do {
            if (current.target instanceof ExecutorAwareStage asyncStage) {
                asyncStage.executor(this.start.executor);
            }

            current.target.toTarget();

            Chain<H, ?> previous = current.previous;

            if (null != previous) {
                previous.link();
            }

            current = current.next;
        } while (null != current);

        Target<H> head = castTarget(this.start.target.toTarget());

        return new DefaultPipeline<>(head);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void link() {
        // Only called from buildPipeline() on non-terminal nodes; source is always non-null.
        Source src = this.source.toSource();
        Target target = this.next.target.toTarget();

        src.linkTo(target);
    }

    private void throwIfBuilt() {
        if (this.start.built) {
            throw new IllegalStateException(
                    "This pipeline chain has already been finalized.  Call to() only once per chain."
            );
        }
    }
}
