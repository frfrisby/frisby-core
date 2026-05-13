package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BatchBlock} and its builder.  Covers builder validation, both
 * {@code post} overloads, batching semantics (size threshold, timeout, partial-batch flush on
 * {@code complete()}), the completion lifecycle, and all three delegate handler callbacks.
 */
class BatchBlockTest {
    private static final String PREFIX = "TestBatch";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'BatchBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";

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
        void nullExecutor_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> BatchBlock.<String>builder().build()
            );
        }

        @Test
        void capacityBelowMin_throwsNumericValueOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NumericValueOutsideRangeException.class,
                        () -> BatchBlock.<String>builder()
                                .capacity(0)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void batchSizeBelowMin_throwsNumericValueOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        NumericValueOutsideRangeException.class,
                        () -> BatchBlock.<String>builder()
                                .batchSize(0)
                                .executor(executor)
                                .build()
                );
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void nonPositiveTimeout_throwsDurationOutsideRangeException() {
            NamedExecutorService executor = newExecutor();

            try {
                assertThrows(
                        DurationOutsideRangeException.class,
                        () -> BatchBlock.<String>builder()
                                .timeout(Duration.ZERO)
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
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .executor(executor)
                        .build();

                assertEquals(1024, block.capacity());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void defaultBatchSizeAndTimeout_useDefaults() {
            NamedExecutorService executor = newExecutor();

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .executor(executor)
                        .build();

                assertEquals(128, block.batchSize());
                assertEquals(Duration.ofSeconds(5), block.timeout());
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
                BatchBlock<String> block = BatchBlock.<String>builder()
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
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void validItem_returnsTrueAndDeliveredInBatch() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(1);
                AtomicReference<List<String>> received = new AtomicReference<>();

                try {
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .batchSize(1)
                            .executor(executor)
                            .build();

                    block.linkTo(batch -> {
                        received.set(batch);
                        delivered.countDown();
                        return true;
                    });

                    assertTrue(block.post("hello"));
                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertNotNull(received.get());
                    assertEquals(1, received.get().size());
                    assertEquals("hello", received.get().get(0));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    BatchBlock<String> block = BatchBlock.<String>builder()
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
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .capacity(2)
                            .batchSize(1)
                            .executor(executor)
                            .build();

                    block.linkTo(batch -> {
                        if ("a".equals(batch.get(0))) {
                            targetStarted.countDown();

                            try {
                                targetRelease.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        if ("c".equals(batch.get(0))) {
                            cDelivered.countDown();
                        }

                        return true;
                    });

                    // "a" is taken by the worker, immediately flushed as a 1-item batch,
                    // and held in the blocking target (permit 1 consumed; not released until target returns).
                    block.post("a");
                    assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                    // "b" fills the second capacity slot (permit 2 consumed).
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
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .capacity(2)
                            .batchSize(1)
                            .executor(executor)
                            .build();

                    block.linkTo(batch -> {
                        try {
                            // Block all deliveries until the latch is signaled.
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
                            // The block is already at capacity and the item cannot be accepted.
                            postBlocked.set(true);
                            blockSizes.add(block.size());
                        } else {
                            // The block failed to block and incorrectly accepted the item.
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

                    // Unblock the target.
                    targetLatch.countDown();

                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertTrue(postBlocked.get());
                    assertEquals(List.of("hello-1", "hello-2"), sent);
                    assertEquals(List.of(1, 2, 2), blockSizes);
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
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .executor(executor)
                            .build();

                    block.linkTo(ACCEPT);

                    assertFalse(block.post(null, Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void nullTimeout_throwsNullValueException() {
                NamedExecutorService executor = newExecutor();

                try {
                    BatchBlock<String> block = BatchBlock.<String>builder()
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
                    BatchBlock<String> block = BatchBlock.<String>builder()
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
            void validItem_returnsTrueAndDeliveredInBatch() throws Exception {
                NamedExecutorService executor = newExecutor();
                CountDownLatch delivered = new CountDownLatch(1);
                AtomicReference<List<String>> received = new AtomicReference<>();

                try {
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .batchSize(1)
                            .executor(executor)
                            .build();

                    block.linkTo(batch -> {
                        received.set(batch);
                        delivered.countDown();
                        return true;
                    });

                    assertTrue(block.post("hello", Duration.ofSeconds(5)));
                    assertTrue(delivered.await(5, TimeUnit.SECONDS));
                    assertNotNull(received.get());
                    assertEquals(1, received.get().size());
                    assertEquals("hello", received.get().get(0));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() {
                NamedExecutorService executor = newExecutor();

                try {
                    BatchBlock<String> block = BatchBlock.<String>builder()
                            .executor(executor)
                            .build();

                    block.complete();

                    assertFalse(block.post("hello", Duration.ofSeconds(5)));
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
        void withCriteria_deliversOnlyMatchingBatches() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger matchCount = new AtomicInteger(0);

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(1)
                        .executor(executor)
                        .build();

                BranchBlock<List<String>> branch = BranchBlock.<List<String>>builder()
                        .when(
                                batch -> batch.get(0).startsWith("y"),
                                batch -> {
                                    matchCount.incrementAndGet();
                                    return true;
                                }
                        )
                        .otherwise(batch -> true)
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
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(1)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> true);

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> block.linkTo(batch -> true)
                );

                assertEquals(LINK_TO_CALLED_TWICE_MSG, ex.getMessage());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Batching semantics
    // -------------------------------------------------------------------------

    @Nested
    class Batching {
        @Test
        void batchSizeReached_flushesFullBatch() throws Exception {
            NamedExecutorService executor = newExecutor();
            int batchSize = 3;
            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<List<String>> received = new AtomicReference<>();

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(batchSize)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.set(batch);
                    delivered.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertNotNull(received.get());
                assertEquals(batchSize, received.get().size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void timeoutExpires_flushesPartialBatch() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<List<String>> received = new AtomicReference<>();

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(10)
                        .timeout(Duration.ofMillis(200))
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.set(batch);
                    delivered.countDown();
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertNotNull(received.get());
                assertEquals(3, received.get().size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void complete_flushesPartialBatchImmediately() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredCount = new AtomicInteger(0);
            AtomicReference<List<String>> received = new AtomicReference<>();

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(10)
                        .timeout(Duration.ofSeconds(30))
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    received.set(batch);
                    deliveredCount.addAndGet(batch.size());
                    return true;
                });

                block.post("a");
                block.post("b");
                block.post("c");

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertNotNull(received.get());
                assertEquals(3, received.get().size());
                assertEquals(3, deliveredCount.get());
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
                DefaultBatchBlock<String> block = (DefaultBatchBlock<String>) BatchBlock.<String>builder()
                        .batchSize(1)
                        .executor(executor)
                        .build();

                // Post a probe item with batchSize(1) and await delivery — once the batch arrives
                // the worker thread is guaranteed to be running, making the assertion deterministic.
                CountDownLatch delivered = new CountDownLatch(1);

                block.linkTo(batch -> {
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
                DefaultBatchBlock<String> block = (DefaultBatchBlock<String>) BatchBlock.<String>builder()
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
        void complete_isIdempotent() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(1)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    deliveredCount.addAndGet(batch.size());
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
        void completion_resolvesAfterAllItemsDelivered() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger deliveredCount = new AtomicInteger(0);
            int itemCount = 5;

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(itemCount)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    deliveredCount.addAndGet(batch.size());
                    return true;
                });

                for (int i = 0; i < itemCount; i++) {
                    block.post("item-" + i);
                }

                block.complete();
                block.completion().get(5, TimeUnit.SECONDS);

                assertEquals(itemCount, deliveredCount.get());
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
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(1)
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
        void itemDeliveredHandler_isCalledOnBatchDelivery() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<List<String>> deliveredBatch = new AtomicReference<>();

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(1)
                        .executor(executor)
                        .itemDeliveredHandler((source, target, batch) -> {
                            deliveredBatch.set(batch);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(ACCEPT);
                block.post("hello");

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertNotNull(deliveredBatch.get());
                assertEquals(1, deliveredBatch.get().size());
                assertEquals("hello", deliveredBatch.get().get(0));
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
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(1)
                        .executor(executor)
                        .errorOccurredHandler((source, target, item, error) -> {
                            capturedError.set(error);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(batch -> {
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
                BatchBlock<Integer> block = BatchBlock.<Integer>builder()
                        .capacity(64)
                        .batchSize(5)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    for (int ignored : batch) {
                        deliveredCount.incrementAndGet();
                        allDelivered.countDown();
                    }

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

            AtomicInteger batchesReceived = new AtomicInteger();

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .batchSize(10)
                        .executor(executor)
                        .build();

                block.linkTo(batch -> {
                    batchesReceived.addAndGet(batch.size());
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
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .executor(executor)
                        .build();

                assertEquals(0, block.inFlight());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void inFlight_itemsInBatch_equalsSizeWhenDownstreamInFlightIsZero() throws Exception {
            NamedExecutorService executor = newExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);

            try {
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .capacity(10)
                        .batchSize(10)
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

                // Batch is being delivered; downstream inFlight() is default 0.
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
                BatchBlock<String> block = BatchBlock.<String>builder()
                        .capacity(10)
                        .batchSize(10)
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

