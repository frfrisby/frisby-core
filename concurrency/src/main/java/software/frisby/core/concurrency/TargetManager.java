package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("ALL")
final class TargetManager<T> {
    private final Object source;
    private final EventSource eventSource;

    private final ItemDeliveredManager<T> deliveredManager;
    private final ErrorOccurredManager<T> errorManager;

    private final CountDownLatch targetLatch;

    private final String blockName;

    private final AtomicReference<Link<T>> linkedTarget;

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

        this.linkedTarget = new AtomicReference<>(null);
        this.targetLatch = new CountDownLatch(1);
    }

    TargetManager(Object source,
                  EventSource eventSource,
                  ItemDeliveredHandler<T> itemDeliveredHandler) {
        this(source, eventSource, itemDeliveredHandler, null);
    }

    void postToTarget(T item) {
        if (null != item) {
            this.linkedTarget.get().post(item);
        }
    }

    /**
     * Posts {@code item} to the linked target, waiting up to {@code timeout} for it to be
     * accepted.
     *
     * <p>Unlike {@link #postToTarget(Object)}, this method returns the real acceptance result —
     * it is new code, not shared with the no-timeout path above, so it can report {@code true}/
     * {@code false} correctly from the outset without touching that method's long-standing
     * (unrelated) discard-the-result behavior.</p>
     *
     * @param item    The item to post; a {@code null} item is a no-op that returns {@code false}.
     * @param timeout The maximum time to wait for the linked target to accept the item.
     * @return {@code true} if the item was accepted before {@code timeout} elapsed;
     * {@code false} if {@code item} was null, or {@code timeout} elapsed before the item could
     * be accepted.
     */
    boolean postToTarget(T item, Duration timeout) {
        if (null != item) {
            return this.linkedTarget.get().post(item, timeout);
        }

        return false;
    }

    void add(Target<T> target) {
        Values.notNull("target", target);

        if (source == target) {
            throw new IllegalArgumentException("The 'target' value is invalid.  A block cannot be linked to itself.");
        }

        Link<T> newLink = new Link<>(this.deliveredManager, this.errorManager, target);

        if (!this.linkedTarget.compareAndSet(null, newLink)) {
            throw new IllegalStateException(
                    String.format(
                            "The '%s' block already has a linked target.  A single-target block may only be linked to one downstream target.",
                            this.blockName
                    )
            );
        }

        target.onLinked();

        this.targetLatch.countDown();
    }

    void awaitTargets() {
        // Fast path: the target is already linked (steady-state for every post() call after
        // the first linkTo()).  A single volatile read of linkedTarget short-circuits the
        // CountDownLatch AQS machinery (Thread.interrupted() + state volatile-read + branch)
        // that await() incurs even when the latch is already open.
        if (null != this.linkedTarget.get()) {
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
        Link<T> link = this.linkedTarget.get();

        if (null != link) {
            link.target.complete();
        }
    }

    int inFlight() {
        Link<T> link = this.linkedTarget.get();

        if (null == link) {
            return 0;
        }

        return link.target.inFlight();
    }

    CompletableFuture<Void> completion() {
        Link<T> link = this.linkedTarget.get();

        if (null == link) {
            return CompletableFuture.completedFuture(null);
        }

        return link.target.completion();
    }

    private static final class Link<T> {
        private final Target<T> target;
        private final ItemDeliveredManager<T> deliveredManager;
        private final ErrorOccurredManager<T> errorManager;

        private Link(ItemDeliveredManager<T> deliveredManager,
                     ErrorOccurredManager<T> errorManager,
                     Target<T> target) {
            this.deliveredManager = deliveredManager;
            this.errorManager = errorManager;
            this.target = target;
        }

        @SuppressWarnings("java:S1181")
        void post(T item) {
            if (this.errorManager.hasHandler()) {
                try {
                    boolean accepted = this.target.post(item);

                    if (accepted) {
                        this.deliveredManager.sendOnDeliveredNotification(this.target, item);
                    }
                } catch (Throwable t) {
                    // Fatal JVM conditions propagate immediately; everything else is logged
                    // and, since a handler is configured, forwarded to it — consistent with
                    // the non-fatal/fatal split applied to worker delivery loops.
                    Errors.throwIfFatal(t);
                    this.errorManager.sendOnErrorNotification(this.target, item, t);
                }
            } else {
                boolean accepted = this.target.post(item);

                // Only notify on a genuine acceptance — a clean `false` return (e.g. the
                // downstream target has already been completed) is not a delivery, and
                // ItemDeliveredHandler's own contract promises "successfully delivered."
                if (accepted) {
                    this.deliveredManager.sendOnDeliveredNotification(this.target, item);
                }
            }
        }

        @SuppressWarnings("java:S1181")
        boolean post(T item, Duration timeout) {
            if (this.errorManager.hasHandler()) {
                try {
                    boolean accepted = this.target.post(item, timeout);

                    if (accepted) {
                        this.deliveredManager.sendOnDeliveredNotification(this.target, item);
                    }

                    return accepted;
                } catch (Throwable t) {
                    // Fatal JVM conditions propagate immediately; everything else is logged
                    // and, since a handler is configured, forwarded to it — consistent with
                    // the non-fatal/fatal split applied to worker delivery loops.
                    Errors.throwIfFatal(t);
                    this.errorManager.sendOnErrorNotification(this.target, item, t);

                    return false;
                }
            } else {
                boolean accepted = this.target.post(item, timeout);

                if (accepted) {
                    this.deliveredManager.sendOnDeliveredNotification(this.target, item);
                }

                return accepted;
            }
        }
    }
}
