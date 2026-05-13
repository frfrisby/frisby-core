package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Stage;

import java.util.function.Supplier;

/**
 * A fully assembled pipeline that accepts items at its head, transforms them through each
 * downstream stage, and exposes a tail source that can be linked to additional downstream
 * stages or embedded inside a larger pipeline via {@link OpenRouter}.
 *
 * <p>{@code OpenPipeline<I, O>} extends {@link Pipeline}{@code <I>} (it is a complete,
 * runnable pipeline) and {@link Stage}{@code <I, O>} (its tail can be wired to a
 * downstream {@link software.frisby.core.concurrency.Target}).  This makes it the
 * correct type to supply to {@link OpenRouter#factory(Supplier)}.</p>
 *
 * <p>Typical construction via the builder:</p>
 * <pre>{@code
 * OpenPipeline<Message, List<Message>> arm = OpenPipeline.builder()
 *         .executor(executor)
 *         .from(Buffer.of(Message.class))
 *         .then(Group.of(Message.class, String.class)
 *                 .groupingFunction(Message::customerId))
 *         .build();
 * }</pre>
 *
 * @param <I> The type of items accepted at the head of this pipeline.
 * @param <O> The type of items produced at the tail source of this pipeline.
 * @see Pipeline
 * @see OpenRouter
 * @see OpenPipelineBuilder
 */
public interface OpenPipeline<I, O> extends Pipeline<I>, Stage<I, O> {
    /**
     * Returns a new builder for constructing an {@link OpenPipeline}.
     *
     * @return A new {@link OpenPipelineBuilder} instance.
     */
    static OpenPipelineBuilder builder() {
        return new DefaultOpenPipelineBuilder();
    }
}

