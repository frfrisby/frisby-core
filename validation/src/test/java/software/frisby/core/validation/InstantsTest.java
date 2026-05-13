package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class InstantsTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String NULL_MIN_MSG = "The 'min' value was not provided.";
    private static final String NULL_MAX_MSG = "The 'max' value was not provided.";
    private static final String NULL_CLOCK_MSG = "The 'clock' value was not provided.";
    private static final String NULL_TOLERANCE_MSG = "The 'tolerance' value was not provided.";

    private static final Instant FOUR = Instant.ofEpochSecond(4);
    private static final Instant FIVE = Instant.ofEpochSecond(5);
    private static final Instant SIX = Instant.ofEpochSecond(6);
    private static final Instant SEVEN = Instant.ofEpochSecond(7);
    private static final Instant NINE = Instant.ofEpochSecond(9);
    private static final Instant TEN = Instant.ofEpochSecond(10);
    private static final Instant ELEVEN = Instant.ofEpochSecond(11);

    // Fixed clock and surrounding instants for deterministic clock-relative tests.
    private static final Instant FIXED_NOW = Instant.ofEpochSecond(1_000_000);
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    private static final Instant PAST_VALUE = FIXED_NOW.minusSeconds(1);
    private static final Instant FUTURE_VALUE = FIXED_NOW.plusSeconds(1);

    // Clearly past / clearly future values for the no-clock convenience overloads.
    private static final Instant CLEARLY_PAST = Instant.parse("2000-01-01T00:00:00Z");
    private static final Instant CLEARLY_FUTURE = Instant.parse("2100-01-01T00:00:00Z");

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.notNull(null, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.notNull("   ", FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE, Instants.notNull("field", FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Min
    // -------------------------------------------------------------------------

    @Nested
    class Min {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.min(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.min("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.min("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.min("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.min("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, Instants.min("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, Instants.min("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // Max
    // -------------------------------------------------------------------------

    @Nested
    class Max {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.max(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.max("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.max("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.max("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.max("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, Instants.max("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, Instants.max("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // MinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.minExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.minExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.minExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.minExclusive("field", null, FIVE));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.minExclusive("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.minExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, Instants.minExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // MaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class MaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.maxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.maxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.maxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.maxExclusive("field", null, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.maxExclusive("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.maxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, Instants.maxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // Range
    // -------------------------------------------------------------------------

    @Nested
    class Range {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.range(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.range("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.range("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.range("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.range("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '1970-01-01T00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.range("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.range("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, Instants.range("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.range("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, Instants.range("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.range("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.exclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.exclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.exclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.exclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.exclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.exclusiveRange("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.exclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.exclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.exclusiveRange("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.rangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.rangeExclusiveMax("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.rangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, Instants.rangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.rangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.rangeExclusiveMax("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.rangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.rangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.rangeExclusiveMin("field", null, FIVE, TEN));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.rangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.rangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, Instants.rangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.rangeExclusiveMin("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.past(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.past("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.past("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.past("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, Instants.past("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.past("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.past("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.past("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.past("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, Instants.past("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // PastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class PastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.pastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.pastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.pastOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.pastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, Instants.pastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.pastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.pastOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, Instants.pastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.pastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, Instants.pastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.pastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.pastOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, Instants.pastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.pastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.pastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.pastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.pastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW + 5s <= FIXED_NOW + tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, Instants.pastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsInstantOutsideRangeException() {
            // FIXED_NOW + 6s > FIXED_NOW + tolerance(5s) → fails
            var ex = assertThrows(InstantOutsideRangeException.class,
                    () -> Instants.pastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.futureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.futureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.futureOrPresent("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.futureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, Instants.futureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.futureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.futureOrPresent("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, Instants.futureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.futureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, Instants.futureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.futureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.futureOrPresent("field", null, Duration.ZERO));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, Instants.futureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.futureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.futureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.futureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.futureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            // FIXED_NOW - 5s >= FIXED_NOW - tolerance(5s) → passes
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, Instants.futureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsInstantOutsideRangeException() {
            // FIXED_NOW - 6s < FIXED_NOW - tolerance(5s) → fails
            var ex = assertThrows(InstantOutsideRangeException.class,
                    () -> Instants.futureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.future(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.future("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.future("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void pastValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.future("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, Instants.future("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.future("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Instants.future("field", null, FIXED_CLOCK));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAtNowWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.future("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.future("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, Instants.future("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMin
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMin {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMin(null, FIVE, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMin("   ", FIVE, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMin("field", FIVE, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalMin("field", null, FIVE));
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalMin("field", FOUR, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, Instants.optionalMin("field", FIVE, FIVE));
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(TEN, Instants.optionalMin("field", TEN, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMax
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMax {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMax(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMax("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMax("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalMax("field", null, TEN));
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalMax("field", ELEVEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than or equal to '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, Instants.optionalMax("field", TEN, TEN));
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(FIVE, Instants.optionalMax("field", FIVE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMinExclusive(null, SIX, FIVE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMinExclusive("   ", SIX, FIVE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMinExclusive("field", SIX, null));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalMinExclusive("field", null, FIVE));
        }

        @Test
        void valueAtMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalMinExclusive("field", FIVE, FIVE));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAboveMin_returnsValue() {
            assertEquals(SIX, Instants.optionalMinExclusive("field", SIX, FIVE));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxExclusive
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxExclusive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMaxExclusive(null, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMaxExclusive("   ", FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalMaxExclusive("field", FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalMaxExclusive("field", null, TEN));
        }

        @Test
        void valueAtMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalMaxExclusive("field", TEN, TEN));
            assertEquals("The 'field' value is invalid. The value must be less than '1970-01-01T00:00:10Z'.", ex.getMessage());
        }

        @Test
        void valueBelowMax_returnsValue() {
            assertEquals(NINE, Instants.optionalMaxExclusive("field", NINE, TEN));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalRange
    // -------------------------------------------------------------------------

    @Nested
    class OptionalRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxLessThanMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.optionalRange("field", SEVEN, FIVE, FOUR));
            assertEquals("The 'max' value of '1970-01-01T00:00:04Z' is invalid. The value must be greater than or equal to the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalRange("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalRange("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, Instants.optionalRange("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.optionalRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, Instants.optionalRange("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalRange("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalExclusiveRange(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalExclusiveRange("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalExclusiveRange("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalExclusiveRange("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.optionalExclusiveRange("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalExclusiveRange("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalExclusiveRange("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.optionalExclusiveRange("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalExclusiveRange("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMax(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMax("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMax("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMax("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.optionalRangeExclusiveMax("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalRangeExclusiveMax("field", null, FIVE, TEN));
        }

        @Test
        void valueBelowMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalRangeExclusiveMax("field", FOUR, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than or equal to '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            assertEquals(FIVE, Instants.optionalRangeExclusiveMax("field", FIVE, FIVE, TEN));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.optionalRangeExclusiveMax("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalRangeExclusiveMax("field", TEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMin(null, SEVEN, FIVE, TEN));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMin("   ", SEVEN, FIVE, TEN));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullMin_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMin("field", SEVEN, null, TEN));
            assertEquals(NULL_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullMax_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalRangeExclusiveMin("field", SEVEN, FIVE, null));
            assertEquals(NULL_MAX_MSG, ex.getMessage());
        }

        @Test
        void maxEqualToMin_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Instants.optionalRangeExclusiveMin("field", SEVEN, FIVE, FIVE));
            assertEquals("The 'max' value of '1970-01-01T00:00:05Z' is invalid. The value must be greater than the 'min' value of '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalRangeExclusiveMin("field", null, FIVE, TEN));
        }

        @Test
        void valueAtMin_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalRangeExclusiveMin("field", FIVE, FIVE, TEN));
            assertEquals("The 'field' value is invalid. The value must be greater than '1970-01-01T00:00:05Z'.", ex.getMessage());
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            assertEquals(SEVEN, Instants.optionalRangeExclusiveMin("field", SEVEN, FIVE, TEN));
        }

        @Test
        void valueAtMax_returnsValue() {
            assertEquals(TEN, Instants.optionalRangeExclusiveMin("field", TEN, FIVE, TEN));
        }

        @Test
        void valueAboveMax_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalRangeExclusiveMin("field", ELEVEN, FIVE, TEN));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPast(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPast("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalPast("field", null));
        }

        @Test
        void futureValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalPast("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, Instants.optionalPast("field", CLEARLY_PAST));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPast("field", CLEARLY_PAST, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(Instants.optionalPast("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalPast("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalPast("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, Instants.optionalPast("field", PAST_VALUE, FIXED_CLOCK));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalPastOrPresent
    // -------------------------------------------------------------------------

    @Nested
    class OptionalPastOrPresent {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPastOrPresent(null, CLEARLY_PAST));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPastOrPresent("   ", CLEARLY_PAST));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalPastOrPresent("field", null));
        }

        @Test
        void futureValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalPastOrPresent("field", CLEARLY_FUTURE));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValue_returnsValue() {
            assertEquals(CLEARLY_PAST, Instants.optionalPastOrPresent("field", CLEARLY_PAST));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPastOrPresent("field", CLEARLY_PAST, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(Instants.optionalPastOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, Instants.optionalPastOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void futureValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalPastOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_returnsValue() {
            assertEquals(PAST_VALUE, Instants.optionalPastOrPresent("field", PAST_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPastOrPresent("field", CLEARLY_PAST, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(Instants.optionalPastOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void pastValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_PAST, Instants.optionalPastOrPresent("field", CLEARLY_PAST, Duration.ofSeconds(30)));
        }

        @Test
        void futureValueWithTolerance_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalPastOrPresent("field", CLEARLY_FUTURE, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the past or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPastOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalPastOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(Instants.optionalPastOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.plusSeconds(5);
            assertEquals(withinTolerance, Instants.optionalPastOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class,
                    () -> Instants.optionalPastOrPresent("field", FIXED_NOW.plusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFutureOrPresent(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFutureOrPresent("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalFutureOrPresent("field", null));
        }

        @Test
        void pastValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalFutureOrPresent("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, Instants.optionalFutureOrPresent("field", CLEARLY_FUTURE));
        }

        // --- with clock overload ---

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Clock) null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(Instants.optionalFutureOrPresent("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_returnsValue() {
            assertEquals(FIXED_NOW, Instants.optionalFutureOrPresent("field", FIXED_NOW, FIXED_CLOCK));
        }

        @Test
        void pastValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalFutureOrPresent("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, Instants.optionalFutureOrPresent("field", FUTURE_VALUE, FIXED_CLOCK));
        }

        // --- with tolerance overload ---

        @Test
        void nullTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFutureOrPresent("field", CLEARLY_FUTURE, (Duration) null));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithTolerance_returnsNull() {
            assertNull(Instants.optionalFutureOrPresent("field", null, Duration.ZERO));
        }

        @Test
        void futureValueWithTolerance_returnsValue() {
            assertEquals(CLEARLY_FUTURE, Instants.optionalFutureOrPresent("field", CLEARLY_FUTURE, Duration.ofSeconds(30)));
        }

        @Test
        void pastValueWithTolerance_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalFutureOrPresent("field", CLEARLY_PAST, Duration.ZERO));
            assertEquals("The 'field' value is invalid. The value must be in the future or the present.", ex.getMessage());
        }

        // --- with clock and tolerance overload ---

        @Test
        void nullToleranceWithClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFutureOrPresent("field", FIXED_NOW, null, FIXED_CLOCK));
            assertEquals(NULL_TOLERANCE_MSG, ex.getMessage());
        }

        @Test
        void nullClockWithTolerance_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFutureOrPresent("field", FIXED_NOW, Duration.ZERO, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithToleranceAndClock_returnsNull() {
            assertNull(Instants.optionalFutureOrPresent("field", null, Duration.ZERO, FIXED_CLOCK));
        }

        @Test
        void valueWithinTolerance_returnsValue() {
            var withinTolerance = FIXED_NOW.minusSeconds(5);
            assertEquals(withinTolerance, Instants.optionalFutureOrPresent("field", withinTolerance, Duration.ofSeconds(5), FIXED_CLOCK));
        }

        @Test
        void valueExceedingTolerance_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class,
                    () -> Instants.optionalFutureOrPresent("field", FIXED_NOW.minusSeconds(6), Duration.ofSeconds(5), FIXED_CLOCK));
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
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFuture(null, CLEARLY_FUTURE));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFuture("   ", CLEARLY_FUTURE));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Instants.optionalFuture("field", null));
        }

        @Test
        void pastValue_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalFuture("field", CLEARLY_PAST));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValue_returnsValue() {
            assertEquals(CLEARLY_FUTURE, Instants.optionalFuture("field", CLEARLY_FUTURE));
        }

        @Test
        void nullClock_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Instants.optionalFuture("field", CLEARLY_FUTURE, null));
            assertEquals(NULL_CLOCK_MSG, ex.getMessage());
        }

        @Test
        void nullValueWithClock_returnsNull() {
            assertNull(Instants.optionalFuture("field", null, FIXED_CLOCK));
        }

        @Test
        void valueAtNowWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalFuture("field", FIXED_NOW, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void pastValueWithClock_throwsInstantOutsideRangeException() {
            var ex = assertThrows(InstantOutsideRangeException.class, () -> Instants.optionalFuture("field", PAST_VALUE, FIXED_CLOCK));
            assertEquals("The 'field' value is invalid. The value must be in the future.", ex.getMessage());
        }

        @Test
        void futureValueWithClock_returnsValue() {
            assertEquals(FUTURE_VALUE, Instants.optionalFuture("field", FUTURE_VALUE, FIXED_CLOCK));
        }
    }
}

