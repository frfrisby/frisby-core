package software.frisby.core.validation;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Static utility methods for validating {@link OffsetDateTime} argument values.
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
 *       <pre>this.submittedAt = OffsetDateTimes.pastOrPresent("submittedAt", submittedAt);</pre></li>
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
 *       {@link OffsetDateTimeOutsideRangeException} (value outside the specified constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Two constraint families are provided:
 *
 * <p><b>Range family</b> — eight constraint methods that compare the value against one or
 * two fixed {@link OffsetDateTime} bounds, following the same naming convention as
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
 * <p>Note that {@link OffsetDateTime} comparisons are based on the actual instant in time,
 * adjusting for the UTC offset of each value. Two {@link OffsetDateTime} values representing
 * the same wall-clock time in different offsets are not equal and will not be treated as such.
 *
 * <p><b>Clock-relative family</b> — four constraint methods that compare the value against
 * the current date-time as reported by a {@link Clock}:
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
 * matches the behavior of {@link OffsetDateTime#now()}.
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see Instants
 * @see LocalDateTimes
 * @see OffsetTimes
 * @see ZonedDateTimes
 */
public final class OffsetDateTimes {
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String TOLERANCE = "tolerance";
    private static final String CLOCK = "clock";

    private OffsetDateTimes() {
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
    public static OffsetDateTime notNull(String name, OffsetDateTime value) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before {@code min}.
     */
    public static OffsetDateTime min(String name, OffsetDateTime value, OffsetDateTime min) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after {@code max}.
     */
    public static OffsetDateTime max(String name, OffsetDateTime value, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static OffsetDateTime minExclusive(String name, OffsetDateTime value, OffsetDateTime min) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static OffsetDateTime maxExclusive(String name, OffsetDateTime value, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is before {@code min}.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static OffsetDateTime range(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is not strictly after {@code min}.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static OffsetDateTime exclusiveRange(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is not strictly after {@code min}.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static OffsetDateTime rangeExclusiveMax(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is not strictly after {@code min}.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static OffsetDateTime rangeExclusiveMin(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly before the current date-time.
     * Uses {@link Clock#systemDefaultZone()} for the current time, matching the behavior
     * of {@link OffsetDateTime#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static OffsetDateTime past(String name, OffsetDateTime value) {
        return past(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is strictly before the date-time reported
     * by {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static OffsetDateTime past(String name, OffsetDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isBefore(OffsetDateTime.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or before the current date-time.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static OffsetDateTime pastOrPresent(String name, OffsetDateTime value) {
        return pastOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is at or before the date-time reported
     * by {@code clock}. No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static OffsetDateTime pastOrPresent(String name, OffsetDateTime value, Clock clock) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static OffsetDateTime pastOrPresent(String name, OffsetDateTime value, Duration tolerance) {
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
     * @param clock     The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance}
     *                                             or {@code clock} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static OffsetDateTime pastOrPresent(String name, OffsetDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isAfter(OffsetDateTime.now(clock).plus(tolerance))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or after the current date-time.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static OffsetDateTime futureOrPresent(String name, OffsetDateTime value) {
        return futureOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is at or after the date-time reported
     * by {@code clock}. No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static OffsetDateTime futureOrPresent(String name, OffsetDateTime value, Clock clock) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static OffsetDateTime futureOrPresent(String name, OffsetDateTime value, Duration tolerance) {
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
     * @param clock     The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance}
     *                                             or {@code clock} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static OffsetDateTime futureOrPresent(String name, OffsetDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isBefore(OffsetDateTime.now(clock).minus(tolerance))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly after the current date-time.
     * Uses {@link Clock#systemDefaultZone()} for the current time, matching the behavior
     * of {@link OffsetDateTime#now()}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static OffsetDateTime future(String name, OffsetDateTime value) {
        return future(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value} is not null and is strictly after the date-time reported
     * by {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException                  if {@code value} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static OffsetDateTime future(String name, OffsetDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(OffsetDateTime.now(clock))) throw notInFuture(name);

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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before {@code min}.
     */
    public static OffsetDateTime optionalMin(String name, OffsetDateTime value, OffsetDateTime min) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code max} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after {@code max}.
     */
    public static OffsetDateTime optionalMax(String name, OffsetDateTime value, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static OffsetDateTime optionalMinExclusive(String name, OffsetDateTime value, OffsetDateTime min) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code max} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static OffsetDateTime optionalMaxExclusive(String name, OffsetDateTime value, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is before {@code min}.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}].
     */
    public static OffsetDateTime optionalRange(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is not strictly after {@code min}.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}).
     */
    public static OffsetDateTime optionalExclusiveRange(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is not strictly after {@code min}.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside [{@code min}, {@code max}).
     */
    public static OffsetDateTime optionalRangeExclusiveMax(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException       if {@code max} is not strictly after {@code min}.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is outside ({@code min}, {@code max}].
     */
    public static OffsetDateTime optionalRangeExclusiveMin(String name, OffsetDateTime value, OffsetDateTime min, OffsetDateTime max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly before the current date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static OffsetDateTime optionalPast(String name, OffsetDateTime value) {
        return optionalPast(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is strictly before the date-time reported
     * by {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or after now.
     */
    public static OffsetDateTime optionalPast(String name, OffsetDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isBefore(OffsetDateTime.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or before the current date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static OffsetDateTime optionalPastOrPresent(String name, OffsetDateTime value) {
        return optionalPastOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is at or before the date-time reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     * No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after now.
     */
    public static OffsetDateTime optionalPastOrPresent(String name, OffsetDateTime value, Clock clock) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static OffsetDateTime optionalPastOrPresent(String name, OffsetDateTime value, Duration tolerance) {
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
     * @param clock     The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance}
     *                                             or {@code clock} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static OffsetDateTime optionalPastOrPresent(String name, OffsetDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isAfter(OffsetDateTime.now(clock).plus(tolerance))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or after the current date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static OffsetDateTime optionalFutureOrPresent(String name, OffsetDateTime value) {
        return optionalFutureOrPresent(name, value, Duration.ZERO, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is at or after the date-time reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     * No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before now.
     */
    public static OffsetDateTime optionalFutureOrPresent(String name, OffsetDateTime value, Clock clock) {
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
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static OffsetDateTime optionalFutureOrPresent(String name, OffsetDateTime value, Duration tolerance) {
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
     * @param clock     The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code tolerance}
     *                                             or {@code clock} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static OffsetDateTime optionalFutureOrPresent(String name, OffsetDateTime value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isBefore(OffsetDateTime.now(clock).minus(tolerance))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly after the current date-time.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemDefaultZone()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static OffsetDateTime optionalFuture(String name, OffsetDateTime value) {
        return optionalFuture(name, value, Clock.systemDefaultZone());
    }

    /**
     * Validates that {@code value}, if present, is strictly after the date-time reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current date-time; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException                if {@code name} is null or blank, or if {@code clock} is null.
     * @throws OffsetDateTimeOutsideRangeException if {@code value} is at or before now.
     */
    public static OffsetDateTime optionalFuture(String name, OffsetDateTime value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isAfter(OffsetDateTime.now(clock))) throw notInFuture(name);

        return value;
    }

    private static void throwIfExclusiveBoundsInvalid(OffsetDateTime min, OffsetDateTime max) {
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

    private static OffsetDateTimeOutsideRangeException tooSmall(String name, OffsetDateTime min) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException tooSmallExclusive(String name, OffsetDateTime bound) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException tooLarge(String name, OffsetDateTime max) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException tooLargeExclusive(String name, OffsetDateTime bound) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException notInPast(String name) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past.",
                        name
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException notPastOrPresent(String name) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past or the present.",
                        name
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException notFutureOrPresent(String name) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future or the present.",
                        name
                )
        );
    }

    private static OffsetDateTimeOutsideRangeException notInFuture(String name) {
        return new OffsetDateTimeOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future.",
                        name
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(OffsetDateTime max, OffsetDateTime min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(OffsetDateTime max, OffsetDateTime min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}

