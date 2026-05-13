package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Target;

/**
 * Fluent builder contract for any stage that accepts items — the input side of a pipeline
 * stage.
 *
 * <p>Implementations are lazy: the underlying {@link Target} block is not constructed until
 * {@link #toTarget()} is first called.  Subsequent calls must return the same instance so
 * that {@link PipelineSource#toSource()} and {@code toTarget()} on the same stage object
 * always refer to the same underlying block.</p>
 *
 * @param <T> The type of items accepted by this stage.
 * @see PipelineSource
 * @see PipelineStage
 */
public interface PipelineTarget<T> {
    /**
     * Returns the underlying {@link Target} block for this stage, constructing it on the
     * first call and returning the same instance on all subsequent calls.
     *
     * @return The {@link Target} block that receives items posted to this stage.
     */
    Target<T> toTarget();
}
