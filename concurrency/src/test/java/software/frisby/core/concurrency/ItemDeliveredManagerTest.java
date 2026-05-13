package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ItemDeliveredManager}.
 */
class ItemDeliveredManagerTest {
    @Nested
    class SendOnDeliveredNotification {
        @Test
        void nullHandler_isNoOp() {
            ItemDeliveredManager<String> manager = new ItemDeliveredManager<>(
                    new Object(),
                    new EventSource("TestBlock"),
                    null
            );

            // No handler configured — must not throw and must not log anything.
            try (SystemLogVerifier verifier = SystemLogVerifier.builder().build()) {
                assertDoesNotThrow(
                        () -> manager.sendOnDeliveredNotification("target", "output")
                );

                assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void handlerThrows_exceptionIsSwallowed() {
            ItemDeliveredManager<String> manager = new ItemDeliveredManager<>(
                    new Object(),
                    new EventSource("TestBlock"),
                    (source, target, output) -> {
                        throw new RuntimeException("handler error");
                    }
            );

            // The handler's exception must be caught internally — this call must not throw.
            assertDoesNotThrow(
                    () -> manager.sendOnDeliveredNotification("target", "output")
            );
        }

        @Test
        void handlerThrows_logsErrorViaEventSource() {
            RuntimeException handlerError = new RuntimeException("handler error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains(
                                    "An unexpected exception occurred in the TestBlock while invoking the ItemDeliveredHandler.onDelivered() method.")
                                    && e.thrown() == handlerError)
                            .build()
                    )
                    .build()) {
                ItemDeliveredManager<String> manager = new ItemDeliveredManager<>(
                        new Object(),
                        new EventSource("TestBlock"),
                        (source, target, output) -> {
                            throw handlerError;
                        }
                );

                manager.sendOnDeliveredNotification("target", "output");

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }
    }
}

