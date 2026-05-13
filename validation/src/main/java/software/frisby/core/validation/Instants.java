package software.frisby.core.validation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Static utility methods for validating {@link Instant} argument values.
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
 *       <pre>this.createdAt = Instants.pastOrPresent("createdAt", createdAt);</pre></li>
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
 *       {@link InstantOutsideRangeException} (value outside the specified constraint)
 *       signal that the value being validated does not meet the required criteria.</li>
 * </ul>
 *
 * <p>Two constraint families are provided:
 *
 * <p><b>Range family</b> — eight constraint methods that compare the value against one or
 * two fixed {@link Instant} bounds, following the same naming convention as
 * {@link Numbers} and {@link Durations}:
 * <ul>
 *   <li>{@code min}              — value &gt;= min (inclusive lower bound)</li>
 *   <li>{@code max}              — value &lt;= max (inclusive upper bound)</li>
 *   <li>{@code minExclusive}     — value &gt; min (exclusive lower bound)</li>
 *   <li>{@code maxExclusive}     — value &lt; max (exclusive upper bound)</li>
 *   <li>{@code range}            — min &lt;= value &lt;= max (fully inclusive)</li>
 *   <li>{@code exclusiveRange}   — min &lt; value &lt; max (fully exclusive)</li>
 *   <li>{@code rangeExclusiveMax} — min &lt;= value &lt; max</li>
 *   <li>{@code rangeExclusiveMin} — min &lt; value &lt;= max</li>
 * </ul>
 *
 * <p><b>Clock-relative family</b> — four constraint methods that compare the value against
 * the current instant as reported by a {@link Clock}:
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
 * deterministic unit tests via {@link Clock#fixed(Instant, java.time.ZoneId)}.
 * Overloads without a {@link Clock} argument use {@link Clock#systemUTC()}.
 *
 * <p>{@code optional*} variants pass a {@code null} value through without validation.
 *
 * @see LocalDateTimes
 * @see OffsetDateTimes
 * @see ZonedDateTimes
 */
public final class Instants {
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String TOLERANCE = "tolerance";
    private static final String CLOCK = "clock";

    private Instants() {
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
    public static Instant notNull(String name, Instant value) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is before {@code min}.
     */
    public static Instant min(String name, Instant value, Instant min) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is after {@code max}.
     */
    public static Instant max(String name, Instant value, Instant max) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code min} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static Instant minExclusive(String name, Instant value, Instant min) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code max} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static Instant maxExclusive(String name, Instant value, Instant max) {
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
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is before {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws InstantOutsideRangeException  if {@code value} is outside [{@code min}, {@code max}].
     */
    public static Instant range(String name, Instant value, Instant min, Instant max) {
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
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly after {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws InstantOutsideRangeException  if {@code value} is outside ({@code min}, {@code max}).
     */
    public static Instant exclusiveRange(String name, Instant value, Instant min, Instant max) {
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
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly after {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws InstantOutsideRangeException  if {@code value} is outside [{@code min}, {@code max}).
     */
    public static Instant rangeExclusiveMax(String name, Instant value, Instant min, Instant max) {
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
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly after {@code min}.
     * @throws NullValueException            if {@code value} is null.
     * @throws InstantOutsideRangeException  if {@code value} is outside ({@code min}, {@code max}].
     */
    public static Instant rangeExclusiveMin(String name, Instant value, Instant min, Instant max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly before the current instant.
     * Uses {@link Clock#systemUTC()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or after the current instant.
     */
    public static Instant past(String name, Instant value) {
        return past(name, value, Clock.systemUTC());
    }

    /**
     * Validates that {@code value} is not null and is strictly before the instant reported by
     * {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or after the current instant.
     */
    public static Instant past(String name, Instant value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isBefore(Instant.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or before the current instant.
     * Uses {@link Clock#systemUTC()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is after the current instant.
     */
    public static Instant pastOrPresent(String name, Instant value) {
        return pastOrPresent(name, value, Duration.ZERO, Clock.systemUTC());
    }

    /**
     * Validates that {@code value} is not null and is at or before the instant reported by
     * {@code clock}. No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is after the current instant.
     */
    public static Instant pastOrPresent(String name, Instant value, Clock clock) {
        return pastOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value} is not null and is at or before {@code now + tolerance},
     * where {@code now} is determined by {@link Clock#systemUTC()}.
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static Instant pastOrPresent(String name, Instant value, Duration tolerance) {
        return pastOrPresent(name, value, tolerance, Clock.systemUTC());
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
     * @param clock     The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance}
     *                                      or {@code clock} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static Instant pastOrPresent(String name, Instant value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isAfter(Instant.now(clock).plus(tolerance))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is at or after the current instant.
     * Uses {@link Clock#systemUTC()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is before the current instant.
     */
    public static Instant futureOrPresent(String name, Instant value) {
        return futureOrPresent(name, value, Duration.ZERO, Clock.systemUTC());
    }

    /**
     * Validates that {@code value} is not null and is at or after the instant reported by
     * {@code clock}. No clock-drift tolerance is applied.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is before the current instant.
     */
    public static Instant futureOrPresent(String name, Instant value, Clock clock) {
        return futureOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value} is not null and is at or after {@code now - tolerance},
     * where {@code now} is determined by {@link Clock#systemUTC()}.
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static Instant futureOrPresent(String name, Instant value, Duration tolerance) {
        return futureOrPresent(name, value, tolerance, Clock.systemUTC());
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
     * @param clock     The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance}
     *                                      or {@code clock} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static Instant futureOrPresent(String name, Instant value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (value.isBefore(Instant.now(clock).minus(tolerance))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value} is not null and is strictly after the current instant.
     * Uses {@link Clock#systemUTC()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or before the current instant.
     */
    public static Instant future(String name, Instant value) {
        return future(name, value, Clock.systemUTC());
    }

    /**
     * Validates that {@code value} is not null and is strictly after the instant reported by
     * {@code clock}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws NullValueException           if {@code value} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or before the current instant.
     */
    public static Instant future(String name, Instant value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) throw nullValue(name);
        if (!value.isAfter(Instant.now(clock))) throw notInFuture(name);

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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code min} is null.
     * @throws InstantOutsideRangeException if {@code value} is before {@code min}.
     */
    public static Instant optionalMin(String name, Instant value, Instant min) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code max} is null.
     * @throws InstantOutsideRangeException if {@code value} is after {@code max}.
     */
    public static Instant optionalMax(String name, Instant value, Instant max) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code min} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or before {@code min}.
     */
    public static Instant optionalMinExclusive(String name, Instant value, Instant min) {
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
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code max} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or after {@code max}.
     */
    public static Instant optionalMaxExclusive(String name, Instant value, Instant max) {
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
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is before {@code min}.
     * @throws InstantOutsideRangeException  if {@code value} is outside [{@code min}, {@code max}].
     */
    public static Instant optionalRange(String name, Instant value, Instant min, Instant max) {
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
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly after {@code min}.
     * @throws InstantOutsideRangeException  if {@code value} is outside ({@code min}, {@code max}).
     */
    public static Instant optionalExclusiveRange(String name, Instant value, Instant min, Instant max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within [{@code min}, {@code max}),
     * inclusive minimum, exclusive maximum. A null {@code value} is considered valid and returned
     * as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The inclusive lower bound; must not be null.
     * @param max   The exclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly after {@code min}.
     * @throws InstantOutsideRangeException  if {@code value} is outside [{@code min}, {@code max}).
     */
    public static Instant optionalRangeExclusiveMax(String name, Instant value, Instant min, Instant max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (value.isBefore(min)) throw tooSmall(name, min);
        if (!value.isBefore(max)) throw tooLargeExclusive(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, falls within ({@code min}, {@code max}],
     * exclusive minimum, inclusive maximum. A null {@code value} is considered valid and returned
     * as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param min   The exclusive lower bound; must not be null.
     * @param max   The inclusive upper bound; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException          if {@code name} is null or blank, or if {@code min} or {@code max} is null.
     * @throws IllegalConfigurationException if {@code max} is not strictly after {@code min}.
     * @throws InstantOutsideRangeException  if {@code value} is outside ({@code min}, {@code max}].
     */
    public static Instant optionalRangeExclusiveMin(String name, Instant value, Instant min, Instant max) {
        Throws.ifInvalidName(name);
        throwIfExclusiveBoundsInvalid(min, max);

        if (null == value) return null;
        if (!value.isAfter(min)) throw tooSmallExclusive(name, min);
        if (value.isAfter(max)) throw tooLarge(name, max);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly before the current instant.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemUTC()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws InstantOutsideRangeException if {@code value} is at or after the current instant.
     */
    public static Instant optionalPast(String name, Instant value) {
        return optionalPast(name, value, Clock.systemUTC());
    }

    /**
     * Validates that {@code value}, if present, is strictly before the instant reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or after the current instant.
     */
    public static Instant optionalPast(String name, Instant value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isBefore(Instant.now(clock))) throw notInPast(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or before the current instant.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemUTC()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws InstantOutsideRangeException if {@code value} is after the current instant.
     */
    public static Instant optionalPastOrPresent(String name, Instant value) {
        return optionalPastOrPresent(name, value, Duration.ZERO, Clock.systemUTC());
    }

    /**
     * Validates that {@code value}, if present, is at or before the instant reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws InstantOutsideRangeException if {@code value} is after the current instant.
     */
    public static Instant optionalPastOrPresent(String name, Instant value, Clock clock) {
        return optionalPastOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value}, if present, is at or before {@code now + tolerance}.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemUTC()} for the current time.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param tolerance The maximum amount by which the caller's clock may be ahead of the
     *                  server clock; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws InstantOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static Instant optionalPastOrPresent(String name, Instant value, Duration tolerance) {
        return optionalPastOrPresent(name, value, tolerance, Clock.systemUTC());
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
     * @param clock     The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance}
     *                                      or {@code clock} is null.
     * @throws InstantOutsideRangeException if {@code value} is after {@code now + tolerance}.
     */
    public static Instant optionalPastOrPresent(String name, Instant value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isAfter(Instant.now(clock).plus(tolerance))) throw notPastOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is at or after the current instant.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemUTC()} and no clock-drift tolerance.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws InstantOutsideRangeException if {@code value} is before the current instant.
     */
    public static Instant optionalFutureOrPresent(String name, Instant value) {
        return optionalFutureOrPresent(name, value, Duration.ZERO, Clock.systemUTC());
    }

    /**
     * Validates that {@code value}, if present, is at or after the instant reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws InstantOutsideRangeException if {@code value} is before the current instant.
     */
    public static Instant optionalFutureOrPresent(String name, Instant value, Clock clock) {
        return optionalFutureOrPresent(name, value, Duration.ZERO, clock);
    }

    /**
     * Validates that {@code value}, if present, is at or after {@code now - tolerance}.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemUTC()} for the current time.
     *
     * @param name      The name of the argument being validated, used in exception messages.
     * @param value     The value to validate, may be null.
     * @param tolerance The maximum amount by which the caller's clock may be behind the
     *                  server clock; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance} is null.
     * @throws InstantOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static Instant optionalFutureOrPresent(String name, Instant value, Duration tolerance) {
        return optionalFutureOrPresent(name, value, tolerance, Clock.systemUTC());
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
     * @param clock     The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code tolerance}
     *                                      or {@code clock} is null.
     * @throws InstantOutsideRangeException if {@code value} is before {@code now - tolerance}.
     */
    public static Instant optionalFutureOrPresent(String name, Instant value, Duration tolerance, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(TOLERANCE, tolerance);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (value.isBefore(Instant.now(clock).minus(tolerance))) throw notFutureOrPresent(name);

        return value;
    }

    /**
     * Validates that {@code value}, if present, is strictly after the current instant.
     * A null {@code value} is considered valid and returned as {@code null}.
     * Uses {@link Clock#systemUTC()} for the current time.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank.
     * @throws InstantOutsideRangeException if {@code value} is at or before the current instant.
     */
    public static Instant optionalFuture(String name, Instant value) {
        return optionalFuture(name, value, Clock.systemUTC());
    }

    /**
     * Validates that {@code value}, if present, is strictly after the instant reported by
     * {@code clock}. A null {@code value} is considered valid and returned as {@code null}.
     *
     * @param name  The name of the argument being validated, used in exception messages.
     * @param value The value to validate, may be null.
     * @param clock The clock used to determine the current instant; must not be null.
     * @return The {@code value} unchanged, or {@code null} if {@code value} is null.
     * @throws NullPointerException         if {@code name} is null or blank, or if {@code clock} is null.
     * @throws InstantOutsideRangeException if {@code value} is at or before the current instant.
     */
    public static Instant optionalFuture(String name, Instant value, Clock clock) {
        Throws.ifInvalidName(name);
        Throws.ifNull(CLOCK, clock);

        if (null == value) return null;
        if (!value.isAfter(Instant.now(clock))) throw notInFuture(name);

        return value;
    }

    private static void throwIfExclusiveBoundsInvalid(Instant min, Instant max) {
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

    private static InstantOutsideRangeException tooSmall(String name, Instant min) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than or equal to '%s'.",
                        name,
                        min
                )
        );
    }

    private static InstantOutsideRangeException tooSmallExclusive(String name, Instant bound) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be greater than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static InstantOutsideRangeException tooLarge(String name, Instant max) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than or equal to '%s'.",
                        name,
                        max
                )
        );
    }

    private static InstantOutsideRangeException tooLargeExclusive(String name, Instant bound) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be less than '%s'.",
                        name,
                        bound
                )
        );
    }

    private static InstantOutsideRangeException notInPast(String name) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past.",
                        name
                )
        );
    }

    private static InstantOutsideRangeException notPastOrPresent(String name) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the past or the present.",
                        name
                )
        );
    }

    private static InstantOutsideRangeException notFutureOrPresent(String name) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future or the present.",
                        name
                )
        );
    }

    private static InstantOutsideRangeException notInFuture(String name) {
        return new InstantOutsideRangeException(
                String.format(
                        "The '%s' value is invalid. The value must be in the future.",
                        name
                )
        );
    }

    private static IllegalConfigurationException maxLtMin(Instant max, Instant min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than or equal to the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }

    private static IllegalConfigurationException maxLeMin(Instant max, Instant min) {
        return new IllegalConfigurationException(
                String.format(
                        "The 'max' value of '%s' is invalid. The value must be greater than the 'min' value of '%s'.",
                        max,
                        min
                )
        );
    }
}

