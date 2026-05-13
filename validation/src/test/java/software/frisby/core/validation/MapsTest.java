package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapsTest {
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";
    private static final String NULL_VALUE_MSG = "The 'field' value is invalid. The value must not be null.";
    private static final String EMPTY_VALUE_MSG = "The 'field' value is invalid. The value must not be empty.";
    private static final String NULL_KEY_MSG = "The 'field' value is invalid. The value must not contain null keys.";
    private static final String NULL_MAP_VALUE_MSG = "The 'field' value is invalid. The value must not contain null values.";

    // -------------------------------------------------------------------------
    // NotNull
    // -------------------------------------------------------------------------

    @Nested
    class NotNull {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.notNull(null, Map.of("k", "v")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.notNull("   ", Map.of("k", "v")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Maps.notNull("field", (Map<String, String>) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nonNullValue_returnsValue() {
            var map = Map.of("k", "v");
            assertSame(map, Maps.notNull("field", map));
        }
    }

    // -------------------------------------------------------------------------
    // NotEmpty
    // -------------------------------------------------------------------------

    @Nested
    class NotEmpty {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.notEmpty(null, Map.of("k", "v")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.notEmpty("   ", Map.of("k", "v")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Maps.notEmpty("field", (Map<String, String>) null));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.notEmpty("field", new HashMap<>()));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.notEmpty("field", map));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.notEmpty("field", map));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.notEmpty("field", map));
        }
    }

    // -------------------------------------------------------------------------
    // MinSize
    // -------------------------------------------------------------------------

    @Nested
    class MinSize {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.minSize(null, Map.of("k", "v"), 1));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.minSize("   ", Map.of("k", "v"), 1));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void zeroMinSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.minSize("field", Map.of("k", "v"), 0));
            assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Maps.minSize("field", (Map<String, String>) null, 1));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.minSize("field", new HashMap<>(), 1));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.minSize("field", map, 1));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.minSize("field", map, 1));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.minSize("field", Map.of("k", "v"), 2));
            assertEquals("The 'field' value is invalid. The value must contain at least '2' entries but contained '1'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.minSize("field", map, 2));
        }

        @Test
        void valueAboveMin_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            assertSame(map, Maps.minSize("field", map, 2));
        }
    }

    // -------------------------------------------------------------------------
    // MaxSize
    // -------------------------------------------------------------------------

    @Nested
    class MaxSize {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.maxSize(null, Map.of("k", "v"), 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.maxSize("   ", Map.of("k", "v"), 5));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void zeroMaxSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.maxSize("field", Map.of("k", "v"), 0));
            assertEquals("The 'maxSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Maps.maxSize("field", (Map<String, String>) null, 5));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.maxSize("field", new HashMap<>(), 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.maxSize("field", map, 5));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.maxSize("field", map, 5));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.maxSize("field", Map.of("k1", "v1", "k2", "v2", "k3", "v3"), 2));
            assertEquals("The 'field' value is invalid. The value must not contain more than '2' entries but contained '3'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.maxSize("field", map, 2));
        }

        @Test
        void valueBelowMax_returnsValue() {
            var map = Map.of("k1", "v1");
            assertSame(map, Maps.maxSize("field", map, 2));
        }
    }

    // -------------------------------------------------------------------------
    // Size
    // -------------------------------------------------------------------------

    @Nested
    class Size {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.size(null, Map.of("k", "v"), 1, 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.size("   ", Map.of("k", "v"), 1, 3));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void zeroMinSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.size("field", Map.of("k", "v"), 0, 3));
            assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void maxSizeLessThanMinSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.size("field", Map.of("k", "v"), 3, 2));
            assertEquals("The 'maxSize' value of '2' is invalid. The value must be greater than or equal to the 'minSize' value of '3'.", ex.getMessage());
        }

        @Test
        void nullValue_throwsNullValueException() {
            var ex = assertThrows(NullValueException.class, () -> Maps.size("field", (Map<String, String>) null, 1, 3));
            assertEquals(NULL_VALUE_MSG, ex.getMessage());
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.size("field", new HashMap<>(), 1, 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.size("field", map, 1, 3));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.size("field", map, 1, 3));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.size("field", Map.of("k", "v"), 2, 4));
            assertEquals("The 'field' value is invalid. The value must contain at least '2' entries but contained '1'.", ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.size("field", Map.of("k1", "v1", "k2", "v2", "k3", "v3"), 1, 2));
            assertEquals("The 'field' value is invalid. The value must not contain more than '2' entries but contained '3'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            var map = Map.of("k1", "v1");
            assertSame(map, Maps.size("field", map, 1, 3));
        }

        @Test
        void valueAtMax_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            assertSame(map, Maps.size("field", map, 1, 3));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.size("field", map, 1, 3));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalNotEmpty
    // -------------------------------------------------------------------------

    @Nested
    class OptionalNotEmpty {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalNotEmpty(null, Map.of("k", "v")));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalNotEmpty("   ", Map.of("k", "v")));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Maps.optionalNotEmpty("field", (Map<String, String>) null));
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.optionalNotEmpty("field", new HashMap<>()));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.optionalNotEmpty("field", map));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.optionalNotEmpty("field", map));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void validValue_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.optionalNotEmpty("field", map));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMinSize
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMinSize {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalMinSize(null, Map.of("k", "v"), 1));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalMinSize("   ", Map.of("k", "v"), 1));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void zeroMinSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.optionalMinSize("field", Map.of("k", "v"), 0));
            assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Maps.optionalMinSize("field", (Map<String, String>) null, 2));
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.optionalMinSize("field", new HashMap<>(), 1));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.optionalMinSize("field", map, 1));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.optionalMinSize("field", map, 1));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.optionalMinSize("field", Map.of("k", "v"), 2));
            assertEquals("The 'field' value is invalid. The value must contain at least '2' entries but contained '1'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.optionalMinSize("field", map, 2));
        }

        @Test
        void valueAboveMin_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            assertSame(map, Maps.optionalMinSize("field", map, 2));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalMaxSize
    // -------------------------------------------------------------------------

    @Nested
    class OptionalMaxSize {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalMaxSize(null, Map.of("k", "v"), 5));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalMaxSize("   ", Map.of("k", "v"), 5));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void zeroMaxSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.optionalMaxSize("field", Map.of("k", "v"), 0));
            assertEquals("The 'maxSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Maps.optionalMaxSize("field", (Map<String, String>) null, 5));
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.optionalMaxSize("field", new HashMap<>(), 5));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.optionalMaxSize("field", map, 5));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.optionalMaxSize("field", map, 5));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.optionalMaxSize("field", Map.of("k1", "v1", "k2", "v2", "k3", "v3"), 2));
            assertEquals("The 'field' value is invalid. The value must not contain more than '2' entries but contained '3'.", ex.getMessage());
        }

        @Test
        void valueAtMax_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.optionalMaxSize("field", map, 2));
        }

        @Test
        void valueBelowMax_returnsValue() {
            var map = Map.of("k1", "v1");
            assertSame(map, Maps.optionalMaxSize("field", map, 2));
        }
    }

    // -------------------------------------------------------------------------
    // OptionalSize
    // -------------------------------------------------------------------------

    @Nested
    class OptionalSize {
        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalSize(null, Map.of("k", "v"), 1, 3));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> Maps.optionalSize("   ", Map.of("k", "v"), 1, 3));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void zeroMinSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.optionalSize("field", Map.of("k", "v"), 0, 3));
            assertEquals("The 'minSize' value of '0' is invalid. The value must be greater than or equal to '1'.", ex.getMessage());
        }

        @Test
        void maxSizeLessThanMinSize_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> Maps.optionalSize("field", Map.of("k", "v"), 3, 2));
            assertEquals("The 'maxSize' value of '2' is invalid. The value must be greater than or equal to the 'minSize' value of '3'.", ex.getMessage());
        }

        @Test
        void nullValue_returnsNull() {
            assertNull(Maps.optionalSize("field", (Map<String, String>) null, 1, 3));
        }

        @Test
        void emptyValue_throwsMissingElementsException() {
            var ex = assertThrows(MissingElementsException.class, () -> Maps.optionalSize("field", new HashMap<>(), 1, 3));
            assertEquals(EMPTY_VALUE_MSG, ex.getMessage());
        }

        @Test
        void nullKey_throwsNullMapKeyException() {
            var map = new HashMap<String, String>();
            map.put(null, "value");
            var ex = assertThrows(NullMapKeyException.class, () -> Maps.optionalSize("field", map, 1, 3));
            assertEquals(NULL_KEY_MSG, ex.getMessage());
        }

        @Test
        void nullMapValue_throwsNullMapValueException() {
            var map = new HashMap<String, String>();
            map.put("key", null);
            var ex = assertThrows(NullMapValueException.class, () -> Maps.optionalSize("field", map, 1, 3));
            assertEquals(NULL_MAP_VALUE_MSG, ex.getMessage());
        }

        @Test
        void valueBelowMin_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.optionalSize("field", Map.of("k", "v"), 2, 4));
            assertEquals("The 'field' value is invalid. The value must contain at least '2' entries but contained '1'.", ex.getMessage());
        }

        @Test
        void valueAboveMax_throwsMapSizeOutsideRangeException() {
            var ex = assertThrows(MapSizeOutsideRangeException.class, () -> Maps.optionalSize("field", Map.of("k1", "v1", "k2", "v2", "k3", "v3"), 1, 2));
            assertEquals("The 'field' value is invalid. The value must not contain more than '2' entries but contained '3'.", ex.getMessage());
        }

        @Test
        void valueAtMin_returnsValue() {
            var map = Map.of("k1", "v1");
            assertSame(map, Maps.optionalSize("field", map, 1, 3));
        }

        @Test
        void valueAtMax_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2", "k3", "v3");
            assertSame(map, Maps.optionalSize("field", map, 1, 3));
        }

        @Test
        void valueBetweenMinAndMax_returnsValue() {
            var map = Map.of("k1", "v1", "k2", "v2");
            assertSame(map, Maps.optionalSize("field", map, 1, 3));
        }
    }
}

