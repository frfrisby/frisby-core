package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.mocks.MockInterruptedQueue;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GroupBlock} and its builder.  Covers builder validation, grouping
 * semantics, idle-timeout flushing, the completion lifecycle, {@link GroupObserver} retention
 * policies, both {@code post} overloads, and all three delegate handler callbacks.
 *
 * <p>{@link DefaultGroupBlock} uses an {@code ArrayBlockingQueue} as its inbound queue, with
 * a {@link CapacityGate} (fair {@code Semaphore}) layered on top to enforce end-to-end capacity.
 * Permits are acquired by the posting thread and released only after a group is published to the
 * downstream target, ensuring that items held in the internal {@code groups} {@code HashMap}
 * count against the configured capacity.</p>
 */
class GroupBlockTest {
    private static final String PREFIX = "TestGroup";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'GroupBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";

    // A no-op target that accepts every batch.
    private static final Target<List<String>> ACCEPT = batch -> true;

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
        void nullGroupingFunction_throwsNullValueException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NullValueException.class,
                        () -> GroupBlock.<String, String>builder()
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void nullExecutor_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .build()
            );
        }

        @Test
        void nonPositiveTimeout_throwsDurationOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        DurationOutsideRangeException.class,
                        () -> GroupBlock.<String, String>builder()
                                .groupingFunction(item -> item)
                                .timeout(Duration.ZERO)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void nonPositiveIdleTimeout_throwsDurationOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        DurationOutsideRangeException.class,
                        () -> GroupBlock.<String, String>builder()
                                .groupingFunction(item -> item)
                                .idleTimeout(Duration.ZERO)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void defaultValues_capacityTimeoutIdleTimeoutAndMaxGroupSize() {
            NamedExecutorService executor = newExecutor();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .executor(executor)
                        .build();

                assertEquals(1024, block.capacity());
                assertEquals(Duration.ofSeconds(10), block.timeout());
                assertEquals(Duration.ofSeconds(5), block.idleTimeout());
                assertEquals(128, block.maxGroupSize());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void negativeMaxGroupSize_throwsNumericValueOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NumericValueOutsideRangeException.class,
                        () -> GroupBlock.<String, String>builder()
                                .groupingFunction(item -> item)
                                .maxGroupSize(-1)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void zeroMaxGroupSize_throwsNumericValueOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NumericValueOutsideRangeException.class,
                        () -> GroupBlock.<String, String>builder()
                                .groupingFunction(item -> item)
                                .maxGroupSize(0)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void configuredMaxGroupSize_returnsConfiguredValue() {
            NamedExecutorService executor = newExecutor();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .maxGroupSize(3)
                        .executor(executor)
                        .build();

                assertEquals(3, block.maxGroupSize());
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
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .executor(executor)
                        .build();

                assertEquals(0, block.size());
            } finally {
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
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .build();

                    block.complete();

                    assertFalse(block.post("hello"));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void postWhenCallingThreadInterrupted_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch invoked = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    MockInterruptedQueue<String> mockQueue = new MockInterruptedQueue<>();

                    DefaultGroupBlock<String, String> block = new DefaultGroupBlock<>(
                            mockQueue,
                            item -> item,
                            DefaultGroupBlock.DEFAULT_TIMEOUT,
                            DefaultGroupBlock.DEFAULT_IDLE_TIMEOUT,
                            DefaultGroupBlock.DEFAULT_CAPACITY,
                            DefaultGroupBlock.DEFAULT_MAX_GROUP_SIZE,
                            executor,
                            null,
                            null,
                            null,
                            null
                    );

                    block.linkTo(ACCEPT);

                    Thread poster = new Thread(() -> {
                        postResult.set(block.post("hello"));
                        invoked.countDown();
                    });
                    poster.start();

                    assertTrue(invoked.await(5, TimeUnit.SECONDS));

                    poster.join(5_000);
                    assertTrue(poster.isInterrupted());

                    assertFalse(postResult.get());
                    assertTrue(mockQueue.awaitPut(5, TimeUnit.SECONDS));
                    assertEquals(1, mockQueue.putInvokes());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void validItem_returnsTrueAndDeliversToTarget() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(1);
                AtomicReference<List<String>> received = new AtomicReference<>();

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .timeout(Duration.ofSeconds(10))
                            .idleTimeout(Duration.ofSeconds(2))
                            .build();

                    block.linkTo(item -> {
                        received.set(item);
                        delivered.countDown();
                        return true;
                    });

                    assertTrue(block.post("hello"));
                    assertTrue(block.post("hello"));
                    assertTrue(block.post("hello"));

                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertEquals(List.of("hello", "hello", "hello"), received.get());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void validItem_multipleGroupsReturnsTrueAndDeliversToTarget() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(4);
                List<List<String>> received = new CopyOnWriteArrayList<>();

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .timeout(Duration.ofSeconds(10))
                            .idleTimeout(Duration.ofSeconds(1))
                            .build();

                    block.linkTo(item -> {
                        received.add(item);
                        delivered.countDown();
                        return true;
                    });

                    assertTrue(block.post("hello-1"));
                    assertTrue(block.post("hello-2"));
                    Thread.sleep(500);

                    assertTrue(block.post("hello-3"));
                    Thread.sleep(500);

                    assertTrue(block.post("hello-4"));

                    assertTrue(delivered.await(5, TimeUnit.SECONDS));

                    // Each item forms its own group (unique keys).  "hello-1" and "hello-2" expire
                    // in the same flushExpiredGroups() scan; their delivery order depends on HashMap
                    // iteration and is therefore non-deterministic.  Assert presence, not position.
                    assertEquals(4, received.size());
                    assertTrue(received.stream().anyMatch(b -> b.equals(List.of("hello-1"))));
                    assertTrue(received.stream().anyMatch(b -> b.equals(List.of("hello-2"))));
                    assertTrue(received.stream().anyMatch(b -> b.equals(List.of("hello-3"))));
                    assertTrue(received.stream().anyMatch(b -> b.equals(List.of("hello-4"))));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void multipleItems_capacityReached() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetLatch = new CountDownLatch(1);
                CountDownLatch posted = new CountDownLatch(1);
                List<String> sent = new ArrayList<>();
                List<Integer> blockSizes = new ArrayList<>();
                CountDownLatch delivered = new CountDownLatch(1);
                AtomicBoolean postBlocked = new AtomicBoolean(false);

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .maxGroupSize(1)
                            .capacity(2)
                            .build();

                    block.linkTo(item -> {
                        try {
                            // Block all incoming posts until the latch is signaled
                            targetLatch.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        delivered.countDown();

                        return true;
                    });

                    Thread poster = new Thread(() -> {
                        List<String> items = List.of("hello-1", "hello-2", "hello-3");

                        String item = items.get(0);
                        block.post(item);
                        sent.add(item);
                        blockSizes.add(block.size());

                        item = items.get(1);
                        block.post(item);
                        sent.add(item);
                        blockSizes.add(block.size());

                        item = items.get(2);
                        if (!block.post(item, Duration.ofSeconds(1))) {
                            // The block is already at capacity and the item cannot be accepted
                            postBlocked.set(true);
                            blockSizes.add(block.size());
                        } else {
                            // The block failed to block and incorrectly accepted the item
                            postBlocked.set(false);
                            sent.add(item);
                            blockSizes.add(block.size());
                        }

                        posted.countDown();
                    });
                    poster.start();

                    assertTrue(posted.await(5, TimeUnit.SECONDS));

                    poster.join(5_000);
                    assertFalse(poster.isInterrupted());

                    // Unblock the target
                    targetLatch.countDown();

                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertTrue(postBlocked.get());
                    assertEquals(List.of("hello-1", "hello-2"), sent);
                    assertEquals(List.of(1, 2, 2), blockSizes);
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void capacityExhausted_interruptedWhileWaitingOnSemaphore_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    // capacity(1) + maxGroupSize(1): the single permit is consumed when "a" is
                    // published and is not released until postToFirst() returns (i.e. until the
                    // blocking target is released).
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .capacity(1)
                            .maxGroupSize(1)
                            .executor(executor)
                            .build();

                    block.linkTo(batch -> {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        return true;
                    });

                    // "a" exhausts the single capacity permit; the permit is not released
                    // until postToFirst() returns (i.e. until targetRelease is counted down).
                    block.post("a");
                    assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                    // The poster must block on capacityGate.acquire() — no permits are available.
                    Thread poster = new Thread(() -> postResult.set(block.post("b")));
                    poster.start();

                    // Spin until the posting thread is confirmed parked in Semaphore.acquire().
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                    while (poster.getState() != Thread.State.WAITING &&
                            System.nanoTime() < deadline) {
                        Thread.onSpinWait();
                    }

                    assertEquals(Thread.State.WAITING, poster.getState());

                    // Interrupt the poster while it is parked on the semaphore — acquired is
                    // still false at this point, so capacityGate.release() must NOT be called.
                    poster.interrupt();

                    poster.join(5_000);

                    assertFalse(postResult.get());
                    assertTrue(poster.isInterrupted());
                } finally {
                    targetRelease.countDown();
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
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
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
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
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
            void validItem_returnsTrue() {
                NamedExecutorService executor = newExecutor();

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertTrue(block.post("hello", Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .build();

                    block.complete();

                    assertFalse(block.post("hello", Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void nullItem_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null, Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void postWhenCallingThreadInterrupted_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch invoked = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    MockInterruptedQueue<String> mockQueue = new MockInterruptedQueue<>();

                    DefaultGroupBlock<String, String> block = new DefaultGroupBlock<>(
                            mockQueue,
                            item -> item,
                            DefaultGroupBlock.DEFAULT_TIMEOUT,
                            DefaultGroupBlock.DEFAULT_IDLE_TIMEOUT,
                            DefaultGroupBlock.DEFAULT_CAPACITY,
                            DefaultGroupBlock.DEFAULT_MAX_GROUP_SIZE,
                            executor,
                            null,
                            null,
                            null,
                            null
                    );

                    block.linkTo(ACCEPT);

                    Thread poster = new Thread(() -> {
                        postResult.set(block.post("hello", Duration.ofSeconds(5)));
                        invoked.countDown();
                    });
                    poster.start();

                    assertTrue(invoked.await(5, TimeUnit.SECONDS));

                    poster.join(5_000);
                    assertTrue(poster.isInterrupted());

                    assertFalse(postResult.get());
                    assertTrue(mockQueue.awaitPut(5, TimeUnit.SECONDS));
                    assertEquals(1, mockQueue.putInvokes());
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void capacityExhausted_interruptedWhileWaitingOnSemaphore_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch targetStarted = new CountDownLatch(1);
                CountDownLatch targetRelease = new CountDownLatch(1);
                AtomicBoolean postResult = new AtomicBoolean(true);

                try {
                    // capacity(1) + maxGroupSize(1): the single permit is consumed when "a" is
                    // published and is not released until postToFirst() returns (i.e. until the
                    // blocking target is released).
                    GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                            .groupingFunction(item -> item)
                            .capacity(1)
                            .maxGroupSize(1)
                            .executor(executor)
                            .build();

                    block.linkTo(batch -> {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        return true;
                    });

                    // "a" exhausts the single capacity permit; the permit is not released
                    // until postToFirst() returns (i.e. until targetRelease is counted down).
                    block.post("a");
                    assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                    // The poster must block on capacityGate.tryAcquire() — no permits are available.
                    Thread poster = new Thread(() -> postResult.set(block.post("b", Duration.ofSeconds(10))));
                    poster.start();

                    // Spin until the posting thread is confirmed parked in Semaphore.tryAcquire().
                    // tryAcquire(nanos) uses a timed wait, so the thread enters TIMED_WAITING.
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                    while (poster.getState() != Thread.State.TIMED_WAITING &&
                            System.nanoTime() < deadline) {
                        Thread.onSpinWait();
                    }

                    assertEquals(Thread.State.TIMED_WAITING, poster.getState());

                    // Interrupt the poster while it is parked on the semaphore — acquired is
                    // still false at this point, so capacityGate.release() must NOT be called.
                    poster.interrupt();

                    poster.join(5_000);

                    assertFalse(postResult.get());
                    assertTrue(poster.isInterrupted());
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
        void linkToCalledTwice_throwsIllegalStateException() {
            NamedExecutorService executor = newExecutor();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
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
    }

    // -------------------------------------------------------------------------
    // Grouping semantics
    // -------------------------------------------------------------------------

    @Nested
    class Grouping {
        @Test
        void sameKey_itemsGroupedTogether() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<List<String>> received = new CopyOnWriteArrayList<>();
            CountDownLatch delivered = new CountDownLatch(1);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")  // all items share the same key
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));
                    delivered.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(1, received.size());
                assertEquals(List.of("a", "b", "c"), received.get(0));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void differentKeys_produceSeparateGroups() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<List<String>> received = new CopyOnWriteArrayList<>();
            CountDownLatch delivered = new CountDownLatch(2);

            try {
                // Key is the first character: "a" items → "a", "b" items → "b"
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item.substring(0, 1))
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));
                    delivered.countDown();
                    return true;
                });

                block.post("a1");
                block.post("a2");
                block.post("b1");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(2, received.size());
                assertTrue(received.stream().anyMatch(
                        batch -> batch.size() == 2 && batch.containsAll(List.of("a1", "a2"))
                ));
                assertTrue(received.stream().anyMatch(
                        batch -> batch.equals(List.of("b1"))
                ));
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Idle timeout flushing
    // -------------------------------------------------------------------------

    @Nested
    class IdleTimeout {
        @Test
        void idleTimeoutExpiry_flushesGroup() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<List<String>> received = new AtomicReference<>();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .idleTimeout(Duration.ofMillis(150))
                        .timeout(Duration.ofSeconds(10))
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.set(new ArrayList<>(batch));
                    delivered.countDown();
                    return true;
                });

                block.post("hello");

                // Wait longer than idleTimeout — the worker should flush automatically.
                assertTrue(delivered.await(3, TimeUnit.SECONDS));
                assertNotNull(received.get());
                assertTrue(received.get().contains("hello"));
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
        void complete_flushesAllPendingGroupsImmediately() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredItemCount = new AtomicInteger(0);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item.substring(0, 1))
                        .timeout(Duration.ofSeconds(30))
                        .idleTimeout(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    deliveredItemCount.addAndGet(batch.size());
                    return true;
                });

                block.post("a1");
                block.post("a2");
                block.post("b1");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(3, deliveredItemCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void complete_isIdempotent() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredItemCount = new AtomicInteger(0);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    deliveredItemCount.addAndGet(batch.size());
                    return true;
                });

                block.post("a");

                block.complete();
                block.complete();

                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(1, deliveredItemCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void isRunning_returnsTrueInitially() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch delivered = new CountDownLatch(1);

            try {
                DefaultGroupBlock<String, String> block =
                        (DefaultGroupBlock<String, String>) GroupBlock.<String, String>builder()
                                .groupingFunction(item -> "k")
                                .idleTimeout(Duration.ofMillis(50))
                                .executor(executor)
                                .build();

                block.linkTo(batch -> {
                    delivered.countDown();
                    return true;
                });

                block.post("probe");

                // Wait for first batch delivery — worker is guaranteed to be running.
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
                DefaultGroupBlock<String, String> block =
                        (DefaultGroupBlock<String, String>) GroupBlock.<String, String>builder()
                                .groupingFunction(item -> "k")
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
        void completion_resolvesAfterAllItemsDelivered() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredItemCount = new AtomicInteger(0);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    deliveredItemCount.addAndGet(batch.size());
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(3, deliveredItemCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void complete_withItemsInQueueWhileWorkerBusy_drainsAndDeliversAllItems() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);
            List<List<String>> received = new CopyOnWriteArrayList<>();

            try {
                // maxGroupSize(1) causes each item to be published immediately as its own
                // single-item group.  capacity(10) allows multiple items to be queued while
                // the worker is busy delivering to the blocking target.
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .maxGroupSize(1)
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));

                    if ("a".equals(batch.get(0))) {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return true;
                });

                // "a" is delivered to the blocking target — the worker is now stuck in postToTarget().
                block.post("a");
                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                // "b" and "c" land in the inbound queue while the worker is stuck in postToTarget().
                block.post("b");
                block.post("c");

                // complete() marks the queue as completed.  targetRelease is then counted down
                // so the worker can finish delivering "a" and loop back to process "b" and "c".
                block.complete();
                targetRelease.countDown();

                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(3, received.size());
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("a"))));
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("b"))));
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("c"))));
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }

        @Test
        void complete_sameKeyItemsInQueueWhileWorkerBusy_groupsAndDeliversCorrectly() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);
            List<List<String>> received = new CopyOnWriteArrayList<>();

            try {
                // maxGroupSize(2) + groupingFunction(first char) so that "b1" and "b2" share
                // key "b".  "a1" and "a2" share key "a" and are published as a batch once
                // the group reaches size 2, at which point the target blocks.
                // While the target is blocking, "b1" and "b2" land in the inbound queue.
                // After targetRelease is counted down the worker finishes delivering ["a1", "a2"],
                // then processes "b1" (creates group "b") and "b2" (appends to group "b",
                // reaching size 2 and triggering publishGroup).
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item.substring(0, 1))
                        .maxGroupSize(2)
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));

                    if (batch.contains("a1")) {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return true;
                });

                // "a1" and "a2" share key "a".  Once the group reaches size 2 the worker
                // publishes it and blocks inside the target.
                block.post("a1");
                block.post("a2");
                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                // "b1" and "b2" land in the inbound queue while the worker is stuck in
                // postToTarget() delivering the ["a1", "a2"] batch.
                block.post("b1");
                block.post("b2");

                // complete() marks the queue as completed.  targetRelease is then counted down
                // so the worker can finish delivering ["a1", "a2"] and loop back to process
                // "b1" and "b2" via the normal item loop.
                block.complete();
                targetRelease.countDown();

                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(2, received.size());
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("a1", "a2"))));
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("b1", "b2"))));
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }

        @Test
        void complete_itemInQueueWhileWorkerBusy_flushesViaMainLoop() throws Exception {
            // An item is posted while the worker is busy delivering a previous group to a
            // blocking target.  After complete() marks the queue as completed and targetRelease
            // is counted down, the worker finishes the current delivery, then processes the
            // queued item through the normal item loop: processItem() runs the observer (RELEASE)
            // which publishes it immediately.  flushAllGroups() at the drain exit finds nothing
            // remaining.
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);
            List<List<String>> received = new CopyOnWriteArrayList<>();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .groupObserver(group -> Retention.RELEASE)
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));

                    if ("first".equals(batch.get(0))) {
                        targetStarted.countDown();

                        try {
                            targetRelease.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    return true;
                });

                block.post("first");
                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                // "extra" lands in the inbound queue while the worker is stuck delivering "first".
                block.post("extra");

                // complete() marks the queue as completed.  targetRelease is then counted down
                // so the worker can finish delivering "first" and process "extra" via the
                // normal item loop.
                block.complete();
                targetRelease.countDown();

                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(2, received.size());
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("first"))));
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("extra"))));
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // GroupObserver
    // -------------------------------------------------------------------------

    @Nested
    class Observer {
        @Test
        void retentionRelease_flushesGroupImmediately() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<List<String>> received = new CopyOnWriteArrayList<>();
            CountDownLatch delivered = new CountDownLatch(2);

            try {
                // All items share the same key; observer always returns RELEASE.
                // Each item is published as its own single-item group immediately.
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .groupObserver(group -> Retention.RELEASE)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));
                    delivered.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(2, received.size());
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("a"))));
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("b"))));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void retentionHold_accumulatesItemsBeforeFlush() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<List<String>> received = new CopyOnWriteArrayList<>();
            CountDownLatch delivered = new CountDownLatch(1);

            try {
                // All items share the same key; observer always returns HOLD.
                // Expected: one flush with all items — [a, b].
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .groupObserver(group -> Retention.HOLD)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));
                    delivered.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(1, received.size());
                assertEquals(List.of("a", "b"), received.get(0));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void observerException_isSwallowed_errorHandlerCalled_groupHeld() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch errorCaptured = new CountDownLatch(1);
            AtomicReference<Throwable> capturedError = new AtomicReference<>();
            AtomicInteger deliveredItemCount = new AtomicInteger(0);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .groupObserver(group -> {
                            throw new RuntimeException("observer error");
                        })
                        .errorOccurredHandler((source, target, item, error) -> {
                            if (capturedError.compareAndSet(null, error)) {
                                errorCaptured.countDown();
                            }
                        })
                        .build();

                block.linkTo(batch -> {
                    deliveredItemCount.addAndGet(batch.size());
                    return true;
                });

                block.post("a");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                // Error handler must have been called with the observer exception.
                assertTrue(errorCaptured.await(5, TimeUnit.SECONDS));
                assertNotNull(capturedError.get());
                assertEquals("observer error", capturedError.get().getMessage());

                // Group was held (observer returned HOLD after exception) and flushed on complete().
                assertEquals(1, deliveredItemCount.get());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // MaxGroupSize
    // -------------------------------------------------------------------------

    @Nested
    class MaxGroupSize {
        @Test
        void maxGroupSizeReached_immediatelyFlushesGroup() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<List<String>> received = new CopyOnWriteArrayList<>();
            CountDownLatch firstBatch = new CountDownLatch(1);

            try {
                // maxGroupSize(2): group should be published as soon as it has 2 items,
                // without waiting for the idle or max timeout.
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .timeout(Duration.ofSeconds(30))
                        .idleTimeout(Duration.ofSeconds(30))
                        .maxGroupSize(2)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));
                    firstBatch.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");

                // The group should be flushed immediately when size reaches 2 —
                // no need to wait for a timeout.
                assertTrue(firstBatch.await(5, TimeUnit.SECONDS));
                assertEquals(1, received.size());
                assertEquals(List.of("a", "b"), received.get(0));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void maxGroupSizeExceededAcrossMultiplePosts_flushesInSeparateBatches() throws Exception {
            NamedExecutorService executor = newExecutor();
            List<List<String>> received = new CopyOnWriteArrayList<>();
            CountDownLatch bothBatches = new CountDownLatch(2);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .timeout(Duration.ofSeconds(30))
                        .idleTimeout(Duration.ofSeconds(30))
                        .maxGroupSize(2)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.add(new ArrayList<>(batch));
                    bothBatches.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");  // first flush: [a, b]
                block.post("c");
                block.post("d");  // second flush: [c, d]

                assertTrue(bothBatches.await(5, TimeUnit.SECONDS));
                assertEquals(2, received.size());
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("a", "b"))));
                assertTrue(received.stream().anyMatch(b -> b.equals(List.of("c", "d"))));
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

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .itemPostedHandler((source, item, wasAccepted) -> {
                            postedItem.set(item);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(ACCEPT);
                block.post("hello");

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertEquals("hello", postedItem.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void itemDeliveredHandler_isCalledOnBatchDelivery() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<List<String>> deliveredBatch = new AtomicReference<>();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .itemDeliveredHandler((source, target, batch) -> {
                            deliveredBatch.set(batch);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(ACCEPT);
                block.post("hello");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertNotNull(deliveredBatch.get());
                assertTrue(deliveredBatch.get().contains("hello"));
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
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> "k")
                        .executor(executor)
                        .errorOccurredHandler((source, target, item, error) -> {
                            if (capturedError.compareAndSet(null, error)) {
                                notified.countDown();
                            }
                        })
                        .build();

                block.linkTo(batch -> {
                    throw new RuntimeException("target error");
                });

                block.post("hello");

                block.complete();

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
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix(PREFIX)
                    .build();

            AtomicInteger batchesReceived = new AtomicInteger();

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(s -> s)
                        .maxGroupSize(10)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    batchesReceived.incrementAndGet();
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
                assertEquals(2, batchesReceived.get());
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
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .executor(executor)
                        .build();

                assertEquals(0, block.inFlight());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void inFlight_itemsInFlight_equalsSizeWhenDownstreamInFlightIsZero() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);

            try {
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
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
                block.complete();

                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                // size() counts items via CapacityGate — includes both queued and grouped items.
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
                GroupBlock<String, String> block = GroupBlock.<String, String>builder()
                        .groupingFunction(item -> item)
                        .capacity(10)
                        .executor(executor)
                        .build();

                block.linkTo(new Target<>() {
                    @Override
                    public boolean post(java.util.List<String> item) {
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
                block.complete();

                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                assertEquals(block.size() + 5, block.inFlight());
            } finally {
                targetRelease.countDown();
                executor.shutdown();
            }
        }
    }
}

