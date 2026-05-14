package software.frisby.core.validation;

import java.util.Map;

/**
 * Static utility methods for validating {@link Map} argument values.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>Configuration arguments ({@code minSize}, {@code maxSize}) must be ≥ 1; passing a
 *       value less than 1 throws {@link IllegalConfigurationException}. For {@code size},
 *       {@code maxSize} must additionally be ≥ {@code minSize}.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.headers = Maps.notEmpty("headers", headers);</pre>
 *       The concrete map type is preserved via a bounded type parameter, so no cast is
 *       required at the call site.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}) and
 *       {@link IllegalConfigurationException} (invalid size bound configuration) signal that
 *       the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null map),
 *       {@link MissingElementsException} (empty map),
 *       {@link NullMapKeyException} (null key within the map),
 *       {@link NullMapValueException} (null value within the map), and
 *       {@link MapSizeOutsideRangeException} (entry count outside the allowed range) signal
 *       that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Five constraint families are provided:
 * <ul>
 *   <li>{@code notNull}  — map must not be null</li>
 *   <li>{@code notEmpty} — not null, not empty, no null keys, no null values</li>
 *   <li>{@code minSize}  — {@code notEmpty} + entry count ≥ minSize</li>
 *   <li>{@code maxSize}  — {@code notEmpty} + entry count ≤ maxSize</li>
 *   <li>{@code size}     — {@code notEmpty} + minSize ≤ entry count ≤ maxSize</li>
 * </ul>
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * <p>Key-content and value-content validation are handled by composing {@link Sequences} or
 * {@link StringSequences} with {@code map.keySet()} and {@code map.values()} at the call
 * site. For example:
 * <pre>
 * this.headers = Maps.maxSize("headers", headers, 100);
 * StringSequences.notBlank("headers.keys", headers.keySet());
 * StringSequences.notBlankWithMaxLength("headers.values", headers.values(), 256);
 * </pre>
 *
 * @see Sequences
 * @see StringSequences
 */
public final class Maps {
    private static final String MIN_SIZE = "minSize";
    private static final String MAX_SIZE = "maxSize";

    private Maps() {
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <K>   The key type.
     * @param <V>   The value type.
     * @param <M>   The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static <K, V, M extends Map<K, V>> M notNull(String name, M value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, and contains no null keys or null values.
     *
     * <p>The null-key and null-value check is performed in a single pass over the entry set.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <K>   The key type.
     * @param <V>   The value type.
     * @param <M>   The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullMapKeyException      if {@code value} contains any null key.
     * @throws NullMapValueException    if {@code value} contains any null value.
     */
    public static <K, V, M extends Map<K, V>> M notEmpty(String name, M value) {
        Throws.ifInvalidName(name);
        throwIfNullOrEmptyOrContainsNullKeyOrValue(name, value);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null keys or null values,
     * and has at least {@code minSize} entries.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param minSize The minimum number of entries allowed, inclusive; must be ≥ 1.
     * @param <K>     The key type.
     * @param <V>     The value type.
     * @param <M>     The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws IllegalConfigurationException if {@code minSize} is less than 1.
     * @throws NullValueException            if {@code value} is null.
     * @throws MissingElementsException      if {@code value} is empty.
     * @throws NullMapKeyException           if {@code value} contains any null key.
     * @throws NullMapValueException         if {@code value} contains any null value.
     * @throws MapSizeOutsideRangeException  if {@code value} contains fewer than {@code minSize} entries.
     */
    public static <K, V, M extends Map<K, V>> M minSize(String name, M value, int minSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        throwIfNullOrEmptyOrContainsNullKeyOrValue(name, value);

        if (value.size() < minSize) throw tooFew(name, minSize, value.size());

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null keys or null values,
     * and has at most {@code maxSize} entries.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param maxSize The maximum number of entries allowed, inclusive; must be ≥ 1.
     * @param <K>     The key type.
     * @param <V>     The value type.
     * @param <M>     The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws IllegalConfigurationException if {@code maxSize} is less than 1.
     * @throws NullValueException            if {@code value} is null.
     * @throws MissingElementsException      if {@code value} is empty.
     * @throws NullMapKeyException           if {@code value} contains any null key.
     * @throws NullMapValueException         if {@code value} contains any null value.
     * @throws MapSizeOutsideRangeException  if {@code value} contains more than {@code maxSize} entries.
     */
    public static <K, V, M extends Map<K, V>> M maxSize(String name, M value, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_SIZE, maxSize);
        throwIfNullOrEmptyOrContainsNullKeyOrValue(name, value);

        if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null keys or null values,
     * and has a number of entries within [{@code minSize}, {@code maxSize}], inclusive on both sides.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param minSize The minimum number of entries allowed, inclusive; must be ≥ 1.
     * @param maxSize The maximum number of entries allowed, inclusive; must be ≥ {@code minSize}.
     * @param <K>     The key type.
     * @param <V>     The value type.
     * @param <M>     The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws IllegalConfigurationException if {@code minSize} is less than 1, or {@code maxSize} is
     *                                       less than {@code minSize}.
     * @throws NullValueException            if {@code value} is null.
     * @throws MissingElementsException      if {@code value} is empty.
     * @throws NullMapKeyException           if {@code value} contains any null key.
     * @throws NullMapValueException         if {@code value} contains any null value.
     * @throws MapSizeOutsideRangeException  if {@code value}'s entry count falls outside
     *                                       [{@code minSize}, {@code maxSize}].
     */
    public static <K, V, M extends Map<K, V>> M size(String name, M value, int minSize, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        Throws.ifLessThan(MAX_SIZE, maxSize, MIN_SIZE, minSize);
        throwIfNullOrEmptyOrContainsNullKeyOrValue(name, value);

        if (value.size() < minSize) throw tooFew(name, minSize, value.size());
        if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty and contains no null keys or null
     * values. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <K>   The key type.
     * @param <V>   The value type.
     * @param <M>   The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullMapKeyException      if {@code value} contains any null key.
     * @throws NullMapValueException    if {@code value} contains any null value.
     */
    public static <K, V, M extends Map<K, V>> M optionalNotEmpty(String name, M value) {
        Throws.ifInvalidName(name);

        if (null != value) {
            throwIfEmptyOrContainsNullKeyOrValue(name, value);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null keys or null values,
     * and has at least {@code minSize} entries. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param minSize The minimum number of entries allowed, inclusive; must be ≥ 1.
     * @param <K>     The key type.
     * @param <V>     The value type.
     * @param <M>     The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws IllegalConfigurationException if {@code minSize} is less than 1.
     * @throws MissingElementsException      if {@code value} is empty.
     * @throws NullMapKeyException           if {@code value} contains any null key.
     * @throws NullMapValueException         if {@code value} contains any null value.
     * @throws MapSizeOutsideRangeException  if {@code value} contains fewer than {@code minSize} entries.
     */
    public static <K, V, M extends Map<K, V>> M optionalMinSize(String name, M value, int minSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);

        if (null != value) {
            throwIfEmptyOrContainsNullKeyOrValue(name, value);

            if (value.size() < minSize) throw tooFew(name, minSize, value.size());
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null keys or null values,
     * and has at most {@code maxSize} entries. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param maxSize The maximum number of entries allowed, inclusive; must be ≥ 1.
     * @param <K>     The key type.
     * @param <V>     The value type.
     * @param <M>     The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws IllegalConfigurationException if {@code maxSize} is less than 1.
     * @throws MissingElementsException      if {@code value} is empty.
     * @throws NullMapKeyException           if {@code value} contains any null key.
     * @throws NullMapValueException         if {@code value} contains any null value.
     * @throws MapSizeOutsideRangeException  if {@code value} contains more than {@code maxSize} entries.
     */
    public static <K, V, M extends Map<K, V>> M optionalMaxSize(String name, M value, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_SIZE, maxSize);

        if (null != value) {
            throwIfEmptyOrContainsNullKeyOrValue(name, value);

            if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null keys or null values,
     * and has a number of entries within [{@code minSize}, {@code maxSize}], inclusive on both sides.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param minSize The minimum number of entries allowed, inclusive; must be ≥ 1.
     * @param maxSize The maximum number of entries allowed, inclusive; must be ≥ {@code minSize}.
     * @param <K>     The key type.
     * @param <V>     The value type.
     * @param <M>     The concrete map type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws IllegalConfigurationException if {@code minSize} is less than 1, or {@code maxSize} is
     *                                       less than {@code minSize}.
     * @throws MissingElementsException      if {@code value} is empty.
     * @throws NullMapKeyException           if {@code value} contains any null key.
     * @throws NullMapValueException         if {@code value} contains any null value.
     * @throws MapSizeOutsideRangeException  if {@code value}'s entry count falls outside
     *                                       [{@code minSize}, {@code maxSize}].
     */
    public static <K, V, M extends Map<K, V>> M optionalSize(String name, M value, int minSize, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        Throws.ifLessThan(MAX_SIZE, maxSize, MIN_SIZE, minSize);

        if (null != value) {
            throwIfEmptyOrContainsNullKeyOrValue(name, value);

            if (value.size() < minSize) throw tooFew(name, minSize, value.size());
            if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());
        }

        return value;
    }

    private static <K, V> void throwIfNullOrEmptyOrContainsNullKeyOrValue(String name, Map<K, V> value) {
        if (null == value) throw nullValue(name);

        throwIfEmptyOrContainsNullKeyOrValue(name, value);
    }

    private static <K, V> void throwIfEmptyOrContainsNullKeyOrValue(String name, Map<K, V> value) {
        if (value.isEmpty()) throw empty(name);

        for (var entry : value.entrySet()) {
            if (null == entry.getKey()) throw nullKey(name);
            if (null == entry.getValue()) throw nullMapValue(name);
        }
    }

    private static NullValueException nullValue(String name) {
        return new NullValueException(
                String.format(
                        "The '%s' value is invalid. The value must not be null.",
                        name
                )
        );
    }

    private static MissingElementsException empty(String name) {
        return new MissingElementsException(
                String.format(
                        "The '%s' value is invalid. The value must not be empty.",
                        name
                )
        );
    }

    private static NullMapKeyException nullKey(String name) {
        return new NullMapKeyException(
                String.format(
                        "The '%s' value is invalid. The value must not contain null keys.",
                        name
                )
        );
    }

    private static NullMapValueException nullMapValue(String name) {
        return new NullMapValueException(
                String.format(
                        "The '%s' value is invalid. The value must not contain null values.",
                        name
                )
        );
    }

    private static MapSizeOutsideRangeException tooFew(String name, int minSize, int actualSize) {
        return new MapSizeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must contain at least '%d' entries but contained '%d'.",
                        name,
                        minSize,
                        actualSize
                )
        );
    }

    private static MapSizeOutsideRangeException tooMany(String name, int maxSize, int actualSize) {
        return new MapSizeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must not contain more than '%d' entries but contained '%d'.",
                        name,
                        maxSize,
                        actualSize
                )
        );
    }
}

