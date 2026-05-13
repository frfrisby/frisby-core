package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.mocks.MockInterruptedQueue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AsyncBatch} exercising the paths that are not reachable through
 * {@link BatchBlock} because {@link DefaultBatchBlock} guards with its own {@code completed}
 * flag before delegating.  All tests instantiate {@link AsyncBatch} directly.
 */
class AsyncBatchTest {
    private static final String PREFIX = "TestAsyncBatch";

    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix(PREFIX)
                .build();
    }

    // -------------------------------------------------------------------------
    // post(T) — paths guarded by DefaultBatchBlock in production
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Nested
        class WithoutTimeout {
            @Test
            void afterComplete_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBatch<String> batch = new AsyncBatch<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            items -> {
                            },
                            1,
                            Duration.ofSeconds(5),
                            executor
                    );

                    batch.complete().get(5, TimeUnit.SECONDS);

                    assertFalse(batch.post("hello"));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void queuePutThrowsInterruptedException_releasesPermitAndReturnsFalse() {
                // MockInterruptedQueue.put() throws InterruptedException immediately.
                // CompletableQueue.enqueue() catches it, restores the interrupt flag on the
                // calling thread, and returns false.  post() sees the false result, releases
                // the capacity permit, and propagates the false return to the caller.
                MockInterruptedQueue<String> queue = new MockInterruptedQueue<>();
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBatch<String> batch = new AsyncBatch<>(
                            queue,
                            1024,
                            items -> {
                            },
                            1,
                            Duration.ofSeconds(5),
                            executor
                    );

                    boolean result = batch.post("hello");

                    assertFalse(result);
                    assertEquals(1, queue.putInvokes());

                    // Outer catch restores the interrupt flag on the calling thread.
                    assertTrue(Thread.currentThread().isInterrupted());
                } finally {
                    Thread.interrupted(); // clear for the test thread before any latch operations
                    executor.shutdown();
                }
            }

            @Test
            void capacityGateThrowsInterruptedException_returnsFalse() {
                // Semaphore.acquire() immediately throws InterruptedException when the calling
                // thread is already interrupted on entry.
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBatch<String> batch = new AsyncBatch<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            items -> {
                            },
                            1,
                            Duration.ofSeconds(5),
                            executor
                    );

                    Thread.currentThread().interrupt();

                    boolean result = batch.post("hello");

                    assertFalse(result);
                    assertTrue(Thread.currentThread().isInterrupted());
                } finally {
                    Thread.interrupted(); // clear for the test thread
                    executor.shutdown();
                }
            }
        }

        @Nested
        class WithTimeout {
            @Test
            void afterComplete_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBatch<String> batch = new AsyncBatch<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            items -> {
                            },
                            1,
                            Duration.ofSeconds(5),
                            executor
                    );

                    batch.complete().get(5, TimeUnit.SECONDS);

                    assertFalse(batch.post("hello", Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void queuePutThrowsInterruptedException_releasesPermitAndReturnsFalse() {
                // Same path as the without-timeout overload: MockInterruptedQueue.put() throws,
                // CompletableQueue.enqueue() restores the interrupt flag and returns false,
                // post() releases the permit and returns false to the caller.
                MockInterruptedQueue<String> queue = new MockInterruptedQueue<>();
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBatch<String> batch = new AsyncBatch<>(
                            queue,
                            1024,
                            items -> {
                            },
                            1,
                            Duration.ofSeconds(5),
                            executor
                    );

                    boolean result = batch.post("hello", Duration.ofSeconds(5));

                    assertFalse(result);
                    assertEquals(1, queue.putInvokes());

                    // Outer catch restores the interrupt flag on the calling thread.
                    assertTrue(Thread.currentThread().isInterrupted());
                } finally {
                    Thread.interrupted(); // clear for the test thread
                    executor.shutdown();
                }
            }

            @Test
            void capacityGateThrowsInterruptedException_returnsFalse() {
                // Semaphore.tryAcquire(timeout, unit) immediately throws InterruptedException
                // when the calling thread is already interrupted on entry.
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBatch<String> batch = new AsyncBatch<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            items -> {
                            },
                            1,
                            Duration.ofSeconds(5),
                            executor
                    );

                    Thread.currentThread().interrupt();

                    boolean result = batch.post("hello", Duration.ofMillis(10));

                    assertFalse(result);
                    assertTrue(Thread.currentThread().isInterrupted());
                } finally {
                    Thread.interrupted(); // clear for the test thread
                    executor.shutdown();
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // complete()
    // -------------------------------------------------------------------------

    @Nested
    class Complete {
        @Test
        void calledTwice_secondCallIsIdempotent() throws Exception {
            NamedExecutorService executor = newExecutor();

            try {
                AsyncBatch<String> batch = new AsyncBatch<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        items -> {
                        },
                        1,
                        Duration.ofSeconds(5),
                        executor
                );

                CompletableFuture<Void> first = batch.complete();
                CompletableFuture<Void> second = batch.complete();

                assertSame(first, second);

                first.get(5, TimeUnit.SECONDS);
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // completion()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void completion_returnsNonNullFuture() {
            NamedExecutorService executor = newExecutor();

            try {
                AsyncBatch<String> batch = new AsyncBatch<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        items -> {
                        },
                        1,
                        Duration.ofSeconds(5),
                        executor
                );

                assertNotNull(batch.completion());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void completion_resolvesAfterComplete() throws Exception {
            NamedExecutorService executor = newExecutor();

            try {
                AsyncBatch<String> batch = new AsyncBatch<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        items -> {
                        },
                        1,
                        Duration.ofSeconds(5),
                        executor
                );

                batch.complete();

                // completion() must return the same future that resolves after drain.
                batch.completion().get(5, TimeUnit.SECONDS);
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Worker — post-loop flush (drain-before-start path)
    // -------------------------------------------------------------------------

    @Nested
    class Worker {
        @Test
        void fullBatchFlushed_pollTimesOutWithEmptyList_resetsToTakeMode() throws Exception {
            // After a size-triggered full-batch flush, beginPolling remains true but list is empty.
            // The next poll() call times out, and batchReady = !list.isEmpty() evaluates to false —
            // the branch under test.  The worker then resets beginPolling to false and blocks in
            // take() until the next item arrives, confirming the worker is still operational.
            NamedExecutorService executor = newExecutor();
            CountDownLatch firstBatch = new CountDownLatch(1);
            CountDownLatch secondBatch = new CountDownLatch(1);

            try {
                AsyncBatch<String> batch = new AsyncBatch<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        items -> {
                            if (firstBatch.getCount() > 0) {
                                firstBatch.countDown();
                            } else {
                                secondBatch.countDown();
                            }
                        },
                        1,                       // batchSize = 1: every item is its own flush
                        Duration.ofMillis(50),   // short poll timeout
                        executor
                );

                // Post the first item — batchSize(1) triggers an immediate size-flush.
                // After the flush: list is empty, beginPolling is still true.
                batch.post("a");

                assertTrue(firstBatch.await(5, TimeUnit.SECONDS));

                // Sleep well past the poll timeout so the worker polls with an empty list,
                // evaluates batchReady = !list.isEmpty() = false, resets beginPolling, and
                // blocks in take() before the next post arrives.
                Thread.sleep(300);

                // Post a second item — the worker is now in take() mode and accepts it
                // immediately, then flushes it in its own single-item batch.
                batch.post("b");

                assertTrue(secondBatch.await(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void completeBeforeWorkerStarts_withQueuedItems_flushesPartialBatch() throws Exception {
            // complete() is called before the worker thread starts — the queue is marked
            // completed but no drain signal can be sent yet.  When the worker eventually
            // starts, dequeue() and dequeue(timeout) return items immediately (no blocking,
            // since completed=true causes the condition wait to be skipped).  After all
            // enqueued items are consumed the next dequeue(timeout) call returns null with
            // isCompleted() && isEmpty() == true, triggering the drain-exit branch which
            // flushes the partial batch (batchSize > items posted, so no size-triggered
            // flush ever fires).
            CountDownLatch workerStart = new CountDownLatch(1);
            CountDownLatch delivered = new CountDownLatch(1);
            AtomicReference<List<String>> received = new AtomicReference<>();

            NamedExecutorService executor = newExecutor();

            try {
                Executor delayedStart = task -> executor.execute(() -> {
                    try {
                        workerStart.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    task.run();
                });

                AsyncBatch<String> batch = new AsyncBatch<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        items -> {
                            received.set(items);
                            delivered.countDown();
                        },
                        5,  // batchSize > items posted — no size-triggered flush
                        Duration.ofSeconds(5),
                        delayedStart
                );

                batch.post("a");
                batch.post("b");
                batch.post("c");

                // complete() before the worker starts — queue marked completed immediately,
                // worker will dequeue items without blocking when it eventually starts.
                batch.complete();

                // Release the worker.  It consumes all three items, then dequeue(timeout)
                // returns null with isCompleted() + isEmpty() == true, triggering the
                // drain-exit flush of the partial list.
                workerStart.countDown();

                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals(List.of("a", "b", "c"), received.get());
            } finally {
                executor.shutdown();
            }
        }
    }
}

