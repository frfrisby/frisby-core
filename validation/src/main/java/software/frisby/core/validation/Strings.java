package software.frisby.core.validation;

import java.util.regex.Pattern;

/**
 * Static utility methods for validating {@link String} argument values.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.email = Strings.notBlankWithMaxLength("email", email, 254);</pre></li>
 *   <li>Length constraints are measured in Unicode code points, not Java {@code char}
 *       values or UTF-8 bytes, ensuring correct behavior with supplementary characters
 *       such as emoji.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name},
 *       or null {@code pattern}) and {@link IllegalConfigurationException} (invalid
 *       configuration argument such as {@code maxLength &lt; 1}) signal that the calling
 *       code is incorrect.</li>
 *   <li><b>Value failures</b> — subtypes of {@link IllegalArgumentException}
 *       ({@link NullValueException}, {@link EmptyValueException}, {@link BlankValueException},
 *       {@link StringLengthOutsideRangeException}, {@link PatternMismatchException}) signal
 *       that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * @see StringSequences
 * @see TrimmedStrings
 */
public final class Strings {
    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_LENGTH = "minLength";
    private static final String LENGTH = "length";
    private static final String PATTERN = "pattern";

    private Strings() {
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static String notNull(String name, String value) {
        Throws.ifInvalidName(name);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null and not empty.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} if it is not null and not empty.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     * @throws EmptyValueException  if {@code value} is empty.
     */
    public static String notEmpty(String name, String value) {
        notNull(name, value);

        if (value.isEmpty()) {
            throw new EmptyValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be empty.",
                            name
                    )
            );
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null and not blank (i.e., contains at least one
     * non-whitespace character).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} if it is not null and not blank.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     * @throws BlankValueException  if {@code value} is blank.
     */
    public static String notBlank(String name, String value) {
        notNull(name, value);

        if (value.isBlank()) {
            throw new BlankValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not contain only whitespace characters.",
                            name
                    )
            );
        }

        return value;
    }

    /**
     * Validates that {@code value} is not null and matches {@code pattern} in its entirety.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if it is not null and matches {@code pattern}.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws PatternMismatchException if {@code value} does not match {@code pattern}.
     */
    public static String notNullWithMatches(String name, String value, Pattern pattern) {
        notNull(name, value);
        Throws.ifNull(PATTERN, pattern);

        return patternMatches(name, value, pattern);
    }

    /**
     * Validates that {@code value} is not null, not empty, and matches {@code pattern} in
     * its entirety.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if it is not null, not empty, and matches {@code pattern}.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws EmptyValueException      if {@code value} is empty.
     * @throws PatternMismatchException if {@code value} does not match {@code pattern}.
     */
    public static String notEmptyWithMatches(String name, String value, Pattern pattern) {
        notEmpty(name, value);
        Throws.ifNull(PATTERN, pattern);

        return patternMatches(name, value, pattern);
    }

    /**
     * Validates that {@code value} is not null, not blank, and matches {@code pattern} in
     * its entirety.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate.
     * @param pattern The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if it is not null, not blank, and matches {@code pattern}.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws BlankValueException      if {@code value} is blank.
     * @throws PatternMismatchException if {@code value} does not match {@code pattern}.
     */
    public static String notBlankWithMatches(String name, String value, Pattern pattern) {
        notBlank(name, value);
        Throws.ifNull(PATTERN, pattern);

        return patternMatches(name, value, pattern);
    }

    /**
     * Validates that {@code value} is not null, does not exceed {@code maxLength} Unicode
     * code points, and matches {@code pattern} in its entirety.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if all constraints are satisfied.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if {@code value} does not match {@code pattern}.
     */
    public static String notNullWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        notNull(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        return patternMatches(name, isShorterThanOrEqual(name, value, maxLength), pattern);
    }

    /**
     * Validates that {@code value} is not null, not empty, does not exceed {@code maxLength}
     * Unicode code points, and matches {@code pattern} in its entirety.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if all constraints are satisfied.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws EmptyValueException               if {@code value} is empty.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if {@code value} does not match {@code pattern}.
     */
    public static String notEmptyWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        notEmpty(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        return patternMatches(name, isShorterThanOrEqual(name, value, maxLength), pattern);
    }

    /**
     * Validates that {@code value} is not null, not blank, does not exceed {@code maxLength}
     * Unicode code points, and matches {@code pattern} in its entirety.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if all constraints are satisfied.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if {@code value} is blank.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if {@code value} does not match {@code pattern}.
     */
    public static String notBlankWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        notBlank(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        return patternMatches(name, isShorterThanOrEqual(name, value, maxLength), pattern);
    }

    /**
     * Validates that {@code value} is not null and does not exceed {@code maxLength} Unicode
     * code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null and does not exceed {@code maxLength} code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String maxLength(String name, String value, int maxLength) {
        notNull(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        return isShorterThanOrEqual(name, value, maxLength);
    }

    /**
     * Validates that {@code value} is not null and contains at least {@code minLength} Unicode
     * code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null and meets the minimum length requirement.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws StringLengthOutsideRangeException if {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String minLength(String name, String value, int minLength) {
        notNull(name, value);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        return isLongerThanOrEqual(name, value, minLength);
    }

    /**
     * Validates that {@code value} is not null and its length in Unicode code points falls
     * within [{@code minLength}, {@code maxLength}], inclusive.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The {@code value} if it is not null and its length falls within the specified range.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws StringLengthOutsideRangeException if {@code value} length is outside the specified range.
     */
    public static String length(String name, String value, int minLength, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        return maxLength(
                name,
                minLength(name, value, minLength),
                maxLength
        );
    }

    /**
     * Validates that {@code value} is not null, not empty, and does not exceed
     * {@code maxLength} Unicode code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null, not empty, and does not exceed {@code maxLength} code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws EmptyValueException               if {@code value} is empty.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String notEmptyWithMaxLength(String name, String value, int maxLength) {
        notEmpty(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        return isShorterThanOrEqual(name, value, maxLength);
    }

    /**
     * Validates that {@code value} is not null, not empty, and contains at least
     * {@code minLength} Unicode code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null, not empty, and meets the minimum length requirement.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws EmptyValueException               if {@code value} is empty.
     * @throws StringLengthOutsideRangeException if {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String notEmptyWithMinLength(String name, String value, int minLength) {
        notEmpty(name, value);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        return isLongerThanOrEqual(name, value, minLength);
    }

    /**
     * Validates that {@code value} is not null, not empty, and its length in Unicode code
     * points falls within [{@code minLength}, {@code maxLength}], inclusive.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The {@code value} if it is not null, not empty, and its length falls within the specified range.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws EmptyValueException               if {@code value} is empty.
     * @throws StringLengthOutsideRangeException if {@code value} length is outside the specified range.
     */
    public static String notEmptyWithLength(String name, String value, int minLength, int maxLength) {
        notEmpty(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        return isShorterThanOrEqual(
                name,
                isLongerThanOrEqual(name, value, minLength),
                maxLength
        );
    }

    /**
     * Validates that {@code value} is not null, not blank, and does not exceed
     * {@code maxLength} Unicode code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null, not blank, and does not exceed {@code maxLength} code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if {@code value} is blank.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String notBlankWithMaxLength(String name, String value, int maxLength) {
        notBlank(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        return isShorterThanOrEqual(name, value, maxLength);
    }

    /**
     * Validates that {@code value} is not null, not blank, and contains at least
     * {@code minLength} Unicode code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null, not blank, and meets the minimum length requirement.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if {@code value} is blank.
     * @throws StringLengthOutsideRangeException if {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String notBlankWithMinLength(String name, String value, int minLength) {
        notBlank(name, value);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        return isLongerThanOrEqual(name, value, minLength);
    }

    /**
     * Validates that {@code value} is not null, not blank, and its length in Unicode code
     * points falls within [{@code minLength}, {@code maxLength}], inclusive.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The {@code value} if it is not null, not blank, and its length falls within the specified range.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if {@code value} is blank.
     * @throws StringLengthOutsideRangeException if {@code value} length is outside the specified range.
     */
    public static String notBlankWithLength(String name, String value, int minLength, int maxLength) {
        notBlank(name, value);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        return isShorterThanOrEqual(
                name,
                isLongerThanOrEqual(name, value, minLength),
                maxLength
        );
    }

    /**
     * Validates that {@code value} is not null and contains exactly {@code length} Unicode
     * code points.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null and contains exactly {@code length} Unicode code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws StringLengthOutsideRangeException if {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String length(String name, String value, int length) {
        notNull(name, value);
        Throws.ifLessThanOne(LENGTH, length);

        return isExactLength(name, value, length);
    }

    /**
     * Validates that {@code value} is not null, not empty, and contains exactly {@code length}
     * Unicode code points.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null, not empty, and contains exactly {@code length} Unicode code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws EmptyValueException               if {@code value} is empty.
     * @throws StringLengthOutsideRangeException if {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String notEmptyWithLength(String name, String value, int length) {
        notEmpty(name, value);
        Throws.ifLessThanOne(LENGTH, length);

        return isExactLength(name, value, length);
    }

    /**
     * Validates that {@code value} is not null, not blank, and contains exactly {@code length}
     * Unicode code points.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not null, not blank, and contains exactly {@code length} Unicode code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if {@code value} is blank.
     * @throws StringLengthOutsideRangeException if {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String notBlankWithLength(String name, String value, int length) {
        notBlank(name, value);
        Throws.ifLessThanOne(LENGTH, length);

        return isExactLength(name, value, length);
    }

    /**
     * Validates that {@code value}, if present, is not empty. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} if it is not empty, or {@code null} if {@code value} is null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws EmptyValueException  if {@code value} is non-null and empty.
     */
    public static String optionalNotEmpty(String name, String value) {
        Throws.ifInvalidName(name);

        if (null == value) {
            return null;
        }

        return notEmpty(name, value);
    }

    /**
     * Validates that {@code value}, if present, is not blank. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} if it is not blank, or {@code null} if {@code value} is null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws BlankValueException  if {@code value} is non-null and blank.
     */
    public static String optionalNotBlank(String name, String value) {
        Throws.ifInvalidName(name);

        if (null == value) {
            return null;
        }

        return notBlank(name, value);
    }

    /**
     * Validates that {@code value}, if present, matches {@code pattern} in its entirety.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if it matches {@code pattern}, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws PatternMismatchException if {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalMatches(String name, String value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return notNullWithMatches(name, value, pattern);
    }

    /**
     * Validates that {@code value}, if present, is not empty and matches {@code pattern} in
     * its entirety. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if it is not empty and matches {@code pattern}, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws EmptyValueException      if {@code value} is non-null and empty.
     * @throws PatternMismatchException if {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalNotEmptyWithMatches(String name, String value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return notEmptyWithMatches(name, value, pattern);
    }

    /**
     * Validates that {@code value}, if present, is not blank and matches {@code pattern} in
     * its entirety. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to validate, may be null.
     * @param pattern The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if it is not blank and matches {@code pattern}, or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws BlankValueException      if {@code value} is non-null and blank.
     * @throws PatternMismatchException if {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalNotBlankWithMatches(String name, String value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return notBlankWithMatches(name, value, pattern);
    }

    /**
     * Validates that {@code value}, if present, does not exceed {@code maxLength} Unicode
     * code points and matches {@code pattern} in its entirety. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if all constraints are satisfied, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return notNullWithMaxLengthAndMatches(name, value, maxLength, pattern);
    }

    /**
     * Validates that {@code value}, if present, is not empty, does not exceed {@code maxLength}
     * Unicode code points, and matches {@code pattern} in its entirety. A null {@code value}
     * is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if all constraints are satisfied, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws EmptyValueException               if {@code value} is non-null and empty.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalNotEmptyWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return notEmptyWithMaxLengthAndMatches(name, value, maxLength, pattern);
    }

    /**
     * Validates that {@code value}, if present, is not blank, does not exceed {@code maxLength}
     * Unicode code points, and matches {@code pattern} in its entirety. A null {@code value}
     * is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the value must match in its entirety.
     * @return The {@code value} if all constraints are satisfied, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws BlankValueException               if {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalNotBlankWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return notBlankWithMaxLengthAndMatches(name, value, maxLength, pattern);
    }

    /**
     * Validates that {@code value}, if present, does not exceed {@code maxLength} Unicode
     * code points. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it does not exceed {@code maxLength} code points, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String optionalMaxLength(String name, String value, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        if (null == value) {
            return null;
        }

        return isShorterThanOrEqual(name, value, maxLength);
    }

    /**
     * Validates that {@code value}, if present, is not empty and does not exceed
     * {@code maxLength} Unicode code points. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not empty and does not exceed {@code maxLength} code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws EmptyValueException               if {@code value} is non-null and empty.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String optionalNotEmptyWithMaxLength(String name, String value, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        if (null == value) {
            return null;
        }

        return notEmptyWithMaxLength(name, value, maxLength);
    }

    /**
     * Validates that {@code value}, if present, is not blank and does not exceed
     * {@code maxLength} Unicode code points. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not blank and does not exceed {@code maxLength} code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws BlankValueException               if {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String optionalNotBlankWithMaxLength(String name, String value, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        if (null == value) {
            return null;
        }

        return notBlankWithMaxLength(name, value, maxLength);
    }

    /**
     * Validates that {@code value}, if present, contains at least {@code minLength} Unicode
     * code points. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it meets the minimum length requirement, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws StringLengthOutsideRangeException if {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String optionalMinLength(String name, String value, int minLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return isLongerThanOrEqual(name, value, minLength);
    }

    /**
     * Validates that {@code value}, if present, is not empty and contains at least
     * {@code minLength} Unicode code points. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not empty and meets the minimum length requirement,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws EmptyValueException               if {@code value} is non-null and empty.
     * @throws StringLengthOutsideRangeException if {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String optionalNotEmptyWithMinLength(String name, String value, int minLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return notEmptyWithMinLength(name, value, minLength);
    }

    /**
     * Validates that {@code value}, if present, is not blank and contains at least
     * {@code minLength} Unicode code points. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not blank and meets the minimum length requirement,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws BlankValueException               if {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String optionalNotBlankWithMinLength(String name, String value, int minLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return notBlankWithMinLength(name, value, minLength);
    }

    /**
     * Validates that {@code value}, if present, has a length in Unicode code points within
     * [{@code minLength}, {@code maxLength}], inclusive. A null {@code value} is considered
     * valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The {@code value} if its length falls within the specified range, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws StringLengthOutsideRangeException if {@code value} length is outside the specified range.
     */
    public static String optionalLength(String name, String value, int minLength, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return length(name, value, minLength, maxLength);
    }

    /**
     * Validates that {@code value}, if present, is not empty and has a length in Unicode code
     * points within [{@code minLength}, {@code maxLength}], inclusive. A null {@code value}
     * is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The {@code value} if it is not empty and its length falls within the specified range,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws EmptyValueException               if {@code value} is non-null and empty.
     * @throws StringLengthOutsideRangeException if {@code value} length is outside the specified range.
     */
    public static String optionalNotEmptyWithLength(String name, String value, int minLength, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return notEmptyWithLength(name, value, minLength, maxLength);
    }

    /**
     * Validates that {@code value}, if present, is not blank and has a length in Unicode code
     * points within [{@code minLength}, {@code maxLength}], inclusive. A null {@code value}
     * is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The {@code value} if it is not blank and its length falls within the specified range,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws BlankValueException               if {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if {@code value} length is outside the specified range.
     */
    public static String optionalNotBlankWithLength(String name, String value, int minLength, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return notBlankWithLength(name, value, minLength, maxLength);
    }

    /**
     * Validates that {@code value}, if present, contains exactly {@code length} Unicode code
     * points. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it contains exactly {@code length} Unicode code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws StringLengthOutsideRangeException if {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String optionalLength(String name, String value, int length) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(LENGTH, length);

        if (null == value) {
            return null;
        }

        return isExactLength(name, value, length);
    }

    /**
     * Validates that {@code value}, if present, is not empty and contains exactly
     * {@code length} Unicode code points. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not empty and contains exactly {@code length} Unicode code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws EmptyValueException               if {@code value} is empty.
     * @throws StringLengthOutsideRangeException if {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String optionalNotEmptyWithLength(String name, String value, int length) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(LENGTH, length);

        if (null == value) {
            return null;
        }

        return notEmptyWithLength(name, value, length);
    }

    /**
     * Validates that {@code value}, if present, is not blank and contains exactly
     * {@code length} Unicode code points. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The {@code value} if it is not blank and contains exactly {@code length} Unicode code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if {@code value} is blank.
     * @throws StringLengthOutsideRangeException if {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String optionalNotBlankWithLength(String name, String value, int length) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(LENGTH, length);

        if (null == value) {
            return null;
        }

        return notBlankWithLength(name, value, length);
    }

    private static String patternMatches(String name, String value, Pattern pattern) {
        if (!pattern.matcher(value).matches()) {
            throw new PatternMismatchException(
                    String.format(
                            "The '%s' value is invalid. The value does not match the expected pattern.",
                            name
                    )
            );
        }

        return value;
    }

    private static String isShorterThanOrEqual(String name, String value, int maxLength) {
        if (value.codePointCount(0, value.length()) > maxLength) {
            throw new StringLengthOutsideRangeException(
                    String.format(
                            "The '%s' value is invalid. The value cannot exceed a maximum length of '%d' Unicode code points.",
                            name,
                            maxLength
                    )
            );
        }

        return value;
    }

    private static String isLongerThanOrEqual(String name, String value, int minLength) {
        if (value.codePointCount(0, value.length()) < minLength) {
            throw new StringLengthOutsideRangeException(
                    String.format(
                            "The '%s' value is invalid. The value must contain at least '%d' Unicode code points.",
                            name,
                            minLength
                    )
            );
        }

        return value;
    }

    private static String isExactLength(String name, String value, int length) {
        if (value.codePointCount(0, value.length()) != length) {
            throw new StringLengthOutsideRangeException(
                    String.format(
                            "The '%s' value is invalid. The value must be exactly '%d' Unicode code points.",
                            name,
                            length
                    )
            );
        }

        return value;
    }
}
