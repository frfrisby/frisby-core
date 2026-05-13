package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PriorityBufferBlock} and its builder.  Covers builder validation, capacity,
 * size, both {@code post} overloads, {@code linkTo}, priority ordering,
 * the completion lifecycle, and all three delegate handler callbacks.
 */
class PriorityBufferBlockTest {
    private static final String PREFIX = "TestPriorityBuffer";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'PriorityBufferBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";
    private static final String LINK_TO_SELF_MSG =
            "The 'target' value is invalid.  A block cannot be linked to itself.";

    // A no-op target that accepts every item.
    private static final Target<String> ACCEPT = item -> true;

    // Natural alphabetical ordering used throughout these tests.
    private static final Comparator<String> NATURAL_ORDER = Comparator.naturalOrder();

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
                    () -> PriorityBufferBlock.<String>builder()
                            .comparator(NATURAL_ORDER)
                            .build()
            );
        }

        @Test
        void nullComparator_throwsNullValueException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NullValueException.class,
                        () -> PriorityBufferBlock.<String>builder()
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
                        () -> PriorityBufferBlock.<String>builder()
                                .capacity(0)
                                .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(64)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
                            .executor(executor)
                            .build();

                    block.complete();

                    assertFalse(block.post("hello"));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void whenPostingThreadInterrupted_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(2)
                            .comparator(NATURAL_ORDER)
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

                    // "a" is taken by the worker and held in the blocking target (permit 1 consumed).
                    block.post("a");
                    assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                    // "b" fills the second capacity slot (permit 2 consumed); the buffer is now at capacity.
                    block.post("b");

                    // The poster for "c" blocks inside Semaphore.acquire() — interrupting it causes
                    // CapacityGate.acquire() to throw InterruptedException, so buffer.post() returns false.
                    Thread poster = new Thread(() -> postResult.set(block.post("c")));
                    poster.start();

                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                    while (poster.getState() != Thread.State.WAITING &&
                            System.nanoTime() < deadline) {
                        Thread.onSpinWait();
                    }

                    assertEquals(Thread.State.WAITING, poster.getState());

                    poster.interrupt();
                    poster.join(5_000);

                    assertFalse(postResult.get());
                } finally {
                    targetRelease.countDown();
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(2)
                            .comparator(NATURAL_ORDER)
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
                    assertTrue(cPostSucceeded.get());
                } finally {
                    executor.shutdown();
                }
            }
        }

        @Nested
        class WithTimeout {
            @Test
            void nullItem_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null, Duration.ofSeconds(1)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void nullTimeout_throwsNullValueException() {
                NamedExecutorService executor = newExecutor();

                try {
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(10)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(2)
                            .comparator(NATURAL_ORDER)
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
                    PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                            .capacity(2)
                            .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
    // Priority ordering
    // -------------------------------------------------------------------------

    @Nested
    class Priority {
        @Test
        void samePriority_itemsDeliveredInFifoOrder() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch firstDelivered = new CountDownLatch(1);
            CountDownLatch allPosted = new CountDownLatch(1);
            CountDownLatch allDelivered = new CountDownLatch(3);

            // All strings of equal length compare as equal priority; FIFO tiebreaker must apply.
            Comparator<String> byLength = Comparator.comparingInt(String::length);

            try {
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(byLength)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    received.add(item);
                    firstDelivered.countDown();
                    allDelivered.countDown();

                    if (received.size() == 1) {
                        try {
                            allPosted.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return true;
                });

                // Post "first", wait for it to be taken by the worker, then post the remaining
                // two items so they are both in the priority queue simultaneously.
                block.post("first");
                assertTrue(firstDelivered.await(5, TimeUnit.SECONDS));

                block.post("secnd");
                block.post("third");
                allPosted.countDown();

                assertTrue(allDelivered.await(5, TimeUnit.SECONDS));

                // All three strings are the same length — priority is equal.
                // The tiebreaker is insertion order, so second and third must arrive in FIFO order.
                assertEquals("secnd", received.get(1));
                assertEquals("third", received.get(2));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void itemsPostedOutOfOrder_deliveredInComparatorOrder() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch firstDelivered = new CountDownLatch(1);
            CountDownLatch allPosted = new CountDownLatch(1);
            CountDownLatch allDelivered = new CountDownLatch(3);

            try {
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    received.add(item);
                    firstDelivered.countDown();
                    allDelivered.countDown();

                    // Hold the worker on the first delivery so the remaining items accumulate
                    // in the PriorityBlockingQueue before it drains them in priority order.
                    if (received.size() == 1) {
                        try {
                            allPosted.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return true;
                });

                // Post in reverse alphabetical order.
                block.post("gamma");

                // Wait until the worker has taken "gamma" and is held in the target callback.
                assertTrue(firstDelivered.await(5, TimeUnit.SECONDS));

                // Post remaining items while the worker is blocked; both land in the priority queue.
                block.post("alpha");
                block.post("beta");

                // Release the worker — it now drains the queue in natural (alphabetical) order.
                allPosted.countDown();

                assertTrue(allDelivered.await(5, TimeUnit.SECONDS));

                // The first item is whichever was taken before priority ordering could apply.
                // The second and third must arrive in sorted order.
                assertEquals("alpha", received.get(1));
                assertEquals("beta", received.get(2));
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
                DefaultPriorityBufferBlock<String> block =
                        (DefaultPriorityBufferBlock<String>) PriorityBufferBlock.<String>builder()
                                .capacity(10)
                                .comparator(NATURAL_ORDER)
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
                DefaultPriorityBufferBlock<String> block =
                        (DefaultPriorityBufferBlock<String>) PriorityBufferBlock.<String>builder()
                                .capacity(10)
                                .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .capacity(10)
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<Integer> block = PriorityBufferBlock.<Integer>builder()
                        .capacity(64)
                        .comparator(Comparator.naturalOrder())
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
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger received = new AtomicInteger(0);

            try {
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .comparator(NATURAL_ORDER)
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
                PriorityBufferBlock<String> block = PriorityBufferBlock.<String>builder()
                        .comparator(NATURAL_ORDER)
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
    }
}
