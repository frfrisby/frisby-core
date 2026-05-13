package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ItemPostedManager}.
 */
class ItemPostedManagerTest {
    @Nested
    class SendOnPostedNotification {
        @Test
        void nullHandler_isNoOp() {
            ItemPostedManager<String> manager = new ItemPostedManager<>(
                    new Object(),
                    new EventSource("TestBlock"),
                    null
            );

            // No handler configured — must not throw and must not log anything.
            try (SystemLogVerifier verifier = SystemLogVerifier.builder().build()) {
                assertDoesNotThrow(
                        () -> manager.sendOnPostedNotification("input", true)
                );

                assertEquals(0, verifier.errorCount());
            }
        }

        @Test
        void handlerThrows_exceptionIsSwallowed() {
            ItemPostedManager<String> manager = new ItemPostedManager<>(
                    new Object(),
                    new EventSource("TestBlock"),
                    (source, input, accepted) -> {
                        throw new RuntimeException("handler error");
                    }
            );

            // The handler's exception must be caught internally — this call must not throw.
            assertDoesNotThrow(
                    () -> manager.sendOnPostedNotification("input", true)
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
                                    "An unexpected exception occurred in the TestBlock while invoking the ItemPostedHandler.onPosted() method.")
                                    && e.thrown() == handlerError)
                            .build()
                    )
                    .build()) {
                ItemPostedManager<String> manager = new ItemPostedManager<>(
                        new Object(),
                        new EventSource("TestBlock"),
                        (source, input, accepted) -> {
                            throw handlerError;
                        }
                );

                manager.sendOnPostedNotification("input", true);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }
    }
}

