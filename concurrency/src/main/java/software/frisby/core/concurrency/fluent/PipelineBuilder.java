package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.NamedExecutorService;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Builder for constructing a terminal {@link Pipeline}.
 *
 * <p>Configure an optional shared executor, then specify the first stage via {@link #from};
 * each subsequent call to {@link Chain#then} adds the next stage.  Terminate the chain with
 * {@link Chain#to(Consumer)} or {@link Chain#to(PipelineTarget)}, both
 * of which return the finished {@link Pipeline}.</p>
 *
 * @param <T> The type of items accepted at the head of the pipeline being built.
 * @see Pipeline#builder()
 * @see OpenPipelineBuilder
 */
public interface PipelineBuilder<T> {
    /**
     * Sets the {@link Executor} that will run the worker thread for all asynchronous
     * pipeline stages.  Any {@link Executor} implementation is accepted, including
     * {@code Executors.newVirtualThreadPerTaskExecutor()} on Java 21+.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     * <p>
     * The {@link Executor} is <b>required</b> when constructing pipelines consisting of asynchronous stages
     * such as {@link Batch}, {@link Buffer}, {@link Delay}, {@link Group}, or {@link PriorityBuffer}.
     *
     * @param executor The executor that will run async stage worker threads.
     * @return This builder, for method chaining.
     */
    PipelineBuilder<T> executor(Executor executor);

    /**
     * Sets the first stage of the pipeline and returns the chain builder for adding further
     * stages.
     *
     * @param <N>    The output type of the first stage.
     * @param source The first stage; provides both the head {@link software.frisby.core.concurrency.Target}
     *               and the initial source link.
     * @return A {@link Chain} rooted at type {@code T} with current output type {@code N}.
     */
    <N> Chain<T, N> from(PipelineStage<T, N> source);

    /**
     * Sets the first stage of the pipeline to a pre-assembled {@link OpenPipeline} and returns
     * the chain builder for adding further stages.
     *
     * <p>The open pipeline's executor and internal wiring are already fixed; no executor is
     * injected into it.  Use this overload to embed a reusable sub-pipeline at the head of a
     * larger terminal pipeline.</p>
     *
     * <pre>{@code
     * Pipeline<Message> pipeline = Pipeline.<Message>builder()
     *         .executor(executor)
     *         .from(openRouterPipeline)
     *         .to(messages -> process(messages));
     * }</pre>
     *
     * @param <N>      The output type of the open pipeline's tail.
     * @param pipeline The pre-assembled open pipeline to use as the first stage.
     * @return A {@link Chain} rooted at type {@code T} with current output type {@code N}.
     */
    <N> Chain<T, N> from(OpenPipeline<T, N> pipeline);

    /**
     * Builds a single-stage terminal pipeline whose only stage is the supplied
     * {@link PipelineTarget}.  Use this overload when the pipeline consists of a single
     * terminal block — {@link Action}, {@link Branch}, {@link Broadcast}, or {@link Router} —
     * that does not produce output and therefore cannot be followed by further stages.
     *
     * <pre>{@code
     * Pipeline<Message> p = Pipeline.<Message>builder()
     *         .from(Action.<Message>of()
     *                 .action(message -> process(message)));
     * }</pre>
     *
     * @param terminal The single terminal stage that will receive all items posted to this pipeline.
     * @return The finished {@link Pipeline}{@code <T>}.
     */
    Pipeline<T> from(PipelineTarget<T> terminal);

    /**
     * Builds a single-stage terminal pipeline whose only stage is an {@link Action} that invokes
     * the supplied {@link Consumer}.  Use this overload when the pipeline consists of a single
     * terminal block that does not produce output and therefore cannot be followed by further stages.
     *
     * <pre>{@code
     * Pipeline<Message> p = Pipeline.<Message>builder()
     *         .from(message -> process(message));
     * }</pre>
     *
     * @param consumer The single terminal {@link Consumer} that will receive all items posted to this pipeline.
     * @return The finished {@link Pipeline}{@code <T>}.
     */
    Pipeline<T> from(Consumer<T> consumer);
}
