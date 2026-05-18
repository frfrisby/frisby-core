package software.frisby.core.concurrency;


import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;


@SuppressWarnings("ALL")
final class DefaultSourceBlock<T> implements SourceBlock<T> {
    private final TargetManager<T> targetManager;
    private final List<WorkerLifecycle> lifecycles;
    private final CountDownLatch startLatch;

    DefaultSourceBlock(Supplier<T> singleItemSupplier,
                       Supplier<List<T>> batchSupplier,
                       SourceConcurrencyPolicy policy,
                       Executor executor,
                       ItemDeliveredHandler<T> itemDeliveredHandler,
                       ErrorOccurredHandler<T> errorOccurredHandler) {
        Values.notNull("executor", executor);

        EventSource eventSource = new EventSource("SourceBlock");
        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler, errorOccurredHandler);

        this.startLatch = new CountDownLatch(1);

        int threadCount = (null == policy) ? 1 : policy.maxThreads();

        AdaptiveConcurrencyGate gate = (policy instanceof AdaptiveConcurrencyPolicy adaptivePolicy)
                ? new AdaptiveConcurrencyGate(adaptivePolicy.minThreads(), adaptivePolicy.maxThreads(), adaptivePolicy.scaleUpThreshold())
                : null;

        this.lifecycles = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            WorkerLifecycle lifecycle = new WorkerLifecycle();
            this.lifecycles.add(lifecycle);

            Runnable worker;
            if (null == gate) {
                worker = new FixedWorker<>(
                        singleItemSupplier,
                        batchSupplier,
                        this.targetManager::postToTarget,
                        eventSource,
                        this.startLatch,
                        lifecycle
                );
            } else {
                worker = new AdaptiveWorker<>(
                        singleItemSupplier,
                        batchSupplier,
                        this.targetManager::postToTarget,
                        eventSource,
                        this.startLatch,
                        lifecycle,
                        gate
                );
            }

            executor.execute(worker);
        }
    }

    @Override
    public void linkTo(Target<T> target) {
        this.targetManager.add(target);
        this.startLatch.countDown();
    }

    boolean isRunning() {
        for (WorkerLifecycle lifecycle : this.lifecycles) {
            if (lifecycle.isRunning()) {
                return true;
            }
        }

        return false;
    }

    // Controls adaptive concurrency: gates worker threads behind a Semaphore whose permit count
    // is adjusted up or down based on whether the supplier is returning results.
    //
    // Scale-up: after scaleUpThreshold consecutive successful iterations (>=1 item forwarded)
    //           while below the ceiling, add one permit and reset the counter.  At-ceiling
    //           iterations release the semaphore immediately without touching the counter,
    //           preventing unbounded counter growth on a perpetually busy system.
    // Scale-down: on any miss (zero items forwarded), reset the counter and retire one permit
    //             unless already at the floor (minPermits), in which case release normally to
    //             prevent deadlock.
    //
    // The semaphore is always released AFTER dropping the lock so that any OS-level thread
    // wake-up (LockSupport.unpark) never occurs while the lock is held.  This keeps the
    // critical section to pure in-memory state mutation and minimises contention at high
    // throughput.
    private static final class AdaptiveConcurrencyGate {
        private final Object lock;
        private final Semaphore semaphore;
        private final int minPermits;
        private final int maxPermits;
        private final int scaleUpThreshold;

        private int currentMaxPermits;
        private int consecutiveSuccesses;

        private AdaptiveConcurrencyGate(int minPermits, int maxPermits, int scaleUpThreshold) {
            this.lock = new Object();
            this.semaphore = new Semaphore(minPermits);
            this.minPermits = minPermits;
            this.maxPermits = maxPermits;
            this.scaleUpThreshold = scaleUpThreshold;

            this.currentMaxPermits = minPermits;
            this.consecutiveSuccesses = 0;
        }

        void acquire() throws InterruptedException {
            this.semaphore.acquire();
        }

        void release(boolean hasResult) {
            int permitsToRelease;

            synchronized (this.lock) {
                if (hasResult) {
                    if (this.currentMaxPermits < this.maxPermits) {
                        this.consecutiveSuccesses++;

                        if (this.consecutiveSuccesses >= this.scaleUpThreshold) {
                            this.currentMaxPermits++;
                            this.consecutiveSuccesses = 0;
                            permitsToRelease = 2;  // release for this thread + unblock one waiting thread
                        } else {
                            permitsToRelease = 1;
                        }
                    } else {
                        permitsToRelease = 1;
                    }
                } else {
                    this.consecutiveSuccesses = 0;

                    if (this.currentMaxPermits > this.minPermits) {
                        this.currentMaxPermits--;
                        permitsToRelease = 0;  // Retires this thread's permit slot, reducing active concurrency.
                    } else {
                        permitsToRelease = 1;  // At the floor — must release to avoid deadlock.
                    }
                }
            }

            if (permitsToRelease > 0) {
                this.semaphore.release(permitsToRelease);
            }
        }
    }

    private static final class AdaptiveWorker<T> implements Runnable {
        private final Supplier<T> singleItemSupplier;
        private final Supplier<List<T>> batchSupplier;
        private final Consumer<T> consumer;
        private final EventSource eventSource;
        private final CountDownLatch startLatch;
        private final WorkerLifecycle lifecycle;
        private final AdaptiveConcurrencyGate gate;

        private AdaptiveWorker(Supplier<T> singleItemSupplier,
                               Supplier<List<T>> batchSupplier,
                               Consumer<T> consumer,
                               EventSource eventSource,
                               CountDownLatch startLatch,
                               WorkerLifecycle lifecycle,
                               AdaptiveConcurrencyGate gate) {
            this.singleItemSupplier = singleItemSupplier;
            this.batchSupplier = batchSupplier;
            this.consumer = consumer;
            this.eventSource = eventSource;
            this.startLatch = startLatch;
            this.lifecycle = lifecycle;
            this.gate = gate;
        }

        @Override
        @SuppressWarnings("java:S1141")
        public void run() {
            this.lifecycle.start();

            try {
                // Wait until at least one target is linked before polling the supplier.
                // Using await() once before the loop avoids per-iteration AQS overhead.
                this.startLatch.await();

                while (!Thread.currentThread().isInterrupted()) {
                    this.gate.acquire();

                    boolean hasResult = false;

                    try {
                        if (null != this.singleItemSupplier) {
                            T item = this.singleItemSupplier.get();

                            if (null != item) {
                                hasResult = true;
                                this.consumer.accept(item);
                            }
                        } else {
                            List<T> batch = this.batchSupplier.get();

                            if (null != batch && !batch.isEmpty()) {
                                hasResult = true;

                                for (T item : batch) {
                                    this.consumer.accept(item);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        this.eventSource.createErrorEvent(ex);
                    } finally {
                        this.gate.release(hasResult);
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            this.lifecycle.finish();
        }
    }

    private static final class FixedWorker<T> implements Runnable {
        private final Supplier<T> singleItemSupplier;
        private final Supplier<List<T>> batchSupplier;
        private final Consumer<T> consumer;
        private final EventSource eventSource;
        private final CountDownLatch startLatch;
        private final WorkerLifecycle lifecycle;

        private FixedWorker(Supplier<T> singleItemSupplier,
                            Supplier<List<T>> batchSupplier,
                            Consumer<T> consumer,
                            EventSource eventSource,
                            CountDownLatch startLatch,
                            WorkerLifecycle lifecycle) {
            this.singleItemSupplier = singleItemSupplier;
            this.batchSupplier = batchSupplier;
            this.consumer = consumer;
            this.eventSource = eventSource;
            this.startLatch = startLatch;
            this.lifecycle = lifecycle;
        }

        @Override
        @SuppressWarnings("java:S1141")
        public void run() {
            this.lifecycle.start();

            try {
                // Wait exactly once until at least one target is linked.
                //
                // Performing this before the loop eliminates the per-item AQS overhead
                // (Thread.interrupted() + volatile read).
                this.startLatch.await();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (null != this.singleItemSupplier) {
                            T item = this.singleItemSupplier.get();

                            if (null != item) {
                                this.consumer.accept(item);
                            }
                        } else {
                            List<T> batch = this.batchSupplier.get();

                            if (null != batch) {
                                for (T item : batch) {
                                    this.consumer.accept(item);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        this.eventSource.createErrorEvent(ex);
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            // WorkerLifecycle enforces the ordering invariant: isRunning is set to false
            // BEFORE the completion future resolves, so any thread spinning on isRunning()
            // is guaranteed to observe false once the worker has fully exited run().
            this.lifecycle.finish();
        }
    }
}
