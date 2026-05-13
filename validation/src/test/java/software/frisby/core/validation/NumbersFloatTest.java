package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersFloatTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, 5.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", 5.0f));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (Float) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsUnboxedValue() {
            assertEquals(5.0f, Numbers.notNull("field", 5.0f));
        }
    }

    @Nested
    class Min {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, 5.0f, 5.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", 5.0f, 5.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", 4.0f, 5.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0f, Numbers.min("field", 5.0f, 5.0f));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10.0f, Numbers.min("field", 10.0f, 5.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.min("field", Float.NaN, 5.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (Float) 5.0f, 5.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (Float) 5.0f, 5.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", (Float) null, 5.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (Float) 4.0f, 5.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0f, Numbers.min("field", (Float) 5.0f, 5.0f));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10.0f, Numbers.min("field", (Float) 10.0f, 5.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.min("field", (Float) Float.NaN, 5.0f));
            }
        }
    }

    @Nested
    class Max {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", 11.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0f, Numbers.max("field", 10.0f, 10.0f));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5.0f, Numbers.max("field", 5.0f, 10.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.max("field", Float.NaN, 10.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (Float) 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (Float) 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", (Float) null, 10.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (Float) 11.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0f, Numbers.max("field", (Float) 10.0f, 10.0f));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5.0f, Numbers.max("field", (Float) 5.0f, 10.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.max("field", (Float) Float.NaN, 10.0f));
            }
        }
    }

    @Nested
    class MinExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, 6.0f, 5.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", 6.0f, 5.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 4.0f, 5.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 5.0f, 5.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6.0f, Numbers.minExclusive("field", 6.0f, 5.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.minExclusive("field", Float.NaN, 5.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (Float) 6.0f, 5.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (Float) 6.0f, 5.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", (Float) null, 5.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Float) 4.0f, 5.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Float) 5.0f, 5.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6.0f, Numbers.minExclusive("field", (Float) 6.0f, 5.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.minExclusive("field", (Float) Float.NaN, 5.0f));
            }
        }
    }

    @Nested
    class MaxExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 11.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 10.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9.0f, Numbers.maxExclusive("field", 9.0f, 10.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.maxExclusive("field", Float.NaN, 10.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (Float) 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (Float) 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", (Float) null, 10.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Float) 11.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Float) 10.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9.0f, Numbers.maxExclusive("field", (Float) 9.0f, 10.0f));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.maxExclusive("field", (Float) Float.NaN, 10.0f));
            }
        }
    }

    @Nested
    class Range {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", 7.0f, 5.0f, 4.0f));
                assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than or equal to the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 4.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0f, Numbers.range("field", 5.0f, 5.0f, 10.0f));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.range("field", 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0f, Numbers.range("field", 10.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 11.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.range("field", Float.NaN, 5.0f, 10.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (Float) 7.0f, 5.0f, 4.0f));
                assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than or equal to the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", (Float) null, 5.0f, 10.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Float) 4.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0f, Numbers.range("field", (Float) 5.0f, 5.0f, 10.0f));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.range("field", (Float) 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0f, Numbers.range("field", (Float) 10.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Float) 11.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.range("field", (Float) Float.NaN, 5.0f, 10.0f));
            }
        }
    }

    @Nested
    class ExclusiveRange {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7.0f, 5.0f, 5.0f));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7.0f, 5.0f, 4.0f));
                assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 5.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.exclusiveRange("field", 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 10.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.exclusiveRange("field", Float.NaN, 5.0f, 10.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (Float) 7.0f, 5.0f, 5.0f));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", (Float) null, 5.0f, 10.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Float) 5.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.exclusiveRange("field", (Float) 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Float) 10.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.exclusiveRange("field", (Float) Float.NaN, 5.0f, 10.0f));
            }
        }
    }

    @Nested
    class RangeExclusiveMax {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", 7.0f, 5.0f, 5.0f));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 4.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0f, Numbers.rangeExclusiveMax("field", 5.0f, 5.0f, 10.0f));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.rangeExclusiveMax("field", 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 10.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMax("field", Float.NaN, 5.0f, 10.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (Float) 7.0f, 5.0f, 5.0f));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", (Float) null, 5.0f, 10.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Float) 4.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0f, Numbers.rangeExclusiveMax("field", (Float) 5.0f, 5.0f, 10.0f));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.rangeExclusiveMax("field", (Float) 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Float) 10.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMax("field", (Float) Float.NaN, 5.0f, 10.0f));
            }
        }
    }

    @Nested
    class RangeExclusiveMin {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", 7.0f, 5.0f, 5.0f));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 5.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.rangeExclusiveMin("field", 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0f, Numbers.rangeExclusiveMin("field", 10.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 11.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMin("field", Float.NaN, 5.0f, 10.0f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (Float) 7.0f, 5.0f, 10.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (Float) 7.0f, 5.0f, 5.0f));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", (Float) null, 5.0f, 10.0f));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Float) 5.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0f, Numbers.rangeExclusiveMin("field", (Float) 7.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0f, Numbers.rangeExclusiveMin("field", (Float) 10.0f, 5.0f, 10.0f));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Float) 11.0f, 5.0f, 10.0f));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMin("field", (Float) Float.NaN, 5.0f, 10.0f));
            }
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, 5.0f, 5.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", (Float) null, 5.0f));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", 4.0f, 5.0f));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5.0f, Numbers.optionalMin("field", 5.0f, 5.0f));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(10.0f, Numbers.optionalMin("field", 10.0f, 5.0f));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMin("field", Float.NaN, 5.0f));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, 5.0f, 10.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", (Float) null, 10.0f));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", 11.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10.0f, Numbers.optionalMax("field", 10.0f, 10.0f));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(5.0f, Numbers.optionalMax("field", 5.0f, 10.0f));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMax("field", Float.NaN, 10.0f));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, 6.0f, 5.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", (Float) null, 5.0f));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", 5.0f, 5.0f));
            assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(6.0f, Numbers.optionalMinExclusive("field", 6.0f, 5.0f));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMinExclusive("field", Float.NaN, 5.0f));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, 5.0f, 10.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", (Float) null, 10.0f));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", 10.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(9.0f, Numbers.optionalMaxExclusive("field", 9.0f, 10.0f));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMaxExclusive("field", Float.NaN, 10.0f));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, 7.0f, 5.0f, 10.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", 7.0f, 5.0f, 4.0f));
            assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than or equal to the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", (Float) null, 5.0f, 10.0f));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 4.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5.0f, Numbers.optionalRange("field", 5.0f, 5.0f, 10.0f));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0f, Numbers.optionalRange("field", 7.0f, 5.0f, 10.0f));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10.0f, Numbers.optionalRange("field", 10.0f, 5.0f, 10.0f));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 11.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalRange("field", Float.NaN, 5.0f, 10.0f));
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, 7.0f, 5.0f, 10.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", 7.0f, 5.0f, 5.0f));
            assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", (Float) null, 5.0f, 10.0f));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 5.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0f, Numbers.optionalExclusiveRange("field", 7.0f, 5.0f, 10.0f));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 10.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalExclusiveRange("field", Float.NaN, 5.0f, 10.0f));
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, 7.0f, 5.0f, 10.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", 7.0f, 5.0f, 5.0f));
            assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", (Float) null, 5.0f, 10.0f));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 4.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5.0f, Numbers.optionalRangeExclusiveMax("field", 5.0f, 5.0f, 10.0f));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0f, Numbers.optionalRangeExclusiveMax("field", 7.0f, 5.0f, 10.0f));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 10.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalRangeExclusiveMax("field", Float.NaN, 5.0f, 10.0f));
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, 7.0f, 5.0f, 10.0f));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", 7.0f, 5.0f, 5.0f));
            assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", (Float) null, 5.0f, 10.0f));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 5.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0f, Numbers.optionalRangeExclusiveMin("field", 7.0f, 5.0f, 10.0f));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10.0f, Numbers.optionalRangeExclusiveMin("field", 10.0f, 5.0f, 10.0f));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 11.0f, 5.0f, 10.0f));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalRangeExclusiveMin("field", Float.NaN, 5.0f, 10.0f));
        }
    }

    @Nested
    class Positive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, 1.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", 1.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", 0.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '0.0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", -1.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", Float.NaN));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1f, Numbers.positive("field", 0.1f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (Float) 1.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (Float) 1.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.positive("field", (Float) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Float) 0.0f));
                assertEquals("The 'field' value is invalid. The value must be greater than '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Float) Float.NaN));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1f, Numbers.positive("field", (Float) 0.1f));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive(null, (Float) 1.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive("   ", (Float) 1.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalPositive("field", (Float) null));
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", (Float) 0.0f));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.5f, Numbers.optionalPositive("field", (Float) 0.5f));
            }
        }
    }

    @Nested
    class NotNegative {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, 0.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", 0.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", -0.1f));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", Float.NaN));
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0.0f, Numbers.notNegative("field", 0.0f));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1f, Numbers.notNegative("field", 0.1f));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (Float) 0.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (Float) 0.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (Float) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Float) (-0.1f)));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Float) Float.NaN));
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0.0f, Numbers.notNegative("field", (Float) 0.0f));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1f, Numbers.notNegative("field", (Float) 0.1f));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative(null, (Float) 0.0f));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative("   ", (Float) 0.0f));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalNotNegative("field", (Float) null));
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", (Float) (-0.1f)));
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0.0f, Numbers.optionalNotNegative("field", (Float) 0.0f));
            }
        }
    }

}










