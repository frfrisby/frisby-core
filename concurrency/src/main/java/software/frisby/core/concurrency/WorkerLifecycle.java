package software.frisby.core.concurrency;

import java.util.concurrent.CompletableFuture;

/**
 * Encapsulates the thread-lifecycle protocol shared by every async worker in the module.
 *
 * <p>Every async worker records the same two events: the moment it starts running and
 * the moment it finishes.  Without a shared abstraction this knowledge is duplicated
 * across {@code AsyncBuffer}, {@code AsyncBatch}, {@code DefaultGroupBlock},
 * {@code DefaultDelayBlock}, and {@code DefaultSourceBlock}.  {@code WorkerLifecycle}
 * owns the three things every worker needs to express those events correctly:
 *
 * <ul>
 *   <li>{@code volatile boolean isRunning} — written by the worker thread, read externally
 *       for observability (e.g. {@code BufferBlock.isRunning()}).</li>
 *   <li>{@code CompletableFuture<Void> completionFuture} — resolves when the worker
 *       calls {@link #finish()}, enabling callers to await clean shutdown.</li>
 *   <li>The ordering invariant: {@code isRunning} is set to {@code false} <em>before</em>
 *       the future is completed, guaranteeing that any thread unblocked by
 *       {@code completion().get()} will always observe {@code isRunning() == false}.</li>
 * </ul>
 *
 * <h2>Usage pattern</h2>
 *
 * <pre>{@code
 * // Construction — one instance per worker, created by the enclosing block.
 * WorkerLifecycle lifecycle = new WorkerLifecycle();
 *
 * // For queue-based workers: wire the CompletableQueue → WorkerLifecycle bridge.
 * completableQueue.completion()
 *         .thenRun(lifecycle::finish);
 *
 * // Inside Worker.run():
 * public void run() {
 *     lifecycle.start();
 *     try {
 *         // ... process items until the queue is drained or interrupted ...
 *     } finally {
 *         lifecycle.finish();  // or omit if the bridge pattern is used
 *     }
 * }
 * }</pre>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>For queue-based workers the bridge pattern is preferred: the queue's own
 *       {@code completion()} future triggers {@code finish()} via {@code thenRun()},
 *       so the worker calls only {@link #start()} and the bridge handles the rest.</li>
 *   <li>For {@code DefaultSourceBlock.Worker} — which is producer-driven and exits
 *       solely on interrupt — {@link #finish()} is called directly at the end of
 *       {@code run()}.</li>
 *   <li>{@link #finish()} is idempotent: calling it more than once is safe.</li>
 * </ul>
 */
final class WorkerLifecycle {
    private final CompletableFuture<Void> completionFuture;

    // Volatile: written by the worker thread, read by external observers on other threads.
    // Without volatile the JVM may cache the value in a register and external readers
    // may never observe the update.
    private volatile boolean isRunning;

    WorkerLifecycle() {
        this.completionFuture = new CompletableFuture<>();
    }

    /**
     * Called at the very start of the worker's {@code run()} method, immediately after
     * {@code Thread.currentThread()} is available.  Records that the worker is now active.
     */
    void start() {
        this.isRunning = true;
    }

    /**
     * Called when the worker has finished all processing.  Sets {@code isRunning} to
     * {@code false} <em>before</em> completing the future, ensuring that any thread
     * waiting on {@code completion().get()} will observe {@code isRunning() == false}
     * when it unblocks.
     *
     * <p>This method is idempotent: calling it more than once is safe.
     */
    void finish() {
        this.isRunning = false;
        this.completionFuture.complete(null);
    }

    /**
     * Returns {@code true} if the worker thread is currently running, {@code false}
     * otherwise.
     *
     * @return {@code true} if running.
     */
    boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Returns the {@code CompletableFuture} that completes when {@link #finish()} is
     * called.  The future is guaranteed to complete only after {@code isRunning} has
     * already been set to {@code false}.
     *
     * @return The completion future.
     */
    CompletableFuture<Void> completion() {
        return this.completionFuture;
    }
}

