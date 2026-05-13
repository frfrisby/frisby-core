package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValuesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_ALLOWED_MSG = "The 'allowed' value was not provided.";
    private static final String EMPTY_ALLOWED_MSG = "The 'allowed' value of '0' is invalid. The value must be greater than or equal to '1'.";
    private static final String NOT_IN_SET_MSG = "The 'field' value of 'C' is invalid. The value must be one of: 'A', 'B'.";
    private static final String NULL_DISALLOWED_MSG = "The 'disallowed' value was not provided.";
    private static final String EMPTY_DISALLOWED_MSG = "The 'disallowed' value of '0' is invalid. The value must be greater than or equal to '1'.";
    private static final String IN_DISALLOWED_SET_MSG = "The 'field' value of 'A' is invalid. The value must not be one of: 'A', 'B'.";

    private enum Direction {NORTH, SOUTH}

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.notNull(null, "value"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.notNull("   ", "value"));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Values.notNull("field", (String) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullString_returnsSameReference() {
            var value = "hello";
            assertSame(value, Values.notNull("field", value));
        }

        @Test
        void nonNullEnum_returnsSameReference() {
            assertSame(Direction.NORTH, Values.notNull("field", Direction.NORTH));
        }

        @Test
        void nonNullObject_returnsSameReference() {
            var value = new Object();
            assertSame(value, Values.notNull("field", value));
        }
    }

    @Nested
    class OneOf {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.oneOf(null, "A", Set.of("A", "B")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.oneOf("   ", "A", Set.of("A", "B")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullAllowed_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.oneOf("field", "A", null));
            assertEquals(NULL_ALLOWED_MSG, ex.getMessage());
        }

        @Test
        void emptyAllowed_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Values.oneOf("field", "A", Set.of()));
            assertEquals(EMPTY_ALLOWED_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Values.oneOf("field", null, Set.of("A", "B")));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueNotInSet_throwsDisallowedValueException() {
            var ex = assertThrows(DisallowedValueException.class, () -> Values.oneOf("field", "C", Set.of("A", "B")));
            assertEquals(NOT_IN_SET_MSG, ex.getMessage());
        }

        @Test
        void valueInSet_returnsSameReference() {
            var value = "A";
            assertSame(value, Values.oneOf("field", value, Set.of("A", "B")));
        }

        @Test
        void allowedListSortedLexicographically_messageShowsSortedOrder() {
            var ex = assertThrows(DisallowedValueException.class, () -> Values.oneOf("field", "X", Set.of("Z", "A", "M")));
            assertEquals("The 'field' value of 'X' is invalid. The value must be one of: 'A', 'M', 'Z'.", ex.getMessage());
        }

        @Test
        void enumValueInSet_returnsSameReference() {
            assertSame(Direction.NORTH, Values.oneOf("field", Direction.NORTH, Set.of(Direction.NORTH, Direction.SOUTH)));
        }
    }

    @Nested
    class OptionalOneOf {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.optionalOneOf(null, "A", Set.of("A", "B")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.optionalOneOf("   ", "A", Set.of("A", "B")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullAllowed_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.optionalOneOf("field", "A", null));
            assertEquals(NULL_ALLOWED_MSG, ex.getMessage());
        }

        @Test
        void emptyAllowed_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Values.optionalOneOf("field", "A", Set.of()));
            assertEquals(EMPTY_ALLOWED_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Values.optionalOneOf("field", null, Set.of("A", "B")));
        }

        @Test
        void valueNotInSet_throwsDisallowedValueException() {
            var ex = assertThrows(DisallowedValueException.class, () -> Values.optionalOneOf("field", "C", Set.of("A", "B")));
            assertEquals(NOT_IN_SET_MSG, ex.getMessage());
        }

        @Test
        void valueInSet_returnsSameReference() {
            var value = "B";
            assertSame(value, Values.optionalOneOf("field", value, Set.of("A", "B")));
        }
    }

    @Nested
    class NotOneOf {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.notOneOf(null, "A", Set.of("A", "B")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.notOneOf("   ", "A", Set.of("A", "B")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullDisallowed_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.notOneOf("field", "A", null));
            assertEquals(NULL_DISALLOWED_MSG, ex.getMessage());
        }

        @Test
        void emptyDisallowed_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Values.notOneOf("field", "A", Set.of()));
            assertEquals(EMPTY_DISALLOWED_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Values.notOneOf("field", null, Set.of("A", "B")));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueInDisallowedSet_throwsDisallowedValueException() {
            var ex = assertThrows(DisallowedValueException.class, () -> Values.notOneOf("field", "A", Set.of("A", "B")));
            assertEquals(IN_DISALLOWED_SET_MSG, ex.getMessage());
        }

        @Test
        void valueNotInDisallowedSet_returnsSameReference() {
            var value = "C";
            assertSame(value, Values.notOneOf("field", value, Set.of("A", "B")));
        }

        @Test
        void enumSentinelInDisallowedSet_throwsDisallowedValueException() {
            var ex = assertThrows(DisallowedValueException.class,
                    () -> Values.notOneOf("field", Direction.NORTH, Set.of(Direction.NORTH)));
            assertEquals(
                    "The 'field' value of 'NORTH' is invalid. The value must not be one of: 'NORTH'.",
                    ex.getMessage()
            );
        }

        @Test
        void enumValueNotInDisallowedSet_returnsSameReference() {
            assertSame(Direction.SOUTH, Values.notOneOf("field", Direction.SOUTH, Set.of(Direction.NORTH)));
        }
    }

    @Nested
    class OptionalNotOneOf {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.optionalNotOneOf(null, "A", Set.of("A", "B")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.optionalNotOneOf("   ", "A", Set.of("A", "B")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullDisallowed_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Values.optionalNotOneOf("field", "A", null));
            assertEquals(NULL_DISALLOWED_MSG, ex.getMessage());
        }

        @Test
        void emptyDisallowed_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Values.optionalNotOneOf("field", "A", Set.of()));
            assertEquals(EMPTY_DISALLOWED_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Values.optionalNotOneOf("field", null, Set.of("A", "B")));
        }

        @Test
        void valueInDisallowedSet_throwsDisallowedValueException() {
            var ex = assertThrows(DisallowedValueException.class, () -> Values.optionalNotOneOf("field", "A", Set.of("A", "B")));
            assertEquals(IN_DISALLOWED_SET_MSG, ex.getMessage());
        }

        @Test
        void valueNotInDisallowedSet_returnsSameReference() {
            var value = "C";
            assertSame(value, Values.optionalNotOneOf("field", value, Set.of("A", "B")));
        }
    }
}

