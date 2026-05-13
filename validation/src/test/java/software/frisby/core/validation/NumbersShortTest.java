package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersShortTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, (short) 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", (short) 5));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (Short) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsUnboxedValue() {
            assertEquals((short) 5, Numbers.notNull("field", (short) 5));
        }
    }

    @Nested
    class Min {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (short) 5, (short) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (short) 5, (short) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (short) 4, (short) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((short) 5, Numbers.min("field", (short) 5, (short) 5));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((short) 10, Numbers.min("field", (short) 10, (short) 5));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (Short) (short) 5, (short) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (Short) (short) 5, (short) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", (Short) null, (short) 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (Short) (short) 4, (short) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((short) 5, Numbers.min("field", (Short) (short) 5, (short) 5));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((short) 10, Numbers.min("field", (Short) (short) 10, (short) 5));
            }
        }
    }

    @Nested
    class Max {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (short) 11, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((short) 10, Numbers.max("field", (short) 10, (short) 10));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((short) 5, Numbers.max("field", (short) 5, (short) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (Short) (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (Short) (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", (Short) null, (short) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (Short) (short) 11, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((short) 10, Numbers.max("field", (Short) (short) 10, (short) 10));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((short) 5, Numbers.max("field", (Short) (short) 5, (short) 10));
            }
        }
    }

    @Nested
    class MinExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (short) 6, (short) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (short) 6, (short) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (short) 4, (short) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (short) 5, (short) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((short) 6, Numbers.minExclusive("field", (short) 6, (short) 5));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (Short) (short) 6, (short) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (Short) (short) 6, (short) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", (Short) null, (short) 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Short) (short) 4, (short) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Short) (short) 5, (short) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((short) 6, Numbers.minExclusive("field", (Short) (short) 6, (short) 5));
            }
        }
    }

    @Nested
    class MaxExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (short) 11, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (short) 10, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((short) 9, Numbers.maxExclusive("field", (short) 9, (short) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (Short) (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (Short) (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", (Short) null, (short) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Short) (short) 11, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Short) (short) 10, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((short) 9, Numbers.maxExclusive("field", (Short) (short) 9, (short) 10));
            }
        }
    }

    @Nested
    class Range {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (short) 7, (short) 5, (short) 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (short) 4, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((short) 5, Numbers.range("field", (short) 5, (short) 5, (short) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.range("field", (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((short) 10, Numbers.range("field", (short) 10, (short) 5, (short) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (short) 11, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (Short) (short) 7, (short) 5, (short) 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", (Short) null, (short) 5, (short) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Short) (short) 4, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((short) 5, Numbers.range("field", (Short) (short) 5, (short) 5, (short) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.range("field", (Short) (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((short) 10, Numbers.range("field", (Short) (short) 10, (short) 5, (short) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Short) (short) 11, (short) 5, (short) 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (short) 7, (short) 5, (short) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (short) 7, (short) 5, (short) 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (short) 5, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.exclusiveRange("field", (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (short) 10, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (Short) (short) 7, (short) 5, (short) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", (Short) null, (short) 5, (short) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Short) (short) 5, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.exclusiveRange("field", (Short) (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Short) (short) 10, (short) 5, (short) 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (short) 7, (short) 5, (short) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (short) 4, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((short) 5, Numbers.rangeExclusiveMax("field", (short) 5, (short) 5, (short) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.rangeExclusiveMax("field", (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (short) 10, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (Short) (short) 7, (short) 5, (short) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", (Short) null, (short) 5, (short) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Short) (short) 4, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((short) 5, Numbers.rangeExclusiveMax("field", (Short) (short) 5, (short) 5, (short) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.rangeExclusiveMax("field", (Short) (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Short) (short) 10, (short) 5, (short) 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (short) 7, (short) 5, (short) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (short) 5, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.rangeExclusiveMin("field", (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((short) 10, Numbers.rangeExclusiveMin("field", (short) 10, (short) 5, (short) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (short) 11, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (Short) (short) 7, (short) 5, (short) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (Short) (short) 7, (short) 5, (short) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", (Short) null, (short) 5, (short) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Short) (short) 5, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((short) 7, Numbers.rangeExclusiveMin("field", (Short) (short) 7, (short) 5, (short) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((short) 10, Numbers.rangeExclusiveMin("field", (Short) (short) 10, (short) 5, (short) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Short) (short) 11, (short) 5, (short) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, (short) 5, (short) 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", (Short) null, (short) 5));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", (short) 4, (short) 5));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals((short) 5, Numbers.optionalMin("field", (short) 5, (short) 5));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals((short) 10, Numbers.optionalMin("field", (short) 10, (short) 5));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, (short) 5, (short) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", (Short) null, (short) 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", (short) 11, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals((short) 10, Numbers.optionalMax("field", (short) 10, (short) 10));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals((short) 5, Numbers.optionalMax("field", (short) 5, (short) 10));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, (short) 6, (short) 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", (Short) null, (short) 5));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", (short) 5, (short) 5));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals((short) 6, Numbers.optionalMinExclusive("field", (short) 6, (short) 5));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, (short) 5, (short) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", (Short) null, (short) 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", (short) 10, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals((short) 9, Numbers.optionalMaxExclusive("field", (short) 9, (short) 10));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, (short) 7, (short) 5, (short) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", (short) 7, (short) 5, (short) 4));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", (Short) null, (short) 5, (short) 10));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", (short) 4, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals((short) 5, Numbers.optionalRange("field", (short) 5, (short) 5, (short) 10));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((short) 7, Numbers.optionalRange("field", (short) 7, (short) 5, (short) 10));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals((short) 10, Numbers.optionalRange("field", (short) 10, (short) 5, (short) 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", (short) 11, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, (short) 7, (short) 5, (short) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", (short) 7, (short) 5, (short) 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", (Short) null, (short) 5, (short) 10));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", (short) 5, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((short) 7, Numbers.optionalExclusiveRange("field", (short) 7, (short) 5, (short) 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", (short) 10, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, (short) 7, (short) 5, (short) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", (short) 7, (short) 5, (short) 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", (Short) null, (short) 5, (short) 10));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", (short) 4, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals((short) 5, Numbers.optionalRangeExclusiveMax("field", (short) 5, (short) 5, (short) 10));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((short) 7, Numbers.optionalRangeExclusiveMax("field", (short) 7, (short) 5, (short) 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", (short) 10, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, (short) 7, (short) 5, (short) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", (short) 7, (short) 5, (short) 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", (Short) null, (short) 5, (short) 10));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", (short) 5, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((short) 7, Numbers.optionalRangeExclusiveMin("field", (short) 7, (short) 5, (short) 10));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals((short) 10, Numbers.optionalRangeExclusiveMin("field", (short) 10, (short) 5, (short) 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", (short) 11, (short) 5, (short) 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class Positive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (short) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (short) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (short) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (short) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals((short) 1, Numbers.positive("field", (short) 1));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((short) 10, Numbers.positive("field", (short) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (Short) (short) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (Short) (short) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.positive("field", (Short) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Short) (short) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Short) (short) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals((short) 1, Numbers.positive("field", (Short) (short) 1));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((short) 10, Numbers.positive("field", (Short) (short) 10));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive(null, (Short) (short) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive("   ", (Short) (short) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalPositive("field", (Short) null));
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", (Short) (short) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((short) 5, Numbers.optionalPositive("field", (Short) (short) 5));
            }
        }
    }

    @Nested
    class NotNegative {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (short) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (short) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (short) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals((short) 0, Numbers.notNegative("field", (short) 0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((short) 10, Numbers.notNegative("field", (short) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (Short) (short) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (Short) (short) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (Short) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Short) (short) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals((short) 0, Numbers.notNegative("field", (Short) (short) 0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((short) 10, Numbers.notNegative("field", (Short) (short) 10));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative(null, (Short) (short) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative("   ", (Short) (short) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalNotNegative("field", (Short) null));
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", (Short) (short) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals((short) 0, Numbers.optionalNotNegative("field", (Short) (short) 0));
            }
        }
    }

}










