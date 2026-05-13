package software.frisby.core.validation;

import java.util.List;
import java.util.Set;

/**
 * Static utility methods for validating any reference-type argument value.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>Methods return the validated {@code value} unchanged, preserving its concrete
 *       type so no cast is required at the call site:
 *       <pre>this.status    = Values.notNull("status", status);
 * this.direction = Values.oneOf("direction", direction, Set.of("ASC", "DESC"));
 * this.role      = Values.notOneOf("role", role, Set.of(Role.UNKNOWN));</pre></li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name},
 *       or null {@code allowed} / {@code disallowed}) and {@link IllegalConfigurationException}
 *       (empty {@code allowed} / {@code disallowed} set) signal that the calling code is
 *       incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null value) and
 *       {@link DisallowedValueException} (value not in the permitted set, or value in the
 *       disallowed set) signal that the value being validated does not meet the required
 *       criteria.</li>
 * </ul>
 *
 * <p>Five constraint methods are provided:
 * <ul>
 *   <li>{@code notNull}           — value must not be null</li>
 *   <li>{@code oneOf}             — value must not be null and must be a member of the allowed set</li>
 *   <li>{@code optionalOneOf}     — if value is not null, it must be a member of the allowed set</li>
 *   <li>{@code notOneOf}          — value must not be null and must not be a member of the disallowed set</li>
 *   <li>{@code optionalNotOneOf}  — if value is not null, it must not be a member of the disallowed set</li>
 * </ul>
 *
 * <p>{@code notOneOf} is particularly useful for enum types that carry a sentinel value
 * (e.g. {@code UNKNOWN}) representing an unrecognized variant in a forward-compatible SDK.
 * Rather than enumerating every valid constant, callers can reject only the sentinel:
 * <pre>this.role = Values.notOneOf("role", role, Set.of(Role.UNKNOWN));</pre>
 *
 * @see Strings
 * @see Numbers
 */
public final class Values {
    private static final String ALLOWED = "allowed";
    private static final String DISALLOWED = "disallowed";

    private Values() {
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param <T>   The type of the value being validated.
     * @param name  The name of the argument being validated; used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static <T> T notNull(String name, T value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is a member of {@code allowed}.
     *
     * <p>The allowed-value list in the exception message is sorted lexicographically
     * by {@link String#valueOf(Object)} so that the message is deterministic regardless
     * of the iteration order of the supplied {@link Set}.
     *
     * @param <T>     The type of the value being validated.
     * @param name    The name of the argument being validated; used in exception messages.
     * @param value   The value to validate.
     * @param allowed The set of permitted values; must not be null and must not be empty.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code allowed} is null.
     * @throws IllegalConfigurationException if {@code allowed} is empty.
     * @throws NullValueException            if {@code value} is null.
     * @throws DisallowedValueException      if {@code value} is not a member of {@code allowed}.
     */
    public static <T> T oneOf(String name, T value, Set<T> allowed) {
        Throws.ifInvalidName(name);
        Throws.ifNull(ALLOWED, allowed);
        Throws.ifLessThanOne(ALLOWED, allowed.size());

        if (null == value) throw nullValue(name);

        if (!allowed.contains(value)) throw notInSet(name, value, allowed);

        return value;
    }

    /**
     * Validates that {@code value}, if not null, is a member of {@code allowed}.
     *
     * <p>A null value is passed through without validation. The allowed-value list in
     * the exception message is sorted lexicographically by {@link String#valueOf(Object)}
     * so that the message is deterministic regardless of the iteration order of the
     * supplied {@link Set}.
     *
     * @param <T>     The type of the value being validated.
     * @param name    The name of the argument being validated; used in exception messages.
     * @param value   The value to validate; may be null.
     * @param allowed The set of permitted values; must not be null and must not be empty.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code allowed} is null.
     * @throws IllegalConfigurationException if {@code allowed} is empty.
     * @throws DisallowedValueException      if {@code value} is not null and is not a member of {@code allowed}.
     */
    public static <T> T optionalOneOf(String name, T value, Set<T> allowed) {
        Throws.ifInvalidName(name);
        Throws.ifNull(ALLOWED, allowed);
        Throws.ifLessThanOne(ALLOWED, allowed.size());

        if (null == value) return null;

        if (!allowed.contains(value)) throw notInSet(name, value, allowed);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is not a member of {@code disallowed}.
     *
     * <p>This method is well-suited to enum types that include a sentinel variant (such as
     * {@code UNKNOWN}) representing an unrecognized value in a forward-compatible SDK.
     * Rejecting only the sentinel is more maintainable than enumerating every permitted
     * constant, since the allowed set grows as new variants are added while the disallowed
     * set remains stable.
     *
     * <p>The disallowed-value list in the exception message is sorted lexicographically
     * by {@link String#valueOf(Object)} so that the message is deterministic regardless
     * of the iteration order of the supplied {@link Set}.
     *
     * @param <T>        The type of the value being validated.
     * @param name       The name of the argument being validated; used in exception messages.
     * @param value      The value to validate.
     * @param disallowed The set of forbidden values; must not be null and must not be empty.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code disallowed} is null.
     * @throws IllegalConfigurationException if {@code disallowed} is empty.
     * @throws NullValueException            if {@code value} is null.
     * @throws DisallowedValueException      if {@code value} is a member of {@code disallowed}.
     */
    public static <T> T notOneOf(String name, T value, Set<T> disallowed) {
        Throws.ifInvalidName(name);
        Throws.ifNull(DISALLOWED, disallowed);
        Throws.ifLessThanOne(DISALLOWED, disallowed.size());

        if (null == value) throw nullValue(name);

        if (disallowed.contains(value)) throw inDisallowedSet(name, value, disallowed);

        return value;
    }

    /**
     * Validates that {@code value}, if not null, is not a member of {@code disallowed}.
     *
     * <p>A null value is passed through without validation. The disallowed-value list in
     * the exception message is sorted lexicographically by {@link String#valueOf(Object)}
     * so that the message is deterministic regardless of the iteration order of the
     * supplied {@link Set}.
     *
     * @param <T>        The type of the value being validated.
     * @param name       The name of the argument being validated; used in exception messages.
     * @param value      The value to validate; may be null.
     * @param disallowed The set of forbidden values; must not be null and must not be empty.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code disallowed} is null.
     * @throws IllegalConfigurationException if {@code disallowed} is empty.
     * @throws DisallowedValueException      if {@code value} is not null and is a member of {@code disallowed}.
     */
    public static <T> T optionalNotOneOf(String name, T value, Set<T> disallowed) {
        Throws.ifInvalidName(name);
        Throws.ifNull(DISALLOWED, disallowed);
        Throws.ifLessThanOne(DISALLOWED, disallowed.size());

        if (null == value) return null;

        if (disallowed.contains(value)) throw inDisallowedSet(name, value, disallowed);

        return value;
    }

    private static NullValueException nullValue(String name) {
        return new NullValueException(
                String.format(
                        "The '%s' value is invalid. The value must not be null.",
                        name
                )
        );
    }

    private static DisallowedValueException notInSet(String name, Object value, Set<?> allowed) {
        return new DisallowedValueException(
                String.format(
                        "The '%s' value of '%s' is invalid. The value must be one of: %s.",
                        name,
                        value,
                        sortedQuotedList(allowed)
                )
        );
    }

    private static DisallowedValueException inDisallowedSet(String name, Object value, Set<?> disallowed) {
        return new DisallowedValueException(
                String.format(
                        "The '%s' value of '%s' is invalid. The value must not be one of: %s.",
                        name,
                        value,
                        sortedQuotedList(disallowed)
                )
        );
    }

    private static String sortedQuotedList(Set<?> values) {
        List<String> sorted = values.stream()
                .map(String::valueOf)
                .sorted()
                .toList();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append("'").append(sorted.get(i)).append("'");
        }

        return sb.toString();
    }
}
