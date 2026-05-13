package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MiscCoverageTest {
    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix("MiscCoverageTest")
                .build();
    }

    // =========================================================================
    // DefaultPipeline — onLinked() and size()
    // =========================================================================

    @Nested
    class DefaultPipelineTests {
        @Test
        void size_delegatesToHead() {
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Action.<String>of().action(item -> {
                        }));

                // size() is available immediately — buffer starts empty
                assertEquals(0, pipeline.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void onLinked_doesNotThrow() {
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Action.<String>of().action(item -> {
                        }));

                assertDoesNotThrow(pipeline::onLinked);
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // OpenRouter — additional config paths
    // =========================================================================

    @Nested
    class OpenRouterAdditionalTests {
        @Test
        void roundRobin_allItemsDelivered() {
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .then(OpenRouter.<String, String>of()
                                .roundRobin()
                                .routes(3)
                                .factory(() -> OpenPipeline.builder()
                                        .executor(executor)
                                        .from(Buffer.of(String.class))
                                        .build()))
                        .to(received::add);

                for (int i = 0; i < 9; i++) {
                    pipeline.post("item-" + i);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(9, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void balanced_allItemsDelivered() {
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .then(OpenRouter.<String, String>of()
                                .balanced()
                                .routes(2)
                                .factory(() -> OpenPipeline.builder()
                                        .executor(executor)
                                        .from(Buffer.of(String.class))
                                        .build()))
                        .to(received::add);

                for (int i = 0; i < 10; i++) {
                    pipeline.post("item-" + i);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(10, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void ofWithTwoClasses_returnsInstance() {
            assertNotNull(OpenRouter.of(String.class, Integer.class));
        }

        @Test
        void ofWithSingleClass_returnsInstance() {
            assertNotNull(OpenRouter.of(String.class));
        }

        @Test
        void ofWithBothGenericTypes_returnsInstance() {
            assertNotNull(OpenRouter.of(new GenericType<String>() {
            }, new GenericType<Integer>() {
            }));
        }

        @Test
        void ofWithSingleGenericType_returnsInstance() {
            assertNotNull(OpenRouter.of(new GenericType<String>() {
            }));
        }

        @Test
        void ofLists_twoTypes_returnsInstance() {
            assertNotNull(OpenRouter.ofLists(String.class, Integer.class));
        }

        @Test
        void ofLists_singleType_returnsInstance() {
            assertNotNull(OpenRouter.ofLists(String.class));
        }
    }

    // =========================================================================
    // GenericType — additional edge cases
    // =========================================================================

    @Nested
    class GenericTypeAdditionalTests {
        @Test
        void ofLists_buffer_worksThroughFactory() {
            Buffer<List<String>> buffer = Buffer.ofLists(String.class);

            assertNotNull(buffer);
        }

        @Test
        void ofLists_batch_worksThroughFactory() {
            assertNotNull(Batch.of(new GenericType<List<String>>() {
            }));
        }

        @Test
        void ofLists_delay_worksThroughFactory() {
            assertNotNull(Delay.ofLists(String.class));
        }

        @Test
        void ofLists_priorityBuffer_worksThroughFactory() {
            assertNotNull(PriorityBuffer.ofLists(String.class));
        }

        @Test
        void ofLists_transform_sameType() {
            assertNotNull(Transform.ofLists(String.class));
        }

        @Test
        void ofLists_action_returnsInstance() {
            assertNotNull(Action.ofLists(String.class));
        }

        @Test
        void ofLists_tap_returnsInstance() {
            assertNotNull(Tap.ofLists(String.class));
        }

        @Test
        void ofLists_broadcast_returnsInstance() {
            assertNotNull(Broadcast.ofLists(String.class));
        }

        @Test
        void ofLists_router_returnsInstance() {
            assertNotNull(Router.ofLists(String.class));
        }

        @Test
        void ofLists_branch_returnsInstance() {
            assertNotNull(Branch.ofLists(String.class));
        }
    }

    // =========================================================================
    // Buffer — ofLists and itemPostedHandler coverage
    // =========================================================================

    @Nested
    class BufferAdditionalTests {
        @Test
        void ofLists_buildsListPipeline() {
            List<List<String>> received = new ArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<List<String>> pipeline = Pipeline.<List<String>>builder()
                        .executor(executor)
                        .from(Buffer.ofLists(String.class))
                        .to(received::add);

                pipeline.post(List.of("a", "b"));
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(1, received.size());
                assertEquals(List.of("a", "b"), received.get(0));
            } finally {
                executor.shutdown();
            }
        }
    }

    // =========================================================================
    // Block builder inference overloads — builder(Class<T>)
    // =========================================================================

    @Nested
    class BlockBuilderInferenceTests {
        @Test
        void actionBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(ActionBlock.builder(String.class));
        }

        @Test
        void batchBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(BatchBlock.builder(String.class));
        }

        @Test
        void branchBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(BranchBlock.builder(String.class));
        }

        @Test
        void broadcastBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(BroadcastBlock.builder(String.class));
        }

        @Test
        void bufferBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(BufferBlock.builder(String.class));
        }

        @Test
        void delayBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(DelayBlock.builder(String.class));
        }

        @Test
        void expandBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(ExpandBlock.builder(String.class));
        }

        @Test
        void groupBlock_builder_withClassTokens_returnsBuilder() {
            assertNotNull(GroupBlock.builder(String.class, Integer.class));
        }

        @Test
        void priorityBufferBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(PriorityBufferBlock.builder(String.class));
        }

        @Test
        void routerBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(RouterBlock.builder(String.class));
        }

        @Test
        void sourceBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(SourceBlock.builder(String.class));
        }

        @Test
        void tapBlock_builder_withClassToken_returnsBuilder() {
            assertNotNull(TapBlock.builder(String.class));
        }

        @Test
        void transformBlock_builder_withClassTokens_returnsBuilder() {
            assertNotNull(TransformBlock.builder(String.class, Integer.class));
        }
    }
}

