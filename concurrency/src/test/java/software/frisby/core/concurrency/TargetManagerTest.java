package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;
import software.frisby.core.validation.NullValueException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TargetManager}.  Exercises {@code inFlight()}, {@code complete()},
 * {@code completion()}, the {@code add()} guard paths, and the {@code awaitTargets()} paths.
 */
class TargetManagerTest {
    private static final Object SOURCE = new Object();

    private static TargetManager<String> newManager() {
        return new TargetManager<>(SOURCE, new EventSource("test"), null, null);
    }

    // -------------------------------------------------------------------------
    // inFlight()
    // -------------------------------------------------------------------------

    @Nested
    class InFlight {
        @Test
        void inFlight_noTargetLinked_returnsZero() {
            assertEquals(0, newManager().inFlight());
        }

        @Test
        void inFlight_targetLinked_delegatesToTarget() {
            TargetManager<String> manager = newManager();

            manager.add(new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 42;
                }
            });

            assertEquals(42, manager.inFlight());
        }
    }

    // -------------------------------------------------------------------------
    // complete()
    // -------------------------------------------------------------------------

    @Nested
    class Complete {
        @Test
        void complete_noTargetLinked_doesNotThrow() {
            assertDoesNotThrow(() -> newManager().complete());
        }

        @Test
        void complete_targetLinked_callsTargetComplete() {
            TargetManager<String> manager = newManager();
            AtomicBoolean completed = new AtomicBoolean(false);

            manager.add(new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    completed.set(true);
                }
            });

            manager.complete();

            assertTrue(completed.get());
        }
    }

    // -------------------------------------------------------------------------
    // completion()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void completion_noTargetLinked_returnsCompletedFuture() {
            assertTrue(newManager().completion().isDone());
        }

        @Test
        void completion_targetLinked_delegatesToTarget() {
            TargetManager<String> manager = newManager();
            CompletableFuture<Void> future = new CompletableFuture<>();

            manager.add(new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public CompletableFuture<Void> completion() {
                    return future;
                }
            });

            assertSame(future, manager.completion());
        }
    }

    // -------------------------------------------------------------------------
    // add()
    // -------------------------------------------------------------------------

    @Nested
    class Add {
        @Test
        void add_nullTarget_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> newManager().add(null));
        }

        @Test
        void add_selfLink_throwsIllegalArgumentException() {
            // SOURCE is not a Target, but the self-link guard uses object identity.
            // We need a manager where the source object is also a Target.
            Target<String> selfTarget = item -> true;

            TargetManager<String> selfManager = new TargetManager<>(selfTarget, new EventSource("test"), null, null);

            assertThrows(IllegalArgumentException.class, () -> selfManager.add(selfTarget));
        }

        @Test
        void add_secondTarget_throwsIllegalStateException() {
            TargetManager<String> manager = newManager();

            manager.add(item -> true);

            assertThrows(IllegalStateException.class, () -> manager.add(item -> true));
        }
    }

    // -------------------------------------------------------------------------
    // awaitTargets() — interrupt path
    // -------------------------------------------------------------------------

    @Nested
    class AwaitTargets {
        @Test
        void awaitTargets_interruptedBeforeTargetAdded_setsInterruptFlag() throws Exception {
            TargetManager<String> manager = newManager();

            // No target has been added — the latch count is 1; awaitTargets() will block.
            Thread waiter = new Thread(manager::awaitTargets);
            waiter.start();

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
        void awaitTargets_noTargetLinked_logsWarning() throws Exception {
            TargetManager<String> manager = newManager();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.WARNING)
                            .predicate(e -> e.message().contains("test")
                                    && e.message().contains("no downstream target linked"))
                            .build()
                    )
                    .build()) {
                Thread waiter = new Thread(manager::awaitTargets);
                waiter.start();

                verifier.assertExpectations(Duration.ofSeconds(5));

                manager.add(item -> true);
                waiter.join(5_000);
            }
        }
    }

    // -------------------------------------------------------------------------
    // awaitTargets() with CountDownLatch synchronization
    // -------------------------------------------------------------------------

    @Nested
    class AwaitTargetsBlocking {
        @Test
        void awaitTargets_blocksUntilTargetIsAdded() throws Exception {
            TargetManager<String> manager = newManager();
            CountDownLatch awaitingStarted = new CountDownLatch(1);
            AtomicBoolean awaitReturned = new AtomicBoolean(false);

            Thread waiter = new Thread(() -> {
                awaitingStarted.countDown();
                manager.awaitTargets();
                awaitReturned.set(true);
            });
            waiter.start();

            assertTrue(awaitingStarted.await(5, TimeUnit.SECONDS));

            // Before adding a target, awaitTargets() has not returned.
            assertFalse(awaitReturned.get());

            // Adding the target counts down the latch — awaitTargets() must return.
            manager.add(item -> true);
            waiter.join(5_000);

            assertTrue(awaitReturned.get());
        }
    }
}
