package software.frisby.core.concurrency;

import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
final class AsyncBuffer<T> {
    private final CompletableQueue<T> completableQueue;
    private final CapacityGate capacityGate;
    private final WorkerLifecycle lifecycle;

    AsyncBuffer(BlockingQueue<T> queue,
                int capacity,
                Consumer<T> consumer,
                Executor executor) {
        Sequences.notNull("queue", queue);
        Numbers.positive("capacity", capacity);
        Values.notNull("consumer", consumer);
        Values.notNull("executor", executor);

        this.lifecycle = new WorkerLifecycle();
        this.capacityGate = new CapacityGate(capacity);
        this.completableQueue = new CompletableQueue<>(queue);

        executor.execute(new Worker<>(consumer, this.completableQueue, this.capacityGate, this.lifecycle));
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

        private Worker(Consumer<T> consumer,
                       CompletableQueue<T> completableQueue,
                       CapacityGate capacityGate,
                       WorkerLifecycle lifecycle) {
            this.consumer = consumer;
            this.completableQueue = completableQueue;
            this.capacityGate = capacityGate;
            this.lifecycle = lifecycle;
        }

        @Override
        public void run() {
            this.lifecycle.start();

            T item;
            while (null != (item = this.completableQueue.dequeue())) {
                this.consumer.accept(item);

                // Release the capacity permit after full delivery so that downstream
                // back-pressure is correctly propagated back to posting threads.
                this.capacityGate.release();
            }

            // Ensure isRunning is false on all exit paths.  dequeue() returns null when the
            // queue is both completed and empty (graceful drain) or when the worker thread is
            // interrupted (external NamedExecutorService.shutdown()).  In either case, all
            // previously dequeued items have already been fully delivered via consumer.accept(),
            // so signaling finish() here correctly reflects that the worker is truly done.
            this.lifecycle.finish();
        }
    }
}
