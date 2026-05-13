package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class OffsetDateTimesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";
    private static final String NULL_CLOCK_MSG = "The 'clock' value was not provided.";
    private static final String NULL_TOLERANCE_MSG = "The 'tolerance' value was not provided.";

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private static final OffsetDateTime FOUR = OffsetDateTime.of(1970, 1, 1, 0, 0, 4, 0, UTC);
    private static final OffsetDateTime FIVE = OffsetDateTime.of(1970, 1, 1, 0, 0, 5, 0, UTC);
    private static final OffsetDateTime SIX = OffsetDateTime.of(1970, 1, 1, 0, 0, 6, 0, UTC);
    private static final OffsetDateTime SEVEN = OffsetDateTime.of(1970, 1, 1, 0, 0, 7, 0, UTC);
    private static final OffsetDateTime NINE = OffsetDateTime.of(1970, 1, 1, 0, 0, 9, 0, UTC);
    private static final OffsetDateTime TEN = OffsetDateTime.of(1970, 1, 1, 0, 0, 10, 0, UTC);
    private static final OffsetDateTime ELEVEN = OffsetDateTime.of(1970, 1, 1, 0, 0, 11, 0, UTC);

    // Fixed clock and surrounding values for deterministic clock-relative tests.
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.ofEpochSecond(1_000_000), UTC);
    private static final OffsetDateTime FIXED_NOW = OffsetDateTime.now(FIXED_CLOCK);
    private static final OffsetDateTime PAST_VALUE = FIXED_NOW.minusSeconds(1);
    private static final OffsetDateTime FUTURE_VALUE = FIXED_NOW.plusSeconds(1);

    // Clearly past / clearly future values for the no-clock convenience overloads.
    private static final OffsetDateTime CLEARLY_PAST = OffsetDateTime.parse("2000-01-01T00:00:00Z");
    private static final OffsetDateTime CLEARLY_FUTURE = OffsetDateTime.parse("2100-01-01T00:00:00Z");

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.notNull(null, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.notNull("   ", FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.notNull("field", FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Min
    // -------------------------------------------------------------------------

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.min(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.min("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.min("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.min("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.min("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.min("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.min("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Max
    // -------------------------------------------------------------------------

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.max(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.max("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.max("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.max("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.max("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.max("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.max("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // MinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.minExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.minExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.minExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.minExclusive("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.minExclusive("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.minExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, OffsetDateTimes.minExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // MaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.maxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.maxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.maxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.maxExclusive("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.maxExclusive("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.maxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, OffsetDateTimes.maxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // Range
    // -------------------------------------------------------------------------

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.range(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.range("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.range("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.range("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.range("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '1970-01-01T00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.range("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.range("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.range("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.range("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.range("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.range("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.exclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.exclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.exclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.exclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.exclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.exclusiveRange("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.exclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.exclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.exclusiveRange("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.rangeExclusiveMax("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.rangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.rangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.rangeExclusiveMax("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.rangeExclusiveMin("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.rangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.rangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.rangeExclusiveMin("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.past(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.past("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.past("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.past("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, OffsetDateTimes.past("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.past("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.past("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.past("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.past("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, OffsetDateTimes.past("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // PastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class PastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.pastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.pastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.pastOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.pastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, OffsetDateTimes.pastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.pastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.pastOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, OffsetDateTimes.pastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.pastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, OffsetDateTimes.pastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.pastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.pastOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, OffsetDateTimes.pastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.pastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.pastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.pastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.pastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW + 5s <= FIXED_NOW + tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, OffsetDateTimes.pastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsOffsetDateTimeOutsideRangeException() {
            // FIXED_NOW + 6s > FIXED_NOW + tolerance(5s) → fails
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class,
                    () -> OffsetDateTimes.pastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.futureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.futureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.futureOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.futureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, OffsetDateTimes.futureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.futureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.futureOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, OffsetDateTimes.futureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.futureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, OffsetDateTimes.futureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.futureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.futureOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, OffsetDateTimes.futureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.futureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.futureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.futureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.futureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW - 5s >= FIXED_NOW - tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, OffsetDateTimes.futureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsOffsetDateTimeOutsideRangeException() {
            // FIXED_NOW - 6s < FIXED_NOW - tolerance(5s) → fails
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class,
                    () -> OffsetDateTimes.futureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.future(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.future("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.future("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.future("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, OffsetDateTimes.future("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.future("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> OffsetDateTimes.future("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.future("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.future("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, OffsetDateTimes.future("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMin(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMin("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMin("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalMin("field", null, FIVE));
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalMin("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.optionalMin("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.optionalMin("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMax(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMax("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMax("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalMax("field", null, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalMax("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.optionalMax("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.optionalMax("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMinExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMinExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMinExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalMinExclusive("field", null, FIVE));
        }

        @Test
        void valueAtMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalMinExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, OffsetDateTimes.optionalMinExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMaxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMaxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalMaxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalMaxExclusive("field", null, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalMaxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, OffsetDateTimes.optionalMaxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.optionalRange("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '1970-01-01T00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalRange("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalRange("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.optionalRange("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.optionalRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.optionalRange("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalRange("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalExclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalExclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalExclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalExclusiveRange("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalExclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalExclusiveRange("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalRangeExclusiveMax("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, OffsetDateTimes.optionalRangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalRangeExclusiveMax("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalRangeExclusiveMin("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, OffsetDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, OffsetDateTimes.optionalRangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalRangeExclusiveMin("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPast(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPast("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalPast("field", null));
        }

        @Test
        void futureValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalPast("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, OffsetDateTimes.optionalPast("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPast("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(OffsetDateTimes.optionalPast("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalPast("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalPast("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, OffsetDateTimes.optionalPast("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalPastOrPresent("field", null));
        }

        @Test
        void futureValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, OffsetDateTimes.optionalPastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(OffsetDateTimes.optionalPastOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, OffsetDateTimes.optionalPastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, OffsetDateTimes.optionalPastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(OffsetDateTimes.optionalPastOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, OffsetDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalPastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(OffsetDateTimes.optionalPastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, OffsetDateTimes.optionalPastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class,
                    () -> OffsetDateTimes.optionalPastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFutureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFutureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalFutureOrPresent("field", null));
        }

        @Test
        void pastValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, OffsetDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(OffsetDateTimes.optionalFutureOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, OffsetDateTimes.optionalFutureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, OffsetDateTimes.optionalFutureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(OffsetDateTimes.optionalFutureOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, OffsetDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFutureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(OffsetDateTimes.optionalFutureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, OffsetDateTimes.optionalFutureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class,
                    () -> OffsetDateTimes.optionalFutureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFuture(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFuture("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(OffsetDateTimes.optionalFuture("field", null));
        }

        @Test
        void pastValue_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalFuture("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, OffsetDateTimes.optionalFuture("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> OffsetDateTimes.optionalFuture("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(OffsetDateTimes.optionalFuture("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalFuture("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsOffsetDateTimeOutsideRangeException() {
            var ex = assertThrows(OffsetDateTimeOutsideRangeException.class, () -> OffsetDateTimes.optionalFuture("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, OffsetDateTimes.optionalFuture("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }
}

