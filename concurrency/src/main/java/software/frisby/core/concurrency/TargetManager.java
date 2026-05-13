package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("ALL")
final class TargetManager<T> {
    private final Object source;
    private final EventSource eventSource;

    private final ItemDeliveredManager<T> deliveredManager;
    private final ErrorOccurredManager<T> errorManager;

    private final CountDownLatch targetLatch;

    private final String blockName;

    private volatile Link<T> linkedTarget;

    TargetManager(Object source,
                  EventSource eventSource,
                  ItemDeliveredHandler<T> itemDeliveredHandler,
                  ErrorOccurredHandler<T> errorOccurredHandler) {
        Values.notNull("source", source);
        Values.notNull("eventSource", eventSource);

        this.source = source;

        this.eventSource = eventSource;

        this.deliveredManager = new ItemDeliveredManager<>(source, eventSource, itemDeliveredHandler);
        this.errorManager = new ErrorOccurredManager<>(source, eventSource, errorOccurredHandler);

        this.blockName = eventSource.sourceName();

        this.linkedTarget = null;
        this.targetLatch = new CountDownLatch(1);
    }

    TargetManager(Object source,
                  EventSource eventSource,
                  ItemDeliveredHandler<T> itemDeliveredHandler) {
        this(source, eventSource, itemDeliveredHandler, null);
    }

    void postToTarget(T item) {
        if (null != item) {
            this.linkedTarget.post(item);
        }
    }

    void add(Target<T> target) {
        Values.notNull("target", target);

        if (null != this.linkedTarget) {
            throw new IllegalStateException(
                    String.format(
                            "The '%s' block already has a linked target.  A single-target block may only be linked to one downstream target.",
                            this.blockName
                    )
            );
        }

        if (source == target) {
            throw new IllegalArgumentException("The 'target' value is invalid.  A block cannot be linked to itself.");
        }

        this.linkedTarget = new Link<>(this.deliveredManager, this.errorManager, target, this.eventSource);
        target.onLinked();

        this.targetLatch.countDown();
    }

    void awaitTargets() {
        // Fast path: the target is already linked (steady-state for every post() call after
        // the first linkTo()).  A single volatile read of linkedTarget short-circuits the
        // CountDownLatch AQS machinery (Thread.interrupted() + state volatile-read + branch)
        // that await() incurs even when the latch is already open.
        if (null != this.linkedTarget) {
            return;
        }

        this.eventSource.createNoTargetLinkedWarningEvent();

        try {
            this.targetLatch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    void complete() {
        if (null != this.linkedTarget) {
            this.linkedTarget.target.complete();
        }
    }

    int inFlight() {
        if (null == this.linkedTarget) {
            return 0;
        }

        return this.linkedTarget.target.inFlight();
    }

    CompletableFuture<Void> completion() {
        if (null == this.linkedTarget) {
            return CompletableFuture.completedFuture(null);
        }

        return this.linkedTarget.target.completion();
    }

    private static final class Link<T> {
        private final Target<T> target;
        private final EventSource eventSource;

        private final ItemDeliveredManager<T> deliveredManager;
        private final ErrorOccurredManager<T> errorManager;

        private Link(ItemDeliveredManager<T> deliveredManager,
                     ErrorOccurredManager<T> errorManager,
                     Target<T> target,
                     EventSource eventSource) {
            this.deliveredManager = deliveredManager;
            this.errorManager = errorManager;
            this.target = target;
            this.eventSource = eventSource;
        }

        void post(T item) {

            if (this.errorManager.hasHandler()) {
                try {
                    this.target.post(item);
                    this.deliveredManager.sendOnDeliveredNotification(this.target, item);
                } catch (Exception ex) {
                    this.errorManager.sendOnErrorNotification(this.target, item, ex);
                }
            } else {
                this.target.post(item);
                this.deliveredManager.sendOnDeliveredNotification(this.target, item);
            }
        }
    }
}
