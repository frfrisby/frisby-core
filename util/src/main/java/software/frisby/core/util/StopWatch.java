package software.frisby.core.util;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread-safe stopwatch that measures elapsed time with nanosecond precision.
 *
 * <p>To start timing, call the static {@link StopWatch#start()} factory method.
 * While running, {@link StopWatch#duration()} returns the time elapsed since
 * start. Once {@link StopWatch#stop()} is called the duration is frozen at the
 * moment of the first stop call; subsequent calls to {@link StopWatch#stop()}
 * have no effect.
 *
 * <p>{@code StopWatch} is safe for use across multiple threads. If two threads
 * call {@link #stop()} concurrently, exactly one will record the stop time and
 * the other will have no effect.
 */
public final class StopWatch {
    private final long startTime;
    private final AtomicLong stopTime;

    private StopWatch() {
        startTime = System.nanoTime();
        stopTime = new AtomicLong(-1);
    }

    /**
     * Creates a new {@link StopWatch} in a running state.
     *
     * @return A new {@link StopWatch} instance that is already running.
     */
    public static StopWatch start() {
        return new StopWatch();
    }

    /**
     * Returns the elapsed time since the stopwatch was started.
     *
     * <p>If the stopwatch is still running the duration increases on each call.
     * Once {@link #stop()} has been called the returned value is frozen at the
     * duration recorded at that moment.
     *
     * @return The elapsed {@link Duration} in nanosecond precision.
     */
    public Duration duration() {
        long stopped = stopTime.get();

        if (stopped < 0) {
            return Duration.ofNanos(System.nanoTime() - startTime);
        } else {
            return Duration.ofNanos(stopped - startTime);
        }
    }

    /**
     * Indicates whether the stopwatch has been stopped.
     *
     * @return {@code true} if {@link #stop()} has been called; {@code false} if the
     *         stopwatch is still running.
     */
    public boolean isStopped() {
        return stopTime.get() >= 0;
    }

    /**
     * Stops the stopwatch and freezes the value returned by {@link #duration()}.
     *
     * <p>If the stopwatch has already been stopped this method has no effect; the
     * duration recorded at the first call is preserved unchanged. This method is
     * safe to call concurrently from multiple threads.
     */
    public void stop() {
        stopTime.compareAndSet(-1, System.nanoTime());
    }
}
