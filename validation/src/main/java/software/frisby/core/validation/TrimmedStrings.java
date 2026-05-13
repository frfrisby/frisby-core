package software.frisby.core.validation;

import java.util.regex.Pattern;

/**
 * Static utility methods for validating {@link String} argument values after stripping
 * leading and trailing whitespace.
 *
 * <p>Each method strips {@code value} using {@link String#strip()} before performing any
 * content or length validation, and returns the stripped value. The returned value may
 * therefore differ from the original input, making these methods suitable for scenarios
 * where surrounding whitespace in user-supplied input should be silently normalized rather
 * than explicitly rejected.
 *
 * <p>Because {@link String#strip()} reduces a blank string to an empty string, the
 * distinction between blank and empty becomes meaningless after stripping. For this reason
 * only {@code notBlank}-based methods are provided; callers who need the stripped value to
 * have real content should use these methods without needing to choose between
 * {@code notEmpty} and {@code notBlank} variants.
 *
 * <p>All other contracts match {@link Strings}: the {@code name} parameter identifies the
 * argument being validated, lengths are measured in Unicode code points, and the same
 * two-category exception hierarchy applies ({@link NullPointerException} /
 * {@link IllegalConfigurationException} for API misuse; {@link IllegalArgumentException}
 * subtypes for value failures).
 *
 * @see Strings
 * @see StringSequences
 */
public final class TrimmedStrings {
    private static final String MAX_LENGTH = "maxLength";
    private static final String MIN_LENGTH = "minLength";
    private static final String LENGTH = "length";
    private static final String PATTERN = "pattern";

    private TrimmedStrings() {
    }

    /**
     * Strips {@code value} and validates that the result is not blank.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to strip and validate.
     * @return The stripped {@code value} if it is not blank.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     * @throws BlankValueException  if the stripped {@code value} is blank.
     */
    public static String notBlank(String name, String value) {
        Throws.ifInvalidName(name);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlank(name, value.strip());
    }

    /**
     * Strips {@code value} and validates that the result is not blank and does not exceed
     * {@code maxLength} Unicode code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The stripped {@code value} if it is not blank and does not exceed {@code maxLength} code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if the stripped {@code value} is blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String notBlankWithMaxLength(String name, String value, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlankWithMaxLength(name, value.strip(), maxLength);
    }

    /**
     * Strips {@code value} and validates that the result is not blank and contains at least
     * {@code minLength} Unicode code points.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The stripped {@code value} if it is not blank and meets the minimum length requirement.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if the stripped {@code value} is blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String notBlankWithMinLength(String name, String value, int minLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlankWithMinLength(name, value.strip(), minLength);
    }

    /**
     * Strips {@code value} and validates that the result is not blank and its length in
     * Unicode code points falls within [{@code minLength}, {@code maxLength}], inclusive.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The stripped {@code value} if it is not blank and its length falls within the specified range.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if the stripped {@code value} is blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} length is outside the specified range.
     */
    public static String notBlankWithLength(String name, String value, int minLength, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlankWithLength(name, value.strip(), minLength, maxLength);
    }

    /**
     * Strips {@code value} and validates that the result is not blank and contains exactly
     * {@code length} Unicode code points.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to strip and validate.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The stripped {@code value} if it is not blank and contains exactly {@code length} Unicode code points.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if the stripped {@code value} is blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String notBlankWithLength(String name, String value, int length) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(LENGTH, length);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlankWithLength(name, value.strip(), length);
    }

    /**
     * Strips {@code value} and validates that the result is not blank and matches
     * {@code pattern} in its entirety.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to strip and validate.
     * @param pattern The {@link Pattern} the stripped value must match in its entirety.
     * @return The stripped {@code value} if it is not blank and matches {@code pattern}.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws NullValueException       if {@code value} is null.
     * @throws BlankValueException      if the stripped {@code value} is blank.
     * @throws PatternMismatchException if the stripped {@code value} does not match {@code pattern}.
     */
    public static String notBlankWithMatches(String name, String value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlankWithMatches(name, value.strip(), pattern);
    }

    /**
     * Strips {@code value} and validates that the result is not blank, does not exceed
     * {@code maxLength} Unicode code points, and matches {@code pattern} in its entirety.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the stripped value must match in its entirety.
     * @return The stripped {@code value} if all constraints are satisfied.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws NullValueException                if {@code value} is null.
     * @throws BlankValueException               if the stripped {@code value} is blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if the stripped {@code value} does not match {@code pattern}.
     */
    public static String notBlankWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            throw new NullValueException(
                    String.format(
                            "The '%s' value is invalid. The value must not be null.",
                            name
                    )
            );
        }

        return Strings.notBlankWithMaxLengthAndMatches(name, value.strip(), maxLength, pattern);
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank. A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to strip and validate, may be null.
     * @return The stripped {@code value} if it is not blank, or {@code null} if {@code value} is null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws BlankValueException  if the stripped {@code value} is non-null and blank.
     */
    public static String optionalNotBlank(String name, String value) {
        Throws.ifInvalidName(name);

        if (null == value) {
            return null;
        }

        return Strings.notBlank(name, value.strip());
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank and does
     * not exceed {@code maxLength} Unicode code points. A null {@code value} is considered
     * valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @return The stripped {@code value} if it is not blank and does not exceed {@code maxLength} code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws BlankValueException               if the stripped {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} exceeds {@code maxLength} Unicode code points.
     */
    public static String optionalNotBlankWithMaxLength(String name, String value, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);

        if (null == value) {
            return null;
        }

        return Strings.notBlankWithMaxLength(name, value.strip(), maxLength);
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank and
     * contains at least {@code minLength} Unicode code points. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @return The stripped {@code value} if it is not blank and meets the minimum length requirement,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code minLength} is less than 1.
     * @throws BlankValueException               if the stripped {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} contains fewer than {@code minLength} Unicode code points.
     */
    public static String optionalNotBlankWithMinLength(String name, String value, int minLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return Strings.notBlankWithMinLength(name, value.strip(), minLength);
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank and has a
     * length in Unicode code points within [{@code minLength}, {@code maxLength}], inclusive.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate, may be null.
     * @param minLength The minimum required length in Unicode code points, must be &gt;= 1.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= {@code minLength}.
     * @return The stripped {@code value} if it is not blank and its length falls within the specified range,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code maxLength} or {@code minLength} is less than 1,
     *                                           or {@code maxLength} is less than {@code minLength}.
     * @throws BlankValueException               if the stripped {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} length is outside the specified range.
     */
    public static String optionalNotBlankWithLength(String name, String value, int minLength, int maxLength) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifLessThanOne(MIN_LENGTH, minLength);
        Throws.ifLessThan(MAX_LENGTH, maxLength, MIN_LENGTH, minLength);

        if (null == value) {
            return null;
        }

        return Strings.notBlankWithLength(name, value.strip(), minLength, maxLength);
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank and
     * contains exactly {@code length} Unicode code points. A null {@code value} is
     * considered valid and returned as {@code null}.
     *
     * @param name   The name of the argument being validated, used in exception messages.
     * @param value  The value to strip and validate, may be null.
     * @param length The exact required length in Unicode code points, must be &gt;= 1.
     * @return The stripped {@code value} if it is not blank and contains exactly {@code length} Unicode code points,
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code length} is less than 1.
     * @throws BlankValueException               if the stripped {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} does not contain exactly {@code length} Unicode code points.
     */
    public static String optionalNotBlankWithLength(String name, String value, int length) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(LENGTH, length);

        if (null == value) {
            return null;
        }

        return Strings.notBlankWithLength(name, value.strip(), length);
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank and
     * matches {@code pattern} in its entirety. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name    The name of the argument being validated, used in exception messages.
     * @param value   The value to strip and validate, may be null.
     * @param pattern The {@link Pattern} the stripped value must match in its entirety.
     * @return The stripped {@code value} if it is not blank and matches {@code pattern},
     * or {@code null} if {@code value} is null.
     * @throws NullPointerException     if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws BlankValueException      if the stripped {@code value} is non-null and blank.
     * @throws PatternMismatchException if the stripped {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalNotBlankWithMatches(String name, String value, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return Strings.notBlankWithMatches(name, value.strip(), pattern);
    }

    /**
     * Strips {@code value} and validates that the result, if present, is not blank, does not
     * exceed {@code maxLength} Unicode code points, and matches {@code pattern} in its
     * entirety. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to strip and validate, may be null.
     * @param maxLength The maximum permitted length in Unicode code points, must be &gt;= 1.
     * @param pattern   The {@link Pattern} the stripped value must match in its entirety.
     * @return The stripped {@code value} if all constraints are satisfied, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code pattern} is null.
     * @throws IllegalConfigurationException     if {@code maxLength} is less than 1.
     * @throws BlankValueException               if the stripped {@code value} is non-null and blank.
     * @throws StringLengthOutsideRangeException if the stripped {@code value} exceeds {@code maxLength} Unicode code points.
     * @throws PatternMismatchException          if the stripped {@code value} is non-null and does not match {@code pattern}.
     */
    public static String optionalNotBlankWithMaxLengthAndMatches(String name, String value, int maxLength, Pattern pattern) {
        Throws.ifInvalidName(name);
        Throws.ifLessThanOne(MAX_LENGTH, maxLength);
        Throws.ifNull(PATTERN, pattern);

        if (null == value) {
            return null;
        }

        return Strings.notBlankWithMaxLengthAndMatches(name, value.strip(), maxLength, pattern);
    }
}
