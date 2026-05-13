package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class CompletableQueue<T> {
    private final BlockingQueue<T> queue;
    private final int capacity;

    private final AtomicBoolean completed;
    private final AtomicInteger pendingCompletes;

    private final CompletableFuture<Void> completionFuture;

    private final ReentrantLock lock;
    private final Condition notEmpty;

    CompletableQueue(BlockingQueue<T> queue) {
        Values.notNull("queue", queue);

        this.queue = queue;

        // Note that the provided queue is always created by internal classes and
        // will always be empty at this point, so the remaining capacity is the
        // total capacity of the queue.

        this.capacity = queue.remainingCapacity();

        this.completed = new AtomicBoolean(false);
        this.pendingCompletes = new AtomicInteger(0);

        this.completionFuture = new CompletableFuture<>();

        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
    }

    int capacity() {
        return this.capacity;
    }

    int size() {
        return this.queue.size();
    }

    boolean isEmpty() {
        return this.queue.isEmpty();
    }

    boolean isCompleted() {
        return this.completed.get();
    }

    void onLinked() {
        this.pendingCompletes.incrementAndGet();
    }

    boolean enqueue(T item) {
        if (this.completed.get()) {
            return false;
        }

        if (null != item) {
            try {
                this.queue.put(item);

                this.lock.lock();
                try {
                    this.notEmpty.signal();
                } finally {
                    this.lock.unlock();
                }

                return true;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return false;
    }

    boolean enqueue(T item, long timeout, TimeUnit unit) {
        if (this.completed.get()) {
            return false;
        }

        boolean result = false;
        if (null != item) {
            try {
                result = this.queue.offer(item, timeout, unit);

                if (result) {
                    this.lock.lock();
                    try {
                        this.notEmpty.signal();
                    } finally {
                        this.lock.unlock();
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return result;
    }

    /**
     * Retrieves and removes the head of the queue, waiting up to the
     * specified wait time if necessary for an element to become available.
     * If {@link #complete()} is called while this method is waiting, it will
     * wake up immediately and return {@code null}.
     *
     * @param timeout How long to wait before giving up, in units of
     *                {@code unit}.
     * @param unit    A {@code TimeUnit} determining how to interpret the
     *                {@code timeout} parameter.
     * @return The head of the queue, or {@code null} if the
     * specified waiting time elapses before an element is available.
     */
    T dequeue(long timeout, TimeUnit unit) {
        long nanosRemaining = unit.toNanos(timeout);

        // Only enter the condition wait when there is actually time to wait and the queue
        // is truly idle (empty and not yet completed).  Callers that pass a zero timeout
        // (e.g. the tail call inside the no-arg dequeue()) skip this block entirely and
        // fall straight through to the non-blocking poll below.
        if (nanosRemaining > 0 && !this.completed.get() && this.queue.isEmpty()) {
            this.lock.lock();
            try {
                while (!this.completed.get() && this.queue.isEmpty() && nanosRemaining > 0) {
                    nanosRemaining = this.notEmpty.awaitNanos(nanosRemaining);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                this.lock.unlock();
            }
        }

        T head = null;

        try {
            head = this.queue.poll(0L, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        if (this.completed.get() && this.queue.isEmpty()) {
            this.completionFuture.complete(null);
        }

        return head;
    }

    /**
     * Retrieves and removes the head of the queue, waiting if necessary
     * until an element becomes available or the {@link #isCompleted()} flag is
     * set to {@code true}.
     *
     * @return The head of this queue, or {@code null} if this queue is empty.
     */
    T dequeue() {
        if (!this.completed.get()) {
            try {
                this.lock.lockInterruptibly();
                try {
                    while (!this.completed.get() && this.queue.isEmpty()) {
                        this.notEmpty.await();
                    }
                } finally {
                    this.lock.unlock();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        return dequeue(0L, TimeUnit.NANOSECONDS);
    }

    void complete() {
        if (this.pendingCompletes.decrementAndGet() <= 0) {
            if (this.completed.compareAndSet(false, true)) {
                this.lock.lock();
                try {
                    if (this.queue.isEmpty()) {
                        this.completionFuture.complete(null);
                    }

                    this.notEmpty.signal();
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }

    CompletableFuture<Void> completion() {
        return this.completionFuture;
    }
}
