package software.frisby.core.concurrency;

/**
 * Represents the sending end of a pipeline connection: a block that can be wired to a
 * single downstream {@link Target}.
 *
 * <p>After calling {@link #linkTo}, the block begins forwarding items it produces or
 * receives to the linked target.  For predicate-driven routing to multiple targets, wire a
 * {@link BranchBlock} as the immediate downstream target.</p>
 *
 * @param <T> The type of item forwarded to the linked downstream target.
 */
public interface Source<T> {
    /**
     * Links this source to the specified downstream target.
     *
     * <p>A source may be linked to at most one target.  Calling this method a second time
     * throws {@link IllegalStateException}.</p>
     *
     * @param target The downstream target that will receive items produced by this source.
     * @throws software.frisby.core.validation.NullValueException if {@code target} is null.
     * @throws IllegalStateException                              if a target has already been linked to this source.
     */
    void linkTo(Target<T> target);
}
