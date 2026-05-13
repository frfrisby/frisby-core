package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;
import software.frisby.core.validation.NullValueException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SourceBlock} and its builder.  Covers builder validation, single-item
 * mode, batch mode, the worker lifecycle ({@code isRunning}), and delegate handler callbacks.
 *
 * <p>A {@link LinkedBlockingQueue} is used as the supplier source in most tests so that
 * {@code queue::take} blocks the worker when no items are available — exactly the canonical
 * production usage.</p>
 */
class SourceBlockTest {
    private static final String PREFIX = "TestSource";

    private static final String NO_SUPPLIER_MSG =
            "A supplier must be configured.  Call supplier(Supplier<T>) for single-item mode or supplier(Supplier<List<T>>) for batch mode.";
    private static final String SUPPLIER_ALREADY_CONFIGURED_MSG =
            "The 'SourceBlock' block already has a supplier configured.  Call only one of supplier(...) or batchSupplier(...).";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'SourceBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";

    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix(PREFIX)
                .build();
    }

    /**
     * Wraps {@link BlockingQueue#take()} in a {@link java.util.function.Supplier} that handles the checked exception.
     */
    private static <T> java.util.function.Supplier<T> taking(BlockingQueue<T> queue) {
        return () -> {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        };
    }

    /**
     * Wraps a batch {@link BlockingQueue#take()} in a {@link java.util.function.Supplier} returning {@code List<T>}.
     */
    private static <T> java.util.function.Supplier<List<T>> takingBatch(BlockingQueue<List<T>> queue) {
        return () -> {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return List.of();
            }
        };
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void noSupplierSet_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> SourceBlock.<String>builder().build()
            );

            assertEquals(NO_SUPPLIER_MSG, ex.getMessage());
        }

        @Test
        void nullExecutor_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> SourceBlock.<String>builder()
                            .supplier(() -> "hello")
                            .build()
            );
        }

        @Test
        void supplierThenBatchSupplier_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> SourceBlock.<String>builder()
                            .supplier(() -> "single-item")
                            .batchSupplier(() -> List.of("batch-item"))
            );

            assertEquals(SUPPLIER_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }

        @Test
        void batchSupplierThenSupplier_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> SourceBlock.<String>builder()
                            .batchSupplier(() -> List.of("batch-item"))
                            .supplier(() -> "single-item")
            );

            assertEquals(SUPPLIER_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Single-item mode
    // -------------------------------------------------------------------------

    @Nested
    class SingleItemMode {
        @Test
        void items_areDeliveredToLinkedTarget() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();
            source.add("hello");
            source.add("world");

            CountDownLatch delivered = new CountDownLatch(2);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(2, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void workerBlocksUntilTargetIsLinked() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();
            source.add("hello");

            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .build();

                // Small delay before linking — verifies the worker waits.
                Thread.sleep(50);

                block.linkTo(item -> {
                    received.set(item);
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals("hello", received.get());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Batch mode
    // -------------------------------------------------------------------------

    @Nested
    class BatchMode {
        @Test
        void batchItems_areDeliveredIndividuallyToLinkedTarget() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<List<String>> source = new LinkedBlockingQueue<>();
            source.add(List.of("a", "b", "c"));

            CountDownLatch delivered = new CountDownLatch(3);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .batchSupplier(takingBatch(source))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(3, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void emptyBatch_isNoOpAndWorkerLoopsBack() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<List<String>> source = new LinkedBlockingQueue<>();
            source.add(List.of());          // empty — no-op
            source.add(List.of("item"));    // then a real item

            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .batchSupplier(takingBatch(source))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    received.set(item);
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals("item", received.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void nullBatch_isNoOpAndWorkerLoopsBack() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger callCount = new AtomicInteger(0);
            BlockingQueue<String> realSource = new LinkedBlockingQueue<>();
            realSource.add("item");

            CountDownLatch delivered = new CountDownLatch(1);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .batchSupplier(() -> {
                            if (callCount.getAndIncrement() == 0) {
                                return null;  // first call returns null — must be a no-op
                            }

                            try {
                                return List.of(realSource.take());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return List.of();
                            }
                        })
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle (isRunning)
    // -------------------------------------------------------------------------

    @Nested
    class Lifecycle {
        @Test
        void isRunning_returnsTrueWhileWorkerIsActive() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();
            source.add("probe");

            CountDownLatch delivered = new CountDownLatch(1);

            try {
                DefaultSourceBlock<String> block = (DefaultSourceBlock<String>) SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .build();

                block.linkTo(item -> {
                    delivered.countDown();
                    return true;
                });

                // Once the item is delivered the worker is guaranteed to be running.
                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertTrue(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void isRunning_returnsFalseAfterExecutorShutdown() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();

            try {
                DefaultSourceBlock<String> block = (DefaultSourceBlock<String>) SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .build();

                // Link target so the worker starts running (exits the start latch).
                block.linkTo(item -> true);

                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);

                assertFalse(block.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void executorShutdownWhileAwaitingFirstTarget_interruptsCatchBlock() throws Exception {
            // When the executor shuts down before linkTo() is called, the worker thread is
            // interrupted while blocking in startLatch.await().  The InterruptedException is
            // caught, isRunning is set to false, and run() returns early.
            //
            // Spinning until isRunning() == true guarantees the worker has reached
            // startLatch.await() before the interrupt is sent.  Because no linkTo() is
            // called, the latch count stays at 1 and the interrupt is the only way out.
            NamedExecutorService executor = newExecutor();

            try {
                DefaultSourceBlock<String> block = (DefaultSourceBlock<String>) SourceBlock.<String>builder()
                        .supplier(() -> "hello")
                        .executor(executor)
                        .build();

                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                while (!block.isRunning() && System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }

                assertTrue(block.isRunning(), "worker did not start within timeout");

                // No linkTo() — latch count is still 1.  shutdown() calls shutdownNow()
                // internally, which sends an interrupt.  The interrupt fires inside
                // startLatch.await(), triggering the catch block in run().
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);

                assertFalse(block.isRunning());
            } finally {
                executor.shutdown();
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
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(() -> "hello")
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
    }

    // -------------------------------------------------------------------------
    // Concurrency policy
    // -------------------------------------------------------------------------

    @Nested
    class ConcurrencyPolicy {
        @Test
        void nullConcurrencyPolicy_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> SourceBlock.<String>builder()
                            .supplier(() -> "hello")
                            .concurrencyPolicy(null)
            );
        }

        @Test
        void fixedPolicy_multipleThreads_deliversAllItems() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();
            for (int i = 0; i < 6; i++) source.add("item-" + i);

            CountDownLatch delivered = new CountDownLatch(6);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .concurrencyPolicy(SourceConcurrencyPolicy.fixed(3))
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(6, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_deliversItems() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();
            source.add("hello");
            source.add("world");

            CountDownLatch delivered = new CountDownLatch(2);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .concurrencyPolicy(SourceConcurrencyPolicy.adaptive(2))
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(2, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_supplierMissAtFloor_workerContinues() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger callCount = new AtomicInteger(0);
            BlockingQueue<String> realSource = new LinkedBlockingQueue<>();
            realSource.add("item");

            CountDownLatch delivered = new CountDownLatch(1);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(() -> {
                            if (callCount.getAndIncrement() == 0) {
                                return null;  // miss at the floor — semaphore must still be released
                            }

                            try {
                                return realSource.take();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }
                        })
                        .executor(executor)
                        .concurrencyPolicy(SourceConcurrencyPolicy.adaptive(2))
                        .build();

                block.linkTo(item -> {
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_scalesUpAfterConsecutiveSuccesses() throws Exception {
            // With adaptive(2).scaleUpThreshold(1): thread 1 runs call 0 (1 consecutive
            // success), triggering scale-up to maxPermits=2.  The gate then grants a permit to
            // thread 2.  Both threads block inside the supplier, confirming via CountDownLatch
            // that 2 threads are simultaneously active.
            //
            // Critically, calls after scale-up exercise the at-ceiling branch of release(true):
            // currentMaxPermits == maxPermits (2 == 2), so the gate bypasses all counter
            // bookkeeping and releases 1 permit normally — confirming that consecutiveSuccesses
            // is never incremented once the ceiling is reached.
            NamedExecutorService executor = newExecutor();
            int threshold = 1;
            AtomicInteger callCount = new AtomicInteger(0);
            CountDownLatch twoConcurrent = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);
            CountDownLatch twoDelivered = new CountDownLatch(2);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(() -> {
                            int call = callCount.getAndIncrement();

                            if (call < threshold) {
                                return "item-" + call;  // drives scale-up
                            }

                            // After scale-up both threads are active.  Both reach here, count
                            // down the latch, and park until the test releases them.
                            twoConcurrent.countDown();

                            try {
                                release.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }

                            return "concurrent-" + call;
                        })
                        .executor(executor)
                        .concurrencyPolicy(
                                SourceConcurrencyPolicy.adaptive(2)
                                        .scaleUpThreshold(threshold)
                        )
                        .build();

                block.linkTo(item -> {
                    if (item.startsWith("concurrent-")) {
                        twoDelivered.countDown();
                    }
                    return true;
                });

                assertTrue(twoConcurrent.await(5, TimeUnit.SECONDS),
                        "Expected 2 concurrent supplier calls after scale-up");

                release.countDown();

                assertTrue(twoDelivered.await(5, TimeUnit.SECONDS));
            } finally {
                release.countDown();
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_scalesDownAfterMissAboveFloor() throws Exception {
            // Uses the same scale-up setup as the previous test.  Once 2 threads are active and
            // blocked in the supplier, releasing them causes the thread that drew call==threshold
            // to return null — hitting the scale-down path where currentMaxPermits > minPermits
            // and the permit is retired.  The other thread returns "final", proving delivery
            // continues after scale-down.
            NamedExecutorService executor = newExecutor();
            int threshold = 2;
            AtomicInteger callCount = new AtomicInteger(0);
            CountDownLatch twoConcurrent = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);
            CountDownLatch finalDelivered = new CountDownLatch(1);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(() -> {
                            int call = callCount.getAndIncrement();

                            if (call < threshold) {
                                return "item-" + call;  // scale-up triggers after these successes
                            }

                            // 2 threads are now active — both park here.
                            twoConcurrent.countDown();

                            try {
                                release.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }

                            // The thread that drew call == threshold returns null, exercising
                            // the scale-down branch (currentMaxPermits > minPermits → retire permit).
                            // The other thread returns "final" to confirm delivery still works.
                            return call == threshold ? null : "final";
                        })
                        .executor(executor)
                        .concurrencyPolicy(
                                SourceConcurrencyPolicy.adaptive(4)
                                        .scaleUpThreshold(threshold)
                        )
                        .build();

                block.linkTo(item -> {
                    if ("final".equals(item)) {
                        finalDelivered.countDown();
                    }
                    return true;
                });

                assertTrue(twoConcurrent.await(5, TimeUnit.SECONDS),
                        "Expected 2 concurrent supplier calls after scale-up");

                release.countDown();

                assertTrue(finalDelivered.await(5, TimeUnit.SECONDS));
            } finally {
                release.countDown();
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_supplierThrowsException_logsErrorViaEventSource() {
            // Covers AdaptiveWorker.run() line 206 — the catch(Exception ex) block that
            // forwards a supplier exception to EventSource.createErrorEvent().  The
            // FixedWorker path is tested by Delegates.supplierThrowsException_logsErrorViaEventSource;
            // this test exercises the equivalent path in AdaptiveWorker by enabling an
            // adaptive concurrency policy, which causes DefaultSourceBlock to use an
            // AdaptiveWorker instead of a FixedWorker.
            NamedExecutorService executor = newExecutor();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("An unexpected exception occurred in the SourceBlock"))
                            .build()
                    )
                    .build()) {
                try {
                    SourceBlock<String> block = SourceBlock.<String>builder()
                            .supplier(() -> {
                                throw new RuntimeException("supplier error");
                            })
                            .executor(executor)
                            .concurrencyPolicy(SourceConcurrencyPolicy.adaptive(2))
                            .build();

                    block.linkTo(item -> true);

                    verifier.assertExpectations(Duration.ofSeconds(5));
                } finally {
                    executor.shutdown();
                }
            }
        }

        @Test
        void adaptivePolicy_batchSupplier_deliversItems() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<List<String>> source = new LinkedBlockingQueue<>();
            source.add(List.of("a", "b", "c"));

            CountDownLatch delivered = new CountDownLatch(3);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .batchSupplier(takingBatch(source))
                        .executor(executor)
                        .concurrencyPolicy(SourceConcurrencyPolicy.adaptive(2))
                        .build();

                block.linkTo(item -> {
                    deliveredCount.incrementAndGet();
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(3, deliveredCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_batchSupplier_nullBatch_isNoOp() throws Exception {
            NamedExecutorService executor = newExecutor();
            AtomicInteger callCount = new AtomicInteger(0);
            BlockingQueue<String> realSource = new LinkedBlockingQueue<>();
            realSource.add("item");

            CountDownLatch delivered = new CountDownLatch(1);

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .batchSupplier(() -> {
                            if (callCount.getAndIncrement() == 0) {
                                return null;  // null batch — miss at floor, semaphore released
                            }

                            try {
                                return List.of(realSource.take());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return List.of();
                            }
                        })
                        .executor(executor)
                        .concurrencyPolicy(SourceConcurrencyPolicy.adaptive(2))
                        .build();

                block.linkTo(item -> {
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void adaptivePolicy_batchSupplier_emptyBatch_isNoOp() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<List<String>> source = new LinkedBlockingQueue<>();
            source.add(List.of());          // empty — miss at floor, worker loops back
            source.add(List.of("item"));    // then a real item

            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<String> received = new AtomicReference<>();

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .batchSupplier(takingBatch(source))
                        .executor(executor)
                        .concurrencyPolicy(SourceConcurrencyPolicy.adaptive(2))
                        .build();

                block.linkTo(item -> {
                    received.set(item);
                    delivered.countDown();
                    return true;
                });

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals("item", received.get());
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
        void itemDeliveredHandler_isCalledOnDelivery() throws Exception {
            NamedExecutorService executor = newExecutor();
            BlockingQueue<String> source = new LinkedBlockingQueue<>();
            source.add("hello");

            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<String> deliveredItem = new AtomicReference<>();

            try {
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(taking(source))
                        .executor(executor)
                        .itemDeliveredHandler((src, target, item) -> {
                            deliveredItem.set(item);
                            notified.countDown();
                        })
                        .build();

                block.linkTo(item -> true);

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
                SourceBlock<String> block = SourceBlock.<String>builder()
                        .supplier(() -> "hello")
                        .executor(executor)
                        .errorOccurredHandler((source, target, item, error) -> {
                            if (capturedError.compareAndSet(null, error)) {
                                notified.countDown();
                            }
                        })
                        .build();

                block.linkTo(item -> {
                    throw new RuntimeException("target error");
                });

                assertTrue(notified.await(5, TimeUnit.SECONDS));
                assertNotNull(capturedError.get());
                assertEquals("target error", capturedError.get().getMessage());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void supplierThrowsException_logsErrorViaEventSource() {
            // When an exception propagates from the supplier through consumer.accept() and is not
            // caught by an error handler, the worker catches it and forwards it to
            // EventSource.createErrorEvent(), which logs at ERROR level.
            NamedExecutorService executor = newExecutor();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("An unexpected exception occurred in the SourceBlock"))
                            .build()
                    )
                    .build()) {
                try {
                    SourceBlock<String> block = SourceBlock.<String>builder()
                            .supplier(() -> {
                                throw new RuntimeException("supplier error");
                            })
                            .executor(executor)
                            .build();

                    block.linkTo(item -> true);

                    verifier.assertExpectations(Duration.ofSeconds(5));
                } finally {
                    executor.shutdown();
                }
            }
        }
    }
}












