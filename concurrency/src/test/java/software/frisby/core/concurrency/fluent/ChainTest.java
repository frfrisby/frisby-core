package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChainTest {
    private static final String ALREADY_FINALIZED_MSG =
            "This pipeline chain has already been finalized.  Call to() only once per chain.";

    @Nested
    class Finalization {
        @Test
        void thenAfterTo_throwsIllegalStateException() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                Chain<String, String> chain = Pipeline.<String>builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class));

                chain.to(item -> {
                });

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
        void toAfterTo_throwsIllegalStateException() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                Chain<String, String> chain = Pipeline.<String>builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class));

                chain.to(item -> {
                });

                IllegalStateException ex = assertThrows(
                        IllegalStateException.class,
                        () -> chain.to(item -> {
                        })
                );

                assertEquals(ALREADY_FINALIZED_MSG, ex.getMessage());
            } finally {
                testExecutor.shutdown();
            }
        }

        @Test
        void thenOnIntermediateLinkAfterFinalization_throwsIllegalStateException() {
            ExecutorService testExecutor = Executors.newSingleThreadExecutor();

            try {
                Chain<String, String> head = Pipeline.<String>builder()
                        .executor(testExecutor)
                        .from(Buffer.of(String.class));

                Chain<String, String> tail = head.then(Transform.of(String.class)
                        .transform(s -> s));

                tail.to(item -> {
                });

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

        @Test
        void executorOverride_pipelineUsesOverriddenExecutor() {
            List<String> received = new ArrayList<>();
            ExecutorService executor1 = Executors.newSingleThreadExecutor();
            ExecutorService executor2 = Executors.newSingleThreadExecutor();

            try {
                Chain<String, String> chain = Pipeline.<String>builder()
                        .executor(executor1)
                        .from(Buffer.of(String.class));

                chain.executor(executor2);

                Pipeline<String> pipeline = chain.to(received::add);

                pipeline.post("hello");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(List.of("hello"), received);
            } finally {
                executor1.shutdown();
                executor2.shutdown();
            }
        }
    }

    @Nested
    class SingleStageFromPipelineTarget {
        @Test
        void fromPipelineTarget_buildsAndPosts() {
            List<String> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Action.<String>of().action(received::add));

            pipeline.post("hello");
            pipeline.post("world");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("hello", "world"), received);
        }
    }

    @Nested
    class MultiStageAssembly {
        @Test
        void twoStages_itemTransformedAndConsumed() {
            List<String> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class)
                            .transform(String::toUpperCase))
                    .to(received::add);

            pipeline.post("hello");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("HELLO"), received);
        }

        @Test
        void toWithAction_itemConsumed() {
            List<String> received = new ArrayList<>();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class)
                            .transform(s -> s))
                    .to(Action.<String>of().action(received::add));

            pipeline.post("test");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("test"), received);
        }
    }
}



