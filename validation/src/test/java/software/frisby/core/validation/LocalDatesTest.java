package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LocalDatesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";
    private static final String NULL_CLOCK_MSG = "The 'clock' value was not provided.";

    private static final LocalDate FOUR = LocalDate.of(2025, 1, 4);
    private static final LocalDate FIVE = LocalDate.of(2025, 1, 5);
    private static final LocalDate SIX = LocalDate.of(2025, 1, 6);
    private static final LocalDate SEVEN = LocalDate.of(2025, 1, 7);
    private static final LocalDate NINE = LocalDate.of(2025, 1, 9);
    private static final LocalDate TEN = LocalDate.of(2025, 1, 10);
    private static final LocalDate ELEVEN = LocalDate.of(2025, 1, 11);

    // Fixed clock and surrounding dates for deterministic clock-relative tests.
    private static final LocalDate FIXED_TODAY = LocalDate.of(2025, 6, 15);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-06-15T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate PAST_DATE = LocalDate.of(2025, 6, 14);
    private static final LocalDate FUTURE_DATE = LocalDate.of(2025, 6, 16);

    // Clearly past / clearly future values for the no-clock convenience overloads.
    private static final LocalDate CLEARLY_PAST = LocalDate.of(2000, 1, 1);
    private static final LocalDate CLEARLY_FUTURE = LocalDate.of(2100, 1, 1);

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.notNull(null, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.notNull("   ", FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE, LocalDates.notNull("field", FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Min
    // -------------------------------------------------------------------------

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.min(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.min("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.min("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.min("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.min("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDates.min("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, LocalDates.min("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Max
    // -------------------------------------------------------------------------

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.max(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.max("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.max("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.max("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.max("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDates.max("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, LocalDates.max("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // MinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.minExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.minExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.minExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.minExclusive("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.minExclusive("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.minExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, LocalDates.minExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // MaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.maxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.maxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.maxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.maxExclusive("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.maxExclusive("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.maxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, LocalDates.maxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // Range
    // -------------------------------------------------------------------------

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.range(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.range("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.range("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.range("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.range("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '2025-01-04' is invalid. The value must be greater than or equal to the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.range("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.range("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDates.range("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.range("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDates.range("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.range("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class ExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.exclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.exclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.exclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.exclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.exclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-05' is invalid. The value must be greater than the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.exclusiveRange("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.exclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.exclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.exclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.rangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-05' is invalid. The value must be greater than the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.rangeExclusiveMax("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.rangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDates.rangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.rangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.rangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // RangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class RangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.rangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.rangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-05' is invalid. The value must be greater than the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.rangeExclusiveMin("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.rangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.rangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDates.rangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.rangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Past
    // -------------------------------------------------------------------------

    @Nested
    class Past {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.past(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.past("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.past("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.past("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDates.past("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.past("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.past("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void todayWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.past("field", FIXED_TODAY, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.past("field", FUTURE_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastDateWithClock_returnsValue() {
            assertEquals(PAST_DATE, LocalDates.past("field", PAST_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // PastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class PastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.pastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.pastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.pastOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.pastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDates.pastOrPresent("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.pastOrPresent("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.pastOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void todayWithClock_returnsValue() {
            assertEquals(FIXED_TODAY, LocalDates.pastOrPresent("field", FIXED_TODAY, FIXED_CLOCK));
        }

        @Test
        void futureDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.pastOrPresent("field", FUTURE_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastDateWithClock_returnsValue() {
            assertEquals(PAST_DATE, LocalDates.pastOrPresent("field", PAST_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // FutureOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class FutureOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.futureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.futureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.futureOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.futureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDates.futureOrPresent("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.futureOrPresent("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.futureOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void todayWithClock_returnsValue() {
            assertEquals(FIXED_TODAY, LocalDates.futureOrPresent("field", FIXED_TODAY, FIXED_CLOCK));
        }

        @Test
        void pastDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.futureOrPresent("field", PAST_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureDateWithClock_returnsValue() {
            assertEquals(FUTURE_DATE, LocalDates.futureOrPresent("field", FUTURE_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // Future
    // -------------------------------------------------------------------------

    @Nested
    class Future {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.future(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.future("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.future("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.future("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDates.future("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.future("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> LocalDates.future("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void todayWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.future("field", FIXED_TODAY, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.future("field", PAST_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureDateWithClock_returnsValue() {
            assertEquals(FUTURE_DATE, LocalDates.future("field", FUTURE_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMin(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMin("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMin("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalMin("field", null, FIVE));
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalMin("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDates.optionalMin("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, LocalDates.optionalMin("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMax(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMax("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMax("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalMax("field", null, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalMax("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-10'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDates.optionalMax("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, LocalDates.optionalMax("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMinExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMinExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMinExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalMinExclusive("field", null, FIVE));
        }

        @Test
        void valueAtMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalMinExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, LocalDates.optionalMinExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMaxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMaxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalMaxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalMaxExclusive("field", null, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalMaxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, LocalDates.optionalMaxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.optionalRange("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '2025-01-04' is invalid. The value must be greater than or equal to the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalRange("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalRange("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDates.optionalRange("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.optionalRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDates.optionalRange("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalRange("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalExclusiveRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalExclusiveRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalExclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalExclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalExclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalExclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.optionalExclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-05' is invalid. The value must be greater than the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalExclusiveRange("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalExclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.optionalExclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalExclusiveRange("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.optionalRangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-05' is invalid. The value must be greater than the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalRangeExclusiveMax("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalRangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, LocalDates.optionalRangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.optionalRangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalRangeExclusiveMax("field", TEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRangeExclusiveMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRangeExclusiveMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalRangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> LocalDates.optionalRangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '2025-01-05' is invalid. The value must be greater than the 'min' value of '2025-01-05'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalRangeExclusiveMin("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalRangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '2025-01-05'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, LocalDates.optionalRangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, LocalDates.optionalRangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalRangeExclusiveMin("field", ELEVEN, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '2025-01-10'.", ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPast
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPast {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalPast(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalPast("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalPast("field", null));
        }

        @Test
        void futureValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalPast("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDates.optionalPast("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalPast("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDates.optionalPast("field", null, FIXED_CLOCK));
        }

        @Test
        void todayWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalPast("field", FIXED_TODAY, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalPast("field", FUTURE_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastDateWithClock_returnsValue() {
            assertEquals(PAST_DATE, LocalDates.optionalPast("field", PAST_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalPastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalPastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalPastOrPresent("field", null));
        }

        @Test
        void futureValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalPastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, LocalDates.optionalPastOrPresent("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalPastOrPresent("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDates.optionalPastOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void todayWithClock_returnsValue() {
            assertEquals(FIXED_TODAY, LocalDates.optionalPastOrPresent("field", FIXED_TODAY, FIXED_CLOCK));
        }

        @Test
        void futureDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalPastOrPresent("field", FUTURE_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastDateWithClock_returnsValue() {
            assertEquals(PAST_DATE, LocalDates.optionalPastOrPresent("field", PAST_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalFutureOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalFutureOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalFutureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalFutureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalFutureOrPresent("field", null));
        }

        @Test
        void pastValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalFutureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDates.optionalFutureOrPresent("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalFutureOrPresent("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDates.optionalFutureOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void todayWithClock_returnsValue() {
            assertEquals(FIXED_TODAY, LocalDates.optionalFutureOrPresent("field", FIXED_TODAY, FIXED_CLOCK));
        }

        @Test
        void pastDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalFutureOrPresent("field", PAST_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureDateWithClock_returnsValue() {
            assertEquals(FUTURE_DATE, LocalDates.optionalFutureOrPresent("field", FUTURE_DATE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalFuture
    // -------------------------------------------------------------------------

    @Nested
    class OptionalFuture {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalFuture(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalFuture("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(LocalDates.optionalFuture("field", null));
        }

        @Test
        void pastValue_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalFuture("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, LocalDates.optionalFuture("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> LocalDates.optionalFuture("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(LocalDates.optionalFuture("field", null, FIXED_CLOCK));
        }

        @Test
        void todayWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalFuture("field", FIXED_TODAY, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastDateWithClock_throwsLocalDateOutsideRangeException() {
            var ex = assertThrows(LocalDateOutsideRangeException.class, () -> LocalDates.optionalFuture("field", PAST_DATE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureDateWithClock_returnsValue() {
            assertEquals(FUTURE_DATE, LocalDates.optionalFuture("field", FUTURE_DATE, FIXED_CLOCK));
        }
    }
}

