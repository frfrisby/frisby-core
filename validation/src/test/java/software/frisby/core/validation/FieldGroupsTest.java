package software.frisby.core.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FieldGroupsTest {
    // -------------------------------------------------------------------------
    // Shared constants
    // -------------------------------------------------------------------------

    private static final String NULL_GROUP_MSG = "The 'group' value was not provided.";

    private static final String ARITY_3_VS_2_MSG = "The 'FieldGroup' defines '3' fields but '2' values were provided.";

    // Messages for 2-field groups (f1, f2)
    private static final String MISSING_AT_LEAST_ONE_2_MSG = "At least one of the 'f1' or 'f2' fields must be provided.";
    private static final String ONLY_ONE_TOO_MANY_2_MSG = "Only one of the 'f1' or 'f2' fields may be provided.";
    private static final String AT_MOST_ONE_TOO_MANY_2_MSG = "At most one of the 'f1' or 'f2' fields may be provided.";
    private static final String NONE_OR_ALL_2_MSG = "The 'f1' and 'f2' fields must all be provided together, or none at all.";

    // Messages for 3-field groups (f1, f2, f3)
    private static final String MISSING_AT_LEAST_ONE_3_MSG = "At least one of the 'f1', 'f2', or 'f3' fields must be provided.";
    private static final String ONLY_ONE_TOO_MANY_3_MSG = "Only one of the 'f1' or 'f2' fields may be provided.";
    private static final String AT_MOST_ONE_TOO_MANY_3_MSG = "At most one of the 'f1' or 'f2' fields may be provided.";
    private static final String NONE_OR_ALL_3_MSG = "The 'f1', 'f2', and 'f3' fields must all be provided together, or none at all.";

    // Pre-built groups used across all tests
    private static final FieldGroup GROUP_2 = FieldGroup.of("f1", "f2");
    private static final FieldGroup GROUP_3 = FieldGroup.of("f1", "f2", "f3");
    private static final FieldGroup GROUP_4 = FieldGroup.of("f1", "f2", "f3", "f4");
    private static final FieldGroup GROUP_5 = FieldGroup.of("f1", "f2", "f3", "f4", "f5");
    private static final FieldGroup GROUP_6 = FieldGroup.of("f1", "f2", "f3", "f4", "f5", "f6");
    private static final FieldGroup GROUP_7 = FieldGroup.of("f1", "f2", "f3", "f4", "f5", "f6", "f7");
    private static final FieldGroup GROUP_8 = FieldGroup.of("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8");
    private static final FieldGroup GROUP_9 = FieldGroup.of("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9");
    private static final FieldGroup GROUP_10 = FieldGroup.of("f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10");
    private static final FieldGroup GROUP_11 = FieldGroup.of(
            "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "f10", "f11"
    );

    // Sentinel non-null, non-String value to test the "any other reference type → provided" branch
    private static final Object AN_OBJECT = new Object();

    // -------------------------------------------------------------------------
    // AtLeastOne
    // -------------------------------------------------------------------------

    @Nested
    class AtLeastOne {
        static Stream<Executable> atLeastOne_singleFieldProvided() {
            return Stream.concat(
                    Stream.concat(
                            Stream.of(
                                    // arity 2
                                    () -> FieldGroups.atLeastOne(GROUP_2, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_2, null, "yes"),
                                    // arity 3
                                    () -> FieldGroups.atLeastOne(GROUP_3, "yes", null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_3, null, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_3, null, null, "yes"),
                                    // arity 4
                                    () -> FieldGroups.atLeastOne(GROUP_4, "yes", null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_4, null, "yes", null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_4, null, null, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_4, null, null, null, "yes"),
                                    // arity 5
                                    () -> FieldGroups.atLeastOne(GROUP_5, "yes", null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_5, null, "yes", null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_5, null, null, "yes", null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_5, null, null, null, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_5, null, null, null, null, "yes")
                            ),
                            Stream.of(
                                    // arity 6
                                    () -> FieldGroups.atLeastOne(GROUP_6, "yes", null, null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_6, null, "yes", null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_6, null, null, "yes", null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_6, null, null, null, "yes", null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_6, null, null, null, null, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_6, null, null, null, null, null, "yes"),
                                    // arity 7
                                    () -> FieldGroups.atLeastOne(GROUP_7, "yes", null, null, null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_7, null, "yes", null, null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_7, null, null, "yes", null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_7, null, null, null, "yes", null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_7, null, null, null, null, "yes", null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_7, null, null, null, null, null, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_7, null, null, null, null, null, null, "yes"),
                                    // arity 8
                                    () -> FieldGroups.atLeastOne(GROUP_8, "yes", null, null, null, null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, "yes", null, null, null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, "yes", null, null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, null, "yes", null, null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, null, null, "yes", null, null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, null, null, null, "yes", null, null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, null, null, null, null, "yes", null),
                                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, null, null, null, null, null, "yes")
                            )
                    ),
                    Stream.of(
                            // arity 9
                            () -> FieldGroups.atLeastOne(GROUP_9, "yes", null, null, null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, "yes", null, null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, "yes", null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, "yes", null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, null, "yes", null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, null, null, "yes", null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, null, null, null, "yes", null, null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, null, null, null, null, "yes", null),
                            () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, null, null, null, null, null, "yes"),
                            // arity 10
                            () -> FieldGroups.atLeastOne(GROUP_10, "yes", null, null, null, null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, "yes", null, null, null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, "yes", null, null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, "yes", null, null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, "yes", null, null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, null, "yes", null, null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, null, null, "yes", null, null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, null, null, null, "yes", null, null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, null, null, null, null, "yes", null),
                            () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, null, null, null, null, null, "yes")
                    )
            );
        }

        static Stream<Executable> atLeastOne_noneProvided() {
            return Stream.of(
                    () -> FieldGroups.atLeastOne(GROUP_4, null, null, null, null),
                    () -> FieldGroups.atLeastOne(GROUP_5, null, null, null, null, null),
                    () -> FieldGroups.atLeastOne(GROUP_6, null, null, null, null, null, null),
                    () -> FieldGroups.atLeastOne(GROUP_7, null, null, null, null, null, null, null),
                    () -> FieldGroups.atLeastOne(GROUP_8, null, null, null, null, null, null, null, null),
                    () -> FieldGroups.atLeastOne(GROUP_9, null, null, null, null, null, null, null, null, null),
                    () -> FieldGroups.atLeastOne(GROUP_10, null, null, null, null, null, null, null, null, null, null)
            );
        }

        // --- isProvided semantics ---

        @Test
        void nullGroup_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroups.atLeastOne(null, "a", "b"));
            assertEquals(NULL_GROUP_MSG, ex.getMessage());
        }

        @Test
        void arityMismatch_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> FieldGroups.atLeastOne(GROUP_3, "a", "b"));
            assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
        }

        // null is not provided
        @Test
        void nullValue_notProvided() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_2, null, null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // blank String is not provided
        @Test
        void blankStringValue_notProvided() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_2, "   ", null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // non-blank String is provided
        @Test
        void nonBlankStringValue_provided() {
            assertDoesNotThrow(() -> FieldGroups.atLeastOne(GROUP_2, "hello", null));
        }

        // empty Collection is not provided
        @Test
        void emptyCollectionValue_notProvided() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_2, List.of(), null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // non-empty Collection is provided
        @Test
        void nonEmptyCollectionValue_provided() {
            assertDoesNotThrow(() -> FieldGroups.atLeastOne(GROUP_2, List.of("x"), null));
        }

        // empty Object array is not provided
        @Test
        void emptyObjectArrayValue_notProvided() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_2, new Object[0], null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // non-empty Object array is provided
        @Test
        void nonEmptyObjectArrayValue_provided() {
            assertDoesNotThrow(() -> FieldGroups.atLeastOne(GROUP_2, new Object[]{AN_OBJECT}, null));
        }

        // empty Map is not provided
        @Test
        void emptyMapValue_notProvided() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_2, Map.of(), null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // --- Failure paths (with message verification) ---

        // non-empty Map is provided
        @Test
        void nonEmptyMapValue_provided() {
            assertDoesNotThrow(() -> FieldGroups.atLeastOne(GROUP_2, Map.of("k", "v"), null));
        }

        // arbitrary non-null Object is provided
        @Test
        void nonNullObjectValue_provided() {
            assertDoesNotThrow(() -> FieldGroups.atLeastOne(GROUP_2, AN_OBJECT, null));
        }

        // --- Positional coverage (arities 2–10, every position) ---

        @Test
        void twoFields_noneProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_2, null, null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // --- None-provided failure path (arities 4–10) ---

        @Test
        void threeFields_noneProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.atLeastOne(GROUP_3, null, null, null));
            assertEquals(MISSING_AT_LEAST_ONE_3_MSG, ex.getMessage());
        }

        // --- Varargs overload ---

        @ParameterizedTest
        @MethodSource("atLeastOne_singleFieldProvided")
        void singleFieldProvided_passes(Executable action) {
            assertDoesNotThrow(action);
        }

        // --- Providers ---

        @ParameterizedTest
        @MethodSource("atLeastOne_noneProvided")
        void noneProvided_throwsMissingFieldException(Executable action) {
            assertThrows(MissingFieldException.class, action);
        }

        @Nested
        class Varargs {
            @Test
            void nullGroup_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> FieldGroups.atLeastOne(null, new Object[]{"a", "b"}));
                assertEquals(NULL_GROUP_MSG, ex.getMessage());
            }

            @Test
            void arityMismatch_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () ->
                        FieldGroups.atLeastOne(GROUP_3, new Object[]{"a", "b"}));
                assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
            }

            @Test
            void noneProvided_throwsMissingFieldException() {
                var ex = assertThrows(MissingFieldException.class, () ->
                        FieldGroups.atLeastOne(GROUP_11, null, null, null, null, null, null, null, null, null, null, null));
                assertEquals(
                        "At least one of the 'f1', 'f2', 'f3', 'f4', 'f5', 'f6', 'f7', 'f8', 'f9', 'f10', or 'f11' fields must be provided.",
                        ex.getMessage()
                );
            }

            @Test
            void oneProvided_passes() {
                assertDoesNotThrow(() ->
                        FieldGroups.atLeastOne(GROUP_11, null, null, null, null, null, null, null, null, null, null, "yes"));
            }
        }
    }

    // -------------------------------------------------------------------------
    // OnlyOne
    // -------------------------------------------------------------------------

    @Nested
    class OnlyOne {
        static Stream<Executable> onlyOne_singleFieldProvided() {
            return Stream.concat(
                    Stream.concat(
                            Stream.of(
                                    // arity 2
                                    () -> FieldGroups.onlyOne(GROUP_2, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_2, null, "yes"),
                                    // arity 3
                                    () -> FieldGroups.onlyOne(GROUP_3, "yes", null, null),
                                    () -> FieldGroups.onlyOne(GROUP_3, null, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_3, null, null, "yes"),
                                    // arity 4
                                    () -> FieldGroups.onlyOne(GROUP_4, "yes", null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_4, null, "yes", null, null),
                                    () -> FieldGroups.onlyOne(GROUP_4, null, null, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_4, null, null, null, "yes"),
                                    // arity 5
                                    () -> FieldGroups.onlyOne(GROUP_5, "yes", null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_5, null, "yes", null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_5, null, null, "yes", null, null),
                                    () -> FieldGroups.onlyOne(GROUP_5, null, null, null, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_5, null, null, null, null, "yes")
                            ),
                            Stream.of(
                                    // arity 6
                                    () -> FieldGroups.onlyOne(GROUP_6, "yes", null, null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_6, null, "yes", null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_6, null, null, "yes", null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_6, null, null, null, "yes", null, null),
                                    () -> FieldGroups.onlyOne(GROUP_6, null, null, null, null, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_6, null, null, null, null, null, "yes"),
                                    // arity 7
                                    () -> FieldGroups.onlyOne(GROUP_7, "yes", null, null, null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_7, null, "yes", null, null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_7, null, null, "yes", null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, "yes", null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, null, "yes", null, null),
                                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, null, null, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, null, null, null, "yes"),
                                    // arity 8
                                    () -> FieldGroups.onlyOne(GROUP_8, "yes", null, null, null, null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, "yes", null, null, null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, null, "yes", null, null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, "yes", null, null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, "yes", null, null, null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, null, "yes", null, null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, null, null, "yes", null),
                                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, null, null, null, "yes")
                            )
                    ),
                    Stream.of(
                            // arity 9
                            () -> FieldGroups.onlyOne(GROUP_9, "yes", null, null, null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, "yes", null, null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, "yes", null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, null, "yes", null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, "yes", null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, "yes", null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, null, "yes", null, null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, null, null, "yes", null),
                            () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, null, null, null, "yes"),
                            // arity 10
                            () -> FieldGroups.onlyOne(GROUP_10, "yes", null, null, null, null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, "yes", null, null, null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, "yes", null, null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, "yes", null, null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, "yes", null, null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, "yes", null, null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, "yes", null, null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, null, "yes", null, null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, null, null, "yes", null),
                            () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, null, null, null, "yes")
                    )
            );
        }

        static Stream<Executable> onlyOne_twoFieldsProvided() {
            return Stream.of(
                    // arity 3 — (1,2), (2,3), (1,3) cover all ci true/false branches
                    () -> FieldGroups.onlyOne(GROUP_3, "a", "b", null),
                    () -> FieldGroups.onlyOne(GROUP_3, null, "b", "c"),
                    () -> FieldGroups.onlyOne(GROUP_3, "a", null, "c"),
                    // arity 4 — (1,2) and (3,4) are complementary; all branches covered
                    () -> FieldGroups.onlyOne(GROUP_4, "a", "b", null, null),
                    () -> FieldGroups.onlyOne(GROUP_4, null, null, "c", "d"),
                    // arity 5 — (1,2), (3,4), (4,5)
                    () -> FieldGroups.onlyOne(GROUP_5, "a", "b", null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_5, null, null, "c", "d", null),
                    () -> FieldGroups.onlyOne(GROUP_5, null, null, null, "d", "e"),
                    // arity 6 — (1,2), (3,4), (5,6)
                    () -> FieldGroups.onlyOne(GROUP_6, "a", "b", null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_6, null, null, "c", "d", null, null),
                    () -> FieldGroups.onlyOne(GROUP_6, null, null, null, null, "e", "f"),
                    // arity 7 — (1,2), (3,4), (5,6), (6,7)
                    () -> FieldGroups.onlyOne(GROUP_7, "a", "b", null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_7, null, null, "c", "d", null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, null, "e", "f", null),
                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, null, null, "f", "g"),
                    // arity 8 — (1,2), (3,4), (5,6), (7,8)
                    () -> FieldGroups.onlyOne(GROUP_8, "a", "b", null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_8, null, null, "c", "d", null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, "e", "f", null, null),
                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, null, null, "g", "h"),
                    // arity 9 — (1,2), (3,4), (5,6), (7,8), (8,9)
                    () -> FieldGroups.onlyOne(GROUP_9, "a", "b", null, null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_9, null, null, "c", "d", null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, "e", "f", null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, null, "g", "h", null),
                    () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, null, null, "h", "i"),
                    // arity 10 — (1,2), (3,4), (5,6), (7,8), (9,10)
                    () -> FieldGroups.onlyOne(GROUP_10, "a", "b", null, null, null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_10, null, null, "c", "d", null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, "e", "f", null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, "g", "h", null, null),
                    () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, null, null, "i", "j")
            );
        }

        static Stream<Executable> onlyOne_noneProvided() {
            return Stream.of(
                    () -> FieldGroups.onlyOne(GROUP_4, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_5, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_6, null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_7, null, null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_8, null, null, null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_9, null, null, null, null, null, null, null, null, null),
                    () -> FieldGroups.onlyOne(GROUP_10, null, null, null, null, null, null, null, null, null, null)
            );
        }

        @Test
        void nullGroup_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroups.onlyOne(null, "a", "b"));
            assertEquals(NULL_GROUP_MSG, ex.getMessage());
        }

        @Test
        void arityMismatch_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> FieldGroups.onlyOne(GROUP_3, "a", "b"));
            assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
        }

        // 2-field — none provided
        @Test
        void twoFields_noneProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.onlyOne(GROUP_2, null, null));
            assertEquals(MISSING_AT_LEAST_ONE_2_MSG, ex.getMessage());
        }

        // 2-field — both provided
        @Test
        void twoFields_bothProvided_throwsTooManyFieldsException() {
            var ex = assertThrows(TooManyFieldsException.class, () -> FieldGroups.onlyOne(GROUP_2, "a", "b"));
            assertEquals(ONLY_ONE_TOO_MANY_2_MSG, ex.getMessage());
        }

        // --- Positional coverage (arities 2–10, every position) ---

        // 2-field — exactly one provided
        @Test
        void twoFields_oneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.onlyOne(GROUP_2, "a", null));
        }

        // --- Too-many coverage (key pairs across arities 3–10) ---

        // 3-field — none provided
        @Test
        void threeFields_noneProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.onlyOne(GROUP_3, null, null, null));
            assertEquals(MISSING_AT_LEAST_ONE_3_MSG, ex.getMessage());
        }

        // --- None-provided failure path (arities 4–10) ---

        // 3-field — two provided (f1 and f2) — message verification
        @Test
        void threeFields_firstAndSecondProvided_throwsTooManyFieldsException() {
            var ex = assertThrows(TooManyFieldsException.class, () -> FieldGroups.onlyOne(GROUP_3, "a", "b", null));
            assertEquals(ONLY_ONE_TOO_MANY_3_MSG, ex.getMessage());
        }

        // --- Varargs overload ---

        @ParameterizedTest
        @MethodSource("onlyOne_singleFieldProvided")
        void singleFieldProvided_passes(Executable action) {
            assertDoesNotThrow(action);
        }

        // --- Providers ---

        @ParameterizedTest
        @MethodSource("onlyOne_twoFieldsProvided")
        void twoFieldsProvided_throwsTooManyFieldsException(Executable action) {
            assertThrows(TooManyFieldsException.class, action);
        }

        @ParameterizedTest
        @MethodSource("onlyOne_noneProvided")
        void noneProvided_throwsMissingFieldException(Executable action) {
            assertThrows(MissingFieldException.class, action);
        }

        @Nested
        class Varargs {
            @Test
            void nullGroup_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> FieldGroups.onlyOne(null, new Object[]{"a", "b"}));
                assertEquals(NULL_GROUP_MSG, ex.getMessage());
            }

            @Test
            void arityMismatch_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () ->
                        FieldGroups.onlyOne(GROUP_3, new Object[]{"a", "b"}));
                assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
            }

            @Test
            void noneProvided_throwsMissingFieldException() {
                assertThrows(MissingFieldException.class, () ->
                        FieldGroups.onlyOne(GROUP_11, null, null, null, null, null, null, null, null, null, null, null));
            }

            @Test
            void twoProvided_throwsTooManyFieldsException() {
                var ex = assertThrows(TooManyFieldsException.class, () ->
                        FieldGroups.onlyOne(GROUP_11, "a", "b", null, null, null, null, null, null, null, null, null));
                assertEquals("Only one of the 'f1' or 'f2' fields may be provided.", ex.getMessage());
            }

            @Test
            void oneProvided_passes() {
                assertDoesNotThrow(() ->
                        FieldGroups.onlyOne(GROUP_11, null, null, null, null, null, null, null, null, null, null, "yes"));
            }
        }
    }

    // -------------------------------------------------------------------------
    // AtMostOne
    // -------------------------------------------------------------------------

    @Nested
    class AtMostOne {
        static Stream<Executable> atMostOne_singleFieldProvided() {
            return Stream.concat(
                    Stream.concat(
                            Stream.of(
                                    // arity 2
                                    () -> FieldGroups.atMostOne(GROUP_2, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_2, null, "yes"),
                                    // arity 3
                                    () -> FieldGroups.atMostOne(GROUP_3, "yes", null, null),
                                    () -> FieldGroups.atMostOne(GROUP_3, null, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_3, null, null, "yes"),
                                    // arity 4
                                    () -> FieldGroups.atMostOne(GROUP_4, "yes", null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_4, null, "yes", null, null),
                                    () -> FieldGroups.atMostOne(GROUP_4, null, null, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_4, null, null, null, "yes"),
                                    // arity 5
                                    () -> FieldGroups.atMostOne(GROUP_5, "yes", null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_5, null, "yes", null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_5, null, null, "yes", null, null),
                                    () -> FieldGroups.atMostOne(GROUP_5, null, null, null, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_5, null, null, null, null, "yes")
                            ),
                            Stream.of(
                                    // arity 6
                                    () -> FieldGroups.atMostOne(GROUP_6, "yes", null, null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_6, null, "yes", null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_6, null, null, "yes", null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_6, null, null, null, "yes", null, null),
                                    () -> FieldGroups.atMostOne(GROUP_6, null, null, null, null, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_6, null, null, null, null, null, "yes"),
                                    // arity 7
                                    () -> FieldGroups.atMostOne(GROUP_7, "yes", null, null, null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_7, null, "yes", null, null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_7, null, null, "yes", null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_7, null, null, null, "yes", null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_7, null, null, null, null, "yes", null, null),
                                    () -> FieldGroups.atMostOne(GROUP_7, null, null, null, null, null, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_7, null, null, null, null, null, null, "yes"),
                                    // arity 8
                                    () -> FieldGroups.atMostOne(GROUP_8, "yes", null, null, null, null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, "yes", null, null, null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, null, "yes", null, null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, "yes", null, null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, null, "yes", null, null, null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, null, null, "yes", null, null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, null, null, null, "yes", null),
                                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, null, null, null, null, "yes")
                            )
                    ),
                    Stream.of(
                            // arity 9
                            () -> FieldGroups.atMostOne(GROUP_9, "yes", null, null, null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, "yes", null, null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, "yes", null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, null, "yes", null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, "yes", null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, null, "yes", null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, null, null, "yes", null, null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, null, null, null, "yes", null),
                            () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, null, null, null, null, "yes"),
                            // arity 10
                            () -> FieldGroups.atMostOne(GROUP_10, "yes", null, null, null, null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, "yes", null, null, null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, "yes", null, null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, "yes", null, null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, "yes", null, null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, "yes", null, null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, null, "yes", null, null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, null, null, "yes", null, null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, null, null, null, "yes", null),
                            () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, null, null, null, null, "yes")
                    )
            );
        }

        static Stream<Executable> atMostOne_twoFieldsProvided() {
            return Stream.of(
                    // arity 3 — (1,2), (2,3), (1,3) cover all ci true/false branches
                    () -> FieldGroups.atMostOne(GROUP_3, "a", "b", null),
                    () -> FieldGroups.atMostOne(GROUP_3, null, "b", "c"),
                    () -> FieldGroups.atMostOne(GROUP_3, "a", null, "c"),
                    // arity 4 — (1,2) and (3,4) are complementary; all branches covered
                    () -> FieldGroups.atMostOne(GROUP_4, "a", "b", null, null),
                    () -> FieldGroups.atMostOne(GROUP_4, null, null, "c", "d"),
                    // arity 5 — (1,2), (3,4), (4,5)
                    () -> FieldGroups.atMostOne(GROUP_5, "a", "b", null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_5, null, null, "c", "d", null),
                    () -> FieldGroups.atMostOne(GROUP_5, null, null, null, "d", "e"),
                    // arity 6 — (1,2), (3,4), (5,6)
                    () -> FieldGroups.atMostOne(GROUP_6, "a", "b", null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_6, null, null, "c", "d", null, null),
                    () -> FieldGroups.atMostOne(GROUP_6, null, null, null, null, "e", "f"),
                    // arity 7 — (1,2), (3,4), (5,6), (6,7)
                    () -> FieldGroups.atMostOne(GROUP_7, "a", "b", null, null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_7, null, null, "c", "d", null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_7, null, null, null, null, "e", "f", null),
                    () -> FieldGroups.atMostOne(GROUP_7, null, null, null, null, null, "f", "g"),
                    // arity 8 — (1,2), (3,4), (5,6), (7,8)
                    () -> FieldGroups.atMostOne(GROUP_8, "a", "b", null, null, null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_8, null, null, "c", "d", null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, null, "e", "f", null, null),
                    () -> FieldGroups.atMostOne(GROUP_8, null, null, null, null, null, null, "g", "h"),
                    // arity 9 — (1,2), (3,4), (5,6), (7,8), (8,9)
                    () -> FieldGroups.atMostOne(GROUP_9, "a", "b", null, null, null, null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_9, null, null, "c", "d", null, null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, "e", "f", null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, null, null, "g", "h", null),
                    () -> FieldGroups.atMostOne(GROUP_9, null, null, null, null, null, null, null, "h", "i"),
                    // arity 10 — (1,2), (3,4), (5,6), (7,8), (9,10)
                    () -> FieldGroups.atMostOne(GROUP_10, "a", "b", null, null, null, null, null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_10, null, null, "c", "d", null, null, null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, "e", "f", null, null, null, null),
                    () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, null, "g", "h", null, null),
                    () -> FieldGroups.atMostOne(GROUP_10, null, null, null, null, null, null, null, null, "i", "j")
            );
        }

        @Test
        void nullGroup_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroups.atMostOne(null, "a", "b"));
            assertEquals(NULL_GROUP_MSG, ex.getMessage());
        }

        @Test
        void arityMismatch_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> FieldGroups.atMostOne(GROUP_3, "a", "b"));
            assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
        }

        // 2-field — zero provided (valid)
        @Test
        void twoFields_noneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.atMostOne(GROUP_2, null, null));
        }

        // 2-field — one provided (valid)
        @Test
        void twoFields_oneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.atMostOne(GROUP_2, "a", null));
        }

        // 2-field — both provided
        @Test
        void twoFields_bothProvided_throwsTooManyFieldsException() {
            var ex = assertThrows(TooManyFieldsException.class, () -> FieldGroups.atMostOne(GROUP_2, "a", "b"));
            assertEquals(AT_MOST_ONE_TOO_MANY_2_MSG, ex.getMessage());
        }

        // 3-field — two provided (f1 and f2) — message verification
        @Test
        void threeFields_firstAndSecondProvided_throwsTooManyFieldsException() {
            var ex = assertThrows(TooManyFieldsException.class, () -> FieldGroups.atMostOne(GROUP_3, "a", "b", null));
            assertEquals(AT_MOST_ONE_TOO_MANY_3_MSG, ex.getMessage());
        }

        // --- Positional coverage (arities 2–10, every position) ---

        // 3-field — one provided (valid)
        @Test
        void threeFields_oneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.atMostOne(GROUP_3, null, "hello", null));
        }

        // --- Too-many coverage (key pairs across arities 3–10) ---

        // 3-field — none provided (valid)
        @Test
        void threeFields_noneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.atMostOne(GROUP_3, null, null, null));
        }

        // --- Varargs overload ---

        @ParameterizedTest
        @MethodSource("atMostOne_singleFieldProvided")
        void singleFieldProvided_passes(Executable action) {
            assertDoesNotThrow(action);
        }

        // --- Providers ---

        @ParameterizedTest
        @MethodSource("atMostOne_twoFieldsProvided")
        void twoFieldsProvided_throwsTooManyFieldsException(Executable action) {
            assertThrows(TooManyFieldsException.class, action);
        }

        @Nested
        class Varargs {
            @Test
            void nullGroup_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> FieldGroups.atMostOne(null, new Object[]{"a", "b"}));
                assertEquals(NULL_GROUP_MSG, ex.getMessage());
            }

            @Test
            void arityMismatch_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () ->
                        FieldGroups.atMostOne(GROUP_3, new Object[]{"a", "b"}));
                assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
            }

            @Test
            void twoProvided_throwsTooManyFieldsException() {
                var ex = assertThrows(TooManyFieldsException.class, () ->
                        FieldGroups.atMostOne(GROUP_11, "a", "b", null, null, null, null, null, null, null, null, null));
                assertEquals("At most one of the 'f1' or 'f2' fields may be provided.", ex.getMessage());
            }

            @Test
            void oneProvided_passes() {
                assertDoesNotThrow(() ->
                        FieldGroups.atMostOne(GROUP_11, null, null, null, null, null, null, null, null, null, null, "yes"));
            }
        }
    }

    // -------------------------------------------------------------------------
    // NoneOrAll
    // -------------------------------------------------------------------------

    @Nested
    class NoneOrAll {
        static Stream<Executable> noneOrAll_allProvided() {
            return Stream.of(
                    () -> FieldGroups.noneOrAll(GROUP_4, "a", "b", "c", "d"),
                    () -> FieldGroups.noneOrAll(GROUP_5, "a", "b", "c", "d", "e"),
                    () -> FieldGroups.noneOrAll(GROUP_6, "a", "b", "c", "d", "e", "f"),
                    () -> FieldGroups.noneOrAll(GROUP_7, "a", "b", "c", "d", "e", "f", "g"),
                    () -> FieldGroups.noneOrAll(GROUP_8, "a", "b", "c", "d", "e", "f", "g", "h"),
                    () -> FieldGroups.noneOrAll(GROUP_9, "a", "b", "c", "d", "e", "f", "g", "h", "i"),
                    () -> FieldGroups.noneOrAll(GROUP_10, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j")
            );
        }

        static Stream<Executable> noneOrAll_noneProvided() {
            return Stream.of(
                    () -> FieldGroups.noneOrAll(GROUP_4, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_5, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_6, null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_7, null, null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_8, null, null, null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_9, null, null, null, null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_10, null, null, null, null, null, null, null, null, null, null)
            );
        }

        static Stream<Executable> noneOrAll_partiallyProvided() {
            return Stream.of(
                    () -> FieldGroups.noneOrAll(GROUP_4, "a", null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_5, "a", null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_6, "a", null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_7, "a", null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_8, "a", null, null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_9, "a", null, null, null, null, null, null, null, null),
                    () -> FieldGroups.noneOrAll(GROUP_10, "a", null, null, null, null, null, null, null, null, null)
            );
        }

        @Test
        void nullGroup_throwsNullPointerException() {
            var ex = assertThrows(NullPointerException.class, () -> FieldGroups.noneOrAll(null, "a", "b"));
            assertEquals(NULL_GROUP_MSG, ex.getMessage());
        }

        @Test
        void arityMismatch_throwsIllegalConfigurationException() {
            var ex = assertThrows(IllegalConfigurationException.class, () -> FieldGroups.noneOrAll(GROUP_3, "a", "b"));
            assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
        }

        // 2-field — none provided (valid)
        @Test
        void twoFields_noneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.noneOrAll(GROUP_2, null, null));
        }

        // 2-field — both provided (valid)
        @Test
        void twoFields_allProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.noneOrAll(GROUP_2, "a", "b"));
        }

        // 2-field — partial (first only)
        @Test
        void twoFields_onlyFirstProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.noneOrAll(GROUP_2, "a", null));
            assertEquals(NONE_OR_ALL_2_MSG, ex.getMessage());
        }

        // 2-field — partial (second only)
        @Test
        void twoFields_onlySecondProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.noneOrAll(GROUP_2, null, "b"));
            assertEquals(NONE_OR_ALL_2_MSG, ex.getMessage());
        }

        // --- All-provided passes (arities 4–10) ---

        // 3-field — none provided (valid)
        @Test
        void threeFields_noneProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.noneOrAll(GROUP_3, null, null, null));
        }

        // --- None-provided passes (arities 4–10) ---

        // 3-field — all provided (valid)
        @Test
        void threeFields_allProvided_passes() {
            assertDoesNotThrow(() -> FieldGroups.noneOrAll(GROUP_3, "a", "b", "c"));
        }

        // --- Partial-provided failure path (arities 4–10) ---

        // 3-field — partial
        @Test
        void threeFields_partiallyProvided_throwsMissingFieldException() {
            var ex = assertThrows(MissingFieldException.class, () -> FieldGroups.noneOrAll(GROUP_3, "a", null, "c"));
            assertEquals(NONE_OR_ALL_3_MSG, ex.getMessage());
        }

        // --- Varargs overload ---

        @ParameterizedTest
        @MethodSource("noneOrAll_allProvided")
        void allProvided_passes(Executable action) {
            assertDoesNotThrow(action);
        }

        // --- Providers ---

        @ParameterizedTest
        @MethodSource("noneOrAll_noneProvided")
        void noneProvided_passes(Executable action) {
            assertDoesNotThrow(action);
        }

        @ParameterizedTest
        @MethodSource("noneOrAll_partiallyProvided")
        void partiallyProvided_throwsMissingFieldException(Executable action) {
            assertThrows(MissingFieldException.class, action);
        }

        @Nested
        class Varargs {
            @Test
            void nullGroup_throwsNullPointerException() {
                var ex = assertThrows(NullPointerException.class, () -> FieldGroups.noneOrAll(null, new Object[]{"a", "b"}));
                assertEquals(NULL_GROUP_MSG, ex.getMessage());
            }

            @Test
            void arityMismatch_throwsIllegalConfigurationException() {
                var ex = assertThrows(IllegalConfigurationException.class, () ->
                        FieldGroups.noneOrAll(GROUP_3, new Object[]{"a", "b"}));
                assertEquals(ARITY_3_VS_2_MSG, ex.getMessage());
            }

            @Test
            void partiallyProvided_throwsMissingFieldException() {
                var ex = assertThrows(MissingFieldException.class, () ->
                        FieldGroups.noneOrAll(GROUP_11, "a", null, null, null, null, null, null, null, null, null, null));
                assertEquals(
                        "The 'f1', 'f2', 'f3', 'f4', 'f5', 'f6', 'f7', 'f8', 'f9', 'f10', and 'f11' fields must all be provided together, or none at all.",
                        ex.getMessage()
                );
            }

            @Test
            void allProvided_passes() {
                assertDoesNotThrow(() ->
                        FieldGroups.noneOrAll(GROUP_11, "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"));
            }

            @Test
            void noneProvided_passes() {
                assertDoesNotThrow(() ->
                        FieldGroups.noneOrAll(GROUP_11, null, null, null, null, null, null, null, null, null, null, null));
            }
        }
    }
}
