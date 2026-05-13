package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumbersBigDecimalTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, BigDecimal.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", BigDecimal.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (BigDecimal) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.notNull("field", BigDecimal.valueOf(5)));
        }
    }

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.min("field", BigDecimal.valueOf(5), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", null, BigDecimal.valueOf(5)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.min("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.min("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5)));
        }
    }

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.max("field", BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", null, BigDecimal.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", BigDecimal.valueOf(11), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.max("field", BigDecimal.valueOf(10), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.max("field", BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }
    }

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, BigDecimal.valueOf(6), BigDecimal.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", BigDecimal.valueOf(6), BigDecimal.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("field", BigDecimal.valueOf(6), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", null, BigDecimal.valueOf(5)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(6), Numbers.minExclusive("field", BigDecimal.valueOf(6), BigDecimal.valueOf(5)));
        }
    }

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("field", BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", null, BigDecimal.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", BigDecimal.valueOf(11), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", BigDecimal.valueOf(10), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(9), Numbers.maxExclusive("field", BigDecimal.valueOf(9), BigDecimal.valueOf(10)));
        }
    }

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(4)));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.range("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.range("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.range("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", BigDecimal.valueOf(11), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class ExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.exclusiveRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class RangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class RangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", BigDecimal.valueOf(11), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin("   ", BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin("field", BigDecimal.valueOf(5), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", null, BigDecimal.valueOf(5)));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.optionalMin("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.optionalMin("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5)));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax("   ", BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax("field", BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", null, BigDecimal.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", BigDecimal.valueOf(11), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.optionalMax("field", BigDecimal.valueOf(10), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.optionalMax("field", BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, BigDecimal.valueOf(6), BigDecimal.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive("   ", BigDecimal.valueOf(6), BigDecimal.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive("field", BigDecimal.valueOf(6), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", null, BigDecimal.valueOf(5)));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(6), Numbers.optionalMinExclusive("field", BigDecimal.valueOf(6), BigDecimal.valueOf(5)));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive("   ", BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive("field", BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", null, BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", BigDecimal.valueOf(10), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(9), Numbers.optionalMaxExclusive("field", BigDecimal.valueOf(9), BigDecimal.valueOf(10)));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(4)));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.optionalRange("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.optionalRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.optionalRange("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", BigDecimal.valueOf(11), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.optionalExclusiveRange("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigDecimal.valueOf(5), Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin("   ", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(7), null, BigDecimal.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", null, BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(5), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(7), Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(7), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigDecimal.valueOf(10), Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(10), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigDecimal.valueOf(11), BigDecimal.valueOf(5), BigDecimal.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class Positive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, BigDecimal.ONE));
            assertEquals("The 'name' value was not provided.", ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", BigDecimal.ONE));
            assertEquals("The 'name' value is invalid. The value must be non null and cannot contain only white space characters.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> Numbers.positive("field", (BigDecimal) null));
        }

        @Test
        void zeroValue_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", BigDecimal.ZERO));
            assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
        }

        @Test
        void negativeValue_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", BigDecimal.valueOf(-1)));
            assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(BigDecimal.ONE, Numbers.positive("field", BigDecimal.ONE));
        }

        @Test
        void optionalNullValue_returnsNull() {
            assertNull(Numbers.optionalPositive("field", (BigDecimal) null));
        }

        @Test
        void optionalZeroValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", BigDecimal.ZERO));
        }

        @Test
        void optionalPositiveValue_returnsValue() {
            assertEquals(BigDecimal.ONE, Numbers.optionalPositive("field", BigDecimal.ONE));
        }
    }

    @Nested
    class NotNegative {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, BigDecimal.ZERO));
            assertEquals("The 'name' value was not provided.", ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", BigDecimal.ZERO));
            assertEquals("The 'name' value is invalid. The value must be non null and cannot contain only white space characters.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (BigDecimal) null));
        }

        @Test
        void negativeValue_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", BigDecimal.valueOf(-1)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
        }

        @Test
        void zeroValue_returnsValue() {
            assertEquals(BigDecimal.ZERO, Numbers.notNegative("field", BigDecimal.ZERO));
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(BigDecimal.ONE, Numbers.notNegative("field", BigDecimal.ONE));
        }

        @Test
        void optionalNullValue_returnsNull() {
            assertNull(Numbers.optionalNotNegative("field", (BigDecimal) null));
        }

        @Test
        void optionalNegativeValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", BigDecimal.valueOf(-1)));
        }

        @Test
        void optionalZeroValue_returnsValue() {
            assertEquals(BigDecimal.ZERO, Numbers.optionalNotNegative("field", BigDecimal.ZERO));
        }
    }

}

