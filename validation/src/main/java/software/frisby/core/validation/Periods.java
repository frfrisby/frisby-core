package software.frisby.core.validation;

import java.time.Period;

/**
 * Static utility methods for validating {@link Period} argument values.
 *
 * <p>All methods follow a consistent contract:
 * <ul>
 *   <li>The first argument, {@code name}, identifies the argument being validated and is
 *       used solely to produce meaningful exception messages. It must never be null or
 *       blank; passing an invalid {@code name} is an API misuse and throws
 *       {@link NullPointerException}.</li>
 *   <li>Methods return the validated {@code value} unchanged, allowing inline assignment:
 *       <pre>this.probationPeriod = Periods.positive("probationPeriod", probationPeriod);</pre></li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name})
 *       signals that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null value) and
 *       {@link PeriodOutsideRangeException} (value does not satisfy the required constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Min, max, and range constraints are intentionally omitted. {@link Period} represents
 * a date-based amount of time in years, months, and days, and does not implement
 * {@link Comparable}. There is no total ordering over periods — whether one month is
 * "greater than" thirty days depends on calendar context — so magnitude comparisons are
 * not meaningful as general-purpose validation constraints.
 *
 * <p>Two constraint families are provided:
 * <ul>
 *   <li>{@code positive}     — no component is negative and at least one component is
 *       greater than zero</li>
 *   <li>{@code notNegative}  — no individual component is negative (zero is permitted)</li>
 * </ul>
 *
 * <p><b>Mixed-sign periods:</b> Both constraints delegate to {@link Period#isNegative()},
 * which returns {@code true} when <em>any</em> individual component (years, months, or days)
 * is negative, regardless of the signs of the other components. A mixed-sign period such as
 * {@code Period.of(1, -1, 0)} is therefore treated as negative and will fail both
 * {@code positive} and {@code notNegative}. Callers who need to permit mixed-sign periods
 * should call {@link Period#normalized()} before validating, or perform their own
 * component-level checks.
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see Durations
 */
public final class Periods {
    private Periods() {
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
    public static Period notNull(String name, Period value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly positive — that is,
     * no component is negative and at least one component is greater than zero.
     *
     * <p>This method delegates to {@link Period#isNegative()} and {@link Period#isZero()}.
     * {@link Period#isNegative()} returns {@code true} when <em>any</em> individual
     * component (years, months, or days) is negative, so a mixed-sign period such as
     * {@code Period.of(1, -1, 0)} is considered negative and will fail this validation.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException        if {@code name} is null or blank.
     * @throws NullValueException          if {@code value} is null.
     * @throws PeriodOutsideRangeException if {@code value} is zero or negative.
     */
    public static Period positive(String name, Period value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.isNegative() || value.isZero()) throw notPositive(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is not negative — that is,
     * no individual component is negative. A zero period is considered valid.
     *
     * <p>This method delegates to {@link Period#isNegative()}, which returns {@code true}
     * when <em>any</em> individual component (years, months, or days) is negative,
     * regardless of the signs of the other components. Mixed-sign periods such as
     * {@code Period.of(1, -1, 0)} are therefore considered negative and will fail this
     * validation.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException        if {@code name} is null or blank.
     * @throws NullValueException          if {@code value} is null.
     * @throws PeriodOutsideRangeException if {@code value} is negative.
     */
    public static Period notNegative(String name, Period value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);
        if (value.isNegative()) throw isNegative(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly positive.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * <p>See {@link #positive(String, Period)} for a full description of what
     * constitutes a positive period, including the behavior for mixed-sign periods.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException        if {@code name} is null or blank.
     * @throws PeriodOutsideRangeException if {@code value} is zero or negative.
     */
    public static Period optionalPositive(String name, Period value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.isNegative() || value.isZero()) throw notPositive(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is not negative.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * <p>See {@link #notNegative(String, Period)} for a full description of what
     * constitutes a non-negative period, including the behavior for mixed-sign periods.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException        if {@code name} is null or blank.
     * @throws PeriodOutsideRangeException if {@code value} is negative.
     */
    public static Period optionalNotNegative(String name, Period value) {
        Throws.ifInvalidName(name);

        if (null == value) return null;
        if (value.isNegative()) throw isNegative(name);

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

    private static PeriodOutsideRangeException notPositive(String name) {
        return new PeriodOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be positive.",
                        name
                )
        );
    }

    private static PeriodOutsideRangeException isNegative(String name) {
        return new PeriodOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must not be negative.",
                        name
                )
        );
    }
}

