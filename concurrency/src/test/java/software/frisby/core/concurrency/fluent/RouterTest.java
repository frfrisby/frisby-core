package software.frisby.core.concurrency.fluent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.GenericType;
import software.frisby.core.concurrency.NamedExecutorService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


class RouterTest {
    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix("RouterTest")
                .build();
    }

    @Nested
    class Assembly {
        @Test
        void factory_createsOneArmPerRoute() {
            AtomicInteger factoryCallCount = new AtomicInteger();

            NamedExecutorService executor = newExecutor();

            try {
                Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Router.<String>of()
                                .routes(4)
                                .factory(() -> {
                                    factoryCallCount.incrementAndGet();
                                    return Pipeline.<String>builder()
                                            .from(Action.<String>of().action(item -> {
                                            }));
                                }));

                assertEquals(4, factoryCallCount.get());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void allItemsDelivered_roundRobin() {
            List<String> received = new CopyOnWriteArrayList<>();
            int itemCount = 100;
            int routeCount = 4;

            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Router.<String>of()
                                .roundRobin()
                                .routes(routeCount)
                                .factory(() -> Pipeline.<String>builder()
                                        .from(Action.<String>of().action(received::add))));

                for (int i = 0; i < itemCount; i++) {
                    pipeline.post("item-" + i);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(itemCount, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void allItemsDelivered_balanced() {
            List<String> received = new CopyOnWriteArrayList<>();
            int itemCount = 50;

            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Router.<String>of()
                                .balanced()
                                .routes(3)
                                .factory(() -> Pipeline.<String>builder()
                                        .from(Action.<String>of().action(received::add))));

                for (int i = 0; i < itemCount; i++) {
                    pipeline.post("item-" + i);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(itemCount, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void allItemsDelivered_stickyRouting() {
            List<String> group1 = new CopyOnWriteArrayList<>();
            List<String> group2 = new CopyOnWriteArrayList<>();

            NamedExecutorService executor = newExecutor();

            try {
                List<List<String>> sinks = List.of(group1, group2);
                AtomicInteger index = new AtomicInteger();

                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Router.<String>of()
                                .sticky(s -> s.split(":")[0])
                                .routes(2)
                                .factory(() -> {
                                    List<String> sink = sinks.get(index.getAndIncrement());
                                    return Pipeline.<String>builder()
                                            .from(Action.<String>of().action(sink::add));
                                }));

                pipeline.post("A:1");
                pipeline.post("B:1");
                pipeline.post("A:2");
                pipeline.post("B:2");
                pipeline.post("A:3");

                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(5, group1.size() + group2.size());
                // All "A" items in one arm, all "B" items in the other
                List<String> aItems = group1.stream().filter(s -> s.startsWith("A")).toList();
                List<String> bItems = group1.stream().filter(s -> s.startsWith("B")).toList();
                assertTrue(aItems.isEmpty() || bItems.isEmpty(),
                        "Sticky routing should keep A and B items in separate arms");
            } finally {
                executor.shutdown();
            }
        }
    }

    @Nested
    class FluentSetters {
        @Test
        void routingFunction_buildsSuccessfully() {
            List<String> received = new CopyOnWriteArrayList<>();
            NamedExecutorService executor = newExecutor();

            try {
                Pipeline<String> pipeline = Pipeline.<String>builder()
                        .executor(executor)
                        .from(Buffer.of(String.class))
                        .to(Router.<String>of()
                                .routingFunction(item -> 0)
                                .routes(2)
                                .factory(() -> Pipeline.<String>builder()
                                        .from(Action.<String>of().action(received::add))));

                pipeline.post("hello");
                pipeline.complete();
                pipeline.awaitCompletion();

                assertEquals(1, received.size());
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(Router.of(String.class).itemPostedHandler((src, item, accepted) -> {
            }));
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(Router.of(String.class).itemDeliveredHandler((src, tgt, item) -> {
            }));
        }
    }

    @Nested
    class FactoryMethods {
        @Test
        void of_returnsNewInstance() {
            assertNotNull(Router.of());
        }

        @Test
        void ofWithClass_returnsNewInstance() {
            assertNotNull(Router.of(String.class));
        }

        @Test
        void ofWithGenericType_returnsNewInstance() {
            assertNotNull(Router.of(new GenericType<String>() {
            }));
        }
    }
}

