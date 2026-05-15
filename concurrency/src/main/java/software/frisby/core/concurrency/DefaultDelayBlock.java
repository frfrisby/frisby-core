package software.frisby.core.concurrency;

import software.frisby.core.validation.Durations;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

final class DefaultDelayBlock<T> implements DelayBlock<T> {
    static final int DEFAULT_CAPACITY = 1024;

    private final Worker<T> worker;
    private final TargetManager<T> targetManager;
    private final Function<T, Duration> delayFunction;
    private final int capacity;

    private final ItemPostedManager<T> postedManager;
    private final AtomicBoolean completed;
    private final AtomicInteger pendingCompletes;
    private final CompletableFuture<Void> completionFuture;
    private final WorkerLifecycle lifecycle;

    DefaultDelayBlock(BlockingQueue<DelayedEntry<T>> queue,
                      int capacity,
                      Function<T, Duration> delayFunction,
                      Executor executor,
                      ItemPostedHandler<T> itemPostedHandler,
                      ItemDeliveredHandler<T> itemDeliveredHandler,
                      ErrorOccurredHandler<T> errorOccurredHandler) {
        Sequences.notNull("queue", queue);
        Numbers.positive("capacity", capacity);
        Values.notNull("delayFunction", delayFunction);
        Values.notNull("executor", executor);

        EventSource eventSource = new EventSource(DelayBlock.class.getSimpleName());
        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler, errorOccurredHandler);
        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);

        this.delayFunction = delayFunction;
        this.capacity = capacity;

        this.completed = new AtomicBoolean(false);
        this.pendingCompletes = new AtomicInteger(0);
        this.completionFuture = new CompletableFuture<>();
        this.lifecycle = new WorkerLifecycle();

        CapacityGate capacityGate = new CapacityGate(capacity);

        // Wire: when the worker thread exits after a drain, propagate completion downstream.
        // The lambda runs on the worker thread immediately after lifecycle.finish() resolves
        // its future.  completed.get() distinguishes a graceful drain from an external
        // executor shutdown — only the former propagates to downstream targets.
        this.lifecycle.completion().thenRun(() -> {
            if (this.completed.get()) {
                this.targetManager.complete();
                this.targetManager.completion()
                        .thenAccept(v -> this.completionFuture.complete(null));
            }
        });

        this.worker = new Worker<>(queue, this.targetManager, capacityGate, this.lifecycle);
        executor.execute(this.worker);
    }

    @Override
    public boolean post(T item) {
        if (this.completed.get()) {
            return false;
        }

        this.targetManager.awaitTargets();

        if (null != item) {
            try {
                this.worker.put(new DelayedEntry<>(item, this.delayFunction.apply(item)));
                this.postedManager.sendOnPostedNotification(item, true);

                return true;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return false;
    }

    @Override
    public boolean post(T item, Duration timeout) {
        if (this.completed.get()) {
            return false;
        }

        Durations.positive("timeout", timeout);

        this.targetManager.awaitTargets();

        if (null != item) {
            try {
                if (this.worker.put(new DelayedEntry<>(item, this.delayFunction.apply(item)), timeout)) {
                    this.postedManager.sendOnPostedNotification(item, true);

                    return true;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return false;
    }

    @Override
    public void linkTo(Target<T> target) {
        this.targetManager.add(target);
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    @Override
    public int size() {
        return this.worker.size();
    }

    @Override
    public int inFlight() {
        return size() + this.targetManager.inFlight();
    }

    boolean isRunning() {
        return this.lifecycle.isRunning();
    }

    @Override
    public void onLinked() {
        this.pendingCompletes.incrementAndGet();
    }

    @Override
    public void complete() {
        if (this.pendingCompletes.decrementAndGet() <= 0 &&
                this.completed.compareAndSet(false, true)) {
            this.worker.drain();
        }
    }

    @Override
    public CompletableFuture<Void> completion() {
        return this.completionFuture;
    }

    private static final class Worker<T> implements Runnable {
        private final TargetManager<T> targetManager;
        private final BlockingQueue<DelayedEntry<T>> queue;
        private final CapacityGate capacityGate;
        private final WorkerLifecycle lifecycle;

        // workerThread is written once by the worker thread and read by drain() on a calling
        // thread.  AtomicReference provides the required cross-thread visibility guarantee
        // without the false-positive Sonar S3077 warning that volatile triggers on object refs.
        private final AtomicReference<Thread> workerThread = new AtomicReference<>();
        private volatile boolean draining;

        private Worker(BlockingQueue<DelayedEntry<T>> queue,
                       TargetManager<T> targetManager,
                       CapacityGate capacityGate,
                       WorkerLifecycle lifecycle) {
            this.targetManager = targetManager;
            this.queue = queue;
            this.capacityGate = capacityGate;
            this.lifecycle = lifecycle;
        }

        @Override
        public void run() {
            this.workerThread.set(Thread.currentThread());
            this.lifecycle.start();

            boolean exit = false;
            while (!exit) {
                try {
                    // If draining, flush any remaining items without blocking in take() —
                    // this handles both the normal drain path (draining set while we were
                    // processing) and the race where drain() was called before this thread
                    // started (workerThread was null so no interrupt was sent at the time).
                    if (this.draining) {
                        if (!this.queue.isEmpty()) {
                            flushRemaining();
                        }

                        break;
                    }

                    this.targetManager.postToTarget(this.queue.take().item());

                    // Release the capacity permit now that the item has been fully delivered.
                    // This must happen after postToTarget() returns, not when take() is called,
                    // so that downstream back-pressure is correctly propagated back to posting threads.
                    this.capacityGate.release();

                    if (this.draining && this.queue.isEmpty()) {
                        exit = true;
                    }
                } catch (InterruptedException ex) {
                    if (this.draining) {
                        flushRemaining();
                    }

                    Thread.currentThread().interrupt();
                    exit = true;
                }
            }

            // WorkerLifecycle enforces the ordering invariant: isRunning is set to false
            // BEFORE the completion future resolves, so any thread waiting on completion()
            // is guaranteed to observe isRunning() == false when it wakes.
            this.lifecycle.finish();
        }

        private void flushRemaining() {
            // Deliver all items still in the queue immediately, disregarding unexpired delays.
            // Sorting by natural order (ascending remaining delay) delivers items closest to
            // expiry first, which is the most intuitive ordering on drain.
            // The interrupt flag is already cleared on entry from the catch block; for the
            // direct (non-interrupt) path there is no interrupt to clear.  Either way we do
            // NOT re-interrupt during the flush so that postToTarget() and capacityGate
            // operations are unaffected.
            List<DelayedEntry<T>> remaining = new ArrayList<>(this.queue);
            this.queue.clear();

            remaining.sort(null);

            for (DelayedEntry<T> entry : remaining) {
                this.targetManager.postToTarget(entry.item());
                this.capacityGate.release();
            }
        }


        void put(DelayedEntry<T> item) throws InterruptedException {
            this.capacityGate.acquire();

            try {
                this.queue.put(item);
            } catch (InterruptedException ex) {
                this.capacityGate.release();
                throw ex;
            }
        }

        boolean put(DelayedEntry<T> item, Duration timeout) throws InterruptedException {
            if (!this.capacityGate.tryAcquire(timeout.toNanos())) {
                return false;
            }

            try {
                this.queue.put(item);
            } catch (InterruptedException ex) {
                this.capacityGate.release();
                throw ex;
            }

            return true;
        }

        int size() {
            return this.queue.size();
        }

        void drain() {
            this.draining = true;

            Thread t = this.workerThread.get();

            // Always interrupt the worker when it is alive — even when the queue is non-empty.
            //
            // The worker blocks in DelayQueue.take() waiting for the next item's delay to expire;
            // without the interrupt it would sit there for the full remaining delay before it
            // could detect the draining flag.  The InterruptedException handler delivers all
            // remaining items immediately, bypassing unexpired delays.

            if (null != t) {
                t.interrupt();
            }
        }
    }
}
