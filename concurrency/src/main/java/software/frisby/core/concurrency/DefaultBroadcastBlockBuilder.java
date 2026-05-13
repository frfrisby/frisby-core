package software.frisby.core.concurrency;

import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class DefaultBroadcastBlockBuilder<T> implements BroadcastBlockBuilder<T> {
    private static final String TOO_FEW_TARGETS_MSG =
            "The 'BroadcastBlock' block requires at least two targets.  Call target() or targets() before calling build().";

    private final List<Target<T>> targets = new ArrayList<>();
    private UnaryOperator<T> cloningFunction;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    DefaultBroadcastBlockBuilder() {
    }

    @Override
    public BroadcastBlockBuilder<T> cloningFunction(UnaryOperator<T> cloningFunction) {
        this.cloningFunction = cloningFunction;

        return this;
    }

    @Override
    public BroadcastBlockBuilder<T> target(Target<T> target) {
        Values.notNull("target", target);

        this.targets.add(target);

        return this;
    }


    @Override
    public BroadcastBlockBuilder<T> targets(List<Target<T>> targets) {
        Sequences.notEmpty("targets", targets);

        this.targets.addAll(targets);

        return this;
    }

    @Override
    public BroadcastBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;

        return this;
    }

    @Override
    public BroadcastBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;

        return this;
    }

    @Override
    public BroadcastBlock<T> build() {
        if (this.targets.size() < 2) {
            throw new IllegalStateException(TOO_FEW_TARGETS_MSG);
        }

        return new DefaultBroadcastBlock<>(
                List.copyOf(this.targets),
                this.cloningFunction,
                this.itemPostedHandler,
                this.itemDeliveredHandler
        );
    }
}
