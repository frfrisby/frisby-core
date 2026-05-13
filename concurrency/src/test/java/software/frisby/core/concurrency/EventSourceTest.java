package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.log.LogExpectation;
import software.frisby.core.concurrency.log.SystemLogVerifier;
import software.frisby.core.validation.BlankValueException;
import software.frisby.core.validation.NullValueException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EventSource}.  Every method and every branch of the
 * {@code isLoggable()} guards and the private {@code formatItemName()} helper are covered.
 */
class EventSourceTest {
    private static final String SOURCE = "TestBlock";
    private static final EventSource EVENT_SOURCE = new EventSource(SOURCE);

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Nested
    class Constructor {
        @Test
        void nullSource_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> new EventSource(null));
        }

        @Test
        void emptySource_throwsBlankValueException() {
            assertThrows(BlankValueException.class, () -> new EventSource(""));
        }

        @Test
        void blankSource_throwsBlankValueException() {
            assertThrows(BlankValueException.class, () -> new EventSource("   "));
        }

        @Test
        void validSource_createsInstance() {
            assertDoesNotThrow(() -> new EventSource(SOURCE));
        }

        @Test
        void sourceName_returnsSourcePassedToConstructor() {
            EventSource eventSource = new EventSource("MyBlock");

            assertEquals("MyBlock", eventSource.sourceName());
        }
    }

    // -------------------------------------------------------------------------
    // createNoTargetLinkedWarningEvent
    // -------------------------------------------------------------------------

    @Nested
    class CreateNoTargetLinkedWarningEvent {
        @Test
        void logsWarningMessage() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.WARNING)
                            .predicate(e -> e.message().contains("TestBlock")
                                    && e.message().contains("no downstream target linked")
                                    && e.message().contains("linkTo()"))
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createNoTargetLinkedWarningEvent();

                verifier.assertExpectations();
                assertEquals(1, verifier.warningCount());
            }
        }

        @Test
        void whenLevelNotLoggable_doesNotLog() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .configure(EventSource.class, System.Logger.Level.OFF)
                    .build()) {
                EVENT_SOURCE.createNoTargetLinkedWarningEvent();

                assertEquals(0, verifier.warningCount());
            }
        }
    }

    // -------------------------------------------------------------------------
    // createOnPostedNotificationErrorEvent
    // -------------------------------------------------------------------------

    @Nested
    class CreateOnPostedNotificationErrorEvent {
        @Test
        void exception_logsErrorMessageWithThrowable() {
            RuntimeException ex = new RuntimeException("posted error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("TestBlock")
                                    && e.message().contains("ItemPostedHandler.onPosted()")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createOnPostedNotificationErrorEvent(ex);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }

        @Test
        void whenLevelNotLoggable_doesNotLog() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .configure(EventSource.class, System.Logger.Level.OFF)
                    .build()) {
                EVENT_SOURCE.createOnPostedNotificationErrorEvent(new RuntimeException());

                assertEquals(0, verifier.errorCount());
            }
        }
    }

    // -------------------------------------------------------------------------
    // createOnDeliveredNotificationErrorEvent
    // -------------------------------------------------------------------------

    @Nested
    class CreateOnDeliveredNotificationErrorEvent {
        @Test
        void exception_logsErrorMessageWithThrowable() {
            RuntimeException ex = new RuntimeException("delivered error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("TestBlock")
                                    && e.message().contains("ItemDeliveredHandler.onDelivered()")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createOnDeliveredNotificationErrorEvent(ex);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }

        @Test
        void whenLevelNotLoggable_doesNotLog() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .configure(EventSource.class, System.Logger.Level.OFF)
                    .build()) {
                EVENT_SOURCE.createOnDeliveredNotificationErrorEvent(new RuntimeException());

                assertEquals(0, verifier.errorCount());
            }
        }
    }

    // -------------------------------------------------------------------------
    // createTargetPredicateErrorEvent  (also exercises all branches of formatItemName)
    // -------------------------------------------------------------------------

    @Nested
    class CreateTargetPredicateErrorEvent {
        @Test
        void stringItem_logsErrorMessageWithThrowable() {
            RuntimeException ex = new RuntimeException("predicate error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("TestBlock")
                                    && e.message().contains("linked target 3")
                                    && e.message().contains("hello")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createTargetPredicateErrorEvent("hello", 3, ex);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }

        @Test
        void nullItem_logsNullInMessage() {
            RuntimeException ex = new RuntimeException("predicate error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("null")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createTargetPredicateErrorEvent(null, 1, ex);

                verifier.assertExpectations();
            }
        }

        @Test
        void arrayItem_logsArrayRepresentationInMessage() {
            RuntimeException ex = new RuntimeException("predicate error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("int[]{1, 2, 3}")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createTargetPredicateErrorEvent(new int[]{1, 2, 3}, 1, ex);

                verifier.assertExpectations();
            }
        }

        @Test
        void largeArrayItem_truncatesArrayRepresentation() {
            int[] large = new int[50];
            Arrays.fill(large, Integer.MAX_VALUE);

            RuntimeException ex = new RuntimeException("predicate error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains(", ...")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createTargetPredicateErrorEvent(large, 1, ex);

                verifier.assertExpectations();
            }
        }

        @Test
        void whenLevelNotLoggable_doesNotLog() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .configure(EventSource.class, System.Logger.Level.OFF)
                    .build()) {
                EVENT_SOURCE.createTargetPredicateErrorEvent("hello", 3, new RuntimeException());

                assertEquals(0, verifier.errorCount());
            }
        }
    }

    // -------------------------------------------------------------------------
    // createErrorEvent
    // -------------------------------------------------------------------------

    @Nested
    class CreateErrorEvent {
        @Test
        void throwable_logsErrorMessageWithThrowable() {
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
                EVENT_SOURCE.createErrorEvent(error);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }

        @Test
        void whenLevelNotLoggable_doesNotLog() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .configure(EventSource.class, System.Logger.Level.OFF)
                    .build()) {
                EVENT_SOURCE.createErrorEvent(new RuntimeException());

                assertEquals(0, verifier.errorCount());
            }
        }
    }

    // -------------------------------------------------------------------------
    // createOnErrorNotificationErrorEvent
    // -------------------------------------------------------------------------

    @Nested
    class CreateOnErrorNotificationErrorEvent {
        @Test
        void exception_logsErrorMessageWithThrowable() {
            RuntimeException ex = new RuntimeException("error delegate error");

            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .expect(LogExpectation.builder()
                            .logger(EventSource.class)
                            .level(System.Logger.Level.ERROR)
                            .predicate(e -> e.message().contains("TestBlock")
                                    && e.message().contains("ErrorOccurredHandler.onError()")
                                    && e.thrown() == ex)
                            .build()
                    )
                    .build()) {
                EVENT_SOURCE.createOnErrorNotificationErrorEvent(ex);

                verifier.assertExpectations();
                assertEquals(1, verifier.errorCount());
            }
        }

        @Test
        void whenLevelNotLoggable_doesNotLog() {
            try (SystemLogVerifier verifier = SystemLogVerifier.builder()
                    .configure(EventSource.class, System.Logger.Level.OFF)
                    .build()) {
                EVENT_SOURCE.createOnErrorNotificationErrorEvent(new RuntimeException());

                assertEquals(0, verifier.errorCount());
            }
        }
    }
}

