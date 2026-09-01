package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;
import software.frisby.core.concurrency.mocks.UncaughtExceptionCapture;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BufferBlock} and its builder.  Covers builder validation, capacity, size, both
 * {@code post} overloads, {@code linkTo}, completion lifecycle, and all
 * three delegate handler callbacks.
 */
class BufferBlockTest {
    private static final String PREFIX = "TestBuffer";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'BufferBlock' block already has a linked target. A single-target block may only be linked to one downstream target.";
    private static final String LINK_TO_SELF_MSG =
            "The 'target' value is invalid. A block cannot be linked to itself.";

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
                    () -> BufferBlock.<String>builder().build()
            );
        }

        @Test
        void capacityBelowMin_throwsNumericValueOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NumericValueOutsideRangeException.class,
                        () -> BufferBlock.<String>builder()
                                .capacity(0)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .executor(executor)
                        .build();

                assertEquals(1024, block.capacity());
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(64)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null));
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
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
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
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
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
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(2)
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
            void multipleItems_capacityReached() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);
                CountDownLatch posterDone = new CountDownLatch(1);
                AtomicBoolean thirdPostBlocked = new AtomicBoolean(false);

                try {
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(2)
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
        }

        @Nested
        class WithTimeout {
            @Test
            void nullTimeout_throwsNullValueException() {
                NamedExecutorService executor = newExecutor();

                try {
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
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
            void negativeTimeout_throwsDurationOutsideRangeException() {
                NamedExecutorService executor = newExecutor();

                try {
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertThrows(
                            DurationOutsideRangeException.class,
                            () -> block.post("hello", Duration.ofSeconds(-1))
                    );
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void zeroTimeout_capacityAvailable_returnsTrueImmediately() {
                // Duration.ZERO means "attempt immediately, do not wait at all" — it must not be
                // rejected as an invalid argument when capacity is actually available.
                NamedExecutorService executor = newExecutor();

                try {
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertTrue(block.post("hello", Duration.ZERO));
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
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
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
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(10)
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
                    BufferBlock<String> block = BufferBlock.<String>builder()
                            .capacity(2)
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
                    // (permit 1 consumed; permit not released until target returns).
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(ACCEPT);

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> block.linkTo(ACCEPT)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                DefaultBufferBlock<String> block = (DefaultBufferBlock<String>) BufferBlock.<String>builder()
                        .capacity(10)
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
                DefaultBufferBlock<String> block = (DefaultBufferBlock<String>) BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(ACCEPT);
                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertFalse(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void complete_drainsAllBufferedItemsBeforeResolving() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
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
    // Concurrency
    // -------------------------------------------------------------------------

    @Nested
    class Concurrency {
        @Test
        void multipleConcurrentPosters_allItemsDelivered() throws Exception {
            NamedExecutorService executor = newExecutor();

            int threadCount = 4;
            int itemsPerThread = 10;
            int totalItems = threadCount * itemsPerThread;

            CountDownLatch allDelivered = new CountDownLatch(totalItems);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                BufferBlock<Integer> block = BufferBlock.<Integer>builder()
                        .capacity(64)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    allDelivered.countDown();
                    return true;
                });

                // All posting threads wait at the start gate so they hit the block simultaneously.
                CountDownLatch ready = new CountDownLatch(threadCount);
                CountDownLatch start = new CountDownLatch(1);

                for (int t = 0; t < threadCount; t++) {
                    int base = t * itemsPerThread;

                    new Thread(() -> {
                        ready.countDown();

                        try {
                            start.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }

                        for (int i = 0; i < itemsPerThread; i++) {
                            block.post(base + i);
                        }
                    }).start();
                }

                assertTrue(ready.await(5, TimeUnit.SECONDS));
                start.countDown();

                assertTrue(allDelivered.await(10, TimeUnit.SECONDS));
                assertEquals(totalItems, deliveredCount.get());
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
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() {
            NamedExecutorService executor = newExecutor();

            AtomicInteger received = new AtomicInteger();

            try {
                BufferBlock<String> block = BufferBlock.<String>builder()
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

                block.complete();

                assertTrue(block.awaitCompletion(Duration.ofSeconds(5)));
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
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .executor(executor)
                        .build();

                assertEquals(0, block.inFlight());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void inFlight_itemsInQueue_equalsSizeWhenDownstreamInFlightIsZero() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);

            try {
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    targetStarted.countDown();

                    try {
                        targetRelease.await();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                // One item is being delivered (blocking target); the rest remain in the queue.
                // Downstream's inFlight() is the default 0, so inFlight() == size().
                assertEquals(block.size(), block.inFlight());
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }

        @Test
        void inFlight_withDownstreamInFlight_addsBothCounts() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);

            try {
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(new Target<>() {
                    @Override
                    public boolean post(String item) {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }

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

                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                assertEquals(block.size() + 5, block.inFlight());
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }

        @Test
        void inFlight_downstreamActionBlockCurrentlyRunning_reportsOne() throws Exception {
            // Report 1 end-to-end repro: a Buffer whose worker has dequeued an item and handed
            // it to a downstream ActionBlock that is still executing must report inFlight() == 1
            // for the duration of that call, not 0 — even though the item has already left the
            // Buffer's own queue (size() == 0 at that point).
            NamedExecutorService executor = newExecutor();
            CountDownLatch actionStarted = new CountDownLatch(1);
            CountDownLatch releaseAction = new CountDownLatch(1);

            try {
                BufferBlock<String> block = BufferBlock.<String>builder()
                        .capacity(1)
                        .executor(executor)
                        .build();

                ActionBlock<String> sink = ActionBlock.<String>builder()
                        .action(item -> {
                            actionStarted.countDown();

                            try {
                                releaseAction.await();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        })
                        .build();

                block.linkTo(sink);
                block.post("hello");

                assertTrue(actionStarted.await(5, TimeUnit.SECONDS));

                assertEquals(0, block.size(), "the item has already been dequeued from the Buffer's own queue");
                assertEquals(1, block.inFlight(), "the downstream ActionBlock is still processing the item");
            } finally {
                releaseAction.countDown();
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Worker resilience — Report 2: uncaught exceptions must not silently and
    // permanently kill the worker thread.
    // -------------------------------------------------------------------------

    @Nested
    class WorkerResilience {
        @Test
        void runtimeExceptionFromTarget_noHandlerConfigured_logsAndWorkerSurvives() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch secondItemDelivered = new CountDownLatch(1);

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("BufferBlock")
                                    && null != e.thrown()
                                    && "boom".equals(e.thrown().getMessage()))
                            .build()
                    )
                    .build()) {
                DefaultBufferBlock<Integer> block = (DefaultBufferBlock<Integer>) BufferBlock.<Integer>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    if (1 == item) {
                        throw new RuntimeException("boom");
                    }

                    secondItemDelivered.countDown();
                    return true;
                });

                block.post(1);
                block.post(2);

                assertTrue(secondItemDelivered.await(5, TimeUnit.SECONDS),
                        "worker thread should have survived the first item's exception and delivered the second item");
                assertTrue(block.isRunning());

                verifier.assertExpectations(Duration.ofSeconds(5));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void nonFatalErrorFromTarget_noHandlerConfigured_logsAndWorkerSurvives() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch secondItemDelivered = new CountDownLatch(1);

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> null != e.thrown()
                                    && e.thrown() instanceof AssertionError
                                    && "boom".equals(e.thrown().getMessage()))
                            .build()
                    )
                    .build()) {
                DefaultBufferBlock<Integer> block = (DefaultBufferBlock<Integer>) BufferBlock.<Integer>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    if (1 == item) {
                        throw new AssertionError("boom");
                    }

                    secondItemDelivered.countDown();
                    return true;
                });

                block.post(1);
                block.post(2);

                assertTrue(secondItemDelivered.await(5, TimeUnit.SECONDS),
                        "worker thread should have survived the non-fatal Error and delivered the second item");
                assertTrue(block.isRunning());

                verifier.assertExpectations(Duration.ofSeconds(5));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void fatalErrorFromTarget_propagatesAsUncaughtExceptionAndKillsWorker() {
            NamedExecutorService executor = newExecutor();
            StackOverflowError fatal = new StackOverflowError("fatal boom");

            try (UncaughtExceptionCapture capture = new UncaughtExceptionCapture()) {
                DefaultBufferBlock<Integer> block = (DefaultBufferBlock<Integer>) BufferBlock.<Integer>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    throw fatal;
                });

                block.post(1);

                assertTrue(capture.await(5), "the fatal error should have escaped as a genuinely uncaught exception");
                assertSame(fatal, capture.thrown());

                // isRunning() correctly reports false — WorkerLifecycle.stopRunning() runs in
                // the worker's outer finally on every exit path, including this fatal one.
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (block.isRunning() && System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }
                assertFalse(block.isRunning());

                // But completion()/awaitCompletion() never resolves on a fatal death — finish()
                // is deliberately skipped so this documented trade-off remains observable.
                assertFalse(block.awaitCompletion(Duration.ofMillis(200)));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void executorShutdownWithoutComplete_stopsWorkerCleanlyButLeavesCompletionUnresolved() throws Exception {
            // A hard shutdown (no prior complete()/awaitCompletion() drain) interrupts the
            // worker mid-delivery. Unlike the fatal-error case above, this must be a genuinely
            // clean stop — no uncaught exception — while still leaving completion() unresolved,
            // matching WorkerLifecycle's "interruptionSignalsCompletion == false" contract for
            // queue-based workers: interruption is never a legitimate "done" signal here.
            NamedExecutorService executor = newExecutor();
            CountDownLatch deliveryStarted = new CountDownLatch(1);
            CountDownLatch releaseDelivery = new CountDownLatch(1);

            try (UncaughtExceptionCapture capture = new UncaughtExceptionCapture()) {
                DefaultBufferBlock<Integer> block = (DefaultBufferBlock<Integer>) BufferBlock.<Integer>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveryStarted.countDown();

                    try {
                        releaseDelivery.await();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    return true;
                });

                block.post(1);

                assertTrue(deliveryStarted.await(5, TimeUnit.SECONDS));

                // Hard stop — no complete() call at all.
                executor.shutdown();

                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (block.isRunning() && System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }
                assertFalse(block.isRunning(), "the worker must stop once the executor is shut down");

                assertFalse(block.awaitCompletion(Duration.ofMillis(200)),
                        "completion() must not resolve for a hard stop with no complete() ever called");
                assertFalse(capture.await(1), "an ordinary interrupt must never surface as an uncaught exception");
            } finally {
                releaseDelivery.countDown();
                executor.shutdown();
            }
        }

        @Test
        void runtimeExceptionFromTarget_capacityPermitIsReleasedForNextItem() throws Exception {
            // Capacity 1: if the permit held by the failing first item were not released, the
            // second post() would block forever.  A short, bounded post(item, timeout) proves
            // the permit was released without risking an indefinite test hang.
            NamedExecutorService executor = newExecutor();
            CountDownLatch secondItemDelivered = new CountDownLatch(1);

            try {
                BufferBlock<Integer> block = BufferBlock.<Integer>builder()
                        .capacity(1)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    if (1 == item) {
                        throw new RuntimeException("boom");
                    }

                    secondItemDelivered.countDown();
                    return true;
                });

                block.post(1);

                assertTrue(block.post(2, Duration.ofSeconds(5)),
                        "the capacity permit held by the failed first item should have been released");
                assertTrue(secondItemDelivered.await(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }
    }
}

