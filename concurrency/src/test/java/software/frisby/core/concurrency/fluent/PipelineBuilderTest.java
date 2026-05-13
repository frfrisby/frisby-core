package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
}

