package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class StringSequencesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String EMPTY_VALUE_MSG = "The 'field' value is invalid. The value must not be empty.";
    private static final String NULL_ELEMENT_MSG = "The 'field' value is invalid. The value must not contain null elements.";
    private static final String NULL_PATTERN_MSG = "The 'pattern' value was not provided.";

    private static final Pattern LOWERCASE = Pattern.compile("[a-z]+");

    // -------------------------------------------------------------------------
    // NotEmpty
    // -------------------------------------------------------------------------

    @Nested
    class NotEmpty {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notEmpty(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notEmpty("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notEmpty("field", (java.util.List<String>) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notEmpty("field", List.of()));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notEmpty("field", Arrays.asList("a", null)));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void emptyElement_throwsEmptyValueException() {
                var ex = assertThrows(EmptyValueException.class, () -> StringSequences.notEmpty("field", Arrays.asList("a", "", "b")));
                assertEquals("The 'field' value is invalid. The value must not contain empty elements. Element at index '1' is empty.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notEmpty("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notEmpty(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notEmpty("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notEmpty("field", (String[]) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notEmpty("field", new String[0]));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notEmpty("field", new String[]{"a", null}));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void emptyElement_throwsEmptyValueException() {
                var ex = assertThrows(EmptyValueException.class, () -> StringSequences.notEmpty("field", new String[]{"a", "", "b"}));
                assertEquals("The 'field' value is invalid. The value must not contain empty elements. Element at index '1' is empty.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notEmpty("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotBlank
    // -------------------------------------------------------------------------

    @Nested
    class NotBlank {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlank(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlank("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlank("field", (java.util.List<String>) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlank("field", List.of()));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlank("field", Arrays.asList("a", null)));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlank("field", Arrays.asList("a", "   ", "b")));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void emptyElementIsBlank_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlank("field", Arrays.asList("a", "")));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notBlank("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlank(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlank("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlank("field", (String[]) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlank("field", new String[0]));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlank("field", new String[]{"a", null}));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlank("field", new String[]{"a", "   ", "b"}));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notBlank("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // MaxLength
    // -------------------------------------------------------------------------

    @Nested
    class MaxLength {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.maxLength(null, List.of("a"), 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.maxLength("   ", List.of("a"), 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.maxLength("field", List.of("a"), 0));
                assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.maxLength("field", (java.util.List<String>) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.maxLength("field", List.of(), 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.maxLength("field", Arrays.asList("ab", null), 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.maxLength("field", List.of("abc", "too long"), 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAtMaxLength_returnsValue() {
                var list = List.of("abc", "hello");
                assertSame(list, StringSequences.maxLength("field", list, 5));
            }

            @Test
            void allElementsBelowMaxLength_returnsValue() {
                var list = List.of("a", "bb");
                assertSame(list, StringSequences.maxLength("field", list, 5));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.maxLength(null, new String[]{"a"}, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.maxLength("   ", new String[]{"a"}, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.maxLength("field", new String[]{"a"}, 0));
                assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.maxLength("field", (String[]) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.maxLength("field", new String[0], 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.maxLength("field", new String[]{"ab", null}, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.maxLength("field", new String[]{"abc", "too long"}, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAtMaxLength_returnsValue() {
                var array = new String[]{"abc", "hello"};
                assertSame(array, StringSequences.maxLength("field", array, 5));
            }
        }
    }

    // -------------------------------------------------------------------------
    // MinLength
    // -------------------------------------------------------------------------

    @Nested
    class MinLength {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.minLength(null, List.of("abc"), 3));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.minLength("   ", List.of("abc"), 3));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.minLength("field", List.of("abc"), 0));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.minLength("field", (java.util.List<String>) null, 3));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.minLength("field", List.of(), 3));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.minLength("field", Arrays.asList("abc", null), 3));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.minLength("field", List.of("hello", "ab"), 3));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAtMinLength_returnsValue() {
                var list = List.of("abc", "hello");
                assertSame(list, StringSequences.minLength("field", list, 3));
            }

            @Test
            void allElementsAboveMinLength_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.minLength("field", list, 3));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.minLength(null, new String[]{"abc"}, 3));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.minLength("   ", new String[]{"abc"}, 3));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.minLength("field", new String[]{"abc"}, 0));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.minLength("field", (String[]) null, 3));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.minLength("field", new String[0], 3));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.minLength("field", new String[]{"abc", null}, 3));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.minLength("field", new String[]{"hello", "ab"}, 3));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAtMinLength_returnsValue() {
                var array = new String[]{"abc", "hello"};
                assertSame(array, StringSequences.minLength("field", array, 3));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Length
    // -------------------------------------------------------------------------

    @Nested
    class Length {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.length(null, List.of("abc"), 2, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.length("   ", List.of("abc"), 2, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.length("field", List.of("abc"), 0, 5));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxItemLengthLessThanMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.length("field", List.of("abc"), 5, 3));
                assertEquals("The 'maxItemLength' value of '3' is invalid. The value must be greater than or equal to the 'minItemLength' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.length("field", (java.util.List<String>) null, 2, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.length("field", List.of(), 2, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.length("field", Arrays.asList("abc", null), 2, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.length("field", List.of("abc", "a"), 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAboveMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.length("field", List.of("abc", "too long"), 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAtMinLength_returnsValue() {
                var list = List.of("ab", "abc");
                assertSame(list, StringSequences.length("field", list, 2, 5));
            }

            @Test
            void elementAtMaxLength_returnsValue() {
                var list = List.of("abc", "hello");
                assertSame(list, StringSequences.length("field", list, 2, 5));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.length(null, new String[]{"abc"}, 2, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.length("   ", new String[]{"abc"}, 2, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.length("field", new String[]{"abc"}, 0, 5));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxItemLengthLessThanMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.length("field", new String[]{"abc"}, 5, 3));
                assertEquals("The 'maxItemLength' value of '3' is invalid. The value must be greater than or equal to the 'minItemLength' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.length("field", (String[]) null, 2, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.length("field", new String[0], 2, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.length("field", new String[]{"abc", null}, 2, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.length("field", new String[]{"abc", "a"}, 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAboveMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.length("field", new String[]{"abc", "too long"}, 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementWithinRange_returnsValue() {
                var array = new String[]{"ab", "hello"};
                assertSame(array, StringSequences.length("field", array, 2, 5));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Matches
    // -------------------------------------------------------------------------

    @Nested
    class Matches {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.matches(null, List.of("a"), LOWERCASE));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.matches("   ", List.of("a"), LOWERCASE));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullPattern_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.matches("field", List.of("a"), null));
                assertEquals(NULL_PATTERN_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.matches("field", (java.util.List<String>) null, LOWERCASE));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.matches("field", List.of(), LOWERCASE));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.matches("field", Arrays.asList("abc", null), LOWERCASE));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementFailsPattern_throwsPatternMismatchException() {
                var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.matches("field", List.of("hello", "WORLD"), LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
            }

            @Test
            void allElementsMatchPattern_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.matches("field", list, LOWERCASE));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.matches(null, new String[]{"a"}, LOWERCASE));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.matches("   ", new String[]{"a"}, LOWERCASE));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullPattern_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.matches("field", new String[]{"a"}, null));
                assertEquals(NULL_PATTERN_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.matches("field", (String[]) null, LOWERCASE));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.matches("field", new String[0], LOWERCASE));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.matches("field", new String[]{"abc", null}, LOWERCASE));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void elementFailsPattern_throwsPatternMismatchException() {
                var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.matches("field", new String[]{"hello", "WORLD"}, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
            }

            @Test
            void allElementsMatchPattern_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.matches("field", array, LOWERCASE));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotBlankWithMatches
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMatches {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMatches(null, List.of("a"), LOWERCASE));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullPattern_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMatches("field", List.of("a"), null));
                assertEquals(NULL_PATTERN_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMatches("field", (java.util.List<String>) null, LOWERCASE));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMatches("field", List.of(), LOWERCASE));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMatches("field", Arrays.asList("hello", null), LOWERCASE));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMatches("field", Arrays.asList("hello", "   "), LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementFailsPattern_throwsPatternMismatchException() {
                var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.notBlankWithMatches("field", List.of("hello", "WORLD"), LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notBlankWithMatches("field", list, LOWERCASE));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMatches(null, new String[]{"a"}, LOWERCASE));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullPattern_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMatches("field", new String[]{"a"}, null));
                assertEquals(NULL_PATTERN_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMatches("field", (String[]) null, LOWERCASE));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMatches("field", new String[0], LOWERCASE));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMatches("field", new String[]{"hello", null}, LOWERCASE));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMatches("field", new String[]{"hello", "   "}, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementFailsPattern_throwsPatternMismatchException() {
                var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.notBlankWithMatches("field", new String[]{"hello", "WORLD"}, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notBlankWithMatches("field", array, LOWERCASE));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotBlankWithMinLength
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMinLength {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMinLength(null, List.of("abc"), 3));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithMinLength("field", List.of("abc"), 0));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMinLength("field", (java.util.List<String>) null, 3));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMinLength("field", List.of(), 3));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMinLength("field", Arrays.asList("hello", null), 3));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMinLength("field", Arrays.asList("hello", "   "), 3));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithMinLength("field", List.of("hello", "ab"), 3));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notBlankWithMinLength("field", list, 3));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMinLength(null, new String[]{"abc"}, 3));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithMinLength("field", new String[]{"abc"}, 0));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMinLength("field", (String[]) null, 3));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMinLength("field", new String[0], 3));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMinLength("field", new String[]{"hello", null}, 3));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMinLength("field", new String[]{"hello", "   "}, 3));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithMinLength("field", new String[]{"hello", "ab"}, 3));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notBlankWithMinLength("field", array, 3));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotBlankWithMaxLength
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMaxLength {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMaxLength(null, List.of("abc"), 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithMaxLength("field", List.of("abc"), 0));
                assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMaxLength("field", (java.util.List<String>) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMaxLength("field", List.of(), 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMaxLength("field", Arrays.asList("hello", null), 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMaxLength("field", Arrays.asList("hello", "   "), 5));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithMaxLength("field", List.of("abc", "too long"), 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notBlankWithMaxLength("field", list, 5));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMaxLength(null, new String[]{"abc"}, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithMaxLength("field", new String[]{"abc"}, 0));
                assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMaxLength("field", (String[]) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMaxLength("field", new String[0], 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMaxLength("field", new String[]{"hello", null}, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMaxLength("field", new String[]{"hello", "   "}, 5));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithMaxLength("field", new String[]{"abc", "too long"}, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notBlankWithMaxLength("field", array, 5));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotBlankWithLength
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithLength {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithLength(null, List.of("abc"), 2, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithLength("field", List.of("abc"), 0, 5));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxItemLengthLessThanMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithLength("field", List.of("abc"), 5, 3));
                assertEquals("The 'maxItemLength' value of '3' is invalid. The value must be greater than or equal to the 'minItemLength' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithLength("field", (java.util.List<String>) null, 2, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithLength("field", List.of(), 2, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithLength("field", Arrays.asList("hello", null), 2, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithLength("field", Arrays.asList("hello", "   "), 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithLength("field", List.of("hello", "a"), 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAboveMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithLength("field", List.of("hello", "too long"), 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notBlankWithLength("field", list, 2, 5));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithLength(null, new String[]{"abc"}, 2, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithLength("field", new String[]{"abc"}, 0, 5));
                assertEquals("The 'minItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxItemLengthLessThanMinItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithLength("field", new String[]{"abc"}, 5, 3));
                assertEquals("The 'maxItemLength' value of '3' is invalid. The value must be greater than or equal to the 'minItemLength' value of '5'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithLength("field", (String[]) null, 2, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithLength("field", new String[0], 2, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithLength("field", new String[]{"hello", null}, 2, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithLength("field", new String[]{"hello", "   "}, 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithLength("field", new String[]{"hello", "a"}, 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementAboveMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithLength("field", new String[]{"hello", "too long"}, 2, 5));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notBlankWithLength("field", array, 2, 5));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotBlankWithMaxLengthAndMatches
    // -------------------------------------------------------------------------

    @Nested
    class NotBlankWithMaxLengthAndMatches {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches(null, List.of("abc"), 5, LOWERCASE));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", List.of("abc"), 0, LOWERCASE));
                assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullPattern_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", List.of("abc"), 5, null));
                assertEquals(NULL_PATTERN_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", (java.util.List<String>) null, 5, LOWERCASE));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyCollection_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", List.of(), 5, LOWERCASE));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", Arrays.asList("abc", null), 5, LOWERCASE));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", Arrays.asList("hello", "   "), 5, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", List.of("abc", "too long"), 5, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementFailsPattern_throwsPatternMismatchException() {
                var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", List.of("hello", "WORLD"), 5, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("hello", "world");
                assertSame(list, StringSequences.notBlankWithMaxLengthAndMatches("field", list, 5, LOWERCASE));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches(null, new String[]{"abc"}, 5, LOWERCASE));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxItemLength_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[]{"abc"}, 0, LOWERCASE));
                assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullPattern_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[]{"abc"}, 5, null));
                assertEquals(NULL_PATTERN_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", (String[]) null, 5, LOWERCASE));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyArray_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[0], 5, LOWERCASE));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[]{"abc", null}, 5, LOWERCASE));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void blankElement_throwsBlankValueException() {
                var ex = assertThrows(BlankValueException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[]{"hello", "   "}, 5, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
            }

            @Test
            void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
                var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[]{"abc", "too long"}, 5, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
            }

            @Test
            void elementFailsPattern_throwsPatternMismatchException() {
                var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.notBlankWithMaxLengthAndMatches("field", new String[]{"hello", "WORLD"}, 5, LOWERCASE));
                assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"hello", "world"};
                assertSame(array, StringSequences.notBlankWithMaxLengthAndMatches("field", array, 5, LOWERCASE));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Optional variants — representative coverage
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotEmpty {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotEmpty("field", (java.util.List<String>) null));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotEmpty("field", (String[]) null));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotEmpty("field", List.of()));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotEmpty("field", new String[0]));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotEmpty("field", Arrays.asList("a", null)));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotEmpty("field", new String[]{"a", null}));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void emptyElement_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> StringSequences.optionalNotEmpty("field", Arrays.asList("a", "")));
            assertEquals("The 'field' value is invalid. The value must not contain empty elements. Element at index '1' is empty.", ex.getMessage());
        }

        @Test
        void emptyElement_array_throwsEmptyValueException() {
            var ex = assertThrows(EmptyValueException.class, () -> StringSequences.optionalNotEmpty("field", new String[]{"a", ""}));
            assertEquals("The 'field' value is invalid. The value must not contain empty elements. Element at index '1' is empty.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotEmpty("field", list));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotEmpty("field", array));
        }
    }

    @Nested
    class OptionalNotBlank {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotBlank("field", (java.util.List<String>) null));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotBlank("field", (String[]) null));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlank("field", List.of()));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlank("field", new String[0]));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlank("field", Arrays.asList("a", null)));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlank("field", new String[]{"a", null}));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void blankElement_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlank("field", Arrays.asList("a", "   ")));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void blankElement_array_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlank("field", new String[]{"a", "   "}));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotBlank("field", list));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotBlank("field", array));
        }
    }

    @Nested
    class OptionalMaxLength {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalMaxLength("field", (java.util.List<String>) null, 5));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalMaxLength("field", (String[]) null, 5));
        }

        @Test
        void zeroMaxItemLength_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> StringSequences.optionalMaxLength("field", (java.util.List<String>) null, 0));
            assertEquals("The 'maxItemLength' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalMaxLength("field", List.of(), 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalMaxLength("field", new String[0], 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalMaxLength("field", Arrays.asList("ab", null), 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalMaxLength("field", new String[]{"ab", null}, 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalMaxLength("field", List.of("abc", "too long"), 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementExceedsMaxLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalMaxLength("field", new String[]{"abc", "too long"}, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalMaxLength("field", list, 5));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalMaxLength("field", array, 5));
        }
    }

    @Nested
    class OptionalMinLength {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalMinLength("field", (java.util.List<String>) null, 3));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalMinLength("field", (String[]) null, 3));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalMinLength("field", List.of(), 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalMinLength("field", new String[0], 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalMinLength("field", Arrays.asList("hello", null), 3));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalMinLength("field", new String[]{"hello", null}, 3));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalMinLength("field", List.of("hello", "ab"), 3));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementBelowMinLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalMinLength("field", new String[]{"hello", "ab"}, 3));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalMinLength("field", list, 3));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalMinLength("field", array, 3));
        }
    }

    @Nested
    class OptionalLength {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalLength("field", (java.util.List<String>) null, 2, 5));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalLength("field", (String[]) null, 2, 5));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalLength("field", List.of(), 2, 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalLength("field", new String[0], 2, 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalLength("field", Arrays.asList("abc", null), 2, 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalLength("field", new String[]{"abc", null}, 2, 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalLength("field", List.of("abc", "a"), 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementBelowMinLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalLength("field", new String[]{"abc", "a"}, 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementAboveMaxLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalLength("field", List.of("abc", "too long"), 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementAboveMaxLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalLength("field", new String[]{"abc", "too long"}, 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalLength("field", list, 2, 5));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalLength("field", array, 2, 5));
        }
    }

    @Nested
    class OptionalMatches {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalMatches("field", (java.util.List<String>) null, LOWERCASE));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalMatches("field", (String[]) null, LOWERCASE));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalMatches("field", List.of(), LOWERCASE));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalMatches("field", new String[0], LOWERCASE));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalMatches("field", Arrays.asList("abc", null), LOWERCASE));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalMatches("field", new String[]{"abc", null}, LOWERCASE));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void elementFailsPattern_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.optionalMatches("field", List.of("hello", "WORLD"), LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
        }

        @Test
        void elementFailsPattern_array_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.optionalMatches("field", new String[]{"hello", "WORLD"}, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalMatches("field", list, LOWERCASE));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalMatches("field", array, LOWERCASE));
        }
    }

    @Nested
    class OptionalNotBlankWithMatches {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMatches("field", (java.util.List<String>) null, LOWERCASE));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMatches("field", (String[]) null, LOWERCASE));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMatches("field", List.of(), LOWERCASE));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMatches("field", new String[0], LOWERCASE));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMatches("field", Arrays.asList("hello", null), LOWERCASE));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMatches("field", new String[]{"hello", null}, LOWERCASE));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void blankElement_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMatches("field", Arrays.asList("hello", "   "), LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void blankElement_array_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMatches("field", new String[]{"hello", "   "}, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void elementFailsPattern_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.optionalNotBlankWithMatches("field", List.of("hello", "WORLD"), LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
        }

        @Test
        void elementFailsPattern_array_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.optionalNotBlankWithMatches("field", new String[]{"hello", "WORLD"}, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotBlankWithMatches("field", list, LOWERCASE));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotBlankWithMatches("field", array, LOWERCASE));
        }
    }

    @Nested
    class OptionalNotBlankWithMinLength {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMinLength("field", (java.util.List<String>) null, 3));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMinLength("field", (String[]) null, 3));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", List.of(), 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", new String[0], 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", Arrays.asList("hello", null), 3));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", new String[]{"hello", null}, 3));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void blankElement_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", Arrays.asList("hello", "   "), 3));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void blankElement_array_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", new String[]{"hello", "   "}, 3));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", List.of("hello", "ab"), 3));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementBelowMinLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithMinLength("field", new String[]{"hello", "ab"}, 3));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '3' Unicode code points but element at index '1' contained '2' Unicode code points.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotBlankWithMinLength("field", list, 3));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotBlankWithMinLength("field", array, 3));
        }
    }

    @Nested
    class OptionalNotBlankWithMaxLength {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMaxLength("field", (java.util.List<String>) null, 5));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMaxLength("field", (String[]) null, 5));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", List.of(), 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", new String[0], 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", Arrays.asList("hello", null), 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", new String[]{"hello", null}, 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void blankElement_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", Arrays.asList("hello", "   "), 5));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void blankElement_array_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", new String[]{"hello", "   "}, 5));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", List.of("abc", "too long"), 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementExceedsMaxLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithMaxLength("field", new String[]{"abc", "too long"}, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotBlankWithMaxLength("field", list, 5));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotBlankWithMaxLength("field", array, 5));
        }
    }

    @Nested
    class OptionalNotBlankWithLength {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithLength("field", (java.util.List<String>) null, 2, 5));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithLength("field", (String[]) null, 2, 5));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithLength("field", List.of(), 2, 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithLength("field", new String[0], 2, 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithLength("field", Arrays.asList("hello", null), 2, 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithLength("field", new String[]{"hello", null}, 2, 5));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void blankElement_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithLength("field", Arrays.asList("hello", "   "), 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void blankElement_array_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithLength("field", new String[]{"hello", "   "}, 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void elementBelowMinLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithLength("field", List.of("hello", "a"), 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementBelowMinLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithLength("field", new String[]{"hello", "a"}, 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements with fewer than '2' Unicode code points but element at index '1' contained '1' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementAboveMaxLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithLength("field", List.of("hello", "too long"), 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementAboveMaxLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithLength("field", new String[]{"hello", "too long"}, 2, 5));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotBlankWithLength("field", list, 2, 5));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotBlankWithLength("field", array, 2, 5));
        }
    }

    @Nested
    class OptionalNotBlankWithMaxLengthAndMatches {
        @Test
        void nullValue_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", (java.util.List<String>) null, 5, LOWERCASE));
        }

        @Test
        void nullValue_array_returnsNull() {
            assertNull(StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", (String[]) null, 5, LOWERCASE));
        }

        @Test
        void emptyCollection_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", List.of(), 5, LOWERCASE));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyArray_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", new String[0], 5, LOWERCASE));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullElement_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", Arrays.asList("hello", null), 5, LOWERCASE));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void nullElement_array_throwsNullElementException() {
            var ex = assertThrows(NullElementException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", new String[]{"hello", null}, 5, LOWERCASE));
            assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
        }

        @Test
        void blankElement_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", Arrays.asList("hello", "   "), 5, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void blankElement_array_throwsBlankValueException() {
            var ex = assertThrows(BlankValueException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", new String[]{"hello", "   "}, 5, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain blank elements. Element at index '1' is blank.", ex.getMessage());
        }

        @Test
        void elementExceedsMaxLength_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", List.of("abc", "too long"), 5, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementExceedsMaxLength_array_throwsStringLengthOutsideRangeException() {
            var ex = assertThrows(StringLengthOutsideRangeException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", new String[]{"abc", "too long"}, 5, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements exceeding '5' Unicode code points but element at index '1' contained '8' Unicode code points.", ex.getMessage());
        }

        @Test
        void elementFailsPattern_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", List.of("hello", "WORLD"), 5, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
        }

        @Test
        void elementFailsPattern_array_throwsPatternMismatchException() {
            var ex = assertThrows(PatternMismatchException.class, () -> StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", new String[]{"hello", "WORLD"}, 5, LOWERCASE));
            assertEquals("The 'field' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '1' does not match: 'WORLD'.", ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var list = List.of("hello");
            assertSame(list, StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", list, 5, LOWERCASE));
        }

        @Test
        void validValue_array_returnsValue() {
            var array = new String[]{"hello"};
            assertSame(array, StringSequences.optionalNotBlankWithMaxLengthAndMatches("field", array, 5, LOWERCASE));
        }
    }
}

