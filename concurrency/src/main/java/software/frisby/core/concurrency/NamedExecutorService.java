package software.frisby.core.concurrency;

import java.util.concurrent.ExecutorService;

/**
 * A managed {@link ExecutorService} that allocates named threads for background worker tasks
 * and supports clean pipeline shutdown.
 *
 * <p>Unlike the standard {@link ExecutorService#shutdown()}, calling {@link #shutdown()} on a
 * {@code NamedExecutorService} <em>interrupts</em> all running threads so that worker loops
 * blocked in blocking operations (such as {@link java.util.concurrent.BlockingQueue#take()})
 * are unblocked immediately and exit cleanly.  For a pipeline that must drain before stopping,
 * call {@link Target#complete()} on each downstream block first, then call {@link #shutdown()}
 * to release any remaining blocked threads.</p>
 *
 * <pre>{@code
 * NamedExecutorService executor = NamedExecutorService.builder()
 *         .threadPrefix("DevicePipeline")
 *         .build();
 *
 * // ...wire and run the pipeline...
 *
 * sourceBlock.linkTo(actionBlock);
 * // drain cleanly:
 * actionBlock.complete();
 * actionBlock.awaitCompletion();
 * executor.shutdown();
 * }</pre>
 *
 * @see NamedExecutorServiceBuilder
 */
public interface NamedExecutorService extends ExecutorService {
    /**
     * Returns a new builder for constructing a {@link NamedExecutorService}.
     *
     * @return A new {@link NamedExecutorServiceBuilder} instance.
     */
    static NamedExecutorServiceBuilder builder() {
        return new DefaultNamedExecutorServiceBuilder();
    }

    /**
     * Returns the current number of threads in the executor's thread pool.
     *
     * @return The number of threads in the pool.
     */
    int poolSize();

    /**
     * Returns the number of threads currently executing a task.
     *
     * @return The number of threads actively executing tasks.
     */
    int activeCount();
}
