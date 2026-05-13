package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.NamedExecutorService;

import java.util.concurrent.Executor;

/**
 * Builder for constructing an {@link OpenPipeline}.
 *
 * <p>Configure an optional shared executor, then specify the first stage via {@link #from};
 * each subsequent call to {@link OpenChain#then} adds the next stage.  Terminate the chain
 * with {@link OpenChain#build()} to produce the finished {@link OpenPipeline}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * OpenPipeline<Message, List<Message>> arm = OpenPipeline.builder()
 *         .executor(executor)
 *         .from(Buffer.of(Message.class))
 *         .then(Group.of(Message.class, String.class)
 *                 .groupingFunction(Message::customerId))
 *         .build();
 * }</pre>
 *
 * @see OpenPipeline#builder()
 * @see PipelineBuilder
 */
public interface OpenPipelineBuilder {
    /**
     * Sets the {@link Executor} that will run the worker thread for all asynchronous
     * pipeline stages.  Any {@link Executor} implementation is accepted.  Shutdown coordination
     * for executors not managed by {@link NamedExecutorService} is the caller's responsibility.
     * <p>
     * The {@link Executor} is <b>required</b> when constructing pipelines consisting of asynchronous stages
     * such as {@link Batch}, {@link Buffer}, {@link Delay}, {@link Group}, or {@link PriorityBuffer}.
     *
     * @param executor The executor that will run async stage worker threads.
     * @return This builder, for method chaining.
     */
    OpenPipelineBuilder executor(Executor executor);

    /**
     * Sets the first stage of the open pipeline and returns the chain builder for adding
     * further stages.
     *
     * @param <I>    The input type of the first stage (also the pipeline head input type).
     * @param <N>    The output type of the first stage.
     * @param source The first stage.
     * @return An {@link OpenChain} rooted at type {@code I} with current output type {@code N}.
     */
    <I, N> OpenChain<I, I, N> from(PipelineStage<I, N> source);

    /**
     * Sets the first stage of the open pipeline to a pre-assembled {@link OpenPipeline} and
     * returns the chain builder for adding further stages.
     *
     * <p>The open pipeline's executor and internal wiring are already fixed; no executor is
     * injected into it.  Use this overload to embed a reusable sub-pipeline at the head of a
     * larger open pipeline.</p>
     *
     * <pre>{@code
     * OpenPipeline<Message, Result> pipeline = OpenPipeline.builder()
     *         .from(openRouterPipeline)
     *         .then(Transform.of(List.class, Result.class)
     *                 .transform(messages -> aggregate(messages)))
     *         .build();
     * }</pre>
     *
     * @param <I>      The input type of the embedded pipeline's head (also this pipeline's
     *                 head input type).
     * @param <N>      The output type of the embedded pipeline's tail.
     * @param pipeline The pre-assembled open pipeline to use as the first stage.
     * @return An {@link OpenChain} rooted at type {@code I} with current output type {@code N}.
     */
    <I, N> OpenChain<I, I, N> from(OpenPipeline<I, N> pipeline);
}

