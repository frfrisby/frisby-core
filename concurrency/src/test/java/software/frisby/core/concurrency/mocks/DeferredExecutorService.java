package software.frisby.core.concurrency.mocks;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test-only {@link ExecutorService} that defers actually running any submitted task until a
 * caller-controlled {@link CountDownLatch} gate is released, while still returning a real,
 * cancellable {@link java.util.concurrent.Future} from {@code submit()} immediately — exercising
 * the "task submitted but not yet running" window that a bare {@code Executor.execute()}-based
 * deferred wrapper cannot express.
 *
 * <p>Extending {@link AbstractExecutorService} rather than implementing {@link ExecutorService}
 * directly means only {@link #execute(Runnable)} and the lifecycle methods need overriding;
 * {@code submit()}, {@code invokeAll()}, and {@code invokeAny()} are all implemented in terms of
 * {@link #execute(Runnable)} by the parent class, so they automatically inherit the deferred
 * behavior.</p>
 *
 * <pre>{@code
 * NamedExecutorService realExecutor = NamedExecutorService.builder().threadPrefix("Test").build();
 * CountDownLatch gate = new CountDownLatch(1);
 * ExecutorService deferred = new DeferredExecutorService(realExecutor, gate);
 *
 * // Worker is submitted, and a real Future is returned, but the task body does not begin
 * // running until gate.countDown() is called.
 * Future<?> future = deferred.submit(worker);
 * }</pre>
 */
public final class DeferredExecutorService extends AbstractExecutorService {
    private final ExecutorService delegate;
    private final CountDownLatch gate;

    /**
     * Constructs a deferred executor that runs tasks on {@code delegate} only after
     * {@code gate} reaches zero.
     *
     * @param delegate The real executor that eventually runs each deferred task.
     * @param gate     The latch that must count down to zero before any submitted task begins
     *                 running.
     */
    public DeferredExecutorService(ExecutorService delegate, CountDownLatch gate) {
        this.delegate = delegate;
        this.gate = gate;
    }

    @Override
    public void execute(Runnable command) {
        this.delegate.execute(() -> {
            try {
                this.gate.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }

            command.run();
        });
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }
}

