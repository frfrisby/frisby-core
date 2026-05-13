package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";
    private static final String NULL_CLOCK_MSG = "The 'clock' value was not provided.";
    private static final String NULL_TOLERANCE_MSG = "The 'tolerance' value was not provided.";

    private static final LocalDateTime FOUR = LocalDateTime.of(2025, 1, 1, 0, 0, 4);
    private static final LocalDateTime FIVE = LocalDateTime.of(2025, 1, 1, 0, 0, 5);
    private static final LocalDateTime SIX = LocalDateTime.of(2025, 1, 1, 0, 0, 6);
    private static final LocalDateTime SEVEN = LocalDateTime.of(2025, 1, 1, 0, 0, 7);
    private static final LocalDateTime NINE = LocalDateTime.of(2025, 1, 1, 0, 0, 9);
    private static final LocalDateTime TEN = LocalDateTime.of(2025, 1, 1, 0, 0, 10);
    private static final LocalDateTime ELEVEN = LocalDateTime.of(2025, 1, 1, 0, 0, 11);

    // Fixed clock and surrounding values for deterministic clock-relative tests.
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 6, 15, 12, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-06-15T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime PAST_VALUE = FIXED_NOW.minusSeconds(1);
    private static final LocalDateTime FUTURE_VALUE = FIXED_NOW.plusSeconds(1);

    // Clearly past / clearly future values for the no-clock convenience overloads.
    private static final LocalDateTime CLEARLY_PAST = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    private static final LocalDateTime CLEARLY_FUTURE = LocalDateTime.of(2100, 1, 1, 0, 0, 0);

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.notNull(null, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.notNull("   ", FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.notNull("field", FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Min
    // -------------------------------------------------------------------------

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.min(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.min("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.min("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.min("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.min("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.min("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, LocalDateTimes.min("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Max
    // -------------------------------------------------------------------------

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.max(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.max("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.max("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.max("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.max("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-01T00:00:10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDateTimes.max("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.max("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // MinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.minExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.minExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.minExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.minExclusive("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.minExclusive("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.minExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, LocalDateTimes.minExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // MaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.maxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.maxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.maxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.maxExclusive("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.maxExclusive("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.maxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, LocalDateTimes.maxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // Range
    // -------------------------------------------------------------------------

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.range(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.range("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.range("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.range("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.range("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '2025-01-01T00:00:04' is invalid. The value must be greater than or equal to the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.range("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.range("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.range("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.range("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDateTimes.range("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.range("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class ExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.exclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.exclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.exclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.exclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.exclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-01T00:00:05' is invalid. The value must be greater than the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.exclusiveRange("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.exclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.exclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.exclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-01T00:00:05' is invalid. The value must be greater than the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.rangeExclusiveMax("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.rangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.rangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.rangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.rangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-01T00:00:05' is invalid. The value must be greater than the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.rangeExclusiveMin("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.rangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.rangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDateTimes.rangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.rangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Past
    // -------------------------------------------------------------------------

    @Nested
    class Past {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.past(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.past("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.past("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.past("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDateTimes.past("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.past("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.past("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.past("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.past("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, LocalDateTimes.past("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // PastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class PastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.pastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.pastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.pastOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.pastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDateTimes.pastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.pastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.pastOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, LocalDateTimes.pastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.pastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, LocalDateTimes.pastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.pastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.pastOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDateTimes.pastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.pastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.pastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.pastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.pastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW + 5s <= FIXED_NOW + tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, LocalDateTimes.pastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsLocalDateTimeOutsideRangeException() {
            // FIXED_NOW + 6s > FIXED_NOW + tolerance(5s) → fails
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class,
                    () -> LocalDateTimes.pastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.futureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.futureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.futureOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.futureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDateTimes.futureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.futureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.futureOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, LocalDateTimes.futureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.futureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, LocalDateTimes.futureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.futureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.futureOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDateTimes.futureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.futureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.futureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.futureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.futureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW - 5s >= FIXED_NOW - tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, LocalDateTimes.futureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsLocalDateTimeOutsideRangeException() {
            // FIXED_NOW - 6s < FIXED_NOW - tolerance(5s) → fails
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class,
                    () -> LocalDateTimes.futureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.future(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.future("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.future("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.future("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDateTimes.future("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.future("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDateTimes.future("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.future("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.future("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, LocalDateTimes.future("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMin(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMin("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMin("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalMin("field", null, FIVE));
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalMin("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.optionalMin("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, LocalDateTimes.optionalMin("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMax(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMax("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMax("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalMax("field", null, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalMax("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-01T00:00:10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDateTimes.optionalMax("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.optionalMax("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMinExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMinExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMinExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalMinExclusive("field", null, FIVE));
        }

        @Test
        void valueAtMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalMinExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, LocalDateTimes.optionalMinExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMaxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMaxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalMaxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalMaxExclusive("field", null, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalMaxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, LocalDateTimes.optionalMaxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.optionalRange("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '2025-01-01T00:00:04' is invalid. The value must be greater than or equal to the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalRange("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalRange("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.optionalRange("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.optionalRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDateTimes.optionalRange("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalRange("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalExclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalExclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalExclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-01T00:00:05' is invalid. The value must be greater than the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalExclusiveRange("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalExclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.optionalExclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalExclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-01T00:00:05' is invalid. The value must be greater than the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalRangeExclusiveMax("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalRangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDateTimes.optionalRangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.optionalRangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalRangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-01T00:00:05' is invalid. The value must be greater than the 'min' value of '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalRangeExclusiveMin("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalRangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-01T00:00:05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDateTimes.optionalRangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDateTimes.optionalRangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalRangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-01T00:00:10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPast
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPast {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPast(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPast("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalPast("field", null));
        }

        @Test
        void futureValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalPast("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDateTimes.optionalPast("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPast("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDateTimes.optionalPast("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalPast("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalPast("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, LocalDateTimes.optionalPast("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalPastOrPresent("field", null));
        }

        @Test
        void futureValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalPastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDateTimes.optionalPastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDateTimes.optionalPastOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, LocalDateTimes.optionalPastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalPastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, LocalDateTimes.optionalPastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(LocalDateTimes.optionalPastOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDateTimes.optionalPastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalPastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalPastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(LocalDateTimes.optionalPastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, LocalDateTimes.optionalPastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class,
                    () -> LocalDateTimes.optionalPastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFutureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFutureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalFutureOrPresent("field", null));
        }

        @Test
        void pastValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDateTimes.optionalFutureOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, LocalDateTimes.optionalFutureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, LocalDateTimes.optionalFutureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(LocalDateTimes.optionalFutureOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDateTimes.optionalFutureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFutureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(LocalDateTimes.optionalFutureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, LocalDateTimes.optionalFutureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class,
                    () -> LocalDateTimes.optionalFutureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFuture(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFuture("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDateTimes.optionalFuture("field", null));
        }

        @Test
        void pastValue_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalFuture("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDateTimes.optionalFuture("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDateTimes.optionalFuture("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDateTimes.optionalFuture("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalFuture("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsLocalDateTimeOutsideRangeException() {
            var ex = assertThrows(LocalDateTimeOutsideRangeException.class, () -> LocalDateTimes.optionalFuture("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, LocalDateTimes.optionalFuture("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }
}

