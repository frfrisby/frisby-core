package software.frisby.core.concurrency.fluent;

import java.util.concurrent.Executor;

/**
 * Marker interface for asynchronous pipeline stages that require an {@link Executor} to run
 * their internal worker thread.  Implemented alongside {@link PipelineStage} by all async
 * stage helpers ({@link Batch}, {@link Buffer}, {@link Delay}, {@link Group},
 * {@link PriorityBuffer}).
 *
 * <p>{@link Chain} and {@link OpenChain} use this interface during pipeline assembly to inject
 * a shared executor into every async stage in the chain, avoiding the need for callers to
 * configure an executor on each stage individually.</p>
 */
interface ExecutorAwareStage {
    /**
     * Sets the executor that will run this stage's internal worker thread.
     *
     * @param executor The executor to use.
     */
    void executor(Executor executor);
}


