package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ErrorOccurredManager}.
 */
class ErrorOccurredManagerTest {
    @Nested
    class HasHandler {
        @Test
        void hasHandler_withNullHandler_returnsFalse() {
            ErrorOccurredManager<String> manager = new ErrorOccurredManager<>(
                    new Object(),
                    new EventSource("test"),
                    null
            );

            assertFalse(manager.hasHandler());
        }

        @Test
        void hasHandler_withHandler_returnsTrue() {
            ErrorOccurredManager<String> manager = new ErrorOccurredManager<>(
                    new Object(),
                    new EventSource("test"),
                    (source, target, item, error) -> {
                    }
            );

            assertTrue(manager.hasHandler());
        }
    }

    @Nested
    class SendOnErrorNotification {
        @Test
        void sendOnErrorNotification_handlerThrows_exceptionIsSwallowed() {
            ErrorOccurredManager<String> manager = new ErrorOccurredManager<>(
                    new Object(),
                    new EventSource("test"),
                    (source, target, item, error) -> {
                        throw new RuntimeException("handler error");
                    }
            );

            // The handler's exception must be caught internally — this call must not throw.
            assertDoesNotThrow(
                    () -> manager.sendOnErrorNotification("target", "item", new RuntimeException("original"))
            );
        }

        @Test
        void sendOnErrorNotification_noHandler_eventIsLogged() {
            RuntimeException error = new RuntimeException("unexpected");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("An unexpected exception occurred in the TestBlock.")
                                    && e.thrown() == error)
                            .build()
                    )
                    .build()) {
                ErrorOccurredManager<String> manager = new ErrorOccurredManager<>(
                        new Object(),
                        new EventSource("TestBlock"),
                        null
                );


                manager.sendOnErrorNotification("target", "item", error);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }
    }
}
