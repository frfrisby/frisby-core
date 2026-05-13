package software.frisby.core.concurrency;

import software.frisby.core.validation.NullValueException;

import java.util.function.Predicate;

/**
 * A builder for creating an instance of {@link BranchBlock}.  This can be created with the static
 * {@link BranchBlock#builder()} method.
 * <p>
 * At least one {@link #when} clause and an {@link #otherwise} target must be configured before
 * {@link #build} is called; the builder throws {@link IllegalStateException} if either is absent.
 * The {@link #otherwise} target may be configured only once; a second call to {@link #otherwise}
 * throws {@link IllegalStateException}.
 *
 * @param <T> The type of elements routed by the block.
 * @see BranchBlock
 */
public interface BranchBlockBuilder<T> extends ObservableBlockBuilder<T, T, BranchBlockBuilder<T>> {
    /**
     * Adds a conditional routing clause.  If {@code predicate} matches a posted item, that item
     * will be forwarded to {@code target}.  Clauses are evaluated in the order they are declared;
     * the first match wins.
     * <p>
     * This method may be called multiple times to add multiple conditional branches.
     *
     * @param predicate The predicate an item must satisfy to be forwarded to {@code target}.
     * @param target    The target that will receive items matching {@code predicate}.
     * @return This builder, for method chaining.
     * @throws NullValueException if {@code predicate} or
     *                            {@code target} is null.
     */
    BranchBlockBuilder<T> when(Predicate<T> predicate, Target<T> target);

    /**
     * Sets the default routing target.  Items that do not match any {@link #when} predicate will
     * be forwarded to {@code target}.  This method must be called exactly once before {@link #build};
     * a second call throws {@link IllegalStateException}.
     *
     * @param target The default target that will receive items that do not match any when clause.
     * @return This builder, for method chaining.
     * @throws NullValueException    if {@code target} is null.
     * @throws IllegalStateException if {@code otherwise} has already been called on this builder.
     */
    BranchBlockBuilder<T> otherwise(Target<T> target);

    /**
     * Returns a new {@link BranchBlock} instance configured by the options set on this builder.
     *
     * @return A new {@link BranchBlock} instance.
     * @throws IllegalStateException if no {@code when} clause has been configured.
     * @throws IllegalStateException if no {@code otherwise} target has been configured.
     */
    BranchBlock<T> build();
}
