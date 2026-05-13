package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersByteTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, (byte) 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", (byte) 5));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (Byte) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsUnboxedValue() {
            assertEquals((byte) 5, Numbers.notNull("field", (byte) 5));
        }
    }

    @Nested
    class Min {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (byte) 5, (byte) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (byte) 5, (byte) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (byte) 4, (byte) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((byte) 5, Numbers.min("field", (byte) 5, (byte) 5));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((byte) 10, Numbers.min("field", (byte) 10, (byte) 5));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (Byte) (byte) 5, (byte) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (Byte) (byte) 5, (byte) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", (Byte) null, (byte) 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (Byte) (byte) 4, (byte) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((byte) 5, Numbers.min("field", (Byte) (byte) 5, (byte) 5));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((byte) 10, Numbers.min("field", (Byte) (byte) 10, (byte) 5));
            }
        }
    }

    @Nested
    class Max {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (byte) 11, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((byte) 10, Numbers.max("field", (byte) 10, (byte) 10));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((byte) 5, Numbers.max("field", (byte) 5, (byte) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (Byte) (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (Byte) (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", (Byte) null, (byte) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (Byte) (byte) 11, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((byte) 10, Numbers.max("field", (Byte) (byte) 10, (byte) 10));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((byte) 5, Numbers.max("field", (Byte) (byte) 5, (byte) 10));
            }
        }
    }

    @Nested
    class MinExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (byte) 6, (byte) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (byte) 6, (byte) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (byte) 4, (byte) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (byte) 5, (byte) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((byte) 6, Numbers.minExclusive("field", (byte) 6, (byte) 5));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (Byte) (byte) 6, (byte) 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (Byte) (byte) 6, (byte) 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", (Byte) null, (byte) 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Byte) (byte) 4, (byte) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Byte) (byte) 5, (byte) 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals((byte) 6, Numbers.minExclusive("field", (Byte) (byte) 6, (byte) 5));
            }
        }
    }

    @Nested
    class MaxExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (byte) 11, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (byte) 10, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((byte) 9, Numbers.maxExclusive("field", (byte) 9, (byte) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (Byte) (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (Byte) (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", (Byte) null, (byte) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Byte) (byte) 11, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Byte) (byte) 10, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals((byte) 9, Numbers.maxExclusive("field", (Byte) (byte) 9, (byte) 10));
            }
        }
    }

    @Nested
    class Range {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (byte) 7, (byte) 5, (byte) 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (byte) 4, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((byte) 5, Numbers.range("field", (byte) 5, (byte) 5, (byte) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.range("field", (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((byte) 10, Numbers.range("field", (byte) 10, (byte) 5, (byte) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (byte) 11, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (Byte) (byte) 7, (byte) 5, (byte) 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", (Byte) null, (byte) 5, (byte) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Byte) (byte) 4, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((byte) 5, Numbers.range("field", (Byte) (byte) 5, (byte) 5, (byte) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.range("field", (Byte) (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((byte) 10, Numbers.range("field", (Byte) (byte) 10, (byte) 5, (byte) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Byte) (byte) 11, (byte) 5, (byte) 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (byte) 7, (byte) 5, (byte) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (byte) 7, (byte) 5, (byte) 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (byte) 5, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.exclusiveRange("field", (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (byte) 10, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (Byte) (byte) 7, (byte) 5, (byte) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", (Byte) null, (byte) 5, (byte) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Byte) (byte) 5, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.exclusiveRange("field", (Byte) (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Byte) (byte) 10, (byte) 5, (byte) 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (byte) 7, (byte) 5, (byte) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (byte) 4, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((byte) 5, Numbers.rangeExclusiveMax("field", (byte) 5, (byte) 5, (byte) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.rangeExclusiveMax("field", (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (byte) 10, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (Byte) (byte) 7, (byte) 5, (byte) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", (Byte) null, (byte) 5, (byte) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Byte) (byte) 4, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals((byte) 5, Numbers.rangeExclusiveMax("field", (Byte) (byte) 5, (byte) 5, (byte) 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.rangeExclusiveMax("field", (Byte) (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Byte) (byte) 10, (byte) 5, (byte) 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (byte) 7, (byte) 5, (byte) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (byte) 5, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.rangeExclusiveMin("field", (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((byte) 10, Numbers.rangeExclusiveMin("field", (byte) 10, (byte) 5, (byte) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (byte) 11, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (Byte) (byte) 7, (byte) 5, (byte) 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (Byte) (byte) 7, (byte) 5, (byte) 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", (Byte) null, (byte) 5, (byte) 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Byte) (byte) 5, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals((byte) 7, Numbers.rangeExclusiveMin("field", (Byte) (byte) 7, (byte) 5, (byte) 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals((byte) 10, Numbers.rangeExclusiveMin("field", (Byte) (byte) 10, (byte) 5, (byte) 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Byte) (byte) 11, (byte) 5, (byte) 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, (byte) 5, (byte) 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", (Byte) null, (byte) 5));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", (byte) 4, (byte) 5));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals((byte) 5, Numbers.optionalMin("field", (byte) 5, (byte) 5));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals((byte) 10, Numbers.optionalMin("field", (byte) 10, (byte) 5));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, (byte) 5, (byte) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", (Byte) null, (byte) 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", (byte) 11, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals((byte) 10, Numbers.optionalMax("field", (byte) 10, (byte) 10));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals((byte) 5, Numbers.optionalMax("field", (byte) 5, (byte) 10));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, (byte) 6, (byte) 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", (Byte) null, (byte) 5));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", (byte) 5, (byte) 5));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals((byte) 6, Numbers.optionalMinExclusive("field", (byte) 6, (byte) 5));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, (byte) 5, (byte) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", (Byte) null, (byte) 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", (byte) 10, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals((byte) 9, Numbers.optionalMaxExclusive("field", (byte) 9, (byte) 10));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, (byte) 7, (byte) 5, (byte) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", (byte) 7, (byte) 5, (byte) 4));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", (Byte) null, (byte) 5, (byte) 10));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", (byte) 4, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals((byte) 5, Numbers.optionalRange("field", (byte) 5, (byte) 5, (byte) 10));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((byte) 7, Numbers.optionalRange("field", (byte) 7, (byte) 5, (byte) 10));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals((byte) 10, Numbers.optionalRange("field", (byte) 10, (byte) 5, (byte) 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", (byte) 11, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, (byte) 7, (byte) 5, (byte) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", (byte) 7, (byte) 5, (byte) 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", (Byte) null, (byte) 5, (byte) 10));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", (byte) 5, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((byte) 7, Numbers.optionalExclusiveRange("field", (byte) 7, (byte) 5, (byte) 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", (byte) 10, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, (byte) 7, (byte) 5, (byte) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", (byte) 7, (byte) 5, (byte) 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", (Byte) null, (byte) 5, (byte) 10));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", (byte) 4, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals((byte) 5, Numbers.optionalRangeExclusiveMax("field", (byte) 5, (byte) 5, (byte) 10));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((byte) 7, Numbers.optionalRangeExclusiveMax("field", (byte) 7, (byte) 5, (byte) 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", (byte) 10, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, (byte) 7, (byte) 5, (byte) 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", (byte) 7, (byte) 5, (byte) 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", (Byte) null, (byte) 5, (byte) 10));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", (byte) 5, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals((byte) 7, Numbers.optionalRangeExclusiveMin("field", (byte) 7, (byte) 5, (byte) 10));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals((byte) 10, Numbers.optionalRangeExclusiveMin("field", (byte) 10, (byte) 5, (byte) 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", (byte) 11, (byte) 5, (byte) 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class Positive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (byte) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (byte) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (byte) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (byte) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals((byte) 1, Numbers.positive("field", (byte) 1));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((byte) 10, Numbers.positive("field", (byte) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (Byte) (byte) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (Byte) (byte) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.positive("field", (Byte) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Byte) (byte) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Byte) (byte) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals((byte) 1, Numbers.positive("field", (Byte) (byte) 1));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((byte) 10, Numbers.positive("field", (Byte) (byte) 10));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive(null, (Byte) (byte) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive("   ", (Byte) (byte) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalPositive("field", (Byte) null));
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", (Byte) (byte) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((byte) 5, Numbers.optionalPositive("field", (Byte) (byte) 5));
            }
        }
    }

    @Nested
    class NotNegative {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (byte) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (byte) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (byte) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals((byte) 0, Numbers.notNegative("field", (byte) 0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((byte) 10, Numbers.notNegative("field", (byte) 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (Byte) (byte) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (Byte) (byte) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (Byte) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Byte) (byte) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals((byte) 0, Numbers.notNegative("field", (Byte) (byte) 0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals((byte) 10, Numbers.notNegative("field", (Byte) (byte) 10));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative(null, (Byte) (byte) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative("   ", (Byte) (byte) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalNotNegative("field", (Byte) null));
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", (Byte) (byte) -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals((byte) 0, Numbers.optionalNotNegative("field", (Byte) (byte) 0));
            }
        }
    }

}










