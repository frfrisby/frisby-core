package software.frisby.core.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Static utility methods for validating {@link Collection} and array argument values.
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
 *       <pre>this.tags = Sequences.notEmpty("tags", tags);</pre>
 *       Collection overloads preserve the concrete type via a bounded type parameter, so no
 *       cast is required at the call site.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}) and
 *       {@link IllegalConfigurationException} (invalid size bound configuration) signal that
 *       the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null collection/array),
 *       {@link MissingElementsException} (empty collection/array),
 *       {@link NullElementException} (null element within the collection/array),
 *       {@link SequenceSizeOutsideRangeException} (size outside the allowed range), and
 *       {@link DuplicateElementsException} (duplicate elements) signal that the value being
 *       validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Six constraint families are provided:
 * <ul>
 *   <li>{@code notNull}      — collection/array must not be null</li>
 *   <li>{@code notEmpty}     — not null, not empty, no null elements</li>
 *   <li>{@code minSize}      — {@code notEmpty} + size ≥ minSize</li>
 *   <li>{@code maxSize}      — {@code notEmpty} + size ≤ maxSize</li>
 *   <li>{@code size}         — {@code notEmpty} + minSize ≤ size ≤ maxSize</li>
 *   <li>{@code noDuplicates} — {@code notEmpty} + all elements unique</li>
 * </ul>
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see StringSequences
 */
public final class Sequences {
    private static final String MIN_SIZE = "minSize";
    private static final String MAX_SIZE = "maxSize";
    private static final String KEY_EXTRACTOR = "keyExtractor";

    private Sequences() {
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <T>   The element type.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static <T, C extends Collection<T>> C notNull(String name, C value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <T>   The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static <T> T[] notNull(String name, T[] value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, and contains no null elements.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <T>   The element type.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     */
    public static <T, C extends Collection<T>> C notEmpty(String name, C value) {
        Throws.ifInvalidName(name);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, and contains no null elements.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <T>   The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     */
    public static <T> T[] notEmpty(String name, T[] value) {
        Throws.ifInvalidName(name);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and has at
     * least {@code minSize} elements.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains fewer than {@code minSize} elements.
     */
    public static <T, C extends Collection<T>> C minSize(String name, C value, int minSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        if (value.size() < minSize) throw tooFew(name, minSize, value.size());

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and has at
     * least {@code minSize} elements.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains fewer than {@code minSize} elements.
     */
    public static <T> T[] minSize(String name, T[] value, int minSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        if (value.length < minSize) throw tooFew(name, minSize, value.length);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and has at
     * most {@code maxSize} elements.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxSize} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains more than {@code maxSize} elements.
     */
    public static <T, C extends Collection<T>> C maxSize(String name, C value, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_SIZE, maxSize);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and has at
     * most {@code maxSize} elements.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxSize} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains more than {@code maxSize} elements.
     */
    public static <T> T[] maxSize(String name, T[] value, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_SIZE, maxSize);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        if (value.length > maxSize) throw tooMany(name, maxSize, value.length);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and has a
     * number of elements within [{@code minSize}, {@code maxSize}], inclusive on both sides.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ {@code minSize}.
     * @param <T>     The element type.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1, or {@code maxSize} is
     *                                           less than {@code minSize}.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value}'s size falls outside [{@code minSize}, {@code maxSize}].
     */
    public static <T, C extends Collection<T>> C size(String name, C value, int minSize, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        Throws.ifLessThan(MAX_SIZE, maxSize, MIN_SIZE, minSize);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        if (value.size() < minSize) throw tooFew(name, minSize, value.size());
        if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and has a
     * number of elements within [{@code minSize}, {@code maxSize}], inclusive on both sides.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ {@code minSize}.
     * @param <T>     The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1, or {@code maxSize} is
     *                                           less than {@code minSize}.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value}'s size falls outside [{@code minSize}, {@code maxSize}].
     */
    public static <T> T[] size(String name, T[] value, int minSize, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        Throws.ifLessThan(MAX_SIZE, maxSize, MIN_SIZE, minSize);
        throwIfNullOrEmptyOrContainsNullElement(name, value);

        if (value.length < minSize) throw tooFew(name, minSize, value.length);
        if (value.length > maxSize) throw tooMany(name, maxSize, value.length);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and contains
     * no duplicate elements (determined via {@link Object#equals(Object)}).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <T>   The element type.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException       if {@code name} is null or blank.
     * @throws NullValueException         if {@code value} is null.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if {@code value} contains any duplicate element.
     */
    public static <T, C extends Collection<T>> C noDuplicates(String name, C value) {
        Throws.ifInvalidName(name);
        throwIfNullOrEmptyOrContainsNullElement(name, value);
        throwIfContainsDuplicates(name, value);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and contains
     * no duplicate elements (determined via {@link Object#equals(Object)}).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <T>   The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException       if {@code name} is null or blank.
     * @throws NullValueException         if {@code value} is null.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if {@code value} contains any duplicate element.
     */
    public static <T> T[] noDuplicates(String name, T[] value) {
        Throws.ifInvalidName(name);
        throwIfNullOrEmptyOrContainsNullElement(name, value);
        throwIfContainsDuplicates(name, value);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and contains
     * no duplicate keys as determined by applying {@code keyExtractor} to each element.
     *
     * <p>The duplicate check uses the extracted key's {@link Object#equals(Object)} and
     * {@link Object#hashCode()} methods. The first duplicate key found is included in the
     * exception message via {@link Object#toString()}.
     *
     * @param name         The name of the argument being validated, used in exception messages.
     * @param value        The value to validate.
     * @param keyExtractor A function that extracts the key to compare for uniqueness; must not be null.
     * @param <T>          The element type.
     * @param <C>          The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException       if {@code name} is null or blank, or if {@code keyExtractor} is null.
     * @throws NullValueException         if {@code value} is null.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if any two elements produce the same key under {@code keyExtractor}.
     */
    public static <T, C extends Collection<T>> C noDuplicates(String name, C value, Function<? super T, ?> keyExtractor) {
        Throws.ifInvalidName(name);
        Throws.ifNull(KEY_EXTRACTOR, keyExtractor);
        throwIfNullOrEmptyOrContainsNullElement(name, value);
        throwIfContainsDuplicateKeys(name, value, keyExtractor);

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and contains
     * no duplicate keys as determined by applying {@code keyExtractor} to each element.
     *
     * <p>The duplicate check uses the extracted key's {@link Object#equals(Object)} and
     * {@link Object#hashCode()} methods. The first duplicate key found is included in the
     * exception message via {@link Object#toString()}.
     *
     * @param name         The name of the argument being validated, used in exception messages.
     * @param value        The value to validate.
     * @param keyExtractor A function that extracts the key to compare for uniqueness; must not be null.
     * @param <T>          The element type.
     * @return The {@code value} unchanged.
     * @throws NullPointerException       if {@code name} is null or blank, or if {@code keyExtractor} is null.
     * @throws NullValueException         if {@code value} is null.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if any two elements produce the same key under {@code keyExtractor}.
     */
    public static <T> T[] noDuplicates(String name, T[] value, Function<? super T, ?> keyExtractor) {
        Throws.ifInvalidName(name);
        Throws.ifNull(KEY_EXTRACTOR, keyExtractor);
        throwIfNullOrEmptyOrContainsNullElement(name, value);
        throwIfContainsDuplicateKeys(name, value, keyExtractor);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty and contains no null elements.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <T>   The element type.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     */
    public static <T, C extends Collection<T>> C optionalNotEmpty(String name, C value) {
        Throws.ifInvalidName(name);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty and contains no null elements.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <T>   The element type.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     */
    public static <T> T[] optionalNotEmpty(String name, T[] value) {
        Throws.ifInvalidName(name);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and has at
     * least {@code minSize} elements. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains fewer than {@code minSize} elements.
     */
    public static <T, C extends Collection<T>> C optionalMinSize(String name, C value, int minSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);

            if (value.size() < minSize) throw tooFew(name, minSize, value.size());
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and has at
     * least {@code minSize} elements. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains fewer than {@code minSize} elements.
     */
    public static <T> T[] optionalMinSize(String name, T[] value, int minSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);

            if (value.length < minSize) throw tooFew(name, minSize, value.length);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and has at
     * most {@code maxSize} elements. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxSize} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains more than {@code maxSize} elements.
     */
    public static <T, C extends Collection<T>> C optionalMaxSize(String name, C value, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_SIZE, maxSize);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);

            if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and has at
     * most {@code maxSize} elements. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ 1.
     * @param <T>     The element type.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxSize} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value} contains more than {@code maxSize} elements.
     */
    public static <T> T[] optionalMaxSize(String name, T[] value, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_SIZE, maxSize);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);

            if (value.length > maxSize) throw tooMany(name, maxSize, value.length);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and has a
     * number of elements within [{@code minSize}, {@code maxSize}], inclusive on both sides.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ {@code minSize}.
     * @param <T>     The element type.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1, or {@code maxSize} is
     *                                           less than {@code minSize}.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value}'s size falls outside [{@code minSize}, {@code maxSize}].
     */
    public static <T, C extends Collection<T>> C optionalSize(String name, C value, int minSize, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        Throws.ifLessThan(MAX_SIZE, maxSize, MIN_SIZE, minSize);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);

            if (value.size() < minSize) throw tooFew(name, minSize, value.size());
            if (value.size() > maxSize) throw tooMany(name, maxSize, value.size());
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and has a
     * number of elements within [{@code minSize}, {@code maxSize}], inclusive on both sides.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param minSize The minimum number of elements allowed, inclusive; must be ≥ 1.
     * @param maxSize The maximum number of elements allowed, inclusive; must be ≥ {@code minSize}.
     * @param <T>     The element type.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minSize} is less than 1, or {@code maxSize} is
     *                                           less than {@code minSize}.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws SequenceSizeOutsideRangeException if {@code value}'s size falls outside [{@code minSize}, {@code maxSize}].
     */
    public static <T> T[] optionalSize(String name, T[] value, int minSize, int maxSize) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_SIZE, minSize);
        Throws.ifLessThan(MAX_SIZE, maxSize, MIN_SIZE, minSize);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);

            if (value.length < minSize) throw tooFew(name, minSize, value.length);
            if (value.length > maxSize) throw tooMany(name, maxSize, value.length);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no duplicate elements. A null {@code value} is considered valid and returned
     * as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <T>   The element type.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException       if {@code name} is null or blank.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if {@code value} contains any duplicate element.
     */
    public static <T, C extends Collection<T>> C optionalNoDuplicates(String name, C value) {
        Throws.ifInvalidName(name);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);
            throwIfContainsDuplicates(name, value);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no duplicate elements. A null {@code value} is considered valid and returned
     * as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <T>   The element type.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException       if {@code name} is null or blank.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if {@code value} contains any duplicate element.
     */
    public static <T> T[] optionalNoDuplicates(String name, T[] value) {
        Throws.ifInvalidName(name);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);
            throwIfContainsDuplicates(name, value);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no duplicate keys as determined by applying {@code keyExtractor} to each element.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name         The name of the argument being validated, used in exception messages.
     * @param value        The value to validate, may be null.
     * @param keyExtractor A function that extracts the key to compare for uniqueness; must not be null.
     * @param <T>          The element type.
     * @param <C>          The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException       if {@code name} is null or blank, or if {@code keyExtractor} is null.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if any two elements produce the same key under {@code keyExtractor}.
     */
    public static <T, C extends Collection<T>> C optionalNoDuplicates(String name, C value, Function<? super T, ?> keyExtractor) {
        Throws.ifInvalidName(name);
        Throws.ifNull(KEY_EXTRACTOR, keyExtractor);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);
            throwIfContainsDuplicateKeys(name, value, keyExtractor);
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no duplicate keys as determined by applying {@code keyExtractor} to each element.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name         The name of the argument being validated, used in exception messages.
     * @param value        The value to validate, may be null.
     * @param keyExtractor A function that extracts the key to compare for uniqueness; must not be null.
     * @param <T>          The element type.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException       if {@code name} is null or blank, or if {@code keyExtractor} is null.
     * @throws MissingElementsException   if {@code value} is empty.
     * @throws NullElementException       if {@code value} contains any null element.
     * @throws DuplicateElementsException if any two elements produce the same key under {@code keyExtractor}.
     */
    public static <T> T[] optionalNoDuplicates(String name, T[] value, Function<? super T, ?> keyExtractor) {
        Throws.ifInvalidName(name);
        Throws.ifNull(KEY_EXTRACTOR, keyExtractor);

        if (null != value) {
            throwIfEmptyOrContainsNullElement(name, value);
            throwIfContainsDuplicateKeys(name, value, keyExtractor);
        }

        return value;
    }

    private static <T> void throwIfEmptyOrContainsNullElement(String name, Collection<T> value) {
        if (value.isEmpty()) throw empty(name);

        for (T element : value) {
            if (null == element) throw nullElement(name);
        }
    }

    private static <T> void throwIfEmptyOrContainsNullElement(String name, T[] value) {
        if (value.length == 0) throw empty(name);

        for (T element : value) {
            if (null == element) throw nullElement(name);
        }
    }

    private static <T> void throwIfNullOrEmptyOrContainsNullElement(String name, Collection<T> value) {
        if (null == value) throw nullValue(name);

        throwIfEmptyOrContainsNullElement(name, value);
    }

    private static <T> void throwIfNullOrEmptyOrContainsNullElement(String name, T[] value) {
        if (null == value) throw nullValue(name);

        throwIfEmptyOrContainsNullElement(name, value);
    }

    private static <T> void throwIfContainsDuplicates(String name, Collection<T> value) {
        Set<Object> seen = new HashSet<>();

        for (T element : value) {
            if (!seen.add(element)) throw duplicateElement(name, element);
        }
    }

    private static <T> void throwIfContainsDuplicates(String name, T[] value) {
        Set<Object> seen = new HashSet<>();

        for (T element : value) {
            if (!seen.add(element)) throw duplicateElement(name, element);
        }
    }

    private static <T> void throwIfContainsDuplicateKeys(String name, Collection<T> value, Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();

        for (T element : value) {
            Object key = keyExtractor.apply(element);

            if (!seen.add(key)) throw duplicateElement(name, key);
        }
    }

    private static <T> void throwIfContainsDuplicateKeys(String name, T[] value, Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();

        for (T element : value) {
            Object key = keyExtractor.apply(element);

            if (!seen.add(key)) throw duplicateElement(name, key);
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

    private static NullElementException nullElement(String name) {
        return new NullElementException(
                String.format(
                        "The '%s' value is invalid. The value must not contain null elements.",
                        name
                )
        );
    }

    private static DuplicateElementsException duplicateElement(String name, Object duplicate) {
        return new DuplicateElementsException(
                String.format(
                        "The '%s' value is invalid. The value must not contain duplicate elements. Found duplicate: '%s'.",
                        name,
                        duplicate
                )
        );
    }

    private static SequenceSizeOutsideRangeException tooFew(String name, int minSize, int actualSize) {
        return new SequenceSizeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must contain at least '%d' elements but contained '%d'.",
                        name,
                        minSize,
                        actualSize
                )
        );
    }

    private static SequenceSizeOutsideRangeException tooMany(String name, int maxSize, int actualSize) {
        return new SequenceSizeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must not contain more than '%d' elements but contained '%d'.",
                        name,
                        maxSize,
                        actualSize
                )
        );
    }
}

