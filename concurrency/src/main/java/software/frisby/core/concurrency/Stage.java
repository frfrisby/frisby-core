package software.frisby.core.concurrency;

/**
 * Represents a pipeline stage that both receives items from an upstream source and forwards
 * them — potentially transformed — to a linked downstream target.
 *
 * <p>A {@code Stage} combines {@link Target}{@code <I>} (it accepts posted items of type
 * {@code I}) with {@link Source}{@code <O>} (it can be linked to a downstream target that
 * accepts items of type {@code O}).  The two type parameters allow the stage to perform a
 * type transformation ({@code I → O}), or to pass items through unchanged when both
 * parameters are the same type.</p>
 *
 * <p>All pipeline block types that sit between two other blocks —
 * {@link BufferBlock}, {@link BatchBlock}, {@link PriorityBufferBlock}, {@link DelayBlock},
 * {@link GroupBlock}, {@link TransformBlock}, and {@link ExpandBlock} — extend this
 * interface.</p>
 *
 * @param <I> The type of items received by this stage from an upstream source.
 * @param <O> The type of items forwarded by this stage to a downstream target.
 */
public interface Stage<I, O> extends Target<I>, Source<O> {
}
