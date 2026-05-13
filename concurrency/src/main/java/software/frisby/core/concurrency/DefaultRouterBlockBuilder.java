package software.frisby.core.concurrency;

import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class DefaultRouterBlockBuilder<T> implements RouterBlockBuilder<T> {
    private static final String TOO_FEW_TARGETS_MSG =
            "The 'RouterBlock' block requires at least two targets.  Call target() or targets() before calling build().";

    private static final String ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG =
            "The 'RouterBlock' block already has a routing function configured.  Call only one of roundRobin(), balanced(), sticky(...), or routingFunction(...).";

    private final List<Target<T>> targets = new ArrayList<>();
    private RoutingFunction<T> routingFunction;
    private boolean routingStrategyConfigured;
    private boolean useBalanced;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    DefaultRouterBlockBuilder() {
    }

    private static <T> RoutingFunction<T> balanced(List<Target<T>> targets) {
        return item -> {
            int minLoad = Integer.MAX_VALUE;
            int minIndex = 0;

            for (int i = 0; i < targets.size(); i++) {
                int s = targets.get(i).inFlight();

                if (s < minLoad) {
                    minLoad = s;
                    minIndex = i;
                }
            }

            return minIndex;
        };
    }

    @Override
    public RouterBlockBuilder<T> target(Target<T> target) {
        Values.notNull("target", target);

        this.targets.add(target);

        return this;
    }


    @Override
    public RouterBlockBuilder<T> targets(List<Target<T>> targets) {
        Sequences.notEmpty("targets", targets);

        this.targets.addAll(targets);

        return this;
    }

    @Override
    public RouterBlockBuilder<T> roundRobin() {
        throwIfRoutingStrategyConfigured();

        this.routingFunction = RoutingFunction.roundRobin();
        this.routingStrategyConfigured = true;

        return this;
    }

    @Override
    public RouterBlockBuilder<T> balanced() {
        throwIfRoutingStrategyConfigured();

        this.useBalanced = true;
        this.routingStrategyConfigured = true;

        return this;
    }

    @Override
    public RouterBlockBuilder<T> sticky(Function<T, ?> keyExtractor) {
        throwIfRoutingStrategyConfigured();
        Values.notNull("keyExtractor", keyExtractor);

        this.routingFunction = RoutingFunction.sticky(keyExtractor);
        this.routingStrategyConfigured = true;

        return this;
    }

    @Override
    public RouterBlockBuilder<T> routingFunction(RoutingFunction<T> routingFunction) {
        throwIfRoutingStrategyConfigured();
        Values.notNull("routingFunction", routingFunction);

        this.routingFunction = routingFunction;
        this.routingStrategyConfigured = true;

        return this;
    }

    @Override
    public RouterBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public RouterBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public RouterBlock<T> build() {
        if (this.targets.size() < 2) {
            throw new IllegalStateException(TOO_FEW_TARGETS_MSG);
        }

        List<Target<T>> builtTargets = List.copyOf(this.targets);

        RoutingFunction<T> fn = this.useBalanced
                ? balanced(builtTargets)
                : (null == this.routingFunction ? RoutingFunction.roundRobin() : this.routingFunction);

        return new DefaultRouterBlock<>(
                builtTargets,
                fn,
                this.itemPostedHandler,
                this.itemDeliveredHandler
        );
    }

    private void throwIfRoutingStrategyConfigured() {
        if (this.routingStrategyConfigured) {
            throw new IllegalStateException(ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG);
        }
    }
}