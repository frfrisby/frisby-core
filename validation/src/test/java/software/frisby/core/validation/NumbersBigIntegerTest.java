package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class NumbersBigIntegerTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull(null, BigInteger.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNull("   ", BigInteger.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.notNull("field", (BigInteger) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.notNull("field", BigInteger.valueOf(5)));
        }
    }

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.min(null, BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.min("   ", BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.min("field", BigInteger.valueOf(5), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.min("field", null, BigInteger.valueOf(5)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.min("field", BigInteger.valueOf(4), BigInteger.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.min("field", BigInteger.valueOf(5), BigInteger.valueOf(5)));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.min("field", BigInteger.valueOf(10), BigInteger.valueOf(5)));
        }
    }

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.max(null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.max("   ", BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.max("field", BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.max("field", null, BigInteger.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.max("field", BigInteger.valueOf(11), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.max("field", BigInteger.valueOf(10), BigInteger.valueOf(10)));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.max("field", BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }
    }

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive(null, BigInteger.valueOf(6), BigInteger.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("   ", BigInteger.valueOf(6), BigInteger.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.minExclusive("field", BigInteger.valueOf(6), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.minExclusive("field", null, BigInteger.valueOf(5)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", BigInteger.valueOf(4), BigInteger.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.minExclusive("field", BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigInteger.valueOf(6), Numbers.minExclusive("field", BigInteger.valueOf(6), BigInteger.valueOf(5)));
        }
    }

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive(null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("   ", BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.maxExclusive("field", BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.maxExclusive("field", null, BigInteger.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", BigInteger.valueOf(11), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.maxExclusive("field", BigInteger.valueOf(10), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigInteger.valueOf(9), Numbers.maxExclusive("field", BigInteger.valueOf(9), BigInteger.valueOf(10)));
        }
    }

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.range("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.range("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(4)));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.range("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.range("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.range("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.range("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.range("field", BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class ExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.exclusiveRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.exclusiveRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.exclusiveRange("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.exclusiveRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.exclusiveRange("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class RangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMax("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMax("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMax("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.rangeExclusiveMax("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.rangeExclusiveMax("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMax("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class RangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.rangeExclusiveMin("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.rangeExclusiveMin("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Numbers.rangeExclusiveMin("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.rangeExclusiveMin("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.rangeExclusiveMin("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.rangeExclusiveMin("field", BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin(null, BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin("   ", BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMin("field", BigInteger.valueOf(5), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMin("field", null, BigInteger.valueOf(5)));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMin("field", BigInteger.valueOf(4), BigInteger.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.optionalMin("field", BigInteger.valueOf(5), BigInteger.valueOf(5)));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.optionalMin("field", BigInteger.valueOf(10), BigInteger.valueOf(5)));
        }
    }

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax(null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax("   ", BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMax("field", BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMax("field", null, BigInteger.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMax("field", BigInteger.valueOf(11), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.optionalMax("field", BigInteger.valueOf(10), BigInteger.valueOf(10)));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.optionalMax("field", BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }
    }

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive(null, BigInteger.valueOf(6), BigInteger.valueOf(5)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive("   ", BigInteger.valueOf(6), BigInteger.valueOf(5)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMinExclusive("field", BigInteger.valueOf(6), null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMinExclusive("field", null, BigInteger.valueOf(5)));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMinExclusive("field", BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(BigInteger.valueOf(6), Numbers.optionalMinExclusive("field", BigInteger.valueOf(6), BigInteger.valueOf(5)));
        }
    }

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive(null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive("   ", BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalMaxExclusive("field", BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalMaxExclusive("field", null, BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalMaxExclusive("field", BigInteger.valueOf(10), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(BigInteger.valueOf(9), Numbers.optionalMaxExclusive("field", BigInteger.valueOf(9), BigInteger.valueOf(10)));
        }
    }

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(4)));
            assertEquals("The 'max' value of '4' is invalid. The value must be greater than or equal to the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRange("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.optionalRange("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.optionalRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.optionalRange("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRange("field", BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalExclusiveRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalExclusiveRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalExclusiveRange("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.optionalExclusiveRange("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalExclusiveRange("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMax("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueBelowMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(4), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '5'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(BigInteger.valueOf(5), Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMax("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than '10'.", ex.getMessage());
        }
    }

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin(null, BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin("   ", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(7), null, BigInteger.valueOf(10)));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(7), BigInteger.valueOf(5), null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(5)));
            assertEquals("The 'max' value of '5' is invalid. The value must be greater than the 'min' value of '5'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Numbers.optionalRangeExclusiveMin("field", null, BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMin_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(5), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be greater than '5'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(BigInteger.valueOf(7), Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(7), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(BigInteger.valueOf(10), Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(10), BigInteger.valueOf(5), BigInteger.valueOf(10)));
        }

        @Test
        void valueAboveMax_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalRangeExclusiveMin("field", BigInteger.valueOf(11), BigInteger.valueOf(5), BigInteger.valueOf(10)));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '10'.", ex.getMessage());
        }
    }

    @Nested
    class Positive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.positive(null, BigInteger.ONE));
            assertEquals("The 'name' value was not provided.", ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.positive("   ", BigInteger.ONE));
            assertEquals("The 'name' value is invalid. The value must be non null and cannot contain only white space characters.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> Numbers.positive("field", (BigInteger) null));
        }

        @Test
        void zeroValue_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", BigInteger.ZERO));
            assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
        }

        @Test
        void negativeValue_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.positive("field", BigInteger.valueOf(-1)));
            assertEquals("The 'field' value is invalid. The value must be greater than '0'.", ex.getMessage());
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(BigInteger.ONE, Numbers.positive("field", BigInteger.ONE));
        }

        @Test
        void optionalNullValue_returnsNull() {
            assertNull(Numbers.optionalPositive("field", (BigInteger) null));
        }

        @Test
        void optionalZeroValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalPositive("field", BigInteger.ZERO));
        }

        @Test
        void optionalPositiveValue_returnsValue() {
            assertEquals(BigInteger.ONE, Numbers.optionalPositive("field", BigInteger.ONE));
        }
    }

    @Nested
    class NotNegative {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative(null, BigInteger.ZERO));
            assertEquals("The 'name' value was not provided.", ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Numbers.notNegative("   ", BigInteger.ZERO));
            assertEquals("The 'name' value is invalid. The value must be non null and cannot contain only white space characters.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> Numbers.notNegative("field", (BigInteger) null));
        }

        @Test
        void negativeValue_throwsNumericValueOutsideRangeException() {
            var ex = assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.notNegative("field", BigInteger.valueOf(-1)));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '0'.", ex.getMessage());
        }

        @Test
        void zeroValue_returnsValue() {
            assertEquals(BigInteger.ZERO, Numbers.notNegative("field", BigInteger.ZERO));
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(BigInteger.ONE, Numbers.notNegative("field", BigInteger.ONE));
        }

        @Test
        void optionalNullValue_returnsNull() {
            assertNull(Numbers.optionalNotNegative("field", (BigInteger) null));
        }

        @Test
        void optionalNegativeValue_throwsNumericValueOutsideRangeException() {
            assertThrows(NumericValueOutsideRangeException.class, () -> Numbers.optionalNotNegative("field", BigInteger.valueOf(-1)));
        }

        @Test
        void optionalZeroValue_returnsValue() {
            assertEquals(BigInteger.ZERO, Numbers.optionalNotNegative("field", BigInteger.ZERO));
        }
    }

}

