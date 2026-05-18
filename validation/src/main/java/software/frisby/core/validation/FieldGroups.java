package software.frisby.core.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Static utility methods for validating field-group cardinality constraints.
 *
 * <p>A <em>field group</em> is a set of related fields where the combination of
 * provided values must satisfy a cardinality rule — at least one, exactly one,
 * at most one, or all-or-none. {@link FieldGroups} enforces these rules using
 * a {@link FieldGroup} to identify the field names and a corresponding sequence
 * of runtime values to check.
 *
 * <p>The definition of "provided" depends on the runtime type of each value:
 * <ul>
 *   <li>{@code null} — never provided</li>
 *   <li>{@link String} — provided when non-null and non-blank</li>
 *   <li>{@link Collection} — provided when non-null and non-empty</li>
 *   <li>{@code Object[]} — provided when non-null and non-empty</li>
 *   <li>{@link Map} — provided when non-null and non-empty</li>
 *   <li>Any other reference type — provided when non-null</li>
 * </ul>
 *
 * <p>Four constraint families are provided, each with fixed-arity overloads for
 * 2 through 10 fields and a varargs fallback for 11 or more fields:
 * <ul>
 *   <li>{@code atLeastOne} — at least one field must be provided</li>
 *   <li>{@code onlyOne}    — exactly one field must be provided</li>
 *   <li>{@code atMostOne}  — at most one field may be provided (zero is fine)</li>
 *   <li>{@code noneOrAll}  — either no fields or all fields must be provided</li>
 * </ul>
 *
 * <p>Fixed-arity overloads (2–10 fields) perform no heap allocation on the happy
 * path. The varargs overload allocates an {@code Object[]} on every call and
 * should only be used when more than 10 fields are needed.
 *
 * <p>Usage example:
 * <pre>
 * private static final FieldGroup FILTER = FieldGroup.of("match", "and", "or");
 *
 * FieldGroups.onlyOne(FILTER, match, and, or);
 * </pre>
 *
 * @see FieldGroup
 */
public final class FieldGroups {
    private static final String GROUP_ARGUMENT_NAME = "group";

    private FieldGroups() {
    }

    // -------------------------------------------------------------------------
    // atLeastOne — 2-field through 10-field fixed-arity overloads
    // -------------------------------------------------------------------------

    /**
     * Validates that at least one of the two fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 2 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 2 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group, Object v1, Object v2) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 2);

        if (isProvided(v1) || isProvided(v2)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the three fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 3 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 3 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group, Object v1, Object v2, Object v3) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 3);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the four fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 4 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 4 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group, Object v1, Object v2, Object v3, Object v4) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 4);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the five fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 5 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 5 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group, Object v1, Object v2, Object v3, Object v4, Object v5) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 5);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4) || isProvided(v5)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the six fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 6 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 6 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group,
                                  Object v1,
                                  Object v2,
                                  Object v3,
                                  Object v4,
                                  Object v5,
                                  Object v6) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 6);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4) || isProvided(v5) ||
                isProvided(v6)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the seven fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 7 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 7 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group,
                                  Object v1,
                                  Object v2,
                                  Object v3,
                                  Object v4,
                                  Object v5,
                                  Object v6,
                                  Object v7) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 7);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4) || isProvided(v5) ||
                isProvided(v6) || isProvided(v7)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the eight fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 8 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 8 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group,
                                  Object v1,
                                  Object v2,
                                  Object v3,
                                  Object v4,
                                  Object v5,
                                  Object v6,
                                  Object v7,
                                  Object v8) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 8);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4) || isProvided(v5) ||
                isProvided(v6) || isProvided(v7) || isProvided(v8)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the nine fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 9 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 9 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group,
                                  Object v1,
                                  Object v2,
                                  Object v3,
                                  Object v4,
                                  Object v5,
                                  Object v6,
                                  Object v7,
                                  Object v8,
                                  Object v9) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 9);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4) || isProvided(v5) ||
                isProvided(v6) || isProvided(v7) || isProvided(v8) || isProvided(v9)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the ten fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 10 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @param v10   The value of the tenth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 10 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group,
                                  Object v1,
                                  Object v2,
                                  Object v3,
                                  Object v4,
                                  Object v5,
                                  Object v6,
                                  Object v7,
                                  Object v8,
                                  Object v9,
                                  Object v10) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 10);

        if (isProvided(v1) || isProvided(v2) || isProvided(v3) || isProvided(v4) || isProvided(v5) ||
                isProvided(v6) || isProvided(v7) || isProvided(v8) || isProvided(v9) || isProvided(v10)) return;

        throw missingAtLeastOne(group);
    }

    /**
     * Validates that at least one of the fields represented by {@code group} is provided.
     *
     * <p><b>Note:</b> This overload accepts variable-length arguments and allocates an
     * {@code Object[]} array on every invocation. For groups of 10 or fewer fields,
     * prefer the corresponding fixed-arity overload to avoid this allocation.
     *
     * @param group  The {@link FieldGroup} identifying the field names; must not be null.
     * @param values The field values, in the same order as the names in {@code group}.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if the number of values does not match the number of fields in {@code group}.
     * @throws MissingFieldException         if none of the fields are provided.
     */
    public static void atLeastOne(FieldGroup group, Object... values) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, values.length);

        for (Object v : values) {
            if (isProvided(v)) return;
        }

        throw missingAtLeastOne(group);
    }

    // -------------------------------------------------------------------------
    // onlyOne — 2-field through 10-field fixed-arity overloads
    // -------------------------------------------------------------------------

    /**
     * Validates that exactly one of the two fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 2 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 2 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group, Object v1, Object v2) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 2);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int count = c1 + c2;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) throw tooManyFieldsOnlyOne(group.names());
    }

    /**
     * Validates that exactly one of the three fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 3 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 3 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group, Object v1, Object v2, Object v3) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 3);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int count = c1 + c2 + c3;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the four fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 4 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 4 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group, Object v1, Object v2, Object v3, Object v4) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 4);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int count = c1 + c2 + c3 + c4;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the five fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 5 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 5 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group, Object v1, Object v2, Object v3, Object v4, Object v5) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 5);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the six fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 6 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 6 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group,
                               Object v1,
                               Object v2,
                               Object v3,
                               Object v4,
                               Object v5,
                               Object v6) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 6);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the seven fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 7 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 7 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group,
                               Object v1,
                               Object v2,
                               Object v3,
                               Object v4,
                               Object v5,
                               Object v6,
                               Object v7) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 7);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the eight fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 8 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 8 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group,
                               Object v1,
                               Object v2,
                               Object v3,
                               Object v4,
                               Object v5,
                               Object v6,
                               Object v7,
                               Object v8) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 8);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int c8 = isProvided(v8) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            if (c8 == 1) provided.add(group.nameAt(7));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the nine fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 9 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 9 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group,
                               Object v1,
                               Object v2,
                               Object v3,
                               Object v4,
                               Object v5,
                               Object v6,
                               Object v7,
                               Object v8,
                               Object v9) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 9);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int c8 = isProvided(v8) ? 1 : 0;
        int c9 = isProvided(v9) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            if (c8 == 1) provided.add(group.nameAt(7));
            if (c9 == 1) provided.add(group.nameAt(8));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the ten fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 10 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @param v10   The value of the tenth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 10 fields.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group,
                               Object v1,
                               Object v2,
                               Object v3,
                               Object v4,
                               Object v5,
                               Object v6,
                               Object v7,
                               Object v8,
                               Object v9,
                               Object v10) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 10);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int c8 = isProvided(v8) ? 1 : 0;
        int c9 = isProvided(v9) ? 1 : 0;
        int c10 = isProvided(v10) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9 + c10;

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            if (c8 == 1) provided.add(group.nameAt(7));
            if (c9 == 1) provided.add(group.nameAt(8));
            if (c10 == 1) provided.add(group.nameAt(9));
            throw tooManyFieldsOnlyOne(provided);
        }
    }

    /**
     * Validates that exactly one of the fields represented by {@code group} is provided.
     *
     * <p><b>Note:</b> This overload accepts variable-length arguments and allocates an
     * {@code Object[]} array on every invocation. For groups of 10 or fewer fields,
     * prefer the corresponding fixed-arity overload to avoid this allocation.
     *
     * @param group  The {@link FieldGroup} identifying the field names; must not be null.
     * @param values The field values, in the same order as the names in {@code group}.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if the number of values does not match the number of fields in {@code group}.
     * @throws MissingFieldException         if none of the fields are provided.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void onlyOne(FieldGroup group, Object... values) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, values.length);

        int count = 0;

        for (Object v : values) {
            if (isProvided(v)) count++;
        }

        if (count == 0) throw missingAtLeastOne(group);

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);

            for (int i = 0; i < values.length; i++) {
                if (isProvided(values[i])) provided.add(group.nameAt(i));
            }

            throw tooManyFieldsOnlyOne(provided);
        }
    }

    // -------------------------------------------------------------------------
    // atMostOne — 2-field through 10-field fixed-arity overloads
    // -------------------------------------------------------------------------

    /**
     * Validates that at most one of the two fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 2 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 2 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group, Object v1, Object v2) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 2);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;

        if (c1 + c2 > 1) throw tooManyFieldsAtMostOne(group.names());
    }

    /**
     * Validates that at most one of the three fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 3 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 3 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group, Object v1, Object v2, Object v3) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 3);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int count = c1 + c2 + c3;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the four fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 4 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 4 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group, Object v1, Object v2, Object v3, Object v4) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 4);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int count = c1 + c2 + c3 + c4;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the five fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 5 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 5 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group, Object v1, Object v2, Object v3, Object v4, Object v5) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 5);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the six fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 6 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 6 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 6);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the seven fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 7 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 7 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 7);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the eight fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 8 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 8 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7,
                                 Object v8) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 8);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int c8 = isProvided(v8) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            if (c8 == 1) provided.add(group.nameAt(7));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the nine fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 9 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 9 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7,
                                 Object v8,
                                 Object v9) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 9);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int c8 = isProvided(v8) ? 1 : 0;
        int c9 = isProvided(v9) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            if (c8 == 1) provided.add(group.nameAt(7));
            if (c9 == 1) provided.add(group.nameAt(8));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the ten fields represented by {@code group} is provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 10 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @param v10   The value of the tenth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 10 fields.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7,
                                 Object v8,
                                 Object v9,
                                 Object v10) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 10);

        int c1 = isProvided(v1) ? 1 : 0;
        int c2 = isProvided(v2) ? 1 : 0;
        int c3 = isProvided(v3) ? 1 : 0;
        int c4 = isProvided(v4) ? 1 : 0;
        int c5 = isProvided(v5) ? 1 : 0;
        int c6 = isProvided(v6) ? 1 : 0;
        int c7 = isProvided(v7) ? 1 : 0;
        int c8 = isProvided(v8) ? 1 : 0;
        int c9 = isProvided(v9) ? 1 : 0;
        int c10 = isProvided(v10) ? 1 : 0;
        int count = c1 + c2 + c3 + c4 + c5 + c6 + c7 + c8 + c9 + c10;

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);
            if (c1 == 1) provided.add(group.nameAt(0));
            if (c2 == 1) provided.add(group.nameAt(1));
            if (c3 == 1) provided.add(group.nameAt(2));
            if (c4 == 1) provided.add(group.nameAt(3));
            if (c5 == 1) provided.add(group.nameAt(4));
            if (c6 == 1) provided.add(group.nameAt(5));
            if (c7 == 1) provided.add(group.nameAt(6));
            if (c8 == 1) provided.add(group.nameAt(7));
            if (c9 == 1) provided.add(group.nameAt(8));
            if (c10 == 1) provided.add(group.nameAt(9));
            throw tooManyFieldsAtMostOne(provided);
        }
    }

    /**
     * Validates that at most one of the fields represented by {@code group} is provided.
     *
     * <p><b>Note:</b> This overload accepts variable-length arguments and allocates an
     * {@code Object[]} array on every invocation. For groups of 10 or fewer fields,
     * prefer the corresponding fixed-arity overload to avoid this allocation.
     *
     * @param group  The {@link FieldGroup} identifying the field names; must not be null.
     * @param values The field values, in the same order as the names in {@code group}.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if the number of values does not match the number of fields in {@code group}.
     * @throws TooManyFieldsException        if more than one field is provided.
     */
    public static void atMostOne(FieldGroup group, Object... values) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, values.length);

        int count = 0;

        for (Object v : values) {
            if (isProvided(v)) count++;
        }

        if (count > 1) {
            List<String> provided = new ArrayList<>(count);

            for (int i = 0; i < values.length; i++) {
                if (isProvided(values[i])) provided.add(group.nameAt(i));
            }

            throw tooManyFieldsAtMostOne(provided);
        }
    }

    // -------------------------------------------------------------------------
    // noneOrAll — 2-field through 10-field fixed-arity overloads
    // -------------------------------------------------------------------------

    /**
     * Validates that either none or both of the two fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 2 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 2 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group, Object v1, Object v2) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 2);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0);

        if (count != 0 && count != 2) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all three of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 3 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 3 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group, Object v1, Object v2, Object v3) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 3);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0);

        if (count != 0 && count != 3) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all four of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 4 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 4 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group, Object v1, Object v2, Object v3, Object v4) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 4);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0);

        if (count != 0 && count != 4) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all five of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 5 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 5 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group, Object v1, Object v2, Object v3, Object v4, Object v5) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 5);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0) + (isProvided(v5) ? 1 : 0);

        if (count != 0 && count != 5) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all six of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 6 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 6 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 6);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0) + (isProvided(v5) ? 1 : 0) + (isProvided(v6) ? 1 : 0);

        if (count != 0 && count != 6) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all seven of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 7 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 7 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 7);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0) + (isProvided(v5) ? 1 : 0) + (isProvided(v6) ? 1 : 0) +
                (isProvided(v7) ? 1 : 0);

        if (count != 0 && count != 7) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all eight of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 8 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 8 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7,
                                 Object v8) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 8);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0) + (isProvided(v5) ? 1 : 0) + (isProvided(v6) ? 1 : 0) +
                (isProvided(v7) ? 1 : 0) + (isProvided(v8) ? 1 : 0);

        if (count != 0 && count != 8) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all nine of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 9 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 9 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7,
                                 Object v8,
                                 Object v9) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 9);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0) + (isProvided(v5) ? 1 : 0) + (isProvided(v6) ? 1 : 0) +
                (isProvided(v7) ? 1 : 0) + (isProvided(v8) ? 1 : 0) + (isProvided(v9) ? 1 : 0);

        if (count != 0 && count != 9) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all ten of the fields represented by {@code group} are provided.
     *
     * @param group The {@link FieldGroup} identifying the field names; must not be null and must define exactly 10 fields.
     * @param v1    The value of the first field.
     * @param v2    The value of the second field.
     * @param v3    The value of the third field.
     * @param v4    The value of the fourth field.
     * @param v5    The value of the fifth field.
     * @param v6    The value of the sixth field.
     * @param v7    The value of the seventh field.
     * @param v8    The value of the eighth field.
     * @param v9    The value of the ninth field.
     * @param v10   The value of the tenth field.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if {@code group} does not define exactly 10 fields.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group,
                                 Object v1,
                                 Object v2,
                                 Object v3,
                                 Object v4,
                                 Object v5,
                                 Object v6,
                                 Object v7,
                                 Object v8,
                                 Object v9,
                                 Object v10) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, 10);

        int count = (isProvided(v1) ? 1 : 0) + (isProvided(v2) ? 1 : 0) + (isProvided(v3) ? 1 : 0) +
                (isProvided(v4) ? 1 : 0) + (isProvided(v5) ? 1 : 0) + (isProvided(v6) ? 1 : 0) +
                (isProvided(v7) ? 1 : 0) + (isProvided(v8) ? 1 : 0) + (isProvided(v9) ? 1 : 0) +
                (isProvided(v10) ? 1 : 0);

        if (count != 0 && count != 10) throw missingNoneOrAll(group);
    }

    /**
     * Validates that either none or all of the fields represented by {@code group} are provided.
     *
     * <p><b>Note:</b> This overload accepts variable-length arguments and allocates an
     * {@code Object[]} array on every invocation. For groups of 10 or fewer fields,
     * prefer the corresponding fixed-arity overload to avoid this allocation.
     *
     * @param group  The {@link FieldGroup} identifying the field names; must not be null.
     * @param values The field values, in the same order as the names in {@code group}.
     * @throws NullPointerException          if {@code group} is null.
     * @throws IllegalConfigurationException if the number of values does not match the number of fields in {@code group}.
     * @throws MissingFieldException         if some but not all fields are provided.
     */
    public static void noneOrAll(FieldGroup group, Object... values) {
        Throws.ifNull(GROUP_ARGUMENT_NAME, group);
        throwIfArityMismatch(group, values.length);

        int count = 0;

        for (Object v : values) {
            if (isProvided(v)) count++;
        }

        if (count != 0 && count != values.length) throw missingNoneOrAll(group);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static boolean isProvided(Object value) {
        if (null == value) return false;
        if (value instanceof String s) return !s.isBlank();
        if (value instanceof Collection<?> c) return !c.isEmpty();
        if (value instanceof Object[] a) return a.length > 0;
        if (value instanceof Map<?, ?> m) return !m.isEmpty();
        return true;
    }

    private static void throwIfArityMismatch(FieldGroup group, int valueCount) {
        if (group.size() != valueCount) {
            throw new IllegalConfigurationException(
                    String.format(
                            "The 'FieldGroup' defines '%d' fields but '%d' values were provided.",
                            group.size(),
                            valueCount
                    )
            );
        }
    }

    private static MissingFieldException missingAtLeastOne(FieldGroup group) {
        return new MissingFieldException(
                String.format(
                        "At least one of the %s fields must be provided.",
                        orList(group.names())
                )
        );
    }

    private static MissingFieldException missingNoneOrAll(FieldGroup group) {
        return new MissingFieldException(
                String.format(
                        "The %s fields must all be provided together, or none at all.",
                        andList(group.names())
                )
        );
    }

    private static TooManyFieldsException tooManyFieldsOnlyOne(List<String> provided) {
        return new TooManyFieldsException(
                String.format(
                        "Only one of the %s fields may be provided.",
                        orList(provided)
                )
        );
    }

    private static TooManyFieldsException tooManyFieldsAtMostOne(List<String> provided) {
        return new TooManyFieldsException(
                String.format(
                        "At most one of the %s fields may be provided.",
                        orList(provided)
                )
        );
    }

    private static String orList(List<String> names) {
        if (names.size() == 2) {
            return "'" + names.get(0) + "' or '" + names.get(1) + "'";
        }

        var sb = new StringBuilder();

        for (int i = 0; i < names.size() - 1; i++) {
            sb.append("'").append(names.get(i)).append("', ");
        }

        sb.append("or '").append(names.get(names.size() - 1)).append("'");

        return sb.toString();
    }

    private static String andList(List<String> names) {
        if (names.size() == 2) {
            return "'" + names.get(0) + "' and '" + names.get(1) + "'";
        }

        var sb = new StringBuilder();

        for (int i = 0; i < names.size() - 1; i++) {
            sb.append("'").append(names.get(i)).append("', ");
        }

        sb.append("and '").append(names.get(names.size() - 1)).append("'");

        return sb.toString();
    }
}
