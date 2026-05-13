package software.frisby.core.validation;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Static utility methods for validating numeric argument values.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.count = Numbers.positive("count", count);</pre></li>
 *   <li>Range methods validate that bounds are consistent before checking the value.
 *       An invalid bound configuration (e.g. {@code max &lt; min}) throws
 *       {@link IllegalConfigurationException}.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}, or
 *       null bound for reference-type bounds) and {@link IllegalConfigurationException}
 *       (invalid bound configuration) signal that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null boxed value) and
 *       {@link NumericValueOutsideRangeException} (value outside the specified bound)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Primitive overloads are provided for {@code int}, {@code long}, {@code short},
 * {@code byte}, {@code float}, and {@code double}. Boxed-type overloads add a null
 * guard and throw {@link NullValueException} when the value is {@code null}.
 * {@code optional*} variants pass {@code null} through without validation.
 *
 * <p>Ten constraint families are provided:
 * <ul>
 *   <li>{@code min}  — value &gt;= min (inclusive lower bound)</li>
 *   <li>{@code max}  — value &lt;= max (inclusive upper bound)</li>
 *   <li>{@code minExclusive}  — value &gt; min (exclusive lower bound)</li>
 *   <li>{@code maxExclusive}  — value &lt; max (exclusive upper bound)</li>
 *   <li>{@code range}  — min &lt;= value &lt;= max (fully inclusive)</li>
 *   <li>{@code exclusiveRange}  — min &lt; value &lt; max (fully exclusive)</li>
 *   <li>{@code rangeExclusiveMax}  — min &lt;= value &lt; max (inclusive min, exclusive max)</li>
 *   <li>{@code rangeExclusiveMin}  — min &lt; value &lt;= max (exclusive min, inclusive max)</li>
 *   <li>{@code positive}  — value &gt; 0 (exclusive zero lower bound)</li>
 *   <li>{@code notNegative}  — value &gt;= 0 (inclusive zero lower bound)</li>
 * </ul>
 *
 * <p>For {@code float} and {@code double}, the negated-complement form of each comparison
 * is used (e.g. {@code !(value >= min)} rather than {@code value < min}) so that
 * {@code NaN} values correctly fail every bound check with a
 * {@link NumericValueOutsideRangeException}.
 *
 * @see Durations
 */
public final class Numbers {
    private static final String MIN = "min";
    private static final String MAX = "max";

    private Numbers() {
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code int} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static int notNull(String name, Integer value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int min(String name, int value, int min) {
        Throws.ifInvalidName(name);

        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} as {@code int} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int min(String name, Integer value, int min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int max(String name, int value, int max) {
        Throws.ifInvalidName(name);

        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code int} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int max(String name, Integer value, int max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int minExclusive(String name, int value, int min) {
        Throws.ifInvalidName(name);

        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} as {@code int} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int minExclusive(String name, Integer value, int min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int maxExclusive(String name, int value, int max) {
        Throws.ifInvalidName(name);

        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code int} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int maxExclusive(String name, Integer value, int max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int range(String name, int value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code int} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int range(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int exclusiveRange(String name, int value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code int} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int exclusiveRange(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int rangeExclusiveMax(String name, int value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code int} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int rangeExclusiveMax(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int rangeExclusiveMin(String name, int value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code int} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static int rangeExclusiveMin(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalMin(String name, Integer value, int min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalMax(String name, Integer value, int max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalMinExclusive(String name, Integer value, int min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalMaxExclusive(String name, Integer value, int max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalRange(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalExclusiveRange(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalRangeExclusiveMax(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Integer optionalRangeExclusiveMin(String name, Integer value, int min, int max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static int positive(String name, int value) {
        Throws.ifInvalidName(name);

        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code int} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static int positive(String name, Integer value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static Integer optionalPositive(String name, Integer value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static int notNegative(String name, int value) {
        Throws.ifInvalidName(name);

        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code int} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static int notNegative(String name, Integer value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static Integer optionalNotNegative(String name, Integer value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code long} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static long notNull(String name, Long value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long min(String name, long value, long min) {
        Throws.ifInvalidName(name);

        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} as {@code long} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long min(String name, Long value, long min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long max(String name, long value, long max) {
        Throws.ifInvalidName(name);

        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code long} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long max(String name, Long value, long max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long minExclusive(String name, long value, long min) {
        Throws.ifInvalidName(name);

        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} as {@code long} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long minExclusive(String name, Long value, long min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long maxExclusive(String name, long value, long max) {
        Throws.ifInvalidName(name);

        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code long} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long maxExclusive(String name, Long value, long max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long range(String name, long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code long} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long range(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long exclusiveRange(String name, long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code long} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long exclusiveRange(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long rangeExclusiveMax(String name, long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code long} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long rangeExclusiveMax(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long rangeExclusiveMin(String name, long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code long} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static long rangeExclusiveMin(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalMin(String name, Long value, long min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalMax(String name, Long value, long max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalMinExclusive(String name, Long value, long min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalMaxExclusive(String name, Long value, long max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalRange(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalExclusiveRange(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalRangeExclusiveMax(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Long optionalRangeExclusiveMin(String name, Long value, long min, long max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static long positive(String name, long value) {
        Throws.ifInvalidName(name);

        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code long} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static long positive(String name, Long value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static Long optionalPositive(String name, Long value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static long notNegative(String name, long value) {
        Throws.ifInvalidName(name);

        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code long} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static long notNegative(String name, Long value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static Long optionalNotNegative(String name, Long value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code short} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static short notNull(String name, Short value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short min(String name, short value, short min) {
        Throws.ifInvalidName(name);

        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} as {@code short} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short min(String name, Short value, short min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short max(String name, short value, short max) {
        Throws.ifInvalidName(name);

        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code short} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short max(String name, Short value, short max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short minExclusive(String name, short value, short min) {
        Throws.ifInvalidName(name);

        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} as {@code short} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short minExclusive(String name, Short value, short min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short maxExclusive(String name, short value, short max) {
        Throws.ifInvalidName(name);

        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code short} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short maxExclusive(String name, Short value, short max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short range(String name, short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code short} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short range(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short exclusiveRange(String name, short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code short} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short exclusiveRange(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short rangeExclusiveMax(String name, short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code short} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short rangeExclusiveMax(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short rangeExclusiveMin(String name, short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code short} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static short rangeExclusiveMin(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalMin(String name, Short value, short min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalMax(String name, Short value, short max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalMinExclusive(String name, Short value, short min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalMaxExclusive(String name, Short value, short max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalRange(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalExclusiveRange(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalRangeExclusiveMax(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Short optionalRangeExclusiveMin(String name, Short value, short min, short max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static short positive(String name, short value) {
        Throws.ifInvalidName(name);

        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code short} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static short positive(String name, Short value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static Short optionalPositive(String name, Short value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static short notNegative(String name, short value) {
        Throws.ifInvalidName(name);

        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code short} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static short notNegative(String name, Short value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static Short optionalNotNegative(String name, Short value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code byte} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static byte notNull(String name, Byte value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte min(String name, byte value, byte min) {
        Throws.ifInvalidName(name);

        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} as {@code byte} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte min(String name, Byte value, byte min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte max(String name, byte value, byte max) {
        Throws.ifInvalidName(name);

        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code byte} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte max(String name, Byte value, byte max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte minExclusive(String name, byte value, byte min) {
        Throws.ifInvalidName(name);

        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} as {@code byte} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte minExclusive(String name, Byte value, byte min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte maxExclusive(String name, byte value, byte max) {
        Throws.ifInvalidName(name);

        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code byte} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte maxExclusive(String name, Byte value, byte max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte range(String name, byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code byte} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte range(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte exclusiveRange(String name, byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code byte} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte exclusiveRange(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte rangeExclusiveMax(String name, byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code byte} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte rangeExclusiveMax(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte rangeExclusiveMin(String name, byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code byte} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static byte rangeExclusiveMin(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalMin(String name, Byte value, byte min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalMax(String name, Byte value, byte max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalMinExclusive(String name, Byte value, byte min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalMaxExclusive(String name, Byte value, byte max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalRange(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max < min) throw maxLtMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalExclusiveRange(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalRangeExclusiveMax(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value < min) throw tooSmall(name, min);
        if (value >= max) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Byte optionalRangeExclusiveMin(String name, Byte value, byte min, byte max) {
        Throws.ifInvalidName(name);

        if (max <= min) throw maxLeMin(max, min);
        if (null == value) return null;
        if (value <= min) throw tooSmallExclusive(name, min);
        if (value > max) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static byte positive(String name, byte value) {
        Throws.ifInvalidName(name);

        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code byte} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static byte positive(String name, Byte value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static Byte optionalPositive(String name, Byte value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static byte notNegative(String name, byte value) {
        Throws.ifInvalidName(name);

        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code byte} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static byte notNegative(String name, Byte value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static Byte optionalNotNegative(String name, Byte value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code float} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static float notNull(String name, Float value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float min(String name, float value, float min) {
        Throws.ifInvalidName(name);

        if (!(value >= min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} as {@code float} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float min(String name, Float value, float min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value >= min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float max(String name, float value, float max) {
        Throws.ifInvalidName(name);

        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code float} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float max(String name, Float value, float max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float minExclusive(String name, float value, float min) {
        Throws.ifInvalidName(name);

        if (!(value > min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} as {@code float} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float minExclusive(String name, Float value, float min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value > min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float maxExclusive(String name, float value, float max) {
        Throws.ifInvalidName(name);

        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code float} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float maxExclusive(String name, Float value, float max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float range(String name, float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max >= min)) throw maxLtMin(max, min);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code float} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float range(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max >= min)) throw maxLtMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float exclusiveRange(String name, float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code float} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float exclusiveRange(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float rangeExclusiveMax(String name, float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code float} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float rangeExclusiveMax(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float rangeExclusiveMin(String name, float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code float} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static float rangeExclusiveMin(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalMin(String name, Float value, float min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value >= min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalMax(String name, Float value, float max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalMinExclusive(String name, Float value, float min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value > min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalMaxExclusive(String name, Float value, float max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalRange(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max >= min)) throw maxLtMin(max, min);
        if (null == value) return null;
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalExclusiveRange(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) return null;
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalRangeExclusiveMax(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) return null;
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Float optionalRangeExclusiveMin(String name, Float value, float min, float max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) return null;
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than zero.  Uses the negated-complement
     * form {@code !(value > 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive, or is {@code NaN}.
     */
    public static float positive(String name, float value) {
        Throws.ifInvalidName(name);

        if (!(value > 0)) throw tooSmallExclusive(name, 0.0f);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.  Uses the
     * negated-complement form {@code !(value > 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code float} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive, or is {@code NaN}.
     */
    public static float positive(String name, Float value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value > 0)) throw tooSmallExclusive(name, 0.0f);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.  Uses the
     * negated-complement form {@code !(value > 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive, or is {@code NaN}.
     */
    public static Float optionalPositive(String name, Float value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value > 0)) throw tooSmallExclusive(name, 0.0f);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to zero.  Uses the
     * negated-complement form {@code !(value >= 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative, or is {@code NaN}.
     */
    public static float notNegative(String name, float value) {
        Throws.ifInvalidName(name);

        if (!(value >= 0)) throw tooSmall(name, 0.0f);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.  Uses the
     * negated-complement form {@code !(value >= 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code float} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative, or is {@code NaN}.
     */
    public static float notNegative(String name, Float value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value >= 0)) throw tooSmall(name, 0.0f);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.  Uses the
     * negated-complement form {@code !(value >= 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative, or is {@code NaN}.
     */
    public static Float optionalNotNegative(String name, Float value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value >= 0)) throw tooSmall(name, 0.0f);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code double} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static double notNull(String name, Double value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double min(String name, double value, double min) {
        Throws.ifInvalidName(name);

        if (!(value >= min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @return {@code value} as {@code double} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double min(String name, Double value, double min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value >= min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double max(String name, double value, double max) {
        Throws.ifInvalidName(name);

        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is less than or equal to {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code double} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double max(String name, Double value, double max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double minExclusive(String name, double value, double min) {
        Throws.ifInvalidName(name);

        if (!(value > min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @return {@code value} as {@code double} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double minExclusive(String name, Double value, double min) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value > min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double maxExclusive(String name, double value, double max) {
        Throws.ifInvalidName(name);

        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly less than {@code max}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code double} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double maxExclusive(String name, Double value, double max) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double range(String name, double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max >= min)) throw maxLtMin(max, min);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code double} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double range(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max >= min)) throw maxLtMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double exclusiveRange(String name, double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code double} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double exclusiveRange(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double rangeExclusiveMax(String name, double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} as {@code double} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double rangeExclusiveMax(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double rangeExclusiveMin(String name, double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} as {@code double} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static double rangeExclusiveMin(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) throw nullValue(name);
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalMin(String name, Double value, double min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value >= min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalMax(String name, Double value, double max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalMinExclusive(String name, Double value, double min) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value > min)) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalMaxExclusive(String name, Double value, double max) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalRange(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max >= min)) throw maxLtMin(max, min);
        if (null == value) return null;
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalExclusiveRange(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) return null;
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound.
     * @param max   The exclusive upper bound.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalRangeExclusiveMax(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) return null;
        if (!(value >= min)) throw tooSmall(name, min);
        if (!(value < max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound.
     * @param max   The inclusive upper bound.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static Double optionalRangeExclusiveMin(String name, Double value, double min, double max) {
        Throws.ifInvalidName(name);

        if (!(max > min)) throw maxLeMin(max, min);
        if (null == value) return null;
        if (!(value > min)) throw tooSmallExclusive(name, min);
        if (!(value <= max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is strictly greater than zero.  Uses the negated-complement
     * form {@code !(value > 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive, or is {@code NaN}.
     */
    public static double positive(String name, double value) {
        Throws.ifInvalidName(name);

        if (!(value > 0)) throw tooSmallExclusive(name, 0.0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.  Uses the
     * negated-complement form {@code !(value > 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code double} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive, or is {@code NaN}.
     */
    public static double positive(String name, Double value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value > 0)) throw tooSmallExclusive(name, 0.0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.  Uses the
     * negated-complement form {@code !(value > 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive, or is {@code NaN}.
     */
    public static Double optionalPositive(String name, Double value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value > 0)) throw tooSmallExclusive(name, 0.0);

        return value;
    }

    /**
     * Validates that {@code value} is greater than or equal to zero.  Uses the
     * negated-complement form {@code !(value >= 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative, or is {@code NaN}.
     */
    public static double notNegative(String name, double value) {
        Throws.ifInvalidName(name);

        if (!(value >= 0)) throw tooSmall(name, 0.0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.  Uses the
     * negated-complement form {@code !(value >= 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} as {@code double} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative, or is {@code NaN}.
     */
    public static double notNegative(String name, Double value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (!(value >= 0)) throw tooSmall(name, 0.0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.  Uses the
     * negated-complement form {@code !(value >= 0)} so that {@code NaN} correctly fails this check.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative, or is {@code NaN}.
     */
    public static Double optionalNotNegative(String name, Double value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (!(value >= 0)) throw tooSmall(name, 0.0);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static BigDecimal notNull(String name, BigDecimal value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound, must not be null.
     * @return {@code value} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal min(String name, BigDecimal value, BigDecimal min) {
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
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal max(String name, BigDecimal value, BigDecimal max) {
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
     * @param min   The exclusive lower bound, must not be null.
     * @return {@code value} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal minExclusive(String name, BigDecimal value, BigDecimal min) {
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
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal maxExclusive(String name, BigDecimal value, BigDecimal max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal range(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
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
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal exclusiveRange(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal rangeExclusiveMax(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal rangeExclusiveMin(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound, must not be null.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalMin(String name, BigDecimal value, BigDecimal min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalMax(String name, BigDecimal value, BigDecimal max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);
        if (null == value) return null;
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound, must not be null.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalMinExclusive(String name, BigDecimal value, BigDecimal min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalMaxExclusive(String name, BigDecimal value, BigDecimal max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);
        if (null == value) return null;
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalRange(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
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
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalExclusiveRange(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalRangeExclusiveMax(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigDecimal optionalRangeExclusiveMin(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static BigDecimal positive(String name, BigDecimal value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.compareTo(BigDecimal.ZERO) <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static BigDecimal optionalPositive(String name, BigDecimal value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.compareTo(BigDecimal.ZERO) <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static BigDecimal notNegative(String name, BigDecimal value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.compareTo(BigDecimal.ZERO) < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static BigDecimal optionalNotNegative(String name, BigDecimal value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.compareTo(BigDecimal.ZERO) < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is not null.
     * @throws NullPointerException if {@code name} is null or blank.
     * @throws NullValueException   if {@code value} is null.
     */
    public static BigInteger notNull(String name, BigInteger value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to {@code min}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound, must not be null.
     * @return {@code value} if it is not null and is greater than or equal to {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger min(String name, BigInteger value, BigInteger min) {
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
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is not null and is less than or equal to {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger max(String name, BigInteger value, BigInteger max) {
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
     * @param min   The exclusive lower bound, must not be null.
     * @return {@code value} if it is not null and is strictly greater than {@code min}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger minExclusive(String name, BigInteger value, BigInteger min) {
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
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is not null and is strictly less than {@code max}.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger maxExclusive(String name, BigInteger value, BigInteger max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}], inclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls within [{@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger range(String name, BigInteger value, BigInteger min, BigInteger max) {
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
     * Validates that {@code value} is not null and falls strictly within ({@code min}, {@code max}), exclusive on both sides.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls strictly within ({@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger exclusiveRange(String name, BigInteger value, BigInteger min, BigInteger max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls within [{@code min}, {@code max}).
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger rangeExclusiveMax(String name, BigInteger value, BigInteger min, BigInteger max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is not null and falls within ({@code min}, {@code max}].
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger rangeExclusiveMin(String name, BigInteger value, BigInteger min, BigInteger max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) throw nullValue(name);
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound, must not be null.
     * @return {@code value} if it is greater than or equal to {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalMin(String name, BigInteger value, BigInteger min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is less than or equal to {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it is less than or equal to {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalMax(String name, BigInteger value, BigInteger max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);
        if (null == value) return null;
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than {@code min}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound, must not be null.
     * @return {@code value} if it is strictly greater than {@code min}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalMinExclusive(String name, BigInteger value, BigInteger min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);
        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly less than {@code max}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it is strictly less than {@code max}, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code max} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalMaxExclusive(String name, BigInteger value, BigInteger max) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MAX, max);
        if (null == value) return null;
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}], inclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it falls within [{@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is less than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalRange(String name, BigInteger value, BigInteger min, BigInteger max) {
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
     * Validates that {@code value}, if present, falls strictly within ({@code min}, {@code max}), exclusive on both sides. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it falls strictly within ({@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalExclusiveRange(String name, BigInteger value, BigInteger min, BigInteger max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}), inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound, must not be null.
     * @param max   The exclusive upper bound, must not be null.
     * @return {@code value} if it falls within [{@code min}, {@code max}), or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalRangeExclusiveMax(String name, BigInteger value, BigInteger min, BigInteger max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) return null;
        if (value.compareTo(min) < 0) throw tooSmall(name, min);
        if (value.compareTo(max) >= 0) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}], exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound, must not be null.
     * @param max   The inclusive upper bound, must not be null.
     * @return {@code value} if it falls within ({@code min}, {@code max}], or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException     if {@code max} is not strictly greater than {@code min}.
     * @throws NumericValueOutsideRangeException if {@code value} is outside the specified bound.
     */
    public static BigInteger optionalRangeExclusiveMin(String name, BigInteger value, BigInteger min, BigInteger max) {
        Throws.ifInvalidName(name);

        throwIfExclusiveBoundsInvalid(min, max);
        if (null == value) return null;
        if (value.compareTo(min) <= 0) throw tooSmallExclusive(name, min);
        if (value.compareTo(max) > 0) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly greater than zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is not null and is strictly greater than zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static BigInteger positive(String name, BigInteger value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.compareTo(BigInteger.ZERO) <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly greater than zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is strictly greater than zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is not positive.
     */
    public static BigInteger optionalPositive(String name, BigInteger value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.compareTo(BigInteger.ZERO) <= 0) throw tooSmallExclusive(name, 0);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is greater than or equal to zero.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return {@code value} if it is not null and is greater than or equal to zero.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NullValueException                if {@code value} is null.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static BigInteger notNegative(String name, BigInteger value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.compareTo(BigInteger.ZERO) < 0) throw tooSmall(name, 0);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is greater than or equal to zero.  A null
     * {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return {@code value} if it is greater than or equal to zero, or {@code null} if {@code value} is null.
     * @throws NullPointerException              if {@code name} is null or blank.
     * @throws NumericValueOutsideRangeException if {@code value} is negative.
     */
    public static BigInteger optionalNotNegative(String name, BigInteger value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.compareTo(BigInteger.ZERO) < 0) throw tooSmall(name, 0);

        return value;
    }

    private static <T extends Comparable<T>> void throwIfExclusiveBoundsInvalid(T min, T max) {
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

    private static NumericValueOutsideRangeException tooSmall(String name, Object min) {
        return new NumericValueOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static NumericValueOutsideRangeException tooSmallExclusive(String name, Object bound) {
        return new NumericValueOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static NumericValueOutsideRangeException tooLarge(String name, Object max) {
        return new NumericValueOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static NumericValueOutsideRangeException tooLargeExclusive(String name, Object bound) {
        return new NumericValueOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(Object max, Object min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(Object max, Object min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}







