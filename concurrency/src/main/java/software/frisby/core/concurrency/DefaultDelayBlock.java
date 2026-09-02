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
import java.util.concurrent.ExecutorService;
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
                      ExecutorService executor,
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
        // its future. WorkerLifecycle.finish() is constructed with interruptionSignalsCompletion
        // == false (see the no-arg WorkerLifecycle() below), so it only ever resolves
        // completion() via the graceful drain path — which always sets this.completed = true
        // (in complete(), strictly before drain() runs) before that path can be reached. A hard
        // executor shutdown with no prior complete() call instead leaves the calling thread
        // interrupted at finish() time, so completion() is deliberately left unresolved and this
        // callback never runs at all.  There is no live case where this callback observes
        // this.completed == false.
        this.lifecycle.completion().thenRun(() -> {
            this.targetManager.complete();
            this.targetManager.completion()
                    .thenAccept(v -> this.completionFuture.complete(null));
        });

        this.worker = new Worker<>(queue, this.targetManager, capacityGate, this.lifecycle, eventSource);
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

        Durations.notNegative("timeout", timeout);

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
        private final EventSource eventSource;

        // workerThread is written once by the worker thread and read by drain() on a calling
        // thread.  AtomicReference provides the required cross-thread visibility guarantee
        // without the false-positive Sonar S3077 warning that volatile triggers on object refs.
        //
        // Deliberately NOT tracked via ExecutorService.submit() + Future.cancel(true): FutureTask
        // (the Future implementation submit() wraps every task in) catches every Throwable —
        // including fatal VirtualMachineError/LinkageError — and stores it via setException()
        // rather than letting it propagate, which would silently defeat this module's
        // "fatal errors escape as genuinely uncaught exceptions" contract (see Errors.throwIfFatal
        // and WorkerLifecycle's class-level Javadoc).  execute() plus a manually tracked Thread
        // has no such wrapper, so a fatal error thrown from deliverAndRelease() still propagates
        // straight out of run() exactly as it does for every other async block.
        private final AtomicReference<Thread> workerThread = new AtomicReference<>();
        private volatile boolean draining;

        private Worker(BlockingQueue<DelayedEntry<T>> queue,
                       TargetManager<T> targetManager,
                       CapacityGate capacityGate,
                       WorkerLifecycle lifecycle,
                       EventSource eventSource) {
            this.targetManager = targetManager;
            this.queue = queue;
            this.capacityGate = capacityGate;
            this.lifecycle = lifecycle;
            this.eventSource = eventSource;
        }

        @Override
        @SuppressWarnings("java:S3776")
        public void run() {
            this.workerThread.set(Thread.currentThread());
            this.lifecycle.start();

            try {
                boolean exit = false;
                while (!exit) {
                    try {
                        // If draining, flush any remaining items without blocking in take().
                        //
                        // This handles both the normal drain path (draining set while we were
                        // processing) and the race where drain() was called before this thread
                        // started (workerThread was null so no interrupt was sent at the time).
                        if (this.draining) {
                            if (!this.queue.isEmpty()) {
                                flushRemaining();
                            }

                            break;
                        }

                        deliverAndRelease(this.queue.take().item());

                        if (this.draining && this.queue.isEmpty()) {
                            exit = true;
                        }
                    } catch (InterruptedException ex) {
                        if (this.draining) {
                            // Our own drain()-triggered wake-up — already fully handled by
                            // flushRemaining() above. The JDK clears the interrupt status as
                            // part of throwing InterruptedException; deliberately leave it
                            // cleared here (do NOT restore it) so this fully successful,
                            // graceful drain lets WorkerLifecycle.finish() resolve completion()
                            // normally, rather than being mistaken for an abnormal early exit.
                            flushRemaining();
                        } else {
                            // A genuine external interrupt with no drain ever requested (e.g. a
                            // hard executor shutdown) — restore the flag so finish() correctly
                            // treats this as an abnormal early exit and leaves completion()
                            // unresolved.
                            Thread.currentThread().interrupt();
                        }

                        exit = true;
                    }
                }

                // Normal exit only — a fatal error thrown from deliverAndRelease() (directly,
                // or via flushRemaining()) propagates past this point, skipping finish() so
                // completion() never resolves on fatal death.
                //
                // A graceful, requested drain (this.draining == true) may still leave the
                // interrupt flag set here even without going through the catch block above —
                // drain() unconditionally interrupts this thread as its only way to wake a
                // take() call blocked on an unexpired delay, but if this thread wasn't actually
                // blocked in take() at that exact instant (e.g. it observed the draining flag
                // at the top of the loop instead), that interrupt is never consumed by a thrown
                // InterruptedException and would otherwise linger here. Clear it so a fully
                // successful drain always lets finish() resolve completion() normally, rather
                // than being mistaken for an abnormal early exit.
                if (this.draining) {
                    Thread.interrupted();
                }

                this.lifecycle.finish();
            } finally {
                // Safety net reached on every exit path, including a fatal error.  Guarantees
                // isRunning() is never left reporting true forever, without touching the
                // completion future — see WorkerLifecycle.stopRunning()'s Javadoc.  A harmless
                // no-op re-affirmation on the normal exit path, where finish() above already
                // set isRunning to false.
                this.lifecycle.stopRunning();
            }
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
                deliverAndRelease(entry.item());
            }
        }

        // Delivers a single item to the downstream target and releases its capacity permit,
        // regardless of whether delivery succeeded.  Fatal JVM conditions propagate immediately;
        // everything else is logged and the worker continues with the next item rather than
        // dying silently.
        @SuppressWarnings("java:S1181")
        private void deliverAndRelease(T item) {
            try {
                this.targetManager.postToTarget(item);
            } catch (Throwable t) {
                Errors.throwIfFatal(t);
                this.eventSource.createErrorEvent(t);
            } finally {
                // Release the capacity permit now that delivery has been attempted.  This must
                // happen after postToTarget() returns (or throws), not when the item was
                // dequeued, so that downstream back-pressure is correctly propagated back to
                // posting threads.
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
            // The worker blocks in DelayQueue.take() waiting for the next item's delay to expire.
            // Without the interrupt it would sit there for the full remaining delay before it
            // could detect the draining flag.  The InterruptedException handler delivers all
            // remaining items immediately, bypassing unexpired delays.

            if (null != t) {
                t.interrupt();
            }
        }
    }
}
