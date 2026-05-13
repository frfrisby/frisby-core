package software.frisby.core.concurrency.fluent;

/**
 * Fluent builder contract for an intermediate pipeline stage — one that both accepts items
 * and produces transformed output.
 *
 * <p>A {@code PipelineStage} is the composition of {@link PipelineTarget}{@code <I>} and
 * {@link PipelineSource}{@code <O>}: it receives items of type {@code I}, processes them,
 * and forwards results of type {@code O} to the downstream target.  The two types may be the
 * same (e.g. {@link Buffer}, {@link Delay}) or different (e.g. {@link Batch}
 * {@code <T> → List<T>}, {@link Transform} {@code <T, R>}).</p>
 *
 * <p>This is a sealed interface; only the types listed in the {@code permits} clause may
 * implement it directly.  Use {@link PipelineTarget} for terminal stages that produce no
 * output ({@link Action}, {@link Branch}, {@link Broadcast}, {@link Router}).</p>
 *
 * @param <I> The type of items accepted by this stage.
 * @param <O> The type of items produced by this stage.
 * @see PipelineTarget
 * @see PipelineSource
 */
public sealed interface PipelineStage<I, O> extends PipelineTarget<I>, PipelineSource<O>
        permits Batch, Buffer, Delay, Expand, Group, OpenRouter, PriorityBuffer, Tap, Transform {
}
