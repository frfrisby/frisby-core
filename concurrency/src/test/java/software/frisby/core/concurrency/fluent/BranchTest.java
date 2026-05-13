package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.GenericType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BranchTest {
    private static final String OTHERWISE_ALREADY_CONFIGURED_MSG =
            "The 'BranchBlock' block already has an otherwise target configured.  The otherwise() method may only be called once.";

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private static <T> List<T> armList() {
        return new ArrayList<>();
    }

    private static <T> Pipeline<T> capturingPipeline(List<T> sink) {
        return Pipeline.<T>builder()
                .from(Action.<T>of().action(sink::add));
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Nested
    class Routing {
        @Test
        void whenPredicateMatches_itemRoutedToCorrectArm() {
            List<String> highReceived = armList();
            List<String> lowReceived = armList();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class).transform(s -> s))
                    .to(Branch.<String>of()
                            .when(s -> s.startsWith("HIGH"), capturingPipeline(highReceived))
                            .otherwise(capturingPipeline(lowReceived)));

            pipeline.post("HIGH-001");
            pipeline.post("LOW-001");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("HIGH-001"), highReceived);
            assertEquals(List.of("LOW-001"), lowReceived);
        }

        @Test
        void firstMatchingPredicateWins() {
            List<String> firstReceived = armList();
            List<String> secondReceived = armList();
            List<String> otherwiseReceived = armList();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class).transform(s -> s))
                    .to(Branch.<String>of()
                            .when(s -> s.length() > 2, capturingPipeline(firstReceived))
                            .when(s -> s.startsWith("A"), capturingPipeline(secondReceived))
                            .otherwise(capturingPipeline(otherwiseReceived)));

            pipeline.post("ABC");

            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("ABC"), firstReceived);
            assertTrue(secondReceived.isEmpty());
            assertTrue(otherwiseReceived.isEmpty());
        }

        @Test
        void noPredicateMatches_itemRoutedToOtherwise() {
            List<String> whenReceived = armList();
            List<String> otherwiseReceived = armList();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class).transform(s -> s))
                    .to(Branch.<String>of()
                            .when(s -> s.startsWith("X"), capturingPipeline(whenReceived))
                            .otherwise(capturingPipeline(otherwiseReceived)));

            pipeline.post("hello");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertTrue(whenReceived.isEmpty());
            assertEquals(List.of("hello"), otherwiseReceived);
        }

        @Test
        void multipleItems_eachRoutedIndependently() {
            List<String> evenReceived = armList();
            List<String> oddReceived = armList();

            Pipeline<Integer> pipeline = Pipeline.<Integer>builder()
                    .from(Transform.of(Integer.class).transform(n -> n))
                    .to(Branch.<Integer>of()
                            .when(n -> n % 2 == 0, Pipeline.<Integer>builder()
                                    .from(Action.<Integer>of().action(n -> evenReceived.add(String.valueOf(n)))))
                            .otherwise(Pipeline.<Integer>builder()
                                    .from(Action.<Integer>of().action(n -> oddReceived.add(String.valueOf(n))))));

            for (int i = 1; i <= 6; i++) {
                pipeline.post(i);
            }

            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("2", "4", "6"), evenReceived);
            assertEquals(List.of("1", "3", "5"), oddReceived);
        }
    }

    @Nested
    class FactoryMethods {
        @Test
        void of_returnsNewInstance() {
            assertNotNull(Branch.of());
        }

        @Test
        void ofWithClass_returnsNewInstance() {
            assertNotNull(Branch.of(String.class));
        }

        @Test
        void ofWithGenericType_returnsNewInstance() {
            assertNotNull(Branch.of(new GenericType<String>() {
            }));
        }
    }

    @Nested
    class FluentSetters {
        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Branch.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }
    }

    @Nested
    class GuardClauses {
        @Test
        void otherwiseCalledTwice_throwsIllegalStateException() {
            Pipeline<String> anyPipeline = capturingPipeline(armList());

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> Branch.<String>of()
                            .otherwise(anyPipeline)
                            .otherwise(anyPipeline)
            );

            assertEquals(OTHERWISE_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }
    }
}

