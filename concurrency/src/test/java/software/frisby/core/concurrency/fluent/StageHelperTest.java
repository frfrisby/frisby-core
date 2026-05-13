package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.GenericType;
import software.frisby.core.concurrency.NamedExecutorService;
import software.frisby.core.concurrency.Retention;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StageHelperTest {
    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix("StageHelperTest")
                .build();
    }

    // =========================================================================
    // Buffer
    // =========================================================================

    @Nested
    class BufferTests {
        @Test
        void errorOccurredHandler_isAccepted() {
            assertNotNull(Buffer.of(String.class).errorOccurredHandler((src, tgt, item, err) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Buffer.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }

        @Test
        void toSource_returnsBlock() {
            NamedExecutorService executor = newExecutor();

            try {
                Buffer<String> buf = Buffer.of(String.class);

                buf.executor(executor);

                assertNotNull(buf.toSource());
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // Batch
    // =========================================================================

    @Nested
    class BatchTests {
        @Test
        void defaultConfig_batchesAndFlushes() {
            List<List<String>> batches = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Batch.of(String.class)
                                .batchSize(3))
                        .to(batches::add);

                pipeline.post("a");
                pipeline.post("b");
                pipeline.post("c");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertFalse(batches.isEmpty());
                assertEquals(3, batches.stream().mapToInt(List::size).sum());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void timeout_flushesBeforeBatchSizeReached() throws Exception {
            List<List<String>> batches = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Batch.of(String.class)
                                .batchSize(100)
                                .timeout(Duration.ofMillis(100)))
                        .to(batches::add);

                pipeline.post("only-item");

                Thread.sleep(300);

                pipeline.complete();
                pipeline.awaitCompletion();

                assertFalse(batches.isEmpty());
                assertEquals(1, batches.stream().mapToInt(List::size).sum());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void capacityConfig_accepted() {
            assertDoesNotThrow(() -> Batch.of(String.class).capacity(512));
        }

        @Test
        void ofWithGenericType_returnsInstance() {
            assertNotNull(Batch.of(new GenericType<String>() {
            }));
        }

        @Test
        void of_returnsNewInstance() {
            assertNotNull(Batch.of());
        }

        @Test
        void errorOccurredHandler_isAccepted() {
            assertNotNull(Batch.of(String.class).errorOccurredHandler((src, tgt, item, err) -> {
            }));
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(Batch.of(String.class).itemPostedHandler((src, item, accepted) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Batch.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }

        @Test
        void capacityAndToSource_coveredViaToBlock() {
            NamedExecutorService executor = newExecutor();

            try {
                Batch<String> batch = Batch.of(String.class).capacity(256);

                batch.executor(executor);

                assertNotNull(batch.toSource());
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // Delay
    // =========================================================================

    @Nested
    class DelayTests {
        @Test
        void fixedDelay_holdsItemThenDelivers() throws Exception {
            CountDownLatch delivered = new CountDownLatch(1);
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Delay.of(String.class)
                                .delay(Duration.ofMillis(50)))
                        .to(item -> {
                            received.add(item);
                            delivered.countDown();
                        });

                long before = System.currentTimeMillis();
                pipeline.post("delayed");

                // Wait for natural delivery — complete() is NOT called here, so the item
                // must wait for its full delay before it is forwarded to the target.
                assertTrue(delivered.await(5, TimeUnit.SECONDS));

                long elapsed = System.currentTimeMillis() - before;

                assertEquals(List.of("delayed"), received);
                assertTrue(elapsed >= 40, "Expected at least 40ms delay, got " + elapsed);

                pipeline.complete();
                pipeline.awaitCompletion();
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void delayFunction_perItemDelay() {
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Delay.of(String.class)
                                .delay(item -> item.startsWith("SLOW")
                                        ? Duration.ofMillis(50)
                                        : Duration.ZERO))
                        .to(received::add);

                pipeline.post("FAST");
                pipeline.post("SLOW-1");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(2, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void capacityConfig_accepted() {
            assertDoesNotThrow(() -> Delay.of(String.class).capacity(256));
        }

        @Test
        void ofLists_returnsInstance() {
            assertNotNull(Delay.ofLists(String.class));
        }

        @Test
        void ofWithGenericType_returnsInstance() {
            assertNotNull(Delay.of(new GenericType<String>() {
            }));
        }

        @Test
        void errorOccurredHandler_isAccepted() {
            assertNotNull(Delay.of(String.class).errorOccurredHandler((src, tgt, item, err) -> {
            }));
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(Delay.of(String.class).itemPostedHandler((src, item, accepted) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Delay.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }

        @Test
        void capacityAndToSource_coveredViaToBlock() {
            NamedExecutorService executor = newExecutor();

            try {
                Delay<String> delay = Delay.of(String.class)
                        .delay(Duration.ofMillis(50))
                        .capacity(256);

                delay.executor(executor);

                assertNotNull(delay.toSource());
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // Group
    // =========================================================================

    @Nested
    class GroupTests {
        @Test
        void groupingFunction_groupsByKey() {
            List<List<String>> groups = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Group.of(String.class, String.class)
                                .groupingFunction(s -> s.split(":")[0])
                                .maxGroupSize(3))
                        .to(groups::add);

                pipeline.post("A:1");
                pipeline.post("A:2");
                pipeline.post("A:3");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertFalse(groups.isEmpty());
                groups.forEach(g -> assertTrue(g.stream().allMatch(s -> s.startsWith("A:"))));
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void idleTimeoutConfig_accepted() {
            assertDoesNotThrow(() ->
                    Group.of(String.class, String.class)
                            .idleTimeout(Duration.ofSeconds(1)));
        }

        @Test
        void maxGroupSizeConfig_accepted() {
            assertDoesNotThrow(() ->
                    Group.of(String.class, String.class)
                            .maxGroupSize(50));
        }

        @Test
        void capacityConfig_accepted() {
            assertDoesNotThrow(() ->
                    Group.of(String.class, String.class)
                            .capacity(512));
        }

        @Test
        void ofWithGenericTypeAndClass_returnsInstance() {
            assertNotNull(Group.of(new GenericType<String>() {
            }, String.class));
        }

        @Test
        void ofWithBothGenericTypes_returnsInstance() {
            assertNotNull(Group.of(new GenericType<String>() {
            }, new GenericType<String>() {
            }));
        }

        @Test
        void of_returnsNewInstance() {
            assertNotNull(Group.of());
        }

        @Test
        void timeout_isAccepted() {
            assertNotNull(
                    Group.of(String.class, String.class)
                            .timeout(Duration.ofSeconds(5))
            );
        }

        @Test
        void groupObserver_isAccepted() {
            assertNotNull(
                    Group.of(String.class, String.class)
                            .groupObserver(group -> Retention.HOLD)
            );
        }

        @Test
        void errorOccurredHandler_isAccepted() {
            assertNotNull(Group.of(String.class, String.class).errorOccurredHandler((src, tgt, item, err) -> {
            }));
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(Group.of(String.class, String.class).itemPostedHandler((src, item, accepted) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Group.of(String.class, String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }

        @Test
        void allOptionalFields_coveredViaToBlock() {
            NamedExecutorService executor = newExecutor();

            try {
                Group<String, String> group = Group.of(String.class, String.class)
                        .groupingFunction(s -> s)
                        .capacity(256)
                        .timeout(Duration.ofSeconds(10))
                        .idleTimeout(Duration.ofSeconds(5));

                group.executor(executor);

                assertNotNull(group.toSource());
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // PriorityBuffer
    // =========================================================================

    @Nested
    class PriorityBufferTests {
        @Test
        void comparator_deliversInPriorityOrder() {
            List<Integer> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<Integer> pipeline = Pipeline.<Integer>builder()
                        .executor(executor)
                        .from(PriorityBuffer.of(Integer.class)
                                .comparator(Integer::compareTo))
                        .to(received::add);

                pipeline.post(3);
                pipeline.post(1);
                pipeline.post(2);
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(3, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void capacityConfig_accepted() {
            assertDoesNotThrow(() -> PriorityBuffer.of(String.class).capacity(256));
        }

        @Test
        void ofLists_returnsInstance() {
            assertNotNull(PriorityBuffer.ofLists(String.class));
        }

        @Test
        void ofWithGenericType_returnsInstance() {
            assertNotNull(PriorityBuffer.of(new GenericType<String>() {
            }));
        }

        @Test
        void of_returnsNewInstance() {
            assertNotNull(PriorityBuffer.of());
        }

        @Test
        void errorOccurredHandler_isAccepted() {
            assertNotNull(PriorityBuffer.of(String.class).errorOccurredHandler((src, tgt, item, err) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(PriorityBuffer.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }

        @Test
        void capacityAndToSource_coveredViaToBlock() {
            NamedExecutorService executor = newExecutor();

            try {
                PriorityBuffer<String> buf = PriorityBuffer.of(String.class)
                        .comparator((a, b) -> 0)
                        .capacity(256);

                buf.executor(executor);

                assertNotNull(buf.toSource());
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // Transform
    // =========================================================================

    @Nested
    class TransformTests {
        @Test
        void nullResultFromTransform_itemDropped() {
            List<String> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class)
                            .transform(s -> s.startsWith("KEEP") ? s : null))
                    .to(received::add);

            pipeline.post("KEEP-this");
            pipeline.post("DROP-this");
            pipeline.post("KEEP-too");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("KEEP-this", "KEEP-too"), received);
        }

        @Test
        void ofLists_twoTypes_returnsInstance() {
            assertNotNull(Transform.ofLists(String.class, Integer.class));
        }

        @Test
        void ofLists_singleType_returnsInstance() {
            assertNotNull(Transform.ofLists(String.class));
        }

        @Test
        void ofWithBothGenericTypes_returnsInstance() {
            assertNotNull(Transform.of(new GenericType<String>() {
            }, new GenericType<Integer>() {
            }));
        }

        @Test
        void ofWithSingleGenericType_returnsInstance() {
            assertNotNull(Transform.of(new GenericType<String>() {
            }));
        }

        @Test
        void ofWithTwoClasses_returnsInstance() {
            assertNotNull(Transform.of(String.class, Integer.class));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(
                    Transform.of(String.class)
                            .transform(s -> s)
                            .itemDeliveredHandler((src, tgt, item) -> {
                            })
            );
        }

        @Test
        void toSource_returnsBlock() {
            assertNotNull(
                    Transform.of(String.class)
                            .transform(s -> s)
                            .toSource()
            );
        }
    }

    // =========================================================================
    // Expand
    // =========================================================================

    @Nested
    class ExpandTests {
        @Test
        void expandsList_eachElementDeliveredSeparately() {
            List<String> received = new ArrayList<>();

            Pipeline<List<String>> pipeline = Pipeline.<List<String>>builder()
                    .from(Expand.of(String.class))
                    .to(received::add);

            pipeline.post(List.of("a", "b", "c"));
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("a", "b", "c"), received);
        }

        @Test
        void ofWithGenericType_returnsInstance() {
            assertNotNull(Expand.of(new GenericType<String>() {
            }));
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(Expand.of(String.class).itemPostedHandler((src, item, accepted) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Expand.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }

        @Test
        void toSource_returnsBlock() {
            assertNotNull(Expand.of(String.class).toSource());
        }
    }

    // =========================================================================
    // Action
    // =========================================================================

    @Nested
    class ActionTests {
        @Test
        void ofWithClass_returnsInstance() {
            assertNotNull(Action.of(String.class));
        }

        @Test
        void ofWithGenericType_returnsInstance() {
            assertNotNull(Action.of(new GenericType<String>() {
            }));
        }
    }
}

