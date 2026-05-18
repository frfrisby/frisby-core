package software.frisby.core.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * An immutable, ordered list of field names used by {@link FieldGroups} to identify fields
 * in cardinality constraint exception messages.
 *
 * <p>{@code FieldGroup} instances carry no values and no "provided" state — they are pure
 * name-holders. Because a class's field names are fixed at compile time, instances should
 * always be declared {@code private static final} so that construction occurs once at class
 * initialization and never again:
 *
 * <pre>
 * private static final FieldGroup UPDATE_FIELDS = FieldGroup.of(
 *         "name",
 *         "email",
 *         "accessControl"
 * );
 *
 * // In the constructor — zero allocation at this call site:
 * FieldGroups.atLeastOne(UPDATE_FIELDS, name, email, accessControl);
 * </pre>
 *
 * <p>Two {@code FieldGroup} instances are equal if and only if they contain the same field
 * names in the same order. Order is significant because field names map positionally to the
 * value arguments passed to {@link FieldGroups} methods.
 *
 * @see FieldGroups
 */
public final class FieldGroup {
    private final List<String> names;

    private FieldGroup(List<String> names) {
        this.names = names;
    }

    /**
     * Creates a new {@code FieldGroup} containing the specified field names.
     *
     * <p>The group must contain at least two names. Each name must be non-null and
     * non-blank. No name may appear more than once.
     *
     * @param names The field names; must not be null, must contain at least two elements,
     *              and no element may be null, blank, or a duplicate.
     * @return A new {@code FieldGroup} containing the specified names in the order given.
     * @throws NullPointerException          if {@code names} is null, or if any element is null or blank.
     * @throws IllegalConfigurationException if fewer than two names are supplied, or if any name is duplicated.
     */
    public static FieldGroup of(String... names) {
        Throws.ifNull("names", names);

        if (names.length < 2) {
            throw new IllegalConfigurationException(
                    String.format(
                            "The 'names' value of '%d' is invalid. The value must be greater than or equal to '2'.",
                            names.length
                    )
            );
        }

        var seen = new HashSet<String>();

        for (var name : names) {
            Throws.ifInvalidName(name);

            if (!seen.add(name)) {
                throw new IllegalConfigurationException(
                        String.format(
                                "The 'names' value is invalid. The value must not contain duplicate names; '%s' appears more than once.",
                                name
                        )
                );
            }
        }

        return new FieldGroup(List.of(names));
    }

    /**
     * Returns the number of field names in this group.
     *
     * @return The number of field names; always two or more.
     */
    public int size() {
        return names.size();
    }

    /**
     * Returns the field name at the specified zero-based index.
     *
     * @param index The zero-based index of the field name to return.
     * @return The field name at the specified index.
     * @throws IndexOutOfBoundsException if {@code index} is out of range.
     */
    public String nameAt(int index) {
        return names.get(index);
    }

    /**
     * Returns an immutable list of all field names in this group, in the order they were
     * supplied to {@link #of(String...)}.
     *
     * @return An immutable list of field names.
     */
    public List<String> names() {
        return names;
    }

    /**
     * Returns {@code true} if {@code obj} is a {@code FieldGroup} containing the same field
     * names in the same order as this group.
     *
     * @param obj The object to compare with this group.
     * @return {@code true} if {@code obj} is equal to this group; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FieldGroup that)) return false;
        return Objects.equals(names, that.names);
    }

    /**
     * Returns a hash code consistent with the order-sensitive equality defined by
     * {@link #equals(Object)}.
     *
     * @return The hash code for this group.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(names);
    }

    /**
     * Returns a human-readable representation of this group, listing each field name
     * in declaration order. For example, {@code FieldGroup.of("start", "end", "limit")}
     * produces {@code "FieldGroup['start', 'end', 'limit']"}.
     *
     * @return A string representation of this group.
     */
    @Override
    public String toString() {
        var sb = new StringBuilder("FieldGroup[");

        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append("'").append(names.get(i)).append("'");
        }

        sb.append("]");

        return sb.toString();
    }
}

