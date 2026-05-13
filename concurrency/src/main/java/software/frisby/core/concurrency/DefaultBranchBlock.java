package software.frisby.core.concurrency;

import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@SuppressWarnings("ALL")
final class DefaultBranchBlock<T> implements BranchBlock<T> {
    private static final int OTHERWISE_BRANCH_ID = 0;

    private final List<Filter<T>> filterList;
    private final Target<T> otherwiseTarget;
    private final ItemPostedManager<T> postedManager;
    private final ItemDeliveredManager<T> deliveredManager;
    private final EventSource eventSource;
    private final SyncCompletionGuard guard;

    DefaultBranchBlock(List<Predicate<T>> predicates,
                       List<Target<T>> whenTargets,
                       Target<T> otherwiseTarget,
                       ItemPostedHandler<T> itemPostedHandler,
                       ItemDeliveredHandler<T> itemDeliveredHandler) {
        Sequences.optionalNotEmpty("predicates", predicates);
        Sequences.optionalNotEmpty("whenTargets", whenTargets);
        Values.notNull("otherwiseTarget", otherwiseTarget);

        this.eventSource = new EventSource(BranchBlock.class.getSimpleName());

        this.postedManager = new ItemPostedManager<>(this, this.eventSource, itemPostedHandler);
        this.deliveredManager = new ItemDeliveredManager<>(this, this.eventSource, itemDeliveredHandler);

        this.otherwiseTarget = otherwiseTarget;
        this.guard = new SyncCompletionGuard(this::signalDownstream);

        List<Filter<T>> filters = new ArrayList<>(predicates.size());
        for (int i = 0; i < predicates.size(); i++) {
            filters.add(
                    new Filter<>(
                            this.deliveredManager,
                            i + 1,
                            whenTargets.get(i),
                            predicates.get(i),
                            this.eventSource
                    )
            );
        }

        this.filterList = List.copyOf(filters);

        for (Filter<T> filter : this.filterList) {
            filter.onLinked();
        }

        this.otherwiseTarget.onLinked();
    }

    @Override
    public boolean post(T item) {
        if (this.guard.isCompleted()) {
            return false;
        }

        if (null == item) {
            return false;
        }

        this.guard.begin();

        try {
            for (Filter<T> filter : this.filterList) {
                if (filter.matchesCriteria(item)) {
                    boolean accepted = filter.post(item);
                    this.postedManager.sendOnPostedNotification(item, accepted);

                    return accepted;
                }
            }

            boolean accepted = this.otherwiseTarget.post(item);

            if (accepted) {
                this.deliveredManager.sendOnDeliveredNotification(this.otherwiseTarget, item);
            }

            this.postedManager.sendOnPostedNotification(item, accepted);

            return accepted;
        } finally {
            this.guard.end();
        }
    }

    @Override
    public void onLinked() {
        this.guard.onLinked();
    }

    @Override
    public int inFlight() {
        int sum = 0;

        for (Filter<T> filter : this.filterList) {
            sum += filter.inFlight();
        }

        sum += this.otherwiseTarget.inFlight();

        return sum;
    }

    @Override
    public void complete() {
        this.guard.complete();
    }

    @Override
    public CompletableFuture<Void> completion() {
        CompletableFuture<?>[] futures = new CompletableFuture[this.filterList.size() + 1];

        for (int i = 0; i < this.filterList.size(); i++) {
            futures[i] = this.filterList.get(i).completion();
        }

        futures[this.filterList.size()] = this.otherwiseTarget.completion();

        return CompletableFuture.allOf(futures);
    }

    private void signalDownstream() {
        for (Filter<T> filter : this.filterList) {
            filter.complete();
        }

        this.otherwiseTarget.complete();
    }

    private static final class Filter<T> {
        private final ItemDeliveredManager<T> deliveredManager;
        private final int branchId;
        private final Target<T> target;
        private final Predicate<T> criteria;
        private final EventSource eventSource;

        private Filter(ItemDeliveredManager<T> deliveredManager,
                       int branchId,
                       Target<T> target,
                       Predicate<T> criteria,
                       EventSource eventSource) {
            this.deliveredManager = deliveredManager;
            this.branchId = branchId;
            this.target = target;
            this.criteria = criteria;
            this.eventSource = eventSource;
        }

        boolean matchesCriteria(T item) {
            try {
                return this.criteria.test(item);
            } catch (Exception ex) {
                this.eventSource.createTargetPredicateErrorEvent(item, this.branchId, ex);
                return false;
            }
        }

        boolean post(T item) {

            boolean accepted = this.target.post(item);
            if (accepted) {
                this.deliveredManager.sendOnDeliveredNotification(this.target, item);
            }

            return accepted;
        }

        void onLinked() {
            this.target.onLinked();
        }

        int inFlight() {
            return this.target.inFlight();
        }

        void complete() {
            this.target.complete();
        }

        CompletableFuture<Void> completion() {
            return this.target.completion();
        }
    }
}
