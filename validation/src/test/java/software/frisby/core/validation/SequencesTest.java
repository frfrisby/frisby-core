package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SequencesTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String EMPTY_VALUE_MSG = "The 'field' value is invalid. The value must not be empty.";
    private static final String NULL_ELEMENT_MSG = "The 'field' value is invalid. The value must not contain null elements.";
    private static final String NULL_EXTRACTOR_MSG = "The 'keyExtractor' value was not provided.";

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notNull(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notNull("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.notNull("field", (java.util.List<String>) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nonNullValue_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.notNull("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notNull(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notNull("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.notNull("field", (String[]) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nonNullValue_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.notNull("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NotEmpty
    // -------------------------------------------------------------------------

    @Nested
    class NotEmpty {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notEmpty(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notEmpty("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.notEmpty("field", (java.util.List<String>) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.notEmpty("field", List.of()));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.notEmpty("field", Arrays.asList("a", null, "b")));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("a", "b", "c");
                assertSame(list, Sequences.notEmpty("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notEmpty(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.notEmpty("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.notEmpty("field", (String[]) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.notEmpty("field", new String[0]));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.notEmpty("field", new String[]{"a", null, "b"}));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"a", "b", "c"};
                assertSame(array, Sequences.notEmpty("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // MinSize
    // -------------------------------------------------------------------------

    @Nested
    class MinSize {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.minSize(null, List.of("a"), 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.minSize("   ", List.of("a"), 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.minSize("field", List.of("a"), 0));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.minSize("field", (java.util.List<String>) null, 1));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.minSize("field", List.of(), 1));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.minSize("field", Arrays.asList("a", null), 1));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.minSize("field", List.of("a"), 2));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.minSize("field", list, 2));
            }

            @Test
            void valueAboveMin_returnsValue() {
                var list = List.of("a", "b", "c");
                assertSame(list, Sequences.minSize("field", list, 2));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.minSize(null, new String[]{"a"}, 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.minSize("   ", new String[]{"a"}, 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.minSize("field", new String[]{"a"}, 0));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.minSize("field", (String[]) null, 1));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.minSize("field", new String[0], 1));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.minSize("field", new String[]{"a", null}, 1));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.minSize("field", new String[]{"a"}, 2));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.minSize("field", array, 2));
            }

            @Test
            void valueAboveMin_returnsValue() {
                var array = new String[]{"a", "b", "c"};
                assertSame(array, Sequences.minSize("field", array, 2));
            }
        }
    }

    // -------------------------------------------------------------------------
    // MaxSize
    // -------------------------------------------------------------------------

    @Nested
    class MaxSize {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.maxSize(null, List.of("a"), 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.maxSize("   ", List.of("a"), 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.maxSize("field", List.of("a"), 0));
                assertEquals("The 'maxSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.maxSize("field", (java.util.List<String>) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.maxSize("field", List.of(), 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.maxSize("field", Arrays.asList("a", null), 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.maxSize("field", List.of("a", "b", "c"), 2));
                assertEquals("The 'field' value is invalid. The value must not contain more than '2' elements but contained '3'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.maxSize("field", list, 2));
            }

            @Test
            void valueBelowMax_returnsValue() {
                var list = List.of("a");
                assertSame(list, Sequences.maxSize("field", list, 2));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.maxSize(null, new String[]{"a"}, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.maxSize("   ", new String[]{"a"}, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.maxSize("field", new String[]{"a"}, 0));
                assertEquals("The 'maxSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.maxSize("field", (String[]) null, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.maxSize("field", new String[0], 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.maxSize("field", new String[]{"a", null}, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.maxSize("field", new String[]{"a", "b", "c"}, 2));
                assertEquals("The 'field' value is invalid. The value must not contain more than '2' elements but contained '3'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.maxSize("field", array, 2));
            }

            @Test
            void valueBelowMax_returnsValue() {
                var array = new String[]{"a"};
                assertSame(array, Sequences.maxSize("field", array, 2));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Size
    // -------------------------------------------------------------------------

    @Nested
    class Size {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.size(null, List.of("a", "b"), 1, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.size("   ", List.of("a", "b"), 1, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.size("field", List.of("a"), 0, 5));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxSizeLessThanMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.size("field", List.of("a", "b"), 3, 2));
                assertEquals("The 'maxSize' value of '2' is invalid. The value must be greater than or equal to the 'minSize' value of '3'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.size("field", (java.util.List<String>) null, 1, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.size("field", List.of(), 1, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.size("field", Arrays.asList("a", null), 1, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.size("field", List.of("a"), 2, 4));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.size("field", list, 2, 4));
            }

            @Test
            void valueWithinRange_returnsValue() {
                var list = List.of("a", "b", "c");
                assertSame(list, Sequences.size("field", list, 2, 4));
            }

            @Test
            void valueAtMax_returnsValue() {
                var list = List.of("a", "b", "c", "d");
                assertSame(list, Sequences.size("field", list, 2, 4));
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.size("field", List.of("a", "b", "c", "d", "e"), 2, 4));
                assertEquals("The 'field' value is invalid. The value must not contain more than '4' elements but contained '5'.", ex.getMessage());
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.size(null, new String[]{"a", "b"}, 1, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.size("   ", new String[]{"a", "b"}, 1, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.size("field", new String[]{"a"}, 0, 5));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxSizeLessThanMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.size("field", new String[]{"a", "b"}, 3, 2));
                assertEquals("The 'maxSize' value of '2' is invalid. The value must be greater than or equal to the 'minSize' value of '3'.", ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.size("field", (String[]) null, 1, 5));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.size("field", new String[0], 1, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.size("field", new String[]{"a", null}, 1, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.size("field", new String[]{"a"}, 2, 4));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.size("field", array, 2, 4));
            }

            @Test
            void valueWithinRange_returnsValue() {
                var array = new String[]{"a", "b", "c"};
                assertSame(array, Sequences.size("field", array, 2, 4));
            }

            @Test
            void valueAtMax_returnsValue() {
                var array = new String[]{"a", "b", "c", "d"};
                assertSame(array, Sequences.size("field", array, 2, 4));
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.size("field", new String[]{"a", "b", "c", "d", "e"}, 2, 4));
                assertEquals("The 'field' value is invalid. The value must not contain more than '4' elements but contained '5'.", ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // NoDuplicates
    // -------------------------------------------------------------------------

    @Nested
    class NoDuplicates {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.noDuplicates("field", (java.util.List<String>) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.noDuplicates("field", List.of()));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.noDuplicates("field", Arrays.asList("a", null, "b")));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicates_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.noDuplicates("field", List.of("a", "b", "a")));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: 'a'.", ex.getMessage());
            }

            @Test
            void valueWithNoDuplicates_returnsValue() {
                var list = List.of("a", "b", "c");
                assertSame(list, Sequences.noDuplicates("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.noDuplicates("field", (String[]) null));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.noDuplicates("field", new String[0]));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.noDuplicates("field", new String[]{"a", null, "b"}));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicates_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.noDuplicates("field", new String[]{"a", "b", "a"}));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: 'a'.", ex.getMessage());
            }

            @Test
            void valueWithNoDuplicates_returnsValue() {
                var array = new String[]{"a", "b", "c"};
                assertSame(array, Sequences.noDuplicates("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NoDuplicates — with keyExtractor
    // -------------------------------------------------------------------------

    @Nested
    class NoDuplicatesWithKeyExtractor {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates(null, List.of("a"), String::length));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates("   ", List.of("a"), String::length));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullKeyExtractor_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates("field", List.of("a"), null));
                assertEquals(NULL_EXTRACTOR_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.noDuplicates("field", (java.util.List<String>) null, String::length));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.noDuplicates("field", List.of(), String::length));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.noDuplicates("field", Arrays.asList("a", null, "b"), String::length));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicateKeys_throwsDuplicateElementsException() {
                // "ab" and "cd" both have length 2 — duplicate key
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.noDuplicates("field", List.of("ab", "cd"), String::length));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: '2'.", ex.getMessage());
            }

            @Test
            void valueWithUniqueKeys_returnsValue() {
                var list = List.of("a", "bb", "ccc");
                assertSame(list, Sequences.noDuplicates("field", list, String::length));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates(null, new String[]{"a"}, String::length));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates("   ", new String[]{"a"}, String::length));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullKeyExtractor_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.noDuplicates("field", new String[]{"a"}, null));
                assertEquals(NULL_EXTRACTOR_MSG, ex.getMessage());
            }

            @Test
            void nullValue_throwsNullValueException() {
                var ex = assertThrows(NullValueException.class, () -> Sequences.noDuplicates("field", (String[]) null, String::length));
                assertEquals(NULL_VALUE_MSG, ex.getMessage());
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.noDuplicates("field", new String[0], String::length));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.noDuplicates("field", new String[]{"a", null, "b"}, String::length));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicateKeys_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.noDuplicates("field", new String[]{"ab", "cd"}, String::length));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: '2'.", ex.getMessage());
            }

            @Test
            void valueWithUniqueKeys_returnsValue() {
                var array = new String[]{"a", "bb", "ccc"};
                assertSame(array, Sequences.noDuplicates("field", array, String::length));
            }
        }
    }

    // -------------------------------------------------------------------------
    // OptionalNotEmpty
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotEmpty {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNotEmpty(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNotEmpty("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalNotEmpty("field", (java.util.List<String>) null));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalNotEmpty("field", List.of()));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalNotEmpty("field", Arrays.asList("a", null)));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.optionalNotEmpty("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNotEmpty(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNotEmpty("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalNotEmpty("field", (String[]) null));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalNotEmpty("field", new String[0]));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalNotEmpty("field", new String[]{"a", null}));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void validValue_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.optionalNotEmpty("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinSize
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinSize {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMinSize(null, List.of("a"), 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMinSize("   ", List.of("a"), 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalMinSize("field", List.of("a"), 0));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalMinSize("field", (java.util.List<String>) null, 2));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalMinSize("field", List.of(), 2));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalMinSize("field", Arrays.asList("a", null), 1));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalMinSize("field", List.of("a"), 2));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.optionalMinSize("field", list, 2));
            }

            @Test
            void valueAboveMin_returnsValue() {
                var list = List.of("a", "b", "c");
                assertSame(list, Sequences.optionalMinSize("field", list, 2));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMinSize(null, new String[]{"a"}, 1));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMinSize("   ", new String[]{"a"}, 1));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalMinSize("field", new String[]{"a"}, 0));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalMinSize("field", (String[]) null, 2));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalMinSize("field", new String[0], 2));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalMinSize("field", new String[]{"a", null}, 1));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalMinSize("field", new String[]{"a"}, 2));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.optionalMinSize("field", array, 2));
            }

            @Test
            void valueAboveMin_returnsValue() {
                var array = new String[]{"a", "b", "c"};
                assertSame(array, Sequences.optionalMinSize("field", array, 2));
            }
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxSize
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxSize {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMaxSize(null, List.of("a"), 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMaxSize("   ", List.of("a"), 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalMaxSize("field", List.of("a"), 0));
                assertEquals("The 'maxSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalMaxSize("field", (java.util.List<String>) null, 5));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalMaxSize("field", List.of(), 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalMaxSize("field", Arrays.asList("a", null), 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalMaxSize("field", List.of("a", "b", "c"), 2));
                assertEquals("The 'field' value is invalid. The value must not contain more than '2' elements but contained '3'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.optionalMaxSize("field", list, 2));
            }

            @Test
            void valueBelowMax_returnsValue() {
                var list = List.of("a");
                assertSame(list, Sequences.optionalMaxSize("field", list, 2));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMaxSize(null, new String[]{"a"}, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalMaxSize("   ", new String[]{"a"}, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMaxSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalMaxSize("field", new String[]{"a"}, 0));
                assertEquals("The 'maxSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalMaxSize("field", (String[]) null, 5));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalMaxSize("field", new String[0], 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalMaxSize("field", new String[]{"a", null}, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalMaxSize("field", new String[]{"a", "b", "c"}, 2));
                assertEquals("The 'field' value is invalid. The value must not contain more than '2' elements but contained '3'.", ex.getMessage());
            }

            @Test
            void valueAtMax_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.optionalMaxSize("field", array, 2));
            }

            @Test
            void valueBelowMax_returnsValue() {
                var array = new String[]{"a"};
                assertSame(array, Sequences.optionalMaxSize("field", array, 2));
            }
        }
    }

    // -------------------------------------------------------------------------
    // OptionalSize
    // -------------------------------------------------------------------------

    @Nested
    class OptionalSize {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalSize(null, List.of("a", "b"), 1, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalSize("   ", List.of("a", "b"), 1, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalSize("field", List.of("a"), 0, 5));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxSizeLessThanMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalSize("field", List.of("a"), 3, 2));
                assertEquals("The 'maxSize' value of '2' is invalid. The value must be greater than or equal to the 'minSize' value of '3'.", ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalSize("field", (java.util.List<String>) null, 1, 5));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalSize("field", List.of(), 1, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalSize("field", Arrays.asList("a", null), 1, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalSize("field", List.of("a"), 2, 4));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var list = List.of("a", "b");
                assertSame(list, Sequences.optionalSize("field", list, 2, 4));
            }

            @Test
            void valueAtMax_returnsValue() {
                var list = List.of("a", "b", "c", "d");
                assertSame(list, Sequences.optionalSize("field", list, 2, 4));
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalSize("field", List.of("a", "b", "c", "d", "e"), 2, 4));
                assertEquals("The 'field' value is invalid. The value must not contain more than '4' elements but contained '5'.", ex.getMessage());
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalSize(null, new String[]{"a", "b"}, 1, 5));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalSize("   ", new String[]{"a", "b"}, 1, 5));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void zeroMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalSize("field", new String[]{"a"}, 0, 5));
                assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
            }

            @Test
            void maxSizeLessThanMinSize_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () -> Sequences.optionalSize("field", new String[]{"a"}, 3, 2));
                assertEquals("The 'maxSize' value of '2' is invalid. The value must be greater than or equal to the 'minSize' value of '3'.", ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalSize("field", (String[]) null, 1, 5));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalSize("field", new String[0], 1, 5));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalSize("field", new String[]{"a", null}, 1, 5));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueBelowMin_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalSize("field", new String[]{"a"}, 2, 4));
                assertEquals("The 'field' value is invalid. The value must contain at least '2' elements but contained '1'.", ex.getMessage());
            }

            @Test
            void valueAtMin_returnsValue() {
                var array = new String[]{"a", "b"};
                assertSame(array, Sequences.optionalSize("field", array, 2, 4));
            }

            @Test
            void valueAtMax_returnsValue() {
                var array = new String[]{"a", "b", "c", "d"};
                assertSame(array, Sequences.optionalSize("field", array, 2, 4));
            }

            @Test
            void valueAboveMax_throwsSequenceSizeOutsideRangeException() {
                var ex = assertThrows(SequenceSizeOutsideRangeException.class, () -> Sequences.optionalSize("field", new String[]{"a", "b", "c", "d", "e"}, 2, 4));
                assertEquals("The 'field' value is invalid. The value must not contain more than '4' elements but contained '5'.", ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // OptionalNoDuplicates
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNoDuplicates {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates(null, List.of("a")));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates("   ", List.of("a")));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalNoDuplicates("field", (java.util.List<String>) null));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalNoDuplicates("field", List.of()));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalNoDuplicates("field", Arrays.asList("a", null, "b")));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicates_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.optionalNoDuplicates("field", List.of("a", "b", "a")));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: 'a'.", ex.getMessage());
            }

            @Test
            void valueWithNoDuplicates_returnsValue() {
                var list = List.of("a", "b", "c");
                assertSame(list, Sequences.optionalNoDuplicates("field", list));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates(null, new String[]{"a"}));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates("   ", new String[]{"a"}));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalNoDuplicates("field", (String[]) null));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalNoDuplicates("field", new String[0]));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalNoDuplicates("field", new String[]{"a", null, "b"}));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicates_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.optionalNoDuplicates("field", new String[]{"a", "b", "a"}));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: 'a'.", ex.getMessage());
            }

            @Test
            void valueWithNoDuplicates_returnsValue() {
                var array = new String[]{"a", "b", "c"};
                assertSame(array, Sequences.optionalNoDuplicates("field", array));
            }
        }
    }

    // -------------------------------------------------------------------------
    // OptionalNoDuplicates — with keyExtractor
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNoDuplicatesWithKeyExtractor {
        @Nested
        class Collection {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates(null, List.of("a"), String::length));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates("   ", List.of("a"), String::length));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullKeyExtractor_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates("field", List.of("a"), null));
                assertEquals(NULL_EXTRACTOR_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalNoDuplicates("field", (java.util.List<String>) null, String::length));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalNoDuplicates("field", List.of(), String::length));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalNoDuplicates("field", Arrays.asList("a", null), String::length));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicateKeys_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.optionalNoDuplicates("field", List.of("ab", "cd"), String::length));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: '2'.", ex.getMessage());
            }

            @Test
            void valueWithUniqueKeys_returnsValue() {
                var list = List.of("a", "bb", "ccc");
                assertSame(list, Sequences.optionalNoDuplicates("field", list, String::length));
            }
        }

        @Nested
        class Array {
            @Test
            void nullName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates(null, new String[]{"a"}, String::length));
                assertEquals(NULL_NAME_MSG, ex.getMessage());
            }

            @Test
            void blankName_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates("   ", new String[]{"a"}, String::length));
                assertEquals(BLANK_NAME_MSG, ex.getMessage());
            }

            @Test
            void nullKeyExtractor_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> Sequences.optionalNoDuplicates("field", new String[]{"a"}, null));
                assertEquals(NULL_EXTRACTOR_MSG, ex.getMessage());
            }

            @Test
            void nullValue_returnsNull() {
                assertNull(Sequences.optionalNoDuplicates("field", (String[]) null, String::length));
            }

            @Test
            void emptyValue_throwsMissingElementsException() {
                var ex = assertThrows(MissingElementsException.class, () -> Sequences.optionalNoDuplicates("field", new String[0], String::length));
                assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
            }

            @Test
            void nullElement_throwsNullElementException() {
                var ex = assertThrows(NullElementException.class, () -> Sequences.optionalNoDuplicates("field", new String[]{"a", null}, String::length));
                assertEquals(NULL_ELEMENT_MSG, ex.getMessage());
            }

            @Test
            void valueWithDuplicateKeys_throwsDuplicateElementsException() {
                var ex = assertThrows(DuplicateElementsException.class, () -> Sequences.optionalNoDuplicates("field", new String[]{"ab", "cd"}, String::length));
                assertEquals("The 'field' value is invalid. The value must not contain duplicate elements. Found duplicate: '2'.", ex.getMessage());
            }

            @Test
            void valueWithUniqueKeys_returnsValue() {
                var array = new String[]{"a", "bb", "ccc"};
                assertSame(array, Sequences.optionalNoDuplicates("field", array, String::length));
            }
        }
    }
}

