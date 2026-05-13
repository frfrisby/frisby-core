package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.mocks.MockInterruptedQueue;

import java.time.Duration;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AsyncBuffer} exercising the paths that are not reachable through
 * {@link BufferBlock} because {@link DefaultBufferBlock} guards with its own {@code completed}
 * flag before delegating.  All tests instantiate {@link AsyncBuffer} directly.
 */
class AsyncBufferTest {
    private static final String PREFIX = "TestAsyncBuffer";

    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix(PREFIX)
                .build();
    }

    // -------------------------------------------------------------------------
    // post(T) — paths guarded by DefaultBufferBlock in production
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Nested
        class WithoutTimeout {
            @Test
            void afterComplete_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBuffer<String> buffer = new AsyncBuffer<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            item -> {
                            },
                            executor
                    );

                    buffer.complete().get(5, TimeUnit.SECONDS);

                    assertFalse(buffer.post("hello"));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void queuePutThrowsInterruptedException_releasesPermitAndReturnsFalse() {
                // MockInterruptedQueue.put() records the invocation and throws InterruptedException
                // immediately.  CompletableQueue.enqueue() catches the IE, sets the interrupt flag,
                // and returns false.  AsyncBuffer.post() sees enqueued == false, releases the
                // capacity gate permit, and returns false.  The interrupt flag is left set.
                MockInterruptedQueue<String> queue = new MockInterruptedQueue<>();
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBuffer<String> buffer = new AsyncBuffer<>(queue, 1024, item -> {
                    }, executor);

                    boolean result = buffer.post("hello");

                    assertFalse(result);
                    assertEquals(1, queue.putInvokes());

                    // Outer catch restores the interrupt flag on the calling thread.
                    assertTrue(Thread.currentThread().isInterrupted());
                } finally {
                    Thread.interrupted(); // clear for the test thread before any latch operations
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
                    AsyncBuffer<String> buffer = new AsyncBuffer<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            item -> {
                            },
                            executor
                    );

                    assertFalse(buffer.post(null, Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void afterComplete_returnsFalse() throws Exception {
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBuffer<String> buffer = new AsyncBuffer<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            item -> {
                            },
                            executor
                    );

                    buffer.complete().get(5, TimeUnit.SECONDS);

                    assertFalse(buffer.post("hello", Duration.ofSeconds(5)));
                } finally {
                    executor.shutdown();
                }
            }

            @Test
            void queuePutThrowsInterruptedException_releasesPermitAndReturnsFalse() {
                // Same path as the without-timeout overload: the gate is acquired via tryAcquire(),
                // then CompletableQueue.enqueue() calls put() which throws IE, the IE is caught
                // inside enqueue(), the interrupt flag is restored, and post() releases the permit
                // before returning false.
                MockInterruptedQueue<String> queue = new MockInterruptedQueue<>();
                NamedExecutorService executor = newExecutor();

                try {
                    AsyncBuffer<String> buffer = new AsyncBuffer<>(queue, 1024, item -> {
                    }, executor);

                    boolean result = buffer.post("hello", Duration.ofSeconds(5));

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
                    AsyncBuffer<String> buffer = new AsyncBuffer<>(
                            new ArrayBlockingQueue<>(10),
                            10,
                            item -> {
                            },
                            executor
                    );

                    Thread.currentThread().interrupt();

                    boolean result = buffer.post("hello", Duration.ofMillis(10));

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
                AsyncBuffer<String> buffer = new AsyncBuffer<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        item -> {
                        },
                        executor
                );

                CompletableFuture<Void> first = buffer.complete();
                CompletableFuture<Void> second = buffer.complete();

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
                AsyncBuffer<String> buffer = new AsyncBuffer<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        item -> {
                        },
                        executor
                );

                assertNotNull(buffer.completion());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void completion_resolvesAfterComplete() throws Exception {
            NamedExecutorService executor = newExecutor();

            try {
                AsyncBuffer<String> buffer = new AsyncBuffer<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        item -> {
                        },
                        executor
                );

                buffer.complete();

                // completion() must return the same future that resolves after drain.
                buffer.completion().get(5, TimeUnit.SECONDS);
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Worker — drain-before-start path
    // -------------------------------------------------------------------------

    @Nested
    class Worker {
        @Test
        void drainCalledWhileWorkerRunning_interruptsWorkerAndCompletesCleanly() throws Exception {
            // Spin until the worker has set isRunning = true via lifecycle.start().
            // complete() then calls completableQueue.complete(), which signals the notEmpty
            // condition, waking the worker from dequeue().  The worker sees dequeue() return
            // null (completed and empty), exits the loop, and calls lifecycle.finish().
            NamedExecutorService executor = newExecutor();

            try {
                AsyncBuffer<String> buffer = new AsyncBuffer<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        item -> {
                        },
                        executor
                );

                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

                while (!buffer.isRunning() && System.nanoTime() < deadline) {
                    Thread.onSpinWait();
                }

                assertTrue(buffer.isRunning(), "worker did not start within timeout");

                buffer.complete();
                buffer.completion().get(5, TimeUnit.SECONDS);

                assertFalse(buffer.isRunning());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void drainCalledBeforeWorkerThreadStarts_workerExitsCleanly() throws Exception {
            // A deferred executor holds the worker Runnable until after complete() is called.
            // complete() calls completableQueue.complete() while the worker has not yet started,
            // marking the queue as completed.  When the worker eventually starts, dequeue() sees
            // completed == true && queue.isEmpty(), returns null immediately, and the worker exits
            // cleanly via lifecycle.finish().
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

                AsyncBuffer<String> buffer = new AsyncBuffer<>(
                        new ArrayBlockingQueue<>(10),
                        10,
                        item -> {
                        },
                        deferredExecutor
                );

                // complete() marks the queue as completed before the worker starts.
                // When the worker starts it sees dequeue() return null immediately.
                buffer.complete();

                // Release the worker.
                workerStart.countDown();

                buffer.completion().get(5, TimeUnit.SECONDS);
            } finally {
                executor.shutdown();
            }
        }
    }
}

