package software.frisby.core.concurrency;

import java.util.List;
import java.util.function.Function;

/**
 * A builder for creating an instance of {@link RouterBlock}.  This can be created with the static
 * {@link RouterBlock#builder()} method.
 * <p>
 * At least two targets must be configured before calling {@link #build}; the builder throws
 * {@link IllegalStateException} if fewer than two are provided.  If no routing strategy is
 * configured, items are distributed in round-robin order by default.
 * <p>
 * At most one routing strategy may be configured.  Calling any two of {@link #roundRobin()},
 * {@link #balanced()}, {@link #sticky(Function)}, or {@link #routingFunction} throws
 * {@link IllegalStateException} on the second call.
 *
 * @param <T> The type of elements routed by the block.
 * @see RouterBlock
 */
public interface RouterBlockBuilder<T> extends ObservableBlockBuilder<T, T, RouterBlockBuilder<T>> {
    /**
     * Adds a single downstream target.  This method is additive; each call appends one target to
     * the list.  Suitable for loop-based wiring of dynamically-sized target pools.
     *
     * @param target The downstream target to add.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code target} is null.
     */
    RouterBlockBuilder<T> target(Target<T> target);

    /**
     * Adds two or more downstream targets.  This method is additive; each call appends to the
     * accumulated target list.  For a single target, use {@link #target(Target)} instead.
     *
     * @param targets The list of downstream targets to add.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException       if {@code targets} is null.
     * @throws software.frisby.core.validation.MissingElementsException if {@code targets} is empty.
     * @throws software.frisby.core.validation.NullElementException     if {@code targets} contains a
     *                                                                  null element.
     */
    RouterBlockBuilder<T> targets(List<Target<T>> targets);

    /**
     * Optional. Configures the block to distribute items in round-robin order. This is also
     * the default when no routing strategy is specified, but calling this method makes the
     * intent explicit.
     *
     * @return This builder, for method chaining.
     * @throws IllegalStateException if a routing strategy has already been configured on this builder.
     */
    RouterBlockBuilder<T> roundRobin();

    /**
     * Optional. Configures the block to route each item to the target with the fewest items
     * currently queued, as reported by {@link Target#size()}.  When targets are tied on queue
     * depth the one with the lowest index wins.  If not configured, round-robin is used.
     * <p>
     * The balanced routing function is constructed at {@link #build} time using the exact target
     * list registered with the block, ensuring the two are always in sync.
     *
     * @return This builder, for method chaining.
     * @throws IllegalStateException if a routing strategy has already been configured on this builder.
     */
    RouterBlockBuilder<T> balanced();

    /**
     * Optional. Configures the block to route all items sharing the same extracted key to the
     * same target. The key's {@link Object#hashCode()} is used to select the index, ensuring
     * stable affinity across the lifetime of the router — all events for the same customer,
     * device, or other domain entity are always forwarded to the same downstream target. A
     * {@code null} key is routed to the target at index {@code 0}. If not configured, round-robin
     * is used.
     *
     * @param keyExtractor A function that extracts the routing key from each item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code keyExtractor} is null.
     * @throws IllegalStateException                              if a routing strategy has already been configured on this builder.
     */
    RouterBlockBuilder<T> sticky(Function<T, ?> keyExtractor);

    /**
     * Optional. Sets a custom routing function that determines which target receives each
     * posted item. The function returns a zero-based index; the block maps it to an actual
     * target using {@link Math#floorMod}, so negative return values are handled correctly. If
     * not configured, round-robin is used.
     *
     * @param routingFunction The function that selects a target index for each posted item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code routingFunction} is null.
     * @throws IllegalStateException                              if a routing strategy has already been configured on this builder.
     */
    RouterBlockBuilder<T> routingFunction(RoutingFunction<T> routingFunction);

    /**
     * Returns a new {@link RouterBlock} instance configured by the options set on this builder.
     *
     * @return A new {@link RouterBlock} instance.
     * @throws IllegalStateException if fewer than two targets have been configured.
     */
    RouterBlock<T> build();
}
