package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersDoubleTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, 5.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", 5.0));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (Double) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsUnboxedValue() {
            assertEquals(5.0, Numbers.notNull("field", 5.0));
        }
    }

    @Nested
    class Min {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, 5.0, 5.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", 5.0, 5.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", 4.0, 5.0));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0, Numbers.min("field", 5.0, 5.0));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10.0, Numbers.min("field", 10.0, 5.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.min("field", Double.NaN, 5.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, (Double) 5.0, 5.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", (Double) 5.0, 5.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", null, 5.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", (Double) 4.0, 5.0));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0, Numbers.min("field", (Double) 5.0, 5.0));
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(10.0, Numbers.min("field", (Double) 10.0, 5.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.min("field", (Double) Double.NaN, 5.0));
            }
        }
    }

    @Nested
    class Max {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", 11.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0, Numbers.max("field", 10.0, 10.0));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5.0, Numbers.max("field", 5.0, 10.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.max("field", Double.NaN, 10.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, (Double) 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", (Double) 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", null, 10.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", (Double) 11.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0, Numbers.max("field", (Double) 10.0, 10.0));
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(5.0, Numbers.max("field", (Double) 5.0, 10.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.max("field", (Double) Double.NaN, 10.0));
            }
        }
    }

    @Nested
    class MinExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, 6.0, 5.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", 6.0, 5.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 4.0, 5.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", 5.0, 5.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6.0, Numbers.minExclusive("field", 6.0, 5.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.minExclusive("field", Double.NaN, 5.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, (Double) 6.0, 5.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", (Double) 6.0, 5.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", null, 5.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Double) 4.0, 5.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", (Double) 5.0, 5.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueAboveMin_returnsValue() {
                assertEquals(6.0, Numbers.minExclusive("field", (Double) 6.0, 5.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.minExclusive("field", (Double) Double.NaN, 5.0));
            }
        }
    }

    @Nested
    class MaxExclusive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 11.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", 10.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9.0, Numbers.maxExclusive("field", 9.0, 10.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.maxExclusive("field", Double.NaN, 10.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, (Double) 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", (Double) 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", null, 10.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Double) 11.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", (Double) 10.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMax_returnsValue() {
                assertEquals(9.0, Numbers.maxExclusive("field", (Double) 9.0, 10.0));
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.maxExclusive("field", (Double) Double.NaN, 10.0));
            }
        }
    }

    @Nested
    class Range {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", 7.0, 5.0, 4.0));
                assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than or equal to the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 4.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0, Numbers.range("field", 5.0, 5.0, 10.0));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.range("field", 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0, Numbers.range("field", 10.0, 5.0, 10.0));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", 11.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.range("field", Double.NaN, 5.0, 10.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, (Double) 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", (Double) 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", (Double) 7.0, 5.0, 4.0));
                assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than or equal to the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", null, 5.0, 10.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Double) 4.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0, Numbers.range("field", (Double) 5.0, 5.0, 10.0));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.range("field", (Double) 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0, Numbers.range("field", (Double) 10.0, 5.0, 10.0));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", (Double) 11.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.range("field", (Double) Double.NaN, 5.0, 10.0));
            }
        }
    }

    @Nested
    class ExclusiveRange {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7.0, 5.0, 5.0));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void maxLessThanMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", 7.0, 5.0, 4.0));
                assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 5.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.exclusiveRange("field", 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", 10.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.exclusiveRange("field", Double.NaN, 5.0, 10.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, (Double) 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", (Double) 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", (Double) 7.0, 5.0, 5.0));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", null, 5.0, 10.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Double) 5.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.exclusiveRange("field", (Double) 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", (Double) 10.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.exclusiveRange("field", (Double) Double.NaN, 5.0, 10.0));
            }
        }
    }

    @Nested
    class RangeExclusiveMax {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", 7.0, 5.0, 5.0));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 4.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0, Numbers.rangeExclusiveMax("field", 5.0, 5.0, 10.0));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.rangeExclusiveMax("field", 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", 10.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMax("field", Double.NaN, 5.0, 10.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, (Double) 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", (Double) 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", (Double) 7.0, 5.0, 5.0));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", null, 5.0, 10.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Double) 4.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                assertEquals(5.0, Numbers.rangeExclusiveMax("field", (Double) 5.0, 5.0, 10.0));
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.rangeExclusiveMax("field", (Double) 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", (Double) 10.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMax("field", (Double) Double.NaN, 5.0, 10.0));
            }
        }
    }

    @Nested
    class RangeExclusiveMin {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", 7.0, 5.0, 5.0));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 5.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.rangeExclusiveMin("field", 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0, Numbers.rangeExclusiveMin("field", 10.0, 5.0, 10.0));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", 11.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMin("field", Double.NaN, 5.0, 10.0));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, (Double) 7.0, 5.0, 10.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", (Double) 7.0, 5.0, 10.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void maxEqualToMin_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", (Double) 7.0, 5.0, 5.0));
                assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", null, 5.0, 10.0));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void valueAtMin_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Double) 5.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
            }

            @Test
            void valueBetweenMinAndMax_returnsValue() {
                assertEquals(7.0, Numbers.rangeExclusiveMin("field", (Double) 7.0, 5.0, 10.0));
            }

            @Test
            void valueAtMax_returnsValue() {
                assertEquals(10.0, Numbers.rangeExclusiveMin("field", (Double) 10.0, 5.0, 10.0));
            }

            @Test
            void valueAboveMax_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", (Double) 11.0, 5.0, 10.0));
                assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class,
                        () -> Numbers.rangeExclusiveMin("field", (Double) Double.NaN, 5.0, 10.0));
            }
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, 5.0, 5.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", null, 5.0));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", 4.0, 5.0));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5.0, Numbers.optionalMin("field", 5.0, 5.0));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(10.0, Numbers.optionalMin("field", 10.0, 5.0));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMin("field", Double.NaN, 5.0));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, 5.0, 10.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", null, 10.0));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", 11.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10.0, Numbers.optionalMax("field", 10.0, 10.0));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(5.0, Numbers.optionalMax("field", 5.0, 10.0));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMax("field", Double.NaN, 10.0));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, 6.0, 5.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", null, 5.0));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", 5.0, 5.0));
            assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(6.0, Numbers.optionalMinExclusive("field", 6.0, 5.0));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMinExclusive("field", Double.NaN, 5.0));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, 5.0, 10.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", null, 10.0));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", 10.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(9.0, Numbers.optionalMaxExclusive("field", 9.0, 10.0));
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalMaxExclusive("field", Double.NaN, 10.0));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, 7.0, 5.0, 10.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", 7.0, 5.0, 4.0));
            assertEquals("The 'max' value of '4.0' is invalid. The value must be greater than or equal to the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", null, 5.0, 10.0));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 4.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5.0, Numbers.optionalRange("field", 5.0, 5.0, 10.0));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0, Numbers.optionalRange("field", 7.0, 5.0, 10.0));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10.0, Numbers.optionalRange("field", 10.0, 5.0, 10.0));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", 11.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalRange("field", Double.NaN, 5.0, 10.0));
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, 7.0, 5.0, 10.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", 7.0, 5.0, 5.0));
            assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", null, 5.0, 10.0));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 5.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0, Numbers.optionalExclusiveRange("field", 7.0, 5.0, 10.0));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", 10.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalExclusiveRange("field", Double.NaN, 5.0, 10.0));
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, 7.0, 5.0, 10.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", 7.0, 5.0, 5.0));
            assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", null, 5.0, 10.0));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 4.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5.0'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(5.0, Numbers.optionalRangeExclusiveMax("field", 5.0, 5.0, 10.0));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0, Numbers.optionalRangeExclusiveMax("field", 7.0, 5.0, 10.0));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", 10.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be less than '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalRangeExclusiveMax("field", Double.NaN, 5.0, 10.0));
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, 7.0, 5.0, 10.0));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", 7.0, 5.0, 5.0));
            assertEquals("The 'max' value of '5.0' is invalid. The value must be greater than the 'min' value of '5.0'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", null, 5.0, 10.0));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 5.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be greater than '5.0'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(7.0, Numbers.optionalRangeExclusiveMin("field", 7.0, 5.0, 10.0));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(10.0, Numbers.optionalRangeExclusiveMin("field", 10.0, 5.0, 10.0));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", 11.0, 5.0, 10.0));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10.0'.", ex.getMessage());
        }

        @Test
        void nanValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class,
                    () -> Numbers.optionalRangeExclusiveMin("field", Double.NaN, 5.0, 10.0));
        }
    }

    @Nested
    class Positive {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, 1.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", 1.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", 0.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0.0'.", ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", -1.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", Double.NaN));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1, Numbers.positive("field", 0.1));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, (Double) 1.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", (Double) 1.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.positive("field", (Double) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Double) 0.0));
                assertEquals("The 'field' value is invalid. The value must be greater than '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", (Double) Double.NaN));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1, Numbers.positive("field", (Double) 0.1));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive(null, (Double) 1.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalPositive("   ", (Double) 1.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalPositive("field", (Double) null));
            }

            @Test
            void zeroValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", (Double) 0.0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.5, Numbers.optionalPositive("field", (Double) 0.5));
            }
        }
    }

    @Nested
    class NotNegative {
        @Nested
        class Primitive {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, 0.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", 0.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", -0.1));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", Double.NaN));
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0.0, Numbers.notNegative("field", 0.0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1, Numbers.notNegative("field", 0.1));
            }
        }

        @Nested
        class Boxed {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, (Double) 0.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", (Double) 0.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (Double) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Double) (-0.1)));
                assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0.0'.", ex.getMessage());
            }

            @Test
            void nanValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", (Double) Double.NaN));
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0.0, Numbers.notNegative("field", (Double) 0.0));
            }

            @Test
            void positiveValue_returnsValue() {
                assertEquals(0.1, Numbers.notNegative("field", (Double) 0.1));
            }
        }

        @Nested
        class Optional {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative(null, (Double) 0.0));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalNotNegative("   ", (Double) 0.0));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Numbers.optionalNotNegative("field", (Double) null));
            }

            @Test
            void negativeValue_throwsNumericValueOutsideRangeException() {
                assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", (Double) (-0.1)));
            }

            @Test
            void zeroValue_returnsValue() {
                assertEquals(0.0, Numbers.optionalNotNegative("field", (Double) 0.0));
            }
        }
    }

}










