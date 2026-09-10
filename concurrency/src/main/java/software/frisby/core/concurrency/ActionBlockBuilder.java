package software.frisby.core.concurrency;

import java.util.function.Consumer;

/**
 * Builder for constructing an {@link ActionBlock}.  Obtain an instance via
 * {@link ActionBlock#builder()}.
 *
 * <p>{@code ActionBlock} is a terminal, queue-less block with no downstream target, so
 * {@link ItemDeliveredHandler} here reports that the configured {@link Consumer} completed
 * successfully for a given item — it is never invoked if the action throws.</p>
 *
 * @param <T> The type of items consumed by the block.
 */
public interface ActionBlockBuilder<T> extends ObservableBlockBuilder<T, T, ActionBlockBuilder<T>> {
    /**
     * Sets the consumer that will be invoked for each item received by the block.
     *
     * @param action The consumer to invoke for each item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code action} is null.
     */
    ActionBlockBuilder<T> action(Consumer<T> action);


    /**
     * Returns a new {@link ActionBlock} configured by this builder.
     *
     * @return A new {@link ActionBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no action has been configured.
     */
    ActionBlock<T> build();
}
