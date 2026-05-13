package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class DefaultBranchBlockBuilder<T> implements BranchBlockBuilder<T> {
    private final List<Predicate<T>> predicates = new ArrayList<>();
    private final List<Target<T>> whenTargets = new ArrayList<>();
    private Target<T> otherwiseTarget;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    DefaultBranchBlockBuilder() {
    }

    @Override
    public BranchBlockBuilder<T> when(Predicate<T> predicate, Target<T> target) {
        Values.notNull("predicate", predicate);
        Values.notNull("target", target);

        this.predicates.add(predicate);
        this.whenTargets.add(target);

        return this;
    }

    @Override
    public BranchBlockBuilder<T> otherwise(Target<T> target) {
        Values.notNull("target", target);

        if (null != this.otherwiseTarget) {
            throw new IllegalStateException(
                    "The 'BranchBlock' block already has an otherwise target configured.  The otherwise() method may only be called once."
            );
        }

        this.otherwiseTarget = target;

        return this;
    }

    @Override
    public BranchBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public BranchBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public BranchBlock<T> build() {
        if (this.predicates.isEmpty()) {
            throw new IllegalStateException(
                    "The 'BranchBlock' block requires at least one 'when' clause.  Call when(predicate, target) before calling build()."
            );
        }

        if (null == this.otherwiseTarget) {
            throw new IllegalStateException(
                    "The 'BranchBlock' block requires an 'otherwise' target.  Call otherwise(target) before calling build()."
            );
        }

        return new DefaultBranchBlock<>(
                List.copyOf(this.predicates),
                List.copyOf(this.whenTargets),
                this.otherwiseTarget,
                this.itemPostedHandler,
                this.itemDeliveredHandler
        );
    }
}
