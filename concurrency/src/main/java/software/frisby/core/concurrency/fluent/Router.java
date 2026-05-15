package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fluent builder for a terminal fan-out stage that distributes each incoming item to one of
 * several parallel {@link Pipeline} arms according to a configurable routing strategy.
 *
 * <p>Each arm is a fully assembled, terminal {@link Pipeline}{@code <T>} — it consumes items
 * without producing further output.  Use {@link OpenRouter} when the arms need to feed a
 * shared downstream stage.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * Router.<Message>of()
 *         .sticky(Message::customerId)
 *         .routes(8)
 *         .factory(() -> Pipeline.<Message>builder()
 *                 .executor(executor)
 *                 .from(Buffer.of(Message.class))
 *                 .then(message -> process(message)));
 * }</pre>
 *
 * @param <T> The type of items distributed to each arm.
 * @see OpenRouter
 */
public final class Router<T> implements PipelineTarget<T>, ObservableBlockBuilder<T, T, Router<T>> {
    private RoutingFunction<T> routingFunction;
    private boolean useRoundRobin;
    private boolean useBalanced;
    private Function<T, ?> stickyKeyExtractor;
    private int routes;
    private Supplier<Pipeline<T>> factory;

    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private RouterBlock<T> block;

    private Router() {
    }

    /**
     * Returns a new {@code Router} builder.
     *
     * @param <T> The type of items to route.
     * @return A new {@code Router} instance.
     */
    public static <T> Router<T> of() {
        return new Router<>();
    }

    /**
     * Returns a new {@code Router} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to route.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Router} instance.
     */
    public static <T> Router<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Router} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to route.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Router} instance.
     */
    public static <T> Router<T> of(GenericType<T> itemType) {
        return of(itemType.getRawType());
    }

    /**
     * Returns a new {@code Router<List<T>>} builder for pipelines whose items are lists.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The element type of the lists to route.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Router<List<T>>} instance.
     */
    public static <T> Router<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Optional. Configures the block to distribute items in round-robin order.  This is also
     * the default when no routing strategy is specified, but calling this method makes the
     * intent explicit.
     *
     * @return This builder, for method chaining.
     */
    public Router<T> roundRobin() {
        this.useRoundRobin = true;
        return this;
    }

    /**
     * Optional. Configures the block to route each item to the arm with the fewest items
     * currently queued, as reported by {@link Target#size()}.  When arms are tied on queue
     * depth the one with the lowest index wins.  If not configured, round-robin is used.
     *
     * @return This builder, for method chaining.
     */
    public Router<T> balanced() {
        this.useBalanced = true;
        return this;
    }

    /**
     * Optional. Configures the block to route all items sharing the same extracted key to the
     * same arm.  The key's {@link Object#hashCode()} selects the index.  A {@code null} key is
     * routed to the arm at index {@code 0}.  If not configured, round-robin is used.
     *
     * @param keyExtractor A function that extracts the routing key from each item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code keyExtractor} is null.
     */
    public Router<T> sticky(Function<T, ?> keyExtractor) {
        Values.notNull("keyExtractor", keyExtractor);

        this.stickyKeyExtractor = keyExtractor;
        return this;
    }

    /**
     * Optional. Sets a custom routing function that determines which arm receives each posted
     * item.  The function returns a zero-based index; the block maps it using
     * {@link Math#floorMod}, so negative return values are handled correctly.  If not
     * configured, round-robin is used.
     *
     * @param routingFunction The function that selects an arm index for each posted item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code routingFunction} is null.
     */
    public Router<T> routingFunction(RoutingFunction<T> routingFunction) {
        Values.notNull("routingFunction", routingFunction);

        this.routingFunction = routingFunction;
        return this;
    }

    /**
     * Sets the number of parallel arms.  Must be at least {@code 2}.
     *
     * @param routes The number of arms; must be {@code >= 2}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code routes < 2}.
     */
    public Router<T> routes(int routes) {
        Numbers.min("routes", routes, 2);

        this.routes = routes;
        return this;
    }

    /**
     * Sets the factory that produces the {@link Pipeline} for each arm.  The factory is
     * called {@link #routes} times at build time.
     *
     * @param factory A supplier that creates one terminal arm pipeline per invocation.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code factory} is null.
     */
    public Router<T> factory(Supplier<Pipeline<T>> factory) {
        Values.notNull("factory", factory);

        this.factory = factory;
        return this;
    }

    @Override
    public Router<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Router<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Target<T> toTarget() {
        return toBlock();
    }

    private RouterBlock<T> toBlock() {
        if (null == this.block) {
            RouterBlockBuilder<T> builder = RouterBlock.<T>builder()
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler);

            if (useRoundRobin) {
                builder.roundRobin();
            }

            if (useBalanced) {
                builder.balanced();
            }

            if (null != stickyKeyExtractor) {
                builder.sticky(stickyKeyExtractor);
            }

            if (null != routingFunction) {
                builder.routingFunction(routingFunction);
            }

            List<Target<T>> targetList = new ArrayList<>();

            for (int i = 0; i < routes; i++) {
                targetList.add(factory.get());
            }

            this.block = builder
                    .targets(targetList)
                    .build();
        }

        return this.block;
    }
}
