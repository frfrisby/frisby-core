package software.frisby.core.validation;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Static utility methods for validating {@link Collection} and array argument values where every
 * element is a {@link String}.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>Configuration arguments ({@code minItemLength}, {@code maxItemLength}) must be ≥ 1;
 *       passing a value less than 1 throws {@link IllegalConfigurationException}. For
 *       {@code length}, {@code maxItemLength} must additionally be ≥ {@code minItemLength}.
 *       The {@code pattern} argument must not be null.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.tags = StringSequences.notBlankWithMaxLength("tags", tags, 64);</pre>
 *       Collection overloads preserve the concrete type via a bounded type parameter.</li>
 * </ul>
 *
 * <p>Every method validates a collection baseline before applying its element-level
 * constraint:
 * <ol>
 *   <li>Collection/array is not null → {@link NullValueException}</li>
 *   <li>Collection/array is not empty → {@link MissingElementsException}</li>
 *   <li>No null elements → {@link NullElementException}</li>
 * </ol>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}, or null
 *       {@code pattern}) and {@link IllegalConfigurationException} (invalid length bound
 *       configuration) signal that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException}, {@link MissingElementsException},
 *       {@link NullElementException}, {@link EmptyValueException}, {@link BlankValueException},
 *       {@link StringLengthOutsideRangeException}, and {@link PatternMismatchException} signal
 *       that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Six single-constraint families are provided:
 * <ul>
 *   <li>{@code notEmpty}   — each element is not empty</li>
 *   <li>{@code notBlank}   — each element is not blank</li>
 *   <li>{@code maxLength}  — each element ≤ maxItemLength Unicode code points</li>
 *   <li>{@code minLength}  — each element ≥ minItemLength Unicode code points</li>
 *   <li>{@code length}     — each element within [minItemLength, maxItemLength] code points</li>
 *   <li>{@code matches}    — each element matches a {@link Pattern} in its entirety</li>
 * </ul>
 *
 * <p>Five performance-optimized composite families are also provided; each checks multiple
 * per-element constraints in a single iteration of the collection:
 * <ul>
 *   <li>{@code notBlankWithMatches}            — not blank + matches pattern</li>
 *   <li>{@code notBlankWithMinLength}           — not blank + ≥ minItemLength code points</li>
 *   <li>{@code notBlankWithMaxLength}           — not blank + ≤ maxItemLength code points</li>
 *   <li>{@code notBlankWithLength}              — not blank + within [min, max] code points</li>
 *   <li>{@code notBlankWithMaxLengthAndMatches} — not blank + ≤ maxItemLength + matches pattern</li>
 * </ul>
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * <p>For structural constraints (size, uniqueness) use {@link Sequences} and compose at the
 * call site:
 * <pre>
 * this.tags = Sequences.noDuplicates("tags", Sequences.maxSize("tags", tags, 128));
 * StringSequences.notBlankWithMaxLengthAndMatches("tags", this.tags, 64, PATTERN);
 * </pre>
 *
 * @see Sequences
 * @see Strings
 * @see TrimmedStrings
 */
public final class StringSequences {
    private static final String MIN_ITEM_LENGTH = "minItemLength";
    private static final String MAX_ITEM_LENGTH = "maxItemLength";
    private static final String PATTERN = "pattern";

    private StringSequences() {
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no empty string elements.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws EmptyValueException      if {@code value} contains any empty string element.
     */
    public static <C extends Collection<String>> C notEmpty(String name, C value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isEmpty()) throw emptyElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no empty string elements.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws EmptyValueException      if {@code value} contains any empty string element.
     */
    public static String[] notEmpty(String name, String[] value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isEmpty()) throw emptyElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no empty string elements. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws EmptyValueException      if {@code value} contains any empty string element.
     */
    public static <C extends Collection<String>> C optionalNotEmpty(String name, C value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isEmpty()) throw emptyElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no empty string elements. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws EmptyValueException      if {@code value} contains any empty string element.
     */
    public static String[] optionalNotEmpty(String name, String[] value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isEmpty()) throw emptyElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no blank string elements.
     *
     * <p>A blank element is one for which {@link String#isBlank()} returns {@code true};
     * this includes empty strings and strings containing only whitespace characters.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if {@code value} contains any blank string element.
     */
    public static <C extends Collection<String>> C notBlank(String name, C value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no blank string elements.
     *
     * <p>A blank element is one for which {@link String#isBlank()} returns {@code true};
     * this includes empty strings and strings containing only whitespace characters.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if {@code value} contains any blank string element.
     */
    public static String[] notBlank(String name, String[] value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no blank string elements. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param <C>   The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if {@code value} contains any blank string element.
     */
    public static <C extends Collection<String>> C optionalNotBlank(String name, C value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no blank string elements. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if {@code value} contains any blank string element.
     */
    public static String[] optionalNotBlank(String name, String[] value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements whose length exceeds {@code maxItemLength} Unicode code points.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static <C extends Collection<String>> C maxLength(String name, C value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements whose length exceeds {@code maxItemLength} Unicode code points.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static String[] maxLength(String name, String[] value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements whose length exceeds {@code maxItemLength} Unicode code points.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static <C extends Collection<String>> C optionalMaxLength(String name, C value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements whose length exceeds {@code maxItemLength} Unicode code points.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static String[] optionalMaxLength(String name, String[] value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements whose length is less than {@code minItemLength} Unicode code points.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static <C extends Collection<String>> C minLength(String name, C value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements whose length is less than {@code minItemLength} Unicode code points.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static String[] minLength(String name, String[] value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements whose length is less than {@code minItemLength} Unicode code points.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static <C extends Collection<String>> C optionalMinLength(String name, C value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements whose length is less than {@code minItemLength} Unicode code points.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static String[] optionalMinLength(String name, String[] value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements whose length falls outside [{@code minItemLength}, {@code maxItemLength}]
     * Unicode code points, inclusive.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static <C extends Collection<String>> C length(String name, C value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements whose length falls outside [{@code minItemLength}, {@code maxItemLength}]
     * Unicode code points, inclusive.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static String[] length(String name, String[] value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements whose length falls outside [{@code minItemLength}, {@code maxItemLength}].
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static <C extends Collection<String>> C optionalLength(String name, C value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements whose length falls outside [{@code minItemLength}, {@code maxItemLength}].
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static String[] optionalLength(String name, String[] value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that fail to match {@code pattern} in their entirety.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static <C extends Collection<String>> C matches(String name, C value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that fail to match {@code pattern} in their entirety.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static String[] matches(String name, String[] value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that fail to match {@code pattern}. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static <C extends Collection<String>> C optionalMatches(String name, C value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that fail to match {@code pattern}. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static String[] optionalMatches(String name, String[] value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or fail to match {@code pattern}.
     * Blank and pattern checks are performed in a single iteration.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if any element is blank.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static <C extends Collection<String>> C notBlankWithMatches(String name, C value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or fail to match {@code pattern}.
     * Blank and pattern checks are performed in a single iteration.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if any element is blank.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static String[] notBlankWithMatches(String name, String[] value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or fail to match {@code pattern}. A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @param <C>     The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if any element is blank.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static <C extends Collection<String>> C optionalNotBlankWithMatches(String name, C value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or fail to match {@code pattern}. A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The pattern each element must match in its entirety; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws MissingElementsException if {@code value} is empty.
     * @throws NullElementException     if {@code value} contains any null element.
     * @throws BlankValueException      if any element is blank.
     * @throws PatternMismatchException if any element does not match {@code pattern}.
     */
    public static String[] optionalNotBlankWithMatches(String name, String[] value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or have fewer than {@code minItemLength} Unicode
     * code points. Blank and length checks are performed in a single iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static <C extends Collection<String>> C notBlankWithMinLength(String name, C value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or have fewer than {@code minItemLength} Unicode
     * code points. Blank and length checks are performed in a single iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static String[] notBlankWithMinLength(String name, String[] value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or have fewer than {@code minItemLength} Unicode
     * code points. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static <C extends Collection<String>> C optionalNotBlankWithMinLength(String name, C value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or have fewer than {@code minItemLength} Unicode
     * code points. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element has fewer than {@code minItemLength} code points.
     */
    public static String[] optionalNotBlankWithMinLength(String name, String[] value, int minItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or exceed {@code maxItemLength} Unicode code points.
     * Blank and length checks are performed in a single iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static <C extends Collection<String>> C notBlankWithMaxLength(String name, C value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or exceed {@code maxItemLength} Unicode code points.
     * Blank and length checks are performed in a single iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static String[] notBlankWithMaxLength(String name, String[] value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or exceed {@code maxItemLength} Unicode code points.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static <C extends Collection<String>> C optionalNotBlankWithMaxLength(String name, C value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or exceed {@code maxItemLength} Unicode code points.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     */
    public static String[] optionalNotBlankWithMaxLength(String name, String[] value, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or fall outside [{@code minItemLength},
     * {@code maxItemLength}] Unicode code points. All checks are performed in a single
     * iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static <C extends Collection<String>> C notBlankWithLength(String name, C value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank or fall outside [{@code minItemLength},
     * {@code maxItemLength}] Unicode code points. All checks are performed in a single
     * iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static String[] notBlankWithLength(String name, String[] value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or fall outside [{@code minItemLength},
     * {@code maxItemLength}] Unicode code points. A null {@code value} is considered valid
     * and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static <C extends Collection<String>> C optionalNotBlankWithLength(String name, C value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank or fall outside [{@code minItemLength},
     * {@code maxItemLength}] Unicode code points. A null {@code value} is considered valid
     * and returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param minItemLength The minimum number of Unicode code points required per element; must be ≥ 1.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ {@code minItemLength}.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minItemLength} is less than 1, or
     *                                           {@code maxItemLength} is less than {@code minItemLength}.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element's length falls outside the specified range.
     */
    public static String[] optionalNotBlankWithLength(String name, String[] value, int minItemLength, int maxItemLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_ITEM_LENGTH, minItemLength);
        Throws.ifLessThan(MAX_ITEM_LENGTH, maxItemLength, MIN_ITEM_LENGTH, minItemLength);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints < minItemLength) throw elementTooShort(name, minItemLength, index, codePoints);
            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank, exceed {@code maxItemLength} Unicode code points,
     * or fail to match {@code pattern}. All checks are performed in a single iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param pattern       The pattern each element must match in its entirety; must not be null.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     * @throws PatternMismatchException          if any element does not match {@code pattern}.
     */
    public static <C extends Collection<String>> C notBlankWithMaxLengthAndMatches(String name, C value, int maxItemLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) throw nullValue(name);
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null, not empty, contains no null elements, and
     * contains no elements that are blank, exceed {@code maxItemLength} Unicode code points,
     * or fail to match {@code pattern}. All checks are performed in a single iteration.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param pattern       The pattern each element must match in its entirety; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     * @throws PatternMismatchException          if any element does not match {@code pattern}.
     */
    public static String[] notBlankWithMaxLengthAndMatches(String name, String[] value, int maxItemLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) throw nullValue(name);
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank, exceed {@code maxItemLength} Unicode code points,
     * or fail to match {@code pattern}. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param pattern       The pattern each element must match in its entirety; must not be null.
     * @param <C>           The concrete collection type; preserved in the return value.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     * @throws PatternMismatchException          if any element does not match {@code pattern}.
     */
    public static <C extends Collection<String>> C optionalNotBlankWithMaxLengthAndMatches(String name, C value, int maxItemLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) return null;
        if (value.isEmpty()) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not empty, contains no null elements, and
     * contains no elements that are blank, exceed {@code maxItemLength} Unicode code points,
     * or fail to match {@code pattern}. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name          The name of the argument being validated, used in exception messages.
     * @param value         The value to validate, may be null.
     * @param maxItemLength The maximum number of Unicode code points allowed per element; must be ≥ 1.
     * @param pattern       The pattern each element must match in its entirety; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxItemLength} is less than 1.
     * @throws MissingElementsException          if {@code value} is empty.
     * @throws NullElementException              if {@code value} contains any null element.
     * @throws BlankValueException               if any element is blank.
     * @throws StringLengthOutsideRangeException if any element exceeds {@code maxItemLength} code points.
     * @throws PatternMismatchException          if any element does not match {@code pattern}.
     */
    public static String[] optionalNotBlankWithMaxLengthAndMatches(String name, String[] value, int maxItemLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_ITEM_LENGTH, maxItemLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) return null;
        if (value.length == 0) throw empty(name);

        int index = 0;

        for (String element : value) {
            if (null == element) throw nullElement(name);
            if (element.isBlank()) throw blankElement(name, index);

            int codePoints = element.codePointCount(0, element.length());

            if (codePoints > maxItemLength) throw elementTooLong(name, maxItemLength, index, codePoints);
            if (!pattern.matcher(element).matches()) throw elementFailsPattern(name, index, element);

            index++;
        }

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

    private static EmptyValueException emptyElement(String name, int index) {
        return new EmptyValueException(
                String.format(
                        "The '%s' value is invalid. The value must not contain empty elements. Element at index '%d' is empty.",
                        name,
                        index
                )
        );
    }

    private static BlankValueException blankElement(String name, int index) {
        return new BlankValueException(
                String.format(
                        "The '%s' value is invalid. The value must not contain blank elements. Element at index '%d' is blank.",
                        name,
                        index
                )
        );
    }

    private static StringLengthOutsideRangeException elementTooLong(String name, int maxItemLength, int index, int codePoints) {
        return new StringLengthOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must not contain elements exceeding '%d' Unicode code points but element at index '%d' contained '%d' Unicode code points.",
                        name,
                        maxItemLength,
                        index,
                        codePoints
                )
        );
    }

    private static StringLengthOutsideRangeException elementTooShort(String name, int minItemLength, int index, int codePoints) {
        return new StringLengthOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must not contain elements with fewer than '%d' Unicode code points but element at index '%d' contained '%d' Unicode code points.",
                        name,
                        minItemLength,
                        index,
                        codePoints
                )
        );
    }

    private static PatternMismatchException elementFailsPattern(String name, int index, String element) {
        return new PatternMismatchException(
                String.format(
                        "The '%s' value is invalid. The value must not contain elements that do not match the expected pattern. Element at index '%d' does not match: '%s'.",
                        name,
                        index,
                        element
                )
        );
    }
}
