package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TrimmedStringsTest {
    private static final Pattern ALPHA = Pattern.compile("[a-z]+");

    private static final String NULL_NAME_MSG =
            "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG =
            "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_PATTERN_MSG =
            "The 'pattern' value was not provided.";
    private static final String NULL_VALUE_MSG =
            "The 'field' value is invalid. The value must not be null.";
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

    // -------------------------------------------------------------------------
    // notBlank
    // -------------------------------------------------------------------------

    @Nested
    class NotBlank {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlank(null, "hello"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlank("   ", "hello"));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlank("field", null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlank("field", "   "));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.notBlank("field", "  hello  "));
        }

        @Test
        void valueWithNoWhitespace_returnsValue() {
            assertEquals("hello", TrimmedStrings.notBlank("field", "hello"));
        }
    }

    // -------------------------------------------------------------------------
    // notBlankWithMaxLength
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlankWithMaxLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlankWithMaxLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooLong_throwsStringLengthOutsideRangeException() {
            // "  too long  " strips to "too long" (7 code points), which exceeds maxLength of 3
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.notBlankWithMaxLength("field", "  too long  ", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            // "  hello  " strips to "hello" (5 code points), which is within maxLength of 5
            assertEquals("hello", TrimmedStrings.notBlankWithMaxLength("field", "  hello  ", 5));
        }

        @Test
        void valueWithNoWhitespace_returnsValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithMaxLength("field", "hello", 5));
        }
    }

    // -------------------------------------------------------------------------
    // notBlankWithMinLength
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlankWithMinLength("field", null, 3));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlankWithMinLength("field", "   ", 3));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooShort_throwsStringLengthOutsideRangeException() {
            // "  hi  " strips to "hi" (2 code points), which is below minLength of 8
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.notBlankWithMinLength("field", "  hi  ", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithMinLength("field", "  hello  ", 3));
        }

        @Test
        void valueWithNoWhitespace_returnsValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithMinLength("field", "hello", 3));
        }
    }

    // -------------------------------------------------------------------------
    // notBlankWithLength (range overload)
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlankWithLength("field", null, 3, 8));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlankWithLength("field", "   ", 3, 8));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.notBlankWithLength("field", "  hi  ", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void strippedValueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.notBlankWithLength("field", "  too long  ", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithLength("field", "  hello  ", 3, 8));
        }
    }

    // -------------------------------------------------------------------------
    // notBlankWithLength (exact overload)
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlankWithLength("field", null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlankWithLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueWrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.notBlankWithLength("field", "  too long  ", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithLength("field", "  hello  ", 5));
        }
    }

    // -------------------------------------------------------------------------
    // notBlankWithMatches
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlankWithMatches("field", null, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlankWithMatches("field", "   ", ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueNoMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> TrimmedStrings.notBlankWithMatches("field", "  HELLO  ", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithMatches("field", "  hello  ", ALPHA));
        }
    }

    // -------------------------------------------------------------------------
    // notBlankWithMaxLengthAndMatches
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches(null, "hello", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches("field", "hello", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches("field", "hello", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches("field", null, 5, ALPHA));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches("field", "   ", 5, ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches("field", "  too long  ", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void strippedValueNoMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> TrimmedStrings.notBlankWithMaxLengthAndMatches("field", "  HELLO  ", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.notBlankWithMaxLengthAndMatches("field", "  hello  ", 5, ALPHA));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlank
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlank {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlank(null, "hello"));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlank("field", null));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlank("field", "   "));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlank("field", "  hello  "));
        }

        @Test
        void valueWithNoWhitespace_returnsValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlank("field", "hello"));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlankWithMaxLength
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlankWithMaxLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLength("field", "hello", 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlankWithMaxLength("field", null, 5));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLength("field", "  too long  ", 3));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlankWithMaxLength("field", "  hello  ", 5));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlankWithMinLength
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlankWithMinLength {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithMinLength(null, "hello", 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithMinLength("field", "hello", 0));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlankWithMinLength("field", null, 3));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlankWithMinLength("field", "   ", 3));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.optionalNotBlankWithMinLength("field", "  hi  ", 8));
            assertEquals(TOO_SHORT_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlankWithMinLength("field", "  hello  ", 3));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlankWithLength (range overload)
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlankWithLengthRange {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithLength(null, "hello", 3, 8));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "hello", 3, 0));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void invalidMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "hello", 0, 8));
            assertEquals(INVALID_MIN_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void maxLengthLessThanMinLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "hello", 5, 2));
            assertEquals(MAX_LESS_THAN_MIN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlankWithLength("field", null, 3, 8));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "   ", 3, 8));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooShort_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "  hi  ", 3, 8));
            assertEquals("The 'field' value is invalid. The value must contain at least '3' Unicode code points.", ex.getMessage());
        }

        @Test
        void strippedValueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "  too long  ", 3, 5));
            assertEquals("The 'field' value is invalid. The value cannot exceed a maximum length of '5' Unicode code points.", ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlankWithLength("field", "  hello  ", 3, 8));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlankWithLength (exact overload)
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlankWithLengthExact {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithLength(null, "hello", 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "hello", 0));
            assertEquals(INVALID_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlankWithLength("field", null, 5));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "   ", 5));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueWrongLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.optionalNotBlankWithLength("field", "  too long  ", 3));
            assertEquals(WRONG_EXACT_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlankWithLength("field", "  hello  ", 5));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlankWithMatches
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlankWithMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithMatches(null, "hello", ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithMatches("field", "hello", null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlankWithMatches("field", null, ALPHA));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlankWithMatches("field", "   ", ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueNoMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> TrimmedStrings.optionalNotBlankWithMatches("field", "  HELLO  ", ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlankWithMatches("field", "  hello  ", ALPHA));
        }
    }

    // -------------------------------------------------------------------------
    // optionalNotBlankWithMaxLengthAndMatches
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotBlankWithMaxLengthAndMatches {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches(null, "hello", 5, ALPHA));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void invalidMaxLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", "hello", 0, ALPHA));
            assertEquals(INVALID_MAX_LENGTH_MSG, ex.getMessage());
        }

        @Test
        void nullPattern_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", "hello", 5, null));
            assertEquals(NULL_PATTERN_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", null, 5, ALPHA));
        }

        @Test
        void blankValue_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", "   ", 5, ALPHA));
            assertEquals(BLANK_VALUE_MSG, ex.getMessage());
        }

        @Test
        void strippedValueTooLong_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", "  too long  ", 3, ALPHA));
            assertEquals(TOO_LONG_MSG, ex.getMessage());
        }

        @Test
        void strippedValueNoMatch_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", "  HELLO  ", 5, ALPHA));
            assertEquals(PATTERN_MISMATCH_MSG, ex.getMessage());
        }

        @Test
        void valueWithSurroundingWhitespace_returnsStrippedValue() {
            assertEquals("hello", TrimmedStrings.optionalNotBlankWithMaxLengthAndMatches("field", "  hello  ", 5, ALPHA));
        }
    }
}

