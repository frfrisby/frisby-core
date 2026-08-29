package software.frisby.core.concurrency.mocks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Installs a JVM-wide {@link Thread#setDefaultUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)}
 * for the duration of a test, capturing the first {@link Throwable} that escapes any thread which
 * has no thread-specific handler of its own — exactly what happens when a worker thread's
 * {@code run()} method exits via a genuinely uncaught (fatal) exception.
 *
 * <p>{@code NamedExecutorService}'s {@code ThreadFactory} does not install a per-thread handler,
 * so a fatal {@link Error} propagating out of a worker's {@code run()} method always reaches
 * whatever handler is registered via {@link Thread#setDefaultUncaughtExceptionHandler}.  This
 * makes it the correct, non-invasive hook for asserting "this exception genuinely escaped the
 * worker thread and was not swallowed" from a test.</p>
 *
 * <p>Always use in a try-with-resources block so the previous handler (if any) is restored after
 * the test, regardless of outcome:</p>
 * <pre>{@code
 * try (UncaughtExceptionCapture capture = new UncaughtExceptionCapture()) {
 *     // ... trigger the fatal condition on a worker thread ...
 *     assertTrue(capture.await(Duration.ofSeconds(5)));
 *     assertSame(expectedError, capture.thrown());
 * }
 * }</pre>
 */
public final class UncaughtExceptionCapture implements AutoCloseable {
    private final Thread.UncaughtExceptionHandler previous;
    private final AtomicReference<Throwable> captured;
    private final CountDownLatch latch;

    public UncaughtExceptionCapture() {
        this.captured = new AtomicReference<>();
        this.latch = new CountDownLatch(1);
        this.previous = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            this.captured.compareAndSet(null, throwable);
            this.latch.countDown();
        });
    }

    /**
     * Blocks until an uncaught exception is captured or the timeout elapses.
     *
     * @param timeoutSeconds The maximum number of seconds to wait.
     * @return {@code true} if an uncaught exception was captured within the timeout.
     */
    public boolean await(long timeoutSeconds) {
        try {
            return this.latch.await(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Returns the first captured uncaught {@link Throwable}, or {@code null} if none was captured.
     *
     * @return The captured throwable, or {@code null}.
     */
    public Throwable thrown() {
        return this.captured.get();
    }

    @Override
    public void close() {
        Thread.setDefaultUncaughtExceptionHandler(this.previous);
    }
}

