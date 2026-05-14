# frisby-core

[![CI](https://github.com/frfrisby/frisby-core/actions/workflows/ci.yml/badge.svg)](https://github.com/frfrisby/frisby-core/actions/workflows/ci.yml)
[![CodeQL](https://github.com/frfrisby/frisby-core/actions/workflows/codeql.yml/badge.svg)](https://github.com/frfrisby/frisby-core/actions/workflows/codeql.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=frfrisby_frisby-core&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=frfrisby_frisby-core)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=frfrisby_frisby-core&metric=coverage)](https://sonarcloud.io/summary/new_code?id=frfrisby_frisby-core)
[![Maven Central](https://img.shields.io/maven-central/v/software.frisby.core/validation)](https://central.sonatype.com/artifact/software.frisby.core/validation)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight Java 17 library providing high-quality validation utilities and asynchronous
pipeline building blocks.

---

## Why frisby-core?

Most validation libraries either pull in a significant dependency graph (Jakarta Bean
Validation, Hibernate Validator) or are too minimal to be genuinely useful (Guava
`Preconditions`, Spring `Assert`). frisby-core occupies the gap between the two:

- **Zero dependencies** — the `validation` module ships with no transitive runtime dependencies.
- **Typed exception hierarchy** — every failure mode has its own exception type
  (`NullValueException`, `BlankValueException`, `NumericValueOutsideRangeException`, etc.).
  Callers can catch specific failure modes and act on them precisely, rather than catching
  a generic `IllegalArgumentException` and guessing at the cause.
- **Comprehensive temporal coverage** — all seven `java.time` types are supported, with
  both range constraints and clock-relative constraints (`past`, `futureOrPresent`, etc.).
  Clock injection is built in from day one for deterministic testing.
- **Field-group validation** — cross-field cardinality constraints (`atLeastOne`, `onlyOne`,
  `atMostOne`, `noneOrAll`) with clear, field-name-aware exception messages. This fills a
  gap that no lightweight library addresses cleanly.
- **Correctness-first** — NaN-safe float/double comparisons, Unicode code-point string
  length (not `char` count), and explicit `Period` mixed-sign semantics.
- **Async pipeline building blocks** — a complete set of composable pipeline stages
  (`BufferBlock`, `BatchBlock`, `GroupBlock`, `RouterBlock`, and more) wired through a
  fluent type-safe API.  Backpressure, adaptive concurrency, fan-out, fan-in, and
  ordered completion are handled by the library so application code stays focused on
  business logic.

---

## Modules

| Module        | Artifact                           | Description                                                                                                       |
|---------------|------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `bom`         | `software.frisby.core:bom`         | Bill of Materials — import for managed dependency versions                                                        |
| `validation`  | `software.frisby.core:validation`  | Validation utilities and exception types; zero runtime dependencies                                               |
| `util`        | `software.frisby.core:util`        | General utilities (`StopWatch`, `Decimals`); depends on `validation`                                              |
| `concurrency` | `software.frisby.core:concurrency` | Asynchronous pipeline building blocks (`BufferBlock`, `BatchBlock`, `RouterBlock`, etc.); depends on `validation` |

Additional modules are planned. The BOM will track all of them.

---

## Installation

Import the BOM in your project's `dependencyManagement` section, then declare dependencies
without a version:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.frisby.core</groupId>
            <artifactId>bom</artifactId>
            <version>1.0.0</version><!-- {x-release-please-version} -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Validation utilities — zero runtime dependencies -->
    <dependency>
        <groupId>software.frisby.core</groupId>
        <artifactId>validation</artifactId>
    </dependency>

    <!-- General utilities (StopWatch, Decimals) — only if needed; pulls in validation transitively -->
    <dependency>
        <groupId>software.frisby.core</groupId>
        <artifactId>util</artifactId>
    </dependency>

    <!-- Async pipeline building blocks — only if needed; pulls in validation transitively -->
    <dependency>
        <groupId>software.frisby.core</groupId>
        <artifactId>concurrency</artifactId>
    </dependency>
</dependencies>
```

---

## Quick Start

Every validation method accepts a `name` parameter — the argument name used in exception
messages — followed by the value and any constraint parameters. A valid value is always
returned unchanged, so calls chain naturally as assignments:

```java
public UserRecord createUser(String username, String email, int age) {
    username = Strings.notBlankWithMaxLength("username", username, 50);
    email    = Strings.notBlankWithMatches("email", email, EMAIL_PATTERN);
    age      = Numbers.range("age", age, 18, 120);

    return new UserRecord(username, email, age);
}
```

If a value fails its constraint an exception is thrown immediately with a descriptive
message identifying the argument by name:

```
The 'age' value of '15' is invalid.  The value must be greater than or equal to '18'.
```

---

## Documentation

| Guide | Description |
|-------|-------------|
| [Validation Guide](docs/validation-guide.md) | All validator classes, constraint families, optional variants, and the exception hierarchy |
| [Field Groups Guide](docs/field-groups-guide.md) | Cross-field cardinality constraints with worked examples |
| [Concurrency Guide](docs/concurrency-guide.md) | Block overview, fluent pipeline API, logging & diagnostics, and `errorOccurredHandler` usage patterns |

---

## AI-Assisted Development

### Validation

A compact AI context reference is provided at [`docs/ai/validation.md`](docs/ai/validation.md),
designed to be attached to an AI assistant's context window when generating validation code.
It covers every validator class with exact call signatures and representative examples —
small enough to load without meaningfully consuming a context budget.

### Concurrency

A compact AI context reference is provided at [`docs/ai/concurrency.md`](docs/ai/concurrency.md),
covering block builder signatures, defaults, routing strategies, the fluent pipeline API,
the completion lifecycle, and common anti-patterns.  Attach it when generating pipeline code.

### Persistent Instructions

If your project uses an AI coding assistant with a persistent instructions file (such as
`.github/copilot-instructions.md`), add the following to tell the assistant about this
library:

```markdown
## Validation

This project uses `software.frisby.core:validation` for argument validation.
When generating validation code, attach `docs/ai/validation.md` from the
frisby-core repository for the full API reference.

Key conventions:
- Every validation method takes `(String name, T value, ...)` and returns `value` unchanged.
- `optional*` variants pass `null` through without validation.
- Declare `FieldGroup` instances as `private static final`.

## Concurrency

This project uses `software.frisby.core:concurrency` for async pipeline blocks.
When generating pipeline code, attach `docs/ai/concurrency.md` from the
frisby-core repository for block types, builder signatures, and wiring conventions.

Key conventions:
- Always prefer the fluent Pipeline.builder() / OpenPipeline.builder() API.
- Asynchronous blocks (Buffer, Batch, Group, PriorityBuffer, Delay) require
  an executor; provide a NamedExecutorService.
- Call complete() then awaitCompletion() before shutting down the executor.
- Use Router for terminal fan-out; use OpenRouter when arm results must merge downstream.
- errorOccurredHandler receives the failed item — use it for dead-lettering and retry.
```

Attaching the relevant guide at the start of a session gives the assistant accurate method
signatures, the correct exception types, and established wiring patterns without requiring
access to the frisby-core source.

---

## Contributing

Contributions are welcome. Please open an issue to discuss significant changes before
submitting a pull request. All PRs must pass CI (Java 17 and 21), CodeQL, and SonarCloud
analysis. Use a [Conventional Commit](https://www.conventionalcommits.org/) prefix on
the PR title (`feat:`, `fix:`, `chore:`, etc.) — this drives automated versioning and
changelog generation.

---

## License

[MIT](LICENSE)

