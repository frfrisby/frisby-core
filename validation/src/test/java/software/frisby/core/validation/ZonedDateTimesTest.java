package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class ZonedDateTimesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";
    private static final String NULL_CLOCK_MSG = "The 'clock' value was not provided.";
    private static final String NULL_TOLERANCE_MSG = "The 'tolerance' value was not provided.";

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private static final ZonedDateTime FOUR = ZonedDateTime.of(1970, 1, 1, 0, 0, 4, 0, UTC);
    private static final ZonedDateTime FIVE = ZonedDateTime.of(1970, 1, 1, 0, 0, 5, 0, UTC);
    private static final ZonedDateTime SIX = ZonedDateTime.of(1970, 1, 1, 0, 0, 6, 0, UTC);
    private static final ZonedDateTime SEVEN = ZonedDateTime.of(1970, 1, 1, 0, 0, 7, 0, UTC);
    private static final ZonedDateTime NINE = ZonedDateTime.of(1970, 1, 1, 0, 0, 9, 0, UTC);
    private static final ZonedDateTime TEN = ZonedDateTime.of(1970, 1, 1, 0, 0, 10, 0, UTC);
    private static final ZonedDateTime ELEVEN = ZonedDateTime.of(1970, 1, 1, 0, 0, 11, 0, UTC);

    // Fixed clock and surrounding values for deterministic clock-relative tests.
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.ofEpochSecond(1_000_000), UTC);
    private static final ZonedDateTime FIXED_NOW = ZonedDateTime.now(FIXED_CLOCK);
    private static final ZonedDateTime PAST_VALUE = FIXED_NOW.minusSeconds(1);
    private static final ZonedDateTime FUTURE_VALUE = FIXED_NOW.plusSeconds(1);

    // Clearly past / clearly future values for the no-clock convenience overloads.
    private static final ZonedDateTime CLEARLY_PAST = ZonedDateTime.parse("2000-01-01T00:00:00Z");
    private static final ZonedDateTime CLEARLY_FUTURE = ZonedDateTime.parse("2100-01-01T00:00:00Z");

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.notNull(null, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.notNull("   ", FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.notNull("field", FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Min
    // -------------------------------------------------------------------------

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.min(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.min("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.min("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.min("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.min("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.min("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.min("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Max
    // -------------------------------------------------------------------------

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.max(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.max("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.max("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.max("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.max("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.max("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.max("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // MinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.minExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.minExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.minExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.minExclusive("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.minExclusive("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.minExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, ZonedDateTimes.minExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // MaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.maxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.maxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.maxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.maxExclusive("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.maxExclusive("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.maxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, ZonedDateTimes.maxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // Range
    // -------------------------------------------------------------------------

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.range(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.range("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.range("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.range("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.range("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '1970-01-01T00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.range("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.range("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.range("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.range("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.range("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.range("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class ExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.exclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.exclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.exclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.exclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.exclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.exclusiveRange("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.exclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.exclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.exclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.rangeExclusiveMax("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.rangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.rangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.rangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.rangeExclusiveMin("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.rangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.rangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.rangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Past
    // -------------------------------------------------------------------------

    @Nested
    class Past {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.past(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.past("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.past("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.past("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, ZonedDateTimes.past("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.past("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.past("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.past("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.past("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, ZonedDateTimes.past("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // PastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class PastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.pastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.pastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.pastOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.pastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, ZonedDateTimes.pastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.pastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.pastOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, ZonedDateTimes.pastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.pastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, ZonedDateTimes.pastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.pastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.pastOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, ZonedDateTimes.pastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.pastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.pastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.pastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.pastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW + 5s <= FIXED_NOW + tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, ZonedDateTimes.pastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsZonedDateTimeOutsideRangeException() {
            // FIXED_NOW + 6s > FIXED_NOW + tolerance(5s) → fails
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class,
                    () -> ZonedDateTimes.pastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // FutureOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class FutureOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.futureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.futureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.futureOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.futureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, ZonedDateTimes.futureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.futureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.futureOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, ZonedDateTimes.futureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.futureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, ZonedDateTimes.futureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.futureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.futureOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, ZonedDateTimes.futureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.futureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.futureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.futureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.futureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW - 5s >= FIXED_NOW - tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, ZonedDateTimes.futureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsZonedDateTimeOutsideRangeException() {
            // FIXED_NOW - 6s < FIXED_NOW - tolerance(5s) → fails
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class,
                    () -> ZonedDateTimes.futureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Future
    // -------------------------------------------------------------------------

    @Nested
    class Future {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.future(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.future("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.future("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.future("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, ZonedDateTimes.future("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.future("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> ZonedDateTimes.future("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.future("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.future("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, ZonedDateTimes.future("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMin(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMin("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMin("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalMin("field", null, FIVE));
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalMin("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.optionalMin("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.optionalMin("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMax(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMax("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMax("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalMax("field", null, TEN));
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalMax("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.optionalMax("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.optionalMax("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMinExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMinExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMinExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalMinExclusive("field", null, FIVE));
        }

        @Test
        void valueAtMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalMinExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, ZonedDateTimes.optionalMinExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMaxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMaxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalMaxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalMaxExclusive("field", null, TEN));
        }

        @Test
        void valueAtMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalMaxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, ZonedDateTimes.optionalMaxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.optionalRange("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '1970-01-01T00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalRange("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalRange("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.optionalRange("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.optionalRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.optionalRange("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalRange("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalExclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalExclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalExclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalExclusiveRange("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalExclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalExclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalRangeExclusiveMax("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, ZonedDateTimes.optionalRangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalRangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalRangeExclusiveMin("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, ZonedDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, ZonedDateTimes.optionalRangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalRangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPast
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPast {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPast(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPast("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalPast("field", null));
        }

        @Test
        void futureValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalPast("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, ZonedDateTimes.optionalPast("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPast("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(ZonedDateTimes.optionalPast("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalPast("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalPast("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, ZonedDateTimes.optionalPast("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalPastOrPresent("field", null));
        }

        @Test
        void futureValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, ZonedDateTimes.optionalPastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(ZonedDateTimes.optionalPastOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, ZonedDateTimes.optionalPastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, ZonedDateTimes.optionalPastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(ZonedDateTimes.optionalPastOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, ZonedDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalPastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(ZonedDateTimes.optionalPastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, ZonedDateTimes.optionalPastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class,
                    () -> ZonedDateTimes.optionalPastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalFutureOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalFutureOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFutureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFutureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalFutureOrPresent("field", null));
        }

        @Test
        void pastValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, ZonedDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(ZonedDateTimes.optionalFutureOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, ZonedDateTimes.optionalFutureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, ZonedDateTimes.optionalFutureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(ZonedDateTimes.optionalFutureOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, ZonedDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFutureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(ZonedDateTimes.optionalFutureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, ZonedDateTimes.optionalFutureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class,
                    () -> ZonedDateTimes.optionalFutureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalFuture
    // -------------------------------------------------------------------------

    @Nested
    class OptionalFuture {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFuture(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFuture("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(ZonedDateTimes.optionalFuture("field", null));
        }

        @Test
        void pastValue_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalFuture("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, ZonedDateTimes.optionalFuture("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> ZonedDateTimes.optionalFuture("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(ZonedDateTimes.optionalFuture("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalFuture("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsZonedDateTimeOutsideRangeException() {
            var ex = assertThrows(ZonedDateTimeOutsideRangeException.class, () -> ZonedDateTimes.optionalFuture("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, ZonedDateTimes.optionalFuture("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }
}

