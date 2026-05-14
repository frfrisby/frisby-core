# Field Groups Guide

`FieldGroups` validates **cardinality constraints across a group of related fields** — rules
like "at least one of these must be provided" or "exactly one may be set at a time". These
constraints arise constantly in real APIs and are awkward to express with per-field
validation alone.

---

## The Problem

Consider an API that accepts a notification preference. The caller must supply *exactly one*
of an email address, a phone number, or a webhook URL:

```java
public void updateNotificationPreference(
        String email,
        String phone,
        String webhookUrl) {

    // This is what it looks like without FieldGroups:
    int provided = (email != null ? 1 : 0)
                 + (phone != null ? 1 : 0)
                 + (webhookUrl != null ? 1 : 0);

    if (provided == 0) {
        throw new IllegalArgumentException(
                "At least one of email, phone, or webhookUrl must be provided.");
    }
    if (provided > 1) {
        throw new IllegalArgumentException(
                "Only one of email, phone, or webhookUrl may be provided.");
    }
    // ...
}
```

With `FieldGroups` this collapses to a single line — and the exception message names the
fields automatically:

```java
private static final FieldGroup NOTIFICATION_FIELDS =
        FieldGroup.of("email", "phone", "webhookUrl");

public void updateNotificationPreference(
        String email,
        String phone,
        String webhookUrl) {

    FieldGroups.onlyOne(NOTIFICATION_FIELDS, email, phone, webhookUrl);
    // ...
}
```

---

## FieldGroup — The Descriptor

`FieldGroup` is an immutable, ordered list of field names. It carries no values — it is
a pure name-holder that maps positionally to the value arguments you pass to `FieldGroups`
methods.

```java
FieldGroup group = FieldGroup.of("firstName", "lastName", "email");
```

Rules:
- Minimum of two names.
- Each name must be non-null and non-blank.
- Names must be unique within the group.

Because a class's fields are fixed at compile time, **always declare `FieldGroup` instances
as `private static final`**. Construction happens once at class initialization; the
`FieldGroups` call itself allocates nothing:

```java
// Correct — constructed once
private static final FieldGroup CONTACT_FIELDS =
        FieldGroup.of("email", "phone", "webhookUrl");

// Wrong — constructs a new FieldGroup on every call
FieldGroups.onlyOne(FieldGroup.of("email", "phone", "webhookUrl"), email, phone, webhookUrl);
```

---

## Constraints

### `atLeastOne` — one or more fields must be provided

Use when providing multiple values is valid, but providing none is not.

```java
private static final FieldGroup SEARCH_FIELDS =
        FieldGroup.of("name", "email", "customerId");

// At least one search criterion must be supplied
FieldGroups.atLeastOne(SEARCH_FIELDS, name, email, customerId);
```

**Throws** `MissingFieldException` if all values are null.

**Example message:**
```
At least one of the 'name', 'email', or 'customerId' fields must be provided.
```

---

### `onlyOne` — exactly one field must be provided

Use when exactly one choice is required and multiple choices are mutually exclusive.

```java
private static final FieldGroup PAYMENT_FIELDS =
        FieldGroup.of("creditCardToken", "bankAccountId", "walletId");

// Caller must supply exactly one payment method
FieldGroups.onlyOne(PAYMENT_FIELDS, creditCardToken, bankAccountId, walletId);
```

**Throws** `MissingFieldException` if none are provided; `TooManyFieldsException` if
more than one is provided.

**Example messages:**
```
Only one of the 'creditCardToken' or 'bankAccountId' fields may be provided.
```

---

### `atMostOne` — zero or one field may be provided

Use when a field is optional, but if one is provided then the others must not be.

```java
private static final FieldGroup OVERRIDE_FIELDS =
        FieldGroup.of("fixedPrice", "discountPercent");

// A product may have either a fixed price override or a discount, but not both
FieldGroups.atMostOne(OVERRIDE_FIELDS, fixedPrice, discountPercent);
```

**Throws** `TooManyFieldsException` if more than one value is non-null. Does not throw
if all values are null.

**Example message:**
```
At most one of the 'fixedPrice' or 'discountPercent' fields may be provided.
```

---

### `noneOrAll` — either all fields are provided or none are

Use for groups of fields that form a logical unit and must be supplied together.

```java
private static final FieldGroup ADDRESS_FIELDS =
        FieldGroup.of("street", "city", "postalCode", "country");

// An address is only meaningful if all components are present
FieldGroups.noneOrAll(ADDRESS_FIELDS, street, city, postalCode, country);
```

**Throws** `MissingFieldException` if some but not all values are provided. Does not
throw if all are null or all are non-null.

**Example message:**
```
The 'street', 'city', 'postalCode', and 'country' fields must all be provided together, or none at all.
```

---

## Combining Validators

`FieldGroups` pairs naturally with the field-level validators. Validate the group
constraint first, then validate each individual value:

```java
private static final FieldGroup SCHEDULE_FIELDS =
        FieldGroup.of("startsAt", "endsAt");

public void schedule(OffsetDateTime startsAt, OffsetDateTime endsAt) {
    FieldGroups.noneOrAll(SCHEDULE_FIELDS, startsAt, endsAt);

    if (null != startsAt) {
        startsAt = OffsetDateTimes.future("startsAt", startsAt);
        endsAt   = OffsetDateTimes.min("endsAt", endsAt, startsAt);
    }
}
```

---

## Arity

Each constraint method has explicit overloads for 2–10 fields, plus a varargs fallback
for groups larger than 10. The explicit overloads are preferred — they are resolved at
compile time and allocate no array.

```java
// Explicit overload (preferred for groups of 10 or fewer)
FieldGroups.onlyOne(PAYMENT_FIELDS, creditCardToken, bankAccountId, walletId);

// Varargs fallback (use only when the group exceeds 10 fields)
FieldGroups.onlyOne(LARGE_GROUP, values);
```

