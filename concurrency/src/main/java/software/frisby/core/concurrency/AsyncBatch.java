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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
final class AsyncBatch<T> {
    private final CompletableQueue<T> completableQueue;
    private final CapacityGate capacityGate;
    private final WorkerLifecycle lifecycle;
    private final int batchSize;
    private final Duration timeout;

    AsyncBatch(BlockingQueue<T> queue,
               int capacity,
               Consumer<List<T>> consumer,
               int batchSize,
               Duration timeout,
               Executor executor,
               EventSource eventSource) {
        Sequences.notNull("queue", queue);
        Numbers.positive("capacity", capacity);
        Values.notNull("consumer", consumer);
        Numbers.positive("batchSize", batchSize);
        Durations.positive("timeout", timeout);
        Values.notNull("executor", executor);
        Values.notNull("eventSource", eventSource);

        this.lifecycle = new WorkerLifecycle();
        this.capacityGate = new CapacityGate(capacity);
        this.completableQueue = new CompletableQueue<>(queue);
        this.batchSize = batchSize;
        this.timeout = timeout;

        executor.execute(new Worker<>(consumer, this.completableQueue, batchSize, timeout.toMillis(), this.capacityGate, this.lifecycle, eventSource));
    }

    boolean post(T item) {
        if (this.completableQueue.isCompleted()) {
            return false;
        }

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
            }

            return enqueued;
        }

        return false;
    }

    boolean post(T item, Duration timeout) {
        if (this.completableQueue.isCompleted()) {
            return false;
        }

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
            }

            return enqueued;
        }

        return false;
    }

    int batchSize() {
        return this.batchSize;
    }

    Duration timeout() {
        return this.timeout;
    }

    int size() {
        return this.completableQueue.capacity() - this.capacityGate.available();
    }

    boolean isRunning() {
        return this.lifecycle.isRunning();
    }

    CompletableFuture<Void> complete() {
        this.completableQueue.complete();
        return this.lifecycle.completion();
    }

    CompletableFuture<Void> completion() {
        return this.lifecycle.completion();
    }

    private static final class Worker<T> implements Runnable {
        private final CompletableQueue<T> completableQueue;
        private final Consumer<List<T>> consumer;
        private final int batchSize;

        // Cached millis value of the batch timeout.  Duration.toMillis() involves several
        // arithmetic operations; caching the result avoids recomputing it on every iteration.
        private final long timeoutMs;
        private final CapacityGate capacityGate;
        private final WorkerLifecycle lifecycle;
        private final EventSource eventSource;

        private Worker(Consumer<List<T>> consumer,
                       CompletableQueue<T> completableQueue,
                       int batchSize,
                       long timeoutMs,
                       CapacityGate capacityGate,
                       WorkerLifecycle lifecycle,
                       EventSource eventSource) {
            this.consumer = consumer;
            this.completableQueue = completableQueue;
            this.batchSize = batchSize;
            this.timeoutMs = timeoutMs;
            this.capacityGate = capacityGate;
            this.lifecycle = lifecycle;
            this.eventSource = eventSource;
        }

        @Override
        @SuppressWarnings({"java:S135", "java:S3776"})
        public void run() {
            this.lifecycle.start();

            try {
                ArrayList<T> list = new ArrayList<>(this.batchSize);
                boolean beginPolling = false;

                while (true) {
                    T item;

                    if (beginPolling) {
                        // Poll mode: wait up to the batch timeout for the next item.  A null
                        // return means either the timeout elapsed or the queue is completed + empty.
                        item = this.completableQueue.dequeue(this.timeoutMs, TimeUnit.MILLISECONDS);

                        if (null == item) {
                            if (this.completableQueue.isCompleted()) {
                                // Queue is fully drained.  Flush any partial batch and exit.
                                if (!list.isEmpty()) {
                                    deliverBatch(list);
                                }

                                break;
                            }

                            // Batch timeout elapsed before the batch filled.  Flush any partial
                            // batch accumulated so far, then revert to take mode so the worker
                            // blocks cheaply until the next item arrives.
                            beginPolling = false;

                            if (!list.isEmpty()) {
                                List<T> batch = list;
                                list = new ArrayList<>(this.batchSize);

                                deliverBatch(batch);
                            }

                            continue;
                        }
                    } else {
                        // Take mode: block indefinitely until the first item of the next batch
                        // arrives, or until the queue is completed + empty (returns null).
                        item = this.completableQueue.dequeue();

                        if (null == item) {
                            // Queue is drained.  list is always empty in take mode, so there
                            // is nothing to flush.
                            break;
                        }

                        // First item of a new batch: switch to poll mode for subsequent items.
                        beginPolling = true;
                    }

                    list.add(item);

                    if (list.size() >= this.batchSize) {
                        List<T> batch = list;
                        list = new ArrayList<>(this.batchSize);

                        deliverBatch(batch);

                        // beginPolling remains true: continue filling the next batch in poll mode.
                    }
                }

                // Normal exit only — a fatal error thrown from deliverBatch() propagates past
                // this point, skipping finish() so completion() never resolves on fatal death.
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

        // Delivers a full batch to the downstream target and releases its capacity permits,
        // regardless of whether delivery succeeded.  Fatal JVM conditions propagate immediately;
        // everything else is logged and the worker continues with the next batch rather than
        // dying silently.
        private void deliverBatch(List<T> batch) {
            try {
                this.consumer.accept(batch);
            } catch (Throwable t) {
                Errors.throwIfFatal(t);
                this.eventSource.createErrorEvent(t);
            } finally {
                // Release capacity permits now that the batch has been fully delivered.
                // Items hold their permits from when they were posted until their batch
                // is consumed.
                this.capacityGate.release(batch.size());
            }
        }
    }
}
