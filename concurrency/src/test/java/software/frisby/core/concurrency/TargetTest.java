package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the default methods on the {@link Target} interface: {@link Target#size()},
 * {@link Target#inFlight()}, {@link Target#awaitCompletion()}, and
 * {@link Target#awaitCompletion(Duration)}.
 */
class TargetTest {
    // -------------------------------------------------------------------------
    // size() and inFlight() defaults
    // -------------------------------------------------------------------------

    // A Target whose completion() future never resolves — used to exercise blocking paths.
    private static Target<String> neverCompletingTarget() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        return new Target<>() {
            @Override
            public boolean post(String item) {
                return true;
            }

            @Override
            public CompletableFuture<Void> completion() {
                return future;
            }
        };
    }

    // A Target whose completion() future is already completed exceptionally — exercises the
    // defensive ExecutionException catch blocks that are unreachable through normal library usage.
    private static Target<String> failedTarget() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        future.completeExceptionally(new RuntimeException("simulated failure"));

        return new Target<>() {
            @Override
            public boolean post(String item) {
                return true;
            }

            @Override
            public CompletableFuture<Void> completion() {
                return future;
            }
        };
    }

    @Nested
    class Defaults {
        @Test
        void size_default_returnsZero() {
            Target<String> target = item -> true;

            assertEquals(0, target.size());
        }

        @Test
        void inFlight_default_delegatesToSize() {
            Target<String> target = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int size() {
                    return 5;
                }
            };

            assertEquals(5, target.inFlight());
        }

        @Test
        void inFlight_override_returnsOverriddenValue() {
            Target<String> target = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int size() {
                    return 3;
                }

                @Override
                public int inFlight() {
                    return 7;
                }
            };

            assertEquals(7, target.inFlight());
            assertEquals(3, target.size());
        }
    }

    // -------------------------------------------------------------------------
    // awaitCompletion()
    // -------------------------------------------------------------------------

    @Nested
    class AwaitCompletion {
        @Test
        void awaitCompletion_alreadyCompletedFuture_returnsImmediately() {
            // Lambda target — default completion() returns CompletableFuture.completedFuture(null).
            Target<String> target = item -> true;

            // Must return without blocking.
            target.awaitCompletion();
        }

        @Test
        void awaitCompletion_interruptedWhileWaiting_restoresInterruptFlag() throws Exception {
            Target<String> target = neverCompletingTarget();

            Thread waiter = new Thread(target::awaitCompletion);
            waiter.start();

            // Spin until the waiter is confirmed parked in Future.get().
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (waiter.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }

            assertEquals(Thread.State.WAITING, waiter.getState());

            waiter.interrupt();
            waiter.join(5_000);

            assertTrue(waiter.isInterrupted());
        }

        @Test
        void awaitCompletion_executionException_returnsNormally() {
            // Completion futures in this library never complete exceptionally, but the
            // defensive catch block must still be exercised.
            failedTarget().awaitCompletion();
        }
    }

    // -------------------------------------------------------------------------
    // awaitCompletion(Duration)
    // -------------------------------------------------------------------------

    @Nested
    class AwaitCompletionWithTimeout {
        @Test
        void awaitCompletion_withTimeout_alreadyCompleted_returnsTrue() {
            Target<String> target = item -> true;

            assertTrue(target.awaitCompletion(Duration.ofSeconds(5)));
        }

        @Test
        void awaitCompletion_withTimeout_timeoutExpires_returnsFalse() {
            Target<String> target = neverCompletingTarget();

            // Timeout is intentionally short so the test does not block for long.
            assertFalse(target.awaitCompletion(Duration.ofMillis(50)));
        }

        @Test
        void awaitCompletion_withTimeout_interruptedWhileWaiting_returnsFalse() throws Exception {
            Target<String> target = neverCompletingTarget();
            AtomicBoolean result = new AtomicBoolean(true);

            Thread waiter = new Thread(
                    () -> result.set(target.awaitCompletion(Duration.ofSeconds(10)))
            );
            waiter.start();

            // Spin until the waiter is confirmed parked in Future.get(timeout).
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (waiter.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }

            assertEquals(Thread.State.TIMED_WAITING, waiter.getState());

            waiter.interrupt();
            waiter.join(5_000);

            assertFalse(result.get());
            assertTrue(waiter.isInterrupted());
        }

        @Test
        void awaitCompletion_withTimeout_executionException_returnsTrue() {
            // Same defensive path as the no-timeout overload — must return true.
            assertTrue(failedTarget().awaitCompletion(Duration.ofSeconds(5)));
        }
    }
}
