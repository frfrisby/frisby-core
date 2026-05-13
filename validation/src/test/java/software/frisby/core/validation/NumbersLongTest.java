package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersLongTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, 5L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", 5L));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (Long) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsUnboxedValue() {
            assertEquals(5L, Numbers.notNull("field", 5L));
        }
    }

    @Nested
    class Min {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, 5L, 5L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", 5L, 5L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", 4L, 5L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5L, Numbers.min("field", 5L, 5L));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10L, Numbers.min("field", 10L, 5L));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (Long) 5L, 5L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (Long) 5L, 5L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", (Long) null, 5L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (Long) 4L, 5L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5L, Numbers.min("field", (Long) 5L, 5L));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10L, Numbers.min("field", (Long) 10L, 5L));
            }
        }
    }

    @Nested
    class Max {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", 11L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10L, Numbers.max("field", 10L, 10L));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5L, Numbers.max("field", 5L, 10L));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (Long) 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (Long) 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", (Long) null, 10L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (Long) 11L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10L, Numbers.max("field", (Long) 10L, 10L));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5L, Numbers.max("field", (Long) 5L, 10L));
            }
        }
    }

    @Nested
    class MinExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, 6L, 5L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", 6L, 5L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 4L, 5L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 5L, 5L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6L, Numbers.minExclusive("field", 6L, 5L));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (Long) 6L, 5L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (Long) 6L, 5L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", (Long) null, 5L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Long) 4L, 5L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Long) 5L, 5L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6L, Numbers.minExclusive("field", (Long) 6L, 5L));
            }
        }
    }

    @Nested
    class MaxExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 11L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 10L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9L, Numbers.maxExclusive("field", 9L, 10L));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (Long) 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (Long) 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", (Long) null, 10L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Long) 11L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Long) 10L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9L, Numbers.maxExclusive("field", (Long) 9L, 10L));
            }
        }
    }

    @Nested
    class Range {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", 7L, 5L, 4L));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 4L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5L, Numbers.range("field", 5L, 5L, 10L));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.range("field", 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10L, Numbers.range("field", 10L, 5L, 10L));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 11L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (Long) 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (Long) 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (Long) 7L, 5L, 4L));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", (Long) null, 5L, 10L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Long) 4L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5L, Numbers.range("field", (Long) 5L, 5L, 10L));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.range("field", (Long) 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10L, Numbers.range("field", (Long) 10L, 5L, 10L));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Long) 11L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class ExclusiveRange {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7L, 5L, 5L));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7L, 5L, 4L));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 5L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.exclusiveRange("field", 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 10L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (Long) 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (Long) 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (Long) 7L, 5L, 5L));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", (Long) null, 5L, 10L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Long) 5L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.exclusiveRange("field", (Long) 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Long) 10L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class RangeExclusiveMax {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", 7L, 5L, 5L));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 4L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5L, Numbers.rangeExclusiveMax("field", 5L, 5L, 10L));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.rangeExclusiveMax("field", 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 10L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (Long) 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (Long) 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (Long) 7L, 5L, 5L));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", (Long) null, 5L, 10L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Long) 4L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5L, Numbers.rangeExclusiveMax("field", (Long) 5L, 5L, 10L));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.rangeExclusiveMax("field", (Long) 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Long) 10L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class RangeExclusiveMin {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", 7L, 5L, 5L));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 5L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.rangeExclusiveMin("field", 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10L, Numbers.rangeExclusiveMin("field", 10L, 5L, 10L));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 11L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (Long) 7L, 5L, 10L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (Long) 7L, 5L, 10L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (Long) 7L, 5L, 5L));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", (Long) null, 5L, 10L));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Long) 5L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7L, Numbers.rangeExclusiveMin("field", (Long) 7L, 5L, 10L));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10L, Numbers.rangeExclusiveMin("field", (Long) 10L, 5L, 10L));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Long) 11L, 5L, 10L));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, 5L, 5L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", (Long) null, 5L));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", 4L, 5L));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5L, Numbers.optionalMin("field", 5L, 5L));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(10L, Numbers.optionalMin("field", 10L, 5L));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, 5L, 10L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", (Long) null, 10L));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", 11L, 10L));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10L, Numbers.optionalMax("field", 10L, 10L));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(5L, Numbers.optionalMax("field", 5L, 10L));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, 6L, 5L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", (Long) null, 5L));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", 5L, 5L));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(6L, Numbers.optionalMinExclusive("field", 6L, 5L));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, 5L, 10L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", (Long) null, 10L));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", 10L, 10L));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(9L, Numbers.optionalMaxExclusive("field", 9L, 10L));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, 7L, 5L, 10L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", 7L, 5L, 4L));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", (Long) null, 5L, 10L));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 4L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5L, Numbers.optionalRange("field", 5L, 5L, 10L));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7L, Numbers.optionalRange("field", 7L, 5L, 10L));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10L, Numbers.optionalRange("field", 10L, 5L, 10L));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 11L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, 7L, 5L, 10L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", 7L, 5L, 5L));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", (Long) null, 5L, 10L));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 5L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7L, Numbers.optionalExclusiveRange("field", 7L, 5L, 10L));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 10L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, 7L, 5L, 10L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", 7L, 5L, 5L));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", (Long) null, 5L, 10L));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 4L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5L, Numbers.optionalRangeExclusiveMax("field", 5L, 5L, 10L));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7L, Numbers.optionalRangeExclusiveMax("field", 7L, 5L, 10L));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 10L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, 7L, 5L, 10L));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", 7L, 5L, 5L));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", (Long) null, 5L, 10L));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 5L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7L, Numbers.optionalRangeExclusiveMin("field", 7L, 5L, 10L));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10L, Numbers.optionalRangeExclusiveMin("field", 10L, 5L, 10L));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 11L, 5L, 10L));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class Positive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, 1L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", 1L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", 0L));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", -1L));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals(1L, Numbers.positive("field", 1L));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10L, Numbers.positive("field", 10L));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (Long) 1L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (Long) 1L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.positive("field", (Long) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Long) 0L));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Long) (-1L)));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals(1L, Numbers.positive("field", (Long) 1L));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10L, Numbers.positive("field", (Long) 10L));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive(null, (Long) 1L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive("   ", (Long) 1L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalPositive("field", (Long) null));
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", (Long) 0L));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(5L, Numbers.optionalPositive("field", (Long) 5L));
            }
        }
    }

    @Nested
    class NotNegative {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, 0L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", 0L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", -1L));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0L, Numbers.notNegative("field", 0L));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10L, Numbers.notNegative("field", 10L));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (Long) 0L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (Long) 0L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (Long) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Long) (-1L)));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0L, Numbers.notNegative("field", (Long) 0L));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10L, Numbers.notNegative("field", (Long) 10L));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative(null, (Long) 0L));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative("   ", (Long) 0L));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalNotNegative("field", (Long) null));
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", (Long) (-1L)));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0L, Numbers.optionalNotNegative("field", (Long) 0L));
            }
        }
    }

}










