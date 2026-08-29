package software.frisby.core.concurrency;


import software.frisby.core.validation.Durations;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

final class DefaultGroupBlock<T, K> implements GroupBlock<T> {
    static final int DEFAULT_CAPACITY = 1024;
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_IDLE_TIMEOUT = Duration.ofSeconds(5);
    static final int DEFAULT_MAX_GROUP_SIZE = 128;

    private final CompletableQueue<T> completableQueue;
    private final TargetManager<List<T>> targetManager;
    private final ItemPostedManager<T> postedManager;
    private final int capacity;
    private final int maxGroupSize;
    private final CompletableFuture<Void> completionFuture;
    private final Duration timeout;
    private final Duration idleTimeout;
    private final CapacityGate capacityGate;
    private final WorkerLifecycle lifecycle;

    // Tracks items currently mid-delivery to the downstream target — i.e. inside
    // publishGroup()'s postToTarget() call, after capacityGate.acquire() but before
    // capacityGate.release().  Decoupled from capacityGate itself, which must keep its current
    // acquire/release timing unchanged to correctly bound concurrent processing.  Subtracting
    // this from size() aligns Group's size()/inFlight() semantics with Buffer/Delay: size()
    // reports only items waiting to be grouped, not items currently being handed to the
    // downstream target, so a correctly self-reporting target's own inFlight() is the sole
    // source of truth for that window instead of being double-counted alongside size().
    private final AtomicInteger deliveringCount;

    // Production constructor — called by DefaultGroupBlockBuilder.  Delegates to the
    // queue-accepting constructor via createQueue(), which validates capacity and constructs
    // the ArrayBlockingQueue.  This ordering ensures Numbers.positive fires before
    // ArrayBlockingQueue is constructed, so callers always receive the project-standard
    // exception type rather than IllegalArgumentException.
    @SuppressWarnings("java:S107")
    DefaultGroupBlock(Function<T, K> groupingFunction,
                      Duration timeout,
                      Duration idleTimeout,
                      int capacity,
                      int maxGroupSize,
                      Executor executor,
                      ItemPostedHandler<T> itemPostedHandler,
                      ItemDeliveredHandler<List<T>> itemDeliveredHandler,
                      ErrorOccurredHandler<List<T>> errorOccurredHandler,
                      GroupObserver<T, K> groupObserver) {
        this(
                new ArrayBlockingQueue<>(Numbers.positive("capacity", capacity)),
                groupingFunction,
                timeout,
                idleTimeout,
                capacity,
                maxGroupSize,
                executor,
                itemPostedHandler,
                itemDeliveredHandler,
                errorOccurredHandler,
                groupObserver
        );
    }

    // Package-private constructor used in tests to inject a custom BlockingQueue
    // (e.g. MockInterruptedQueue) for deterministic coverage of interrupt paths.
    @SuppressWarnings("java:S107")
    DefaultGroupBlock(BlockingQueue<T> queue,
                      Function<T, K> groupingFunction,
                      Duration timeout,
                      Duration idleTimeout,
                      int capacity,
                      int maxGroupSize,
                      Executor executor,
                      ItemPostedHandler<T> itemPostedHandler,
                      ItemDeliveredHandler<List<T>> itemDeliveredHandler,
                      ErrorOccurredHandler<List<T>> errorOccurredHandler,
                      GroupObserver<T, K> groupObserver) {
        Sequences.notNull("queue", queue);
        Values.notNull("groupingFunction", groupingFunction);
        Durations.positive("timeout", timeout);
        Durations.positive("idleTimeout", idleTimeout);
        Numbers.positive("capacity", capacity);
        Numbers.positive("maxGroupSize", maxGroupSize);
        Values.notNull("executor", executor);

        this.timeout = timeout;
        this.idleTimeout = idleTimeout;
        this.capacity = capacity;
        this.maxGroupSize = maxGroupSize;
        this.capacityGate = new CapacityGate(capacity);
        this.completableQueue = new CompletableQueue<>(queue);
        this.lifecycle = new WorkerLifecycle();
        this.deliveringCount = new AtomicInteger(0);

        EventSource eventSource = new EventSource("GroupBlock");
        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler, errorOccurredHandler);
        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);

        this.completionFuture = new CompletableFuture<>();

        executor.execute(new Worker<>(
                this.completableQueue,
                groupingFunction,
                this.targetManager,
                this.completionFuture,
                timeout,
                idleTimeout,
                maxGroupSize,
                errorOccurredHandler,
                groupObserver,
                this,
                eventSource,
                this.capacityGate,
                this.lifecycle,
                this.deliveringCount
        ));
    }

    @Override
    public boolean post(T item) {
        if (this.completableQueue.isCompleted()) {
            return false;
        }

        this.targetManager.awaitTargets();

        if (null != item) {
            try {
                this.capacityGate.acquire();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }

            boolean enqueued = this.completableQueue.enqueue(item);
            if (!enqueued) {
                this.capacityGate.release();
                return false;
            }

            this.postedManager.sendOnPostedNotification(item, true);

            return true;
        }

        return false;
    }

    @Override
    public boolean post(T item, Duration timeout) {
        if (this.completableQueue.isCompleted()) {
            return false;
        }

        Durations.positive("timeout", timeout);

        this.targetManager.awaitTargets();

        if (null != item) {
            boolean acquired;

            try {
                acquired = this.capacityGate.tryAcquire(timeout.toNanos());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }

            if (!acquired) {
                return false;
            }

            boolean enqueued = this.completableQueue.enqueue(item);
            if (!enqueued) {
                this.capacityGate.release();
                return false;
            }

            this.postedManager.sendOnPostedNotification(item, true);

            return true;
        }

        return false;
    }

    @Override
    public void linkTo(Target<List<T>> target) {
        this.targetManager.add(target);
    }

    @Override
    public Duration timeout() {
        return this.timeout;
    }

    @Override
    public Duration idleTimeout() {
        return this.idleTimeout;
    }

    @Override
    public int maxGroupSize() {
        return this.maxGroupSize;
    }

    @Override
    public int size() {
        // Derive from CapacityGate: in-flight items = capacity − available permits.
        // The semaphore acquires one permit per accepted item and releases on delivery,
        // so this is always equivalent to the removed AtomicInteger pendingItems.
        //
        // deliveringCount is subtracted to exclude items currently mid-delivery (inside
        // publishGroup()'s postToTarget() call), matching Buffer/Delay's raw-queue-based size()
        // semantics — see deliveringCount's field Javadoc for the full rationale.
        return this.capacity - this.capacityGate.available() - this.deliveringCount.get();
    }

    @Override
    public int inFlight() {
        return size() + this.targetManager.inFlight();
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    boolean isRunning() {
        return this.lifecycle.isRunning();
    }

    @Override
    public void onLinked() {
        this.completableQueue.onLinked();
    }

    @Override
    public void complete() {
        this.completableQueue.complete();
    }

    @Override
    public CompletableFuture<Void> completion() {
        return this.completionFuture;
    }

    private static final class Worker<T, K> implements Runnable {
        private final CompletableQueue<T> completableQueue;
        private final Function<T, K> groupingFunction;
        private final TargetManager<List<T>> targetManager;
        private final Map<K, Group<T, K>> groups;
        private final List<K> expired;
        private final CompletableFuture<Void> completionFuture;
        private final long maxTimeoutMs;
        private final long idleTimeoutMs;
        private final int maxGroupSize;
        private final ErrorOccurredManager<List<T>> errorManager;
        private final GroupObserver<T, K> observer;
        private final CapacityGate capacityGate;
        private final WorkerLifecycle lifecycle;
        private final EventSource eventSource;
        private final AtomicInteger deliveringCount;

        // Tracks the soonest expiry deadline across all active groups, in epoch millis.
        // Maintained as a lower-bound cache: it is only ever updated to a smaller (sooner)
        // value via updateNextDeadline(), and is recomputed fully by flushExpiredGroups()
        // whenever the scan actually runs.  This keeps both computeWaitMs() and the
        // flushExpiredGroups() fast path at O(1) per item in the common case.
        private long nextDeadline = Long.MAX_VALUE;

        @SuppressWarnings("java:S107")
        private Worker(CompletableQueue<T> completableQueue,
                       Function<T, K> groupingFunction,
                       TargetManager<List<T>> targetManager,
                       CompletableFuture<Void> completionFuture,
                       Duration timeout,
                       Duration idleTimeout,
                       int maxGroupSize,
                       ErrorOccurredHandler<List<T>> errorOccurredHandler,
                       GroupObserver<T, K> observer,
                       Object source,
                       EventSource eventSource,
                       CapacityGate capacityGate,
                       WorkerLifecycle lifecycle,
                       AtomicInteger deliveringCount) {
            this.completableQueue = completableQueue;
            this.groupingFunction = groupingFunction;
            this.targetManager = targetManager;
            this.completionFuture = completionFuture;
            this.maxTimeoutMs = timeout.toMillis();
            this.idleTimeoutMs = idleTimeout.toMillis();
            this.maxGroupSize = maxGroupSize;
            this.groups = new HashMap<>();
            this.expired = new ArrayList<>();
            this.errorManager = new ErrorOccurredManager<>(source, eventSource, errorOccurredHandler);
            this.observer = observer;
            this.capacityGate = capacityGate;
            this.lifecycle = lifecycle;
            this.eventSource = eventSource;
            this.deliveringCount = deliveringCount;
        }

        @Override
        @SuppressWarnings("java:S135")
        public void run() {
            this.lifecycle.start();

            try {
                while (true) {
                    T item = takeOrPoll();

                    if (null == item) {
                        if (this.completableQueue.isCompleted()) {
                            break;  // Normal drain: queue completed + all items consumed.
                        }

                        if (Thread.currentThread().isInterrupted()) {
                            break;  // External interrupt (e.g. NamedExecutorService.shutdown()).
                        }

                        // dequeue(waitMs) timed out — a group deadline may have expired.
                        // Fall through to flushExpiredGroups() to publish any expired groups.
                    } else {
                        processItem(item);
                    }

                    flushExpiredGroups();
                }

                // Flush any groups that have not yet been published.  The interrupt flag may be
                // set if complete() and an external interrupt arrived concurrently; clear it
                // before the flush so CapacityGate.acquire() inside publishGroup() does not
                // throw, then restore it afterward.  publishGroup() logs and swallows any
                // non-fatal Throwable, so flushAllGroups() only propagates on a genuinely fatal
                // condition (VirtualMachineError/LinkageError) — which the outer try/finally
                // around this method body already handles correctly via lifecycle.finish().
                boolean interrupted = Thread.interrupted();

                if (this.completableQueue.isCompleted()) {
                    flushAllGroups();
                }

                if (interrupted) {
                    Thread.currentThread().interrupt();
                }

                // Normal exit only — a fatal error thrown above (from publishGroup() via
                // flushAllGroups()/flushExpiredGroups()/processItem()) propagates past this
                // point, skipping finish() so completion() never resolves on fatal death.
                this.lifecycle.finish();

                if (this.completableQueue.isCompleted()) {
                    this.targetManager.complete();
                    this.targetManager.completion()
                            .thenAccept(v -> this.completionFuture.complete(null));
                }
            } finally {
                // Safety net reached on every exit path, including a fatal error.  Guarantees
                // isRunning() is never left reporting true forever, without touching the
                // completion future — see WorkerLifecycle.stopRunning()'s Javadoc.  A harmless
                // no-op re-affirmation on the normal exit path, where finish() above already
                // set isRunning to false.
                this.lifecycle.stopRunning();
            }
        }

        // Blocks until an item arrives or the soonest per-group deadline expires.
        // When no groups are pending, dequeue() is used to avoid busy-waiting.
        // When groups are pending, dequeue(waitMs) wakes up at the earliest expiry time.
        // When a deadline has already passed, waitMs is 0 and dequeue(0) returns immediately.
        private T takeOrPoll() {
            long waitMs = computeWaitMs();
            if (waitMs == Long.MAX_VALUE) {
                return this.completableQueue.dequeue();
            }

            return this.completableQueue.dequeue(waitMs, TimeUnit.MILLISECONDS);
        }

        // Computes how long to wait for the next item.  Returns Long.MAX_VALUE when there are
        // no active groups, signaling that dequeue() should be used.  Returns 0 when at least
        // one group deadline has already passed, signaling an immediate non-blocking poll.
        // O(1) — reads the cached nextDeadline rather than scanning all groups.
        private long computeWaitMs() {
            if (this.groups.isEmpty()) {
                this.nextDeadline = Long.MAX_VALUE;
                return Long.MAX_VALUE;
            }

            long now = System.currentTimeMillis();

            return Math.max(0L, this.nextDeadline - now);
        }

        private void processItem(T item) {
            K key = this.groupingFunction.apply(item);

            Group<T, K> group = this.groups.get(key);
            if (null == group) {
                group = new Group<>(key);
                this.groups.put(key, group);
            }

            group.add(item);

            Retention retention = notifyObserver(group);

            if (Retention.RELEASE == retention || group.size() >= this.maxGroupSize) {
                publishGroup(key, group);
            } else {
                updateNextDeadline(group);
            }
        }

        // Scans all active groups and publishes any whose max-timeout or idle-timeout has
        // expired.
        //
        // The fast path exits in O(1) when nextDeadline has not yet been reached.  The full O(n) scan
        // runs only when at least one group has expired.  The scan also recomputes nextDeadline in the
        // same pass so no second traversal is needed.
        private void flushExpiredGroups() {
            long now = System.currentTimeMillis();

            if (now < this.nextDeadline) {
                return;
            }

            long earliest = Long.MAX_VALUE;

            for (Map.Entry<K, Group<T, K>> entry : this.groups.entrySet()) {
                Group<T, K> group = entry.getValue();
                long deadline = Math.min(
                        group.createdAtMs() + this.maxTimeoutMs,
                        group.lastModifiedMs() + this.idleTimeoutMs
                );

                if (now >= deadline) {
                    this.expired.add(entry.getKey());
                } else {
                    earliest = Math.min(earliest, deadline);
                }
            }

            for (K key : this.expired) {
                publishGroup(key, this.groups.get(key));
            }

            this.expired.clear();

            this.nextDeadline = earliest;
        }

        private void flushAllGroups() {
            List<K> keys = new ArrayList<>(this.groups.keySet());

            for (K key : keys) {
                publishGroup(key, this.groups.get(key));
            }
        }

        private void updateNextDeadline(Group<T, K> group) {
            long deadline = Math.min(
                    group.createdAtMs() + this.maxTimeoutMs,
                    group.lastModifiedMs() + this.idleTimeoutMs
            );

            if (deadline < this.nextDeadline) {
                this.nextDeadline = deadline;
            }
        }

        @SuppressWarnings("java:S1181")
        private void publishGroup(K key, Group<T, K> group) {
            List<T> batch = group.toList();

            this.deliveringCount.addAndGet(batch.size());

            try {
                this.targetManager.postToTarget(batch);
            } catch (Throwable t) {
                // Fatal JVM conditions propagate immediately; everything else is logged and
                // the worker continues rather than dying silently and permanently.
                Errors.throwIfFatal(t);
                this.eventSource.createErrorEvent(t);
            } finally {
                this.deliveringCount.addAndGet(-batch.size());
                this.capacityGate.release(batch.size());
                this.groups.remove(key);
            }
        }

        @SuppressWarnings("java:S1181")
        private Retention notifyObserver(Group<T, K> group) {
            try {
                if (null != this.observer) {
                    return this.observer.onModified(group);
                }
            } catch (Throwable t) {
                // Fatal JVM conditions propagate immediately, killing the worker as intended.
                // Everything else — including non-fatal Errors, not just ordinary
                // Exceptions — is forwarded to the error handler rather than allowed to crash
                // the worker.  The group is retained (HOLD) when the observer throws, ensuring
                // items are still delivered at timeout.
                Errors.throwIfFatal(t);
                this.errorManager.sendOnErrorNotification(this.observer, group.toList(), t);
            }

            return Retention.HOLD;
        }
    }
}
