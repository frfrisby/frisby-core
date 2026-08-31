package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.Target;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PipelineBuilderTest {
    @Nested
    class FromPipelineStage {
        @Test
        void singleStage_postsAndConsumes() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                List<String> received = new ArrayList<>();

                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class))
                        .to(received::add);

                pipeline.post("a");
                pipeline.post("b");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(List.of("a", "b"), received);
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void multipleStages_transformsPropagated() {
            List<Integer> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class, Integer.class)
                            .transform(String::length))
                    .to(received::add);

            pipeline.post("hi");
            pipeline.post("hello");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of(2, 5), received);
        }
    }

    @Nested
    class FromPipelineTarget {
        @Test
        void singleActionStage_postsAndConsumes() {
            List<String> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Action.<String>of().action(received::add));

            pipeline.post("only");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("only"), received);
        }

        @Test
        void postAfterComplete_returnsFalse() {
            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Action.<String>of().action(item -> {
                    }));

            pipeline.complete();

            boolean accepted = pipeline.post("late");

            assertFalse(accepted);
        }

        @Test
        void awaitCompletion_returnsAfterDrain() {
            List<String> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Action.<String>of().action(received::add));

            for (int i = 0; i < 100; i++) {
                pipeline.post("item-" + i);
            }

            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(100, received.size());
        }
    }

    @Nested
    class ExecutorConfiguration {
        @Test
        void noExecutorProvided_throwsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> Pipeline.<String>builder()
                            .from(Buffer.of(String.class))
                            .to(s -> {
                            })
            );

            assertEquals("The 'executor' value is invalid. The value must not be null.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // post(T, Duration) — Pipeline.post(item, timeout) delegates to the head stage
    // -------------------------------------------------------------------------

    @Nested
    class PostWithTimeout {
        @Test
        void headIsAsyncBuffer_acceptsWithinTimeout_deliversToTarget() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                List<String> received = new ArrayList<>();

                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class))
                        .to(received::add);

                assertTrue(pipeline.post("a", Duration.ofSeconds(5)));

                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(List.of("a"), received);
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void headIsAsyncBufferFull_timeoutExpires_returnsFalse() throws Exception {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();
            CountDownLatch targetStarted = new CountDownLatch(1);
            CountDownLatch targetRelease = new CountDownLatch(1);

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class).capacity(1))
                        .to(item -> {
                            targetStarted.countDown();

                            try {
                                targetRelease.await();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        });

                // Fills the buffer's single permit; the worker immediately dequeues it and
                // blocks inside the target above, holding that one permit for the duration.
                assertTrue(pipeline.post("a", Duration.ofSeconds(5)));
                assertTrue(targetStarted.await(5, TimeUnit.SECONDS));

                // No permits available — must return false once the short timeout elapses.
                assertFalse(pipeline.post("b", Duration.ofMillis(50)));
            } finally {
                targetRelease.countDown();
                testExecutor.shutdown();
            }
        }

        @Test
        void headIsSyncActionOnly_neverBlocks_acceptsRegardlessOfTimeoutValue() {
            AtomicReference<String> received = new AtomicReference<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Action.<String>of().action(received::set));

            // A purely synchronous, single-stage pipeline has nothing to wait for — even
            // Duration.ZERO must succeed immediately.
            assertTrue(pipeline.post("only", Duration.ZERO));
            assertEquals("only", received.get());
        }

        @Test
        void headHandWiredCustomTargetWithNoOverride_throwsUnsupportedOperationException() {
            Target<String> customHead = item -> true;
            Pipeline<String> pipeline = new DefaultPipeline<>(customHead);

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> pipeline.post("hello", Duration.ofSeconds(1))
            );
        }
    }
}

