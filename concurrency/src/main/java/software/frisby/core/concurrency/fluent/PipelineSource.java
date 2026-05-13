package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Source;

/**
 * Fluent builder contract for any stage that produces items — the output side of a pipeline
 * stage.
 *
 * <p>Implementations are lazy: the underlying {@link Source} block is not constructed until
 * {@link #toSource()} is first called.  Subsequent calls must return the same instance so
 * that {@code toSource()} and {@link PipelineTarget#toTarget()} on the same stage object
 * always refer to the same underlying block.</p>
 *
 * @param <T> The type of items produced by this stage.
 * @see PipelineTarget
 * @see PipelineStage
 */
public interface PipelineSource<T> {
    /**
     * Returns the underlying {@link Source} block for this stage, constructing it on the
     * first call and returning the same instance on all subsequent calls.
     *
     * @return The {@link Source} block that forwards items to the downstream target.
     */
    Source<T> toSource();
}
