package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.BlankValueException;
import software.frisby.core.validation.NullValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NamedExecutorService} and its builder.  Covers constructor validation,
 * default thread prefix, thread naming, lifecycle methods, and the {@link NamedExecutorService#poolSize()}
 * and {@link NamedExecutorService#activeCount()} metrics.
 */
class NamedExecutorServiceTest {

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullThreadPrefix_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> NamedExecutorService.builder().threadPrefix(null).build()
            );
        }

        @Test
        void emptyThreadPrefix_throwsBlankValueException() {
            assertThrows(
                    BlankValueException.class,
                    () -> NamedExecutorService.builder().threadPrefix("").build()
            );
        }

        @Test
        void blankThreadPrefix_throwsBlankValueException() {
            assertThrows(
                    BlankValueException.class,
                    () -> NamedExecutorService.builder().threadPrefix("   ").build()
            );
        }

        @Test
        void defaultThreadPrefix_usesDataFlowPrefix() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder().build();

            AtomicReference<String> threadName = new AtomicReference<>();
            CountDownLatch started = new CountDownLatch(1);

            try {
                executor.execute(() -> {
                    threadName.set(Thread.currentThread().getName());
                    started.countDown();
                });

                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertTrue(threadName.get().startsWith("Pipeline-"));
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Thread naming
    // -------------------------------------------------------------------------

    @Nested
    class ThreadNaming {
        @Test
        void execute_threadNameMatchesPrefixAndCounter() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            AtomicReference<String> threadName = new AtomicReference<>();
            CountDownLatch started = new CountDownLatch(1);

            try {
                executor.execute(() -> {
                    threadName.set(Thread.currentThread().getName());
                    started.countDown();
                });

                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertTrue(threadName.get().matches("TestPrefix-\\d+"));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void execute_eachThreadReceivesAUniqueName() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            int taskCount = 3;
            List<AtomicReference<String>> names = new ArrayList<>();
            CountDownLatch started = new CountDownLatch(taskCount);
            CountDownLatch release = new CountDownLatch(1);

            try {
                for (int i = 0; i < taskCount; i++) {
                    AtomicReference<String> ref = new AtomicReference<>();
                    names.add(ref);

                    executor.execute(() -> {
                        ref.set(Thread.currentThread().getName());
                        started.countDown();

                        try {
                            release.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }

                assertTrue(started.await(5, TimeUnit.SECONDS));

                long uniqueNames = names.stream().map(AtomicReference::get).distinct().count();

                assertEquals(taskCount, uniqueNames);
                names.forEach(ref -> assertTrue(ref.get().matches("TestPrefix-\\d+")));
            } finally {
                release.countDown();
                executor.shutdown();
            }
        }

        @Test
        void twoInstancesWithSamePrefix_eachStartsCounterAtOne() throws Exception {
            NamedExecutorService executor1 = NamedExecutorService.builder()
                    .threadPrefix("SharedPrefix")
                    .build();

            NamedExecutorService executor2 = NamedExecutorService.builder()
                    .threadPrefix("SharedPrefix")
                    .build();

            AtomicReference<String> name1 = new AtomicReference<>();
            AtomicReference<String> name2 = new AtomicReference<>();

            CountDownLatch started = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);

            try {
                executor1.execute(() -> {
                    name1.set(Thread.currentThread().getName());
                    started.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                executor2.execute(() -> {
                    name2.set(Thread.currentThread().getName());
                    started.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                assertTrue(started.await(5, TimeUnit.SECONDS));

                // Per-instance counter: each executor starts at 1, so both first threads
                // are named "SharedPrefix-1".  Names are unique within a single executor
                // instance but may collide across separate instances with the same prefix.
                assertEquals("SharedPrefix-1", name1.get());
                assertEquals("SharedPrefix-1", name2.get());
            } finally {
                release.countDown();
                executor1.shutdown();
                executor2.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Nested
    class Lifecycle {
        @Test
        void isShutdown_returnsFalseBeforeShutdown() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                assertFalse(executor.isShutdown());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void isShutdown_returnsTrueAfterShutdown() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            executor.shutdown();

            assertTrue(executor.isShutdown());
        }

        @Test
        void isTerminated_returnsFalseBeforeShutdown() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                assertFalse(executor.isTerminated());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void isTerminated_returnsTrueAfterDrain() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            executor.shutdown();

            assertTrue(executor.isTerminated());
        }

        @Test
        void awaitTermination_returnsTrueWhenTerminatedWithinTimeout() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            executor.shutdown();

            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }

        @Test
        void awaitTermination_returnsFalseWhenTimeoutExceeded() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            AtomicBoolean stop = new AtomicBoolean(false);

            executor.execute(() -> {
                // Absorb any interrupt from shutdown() and keep running until the
                // external flag is set.  This ensures the task outlives the short
                // awaitTermination timeout so the assertion is deterministic.
                while (!stop.get()) {
                    Thread.interrupted();
                }
            });

            executor.shutdown();

            try {
                assertFalse(executor.awaitTermination(50, TimeUnit.MILLISECONDS));
            } finally {
                stop.set(true);
            }
        }

        @Test
        void shutdownNow_returnsEmptyListWhenNoQueuedTasks() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            List<Runnable> pending = executor.shutdownNow();

            assertTrue(pending.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // Metrics
    // -------------------------------------------------------------------------

    @Nested
    class Metrics {
        @Test
        void poolSize_returnsZeroInitially() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                assertEquals(0, executor.poolSize());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void poolSize_reflectsActiveThreads() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            CountDownLatch started = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);

            try {
                executor.execute(() -> {
                    started.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                executor.execute(() -> {
                    started.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertEquals(2, executor.poolSize());
            } finally {
                release.countDown();
                executor.shutdown();
            }
        }

        @Test
        void activeCount_returnsZeroWhenIdle() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                CountDownLatch done = new CountDownLatch(1);

                executor.execute(done::countDown);

                assertTrue(done.await(5, TimeUnit.SECONDS));

                // Allow the thread pool a moment to decrement its active count after the task returns.
                Thread.sleep(50);

                assertEquals(0, executor.activeCount());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void activeCount_reflectsRunningTasks() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            CountDownLatch started = new CountDownLatch(2);
            CountDownLatch release = new CountDownLatch(1);

            try {
                executor.execute(() -> {
                    started.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                executor.execute(() -> {
                    started.countDown();

                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                assertTrue(started.await(5, TimeUnit.SECONDS));
                assertEquals(2, executor.activeCount());
            } finally {
                release.countDown();
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Submission
    // -------------------------------------------------------------------------

    @Nested
    class Submission {
        @Test
        void submitCallable_returnsResult() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                Future<String> future = executor.submit(() -> "result");

                assertEquals("result", future.get(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void submitRunnable_completesSuccessfully() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            CountDownLatch done = new CountDownLatch(1);

            try {
                Future<?> future = executor.submit(done::countDown);

                future.get(5, TimeUnit.SECONDS);
                assertTrue(done.await(0, TimeUnit.MILLISECONDS));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void submitRunnableWithResult_returnsProvidedResult() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                Future<String> future = executor.submit(() -> {
                }, "result");

                assertEquals("result", future.get(5, TimeUnit.SECONDS));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void invokeAll_returnsOneCompletedFuturePerTask() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);

                List<Future<Integer>> futures = executor.invokeAll(tasks);

                assertEquals(3, futures.size());
                futures.forEach(f -> assertTrue(f.isDone()));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void invokeAll_withTimeout_returnsOneCompletedFuturePerTask() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2);

                List<Future<Integer>> futures = executor.invokeAll(tasks, 5, TimeUnit.SECONDS);

                assertEquals(2, futures.size());
                futures.forEach(f -> assertTrue(f.isDone()));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void invokeAny_returnsResultFromOneTask() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2);

                int result = executor.invokeAny(tasks);

                assertTrue(result == 1 || result == 2);
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void invokeAny_withTimeout_returnsResultFromOneTask() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TestPrefix")
                    .build();

            try {
                List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2);

                int result = executor.invokeAny(tasks, 5, TimeUnit.SECONDS);

                assertTrue(result == 1 || result == 2);
            } finally {
                executor.shutdown();
            }
        }
    }
}

