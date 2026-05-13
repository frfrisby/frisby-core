package software.frisby.core.concurrency.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.NamedExecutorService;
import software.frisby.core.concurrency.fluent.*;
import software.frisby.core.concurrency.log.SystemLogVerifier;
import software.frisby.core.validation.Maps;
import software.frisby.core.validation.Sequences;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class PipelineTest {
    private static final String PREFIX = "TestPipeline";

    private static NamedExecutorService newExecutor() {
        return NamedExecutorService.builder()
                .threadPrefix(PREFIX)
                .build();
    }

    private static TestData generateMessages(int total) {
        Map<String, List<Message>> customerMessages = new HashMap<>();
        List<Message> allMessages = new ArrayList<>();

        Instant start = Instant.now().minusSeconds(total * 2L);
        for (int i = 0; i < total; i++) {
            String customerId = "customer-" + (i % 100);
            Instant receivedWhen = start.plusSeconds(i);

            Message message = new Message(
                    Priority.values()[i % Priority.values().length],
                    receivedWhen,
                    customerId,
                    "device-" + i,
                    new Location(BigDecimal.valueOf(37.7749), BigDecimal.valueOf(-122.4194)),
                    new TemperatureReading(UnitOfMeasure.values()[i % UnitOfMeasure.values().length], BigDecimal.valueOf(20 + (i % 10)), receivedWhen.minusSeconds(10))
            );

            allMessages.add(message);
            customerMessages.computeIfAbsent(customerId, s -> new ArrayList<>()).add(message);
        }

        return new TestData(customerMessages, allMessages);
    }

    private enum Priority {
        High,
        Medium,
        Low
    }

    private enum UnitOfMeasure {
        CELSIUS,
        FAHRENHEIT,
        KELVIN
    }

    private record TestData(Map<String, List<Message>> messagesByCustomer,
                            List<Message> messages) {
        private TestData {
            Maps.notEmpty("messagesByCustomer", messagesByCustomer);
            Sequences.notEmpty("messages", messages);
        }
    }

    private record Message(Priority priority,
                           Instant receivedWhen,
                           String customerId,
                           String deviceId,
                           Location location,
                           TemperatureReading temperatureReading) {
    }

    private record TemperatureReading(UnitOfMeasure unitOfMeasure,
                                      BigDecimal value,
                                      Instant sampleTime) {
    }

    private record Location(BigDecimal latitude,
                            BigDecimal longitude) {
    }

    @Nested
    class Terminal {
        @Test
        void branched_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            Map<String, Message> highPriorityMessages = new ConcurrentHashMap<>();
            Map<String, Message> mediumPriorityMessages = new ConcurrentHashMap<>();
            Map<String, Message> lowPriorityMessages = new ConcurrentHashMap<>();

            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .from(Branch.of(Message.class)
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet())
                                    .when(
                                            message -> Priority.High.equals(message.priority()),
                                            Pipeline.<Message>builder()
                                                    .executor(executor)
                                                    .from(Buffer.of(Message.class))
                                                    .then(Transform.of(Message.class)
                                                            .transform(message -> {
                                                                String customerId = message.customerId();
                                                                messagesReceivedByCustomer
                                                                        .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                        .add(message);
                                                                highPriorityMessages.put(message.deviceId(), message);

                                                                return message;
                                                            }))
                                                    .to(message -> messagesProcessed.incrementAndGet())
                                    )
                                    .when(
                                            message -> Priority.Medium.equals(message.priority()),
                                            Pipeline.<Message>builder()
                                                    .executor(executor)
                                                    .from(Buffer.of(Message.class))
                                                    .then(Transform.of(Message.class)
                                                            .transform(message -> {
                                                                String customerId = message.customerId();
                                                                messagesReceivedByCustomer
                                                                        .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                        .add(message);
                                                                mediumPriorityMessages.put(message.deviceId(), message);

                                                                return message;
                                                            }))
                                                    .to(message -> messagesProcessed.incrementAndGet())
                                    )
                                    .otherwise(
                                            Pipeline.<Message>builder()
                                                    .executor(executor)
                                                    .from(Buffer.of(Message.class))
                                                    .then(Transform.of(Message.class)
                                                            .transform(message -> {
                                                                String customerId = message.customerId();
                                                                messagesReceivedByCustomer
                                                                        .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                        .add(message);
                                                                lowPriorityMessages.put(message.deviceId(), message);

                                                                return message;
                                                            }))
                                                    .to(message -> messagesProcessed.incrementAndGet())
                                    ));

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                    for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                        String customerId = entry.getKey();
                        List<Message> messages = entry.getValue();
                        messages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                        List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                        receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertEquals(messages, receivedMessages);

                        for (Message message : messages) {
                            if (Priority.High.equals(message.priority())) {
                                Assertions.assertTrue(highPriorityMessages.containsKey(message.deviceId()));
                                Assertions.assertEquals(message, highPriorityMessages.get(message.deviceId()));
                            } else if (Priority.Medium.equals(message.priority())) {
                                Assertions.assertTrue(mediumPriorityMessages.containsKey(message.deviceId()));
                                Assertions.assertEquals(message, mediumPriorityMessages.get(message.deviceId()));
                            } else {
                                Assertions.assertTrue(lowPriorityMessages.containsKey(message.deviceId()));
                                Assertions.assertEquals(message, lowPriorityMessages.get(message.deviceId()));
                            }
                        }
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void broadcast_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            Map<String, Message> highPriorityMessages = new ConcurrentHashMap<>();
            Map<String, Message> mediumPriorityMessages = new ConcurrentHashMap<>();
            Map<String, Message> lowPriorityMessages = new ConcurrentHashMap<>();

            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();
            AtomicInteger messagesAudited = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .from(Broadcast.of(Message.class)
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet())
                                    // Simulate an async auditing path...
                                    .target(Pipeline.<Message>builder()
                                            .executor(executor)
                                            .from(Buffer.of(Message.class))
                                            .to(message -> messagesAudited.incrementAndGet()))
                                    // Simulate a branched processing path...
                                    .target(Pipeline.<Message>builder()
                                            .from(Branch.of(Message.class)
                                                    .when(
                                                            message -> Priority.High.equals(message.priority()),
                                                            Pipeline.<Message>builder()
                                                                    .executor(executor)
                                                                    .from(Buffer.of(Message.class))
                                                                    .then(Transform.of(Message.class)
                                                                            .transform(message -> {
                                                                                String customerId = message.customerId();
                                                                                messagesReceivedByCustomer
                                                                                        .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                                        .add(message);
                                                                                highPriorityMessages.put(message.deviceId(), message);

                                                                                return message;
                                                                            }))
                                                                    .to(message -> messagesProcessed.incrementAndGet())
                                                    )
                                                    .when(
                                                            message -> Priority.Medium.equals(message.priority()),
                                                            Pipeline.<Message>builder()
                                                                    .executor(executor)
                                                                    .from(Buffer.of(Message.class))
                                                                    .then(Transform.of(Message.class)
                                                                            .transform(message -> {
                                                                                String customerId = message.customerId();
                                                                                messagesReceivedByCustomer
                                                                                        .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                                        .add(message);
                                                                                mediumPriorityMessages.put(message.deviceId(), message);

                                                                                return message;
                                                                            }))
                                                                    .to(message -> messagesProcessed.incrementAndGet())
                                                    )
                                                    .otherwise(
                                                            Pipeline.<Message>builder()
                                                                    .executor(executor)
                                                                    .from(Buffer.of(Message.class))
                                                                    .then(Transform.of(Message.class)
                                                                            .transform(message -> {
                                                                                String customerId = message.customerId();
                                                                                messagesReceivedByCustomer
                                                                                        .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                                        .add(message);
                                                                                lowPriorityMessages.put(message.deviceId(), message);

                                                                                return message;
                                                                            }))
                                                                    .to(message -> messagesProcessed.incrementAndGet())
                                                    ))
                                    ));

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());
                    Assertions.assertEquals(testData.messages().size(), messagesAudited.get());

                    for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                        String customerId = entry.getKey();
                        List<Message> messages = entry.getValue();
                        messages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                        List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                        receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertEquals(messages, receivedMessages);

                        for (Message message : messages) {
                            if (Priority.High.equals(message.priority())) {
                                Assertions.assertTrue(highPriorityMessages.containsKey(message.deviceId()));
                                Assertions.assertEquals(message, highPriorityMessages.get(message.deviceId()));
                            } else if (Priority.Medium.equals(message.priority())) {
                                Assertions.assertTrue(mediumPriorityMessages.containsKey(message.deviceId()));
                                Assertions.assertEquals(message, mediumPriorityMessages.get(message.deviceId()));
                            } else {
                                Assertions.assertTrue(lowPriorityMessages.containsKey(message.deviceId()));
                                Assertions.assertEquals(message, lowPriorityMessages.get(message.deviceId()));
                            }
                        }
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void actionOnly_allProcessed() {
            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                Pipeline<Message> pipeline = Pipeline.<Message>builder()
                        .from(Action.of(Message.class)
                                .itemPostedHandler((source, item, accepted) ->
                                        messagesReceived.incrementAndGet())
                                .action(message -> {
                                    String customerId = message.customerId();
                                    messagesReceivedByCustomer
                                            .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                            .add(message);

                                    messagesProcessed.incrementAndGet();
                                }));

                for (Message message : testData.messages()) {
                    pipeline.post(message);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                    String customerId = entry.getKey();
                    List<Message> messages = entry.getValue();
                    messages.sort(Comparator.comparing(Message::receivedWhen));

                    Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                    List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                    receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                    Assertions.assertEquals(messages, receivedMessages);
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void consumerOnly_allProcessed() {
            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                Pipeline<Message> pipeline = Pipeline.<Message>builder()
                        .from(message -> {
                            messagesReceived.incrementAndGet();

                            String customerId = message.customerId();
                            messagesReceivedByCustomer
                                    .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                    .add(message);

                            messagesProcessed.incrementAndGet();
                        });

                for (Message message : testData.messages()) {
                    pipeline.post(message);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                    String customerId = entry.getKey();
                    List<Message> messages = entry.getValue();
                    messages.sort(Comparator.comparing(Message::receivedWhen));

                    Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                    List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                    receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                    Assertions.assertEquals(messages, receivedMessages);
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void syncStagesOnly_allProcessed() {
            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                Pipeline<Message> pipeline = Pipeline.<Message>builder()
                        .from(Transform.of(Message.class)
                                .transform(message -> {
                                    String customerId = message.customerId();
                                    messagesReceivedByCustomer
                                            .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                            .add(message);

                                    return message;
                                })
                                .itemPostedHandler((source, item, accepted) ->
                                        messagesReceived.incrementAndGet()))
                        .to(message -> messagesProcessed.incrementAndGet());

                for (Message message : testData.messages()) {
                    pipeline.post(message);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                    String customerId = entry.getKey();
                    List<Message> messages = entry.getValue();
                    messages.sort(Comparator.comparing(Message::receivedWhen));

                    Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                    List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                    receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                    Assertions.assertEquals(messages, receivedMessages);
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void multipleAsyncAndSyncStages_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(PriorityBuffer.of(Message.class)
                                    .comparator(Comparator.comparing(message -> switch (message.priority) {
                                        case High -> 1;
                                        case Medium -> 2;
                                        case Low -> 3;
                                    }))
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet()))
                            .then(Group.of(Message.class, String.class)
                                    .groupingFunction(Message::customerId)
                                    .maxGroupSize(64))
                            .then(Buffer.ofLists(Message.class)
                                    .capacity(128))
                            .then(Transform.ofLists(Message.class)
                                    .transform(messages -> {
                                        String customerId = messages.get(0).customerId();
                                        messagesReceivedByCustomer
                                                .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                .addAll(messages);

                                        return messages;
                                    }))
                            .then(Expand.of())
                            .then(Delay.of(Message.class)
                                    .delay(Duration.ofSeconds(5)))
                            .then(Batch.of())
                            .to(messages ->
                                    messages.forEach(message ->
                                            messagesProcessed.incrementAndGet()));

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                    for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                        String customerId = entry.getKey();
                        List<Message> messages = entry.getValue();
                        messages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                        List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                        receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertEquals(messages, receivedMessages);
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void fromOpenPipeline_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(1000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    OpenPipeline<Message, Message> bufferPipeline = OpenPipeline.builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class)
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet()))
                            .build();

                    OpenPipeline<Message, List<Message>> routerPipeline = OpenPipeline.builder()
                            .executor(executor)
                            .from(bufferPipeline)
                            .then(OpenRouter.<Message, List<Message>>of()
                                    .routes(10)
                                    .factory(() -> OpenPipeline.builder()
                                            .executor(executor)
                                            .from(Buffer.of(Message.class))
                                            .then(Group.of(Message.class, String.class)
                                                    .groupingFunction(Message::customerId)
                                                    .maxGroupSize(64))
                                            .then(Buffer.ofLists(Message.class)
                                                    .capacity(128))
                                            .then(Transform.ofLists(Message.class)
                                                    .transform(messages -> {
                                                        String customerId = messages.get(0).customerId();
                                                        messagesReceivedByCustomer
                                                                .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                .addAll(messages);

                                                        return messages;
                                                    }))
                                            .build()))
                            .build();

                    Pipeline<List<Message>> actionPipeline = Pipeline.<List<Message>>builder()
                            .from(messages ->
                                    messages.forEach(message ->
                                            messagesProcessed.incrementAndGet()));

                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .from(routerPipeline)
                            .to(actionPipeline);

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                    for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                        String customerId = entry.getKey();
                        List<Message> messages = entry.getValue();
                        messages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                        List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                        receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertEquals(messages, receivedMessages);
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }

    @Nested
    class Routed {
        @Test
        void open_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(2000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class)
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet()))
                            .then(OpenRouter.<Message, List<Message>>of()
                                    .balanced()
                                    .routes(10)
                                    .factory(() -> OpenPipeline.builder()
                                            .executor(executor)
                                            .from(Buffer.of(Message.class))
                                            .then(Group.of(Message.class, String.class)
                                                    .groupingFunction(Message::customerId)
                                                    .maxGroupSize(64))
                                            .then(Buffer.ofLists(Message.class)
                                                    .capacity(128))
                                            .then(Transform.ofLists(Message.class)
                                                    .transform(messages -> {
                                                        String customerId = messages.get(0).customerId();
                                                        messagesReceivedByCustomer
                                                                .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                .addAll(messages);

                                                        return messages;
                                                    }))
                                            .build()))
                            .to(messages ->
                                    messages.forEach(message ->
                                            messagesProcessed.incrementAndGet()));

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                    for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                        String customerId = entry.getKey();
                        List<Message> messages = entry.getValue();
                        messages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                        List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                        receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertEquals(messages, receivedMessages);
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void terminal_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(2000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class)
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet()))
                            .to(Router.<Message>of()
                                    .sticky((Function<Message, String>) Message::deviceId)
                                    .routes(10)
                                    .factory(() -> Pipeline.<Message>builder()
                                            .executor(executor)
                                            .from(Buffer.of(Message.class))
                                            .then(Group.of(Message.class, String.class)
                                                    .groupingFunction(Message::customerId)
                                                    .maxGroupSize(64))
                                            .then(Buffer.ofLists(Message.class)
                                                    .capacity(128))
                                            .then(Transform.ofLists(Message.class)
                                                    .transform(messages -> {
                                                        String customerId = messages.get(0).customerId();
                                                        messagesReceivedByCustomer
                                                                .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                .addAll(messages);

                                                        return messages;
                                                    }))
                                            .to(messages ->
                                                    messages.forEach(message ->
                                                            messagesProcessed.incrementAndGet()))));

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());

                    for (Map.Entry<String, List<Message>> entry : testData.messagesByCustomer().entrySet()) {
                        String customerId = entry.getKey();
                        List<Message> messages = entry.getValue();
                        messages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertTrue(messagesReceivedByCustomer.containsKey(customerId));

                        List<Message> receivedMessages = new ArrayList<>(messagesReceivedByCustomer.get(customerId));
                        receivedMessages.sort(Comparator.comparing(Message::receivedWhen));

                        Assertions.assertEquals(messages, receivedMessages);
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void openRouter_roundRobin_allProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(2000);

            Map<String, List<Message>> messagesReceivedByCustomer = new ConcurrentHashMap<>();
            AtomicInteger messagesReceived = new AtomicInteger();
            AtomicInteger messagesProcessed = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class)
                                    .itemPostedHandler((source, item, accepted) ->
                                            messagesReceived.incrementAndGet()))
                            .then(OpenRouter.<Message, List<Message>>of()
                                    .roundRobin()
                                    .routes(10)
                                    .factory(() -> OpenPipeline.builder()
                                            .executor(executor)
                                            .from(Buffer.of(Message.class))
                                            .then(Group.of(Message.class, String.class)
                                                    .groupingFunction(Message::customerId)
                                                    .maxGroupSize(64))
                                            .then(Buffer.ofLists(Message.class)
                                                    .capacity(128))
                                            .then(Transform.ofLists(Message.class)
                                                    .transform(messages -> {
                                                        String customerId = messages.get(0).customerId();
                                                        messagesReceivedByCustomer
                                                                .computeIfAbsent(customerId, s -> new CopyOnWriteArrayList<>())
                                                                .addAll(messages);

                                                        return messages;
                                                    }))
                                            .build()))
                            .to(messages ->
                                    messages.forEach(message ->
                                            messagesProcessed.incrementAndGet()));

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), messagesReceived.get());
                    Assertions.assertEquals(testData.messages().size(), messagesProcessed.get());
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }

    @Nested
    class ErrorHandling {
        @Test
        void deliveryThrows_withErrorHandler_handlerCalledAndPipelineContinues() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(10);

            Set<String> badDeviceIds = new java.util.HashSet<>();
            for (int i = 1; i < testData.messages().size(); i += 2) {
                badDeviceIds.add(testData.messages().get(i).deviceId());
            }

            AtomicInteger errorHandlerCount = new AtomicInteger();
            AtomicInteger processedCount = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class)
                                    .errorOccurredHandler((source, target, item, ex) ->
                                            errorHandlerCount.incrementAndGet()))
                            .to(message -> {
                                if (badDeviceIds.contains(message.deviceId())) {
                                    throw new RuntimeException("Simulated failure: " + message.deviceId());
                                }

                                processedCount.incrementAndGet();
                            });

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(badDeviceIds.size(), errorHandlerCount.get());
                    Assertions.assertEquals(testData.messages().size() - badDeviceIds.size(), processedCount.get());
                    Assertions.assertTrue(verifier.errorCount() > 0);
                } finally {
                    executor.shutdown();
                }
            }
        }

        @Test
        void syncTransformThrows_propagatesExceptionToCallingThread() {
            AtomicInteger processedCount = new AtomicInteger();

            Message badMessage = generateMessages(1).messages().get(0);

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                Pipeline<Message> pipeline = Pipeline.<Message>builder()
                        .from(Transform.of(Message.class)
                                .transform(message -> {
                                    if (message.deviceId().equals(badMessage.deviceId())) {
                                        throw new RuntimeException("Simulated failure");
                                    }

                                    return message;
                                }))
                        .to(message -> processedCount.incrementAndGet());

                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> pipeline.post(badMessage)
                );

                Assertions.assertEquals(0, processedCount.get());
                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }

    @Nested
    class BranchContract {
        @Test
        void branchWithoutOtherwise_throwsIllegalArgumentException() {
            Pipeline<Message> whenTarget = Pipeline.<Message>builder()
                    .from(message -> {
                    });

            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> Pipeline.<Message>builder()
                            .from(Branch.of(Message.class)
                                    .when(message -> true, whenTarget))
            );
        }
    }

    @Nested
    class PriorityOrdering {
        @Test
        void priorityBuffer_highPriorityDequeued_beforeLow() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(9);

            CountDownLatch gate = new CountDownLatch(1);
            List<Message> received = new CopyOnWriteArrayList<>();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(PriorityBuffer.of(Message.class)
                                    .comparator(Comparator.comparing(message -> switch (message.priority()) {
                                        case High -> 1;
                                        case Medium -> 2;
                                        case Low -> 3;
                                    })))
                            .to(message -> {
                                try {
                                    gate.await();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }

                                received.add(message);
                            });

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    gate.countDown();

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(9, received.size());

                    for (int i = 0; i < 3; i++) {
                        Assertions.assertEquals(Priority.High, received.get(i).priority());
                    }

                    for (int i = 3; i < 6; i++) {
                        Assertions.assertEquals(Priority.Medium, received.get(i).priority());
                    }

                    for (int i = 6; i < 9; i++) {
                        Assertions.assertEquals(Priority.Low, received.get(i).priority());
                    }
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }

    @Nested
    class EdgeCases {
        @Test
        void zeroMessages_completeAndAwait_completionResolves() {
            NamedExecutorService executor = newExecutor();

            AtomicInteger processedCount = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class))
                            .to(message -> processedCount.incrementAndGet());

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(0, processedCount.get());
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void singleMessage_processedCorrectly() {
            NamedExecutorService executor = newExecutor();

            AtomicInteger processedCount = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class))
                            .to(message -> processedCount.incrementAndGet());

                    pipeline.post(generateMessages(1).messages().get(0));

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(1, processedCount.get());
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }

    @Nested
    class AwaitCompletion {
        @Test
        void awaitWithTimeout_pipelineDrains_returnsTrue() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(10);
            AtomicInteger processedCount = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class))
                            .to(message -> processedCount.incrementAndGet());

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();

                    boolean result = pipeline.awaitCompletion(Duration.ofSeconds(10));

                    Assertions.assertTrue(result);
                    Assertions.assertEquals(testData.messages().size(), processedCount.get());
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void awaitWithTimeout_timeoutExceeds_returnsFalse() {
            NamedExecutorService executor = newExecutor();

            CountDownLatch releaseAction = new CountDownLatch(1);

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class))
                            .to(message -> {
                                try {
                                    releaseAction.await();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });

                    pipeline.post(generateMessages(1).messages().get(0));

                    pipeline.complete();

                    boolean result = pipeline.awaitCompletion(Duration.ofMillis(50));

                    Assertions.assertFalse(result);
                } finally {
                    releaseAction.countDown();
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }

    @Nested
    class TapStage {
        @Test
        void tap_consumerInvoked_itemPassesThroughToAction() {
            TestData testData = generateMessages(100);

            List<String> tapped = new CopyOnWriteArrayList<>();
            AtomicInteger processedCount = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                Pipeline<Message> pipeline = Pipeline.<Message>builder()
                        .from(Tap.of(Message.class)
                                .consumer(message -> tapped.add(message.deviceId())))
                        .to(message -> processedCount.incrementAndGet());

                for (Message message : testData.messages()) {
                    pipeline.post(message);
                }

                pipeline.complete();
                pipeline.awaitCompletion();

                Assertions.assertEquals(testData.messages().size(), tapped.size());
                Assertions.assertEquals(testData.messages().size(), processedCount.get());
                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void tap_withUpstreamBuffer_allItemsProcessed() {
            NamedExecutorService executor = newExecutor();

            TestData testData = generateMessages(1000);

            List<String> tapped = new CopyOnWriteArrayList<>();
            AtomicInteger processedCount = new AtomicInteger();

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                try {
                    Pipeline<Message> pipeline = Pipeline.<Message>builder()
                            .executor(executor)
                            .from(Buffer.of(Message.class))
                            .then(Tap.of(Message.class)
                                    .consumer(message -> tapped.add(message.deviceId())))
                            .to(message -> processedCount.incrementAndGet());

                    for (Message message : testData.messages()) {
                        pipeline.post(message);
                    }

                    pipeline.complete();
                    pipeline.awaitCompletion();

                    Assertions.assertEquals(testData.messages().size(), tapped.size());
                    Assertions.assertEquals(testData.messages().size(), processedCount.get());
                } finally {
                    executor.shutdown();
                }

                Assertions.assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void tap_consumerThrows_exceptionPropagatesToCallingThread() {
            AtomicInteger processedCount = new AtomicInteger();

            Message badMessage = generateMessages(1).messages().get(0);

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .build()) {
                Pipeline<Message> pipeline = Pipeline.<Message>builder()
                        .from(Tap.of(Message.class)
                                .consumer(message -> {
                                    if (message.deviceId().equals(badMessage.deviceId())) {
                                        throw new RuntimeException("consumer failure");
                                    }
                                }))
                        .to(message -> processedCount.incrementAndGet());

                Assertions.assertThrows(
                        RuntimeException.class,
                        () -> pipeline.post(badMessage)
                );

                Assertions.assertEquals(0, processedCount.get());
                Assertions.assertEquals(0, verifier.errorCount());
            }
        }
    }
}