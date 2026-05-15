package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fluent builder for an open fan-out stage that distributes each incoming item to one of
 * several parallel {@link OpenPipeline} arms and merges all arm tail sources into a single
 * output that can be wired to a shared downstream stage.
 *
 * <p>This is the open sibling of {@link Router}.  Use it when the arms perform intermediate
 * transformations and the results must flow to a common downstream stage:</p>
 *
 * <pre>{@code
 * Pipeline<Message> pipeline = Pipeline.<Message>builder()
 *         .executor(executor)
 *         .from(Buffer.of(Message.class))
 *         .then(OpenRouter.<Message, List<Message>>of()
 *                 .sticky(Message::deviceId)
 *                 .routes(10)
 *                 .factory(() -> OpenPipeline.builder()
 *                         .executor(executor)
 *                         .from(Buffer.of(Message.class))
 *                         .then(Group.of(Message.class, String.class)
 *                                 .groupingFunction(Message::customerId))
 *                         .build()))
 *         .then(groups -> groups.forEach(this::process));
 * }</pre>
 *
 * @param <T> The type of items distributed to each arm.
 * @param <R> The output type produced by each arm's tail source.
 * @see Router
 */
@SuppressWarnings("ALL")
public final class OpenRouter<T, R> implements PipelineStage<T, R>, ObservableBlockBuilder<T, T, OpenRouter<T, R>> {
    private RoutingFunction<T> routingFunction;
    private boolean useRoundRobin;
    private boolean useBalanced;
    private Function<T, ?> stickyKeyExtractor;
    private int routes;
    private Supplier<OpenPipeline<T, R>> factory;

    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private RouterInfo<T, R> routerInfo;

    private OpenRouter() {
    }

    /**
     * Returns a new {@code OpenRouter} builder.
     *
     * @param <T> The input type of items to route.
     * @param <R> The output type produced by each arm.
     * @return A new {@code OpenRouter} instance.
     */
    public static <T, R> OpenRouter<T, R> of() {
        return new OpenRouter<>();
    }

    /**
     * Returns a new {@code OpenRouter} builder.  The type parameters are used solely for
     * type inference; neither class literal is stored.
     *
     * @param <T>               The input type.
     * @param <R>               The output type.
     * @param ignoredInputType  The input type class; used for inference only.
     * @param ignoredOutputType The output type class; used for inference only.
     * @return A new {@code OpenRouter} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T, R> OpenRouter<T, R> of(Class<T> ignoredInputType, Class<R> ignoredOutputType) {
        return of();
    }

    /**
     * Returns a new passthrough {@code OpenRouter} builder where input and output types are
     * the same.  The type parameter is used solely for inference; the class literal is not
     * stored.
     *
     * @param <T>      The item type.
     * @param itemType The item type class; used for inference only.
     * @return A new {@code OpenRouter} instance.
     */
    public static <T> OpenRouter<T, T> of(Class<T> itemType) {
        return of(itemType, itemType);
    }

    /**
     * Returns a new {@code OpenRouter} builder.  The generic type tokens are used solely for
     * type inference; they are not stored.
     *
     * @param <T>               The input type.
     * @param <R>               The output type.
     * @param ignoredInputType  The input generic type token; used for inference only.
     * @param ignoredOutputType The output generic type token; used for inference only.
     * @return A new {@code OpenRouter} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T, R> OpenRouter<T, R> of(GenericType<T> ignoredInputType, GenericType<R> ignoredOutputType) {
        return of();
    }

    /**
     * Returns a new passthrough {@code OpenRouter} builder where input and output types are
     * the same generic type.  {@code itemType} is used solely for type inference; it is not
     * stored.  Use this overload when {@code T} is itself a generic type (e.g.
     * {@code List<Message>}) and a {@code Class} literal cannot capture the full type.
     *
     * @param <T>      The item type.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code OpenRouter<T, T>} instance.
     */
    public static <T> OpenRouter<T, T> of(GenericType<T> itemType) {
        return of(itemType, itemType);
    }

    /**
     * Returns a new {@code OpenRouter} builder for list-typed arms.  Neither class literal
     * is stored.
     *
     * @param <T>               The element type of the input list.
     * @param <R>               The element type of the output list.
     * @param ignoredInputType  The input element type class; used for inference only.
     * @param ignoredOutputType The output element type class; used for inference only.
     * @return A new {@code OpenRouter}{@code <List<T>, List<R>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T, R> OpenRouter<List<T>, List<R>> ofLists(Class<T> ignoredInputType, Class<R> ignoredOutputType) {
        return of(
                new GenericType<>() {
                },
                new GenericType<>() {
                }
        );
    }

    /**
     * Returns a new passthrough {@code OpenRouter} builder for list-typed arms.  The class
     * literal is not stored.
     *
     * @param <T>      The element type.
     * @param itemType The element type class; used for inference only.
     * @return A new {@code OpenRouter}{@code <List<T>, List<T>>} instance.
     */
    public static <T> OpenRouter<List<T>, List<T>> ofLists(Class<T> itemType) {
        return ofLists(itemType, itemType);
    }

    /**
     * Optional. Configures the block to distribute items in round-robin order.  This is also
     * the default when no routing strategy is specified, but calling this method makes the
     * intent explicit.
     *
     * @return This builder, for method chaining.
     */
    public OpenRouter<T, R> roundRobin() {
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
    public OpenRouter<T, R> balanced() {
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
    public OpenRouter<T, R> sticky(Function<T, ?> keyExtractor) {
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
    public OpenRouter<T, R> routingFunction(RoutingFunction<T> routingFunction) {
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
    public OpenRouter<T, R> routes(int routes) {
        Numbers.min("routes", routes, 2);

        this.routes = routes;
        return this;
    }

    /**
     * Sets the factory that produces the {@link OpenPipeline} for each arm.  The factory is
     * called {@link #routes} times at build time.
     *
     * @param factory A supplier that creates one open arm pipeline per invocation.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code factory} is null.
     */
    public OpenRouter<T, R> factory(Supplier<OpenPipeline<T, R>> factory) {
        Values.notNull("factory", factory);

        this.factory = factory;
        return this;
    }

    @Override
    public OpenRouter<T, R> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public OpenRouter<T, R> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<R> toSource() {
        return toRouterInfo().source();
    }

    @Override
    public Target<T> toTarget() {
        return toRouterInfo().block();
    }

    private RouterInfo<T, R> toRouterInfo() {
        if (null == this.routerInfo) {
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
            List<Source<R>> sourceList = new ArrayList<>();

            for (int i = 0; i < routes; i++) {
                OpenPipeline<T, R> pipeline = factory.get();

                targetList.add(pipeline);
                sourceList.add(pipeline);
            }

            RouterBlock<T> block = builder
                    .targets(targetList)
                    .build();

            this.routerInfo = new RouterInfo<>(new RouterSource<>(sourceList), block);
        }

        return this.routerInfo;
    }

    private static final class RouterInfo<I, O> {
        private final RouterSource<O> source;
        private final RouterBlock<I> block;

        private RouterInfo(RouterSource<O> source, RouterBlock<I> block) {
            Values.notNull("source", source);
            Values.notNull("block", block);

            this.source = source;
            this.block = block;
        }

        RouterSource<O> source() {
            return this.source;
        }

        RouterBlock<I> block() {
            return this.block;
        }
    }

    private static final class RouterSource<O> implements Source<O> {
        private final List<Source<O>> sources;

        private RouterSource(List<Source<O>> sources) {
            Sequences.notEmpty("sources", sources);

            this.sources = sources;
        }

        @Override
        public void linkTo(Target<O> target) {
            for (Source<O> source : this.sources) {
                source.linkTo(target);
            }
        }
    }
}


