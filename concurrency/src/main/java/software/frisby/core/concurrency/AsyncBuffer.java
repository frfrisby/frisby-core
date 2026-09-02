package software.frisby.core.concurrency;

import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
final class AsyncBuffer<T> {
    private final CompletableQueue<T> completableQueue;
    private final CapacityGate capacityGate;
    private final WorkerLifecycle lifecycle;

    AsyncBuffer(BlockingQueue<T> queue,
                int capacity,
                Consumer<T> consumer,
                ExecutorService executor,
                EventSource eventSource) {
        Sequences.notNull("queue", queue);
        Numbers.positive("capacity", capacity);
        Values.notNull("consumer", consumer);
        Values.notNull("executor", executor);
        Values.notNull("eventSource", eventSource);

        this.lifecycle = new WorkerLifecycle();
        this.capacityGate = new CapacityGate(capacity);
        this.completableQueue = new CompletableQueue<>(queue);

        executor.execute(new Worker<>(consumer, this.completableQueue, this.capacityGate, this.lifecycle, eventSource));
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

    int size() {
        return this.completableQueue.size();
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
        private final Consumer<T> consumer;
        private final CapacityGate capacityGate;
        private final WorkerLifecycle lifecycle;
        private final EventSource eventSource;

        private Worker(Consumer<T> consumer,
                       CompletableQueue<T> completableQueue,
                       CapacityGate capacityGate,
                       WorkerLifecycle lifecycle,
                       EventSource eventSource) {
            this.consumer = consumer;
            this.completableQueue = completableQueue;
            this.capacityGate = capacityGate;
            this.lifecycle = lifecycle;
            this.eventSource = eventSource;
        }

        @Override
        @SuppressWarnings("java:S1181")
        public void run() {
            this.lifecycle.start();

            try {
                T item;
                while (null != (item = this.completableQueue.dequeue())) {
                    try {
                        this.consumer.accept(item);
                    } catch (Throwable t) {
                        // Fatal JVM conditions propagate immediately, becoming a genuine uncaught
                        // exception on this thread.  Everything else is logged and the worker
                        // continues processing the next item rather than dying silently.
                        Errors.throwIfFatal(t);
                        this.eventSource.createErrorEvent(t);
                    } finally {
                        // Release the capacity permit after full delivery — successful or not —
                        // so that downstream back-pressure is correctly propagated back to
                        // posting threads and a failed item does not permanently occupy capacity.
                        this.capacityGate.release();
                    }
                }

                // Normal exit: dequeue() returns null when the queue is both completed and
                // empty (graceful drain) or when the worker thread is interrupted (external
                // NamedExecutorService.shutdown()).  In either case, all previously dequeued
                // items have already been fully delivered via consumer.accept(), so calling
                // finish() here correctly resolves completion() for callers awaiting a clean
                // drain.
                this.lifecycle.finish();
            } finally {
                // Safety net reached on every exit path, including a fatal error propagating
                // out of the loop above.  Guarantees isRunning() is never left reporting true
                // forever, without touching the completion future — see
                // WorkerLifecycle.stopRunning()'s Javadoc.  A harmless no-op re-affirmation on
                // the normal exit path, where finish() above already set isRunning to false.
                this.lifecycle.stopRunning();
            }
        }
    }
}
