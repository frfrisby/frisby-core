# Validation Guide

This guide covers all validator classes in the `software.frisby.core.validation` package.

---

## The `name` Parameter

Every validation method takes a `name` as its first argument. This is the logical name of
the value being validated — typically the parameter or field name — and it appears verbatim
in exception messages:

```java
Strings.notBlank("username", username);
// throws: "The 'username' value is invalid. The value must not be blank."

Numbers.min("retryCount", retryCount, 1);
// throws: "The 'retryCount' value of '0' is invalid. The value must be greater than or equal to '1'."
```

Passing a null or blank `name` throws `NullPointerException` immediately. This is
intentional — a missing name is always a programming error, not a value error.

---

## The `optional*` Pattern

Every constraint method has a corresponding `optional*` variant. Optional methods pass
`null` through unchanged and only apply the constraint when the value is non-null. This
is the correct tool for validating fields that are genuinely optional in your domain:

```java
// Required — throws NullValueException if null
String title = Strings.notBlankWithMaxLength("title", title, 200);

// Optional — null is accepted; non-null values are still constrained
String subtitle = Strings.optionalNotBlankWithMaxLength("subtitle", subtitle, 200);
```

---

## Strings

**Class:** `Strings`

Length is measured in **Unicode code points**, not Java `char` values. This ensures
correct behavior for supplementary characters (emoji, certain CJK characters, etc.) that
occupy two `char` values in a Java `String`.

### Null / empty / blank

```java
String value = Strings.notNull("field", value);    // rejects null
String value = Strings.notEmpty("field", value);   // rejects null and ""
String value = Strings.notBlank("field", value);   // rejects null, "", and whitespace-only
```

### Length constraints

```java
// Upper bound only
String value = Strings.maxLength("field", value, 100);

// Lower bound only
String value = Strings.minLength("field", value, 2);

// Both bounds
String value = Strings.length("field", value, 2, 100);

// Exact length
String value = Strings.length("field", value, 8);
```

### Combined not-blank + length

```java
String value = Strings.notBlankWithMaxLength("field", value, 100);
String value = Strings.notBlankWithMinLength("field", value, 2);
String value = Strings.notBlankWithLength("field", value, 2, 100);
```

### Pattern matching

```java
private static final Pattern EMAIL = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

// Validates not-null and matches pattern
String value = Strings.notNullWithMatches("email", value, EMAIL);

// Validates not-blank and matches pattern
String value = Strings.notBlankWithMatches("email", value, EMAIL);

// Combined not-blank, max length, and pattern
String value = Strings.notBlankWithMaxLengthAndMatches("email", value, 254, EMAIL);
```

---

## TrimmedStrings

**Class:** `TrimmedStrings`

Identical contract to `Strings`, but leading and trailing whitespace is trimmed before
length constraints are applied. The trimmed value is returned. Use this when you want to
normalize whitespace and validate length in a single call:

```java
// " hello " passes a maxLength of 5 and returns "hello"
String value = TrimmedStrings.notBlankWithMaxLength("field", value, 5);
```

---

## Numbers

**Class:** `Numbers`

Supports eight numeric types: `int`/`Integer`, `long`/`Long`, `short`/`Short`,
`byte`/`Byte`, `float`/`Float`, `double`/`Double`, `BigDecimal`, `BigInteger`.

Primitive and boxed overloads exist for all constraint methods on the first six types.
`BigDecimal` and `BigInteger` accept only boxed values (with an explicit null check).

`float` and `double` comparisons use negated-complement logic (`!(value >= min)`) so
that `NaN` correctly fails every bound check.

### Constraint families

| Method | Constraint |
|--------|-----------|
| `min(name, value, min)` | `value >= min` |
| `max(name, value, max)` | `value <= max` |
| `minExclusive(name, value, min)` | `value > min` |
| `maxExclusive(name, value, max)` | `value < max` |
| `range(name, value, min, max)` | `min <= value <= max` |
| `exclusiveRange(name, value, min, max)` | `min < value < max` |
| `rangeExclusiveMin(name, value, min, max)` | `min < value <= max` |
| `rangeExclusiveMax(name, value, min, max)` | `min <= value < max` |
| `positive(name, value)` | `value > 0` |
| `notNegative(name, value)` | `value >= 0` |

### Examples

```java
int    count    = Numbers.positive("count", count);
int    scale    = Numbers.notNegative("scale", scale);
long   offset   = Numbers.range("offset", offset, 0L, Long.MAX_VALUE);
double ratio    = Numbers.exclusiveRange("ratio", ratio, 0.0, 1.0);

BigDecimal price = Numbers.positive("price", price);

// Optional — passes null through
Integer page = Numbers.optionalNotNegative("page", page);
```

---

## Durations

**Class:** `Durations`

Supports the eight range constraint families from `Numbers` plus two sign constraints:

```java
Duration timeout = Durations.positive("timeout", timeout);
// rejects zero and negative durations

Duration delay = Durations.notNegative("delay", delay);
// accepts zero and positive durations; rejects negative

Duration window = Durations.range("window", window,
        Duration.ofSeconds(1),
        Duration.ofMinutes(5));
```

---

## Periods

**Class:** `Periods`

`Period` has no natural total ordering, so range constraints are not provided. Two sign
constraints are available:

```java
Period tenure = Periods.positive("tenure", tenure);
// rejects zero and any period with a negative component

Period grace = Periods.notNegative("grace", grace);
// rejects any period with a negative component
```

> **Note:** `Period.isNegative()` returns `true` when *any single component* is negative.
> Mixed-sign periods such as `Period.of(1, -1, 0)` are therefore treated as negative.

---

## Temporal Validators

Seven classes cover the standard `java.time` types. All follow the same constraint
structure as `Numbers`.

| Class | Type | Range family | Clock-relative family |
|-------|------|:---:|:---:|
| `Instants` | `Instant` | ✓ | ✓ |
| `LocalDates` | `LocalDate` | ✓ | ✓ |
| `LocalDateTimes` | `LocalDateTime` | ✓ | ✓ |
| `LocalTimes` | `LocalTime` | ✓ | — |
| `OffsetDateTimes` | `OffsetDateTime` | ✓ | ✓ |
| `OffsetTimes` | `OffsetTime` | ✓ | — |
| `ZonedDateTimes` | `ZonedDateTime` | ✓ | ✓ |

### Range constraints

All eight families are available on every type:

```java
Instant start = Instants.min("start", start, Instant.EPOCH);

LocalDate dob = LocalDates.max("dob", dob, LocalDate.now());

OffsetDateTime scheduledAt = OffsetDateTimes.range("scheduledAt", scheduledAt,
        OffsetDateTime.now(),
        OffsetDateTime.now().plusYears(1));
```

### Clock-relative constraints

Available on `Instants`, `LocalDates`, `LocalDateTimes`, `OffsetDateTimes`, and
`ZonedDateTimes`.

```java
// past — strictly before now
Instant recordedAt = Instants.past("recordedAt", recordedAt);

// future — strictly after now
LocalDate expiresOn = LocalDates.future("expiresOn", expiresOn);

// pastOrPresent / futureOrPresent — inclusive of now
Instant eventTime = Instants.pastOrPresent("eventTime", eventTime);
```

All clock-relative methods accept an explicit `Clock` for deterministic testing:

```java
Instant eventTime = Instants.past("eventTime", eventTime, clock);
```

### Tolerance overloads

`Instants`, `LocalDateTimes`, `OffsetDateTimes`, and `ZonedDateTimes` carry sub-second
precision. To accommodate clock drift between distributed systems, `pastOrPresent` and
`futureOrPresent` accept an optional `Duration` tolerance:

```java
// Accepts values up to 2 seconds *ahead* of the server clock.
// Useful when validating timestamps from remote callers whose clocks may be slightly fast.
Instant ts = Instants.pastOrPresent("ts", ts, Duration.ofSeconds(2));

// Accepts values up to 2 seconds *behind* the server clock.
Instant ts = Instants.futureOrPresent("ts", ts, Duration.ofSeconds(2));

// Full form — explicit tolerance and clock
Instant ts = Instants.pastOrPresent("ts", ts, Duration.ofSeconds(2), clock);
```

---

## Values

**Class:** `Values`

Generic single-value constraints for any type `T`.

```java
// Null check
Status status = Values.notNull("status", status);

// Membership — value must be in the allowed set
private static final Set<String> ROLES = Set.of("admin", "editor", "viewer");
String role = Values.oneOf("role", role, ROLES);

// Exclusion — value must not be in the disallowed set
private static final Set<String> RESERVED = Set.of("root", "system");
String username = Values.notOneOf("username", username, RESERVED);

// Optional variants pass null through
String role = Values.optionalOneOf("role", role, ROLES);
```

---

## Sequences

**Class:** `Sequences`

Validates `Collection<T>` and `T[]` values. Both overloads are provided for every method.

```java
// Null / empty checks
List<String> tags = Sequences.notNull("tags", tags);
List<String> tags = Sequences.notEmpty("tags", tags);

// Size constraints
List<String> tags = Sequences.minSize("tags", tags, 1);
List<String> tags = Sequences.maxSize("tags", tags, 10);
List<String> tags = Sequences.size("tags", tags, 1, 10);

// Duplicate detection — uses equals() by default
List<String> ids = Sequences.noDuplicates("ids", ids);

// Duplicate detection with a key extractor
List<User> users = Sequences.noDuplicates("users", users, User::getId);

// Array overload
String[] tags = Sequences.notEmpty("tags", tags);
```

---

## Maps

**Class:** `Maps`

Validates `Map<K, V>` values.

```java
Map<String, String> headers = Maps.notNull("headers", headers);
Map<String, String> headers = Maps.notEmpty("headers", headers);
Map<String, String> headers = Maps.size("headers", headers, 1, 50);
```

---

## Exception Hierarchy

All value-failure exceptions extend `IllegalArgumentException`. Every exception type
carries a descriptive message identifying the argument by name and stating the constraint
that was violated.

| Exception | Thrown when |
|-----------|-------------|
| `NullValueException` | value is null |
| `EmptyValueException` | string or collection is empty |
| `BlankValueException` | string is blank |
| `StringLengthOutsideRangeException` | string length fails a constraint |
| `PatternMismatchException` | string does not match a pattern |
| `NumericValueOutsideRangeException` | numeric value fails a constraint |
| `DurationOutsideRangeException` | Duration fails a constraint |
| `PeriodOutsideRangeException` | Period fails a sign constraint |
| `InstantOutsideRangeException` | Instant fails a constraint |
| `LocalDateOutsideRangeException` | LocalDate fails a constraint |
| `LocalDateTimeOutsideRangeException` | LocalDateTime fails a constraint |
| `LocalTimeOutsideRangeException` | LocalTime fails a constraint |
| `OffsetDateTimeOutsideRangeException` | OffsetDateTime fails a constraint |
| `OffsetTimeOutsideRangeException` | OffsetTime fails a constraint |
| `ZonedDateTimeOutsideRangeException` | ZonedDateTime fails a constraint |
| `DisallowedValueException` | value is in a disallowed set |
| `SequenceSizeOutsideRangeException` | collection/array size fails a constraint |
| `DuplicateElementsException` | collection/array contains duplicates |
| `MapSizeOutsideRangeException` | map size fails a constraint |
| `IllegalConfigurationException` | a bound or configuration argument is invalid (API misuse) |

`NullPointerException` is thrown (not `NullValueException`) when the `name` parameter
itself is null or blank, or when a required bound argument is null. These indicate
programming errors, not invalid input values.

