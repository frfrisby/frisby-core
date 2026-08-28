package software.frisby.core.concurrency.fluent;

import java.util.concurrent.Executor;

/**
 * Internal helper that configures the shared pipeline executor on stage builders that
 * require asynchronous execution.
 *
 * <p>{@link Batch}, {@link Buffer}, {@link Delay}, {@link Group}, and {@link PriorityBuffer}
 * are the only fluent stage builders that run a background worker thread.  {@link Chain} and
 * {@link OpenChain} both call {@link #configureExecutorIfAsync} while walking their chain
 * during assembly, so that callers never need to set an executor on each async stage
 * individually.</p>
 *
 * @see Chain
 * @see OpenChain
 */
final class AsyncStages {
    private AsyncStages() {
    }

    /**
     * Injects {@code executor} into {@code target} if it is one of the five asynchronous
     * stage builders.  Every other stage type is left untouched.
     *
     * @param target   The stage builder to inspect and, if applicable, configure.
     * @param executor The shared executor configured on the pipeline builder; may be
     *                 {@code null} if the pipeline has no asynchronous stages.
     */
    static void configureExecutorIfAsync(PipelineTarget<?> target, Executor executor) {
        if (target instanceof Batch<?> batchStage) {
            batchStage.executor(executor);
        } else if (target instanceof Buffer<?> bufferStage) {
            bufferStage.executor(executor);
        } else if (target instanceof Delay<?> delayStage) {
            delayStage.executor(executor);
        } else if (target instanceof Group<?, ?> groupStage) {
            groupStage.executor(executor);
        } else if (target instanceof PriorityBuffer<?> priorityBufferStage) {
            priorityBufferStage.executor(executor);
        }
    }
}

