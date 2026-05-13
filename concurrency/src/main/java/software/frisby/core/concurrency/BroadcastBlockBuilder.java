package software.frisby.core.concurrency;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A builder for creating an instance of {@link BroadcastBlock}.  This can be created with the
 * static {@link BroadcastBlock#builder()} method.
 * <p>
 * At least two targets must be configured before calling {@link #build}; the builder throws
 * {@link IllegalStateException} if fewer than two are provided.
 *
 * @param <T> The type of elements held in the {@link BroadcastBlock}.
 * @see BroadcastBlock
 */
public interface BroadcastBlockBuilder<T> extends ObservableBlockBuilder<T, T, BroadcastBlockBuilder<T>> {
    /**
     * Adds a single downstream target.  This method is additive; each call appends one target
     * to the list.  Suitable for loop-based wiring of dynamically-sized target pools.
     *
     * @param target The downstream target to add.
     * @return The current builder instance.
     * @throws software.frisby.core.validation.NullValueException if {@code target} is null.
     */
    BroadcastBlockBuilder<T> target(Target<T> target);

    /**
     * Adds two or more downstream targets.  This method is additive; each call appends to the
     * accumulated target list.  For a single target, use {@link #target(Target)} instead.
     *
     * @param targets The list of downstream targets to add.
     * @return The current builder instance.
     * @throws software.frisby.core.validation.NullValueException       if {@code targets} is null.
     * @throws software.frisby.core.validation.MissingElementsException if {@code targets} is empty.
     * @throws software.frisby.core.validation.NullElementException     if {@code targets} contains a
     *                                                                  null element.
     */
    BroadcastBlockBuilder<T> targets(List<Target<T>> targets);

    /**
     * Optional. Sets the function that will be invoked to produce a fresh copy of each item
     * before it is delivered to each downstream target. If not configured, the original item
     * reference is passed to every target unchanged — appropriate for pipelines that carry
     * immutable messages.
     *
     * @param cloningFunction The function that clones each posted item before delivery.
     * @return The current builder instance.
     */
    BroadcastBlockBuilder<T> cloningFunction(UnaryOperator<T> cloningFunction);

    /**
     * Returns a new {@link BroadcastBlock} instance configured by the options set on this builder.
     *
     * @return A new {@link BroadcastBlock} instance.
     * @throws IllegalStateException if fewer than two targets have been configured.
     */
    BroadcastBlock<T> build();
}
