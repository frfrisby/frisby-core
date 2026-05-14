package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.mocks.MockInterruptedQueue;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DelayBlock} and its builder.  Covers builder validation, capacity, size,
 * both {@code post} overloads, delay semantics (items not released early, items released after
 * delay expires), {@code linkTo}, the completion lifecycle, and all
 * three delegate handler callbacks.
 */
class DelayBlockTest {
    private static final String PREFIX = "TestDelay";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'DelayBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";
    private static final String LINK_TO_SELF_MSG =
            "The 'target' value is invalid.  A block cannot be linked to itself.";

    // A short delay used wherever near-instant delivery is needed.
    private static final Duration INSTANT = Duration.ofMillis(1);

    // A no-op target that accepts every item.
    private static final Target<String> ACCEPT = item -> true;

    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix(PREFIX)
                .build();
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullExecutor_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> DelayBlock.<String>builder()
                            .delay(INSTANT)
                            .build()
            );
        }

        @Test
        void nullDelay_throwsNullValueException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NullValueException.class,
                        () -> DelayBlock.<String>builder()
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void capacityBelowMin_throwsNumericValueOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NumericValueOutsideRangeException.class,
                        () -> DelayBlock.<String>builder()
                                .capacity(0)
                                .delay(INSTANT)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void defaultCapacity_is1024() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                assertEquals(1024, block.capacity());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void perItemDelayFunction_appliedPerItem() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            try {
                // Use the Function<T, Duration> overload so that each item can carry its own delay.
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .delay(item -> item.startsWith("fast") ? Duration.ofMillis(1) : Duration.ofMillis(500))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    received.set(item);
                    delivered.countDown();
                    return true;
                });

                assertTrue(block.post("fast-item"));
                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals("fast-item", received.get());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // capacity()
    // -------------------------------------------------------------------------

    @Nested
    class Capacity {
        @Test
        void capacity_returnsConfiguredValue() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(64)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                assertEquals(64, block.capacity());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // size()
    // -------------------------------------------------------------------------

    @Nested
    class Size {
        @Test
        void size_returnsZeroInitially() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                assertEquals(0, block.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void size_reflectsItemsBufferedWhileTargetIsBlocked() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    targetStarted.countDown();

                    try {
                        targetRelease.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));
                assertEquals(2, block.size());
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // post(T)  /  post(T, Duration)
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Nested
        class WithoutTimeout {
            @Test
            void nullItem_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void validItem_returnsTrueAndDeliversToTargetAfterDelay() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(1);
                AtomicReference<String> received = new AtomicReference<>();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(Duration.ofMillis(100))
                            .executor(executor)
                            .build();

                    block.linkTo(item -> {
                        received.set(item);
                        delivered.countDown();
                        return true;
                    });

                    assertTrue(block.post("hello"));
                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertEquals("hello", received.get());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void itemNotDeliveredBeforeDelayExpires() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(1);

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(Duration.ofMillis(500))
                            .executor(executor)
                            .build();

                    block.linkTo(item -> {
                        delivered.countDown();
                        return true;
                    });

                    block.post("slow");

                    // Not delivered within 100 ms — delay is 500 ms.
                    assertFalse(delivered.await(100, TimeUnit.MILLISECONDS));

                    // Delivered within 5 seconds once the delay expires.
                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.complete();

                    assertFalse(block.post("hello"));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void whenBufferFull_blocksPostingThreadUntilSlotOpens() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);
                CountDownLatch cDelivered = new CountDownLatch(1);
                AtomicBoolean cPostSucceeded = new AtomicBoolean(false);

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(2)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(item -> {
                        if ("a".equals(item)) {
                            targetStarted.countDown();

                            try {
                                targetRelease.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        if ("c".equals(item)) {
                            cDelivered.countDown();
                        }

                        return true;
                    });

                    // "a" is taken by the worker and held in the blocking target (permit 1 consumed).
                    block.post("a");
                    assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                    // "b" fills the second capacity slot (permit 2 consumed); the buffer is now at capacity.
                    block.post("b");

                    // Start a thread to post "c" — it must block because both permits are taken.
                    Thread poster = new Thread(() -> cPostSucceeded.set(block.post("c")));
                    poster.start();

                    // Spin until the posting thread is confirmed blocked in Semaphore.acquire().
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                    while (poster.getState() != Thread.State.WAITING &&
                            System.nanoTime() < deadline) {
                        Thread.onSpinWait();
                    }

                    assertEquals(Thread.State.WAITING, poster.getState());

                    // Release "a" — permit is returned, and "c" is accepted.
                    targetRelease.countDown();

                    assertTrue(cDelivered.await(5, TimeUnit.SECONDS));

                    // Join the poster thread before reading cPostSucceeded — the worker can deliver
                    // "c" (counting down cDelivered) before the poster thread has executed
                    // cPostSucceeded.set(true), so reading the boolean before join() produces a race.
                    poster.join(5_000);

                    assertTrue(cPostSucceeded.get());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void postWhenCallingThreadInterrupted() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch invoked = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    MockInterruptedQueue<DelayedEntry<String>> mockQueue = new MockInterruptedQueue<>();

                    DefaultDelayBlock<String> block = new DefaultDelayBlock<>(
                            mockQueue,
                            10,
                            item -> Duration.ofMillis(100),
                            executor,
                            null,
                            null,
                            null
                    );

                    block.linkTo(item -> true);

                    Thread poster = new Thread(() -> {
                        postResult.set(block.post("hello"));
                        invoked.countDown();
                    });
                    poster.start();

                    assertTrue(invoked.await(5, TimeUnit.SECONDS));

                    poster.join(5000);
                    assertTrue(poster.isInterrupted());

                    assertFalse(postResult.get());
                    assertTrue(mockQueue.awaitPut(5, TimeUnit.SECONDS));
                    assertEquals(1, mockQueue.putInvokes());
                } finally {
                    executor.shutdown();
                }
            }
        }

        @Nested
        class WithTimeout {
            @Test
            void nullTimeout_throwsNullValueException() {
                NamedExecutorService executor = newExecutor();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertThrows(
                            NullValueException.class,
                            () -> block.post("hello", null)
                    );
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void nonPositiveTimeout_throwsDurationOutsideRangeException() {
                NamedExecutorService executor = newExecutor();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertThrows(
                            DurationOutsideRangeException.class,
                            () -> block.post("hello", Duration.ZERO)
                    );
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void validItem_returnsTrueAndDeliversToTarget() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(1);
                AtomicReference<String> received = new AtomicReference<>();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(Duration.ofMillis(100))
                            .executor(executor)
                            .build();

                    block.linkTo(item -> {
                        received.set(item);
                        delivered.countDown();
                        return true;
                    });

                    assertTrue(block.post("hello", Duration.ofSeconds(5)));
                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertEquals("hello", received.get());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.complete();

                    assertFalse(block.post("hello", Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void whenBufferFull_returnsFalseAfterTimeout() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(2)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(item -> {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        return true;
                    });

                    // Item "a" is taken from the queue by the worker and handed to the blocking target
                    // (permit 1 consumed; not released until target returns).
                    block.post("a");
                    assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                    // Item "b" fills the second capacity slot (permit 2 consumed).
                    assertTrue(block.post("b", Duration.ofSeconds(5)));

                    // Item "c" finds all permits taken — must time out.
                    assertFalse(block.post("c", Duration.ofMillis(100)));
                } finally {
                    targetRelease.countDown();
                    executor.shutdown();
                }
            }

            @Test
            void multipleItems_capacityReached() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);
                CountDownLatch posterDone = new CountDownLatch(1);
                AtomicBoolean thirdPostBlocked = new AtomicBoolean(false);

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(2)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(item -> {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        return true;
                    });

                    // Post two items to fill the capacity; the target blocks on the first.
                    Thread poster = new Thread(() -> {
                        block.post("hello-1");
                        block.post("hello-2");

                        // The third post must time out — both permits are held.
                        thirdPostBlocked.set(!block.post("hello-3", Duration.ofSeconds(1)));

                        posterDone.countDown();
                    });
                    poster.start();

                    assertTrue(posterDone.await(5, TimeUnit.SECONDS));
                    poster.join(5_000);
                    assertFalse(poster.isInterrupted());

                    // Unblock the target so the worker can finish cleanly.
                    targetRelease.countDown();

                    assertTrue(thirdPostBlocked.get());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void nullItem_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    DelayBlock<String> block = DelayBlock.<String>builder()
                            .capacity(10)
                            .delay(INSTANT)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null, Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void postWhenCallingThreadInterrupted() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch invoked = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    MockInterruptedQueue<DelayedEntry<String>> mockQueue = new MockInterruptedQueue<>();

                    DefaultDelayBlock<String> block = new DefaultDelayBlock<>(
                            mockQueue,
                            10,
                            item -> Duration.ofMillis(100),
                            executor,
                            null,
                            null,
                            null
                    );

                    block.linkTo(item -> true);

                    Thread poster = new Thread(() -> {
                        postResult.set(block.post("hello", Duration.ofSeconds(5)));
                        invoked.countDown();
                    });
                    poster.start();

                    assertTrue(invoked.await(5, TimeUnit.SECONDS));

                    poster.join(5000);
                    assertTrue(poster.isInterrupted());

                    assertFalse(postResult.get());
                    assertTrue(mockQueue.awaitPut(5, TimeUnit.SECONDS));
                    assertEquals(1, mockQueue.putInvokes());
                } finally {
                    executor.shutdown();
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // linkTo
    // -------------------------------------------------------------------------

    @Nested
    class LinkTo {
        @Test
        void withoutCriteria_deliversAllItemsToTarget() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch delivered = new CountDownLatch(3);
            AtomicInteger count = new AtomicInteger(0);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    count.incrementAndGet();
                    delivered.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(3, count.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void withCriteria_deliversOnlyMatchingItemsToTarget() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger matchCount = new AtomicInteger(0);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                BranchBlock<String> branch = BranchBlock.<String>builder()
                        .when(
                                item -> item.startsWith("y"),
                                item -> {
                                    matchCount.incrementAndGet();
                                    return true;
                                }
                        )
                        .otherwise(item -> true)
                        .build();

                block.linkTo(branch);

                block.post("yes");
                block.post("no");
                block.post("yep");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(2, matchCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void linkToCalledTwice_throwsIllegalStateException() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                block.linkTo(item -> true);

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> block.linkTo(item -> true)
                );

                assertEquals(LINK_TO_CALLED_TWICE_MSG, ex.getMessage());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void linkToSelf_throwsIllegalArgumentException() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                IllegalArgumentException ex = assertThrows(
                        IllegalArgumentException.class,
                        () -> block.linkTo(block)
                );

                assertEquals(LINK_TO_SELF_MSG, ex.getMessage());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // complete() / completion() / isRunning()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void isRunning_returnsTrueInitially() throws Exception {
            NamedExecutorService executor = newExecutor();

            try {
                DefaultDelayBlock<String> block =
                        (DefaultDelayBlock<String>) DelayBlock.<String>builder()
                                .capacity(10)
                                .delay(INSTANT)
                                .executor(executor)
                                .build();

                // Post one item and wait for delivery — once the item has been delivered the worker
                // thread is guaranteed to be running, making the isRunning() assertion deterministic.
                CountDownLatch delivered = new CountDownLatch(1);

                block.linkTo(item -> {
                    delivered.countDown();
                    return true;
                });

                block.post("probe");
                assertTrue(delivered.await(5, TimeUnit.SECONDS));

                assertTrue(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void isRunning_returnsFalseAfterDrain() throws Exception {
            NamedExecutorService executor = newExecutor();

            try {
                DefaultDelayBlock<String> block =
                        (DefaultDelayBlock<String>) DelayBlock.<String>builder()
                                .capacity(10)
                                .delay(INSTANT)
                                .executor(executor)
                                .build();

                block.linkTo(ACCEPT);

                // Spin until the worker has set workerThread — guaranteed because isRunning = true
                // is set immediately after workerThread = Thread.currentThread() in run().
                // This ensures drain() observes a non-null workerThread and takes the interrupt
                // path (queue empty && t != null), exercising that branch deterministically.
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                while (!block.isRunning() && System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }

                assertTrue(block.isRunning(), "worker did not start within timeout");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertFalse(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void complete_immediatelyFlushesRemainingItems() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        // Use a very long delay — items must be flushed immediately on
                        // complete() rather than waiting for their full delay to expire.
                        .delay(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                // complete() must flush the queue immediately — all three items must be
                // delivered well before the 30-second delay expires.
                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(3, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void complete_isIdempotent() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    return true;
                });

                block.post("a");

                block.complete();
                block.complete();

                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(1, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void drainCalledBeforeWorkerThreadStarts_workerExitsCleanly() throws Exception {
            // A deferred executor holds the worker Runnable until after complete() is called.
            // This guarantees that drain() runs while workerThread is still null.
            //
            // drain() sets draining=true but cannot send an interrupt (workerThread is null).
            // When the worker eventually starts it checks draining at the top of its loop,
            // finds the queue empty, and breaks out cleanly without ever calling take().
            NamedExecutorService executor = newExecutor();
            CountDownLatch workerStart = new CountDownLatch(1);

            try {
                Executor deferredExecutor = task -> executor.execute(() -> {
                    try {
                        workerStart.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    task.run();
                });

                DefaultDelayBlock<String> block = new DefaultDelayBlock<>(
                        new DelayQueue<>(),
                        10,
                        item -> INSTANT,
                        deferredExecutor,
                        null,
                        null,
                        null
                );

                block.linkTo(ACCEPT);

                // complete() runs drain() while workerThread is null — null == t branch taken,
                // no interrupt sent.  The worker will detect draining at the top of its loop.
                block.complete();

                // Release the worker.  It immediately sees draining && queue.isEmpty(),
                // breaks out of the loop, and resolves the completion future.
                workerStart.countDown();

                block.completion().get(5, TimeUnit.SECONDS);

                assertFalse(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void drainCalledDuringDeliveryWithQueueNotEmpty_coversNotEmptyFalseBranchThenFlushes() throws Exception {
            // Covers the FALSE branch of both draining guards:
            //   - Line 201 (pre-take check):  draining=true && queue.isEmpty()=false → does NOT break
            //   - Line 212 (post-delivery check): draining=true && queue.isEmpty()=false → exit NOT set
            //
            // Items "a", "b", and "c" are all posted.  The target blocks the worker inside
            // target.post() for "a" while "b" and "c" remain in the queue.  complete() is
            // called at that point — draining=true, worker is interrupted.  The target
            // restores the interrupt flag and returns.  The worker then:
            //   1. capacityGate.release()
            //   2. line 212: draining=true && queue NOT empty → false — exit NOT set
            //   3. loops back; line 201: draining=true && queue NOT empty → false — no break
            //   4. take() → interrupt flag already set → InterruptedException immediately
            //   5. catch: draining=true → flush "b" and "c" immediately
            // All three deliveries complete before completion() resolves.
            NamedExecutorService executor = newExecutor();
            CountDownLatch firstDeliveryStarted = new CountDownLatch(1);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                DefaultDelayBlock<String> block = new DefaultDelayBlock<>(
                        new DelayQueue<>(),
                        10,
                        item -> INSTANT,
                        executor,
                        null,
                        null,
                        null
                );

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();

                    if ("a".equals(item)) {
                        firstDeliveryStarted.countDown();

                        try {
                            // Park the worker so that "b" and "c" remain in the queue
                            // when complete() is called.  The interrupt sent by drain()
                            // wakes this sleep; the catch block restores the flag so
                            // that take() will throw on the next loop iteration.
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                // Worker is blocked inside target.post() for "a"; "b" and "c" sit in
                // the delay queue.  complete() sets draining=true and interrupts the
                // worker, which loops back and hits the not-empty false branch before
                // the catch block flushes "b" and "c".
                assertTrue(firstDeliveryStarted.await(5, TimeUnit.SECONDS));

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(3, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void naturalExpiryWithDrainDuringDelivery_exitsViaPostDequeuePath() throws Exception {
            // Covers DefaultDelayBlock.Worker.run() line 213: exit = true.
            //
            // The path is: take() returns naturally (delay has expired) → postToTarget() is
            // called → while postToTarget() is in progress complete() is called from the test
            // thread, setting draining = true and interrupting the worker thread →
            // deliveryMayFinish.await() throws, the worker restores its interrupt flag and
            // returns from target.post() → capacityGate.release() → line 212 check:
            // (draining = true) && (queue.isEmpty() = true) → line 213: exit = true.
            CountDownLatch deliveryStarted = new CountDownLatch(1);
            NamedExecutorService executor = newExecutor();

            try {
                DefaultDelayBlock<String> block = new DefaultDelayBlock<>(
                        new DelayQueue<>(),
                        10,
                        item -> INSTANT,
                        executor,
                        null,
                        null,
                        null
                );

                block.linkTo(item -> {
                    deliveryStarted.countDown();

                    try {
                        // Park here so the test thread can call complete() while the item has
                        // already been dequeued from the delay queue (queue is now empty) but
                        // the worker has not yet reached the in-loop drain check at line 212.
                        // The interrupt sent by complete() wakes this await().
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        // Restore the interrupt flag.  The worker will observe it after
                        // postToTarget() returns and exit cleanly via line 213.
                        Thread.currentThread().interrupt();
                    }

                    return true;
                });

                block.post("only-item");

                // Item has been dequeued (delay expired) and the worker is blocking inside
                // target.post().  Queue is empty.  Call complete() now — this sets
                // draining = true and interrupts the worker, waking it from Thread.sleep().
                assertTrue(deliveryStarted.await(5, TimeUnit.SECONDS));

                block.complete();

                block.completion().get(5, TimeUnit.SECONDS);

                assertFalse(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Delegate handlers
    // -------------------------------------------------------------------------

    @Nested
    class Delegates {
        @Test
        void itemPostedHandler_isCalledOnPost() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<String> postedItem = new AtomicReference<>();
            AtomicBoolean accepted = new AtomicBoolean(false);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .itemPostedHandler((source, item, wasAccepted) -> {
                            postedItem.set(item);
                            accepted.set(wasAccepted);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(ACCEPT);
                block.post("hello");

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertEquals("hello", postedItem.get());
                assertTrue(accepted.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void itemDeliveredHandler_isCalledOnDelivery() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<String> deliveredItem = new AtomicReference<>();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .itemDeliveredHandler((source, target, item) -> {
                            deliveredItem.set(item);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(ACCEPT);
                block.post("hello");

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertEquals("hello", deliveredItem.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void errorOccurredHandler_isCalledWhenTargetThrows() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<Throwable> capturedError = new AtomicReference<>();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .capacity(10)
                        .delay(INSTANT)
                        .executor(executor)
                        .errorOccurredHandler((source, target, item, error) -> {
                            capturedError.set(error);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(item -> {
                    throw new RuntimeException("target error");
                });

                block.post("hello");

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertNotNull(capturedError.get());
                assertEquals("target error", capturedError.get().getMessage());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Fan-in
    // -------------------------------------------------------------------------

    @Nested
    class FanIn {
        @Test
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger received = new AtomicInteger(0);

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .delay(INSTANT)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    received.incrementAndGet();
                    return true;
                });

                block.onLinked();
                block.onLinked();

                assertTrue(block.post("a"));

                block.complete();

                assertTrue(block.post("b"));
                assertFalse(block.completion().isDone());

                block.complete();

                block.completion().get(5, TimeUnit.SECONDS);
                assertFalse(block.post("c"));
                assertEquals(2, received.get());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // inFlight()
    // -------------------------------------------------------------------------

    @Nested
    class InFlight {
        @Test
        void inFlight_noItems_returnsZero() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .delay(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                assertEquals(0, block.inFlight());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void inFlight_itemsInDelayQueue_equalsSizeWhenDownstreamInFlightIsZero() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .delay(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                block.linkTo(item -> true);

                block.post("a");
                block.post("b");
                block.post("c");

                // Items are held in the delay queue because the delay has not expired.
                assertEquals(block.size(), block.inFlight());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void inFlight_withDownstreamInFlight_addsBothCounts() {
            NamedExecutorService executor = newExecutor();

            try {
                DelayBlock<String> block = DelayBlock.<String>builder()
                        .delay(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                block.linkTo(new Target<>() {
                    @Override
                    public boolean post(String item) {
                        return true;
                    }

                    @Override
                    public int inFlight() {
                        return 5;
                    }
                });

                block.post("a");
                block.post("b");
                block.post("c");

                assertEquals(block.size() + 5, block.inFlight());
            } finally {
                executor.shutdown();
            }
        }
    }
}
