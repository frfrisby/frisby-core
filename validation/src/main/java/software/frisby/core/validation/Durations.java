package software.frisby.core.validation;

import java.time.Duration;

/**
 * Static utility methods for validating {@link Duration} argument values.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>All bound parameters ({@code min}, {@code max}) are reference types and must not
 *       be null; passing a null bound throws {@link NullPointerException}.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.timeout = Durations.positive("timeout", timeout);</pre></li>
 *   <li>Range methods validate that bounds are consistent before checking the value.
 *       An invalid bound configuration (e.g. {@code max} &lt; {@code min}) throws
 *       {@link IllegalConfigurationException}.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}, or
 *       null bound) and {@link IllegalConfigurationException} (invalid bound configuration)
 *       signal that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null value) and
 *       {@link DurationOutsideRangeException} (value outside the specified constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Ten constraint families are provided:
 * <ul>
 *   <li>{@code min}             — value &gt;= min (inclusive lower bound)</li>
 *   <li>{@code max}             — value &lt;= max (inclusive upper bound)</li>
 *   <li>{@code minExclusive}    — value &gt; min (exclusive lower bound)</li>
 *   <li>{@code maxExclusive}    — value &lt; max (exclusive upper bound)</li>
 *   <li>{@code range}           — min &lt;= value &lt;= max (fully inclusive)</li>
 *   <li>{@code exclusiveRange}  — min &lt; value &lt; max (fully exclusive)</li>
 *   <li>{@code rangeExclusiveMax} — min &lt;= value &lt; max (inclusive min, exclusive max)</li>
 *   <li>{@code rangeExclusiveMin} — min &lt; value &lt;= max (exclusive min, inclusive max)</li>
 *   <li>{@code positive}        — value &gt; {@link Duration#ZERO}</li>
 *   <li>{@code notNegative}     — value &gt;= {@link Duration#ZERO}</li>
 * </ul>
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see Numbers
 * @see Periods
 */
public final class Durations {
    private static final String MIN = "min";
    private static final String MAX = "max";

    private Durations() {
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
    public static Duration notNull(String name, Duration value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is less than {@code min}.
     */
    public static Duration min(String name, Duration value, Duration min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) throw nullValue(name);
        if (value.compareTo(min) < 0) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is greater than {@code max}.
     */
    public static Duration max(String name, Duration value, Duration max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) throw nullValue(name);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is less than or equal to {@code min}.
     */
    public static Duration minExclusive(String name, Duration value, Duration min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is greater than or equal to {@code max}.
     */
    public static Duration maxExclusive(String name, Duration value, Duration max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) throw nullValue(name);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is less than {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static Duration range(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        Throws.ifNull(MAX, max);
        if (max.compareTo(min) < 0) throw maxLtMin(max, min);

        if (null == value) throw nullValue(name);
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static Duration exclusiveRange(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static Duration rangeExclusiveMax(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static Duration rangeExclusiveMin(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@link Duration#ZERO}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is zero or negative.
     */
    public static Duration positive(String name, Duration value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.isNegative() || value.isZero()) throw notPositive(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@link Duration#ZERO}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws NullValueException            if {@code value} is null.
     * @throws DurationOutsideRangeException if {@code value} is negative.
     */
    public static Duration notNegative(String name, Duration value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.isNegative()) throw isNegative(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws DurationOutsideRangeException if {@code value} is less than {@code min}.
     */
    public static Duration optionalMin(String name, Duration value, Duration min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code max} is null.
     * @throws DurationOutsideRangeException if {@code value} is greater than {@code max}.
     */
    public static Duration optionalMax(String name, Duration value, Duration max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) return null;
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws DurationOutsideRangeException if {@code value} is less than or equal to {@code min}.
     */
    public static Duration optionalMinExclusive(String name, Duration value, Duration min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code max} is null.
     * @throws DurationOutsideRangeException if {@code value} is greater than or equal to {@code max}.
     */
    public static Duration optionalMaxExclusive(String name, Duration value, Duration max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);

        if (null == value) return null;
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is less than {@code min}.
     * @throws DurationOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static Duration optionalRange(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        Throws.ifNull(MAX, max);
        if (max.compareTo(min) < 0) throw maxLtMin(max, min);

        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly greater than {@code min}.
     * @throws DurationOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static Duration optionalExclusiveRange(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly greater than {@code min}.
     * @throws DurationOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static Duration optionalRangeExclusiveMax(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly greater than {@code min}.
     * @throws DurationOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static Duration optionalRangeExclusiveMin(String name, Duration value, Duration min, Duration max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@link Duration#ZERO}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws DurationOutsideRangeException if {@code value} is zero or negative.
     */
    public static Duration optionalPositive(String name, Duration value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.isNegative() || value.isZero()) throw notPositive(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@link Duration#ZERO}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank.
     * @throws DurationOutsideRangeException if {@code value} is negative.
     */
    public static Duration optionalNotNegative(String name, Duration value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.isNegative()) throw isNegative(name);

        return value;
    }

    private static void throwIfExclusiveBoundsInvalid(Duration min, Duration max) {
        Throws.ifNull(MIN, min);
        Throws.ifNull(MAX, max);
        if (max.compareTo(min) <= 0) throw maxLeMin(max, min);
    }

    private static NullValueException nullValue(String name) {
        return new NullValueException(
                String.format(
                        "The '%s' value is invalid. The value must not be null.",
                        name
                )
        );
    }

    private static DurationOutsideRangeException tooSmall(String name, Duration min) {
        return new DurationOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static DurationOutsideRangeException tooSmallExclusive(String name, Duration bound) {
        return new DurationOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static DurationOutsideRangeException tooLarge(String name, Duration max) {
        return new DurationOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static DurationOutsideRangeException tooLargeExclusive(String name, Duration bound) {
        return new DurationOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static DurationOutsideRangeException notPositive(String name) {
        return new DurationOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be positive.",
                        name
                )
        );
    }

    private static DurationOutsideRangeException isNegative(String name) {
        return new DurationOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must not be negative.",
                        name
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(Duration max, Duration min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(Duration max, Duration min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}

