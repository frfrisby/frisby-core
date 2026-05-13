package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class OffsetTimesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";

    private static final OffsetTime FOUR = OffsetTime.of(0, 0, 4, 0, ZoneOffset.UTC);
    private static final OffsetTime FIVE = OffsetTime.of(0, 0, 5, 0, ZoneOffset.UTC);
    private static final OffsetTime SIX = OffsetTime.of(0, 0, 6, 0, ZoneOffset.UTC);
    private static final OffsetTime SEVEN = OffsetTime.of(0, 0, 7, 0, ZoneOffset.UTC);
    private static final OffsetTime NINE = OffsetTime.of(0, 0, 9, 0, ZoneOffset.UTC);
    private static final OffsetTime TEN = OffsetTime.of(0, 0, 10, 0, ZoneOffset.UTC);
    private static final OffsetTime ELEVEN = OffsetTime.of(0, 0, 11, 0, ZoneOffset.UTC);

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.notNull(null, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.notNull("   ", FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE, OffsetTimes.notNull("field", FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Min
    // -------------------------------------------------------------------------

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.min(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.min("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.min("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.min("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.min("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetTimes.min("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, OffsetTimes.min("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Max
    // -------------------------------------------------------------------------

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.max(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.max("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.max("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.max("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.max("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetTimes.max("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, OffsetTimes.max("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // MinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.minExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.minExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.minExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.minExclusive("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.minExclusive("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.minExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, OffsetTimes.minExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // MaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.maxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.maxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.maxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.maxExclusive("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.maxExclusive("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.maxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, OffsetTimes.maxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // Range
    // -------------------------------------------------------------------------

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.range(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.range("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.range("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.range("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.range("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.range("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.range("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetTimes.range("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.range("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetTimes.range("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.range("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class ExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.exclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.exclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.exclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.exclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.exclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '00:00:05Z' is invalid. The value must be greater than the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.exclusiveRange("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.exclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.exclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.exclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.rangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '00:00:05Z' is invalid. The value must be greater than the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.rangeExclusiveMax("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.rangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetTimes.rangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.rangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.rangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.rangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.rangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '00:00:05Z' is invalid. The value must be greater than the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetTimes.rangeExclusiveMin("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.rangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.rangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetTimes.rangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.rangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMin(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMin("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMin("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalMin("field", null, FIVE));
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalMin("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetTimes.optionalMin("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, OffsetTimes.optionalMin("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMax(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMax("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMax("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalMax("field", null, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalMax("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetTimes.optionalMax("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, OffsetTimes.optionalMax("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMinExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMinExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMinExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalMinExclusive("field", null, FIVE));
        }

        @Test
        void valueAtMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalMinExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, OffsetTimes.optionalMinExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMaxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMaxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalMaxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalMaxExclusive("field", null, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalMaxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, OffsetTimes.optionalMaxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.optionalRange("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalRange("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalRange("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetTimes.optionalRange("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.optionalRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetTimes.optionalRange("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalRange("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalExclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalExclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalExclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalExclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.optionalExclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '00:00:05Z' is invalid. The value must be greater than the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalExclusiveRange("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalExclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.optionalExclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalExclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '00:00:05Z' is invalid. The value must be greater than the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalRangeExclusiveMax("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalRangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetTimes.optionalRangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalRangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '00:00:05Z' is invalid. The value must be greater than the 'min' value of '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetTimes.optionalRangeExclusiveMin("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalRangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetTimes.optionalRangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetTimeOutsideRangeException() {
            var ex = assertThrows(OffsetTimeOutsideRangeException.class, () -> OffsetTimes.optionalRangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '00:00:10Z'.", ex.getMessage());
        }
    }
}

