package software.frisby.core.validation;

import java.time.LocalTime;

/**
 * Static utility methods for validating {@link LocalTime} argument values.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>All bound parameters ({@code min}, {@code max}) must not be null; passing a null
 *       bound throws {@link NullPointerException}.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.openTime = LocalTimes.min("openTime", openTime, EARLIEST_OPEN);</pre></li>
 *   <li>Range methods validate that bounds are consistent before checking the value.
 *       An invalid bound configuration throws {@link IllegalConfigurationException}.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}, or
 *       null bound) and {@link IllegalConfigurationException} (invalid bound configuration)
 *       signal that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null value) and
 *       {@link LocalTimeOutsideRangeException} (value outside the specified constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Only the range constraint family is provided. Clock-relative constraints
 * ({@code past}, {@code future}, etc.) are not offered because a {@link LocalTime} value
 * represents a time-of-day without any date context; "past" and "future" have no
 * well-defined meaning for such a value.
 *
 * <p>Eight constraint methods are provided:
 * <ul>
 *   <li>{@code min}               — value &gt;= min (inclusive lower bound)</li>
 *   <li>{@code max}               — value &lt;= max (inclusive upper bound)</li>
 *   <li>{@code minExclusive}      — value &gt; min (exclusive lower bound)</li>
 *   <li>{@code maxExclusive}      — value &lt; max (exclusive upper bound)</li>
 *   <li>{@code range}             — min &lt;= value &lt;= max (fully inclusive)</li>
 *   <li>{@code exclusiveRange}    — min &lt; value &lt; max (fully exclusive)</li>
 *   <li>{@code rangeExclusiveMax} — min &lt;= value &lt; max</li>
 *   <li>{@code rangeExclusiveMin} — min &lt; value &lt;= max</li>
 * </ul>
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see LocalDateTimes
 * @see OffsetTimes
 */
public final class LocalTimes {
    private static final String MIN = "min";
    private static final String MAX = "max";

    private LocalTimes() {
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static LocalTime notNull(String name, LocalTime value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or after {@code min} (inclusive).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is before {@code min}.
     */
    public static LocalTime min(String name, LocalTime value, LocalTime min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) throw nullValue(name);
        if (value.isBefore(min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or before {@code max} (inclusive).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is after {@code max}.
     */
    public static LocalTime max(String name, LocalTime value, LocalTime max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) throw nullValue(name);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly after {@code min} (exclusive).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static LocalTime minExclusive(String name, LocalTime value, LocalTime min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly before {@code max} (exclusive).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static LocalTime maxExclusive(String name, LocalTime value, LocalTime max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) throw nullValue(name);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}],
     * inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is before {@code min}.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static LocalTime range(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        Throws.ifNull(MAX, max);
        if (max.isBefore(min)) throw maxLtMin(max, min);

        if (null == value) throw nullValue(name);
        if (value.isBefore(min)) throw tooSmall(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}),
     * exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is not strictly after {@code min}.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static LocalTime exclusiveRange(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}),
     * inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is not strictly after {@code min}.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static LocalTime rangeExclusiveMax(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (value.isBefore(min)) throw tooSmall(name, min);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}],
     * exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is not strictly after {@code min}.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static LocalTime rangeExclusiveMin(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or after {@code min} (inclusive).
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is before {@code min}.
     */
    public static LocalTime optionalMin(String name, LocalTime value, LocalTime min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) return null;
        if (value.isBefore(min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or before {@code max} (inclusive).
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code max} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is after {@code max}.
     */
    public static LocalTime optionalMax(String name, LocalTime value, LocalTime max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) return null;
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly after {@code min} (exclusive).
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static LocalTime optionalMinExclusive(String name, LocalTime value, LocalTime min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly before {@code max} (exclusive).
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code max} is null.
     * @throws LocalTimeOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static LocalTime optionalMaxExclusive(String name, LocalTime value, LocalTime max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) return null;
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}],
     * inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is before {@code min}.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static LocalTime optionalRange(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        Throws.ifNull(MAX, max);
        if (max.isBefore(min)) throw maxLtMin(max, min);

        if (null == value) return null;
        if (value.isBefore(min)) throw tooSmall(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}),
     * exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is not strictly after {@code min}.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static LocalTime optionalExclusiveRange(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}),
     * inclusive minimum, exclusive maximum. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is not strictly after {@code min}.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static LocalTime optionalRangeExclusiveMax(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (value.isBefore(min)) throw tooSmall(name, min);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}],
     * exclusive minimum, inclusive maximum. A null {@code value} is considered valid and
     * returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException  if {@code max} is not strictly after {@code min}.
     * @throws LocalTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static LocalTime optionalRangeExclusiveMin(String name, LocalTime value, LocalTime min, LocalTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    private static void throwIfExclusiveBoundsInvalid(LocalTime min, LocalTime max) {
        Throws.ifNull(MIN, min);
        Throws.ifNull(MAX, max);
        if (!max.isAfter(min)) throw maxLeMin(max, min);
    }

    private static NullValueException nullValue(String name) {
        return new NullValueException(
                String.format(
                        "The '%s' value is invalid. The value must not be null.",
                        name
                )
        );
    }

    private static LocalTimeOutsideRangeException tooSmall(String name, LocalTime min) {
        return new LocalTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static LocalTimeOutsideRangeException tooSmallExclusive(String name, LocalTime bound) {
        return new LocalTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static LocalTimeOutsideRangeException tooLarge(String name, LocalTime max) {
        return new LocalTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static LocalTimeOutsideRangeException tooLargeExclusive(String name, LocalTime bound) {
        return new LocalTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(LocalTime max, LocalTime min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(LocalTime max, LocalTime min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}

