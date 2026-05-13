package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersIntTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", 5));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (Integer) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsUnboxedValue() {
            assertEquals(5, Numbers.notNull("field", 5));
        }
    }

    @Nested
    class Min {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, 5, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", 5, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", 4, 5));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5, Numbers.min("field", 5, 5));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10, Numbers.min("field", 10, 5));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (Integer) 5, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (Integer) 5, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", (Integer) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (Integer) 4, 5));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5, Numbers.min("field", (Integer) 5, 5));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10, Numbers.min("field", (Integer) 10, 5));
            }
        }
    }

    @Nested
    class Max {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", 11, 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10, Numbers.max("field", 10, 10));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5, Numbers.max("field", 5, 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (Integer) 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (Integer) 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", (Integer) null, 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (Integer) 11, 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10, Numbers.max("field", (Integer) 10, 10));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5, Numbers.max("field", (Integer) 5, 10));
            }
        }
    }

    @Nested
    class MinExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, 6, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", 6, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 4, 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 5, 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6, Numbers.minExclusive("field", 6, 5));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (Integer) 6, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (Integer) 6, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", (Integer) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Integer) 4, 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Integer) 5, 5));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6, Numbers.minExclusive("field", (Integer) 6, 5));
            }
        }
    }

    @Nested
    class MaxExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 11, 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 10, 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9, Numbers.maxExclusive("field", 9, 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (Integer) 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (Integer) 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", (Integer) null, 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Integer) 11, 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Integer) 10, 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9, Numbers.maxExclusive("field", (Integer) 9, 10));
            }
        }
    }

    @Nested
    class Range {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", 7, 5, 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 4, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5, Numbers.range("field", 5, 5, 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.range("field", 7, 5, 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10, Numbers.range("field", 10, 5, 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 11, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (Integer) 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (Integer) 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (Integer) 7, 5, 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", (Integer) null, 5, 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Integer) 4, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5, Numbers.range("field", (Integer) 5, 5, 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.range("field", (Integer) 7, 5, 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10, Numbers.range("field", (Integer) 10, 5, 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Integer) 11, 5, 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7, 5, 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7, 5, 4));
                assertEquals("The 'max' value of '4' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 5, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.exclusiveRange("field", 7, 5, 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 10, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (Integer) 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (Integer) 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (Integer) 7, 5, 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", (Integer) null, 5, 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Integer) 5, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.exclusiveRange("field", (Integer) 7, 5, 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Integer) 10, 5, 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", 7, 5, 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 4, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5, Numbers.rangeExclusiveMax("field", 5, 5, 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.rangeExclusiveMax("field", 7, 5, 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 10, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (Integer) 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (Integer) 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (Integer) 7, 5, 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", (Integer) null, 5, 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Integer) 4, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5, Numbers.rangeExclusiveMax("field", (Integer) 5, 5, 10));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.rangeExclusiveMax("field", (Integer) 7, 5, 10));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Integer) 10, 5, 10));
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
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", 7, 5, 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 5, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.rangeExclusiveMin("field", 7, 5, 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10, Numbers.rangeExclusiveMin("field", 10, 5, 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 11, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (Integer) 7, 5, 10));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (Integer) 7, 5, 10));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (Integer) 7, 5, 5));
                assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", (Integer) null, 5, 10));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Integer) 5, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7, Numbers.rangeExclusiveMin("field", (Integer) 7, 5, 10));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10, Numbers.rangeExclusiveMin("field", (Integer) 10, 5, 10));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Integer) 11, 5, 10));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
            }
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, 5, 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", (Integer) null, 5));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", 4, 5));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5, Numbers.optionalMin("field", 5, 5));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(10, Numbers.optionalMin("field", 10, 5));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, 5, 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", (Integer) null, 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", 11, 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10, Numbers.optionalMax("field", 10, 10));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(5, Numbers.optionalMax("field", 5, 10));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, 6, 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", (Integer) null, 5));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", 5, 5));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(6, Numbers.optionalMinExclusive("field", 6, 5));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, 5, 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", (Integer) null, 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", 10, 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(9, Numbers.optionalMaxExclusive("field", 9, 10));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, 7, 5, 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", 7, 5, 4));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", (Integer) null, 5, 10));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 4, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5, Numbers.optionalRange("field", 5, 5, 10));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7, Numbers.optionalRange("field", 7, 5, 10));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10, Numbers.optionalRange("field", 10, 5, 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 11, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, 7, 5, 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", 7, 5, 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", (Integer) null, 5, 10));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 5, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7, Numbers.optionalExclusiveRange("field", 7, 5, 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 10, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, 7, 5, 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", 7, 5, 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", (Integer) null, 5, 10));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 4, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5, Numbers.optionalRangeExclusiveMax("field", 5, 5, 10));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7, Numbers.optionalRangeExclusiveMax("field", 7, 5, 10));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 10, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, 7, 5, 10));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", 7, 5, 5));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", (Integer) null, 5, 10));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 5, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7, Numbers.optionalRangeExclusiveMin("field", 7, 5, 10));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10, Numbers.optionalRangeExclusiveMin("field", 10, 5, 10));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 11, 5, 10));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class Positive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", -1));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals(1, Numbers.positive("field", 1));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10, Numbers.positive("field", 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (Integer) 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (Integer) 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.positive("field", (Integer) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Integer) 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Integer) (-1)));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void valueAtOne_returnsValue() {
                assertEquals(1, Numbers.positive("field", (Integer) 1));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10, Numbers.positive("field", (Integer) 10));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive(null, 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive("   ", 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalPositive("field", (Integer) null));
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", 0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(5, Numbers.optionalPositive("field", 5));
            }
        }
    }

    @Nested
    class NotNegative {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0, Numbers.notNegative("field", 0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10, Numbers.notNegative("field", 10));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (Integer) 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (Integer) 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (Integer) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Integer) (-1)));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0, Numbers.notNegative("field", (Integer) 0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(10, Numbers.notNegative("field", (Integer) 10));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative(null, 0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative("   ", 0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalNotNegative("field", (Integer) null));
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", -1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0, Numbers.optionalNotNegative("field", 0));
            }
        }
    }

}










