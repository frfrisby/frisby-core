package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class StringsTest {
    private static final Pattern ALPHA = Pattern.compile("[a-z]+");

    private static final String NULL_NAME_MSG =
            "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG =
            "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_PATTERN_MSG =
            "The 'pattern' value was not provided.";
    private static final String NULL_VALUE_MSG =
            "The 'field' value is invalid. The value must not be null.";
    private static final String EMPTY_VALUE_MSG =
            "The 'field' value is invalid. The value must not be empty.";
    private static final String BLANK_VALUE_MSG =
            "The 'field' value is invalid. The value must not contain only whitespace characters.";
    private static final String PATTERN_MISMATCH_MSG =
            "The 'field' value is invalid. The value does not match the expected pattern.";
    private static final String INVALID_MAX_LENGTH_MSG =
            "The 'maxLength' value of '0' is invalid. The value must be greater than or equal to '1'.";
    private static final String INVALID_MIN_LENGTH_MSG =
            "The 'minLength' value of '0' is invalid. The value must be greater than or equal to '1'.";
    private static final String INVALID_LENGTH_MSG =
            "The 'length' value of '0' is invalid. The value must be greater than or equal to '1'.";
    private static final String MAX_LESS_THAN_MIN_MSG =
            "The 'maxLength' value of '2' is invalid. The value must be greater than or equal to the 'minLength' value of '5'.";
    private static final String TOO_LONG_MSG =
            "The 'field' value is invalid. The value cannot exceed a maximum length of '3' Unicode code points.";
    private static final String TOO_SHORT_MSG =
            "The 'field' value is invalid. The value must contain at least '8' Unicode code points.";
    private static final String WRONG_EXACT_MSG =
            "The 'field' value is invalid. The value must be exactly '3' Unicode code points.";

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notNull(null, "value"));
            assertEquals("The 'name' value was not provided.", ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notNull("   ", "value"));
            assertEquals("The 'name' value is invalid. The value must be non null and cannot contain only white space characters.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notNull("field", null));
            assertEquals("The 'field' value is invalid. The value must not be null.", ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            String value = "hello";
            assertEquals(value, Strings.notNull("field", value));
        }

        @Test
        void emptyValue_returnsEmptyString() {
            assertEquals("", Strings.notNull("field", ""));
        }
    }

    @Nested
    class NotEmpty {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmpty(null, "value"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmpty("   ", "value"));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmpty("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmpty("field", ""));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_returnsValue() {
            assertEquals("   ", Strings.notEmpty("field", "   "));
        }

        @Test
        void nonEmptyValue_returnsValue() {
            assertEquals("hello", Strings.notEmpty("field", "hello"));
        }
    }

    @Nested
    class NotBlank {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlank(null, "value"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlank("   ", "value"));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlank("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlank("field", ""));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlank("field", "   "));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonBlankValue_returnsValue() {
            assertEquals("hello", Strings.notBlank("field", "hello"));
        }
    }

    @Nested
    class NotNullWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notNullWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notNullWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notNullWithMatches("field", null, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.notNullWithMatches("field", "HELLO", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void matchingValue_returnsValue() {
            assertEquals("hello", Strings.notNullWithMatches("field", "hello", ALPHA));
        }
    }

    @Nested
    class NotEmptyWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmptyWithMatches("field", null, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmptyWithMatches("field", "", ALPHA));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.notEmptyWithMatches("field", "HELLO", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void matchingValue_returnsValue() {
            assertEquals("hello", Strings.notEmptyWithMatches("field", "hello", ALPHA));
        }
    }

    @Nested
    class NotBlankWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlankWithMatches("field", null, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlankWithMatches("field", "   ", ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.notBlankWithMatches("field", "HELLO", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void matchingValue_returnsValue() {
            assertEquals("hello", Strings.notBlankWithMatches("field", "hello", ALPHA));
        }
    }

    @Nested
    class NotNullWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notNullWithMaxLengthAndMatches(null, "hi", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notNullWithMaxLengthAndMatches("field", "hi", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notNullWithMaxLengthAndMatches("field", "hi", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notNullWithMaxLengthAndMatches("field", null, 5, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notNullWithMaxLengthAndMatches("field", "too long", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.notNullWithMaxLengthAndMatches("field", "HELLO", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notNullWithMaxLengthAndMatches("field", "hello", 5, ALPHA));
        }
    }

    @Nested
    class NotEmptyWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithMaxLengthAndMatches(null, "hi", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithMaxLengthAndMatches("field", "hi", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithMaxLengthAndMatches("field", "hi", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmptyWithMaxLengthAndMatches("field", null, 5, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmptyWithMaxLengthAndMatches("field", "", 5, ALPHA));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notEmptyWithMaxLengthAndMatches("field", "too long", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.notEmptyWithMaxLengthAndMatches("field", "HELLO", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notEmptyWithMaxLengthAndMatches("field", "hello", 5, ALPHA));
        }
    }

    @Nested
    class NotBlankWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithMaxLengthAndMatches(null, "hi", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithMaxLengthAndMatches("field", "hi", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithMaxLengthAndMatches("field", "hi", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlankWithMaxLengthAndMatches("field", null, 5, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlankWithMaxLengthAndMatches("field", "   ", 5, ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notBlankWithMaxLengthAndMatches("field", "too long", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.notBlankWithMaxLengthAndMatches("field", "HELLO", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notBlankWithMaxLengthAndMatches("field", "hello", 5, ALPHA));
        }
    }

    @Nested
    class MaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.maxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.maxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.maxLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.maxLength("field", "too long", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void valueAtMaxLength_returnsValue() {
            assertEquals("hello", Strings.maxLength("field", "hello", 5));
        }

        @Test
        void valueShorterThanMaxLength_returnsValue() {
            assertEquals("hi", Strings.maxLength("field", "hi", 5));
        }
    }

    @Nested
    class MinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.minLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.minLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.minLength("field", null, 3));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.minLength("field", "hi", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void valueAtMinLength_returnsValue() {
            assertEquals("hello", Strings.minLength("field", "hello", 5));
        }

        @Test
        void valueLongerThanMinLength_returnsValue() {
            assertEquals("hello", Strings.minLength("field", "hello", 3));
        }
    }

    @Nested
    class LengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.length(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.length("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.length("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.length("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void maxLengthEqualToMinLength_actsAsExactLength() {
            assertEquals("hello", Strings.length("field", "hello", 5, 5));
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.length("field", null, 3, 8));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.length("field", "hi", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.length("field", "too long", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithinRange_returnsValue() {
            assertEquals("hello", Strings.length("field", "hello", 3, 8));
        }
    }

    @Nested
    class LengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.length(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.length("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.length("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void wrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.length("field", "too long", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void correctLength_returnsValue() {
            assertEquals("hello", Strings.length("field", "hello", 5));
        }
    }

    @Nested
    class NotEmptyWithMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmptyWithMaxLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmptyWithMaxLength("field", "", 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notEmptyWithMaxLength("field", "too long", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notEmptyWithMaxLength("field", "hello", 5));
        }
    }

    @Nested
    class NotEmptyWithMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmptyWithMinLength("field", null, 3));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmptyWithMinLength("field", "", 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notEmptyWithMinLength("field", "hi", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notEmptyWithMinLength("field", "hello", 3));
        }
    }

    @Nested
    class NotEmptyWithLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmptyWithLength("field", null, 3, 8));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmptyWithLength("field", "", 3, 8));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notEmptyWithLength("field", "hi", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notEmptyWithLength("field", "too long", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithinRange_returnsValue() {
            assertEquals("hello", Strings.notEmptyWithLength("field", "hello", 3, 8));
        }
    }

    @Nested
    class NotEmptyWithLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notEmptyWithLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notEmptyWithLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notEmptyWithLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.notEmptyWithLength("field", "", 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void wrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notEmptyWithLength("field", "too long", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void correctLength_returnsValue() {
            assertEquals("hello", Strings.notEmptyWithLength("field", "hello", 5));
        }
    }

    @Nested
    class NotBlankWithMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlankWithMaxLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlankWithMaxLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notBlankWithMaxLength("field", "too long", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notBlankWithMaxLength("field", "hello", 5));
        }
    }

    @Nested
    class NotBlankWithMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlankWithMinLength("field", null, 3));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlankWithMinLength("field", "   ", 3));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notBlankWithMinLength("field", "hi", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.notBlankWithMinLength("field", "hello", 3));
        }
    }

    @Nested
    class NotBlankWithLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlankWithLength("field", null, 3, 8));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlankWithLength("field", "   ", 3, 8));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notBlankWithLength("field", "hi", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notBlankWithLength("field", "too long", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithinRange_returnsValue() {
            assertEquals("hello", Strings.notBlankWithLength("field", "hello", 3, 8));
        }
    }

    @Nested
    class NotBlankWithLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.notBlankWithLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.notBlankWithLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Strings.notBlankWithLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.notBlankWithLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void wrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.notBlankWithLength("field", "too long", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void correctLength_returnsValue() {
            assertEquals("hello", Strings.notBlankWithLength("field", "hello", 5));
        }
    }

    @Nested
    class OptionalNotEmpty {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmpty(null, "hello"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmpty("field", null));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmpty("field", ""));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_returnsValue() {
            assertEquals("   ", Strings.optionalNotEmpty("field", "   "));
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmpty("field", "hello"));
        }
    }

    @Nested
    class OptionalNotBlank {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlank(null, "hello"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlank("field", null));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlank("field", "   "));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlank("field", "hello"));
        }
    }

    @Nested
    class OptionalMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalMatches("field", null, ALPHA));
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.optionalMatches("field", "HELLO", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void matchingValue_returnsValue() {
            assertEquals("hello", Strings.optionalMatches("field", "hello", ALPHA));
        }
    }

    @Nested
    class OptionalNotEmptyWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmptyWithMatches("field", null, ALPHA));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmptyWithMatches("field", "", ALPHA));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.optionalNotEmptyWithMatches("field", "HELLO", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void matchingValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmptyWithMatches("field", "hello", ALPHA));
        }
    }

    @Nested
    class OptionalNotBlankWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlankWithMatches("field", null, ALPHA));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlankWithMatches("field", "   ", ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.optionalNotBlankWithMatches("field", "HELLO", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void matchingValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlankWithMatches("field", "hello", ALPHA));
        }
    }

    @Nested
    class OptionalMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalMaxLengthAndMatches(null, "hello", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalMaxLengthAndMatches("field", "hello", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalMaxLengthAndMatches("field", "hello", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalMaxLengthAndMatches("field", null, 5, ALPHA));
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalMaxLengthAndMatches("field", "too long", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.optionalMaxLengthAndMatches("field", "HELLO", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalMaxLengthAndMatches("field", "hello", 5, ALPHA));
        }
    }

    @Nested
    class OptionalNotEmptyWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithMaxLengthAndMatches(null, "hello", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithMaxLengthAndMatches("field", "hello", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithMaxLengthAndMatches("field", "hello", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmptyWithMaxLengthAndMatches("field", null, 5, ALPHA));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmptyWithMaxLengthAndMatches("field", "", 5, ALPHA));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotEmptyWithMaxLengthAndMatches("field", "too long", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.optionalNotEmptyWithMaxLengthAndMatches("field", "HELLO", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmptyWithMaxLengthAndMatches("field", "hello", 5, ALPHA));
        }
    }

    @Nested
    class OptionalNotBlankWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithMaxLengthAndMatches(null, "hello", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithMaxLengthAndMatches("field", "hello", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithMaxLengthAndMatches("field", "hello", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlankWithMaxLengthAndMatches("field", null, 5, ALPHA));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlankWithMaxLengthAndMatches("field", "   ", 5, ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotBlankWithMaxLengthAndMatches("field", "too long", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void noMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> Strings.optionalNotBlankWithMaxLengthAndMatches("field", "HELLO", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlankWithMaxLengthAndMatches("field", "hello", 5, ALPHA));
        }
    }

    @Nested
    class OptionalMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalMaxLength("field", null, 5));
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalMaxLength("field", "too long", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalMaxLength("field", "hello", 5));
        }
    }

    @Nested
    class OptionalNotEmptyWithMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmptyWithMaxLength("field", null, 5));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmptyWithMaxLength("field", "", 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotEmptyWithMaxLength("field", "too long", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmptyWithMaxLength("field", "hello", 5));
        }
    }

    @Nested
    class OptionalNotBlankWithMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlankWithMaxLength("field", null, 5));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlankWithMaxLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotBlankWithMaxLength("field", "too long", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlankWithMaxLength("field", "hello", 5));
        }
    }

    @Nested
    class OptionalMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalMinLength("field", null, 3));
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalMinLength("field", "hi", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalMinLength("field", "hello", 3));
        }
    }

    @Nested
    class OptionalNotEmptyWithMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmptyWithMinLength("field", null, 3));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmptyWithMinLength("field", "", 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotEmptyWithMinLength("field", "hi", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmptyWithMinLength("field", "hello", 3));
        }
    }

    @Nested
    class OptionalNotBlankWithMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlankWithMinLength("field", null, 3));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlankWithMinLength("field", "   ", 3));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotBlankWithMinLength("field", "hi", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlankWithMinLength("field", "hello", 3));
        }
    }

    @Nested
    class OptionalLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalLength("field", null, 3, 8));
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalLength("field", "hi", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalLength("field", "too long", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithinRange_returnsValue() {
            assertEquals("hello", Strings.optionalLength("field", "hello", 3, 8));
        }
    }

    @Nested
    class OptionalLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalLength("field", null, 5));
        }

        @Test
        void wrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalLength("field", "too long", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void correctLength_returnsValue() {
            assertEquals("hello", Strings.optionalLength("field", "hello", 5));
        }
    }

    @Nested
    class OptionalNotEmptyWithLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmptyWithLength("field", null, 3, 8));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmptyWithLength("field", "", 3, 8));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotEmptyWithLength("field", "hi", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotEmptyWithLength("field", "too long", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithinRange_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmptyWithLength("field", "hello", 3, 8));
        }
    }

    @Nested
    class OptionalNotEmptyWithLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotEmptyWithLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotEmptyWithLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotEmptyWithLength("field", null, 5));
        }

        @Test
        void emptyValue_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> Strings.optionalNotEmptyWithLength("field", "", 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void wrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotEmptyWithLength("field", "too long", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void correctLength_returnsValue() {
            assertEquals("hello", Strings.optionalNotEmptyWithLength("field", "hello", 5));
        }
    }

    @Nested
    class OptionalNotBlankWithLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlankWithLength("field", null, 3, 8));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlankWithLength("field", "   ", 3, 8));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotBlankWithLength("field", "hi", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotBlankWithLength("field", "too long", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithinRange_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlankWithLength("field", "hello", 3, 8));
        }
    }

    @Nested
    class OptionalNotBlankWithLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Strings.optionalNotBlankWithLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Strings.optionalNotBlankWithLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Strings.optionalNotBlankWithLength("field", null, 5));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> Strings.optionalNotBlankWithLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void wrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> Strings.optionalNotBlankWithLength("field", "too long", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void correctLength_returnsValue() {
            assertEquals("hello", Strings.optionalNotBlankWithLength("field", "hello", 5));
        }
    }
}
