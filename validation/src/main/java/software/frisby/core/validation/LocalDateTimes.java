package software.frisby.core.validation;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Static utility methods for validating {@link LocalDateTime} argument values.
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
 *       <pre>this.scheduledAt = LocalDateTimes.pastOrPresent("scheduledAt", scheduledAt);</pre></li>
 *   <li>Range methods validate that bounds are consistent before checking the value.
 *       An invalid bound configuration throws {@link IllegalConfigurationException}.</li>
 * </ul>
 *
 * <p>Two distinct categories of exception may be thrown:
 * <ul>
 *   <li><b>API misuse</b> — {@link NullPointerException} (null or blank {@code name}, or
 *       null bound, clock, or tolerance) and {@link IllegalConfigurationException} (invalid
 *       bound configuration) signal that the calling code is incorrect.</li>
 *   <li><b>Value failures</b> — {@link NullValueException} (null value) and
 *       {@link LocalDateTimeOutsideRangeException} (value outside the specified constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Two constraint families are provided:
 *
 * <p><b>Range family</b> — eight constraint methods that compare the value against one or
 * two fixed {@link LocalDateTime} bounds, following the same naming convention as
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
 * the current local date-time as reported by a {@link Clock}:
 * <ul>
 *   <li>{@code past}            — value is strictly before now</li>
 *   <li>{@code pastOrPresent}   — value is at or before now</li>
 *   <li>{@code futureOrPresent} — value is at or after now</li>
 *   <li>{@code future}          — value is strictly after now</li>
 * </ul>
 *
 * <p>{@code pastOrPresent} and {@code futureOrPresent} also accept an optional
 * {@link Duration} tolerance to accommodate clock-drift between distributed systems.
 * For example, {@code pastOrPresent(name, value, Duration.ofSeconds(30))} accepts any
 * value at or before {@code now + 30s}, allowing a remote caller's clock to be up to
 * 30 seconds ahead without triggering a validation failure.
 *
 * <p>All clock-relative methods have an overload that accepts a {@link Clock}, enabling
 * deterministic unit tests via {@link Clock#fixed(java.time.Instant, java.time.ZoneId)}.
 * Overloads without a {@link Clock} argument use {@link Clock#systemDefaultZone()}, which
 * matches the behavior of {@link LocalDateTime#now()}.
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see Instants
 * @see LocalDates
 * @see LocalTimes
 * @see OffsetDateTimes
 * @see ZonedDateTimes
 */
public final class LocalDateTimes {
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String TOLERANCE = "tolerance";
    private static final String CLOCK = "clock";

    private LocalDateTimes() {
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
    public static LocalDateTime notNull(String name, LocalDateTime value) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before {@code min}.
     */
    public static LocalDateTime min(String name, LocalDateTime value, LocalDateTime min) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after {@code max}.
     */
    public static LocalDateTime max(String name, LocalDateTime value, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static LocalDateTime minExclusive(String name, LocalDateTime value, LocalDateTime min) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static LocalDateTime maxExclusive(String name, LocalDateTime value, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is before {@code min}.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static LocalDateTime range(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is not strictly after {@code min}.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static LocalDateTime exclusiveRange(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is not strictly after {@code min}.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static LocalDateTime rangeExclusiveMax(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is not strictly after {@code min}.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static LocalDateTime rangeExclusiveMin(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly before the current local date-time.
     * Uses {@link Clock#systemDefaultZone()} for the current time, matching the behavior
     * of {@link LocalDateTime#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static LocalDateTime past(String name, LocalDateTime value) {
        return past(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is strictly before the local date-time
     * reported by {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static LocalDateTime past(String name, LocalDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isBefore(LocalDateTime.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or before the current local date-time.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static LocalDateTime pastOrPresent(String name, LocalDateTime value) {
        return pastOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is at or before the local date-time
     * reported by {@code clock}. No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static LocalDateTime pastOrPresent(String name, LocalDateTime value, Clock clock) {
        return pastOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value} is not null and is at or before {@code now + tolerance},
     * where {@code now} is determined by {@link Clock#systemDefaultZone()}.
     *
     * <p>The {@code tolerance} accommodates clock-drift between distributed systems: a positive
     * {@link Duration} allows the caller's clock to be up to {@code tolerance} ahead of the
     * server clock without triggering a validation failure.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param tolerance The maximum amount by which the caller's clock may be ahead of the
     *                  server clock; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static LocalDateTime pastOrPresent(String name, LocalDateTime value, Duration tolerance) {
        return pastOrPresent(name, value, tolerance, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is at or before {@code now + tolerance},
     * where {@code now} is determined by {@code clock}.
     *
     * <p>The {@code tolerance} accommodates clock-drift between distributed systems: a positive
     * {@link Duration} allows the caller's clock to be up to {@code tolerance} ahead of the
     * server clock without triggering a validation failure.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param tolerance The maximum amount by which the caller's clock may be ahead of the
     *                  server clock; must not be null.
     * @param clock     The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance}
     *                                            or {@code clock} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static LocalDateTime pastOrPresent(String name, LocalDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isAfter(LocalDateTime.now(clock).plus(tolerance))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or after the current local date-time.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static LocalDateTime futureOrPresent(String name, LocalDateTime value) {
        return futureOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is at or after the local date-time reported
     * by {@code clock}. No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static LocalDateTime futureOrPresent(String name, LocalDateTime value, Clock clock) {
        return futureOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value} is not null and is at or after {@code now - tolerance},
     * where {@code now} is determined by {@link Clock#systemDefaultZone()}.
     *
     * <p>The {@code tolerance} accommodates clock-drift between distributed systems: a positive
     * {@link Duration} allows the caller's clock to be up to {@code tolerance} behind the
     * server clock without triggering a validation failure.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param tolerance The maximum amount by which the caller's clock may be behind the
     *                  server clock; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static LocalDateTime futureOrPresent(String name, LocalDateTime value, Duration tolerance) {
        return futureOrPresent(name, value, tolerance, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is at or after {@code now - tolerance},
     * where {@code now} is determined by {@code clock}.
     *
     * <p>The {@code tolerance} accommodates clock-drift between distributed systems: a positive
     * {@link Duration} allows the caller's clock to be up to {@code tolerance} behind the
     * server clock without triggering a validation failure.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate.
     * @param tolerance The maximum amount by which the caller's clock may be behind the
     *                  server clock; must not be null.
     * @param clock     The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance}
     *                                            or {@code clock} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static LocalDateTime futureOrPresent(String name, LocalDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isBefore(LocalDateTime.now(clock).minus(tolerance))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly after the current local date-time.
     * Uses {@link Clock#systemDefaultZone()} for the current time, matching the behavior
     * of {@link LocalDateTime#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static LocalDateTime future(String name, LocalDateTime value) {
        return future(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is strictly after the local date-time
     * reported by {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                 if {@code value} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static LocalDateTime future(String name, LocalDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(LocalDateTime.now(clock))) throw notInFuture(name);

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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before {@code min}.
     */
    public static LocalDateTime optionalMin(String name, LocalDateTime value, LocalDateTime min) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code max} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after {@code max}.
     */
    public static LocalDateTime optionalMax(String name, LocalDateTime value, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static LocalDateTime optionalMinExclusive(String name, LocalDateTime value, LocalDateTime min) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code max} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static LocalDateTime optionalMaxExclusive(String name, LocalDateTime value, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is before {@code min}.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static LocalDateTime optionalRange(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is not strictly after {@code min}.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static LocalDateTime optionalExclusiveRange(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is not strictly after {@code min}.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static LocalDateTime optionalRangeExclusiveMax(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
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
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException      if {@code max} is not strictly after {@code min}.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static LocalDateTime optionalRangeExclusiveMin(String name, LocalDateTime value, LocalDateTime min, LocalDateTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly before the current local date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static LocalDateTime optionalPast(String name, LocalDateTime value) {
        return optionalPast(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is strictly before the local date-time reported
     * by {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static LocalDateTime optionalPast(String name, LocalDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isBefore(LocalDateTime.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or before the current local date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static LocalDateTime optionalPastOrPresent(String name, LocalDateTime value) {
        return optionalPastOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is at or before the local date-time reported
     * by {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     * No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static LocalDateTime optionalPastOrPresent(String name, LocalDateTime value, Clock clock) {
        return optionalPastOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value}, if present, is at or before {@code now + tolerance}.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for the current time.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param tolerance The maximum amount by which the caller's clock may be ahead of the
     *                  server clock; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static LocalDateTime optionalPastOrPresent(String name, LocalDateTime value, Duration tolerance) {
        return optionalPastOrPresent(name, value, tolerance, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is at or before {@code now + tolerance},
     * where {@code now} is determined by {@code clock}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param tolerance The maximum amount by which the caller's clock may be ahead of the
     *                  server clock; must not be null.
     * @param clock     The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance}
     *                                            or {@code clock} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static LocalDateTime optionalPastOrPresent(String name, LocalDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isAfter(LocalDateTime.now(clock).plus(tolerance))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or after the current local date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static LocalDateTime optionalFutureOrPresent(String name, LocalDateTime value) {
        return optionalFutureOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is at or after the local date-time reported
     * by {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     * No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static LocalDateTime optionalFutureOrPresent(String name, LocalDateTime value, Clock clock) {
        return optionalFutureOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value}, if present, is at or after {@code now - tolerance}.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for the current time.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param tolerance The maximum amount by which the caller's clock may be behind the
     *                  server clock; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static LocalDateTime optionalFutureOrPresent(String name, LocalDateTime value, Duration tolerance) {
        return optionalFutureOrPresent(name, value, tolerance, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is at or after {@code now - tolerance},
     * where {@code now} is determined by {@code clock}.
     * A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param tolerance The maximum amount by which the caller's clock may be behind the
     *                  server clock; must not be null.
     * @param clock     The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code tolerance}
     *                                            or {@code clock} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static LocalDateTime optionalFutureOrPresent(String name, LocalDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isBefore(LocalDateTime.now(clock).minus(tolerance))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly after the current local date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static LocalDateTime optionalFuture(String name, LocalDateTime value) {
        return optionalFuture(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is strictly after the local date-time reported
     * by {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current local date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException               if {@code name} is null or blank, or if {@code clock} is null.
     * @throws LocalDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static LocalDateTime optionalFuture(String name, LocalDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isAfter(LocalDateTime.now(clock))) throw notInFuture(name);

        return value;
    }

    private static void throwIfExclusiveBoundsInvalid(LocalDateTime min, LocalDateTime max) {
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

    private static LocalDateTimeOutsideRangeException tooSmall(String name, LocalDateTime min) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static LocalDateTimeOutsideRangeException tooSmallExclusive(String name, LocalDateTime bound) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static LocalDateTimeOutsideRangeException tooLarge(String name, LocalDateTime max) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static LocalDateTimeOutsideRangeException tooLargeExclusive(String name, LocalDateTime bound) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static LocalDateTimeOutsideRangeException notInPast(String name) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past.",
                        name
                )
        );
    }

    private static LocalDateTimeOutsideRangeException notPastOrPresent(String name) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past or the present.",
                        name
                )
        );
    }

    private static LocalDateTimeOutsideRangeException notFutureOrPresent(String name) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future or the present.",
                        name
                )
        );
    }

    private static LocalDateTimeOutsideRangeException notInFuture(String name) {
        return new LocalDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future.",
                        name
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(LocalDateTime max, LocalDateTime min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(LocalDateTime max, LocalDateTime min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}

