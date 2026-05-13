package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.junit.jupiter.api.Assertions.*;

class PeriodsTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";

    private static final Period FIVE_DAYS = Period.ofDays(5);
    private static final Period NEG_ONE_DAY = Period.ofDays(-1);
    private static final Period MIXED_SIGN = Period.of(1, -1, 0);  // 1 year, -1 month — isNegative() == true

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.notNull(null, FIVE_DAYS));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.notNull("   ", FIVE_DAYS));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Periods.notNull("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            assertEquals(FIVE_DAYS, Periods.notNull("field", FIVE_DAYS));
        }
    }

    @Nested
    class Positive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.positive(null, FIVE_DAYS));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.positive("   ", FIVE_DAYS));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Periods.positive("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void zeroValue_throwsPeriodOutsideRangeException() {
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.positive("field", Period.ZERO));
            assertEquals("The 'field' value is invalid. The value must be positive.", ex.getMessage());
        }

        @Test
        void negativeValue_throwsPeriodOutsideRangeException() {
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.positive("field", NEG_ONE_DAY));
            assertEquals("The 'field' value is invalid. The value must be positive.", ex.getMessage());
        }

        @Test
        void mixedSignValue_throwsPeriodOutsideRangeException() {
            // Period.of(1, -1, 0): isNegative() returns true because the months component is negative,
            // regardless of the positive years component.
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.positive("field", MIXED_SIGN));
            assertEquals("The 'field' value is invalid. The value must be positive.", ex.getMessage());
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(FIVE_DAYS, Periods.positive("field", FIVE_DAYS));
        }
    }

    @Nested
    class NotNegative {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.notNegative(null, FIVE_DAYS));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.notNegative("   ", FIVE_DAYS));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Periods.notNegative("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void negativeValue_throwsPeriodOutsideRangeException() {
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.notNegative("field", NEG_ONE_DAY));
            assertEquals("The 'field' value is invalid. The value must not be negative.", ex.getMessage());
        }

        @Test
        void mixedSignValue_throwsPeriodOutsideRangeException() {
            // Period.of(1, -1, 0): isNegative() returns true because the months component is negative,
            // regardless of the positive years component.
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.notNegative("field", MIXED_SIGN));
            assertEquals("The 'field' value is invalid. The value must not be negative.", ex.getMessage());
        }

        @Test
        void zeroValue_returnsValue() {
            assertEquals(Period.ZERO, Periods.notNegative("field", Period.ZERO));
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(FIVE_DAYS, Periods.notNegative("field", FIVE_DAYS));
        }
    }

    @Nested
    class OptionalPositive {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.optionalPositive(null, FIVE_DAYS));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.optionalPositive("   ", FIVE_DAYS));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Periods.optionalPositive("field", null));
        }

        @Test
        void zeroValue_throwsPeriodOutsideRangeException() {
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.optionalPositive("field", Period.ZERO));
            assertEquals("The 'field' value is invalid. The value must be positive.", ex.getMessage());
        }

        @Test
        void negativeValue_throwsPeriodOutsideRangeException() {
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.optionalPositive("field", NEG_ONE_DAY));
            assertEquals("The 'field' value is invalid. The value must be positive.", ex.getMessage());
        }

        @Test
        void mixedSignValue_throwsPeriodOutsideRangeException() {
            // Period.of(1, -1, 0): isNegative() returns true because the months component is negative,
            // regardless of the positive years component.
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.optionalPositive("field", MIXED_SIGN));
            assertEquals("The 'field' value is invalid. The value must be positive.", ex.getMessage());
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(FIVE_DAYS, Periods.optionalPositive("field", FIVE_DAYS));
        }
    }

    @Nested
    class OptionalNotNegative {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.optionalNotNegative(null, FIVE_DAYS));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Periods.optionalNotNegative("   ", FIVE_DAYS));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Periods.optionalNotNegative("field", null));
        }

        @Test
        void negativeValue_throwsPeriodOutsideRangeException() {
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.optionalNotNegative("field", NEG_ONE_DAY));
            assertEquals("The 'field' value is invalid. The value must not be negative.", ex.getMessage());
        }

        @Test
        void mixedSignValue_throwsPeriodOutsideRangeException() {
            // Period.of(1, -1, 0): isNegative() returns true because the months component is negative,
            // regardless of the positive years component.
            var ex = assertThrows(PeriodOutsideRangeException.class, () -> Periods.optionalNotNegative("field", MIXED_SIGN));
            assertEquals("The 'field' value is invalid. The value must not be negative.", ex.getMessage());
        }

        @Test
        void zeroValue_returnsValue() {
            assertEquals(Period.ZERO, Periods.optionalNotNegative("field", Period.ZERO));
        }

        @Test
        void positiveValue_returnsValue() {
            assertEquals(FIVE_DAYS, Periods.optionalNotNegative("field", FIVE_DAYS));
        }
    }
}

