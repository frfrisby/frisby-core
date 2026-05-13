package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullValueException;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SyncCompletionGuardTest {
    @Nested
    class Construction {
        @Test
        void nullDownstream_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> new SyncCompletionGuard(null)
            );
        }

        @Test
        void isCompleted_returnsFalse() {
            SyncCompletionGuard guard = new SyncCompletionGuard(() -> {
            });

            assertFalse(guard.isCompleted(), "A newly constructed guard must not be completed.");
        }
    }

    @Nested
    class FanIn {
        @Test
        void noOnLinked_singleCompleteSignalsDownstream() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.complete();

            assertTrue(guard.isCompleted(), "The guard should be completed.");
            assertEquals(1, count.get(), "Downstream should have been signaled exactly once.");
        }

        @Test
        void onLinkedOnce_singleCompleteSignalsDownstream() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.onLinked();
            guard.complete();

            assertTrue(guard.isCompleted(), "The guard should be completed.");
            assertEquals(1, count.get(), "Downstream should have been signaled exactly once.");
        }

        @Test
        void onLinkedTwice_firstCompleteDoesNotSignal() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.onLinked();
            guard.onLinked();

            guard.complete();

            assertFalse(guard.isCompleted(), "The guard should not be completed after only one of two required complete() calls.");
            assertEquals(0, count.get(), "Downstream should not have been signaled yet.");
        }

        @Test
        void onLinkedTwice_secondCompleteSignalsDownstream() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.onLinked();
            guard.onLinked();

            guard.complete();
            assertFalse(guard.isCompleted(), "The guard should not be completed after the first complete().");
            assertEquals(0, count.get(), "Downstream should not have been signaled after the first complete().");

            guard.complete();
            assertTrue(guard.isCompleted(), "The guard should be completed after the second complete().");
            assertEquals(1, count.get(), "Downstream should have been signaled exactly once.");
        }

        @Test
        void onLinkedThrice_signalsOnlyOnThirdComplete() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.onLinked();
            guard.onLinked();
            guard.onLinked();

            guard.complete();
            assertEquals(0, count.get(), "Downstream should not have been signaled after the first complete().");

            guard.complete();
            assertEquals(0, count.get(), "Downstream should not have been signaled after the second complete().");

            guard.complete();
            assertEquals(1, count.get(), "Downstream should have been signaled exactly once after all three complete() calls.");
        }
    }

    @Nested
    class InFlight {
        @Test
        void beginThenEnd_withoutComplete_doesNotSignal() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.begin();
            guard.end();

            assertFalse(guard.isCompleted(), "The guard should not be completed when complete() was never called.");
            assertEquals(0, count.get(), "Downstream should not have been signaled when the guard has not been completed.");
        }

        @Test
        void completeWhileInFlight_endSignalsDownstream() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            // Simulate an item being in-flight when complete() is called.
            guard.begin();

            guard.complete();

            assertTrue(guard.isCompleted(), "The guard should be completed.");
            assertEquals(0, count.get(), "Downstream should not fire while an item is still in-flight.");

            guard.end();

            assertEquals(1, count.get(), "Downstream should fire exactly once after the last in-flight item finishes.");
        }

        @Test
        void multipleItemsInFlight_lastEndSignalsDownstream() {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.begin();
            guard.begin();
            guard.begin();

            guard.complete();

            assertEquals(0, count.get(), "Downstream should not fire while items are still in-flight.");

            guard.end();
            assertEquals(0, count.get(), "Downstream should not fire after the first end().");

            guard.end();
            assertEquals(0, count.get(), "Downstream should not fire after the second end().");

            guard.end();
            assertEquals(1, count.get(), "Downstream should fire exactly once after the last end().");
        }

        @Test
        void isCompleted_returnsTrueAfterTransition() {
            SyncCompletionGuard guard = new SyncCompletionGuard(() -> {
            });

            assertFalse(guard.isCompleted(), "The guard should not be completed before complete() is called.");
            guard.complete();
            assertTrue(guard.isCompleted(), "The guard should be completed after complete() transitions it.");
        }
    }

    @Nested
    class Concurrent {
        // Repeated to increase the likelihood of hitting the concurrent race window on
        // each run.  The CyclicBarrier forces both threads to reach the race point before
        // either proceeds, making the window as wide as possible.

        @RepeatedTest(20)
        void twoEndsRace_downstreamFiredExactlyOnce() throws Exception {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            // Two items in-flight; complete the guard so that downstream fires as soon as
            // inFlight reaches zero.
            guard.begin();
            guard.begin();
            guard.complete();

            CyclicBarrier barrier = new CyclicBarrier(2);

            Thread t1 = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }

                guard.end();
            });

            Thread t2 = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }

                guard.end();
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertEquals(1, count.get(), "Downstream must be signaled exactly once even when two end() calls race.");
        }

        @RepeatedTest(20)
        void completeAndEndRace_downstreamFiredExactlyOnce() throws Exception {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            // One item in-flight: complete() sees inFlight=1 (no signal) if it fires before
            // end(), and end() sees completed=false (no signal) if it fires before complete().
            // Either way, only one of them observes both conditions as true — but the
            // downstreamSignaled guard must ensure no double-fire even in the race.
            guard.begin();

            CyclicBarrier barrier = new CyclicBarrier(2);

            Thread completeThread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }

                guard.complete();
            });

            Thread endThread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }

                guard.end();
            });

            completeThread.start();
            endThread.start();
            completeThread.join();
            endThread.join();

            assertEquals(1, count.get(), "Downstream must be signaled exactly once even when complete() and end() race.");
        }

        @RepeatedTest(20)
        void duplicateComplete_downstreamFiredOnlyOnce() throws Exception {
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            // No onLinked() calls — the first complete() should transition and signal.
            // A second complete() from another thread must be a no-op.
            CyclicBarrier barrier = new CyclicBarrier(2);

            Thread t1 = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }

                guard.complete();
            });

            Thread t2 = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }

                guard.complete();
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertEquals(1, count.get(), "Downstream must be signaled exactly once even when two complete() calls race.");
        }
    }

    @Nested
    class Idempotency {
        @Test
        void endAfterDownstreamAlreadySignaled_doesNotDoubleSignal() {
            // complete() with no in-flight items fires signalDownstream() immediately
            // (compareAndSet false→true succeeds).  A subsequent begin()/end() cycle
            // then attempts to fire signalDownstream() a second time; the
            // compareAndSet(false, true) must return false — covering the false branch
            // of the idempotency guard — and the downstream action must not run twice.
            AtomicInteger count = new AtomicInteger(0);
            SyncCompletionGuard guard = new SyncCompletionGuard(count::incrementAndGet);

            guard.complete();

            assertEquals(1, count.get(), "Downstream should have fired exactly once after complete().");

            // Simulate an out-of-order begin/end after completion (e.g. a late delivery
            // that arrives after the upstream has already signaled complete).
            guard.begin();
            guard.end();

            assertEquals(1, count.get(), "Downstream must not fire a second time — the CAS guard must block the duplicate signal.");
        }
    }
}

