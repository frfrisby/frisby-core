package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.NamedExecutorService;
import software.frisby.core.concurrency.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenChainTest {
    private static final String ALREADY_FINALIZED_MSG =
            "This open pipeline chain has already been finalized.  Call build() only once per chain.";

    @Nested
    class Finalization {
        @Test
        void thenAfterBuild_throwsIllegalStateException() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                OpenChain<String, String, String> chain = OpenPipeline.builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class));

                chain.build();

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> chain.then(Transform.of(String.class))
                );

                assertEquals(ALREADY_FINALIZED_MSG, ex.getMessage());
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void buildAfterBuild_throwsIllegalStateException() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                OpenChain<String, String, String> chain = OpenPipeline.builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class));

                chain.build();

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        chain::build
                );

                assertEquals(ALREADY_FINALIZED_MSG, ex.getMessage());
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void thenOnEarlierLinkAfterBuild_throwsIllegalStateException() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                OpenChain<String, String, String> head = OpenPipeline.builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class));

                OpenChain<String, String, String> tail = head.then(Transform.of(String.class)
                        .transform(s -> s));

                tail.build();

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> head.then(Transform.of(String.class))
                );

                assertEquals(ALREADY_FINALIZED_MSG, ex.getMessage());
            } finally {
                testExecutor.shutdown();
            }
        }
    }

    @Nested
    class ExecutorPropagation {
        @Test
        void explicitExecutor_pipelineProcessesAllItems() {
            List<String> received = new ArrayList<>();
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                OpenPipeline<String, String> open = OpenPipeline.builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class))
                        .build();

                open.linkTo(received::add);

                open.post("a");
                open.post("b");
                open.complete();
                open.awaitCompletion();

                assertEquals(List.of("a", "b"), received);
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void noExecutorProvided_throwsIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> OpenPipeline.builder()
                            .from(Buffer.of(String.class))
                            .build()
            );

            assertEquals("The 'executor' value is invalid. The value must not be null.", ex.getMessage());
        }

        @Test
        void executorOverride_pipelineUsesOverriddenExecutor() {
            List<String> received = new ArrayList<>();
            ExecutorService executor1 = Executors.newSingleThreadExecutor();
            ExecutorService executor2 = Executors.newSingleThreadExecutor();

            try {
                OpenChain<String, String, String> chain = OpenPipeline.builder()
                        .executor(executor1)
                        .from(Buffer.of(String.class));

                chain.executor(executor2);

                OpenPipeline<String, String> open = chain.build();

                open.linkTo(received::add);
                open.post("hello");
                open.complete();
                open.awaitCompletion();

                assertEquals(List.of("hello"), received);
            } finally {
                executor1.shutdown();
                executor2.shutdown();
            }
        }
    }

    @Nested
    class EmbeddedOpenPipeline {
        @Test
        void thenWithPrebuiltOpenPipeline_itemsFlowThrough() {
            List<String> received = new ArrayList<>();
            ExecutorService executor = Executors.newSingleThreadExecutor();

            try {
                // A pre-assembled open pipeline that transforms items to upper-case.
                OpenPipeline<String, String> embedded = OpenPipeline.builder()
                        .executor(executor)
                        .from(Transform.of(String.class).transform(String::toUpperCase))
                        .build();

                // Embed it as a stage using the then(OpenPipeline) overload.
                OpenPipeline<String, String> open = OpenPipeline.builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .then(embedded)
                        .build();

                open.linkTo(received::add);
                open.post("hello");
                open.complete();
                open.awaitCompletion();

                assertEquals(List.of("HELLO"), received);
            } finally {
                executor.shutdown();
            }
        }
    }

    @Nested
    class TailWiring {
        @Test
        void build_tailSourceCanBeLinkedToDownstream() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                List<String> received = new ArrayList<>();

                OpenPipeline<String, String> open = OpenPipeline.builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class))
                        .then(Transform.of(String.class).transform(String::toUpperCase))
                        .build();

                Target<String> terminal = received::add;

                open.linkTo(terminal);

                open.post("hello");
                open.complete();
                open.awaitCompletion();

                assertEquals(List.of("HELLO"), received);
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void buildSingleStage_tailSourceLinkedCorrectly() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                List<String> received = new ArrayList<>();

                OpenPipeline<String, String> open = OpenPipeline.builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class))
                        .build();

                open.linkTo(received::add);

                open.post("world");
                open.complete();
                open.awaitCompletion();

                assertEquals(List.of("world"), received);
            } finally {
                testExecutor.shutdown();
            }
        }
    }

    // =========================================================================
    // DefaultOpenPipeline — size() and inFlight()
    // =========================================================================

    @Nested
    class DefaultOpenPipelineObservability {
        @Test
        void size_delegatesToHead() {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            try {
                OpenPipeline<String, String> open = OpenPipeline.builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .build();

                assertEquals(0, open.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void inFlight_delegatesToHead() {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("OpenChainTest")
                    .build();

            try {
                OpenPipeline<String, String> open = OpenPipeline.builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .build();

                // Link a tail target that reports a known inFlight() value.
                // This exercises the full delegation chain: open → head → targetManager → tail.
                open.linkTo(new Target<>() {
                    @Override
                    public boolean post(String item) {
                        return true;
                    }

                    @Override
                    public int inFlight() {
                        return 7;
                    }
                });

                assertEquals(7, open.inFlight());
            } finally {
                executor.shutdown();
            }
        }
    }
}






