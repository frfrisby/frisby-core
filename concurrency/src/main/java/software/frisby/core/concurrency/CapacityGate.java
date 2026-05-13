package software.frisby.core.concurrency;

import software.frisby.core.validation.Numbers;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Gates entry into an async block's in-flight capacity.  A single permit is acquired
 * by the posting thread for each item accepted; permits are released by the worker
 * thread once the item has been delivered to the downstream target.
 *
 * <p>This enforces the configured capacity end-to-end — across both the inbound queue
 * and any items currently held by the worker (e.g. a batch accumulator or a
 * key-grouped {@code HashMap}) — rather than only at the queue boundary.  Without
 * this gate, a worker that drains items from the queue into an internal accumulator
 * frees queue slots that the posting thread can refill, allowing far more items into
 * the system than the configured capacity.
 *
 * <p>The underlying {@link Semaphore} is fair ({@code fair = true}), so posting
 * threads are unblocked in FIFO order as capacity becomes available.
 */
final class CapacityGate {
    private final Semaphore semaphore;

    CapacityGate(int capacity) {
        Numbers.positive("capacity", capacity);

        this.semaphore = new Semaphore(capacity, true);
    }

    /**
     * Acquires one permit, blocking until one is available or the calling thread
     * is interrupted.
     *
     * @throws InterruptedException if the calling thread is interrupted while waiting.
     */
    void acquire() throws InterruptedException {
        this.semaphore.acquire();
    }

    /**
     * Acquires one permit if one becomes available within the given timeout.
     *
     * @param nanos The maximum time to wait, in nanoseconds.
     * @return {@code true} if a permit was acquired before the timeout expired;
     * {@code false} if the timeout expired first.
     * @throws InterruptedException if the calling thread is interrupted while waiting.
     */
    boolean tryAcquire(long nanos) throws InterruptedException {
        return tryAcquire(nanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Acquires one permit if one becomes available within the given timeout.
     *
     * @param timeout The maximum time to wait for a permit.
     * @param unit    The time unit of the {@code timeout} argument.
     * @return {@code true} if a permit was acquired before the timeout expired;
     * {@code false} if the timeout expired first.
     * @throws InterruptedException if the calling thread is interrupted while waiting.
     */
    boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        return this.semaphore.tryAcquire(timeout, unit);
    }

    /**
     * Releases one permit.
     */
    void release() {
        this.semaphore.release();
    }

    /**
     * Releases the specified number of permits.
     *
     * @param permits The number of permits to release.
     */
    void release(int permits) {
        this.semaphore.release(permits);
    }

    /**
     * Returns the number of permits currently available (i.e. unused capacity).
     * The in-flight item count can be derived as {@code capacity - available()}.
     *
     * @return The number of available permits.
     */
    int available() {
        return this.semaphore.availablePermits();
    }
}

