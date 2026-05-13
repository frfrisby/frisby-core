package software.frisby.core.util;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopWatchTest {
    @Test
    void duration() throws Exception {
        long nanoStart = System.nanoTime();

        StopWatch actual = StopWatch.start();
        assertFalse(actual.isStopped(), "Incorrect isStopped() return value.");

        TimeUnit.MILLISECONDS.sleep(5);

        Duration firstDuration = actual.duration();
        long firstNanoTimeEnd = System.nanoTime();

        long expectedFirstDurationMax = (firstNanoTimeEnd - nanoStart);

        TimeUnit.MILLISECONDS.sleep(5);

        actual.stop();
        long nanoEnd = System.nanoTime();

        assertTrue(actual.isStopped(), "Incorrect isStopped() return value.");

        Duration endDuration = actual.duration();

        long expectedEndDurationMax = (nanoEnd - nanoStart);

        assertTrue(
                expectedFirstDurationMax >= firstDuration.toNanos(),
                String.format(
                        "Incorrect getElapsedDuration() return value. Context{expectedFirstDurationMax(ns)=%d, firstDuration(ns)=%d}",
                        expectedFirstDurationMax,
                        firstDuration.toNanos()
                )
        );

        assertTrue(
                expectedEndDurationMax >= endDuration.toNanos() &&
                        endDuration.equals(actual.duration()),
                String.format(
                        "Incorrect getElapsedDuration() return value. Context{expectedEndDurationMax(ns)=%d, endDuration(ns)=%d}",
                        expectedEndDurationMax,
                        endDuration.toNanos()
                )
        );
    }

    @Test
    void stop_calledTwice_durationFrozenAtFirstStop() throws Exception {
        var watch = StopWatch.start();

        TimeUnit.MILLISECONDS.sleep(5);
        watch.stop();

        var durationAfterFirstStop = watch.duration();

        TimeUnit.MILLISECONDS.sleep(5);
        watch.stop();

        assertEquals(
                durationAfterFirstStop,
                watch.duration(),
                "stop() called a second time must not change the recorded duration."
        );
    }
}
