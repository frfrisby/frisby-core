package software.frisby.core.validation;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Static utility methods for validating {@link LocalDate} argument values.
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
 *       <pre>this.birthDate = LocalDates.max("birthDate", birthDate, today);</pre></li>
 *   <li>Range methods validate that bounds are consistent before checking the value.
 *       An invalid bound configuration throws {@link IllegalConfigurationException}.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}, or
 *       null bound or clock) and {@link IllegalConfigurationException} (invalid bound
 *       configuration) signal that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null value) and
 *       {@link LocalDateOutsideRangeException} (value outside the specified constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Two constraint families are provided:
 *
 * <p><b>Range family</b> — eight constraint methods that compare the value against one or
 * two fixed {@link LocalDate} bounds, following the same naming convention as
 * {@link Numbers}, {@link Durations}, and {@link Instants}:
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
 * <p><b>Clock-relative family</b> — four constraint methods that compare the value against
 * today's date as reported by a {@link Clock}:
 * <ul>
 *   <li>{@code past}            — value is strictly before today</li>
 *   <li>{@code pastOrPresent}   — value is today or before</li>
 *   <li>{@code futureOrPresent} — value is today or after</li>
 *   <li>{@code future}          — value is strictly after today</li>
 * </ul>
 *
 * <p>Unlike {@link Instants}, no tolerance parameter is offered for the clock-relative
 * family. Clock drift is a sub-second concern that does not apply to day-precision dates;
 * a date that is one day in the future is a deliberate business condition, not a timing
 * artifact.
 *
 * <p>All clock-relative methods have an overload that accepts a {@link Clock}, enabling
 * deterministic unit tests via {@link Clock#fixed(java.time.Instant, java.time.ZoneId)}.
 * Overloads without a {@link Clock} argument use {@link Clock#systemDefaultZone()}, which
 * matches the behavior of {@link LocalDate#now()}.
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see LocalDateTimes
 */
public final class LocalDates {
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String CLOCK = "clock";

    private LocalDates() {
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
    public static LocalDate notNull(String name, LocalDate value) {
        Throws.ifInvalidName(name);

        if (null == value) throw nullValue(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is on or after {@code min} (inclusive).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param min   The inclusive lower bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is before {@code min}.
     */
    public static LocalDate min(String name, LocalDate value, LocalDate min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) throw nullValue(name);
        if (value.isBefore(min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is on or before {@code max} (inclusive).
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is after {@code max}.
     */
    public static LocalDate max(String name, LocalDate value, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is on or before {@code min}.
     */
    public static LocalDate minExclusive(String name, LocalDate value, LocalDate min) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is on or after {@code max}.
     */
    public static LocalDate maxExclusive(String name, LocalDate value, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static LocalDate range(String name, LocalDate value, LocalDate min, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static LocalDate exclusiveRange(String name, LocalDate value, LocalDate min, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static LocalDate rangeExclusiveMax(String name, LocalDate value, LocalDate min, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static LocalDate rangeExclusiveMin(String name, LocalDate value, LocalDate min, LocalDate max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly before today.
     * Uses {@link Clock#systemDefaultZone()} for today's date, matching the behavior
     * of {@link LocalDate#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the future.
     */
    public static LocalDate past(String name, LocalDate value) {
        return past(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is strictly before the date reported by
     * {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the future.
     */
    public static LocalDate past(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isBefore(LocalDate.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is today or before.
     * Uses {@link Clock#systemDefaultZone()} for today's date, matching the behavior
     * of {@link LocalDate#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is after today.
     */
    public static LocalDate pastOrPresent(String name, LocalDate value) {
        return pastOrPresent(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is on or before the date reported by
     * {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is after today.
     */
    public static LocalDate pastOrPresent(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isAfter(LocalDate.now(clock))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is today or after.
     * Uses {@link Clock#systemDefaultZone()} for today's date, matching the behavior
     * of {@link LocalDate#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is before today.
     */
    public static LocalDate futureOrPresent(String name, LocalDate value) {
        return futureOrPresent(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is on or after the date reported by
     * {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is before today.
     */
    public static LocalDate futureOrPresent(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isBefore(LocalDate.now(clock))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly after today.
     * Uses {@link Clock#systemDefaultZone()} for today's date, matching the behavior
     * of {@link LocalDate#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the past.
     */
    public static LocalDate future(String name, LocalDate value) {
        return future(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is strictly after the date reported by
     * {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException             if {@code value} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the past.
     */
    public static LocalDate future(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(LocalDate.now(clock))) throw notInFuture(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is on or after {@code min} (inclusive).
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code min} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is before {@code min}.
     */
    public static LocalDate optionalMin(String name, LocalDate value, LocalDate min) {
        Throws.ifInvalidName(name);
        Throws.ifNull(MIN, min);

        if (null == value) return null;
        if (value.isBefore(min)) throw tooSmall(name, min);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is on or before {@code max} (inclusive).
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code max} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is after {@code max}.
     */
    public static LocalDate optionalMax(String name, LocalDate value, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is on or before {@code min}.
     */
    public static LocalDate optionalMinExclusive(String name, LocalDate value, LocalDate min) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is on or after {@code max}.
     */
    public static LocalDate optionalMaxExclusive(String name, LocalDate value, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static LocalDate optionalRange(String name, LocalDate value, LocalDate min, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static LocalDate optionalExclusiveRange(String name, LocalDate value, LocalDate min, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static LocalDate optionalRangeExclusiveMax(String name, LocalDate value, LocalDate min, LocalDate max) {
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
     * @throws LocalDateOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static LocalDate optionalRangeExclusiveMin(String name, LocalDate value, LocalDate min, LocalDate max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly before today.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for today's date.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the future.
     */
    public static LocalDate optionalPast(String name, LocalDate value) {
        return optionalPast(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is strictly before the date reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the future.
     */
    public static LocalDate optionalPast(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isBefore(LocalDate.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is today or before.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for today's date.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws LocalDateOutsideRangeException if {@code value} is after today.
     */
    public static LocalDate optionalPastOrPresent(String name, LocalDate value) {
        return optionalPastOrPresent(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is on or before the date reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is after today.
     */
    public static LocalDate optionalPastOrPresent(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isAfter(LocalDate.now(clock))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is today or after.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for today's date.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws LocalDateOutsideRangeException if {@code value} is before today.
     */
    public static LocalDate optionalFutureOrPresent(String name, LocalDate value) {
        return optionalFutureOrPresent(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is on or after the date reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is before today.
     */
    public static LocalDate optionalFutureOrPresent(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isBefore(LocalDate.now(clock))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly after today.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for today's date.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the past.
     */
    public static LocalDate optionalFuture(String name, LocalDate value) {
        return optionalFuture(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is strictly after the date reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine today's date; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException           if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateOutsideRangeException if {@code value} is today or in the past.
     */
    public static LocalDate optionalFuture(String name, LocalDate value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isAfter(LocalDate.now(clock))) throw notInFuture(name);

        return value;
    }

    private static void throwIfExclusiveBoundsInvalid(LocalDate min, LocalDate max) {
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

    private static LocalDateOutsideRangeException tooSmall(String name, LocalDate min) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static LocalDateOutsideRangeException tooSmallExclusive(String name, LocalDate bound) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static LocalDateOutsideRangeException tooLarge(String name, LocalDate max) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static LocalDateOutsideRangeException tooLargeExclusive(String name, LocalDate bound) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static LocalDateOutsideRangeException notInPast(String name) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past.",
                        name
                )
        );
    }

    private static LocalDateOutsideRangeException notPastOrPresent(String name) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past or the present.",
                        name
                )
        );
    }

    private static LocalDateOutsideRangeException notFutureOrPresent(String name) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future or the present.",
                        name
                )
        );
    }

    private static LocalDateOutsideRangeException notInFuture(String name) {
        return new LocalDateOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future.",
                        name
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(LocalDate max, LocalDate min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(LocalDate max, LocalDate min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}

