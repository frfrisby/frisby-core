package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.NamedExecutorService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenRouterTest {
    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix("OpenRouterTest")
                .build();
    }

    // =========================================================================
    // Fluent setter methods
    // =========================================================================

    @Nested
    class FluentSetters {
        @Test
        void sticky_isAccepted() {
            assertNotNull(OpenRouter.of(String.class).sticky(String::hashCode));
        }

        @Test
        void routingFunction_isAccepted() {
            assertNotNull(OpenRouter.of(String.class).routingFunction(item -> 0));
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(OpenRouter.of(String.class).itemPostedHandler((src, item, accepted) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(OpenRouter.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }
    }

    // =========================================================================
    // Assembly
    // =========================================================================

    @Nested
    class Assembly {
        @Test
        void toSource_beforeToTarget_lazilyBuildsRouterInfo() {
            NamedExecutorService executor = newExecutor();

            try {
                OpenRouter<String, String> router = OpenRouter.of(String.class)
                        .routes(2)
                        .factory(() -> OpenPipeline.builder()
                                .executor(executor)
                                .from(Transform.of(String.class).transform(s -> s))
                                .build());

                // toSource() is called first; the routerInfo must be created lazily.
                assertNotNull(router.toSource());

                // toTarget() must return the already-constructed block — not rebuild it.
                assertNotNull(router.toTarget());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void stickyRouting_allItemsDelivered() {
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .then(OpenRouter.<String, String>of()
                                .sticky(s -> s.split(":")[0])
                                .routes(2)
                                .factory(() -> OpenPipeline.builder()
                                        .executor(executor)
                                        .from(Transform.of(String.class).transform(s -> s))
                                        .build()))
                        .to(received::add);

                pipeline.post("A:1");
                pipeline.post("B:1");
                pipeline.post("A:2");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(3, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void routingFunction_allItemsDelivered() {
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .then(OpenRouter.<String, String>of()
                                .routingFunction(item -> 0)
                                .routes(2)
                                .factory(() -> OpenPipeline.builder()
                                        .executor(executor)
                                        .from(Transform.of(String.class).transform(s -> s))
                                        .build()))
                        .to(received::add);

                pipeline.post("hello");
                pipeline.post("world");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(2, received.size());
            } finally {
                executor.shutdown();
            }
        }
    }
}

