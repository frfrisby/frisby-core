# Util Module Guide

This guide covers all utility classes in the `software.frisby.core.util` package.

---

## StopWatch

`StopWatch` measures elapsed time with nanosecond precision.  It is thread-safe and
designed for the common pattern of timing a single operation and then reading the result.

### Creating and stopping

A `StopWatch` is created in a running state — there is no separate start call:

```java
StopWatch watch = StopWatch.start();

doSomething();

watch.stop();
Duration elapsed = watch.duration();
```

Calling `stop()` is a one-way transition.  A stopped `StopWatch` cannot be restarted;
create a new instance to time a subsequent operation.

### Thread safety

`stop()` uses `AtomicLong.compareAndSet` internally.  If two threads call `stop()`
concurrently, exactly one wins and records the stop time — the other's call is a no-op.
The duration recorded at the first `stop()` call is always preserved.

### Live vs. frozen duration

While the stopwatch is running, every call to `duration()` returns the time elapsed
since `start()` — the value increases on each call.  Once stopped, `duration()` returns
the same frozen value on every subsequent call.

This makes it safe to check elapsed time inside a loop before stopping:

```java
StopWatch watch = StopWatch.start();
while (hasMoreWork()) {
    if (watch.duration().compareTo(TIMEOUT) > 0) {
        throw new TimeoutException("Exceeded " + TIMEOUT);
    }
    processNext();
}
watch.stop();
```

### What StopWatch does not do

- It does not restart.  Create a new instance for each measurement.
- It does not pause.  There is no `pause()`/`resume()` mechanism.
- It does not throw.  `stop()` and `duration()` are always safe to call in any order.

---

## Decimals

`Decimals` provides correct conversions between floating-point values and `BigDecimal`.

### The problem it solves

Constructing a `BigDecimal` from a `double` or `float` via `new BigDecimal(value)`
captures the exact binary floating-point representation, which is rarely the decimal
value you intended:

```java
new BigDecimal(0.1)
// → 0.1000000000000000055511151231257827021181583404541015625
```

`Decimals` routes all floating-point conversions through `String.valueOf()` and then
`new BigDecimal(String)`, which produces the shortest decimal that round-trips back to
the same floating-point value — the value you actually typed:

```java
Decimals.of(0.1)   // → 0.1
Decimals.of(19.99) // → 19.99
```

**Always use `Decimals.of(double/float)` rather than `new BigDecimal(double/float)`.**

### Converting from floating-point

```java
BigDecimal a = Decimals.of(19.99);                             // 19.99
BigDecimal b = Decimals.of(19.995, 2);                         // 19.99  (RoundingMode.DOWN)
BigDecimal c = Decimals.of(19.995, 2, RoundingMode.HALF_UP);   // 20.00
```

Float overloads follow the same pattern:

```java
BigDecimal d = Decimals.of(1.5f);          // 1.5
BigDecimal e = Decimals.of(1.5f, 0);       // 1    (DOWN)
```

### Parsing from String

```java
BigDecimal f = Decimals.parse("3.14159");                          // 3.14159
BigDecimal g = Decimals.parse("3.14159", 2);                       // 3.14   (DOWN)
BigDecimal h = Decimals.parse("3.14159", 2, RoundingMode.HALF_UP); // 3.14
```

### Formatting to String

All formatting methods produce plain decimal strings with no exponent notation and
trailing zeros stripped, making output suitable for display, logging, and persistence:

```java
Decimals.toString(Decimals.of(1234567.89))                  // "1234567.89"
Decimals.toString(Decimals.of(1234567.89), 1)               // "1234567.8"
Decimals.toString(Decimals.of(1234567.89), 1, HALF_UP)      // "1234567.9"
```

### Default rounding mode

All overloads that accept a `scale` but no `RoundingMode` use `RoundingMode.DOWN`
(truncation toward zero).  Pass an explicit `RoundingMode` when different behavior is
required.

---

## Uris and HttpUris

The `Uris` and `HttpUris` classes are a pair.  Understanding when to use each is the
key to using them correctly.

### Choosing the right class

| Use case                                                 | Class      |
|----------------------------------------------------------|------------|
| Strip or replace path, query, fragment                   | `Uris`     |
| Parse a URI string with validation-convention exceptions | `Uris`     |
| Extract the user or password from a credential URI       | `Uris`     |
| Extract the HTTP origin (CORS, CSP, cookie domain)       | `HttpUris` |
| Resolve the effective port for an HTTP/S URI             | `HttpUris` |
| Upgrade `http` to `https`                                | `HttpUris` |
| Check the scheme                                         | `HttpUris` |

**`Uris`** is scheme-agnostic.  Its methods work with any hierarchical URI — `https`,
`http`, `ftp`, `mongodb`, `postgresql`, and so on — and make no assumptions about the
protocol.

**`HttpUris`** carries HTTP semantics.  Every method validates that the URI scheme is
`http` or `https` before doing anything, and its operations reflect HTTP-specific
meaning — default port suppression, RFC 6454 origin extraction, scheme upgrading.

### Parsing URI strings

`URI.create(string)` and `new URI(string)` are the standard Java entry points, but they
throw a checked `URISyntaxException` or a bare `IllegalArgumentException` with an
unhelpful message.  `Uris.parse` provides the same functionality with
project-standard exceptions:

```java
// Throws NullValueException, BlankValueException, or IllegalArgumentException
// (wrapping URISyntaxException) with a consistent message format.
URI uri = Uris.parse(connectionString);
```

### Path manipulation

All four path-manipulation methods drop the query string and fragment.  This is
intentional — when you navigate to a new path or strip the path entirely, leftover
query parameters from the previous path are rarely meaningful.

```java
URI api = URI.create("https://example.com:8443/api/v1/users?page=2");

Uris.withoutPath(api);             // https://example.com:8443
Uris.withPath(api, "/health");     // https://example.com:8443/health
Uris.appendPath(api, "profile");   // https://example.com:8443/api/v1/users/profile
Uris.withoutQuery(api);            // https://example.com:8443/api/v1/users
```

`appendPath` normalizes the slash boundary automatically — it does not matter whether
the existing path ends with `/` or whether the segment starts with `/`:

```java
Uris.appendPath(URI.create("https://example.com/api/v1/"),  "users"); // .../api/v1/users
Uris.appendPath(URI.create("https://example.com/api/v1"),  "/users"); // .../api/v1/users
Uris.appendPath(URI.create("https://example.com/api/v1/"), "/users"); // .../api/v1/users
```

### Extracting user and password

For URIs that carry credentials in the userinfo component, `Uris.user` and
`Uris.password` return the decoded components:

```java
URI db = Uris.parse("mongodb://frank:secret@localhost:27017/admin?timeoutMS=5000");

Uris.user(db);     // "frank"
Uris.password(db); // "secret"
```

Both methods return `null` when the URI has no userinfo:

```java
Uris.user(URI.create("https://example.com/path"));     // null
Uris.password(URI.create("https://example.com/path")); // null
```

Passwords that contain colons are handled correctly — only the first `:` in the
userinfo is treated as the user/password separator:

```java
URI uri = Uris.parse("postgresql://frank:pa:ss@localhost/mydb");
Uris.user(uri);     // "frank"
Uris.password(uri); // "pa:ss"
```

### HTTP origin extraction

`HttpUris.origin` implements the origin concept defined in RFC 6454.  It is the correct
method to use for CORS validation, CSP policy generation, and cookie domain matching
because it normalizes the URI to exactly the form that browsers and HTTP clients use for
security comparisons — scheme, host, and port, with default ports suppressed:

```java
HttpUris.origin(URI.create("https://example.com:443/path")); // https://example.com
HttpUris.origin(URI.create("https://example.com:8443/api")); // https://example.com:8443
HttpUris.origin(URI.create("http://example.com:80/path"));   // http://example.com
HttpUris.origin(URI.create("http://example.com/path"));      // http://example.com
```

`Uris.withoutPath` is **not** a substitute for `HttpUris.origin` when working with HTTP
URIs.  `withoutPath` performs a structural strip — it preserves the authority verbatim,
including an explicit default port — so `https://example.com:443/path` becomes
`https://example.com:443`, not `https://example.com`.

### Effective port and default port

```java
HttpUris.effectivePort(URI.create("https://example.com/path"));      // 443
HttpUris.effectivePort(URI.create("https://example.com:8443/path")); // 8443
HttpUris.effectivePort(URI.create("http://example.com/path"));       // 80

HttpUris.isDefaultPort(URI.create("https://example.com/path"));       // true
HttpUris.isDefaultPort(URI.create("https://example.com:443/path"));   // true
HttpUris.isDefaultPort(URI.create("https://example.com:8443/path"));  // false
```

### Upgrading to HTTPS

`toHttps` replaces the scheme and preserves everything else — authority, path, query,
and fragment:

```java
HttpUris.toHttps(URI.create("http://example.com/login?next=/dashboard"));
// → https://example.com/login?next=/dashboard

// Already https — returns the same URI instance unchanged
HttpUris.toHttps(URI.create("https://example.com/login"));
// → https://example.com/login  (same object)
```

### Scheme validation

Every `HttpUris` method validates that the URI scheme is `http` or `https`.  Schemes
are compared case-insensitively, so `HTTPS://example.com` is accepted.  A URI with no
scheme (e.g., a relative reference) or a non-HTTP scheme throws an exception:

```java
HttpUris.origin(URI.create("ftp://files.example.com")); // DisallowedValueException
HttpUris.origin(URI.create("/relative/path"));          // NullValueException (no scheme)
```

