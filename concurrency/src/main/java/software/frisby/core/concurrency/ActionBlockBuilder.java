package software.frisby.core.concurrency;

import java.util.function.Consumer;

/**
 * Builder for constructing an {@link ActionBlock}.  Obtain an instance via
 * {@link ActionBlock#builder()}.
 *
 * @param <T> The type of items consumed by the block.
 */
public interface ActionBlockBuilder<T> {
    /**
     * Sets the consumer that will be invoked for each item received by the block.
     *
     * @param action The consumer to invoke for each item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code action} is null.
     */
    ActionBlockBuilder<T> action(Consumer<T> action);

    /**
     * Optional. Sets the handler that will receive a notification each time an item is posted
     * to the block. If not configured, no posted-item notifications are generated.
     *
     * @param handler The handler to notify when items are posted.
     * @return This builder, for method chaining.
     */
    ActionBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler);

    /**
     * Returns a new {@link ActionBlock} configured by this builder.
     *
     * @return A new {@link ActionBlock} instance.
     * @throws software.frisby.core.validation.NullValueException if no action has been configured.
     */
    ActionBlock<T> build();
}
