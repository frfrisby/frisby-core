package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.GenericType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BroadcastTest {
    private static <T> List<T> sink() {
        return new ArrayList<>();
    }

    private static <T> Pipeline<T> capturingPipeline(List<T> sink) {
        return Pipeline.<T>builder()
                .from(Action.<T>of().action(sink::add));
    }

    @Nested
    class Delivery {
        @Test
        void allTargetsReceiveEveryItem() {
            List<String> sink1 = sink();
            List<String> sink2 = sink();
            List<String> sink3 = sink();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class).transform(s -> s))
                    .to(Broadcast.<String>of()
                            .target(capturingPipeline(sink1))
                            .target(capturingPipeline(sink2))
                            .target(capturingPipeline(sink3)));

            pipeline.post("hello");
            pipeline.post("world");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("hello", "world"), sink1);
            assertEquals(List.of("hello", "world"), sink2);
            assertEquals(List.of("hello", "world"), sink3);
        }

        @Test
        void targetsMethod_addsAllTargets() {
            List<String> sink1 = sink();
            List<String> sink2 = sink();

            Pipeline<String> pipeline = Pipeline.<String>builder()
                    .from(Transform.of(String.class).transform(s -> s))
                    .to(Broadcast.<String>of()
                            .targets(List.of(
                                    capturingPipeline(sink1),
                                    capturingPipeline(sink2))));

            pipeline.post("test");
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(List.of("test"), sink1);
            assertEquals(List.of("test"), sink2);
        }

        @Test
        void cloningFunctionApplied_eachTargetReceivesIndependentCopy() {
            List<StringBuilder> sink1 = sink();
            List<StringBuilder> sink2 = sink();

            Pipeline<StringBuilder> pipeline = Pipeline.<StringBuilder>builder()
                    .from(Transform.of(StringBuilder.class).transform(s -> s))
                    .to(Broadcast.<StringBuilder>of()
                            .cloningFunction(sb -> new StringBuilder(sb.toString()))
                            .target(Pipeline.<StringBuilder>builder()
                                    .from(Action.<StringBuilder>of().action(sink1::add)))
                            .target(Pipeline.<StringBuilder>builder()
                                    .from(Action.<StringBuilder>of().action(sink2::add))));

            StringBuilder original = new StringBuilder("shared");

            pipeline.post(original);
            pipeline.complete();
            pipeline.awaitCompletion();

            assertEquals(1, sink1.size());
            assertEquals(1, sink2.size());
            assertNotSame(sink1.get(0), sink2.get(0));
            assertEquals("shared", sink1.get(0).toString());
            assertEquals("shared", sink2.get(0).toString());
        }

        @Test
        void noCloningFunction_sameReferenceDeliveredToAllTargets() {
            List<Object> sink1 = sink();
            List<Object> sink2 = sink();

            Object item = new Object();

            Pipeline<Object> pipeline = Pipeline.builder()
                    .from(Transform.of(Object.class).transform(s -> s))
                    .to(Broadcast.of()
                            .target(Pipeline.builder()
                                    .from(Action.of().action(sink1::add)))
                            .target(Pipeline.builder()
                                    .from(Action.of().action(sink2::add))));

            pipeline.post(item);
            pipeline.complete();
            pipeline.awaitCompletion();

            assertSame(item, sink1.get(0));
            assertSame(item, sink2.get(0));
        }
    }

    @Nested
    class FactoryMethods {
        @Test
        void of_returnsNewInstance() {
            assertNotNull(Broadcast.of());
        }

        @Test
        void ofWithClass_returnsNewInstance() {
            assertNotNull(Broadcast.of(String.class));
        }

        @Test
        void ofWithGenericType_returnsNewInstance() {
            assertNotNull(Broadcast.of(new GenericType<String>() {
            }));
        }
    }

    @Nested
    class FluentSetters {
        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Broadcast.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }
    }
}

