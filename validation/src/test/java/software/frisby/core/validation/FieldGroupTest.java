package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldGroupTest {
    // -------------------------------------------------------------------------
    // Shared constants
    // -------------------------------------------------------------------------

    private static final String NULL_NAMES_MSG = "The 'names' value was not provided.";
    private static final String NULL_NAME_MSG = "The 'name' value was not provided.";
    private static final String BLANK_NAME_MSG = "The 'name' value is invalid. The value must be non null and cannot contain only white space characters.";

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    @Nested
    class Factory {
        @Test
        void nullNames_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroup.of((String[]) null));
            assertEquals(NULL_NAMES_MSG, ex.getMessage());
        }

        @Test
        void zeroNames_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, FieldGroup::of);
            assertEquals(
                    "The 'names' value of '0' is invalid. The value must be greater than or equal to '2'.",
                    ex.getMessage()
            );
        }

        @Test
        void oneNameFewerThanTwoNames_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> FieldGroup.of("a"));
            assertEquals(
                    "The 'names' value of '1' is invalid. The value must be greater than or equal to '2'.",
                    ex.getMessage()
            );
        }

        @Test
        void nullName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroup.of("a", null));
            assertEquals(NULL_NAME_MSG, ex.getMessage());
        }

        @Test
        void blankName_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroup.of("a", "   "));
            assertEquals(BLANK_NAME_MSG, ex.getMessage());
        }

        @Test
        void duplicateName_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> FieldGroup.of("a", "b", "a"));
            assertEquals(
                    "The 'names' value is invalid. The value must not contain duplicate names; 'a' appears more than once.",
                    ex.getMessage()
            );
        }

        @Test
        void validNames_returnsFieldGroup() {
            var group = FieldGroup.of("alpha", "beta", "gamma");

            assertEquals(3, group.size());
            assertEquals("alpha", group.nameAt(0));
            assertEquals("beta", group.nameAt(1));
            assertEquals("gamma", group.nameAt(2));
        }
    }

    // -------------------------------------------------------------------------
    // Equality
    // -------------------------------------------------------------------------

    @Nested
    class Equality {
        @Test
        void sameInstance_isEqualToItself() {
            var group = FieldGroup.of("a", "b");
            //noinspection EqualsWithItself
            assertEquals(group, group);
        }

        @Test
        void equalGroups_areEqual() {
            assertEquals(FieldGroup.of("a", "b", "c"), FieldGroup.of("a", "b", "c"));
        }

        @Test
        void differentOrder_notEqual() {
            assertNotEquals(FieldGroup.of("a", "b"), FieldGroup.of("b", "a"));
        }

        @Test
        void differentNames_notEqual() {
            assertNotEquals(FieldGroup.of("a", "b"), FieldGroup.of("a", "c"));
        }

        @Test
        void differentSize_notEqual() {
            assertNotEquals(FieldGroup.of("a", "b"), FieldGroup.of("a", "b", "c"));
        }

        @Test
        void null_notEqual() {
            assertNotEquals(null, FieldGroup.of("a", "b"));
        }

        @Test
        void differentType_notEqual() {
            //noinspection AssertBetweenInconvertibleTypes
            assertNotEquals("a, b", FieldGroup.of("a", "b"));
        }
    }

    // -------------------------------------------------------------------------
    // HashCode
    // -------------------------------------------------------------------------

    @Nested
    class HashCode {
        @Test
        void equalGroups_haveSameHashCode() {
            assertEquals(
                    FieldGroup.of("a", "b", "c").hashCode(),
                    FieldGroup.of("a", "b", "c").hashCode()
            );
        }

        @Test
        void differentOrder_haveDifferentHashCode() {
            assertNotEquals(
                    FieldGroup.of("a", "b").hashCode(),
                    FieldGroup.of("b", "a").hashCode()
            );
        }
    }

    // -------------------------------------------------------------------------
    // ToString
    // -------------------------------------------------------------------------

    @Nested
    class ToStringMethod {
        @Test
        void twoNames_formatsCorrectly() {
            assertEquals("FieldGroup['f1', 'f2']", FieldGroup.of("f1", "f2").toString());
        }

        @Test
        void threeNames_formatsCorrectly() {
            assertEquals(
                    "FieldGroup['start', 'end', 'limit']",
                    FieldGroup.of("start", "end", "limit").toString()
            );
        }

        @Test
        void containsAllNames() {
            var group = FieldGroup.of("match", "and", "or");
            var str = group.toString();

            assertTrue(str.contains("match"));
            assertTrue(str.contains("and"));
            assertTrue(str.contains("or"));
        }
    }
}

