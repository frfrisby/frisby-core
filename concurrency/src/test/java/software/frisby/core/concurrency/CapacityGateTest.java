package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CapacityGate}.
 */
class CapacityGateTest {
    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void zeroCapacity_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class, () -> new CapacityGate(0));
        }
    }

    // -------------------------------------------------------------------------
    // available()
    // -------------------------------------------------------------------------

    @Nested
    class Available {
        @Test
        void available_freshInstance_returnsFullCapacity() {
            assertEquals(3, new CapacityGate(3).available());
        }

        @Test
        void available_afterAcquire_returnsCapacityMinusOne() throws InterruptedException {
            CapacityGate gate = new CapacityGate(3);

            gate.acquire();

            assertEquals(2, gate.available());
        }

        @Test
        void available_afterRelease_returnsRestoredCapacity() throws InterruptedException {
            CapacityGate gate = new CapacityGate(3);

            gate.acquire();
            gate.release();

            assertEquals(3, gate.available());
        }
    }

    // -------------------------------------------------------------------------
    // acquire()
    // -------------------------------------------------------------------------

    @Nested
    class Acquire {
        @Test
        void acquire_interruptedWhileBlocked_throwsInterruptedException() throws Exception {
            CapacityGate gate = new CapacityGate(1);

            gate.acquire(); // exhaust the single permit

            AtomicBoolean threw = new AtomicBoolean(false);
            Thread blocked = new Thread(() -> {
                try {
                    gate.acquire();
                } catch (InterruptedException ex) {
                    threw.set(true);
                }
            });
            blocked.start();

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (blocked.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }

            assertEquals(Thread.State.WAITING, blocked.getState());

            blocked.interrupt();
            blocked.join(5_000);

            assertTrue(threw.get());
        }
    }

    // -------------------------------------------------------------------------
    // tryAcquire()
    // -------------------------------------------------------------------------

    @Nested
    class TryAcquire {
        @Test
        void tryAcquire_permitAvailable_returnsTrue() throws InterruptedException {
            assertTrue(new CapacityGate(1).tryAcquire(0, TimeUnit.NANOSECONDS));
        }

        @Test
        void tryAcquire_noPermitAvailable_returnsFalse() throws InterruptedException {
            CapacityGate gate = new CapacityGate(1);

            gate.acquire(); // exhaust the single permit

            assertFalse(gate.tryAcquire(1, TimeUnit.MILLISECONDS));
        }

        @Test
        void tryAcquire_interruptedWhileWaiting_throwsInterruptedException() throws Exception {
            CapacityGate gate = new CapacityGate(1);

            gate.acquire(); // exhaust the single permit

            AtomicBoolean threw = new AtomicBoolean(false);
            Thread blocked = new Thread(() -> {
                try {
                    gate.tryAcquire(TimeUnit.SECONDS.toNanos(30));
                } catch (InterruptedException ex) {
                    threw.set(true);
                }
            });
            blocked.start();

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (blocked.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }

            assertEquals(Thread.State.TIMED_WAITING, blocked.getState());

            blocked.interrupt();
            blocked.join(5_000);

            assertTrue(threw.get());
        }
    }

    // -------------------------------------------------------------------------
    // release(int)
    // -------------------------------------------------------------------------

    @Nested
    class Release {
        @Test
        void release_multiplePermits_restoresCorrectCount() throws InterruptedException {
            CapacityGate gate = new CapacityGate(3);

            gate.acquire();
            gate.acquire();
            gate.acquire();

            assertEquals(0, gate.available());

            gate.release(3);

            assertEquals(3, gate.available());
        }
    }
}

