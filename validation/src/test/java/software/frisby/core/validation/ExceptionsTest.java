package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {
    // -------------------------------------------------------------------------
    // BlankValueException
    // -------------------------------------------------------------------------

    @Nested
    class BlankValue {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new BlankValueException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new BlankValueException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new BlankValueException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new BlankValueException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new BlankValueException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new BlankValueException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new BlankValueException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new BlankValueException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // DisallowedValueException
    // -------------------------------------------------------------------------

    @Nested
    class DisallowedValue {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new DisallowedValueException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new DisallowedValueException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new DisallowedValueException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new DisallowedValueException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new DisallowedValueException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new DisallowedValueException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new DisallowedValueException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new DisallowedValueException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // DuplicateElementsException
    // -------------------------------------------------------------------------

    @Nested
    class DuplicateElements {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new DuplicateElementsException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new DuplicateElementsException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new DuplicateElementsException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new DuplicateElementsException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new DuplicateElementsException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new DuplicateElementsException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new DuplicateElementsException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new DuplicateElementsException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // DurationOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class DurationOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new DurationOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new DurationOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new DurationOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new DurationOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new DurationOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new DurationOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new DurationOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new DurationOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // EmptyValueException
    // -------------------------------------------------------------------------

    @Nested
    class EmptyValue {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new EmptyValueException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new EmptyValueException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new EmptyValueException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new EmptyValueException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new EmptyValueException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new EmptyValueException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new EmptyValueException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new EmptyValueException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // IllegalConfigurationException
    // -------------------------------------------------------------------------

    @Nested
    class IllegalConfiguration {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new IllegalConfigurationException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new IllegalConfigurationException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new IllegalConfigurationException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new IllegalConfigurationException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new IllegalConfigurationException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new IllegalConfigurationException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new IllegalConfigurationException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new IllegalConfigurationException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // InstantOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class InstantOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new InstantOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new InstantOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new InstantOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new InstantOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new InstantOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new InstantOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new InstantOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new InstantOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // LocalDateOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class LocalDateOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new LocalDateOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new LocalDateOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new LocalDateOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new LocalDateOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new LocalDateOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new LocalDateOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new LocalDateOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new LocalDateOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // LocalDateTimeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class LocalDateTimeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new LocalDateTimeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new LocalDateTimeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new LocalDateTimeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new LocalDateTimeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new LocalDateTimeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new LocalDateTimeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new LocalDateTimeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new LocalDateTimeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // LocalTimeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class LocalTimeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new LocalTimeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new LocalTimeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new LocalTimeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new LocalTimeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new LocalTimeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new LocalTimeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new LocalTimeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new LocalTimeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // MapSizeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class MapSizeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new MapSizeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new MapSizeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new MapSizeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new MapSizeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new MapSizeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new MapSizeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new MapSizeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new MapSizeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // MissingElementsException
    // -------------------------------------------------------------------------

    @Nested
    class MissingElements {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new MissingElementsException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new MissingElementsException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new MissingElementsException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new MissingElementsException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new MissingElementsException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new MissingElementsException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new MissingElementsException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new MissingElementsException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // MissingFieldException
    // -------------------------------------------------------------------------

    @Nested
    class MissingField {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new MissingFieldException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new MissingFieldException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new MissingFieldException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new MissingFieldException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new MissingFieldException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new MissingFieldException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new MissingFieldException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new MissingFieldException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // NullElementException
    // -------------------------------------------------------------------------

    @Nested
    class NullElement {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new NullElementException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new NullElementException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new NullElementException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullElementException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullElementException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullElementException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new NullElementException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullElementException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // NullMapKeyException
    // -------------------------------------------------------------------------

    @Nested
    class NullMapKey {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new NullMapKeyException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new NullMapKeyException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new NullMapKeyException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullMapKeyException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullMapKeyException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullMapKeyException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new NullMapKeyException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullMapKeyException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // NullMapValueException
    // -------------------------------------------------------------------------

    @Nested
    class NullMapValue {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new NullMapValueException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new NullMapValueException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new NullMapValueException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullMapValueException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullMapValueException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullMapValueException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new NullMapValueException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullMapValueException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // NullValueException
    // -------------------------------------------------------------------------

    @Nested
    class NullValue {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new NullValueException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new NullValueException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new NullValueException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullValueException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullValueException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NullValueException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new NullValueException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NullValueException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // NumericValueOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class NumericValueOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new NumericValueOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new NumericValueOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new NumericValueOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NumericValueOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NumericValueOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new NumericValueOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new NumericValueOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new NumericValueOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // OffsetDateTimeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class OffsetDateTimeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new OffsetDateTimeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new OffsetDateTimeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new OffsetDateTimeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new OffsetDateTimeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new OffsetDateTimeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new OffsetDateTimeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new OffsetDateTimeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new OffsetDateTimeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // OffsetTimeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class OffsetTimeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new OffsetTimeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new OffsetTimeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new OffsetTimeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new OffsetTimeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new OffsetTimeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new OffsetTimeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new OffsetTimeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new OffsetTimeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // PatternMismatchException
    // -------------------------------------------------------------------------

    @Nested
    class PatternMismatch {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new PatternMismatchException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new PatternMismatchException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new PatternMismatchException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new PatternMismatchException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new PatternMismatchException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new PatternMismatchException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new PatternMismatchException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new PatternMismatchException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // PeriodOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class PeriodOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new PeriodOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new PeriodOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new PeriodOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new PeriodOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new PeriodOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new PeriodOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new PeriodOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new PeriodOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // SequenceSizeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class SequenceSizeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new SequenceSizeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new SequenceSizeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new SequenceSizeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new SequenceSizeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new SequenceSizeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new SequenceSizeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new SequenceSizeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new SequenceSizeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // StringLengthOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class StringLengthOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new StringLengthOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new StringLengthOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new StringLengthOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new StringLengthOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new StringLengthOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new StringLengthOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new StringLengthOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new StringLengthOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // TooManyFieldsException
    // -------------------------------------------------------------------------

    @Nested
    class TooManyFields {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new TooManyFieldsException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new TooManyFieldsException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new TooManyFieldsException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new TooManyFieldsException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new TooManyFieldsException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new TooManyFieldsException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new TooManyFieldsException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new TooManyFieldsException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // ZonedDateTimeOutsideRangeException
    // -------------------------------------------------------------------------

    @Nested
    class ZonedDateTimeOutsideRange {
        @Nested
        class NoArg {
            @Test
            void messageIsNull() {
                assertNull(new ZonedDateTimeOutsideRangeException().getMessage());
            }
        }

        @Nested
        class Message {
            @Test
            void setsMessage() {
                assertEquals("msg", new ZonedDateTimeOutsideRangeException("msg").getMessage());
            }
        }

        @Nested
        class MessageAndCause {
            @Test
            void setsMessage() {
                var cause = new RuntimeException("root");

                assertEquals("msg", new ZonedDateTimeOutsideRangeException("msg", cause).getMessage());
            }

            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new ZonedDateTimeOutsideRangeException("msg", cause).getCause());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new ZonedDateTimeOutsideRangeException("msg", null);

                assertEquals("msg", ex.getMessage());
                assertNull(ex.getCause());
            }
        }

        @Nested
        class Cause {
            @Test
            void setsCause() {
                var cause = new RuntimeException("root");

                assertSame(cause, new ZonedDateTimeOutsideRangeException(cause).getCause());
            }

            @Test
            void messageIsCauseToString() {
                var cause = new RuntimeException("root");

                assertEquals(cause.toString(), new ZonedDateTimeOutsideRangeException(cause).getMessage());
            }

            @Test
            void nullCauseIsPermitted() {
                var ex = new ZonedDateTimeOutsideRangeException((Throwable) null);

                assertNull(ex.getCause());
                assertNull(ex.getMessage());
            }
        }
    }
}

