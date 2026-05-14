# Concurrency Module Guide

This guide covers the `software.frisby.core.concurrency` package — a set of asynchronous
pipeline building blocks for constructing high-throughput, low-latency data-processing
pipelines in Java.

---

## Block Overview

Blocks are the units of a pipeline.  Each block accepts items via `post()`, optionally
transforms or accumulates them, then forwards the result downstream via a linked
`Target`.  Every block is constructed through a static `builder()` method.

| Block | Type | Description |
|---|---|---|
| `SourceBlock<T>` | Async producer | Polls a `Supplier<T>` or `Supplier<List<T>>` on a dedicated thread and pushes items downstream. |
| `BufferBlock<T>` | Async pass-through | Accepts items from posting threads and delivers them to a single downstream target on a dedicated worker thread.  Bounded capacity; posting threads block when full. |
| `PriorityBufferBlock<T>` | Async pass-through | Same as `BufferBlock` but items are delivered in priority order determined by a `Comparator<T>`. |
| `BatchBlock<T>` | Async accumulator | Accumulates individual items into `List<T>` batches and delivers them downstream when the batch is full or a timeout fires. |
| `GroupBlock<T, K>` | Async accumulator | Groups items by a key function into per-key `List<T>` groups and delivers each group downstream when it reaches a size limit, a timeout, or an idle timeout. |
| `DelayBlock<T>` | Async delay | Accepts items and holds each one for a duration computed by a `Function<T, Duration>`, then delivers them downstream after their delay expires. |
| `TapBlock<T>` | Synchronous | Passes each item to a `Consumer<T>` as a side effect, then forwards the original item downstream unchanged.  Consumer exceptions propagate to the posting thread. |
| `TransformBlock<T, R>` | Synchronous | Applies a `Function<T, R>` to each item and forwards the result inline on the posting thread.  A `null` result is silently dropped. |
| `ExpandBlock<T>` | Synchronous | Accepts a `List<T>` and posts each element individually to the downstream target. |
| `BranchBlock<T>` | Synchronous | Routes each item to the first branch whose `Predicate<T>` matches, or to an `otherwise` target. |
| `RouterBlock<T>` | Synchronous | Distributes items across multiple downstream targets using round-robin, load-balanced, sticky-key, or custom routing. |
| `OpenRouterBlock<T>` | Synchronous | Like `RouterBlock` but each arm is an `OpenPipeline<T, R>`; merges all arm tail sources into a single output that can be wired to a shared downstream stage. |
| `BroadcastBlock<T>` | Synchronous | Delivers each item to every registered downstream target. |
| `ActionBlock<T>` | Synchronous terminal | Applies a `Consumer<T>` to each item.  Typically used as the final stage in a pipeline. |

---

## Building Pipelines — Fluent API

The recommended way to assemble stages is the fluent `Pipeline.builder()` /
`OpenPipeline.builder()` API in the `software.frisby.core.concurrency.fluent` package.
It provides compile-time type safety across stage transitions and wires all blocks
automatically.

### Terminal pipeline — `Pipeline<T>`

A `Pipeline<T>` accepts items at its head and routes them through every downstream stage
to a terminal block that consumes them without producing further output.

```java
NamedExecutorService executor = NamedExecutorService.builder()
        .threadPrefix("orders")
        .build();

Pipeline<Order> pipeline = Pipeline.<Order>builder()
        .executor(executor)
        .from(Buffer.of(Order.class)               // async head — decouples posting thread
                .capacity(2048))
        .then(Transform.of(Order.class, Invoice.class)
                .transform(order -> invoiceService.create(order)))
        .to(invoice -> store(invoice));            // terminal Consumer<Invoice>
```

**Key rules:**
- `.from(stage)` sets the head and returns a `Chain`.
- `.then(stage)` appends an intermediate stage; each call changes the current output type.
- `.to(consumer)` / `.to(action)` / `.to(target)` closes the chain and returns the finished `Pipeline`.
- `executor()` is required whenever any stage is async (`Buffer`, `Batch`, `Group`, `Delay`, `PriorityBuffer`).

### Open pipeline — `OpenPipeline<I, O>`

An `OpenPipeline<I, O>` is like `Pipeline` but its tail is not terminated — it exposes a
`Source<O>` that can be linked to a shared downstream stage or fed into an `OpenRouter`.
Build one with `OpenPipeline.builder()` and close the chain with `.build()`:

```java
OpenPipeline<Message, List<Message>> arm = OpenPipeline.builder()
        .executor(executor)
        .from(Buffer.of(Message.class))
        .then(Group.of(Message.class, String.class)
                .groupingFunction(Message::customerId)
                .maxGroupSize(128))
        .build();
```

### Fluent stage reference

All fluent stage builders live in `software.frisby.core.concurrency.fluent`.  Pass a
`Class<T>` (or `GenericType<T>` for generic types such as `List<Message>`) to `of()` for
type inference; the argument is not stored.  `GenericType` is in the root
`software.frisby.core.concurrency` package.

An `ofLists(Class<T>)` shorthand is available on all pass-through and terminal builders
when the item type is itself a list — `Buffer`, `Delay`, `PriorityBuffer`, `Tap`,
`Action`, `Broadcast`, `Router`, `Branch`, `Transform`, and `OpenRouter`:

```java
// Instead of:
Buffer.of(new GenericType<List<Message>>() {})

// Write:
Buffer.ofLists(Message.class)   // → Buffer<List<Message>>
```

| Fluent builder | Stage type | Key configuration method(s) |
|---|---|---|
| `Buffer.of(T.class)` | Async pass-through | `.capacity(int)` (default 1024) |
| `PriorityBuffer.of(T.class)` | Async pass-through | `.capacity(int)`, `.comparator(Comparator<T>)` |
| `Batch.of(T.class)` | Async `T → List<T>` | `.batchSize(int)` (default 128), `.timeout(Duration)` (default 5 s) |
| `Group.of(T.class, K.class)` | Async `T → List<T>` | `.groupingFunction(Function<T,K>)` (required), `.maxGroupSize(int)` (default 128), `.timeout(Duration)` (default 10 s), `.idleTimeout(Duration)` (default 5 s) |
| `Delay.of(T.class)` | Async `T → T` | `.delay(Duration)` or `.delay(Function<T,Duration>)` (required) |
| `Tap.of(T.class)` | Sync side-effect | `.consumer(Consumer<T>)` (required) |
| `Transform.of(T.class, R.class)` | Sync `T → R` | `.transform(Function<T,R>)` (required) |
| `Expand.of(T.class)` | Sync `List<T> → T` | _(none — no configuration required)_ |
| `Branch.of(T.class)` | Sync terminal routing | `.when(Predicate<T>, Pipeline<T>)` (repeatable), `.otherwise(Pipeline<T>)` (required) |
| `Router.of(T.class)` | Sync terminal fan-out | `.routes(int)`, `.factory(Supplier<Pipeline<T>>)`, routing strategy |
| `OpenRouter.of(T.class, R.class)` | Sync intermediate fan-out | `.routes(int)`, `.factory(Supplier<OpenPipeline<T,R>>)`, routing strategy |
| `Broadcast.of(T.class)` | Sync terminal fan-out | `.targets(List<Pipeline<T>>)` |
| `Action.of(T.class)` | Sync terminal consumer | `.action(Consumer<T>)` (required) |

**Routing strategies** (for `Router` and `OpenRouter`, mutually exclusive; default is round-robin):
- `.roundRobin()` — explicit round-robin (default when no strategy is set).
- `.balanced()` — routes to the arm with the fewest in-flight items.
- `.sticky(Function<T,?>)` — consistent hashing; all items with the same extracted key go to the same arm.
- `.routingFunction(RoutingFunction<T>)` — custom zero-based arm index.

### Observer callbacks

Async blocks and some sync blocks accept optional observer callbacks:

| Callback | Interface | Fires when |
|---|---|---|
| `itemPostedHandler` | `ItemPostedHandler<T>` | An item is accepted or rejected at `post()`. |
| `itemDeliveredHandler` | `ItemDeliveredHandler<O>` | An item (or batch/group) is successfully delivered downstream. |
| `errorOccurredHandler` | `ErrorOccurredHandler<O>` | A downstream target throws during delivery (async blocks only). |

Register via the fluent builder:

```java
Buffer.of(Message.class)
        .itemPostedHandler((source, item, accepted) -> metrics.increment("posted"))
        .errorOccurredHandler((source, item, error) -> deadLetter.send(item))
```

---

## SourceBlock — Async Producer

`SourceBlock<T>` continuously polls a `Supplier<T>` (or `Supplier<List<T>>`) on one or
more dedicated threads and pushes each item downstream.  It is the canonical starting
point for pipelines that pull work from an external source — an SQS queue, a Kafka
consumer, a database poll loop, or any blocking supplier.

Unlike the intermediate and terminal stages assembled through `Pipeline.builder()`,
`SourceBlock` is always constructed directly and connected to the pipeline head via
`linkTo`:

```java
NamedExecutorService executor = NamedExecutorService.builder()
        .threadPrefix("work-poller")
        .build();

SourceBlock<Message> source = SourceBlock.<Message>builder()
        .batchSupplier(() -> queue.receiveBatch())   // returns up to N items per call
        .executor(executor)
        .build();

Pipeline<Message> pipeline = Pipeline.<Message>builder()
        .executor(executor)
        .from(Buffer.of(Message.class))
        .to(msg -> process(msg));

source.linkTo(pipeline);
```

The worker thread blocks inside `supplier.get()` when nothing is available (e.g., a
remote queue's long-poll call parks the thread until work arrives) and wakes immediately
when items are returned — no busy-spin, no polling delay.

### Supplier modes

| Method | Behavior |
|---|---|
| `.supplier(Supplier<T>)` | Each call returns one item or `null`.  A `null` return is a no-op; the supplier is called again immediately. |
| `.batchSupplier(Supplier<List<T>>)` | Each call returns a list of items.  A `null` or empty list is a no-op.  Items are forwarded to the downstream target individually. |

Exactly one of `.supplier()` or `.batchSupplier()` must be configured.  Calling neither,
or calling both, throws `IllegalStateException` at build time.

### Concurrency policy

By default a single thread polls the supplier.  Pass a `SourceConcurrencyPolicy` to run
multiple threads:

```java
// Fixed — always run exactly N polling threads concurrently.
SourceBlock.<Message>builder()
        .batchSupplier(() -> queue.receiveBatch())
        .concurrencyPolicy(SourceConcurrencyPolicy.fixed(4))
        .executor(executor)
        .build();

// Adaptive — starts at 1 thread, scales up to N as the supplier keeps returning results,
// scales back down when it begins returning empty results.
SourceBlock.<Message>builder()
        .batchSupplier(() -> queue.receiveBatch())
        .concurrencyPolicy(
                SourceConcurrencyPolicy.adaptive(8)
                        .minThreads(2)         // floor; default 1
                        .scaleUpThreshold(5))  // successes before adding a thread; default 10
        .executor(executor)
        .build();
```

**`fixed(int threads)`** — straightforward and predictable.  All threads poll simultaneously
at all times regardless of whether the supplier is returning results.

**`adaptive(int maxThreads)`** — starts at `minThreads` (default `1`) and grows toward
`maxThreads` by adding one thread after `scaleUpThreshold` consecutive non-empty supplier
calls.  On any empty return the counter resets and one thread retires down to the floor.
This naturally matches thread count to queue depth: aggressive when the queue is full,
quiescent when it is empty.

`maxThreads` must be ≥ `2` for adaptive mode.  `SourceConcurrencyPolicy.fixed(1)` (the
implicit default when no policy is configured) and `SourceConcurrencyPolicy.adaptive(N)`
with `minThreads(1)` both start at a single thread; the difference is that `adaptive`
grows and shrinks automatically while `fixed` stays constant.

| Parameter | Default | Description |
|---|---|---|
| `maxThreads` | _(required)_ | Ceiling on active polling threads; must be ≥ 2. |
| `minThreads(int)` | `1` | Floor; threads never retire below this count. |
| `scaleUpThreshold(int)` | `10` | Consecutive non-empty supplier calls required before adding one thread.  Lower values scale up more aggressively. |

> **Thread-safety requirement** — when more than one polling thread is active the
> downstream head block must be safe to call concurrently.  The recommended pattern is
> to link `SourceBlock` directly to a `BufferBlock`: its `post()` is fully thread-safe
> and the internal `BlockingQueue` naturally absorbs concurrent posts.

### Lifecycle

`SourceBlock` does not implement `complete()`.  It runs until its executor is shut down.
The correct drain sequence for a source-driven pipeline is:

```java
// 1. Stop polling — interrupt the supplier threads.
executor.shutdown();

// 2. Drain any items already in-flight through the rest of the pipeline.
pipeline.complete();
pipeline.awaitCompletion();
```

### Delegate handlers

| Method | Fires when |
|---|---|
| `.itemDeliveredHandler(ItemDeliveredHandler<T>)` | An item was successfully forwarded downstream. |
| `.errorOccurredHandler(ErrorOccurredHandler<T>)` | The downstream target threw during delivery. |

---

## OpenRouter Pattern — Parallel Arms With Fan-In

`OpenRouter` is the right tool when you need to distribute work across parallel arms
and then merge results at a single downstream stage.  Each arm is an `OpenPipeline` —
it processes items independently and emits its output to the shared downstream.

```java
Pipeline<Message> pipeline = Pipeline.<Message>builder()
        .executor(executor)
        .from(Buffer.of(Message.class))
        .then(OpenRouter.<Message, List<Message>>of()
                .sticky(Message::deviceId)         // consistent routing by device
                .routes(16)
                .factory(() -> OpenPipeline.builder()
                        .executor(executor)
                        .from(Buffer.of(Message.class))
                        .then(Group.of(Message.class, String.class)
                                .groupingFunction(Message::customerId))
                        .build()))
        .to(groups -> groups.forEach(this::persist));
```

**When to use `Router` vs `OpenRouter`:**
- `Router` — each arm terminates independently; no fan-in needed.
- `OpenRouter` — arms produce output that must flow to a common downstream stage.

---

## Completion and Drain Lifecycle

Every `Pipeline` and block follows the same lifecycle.

```java
// 1. Signal that no more items will be posted.
//    Completion cascades automatically head → tail.
pipeline.complete();

// 2a. Block the calling thread until all items have drained.
pipeline.awaitCompletion();

// 2b. Alternative: block with a timeout.
boolean drained = pipeline.awaitCompletion(Duration.ofSeconds(30));

// 2c. Alternative: get the CompletableFuture and compose asynchronously.
pipeline.completion().thenRun(() -> System.out.println("done"));

// 3. Shutdown the executor AFTER the pipeline has fully drained.
executor.shutdown();
executor.awaitTermination(5, TimeUnit.SECONDS);
```

**`DelayBlock` drain note:** When `complete()` is called, any items still in the delay
queue are delivered immediately — unexpired delays are not honored.  If items must be
delivered at their natural delay, use a `CountDownLatch` and call `complete()` only
after all items have been delivered:

```java
CountDownLatch latch = new CountDownLatch(itemCount);

pipeline.linkTo(item -> {
    process(item);
    latch.countDown();
    return true;
});

items.forEach(pipeline::post);
latch.await();           // wait for natural delivery
pipeline.complete();     // queue is empty; immediate flush is a no-op
```

---

## NamedExecutorService

`NamedExecutorService` is the recommended executor for all async pipeline stages.  It
gives worker threads a readable name prefix (useful in thread dumps), and its
`shutdown()` interrupts blocked workers so they exit cleanly rather than hanging.

```java
NamedExecutorService executor = NamedExecutorService.builder()
        .threadPrefix("order-pipeline")    // required
        .build();
```

Threads are created on demand and cached for 60 seconds after becoming idle — there is
no pool-size cap to configure.

Pass the same instance to every stage builder (or once to `Pipeline.builder().executor()`
and the fluent builder injects it automatically):

```java
Pipeline<Order> pipeline = Pipeline.<Order>builder()
        .executor(executor)    // propagated to all async stages automatically
        .from(Buffer.of(Order.class))
        .then(Batch.of(Order.class).batchSize(50))
        .to(batch -> persist(batch));
```

Use `Executors.newVirtualThreadPerTaskExecutor()` on Java 21+ when thread-per-task
semantics are preferred; any `Executor` implementation is accepted.

---

## Logging & Diagnostics

### Logger Name

All diagnostic output from the concurrency module flows through a single logger:

```
software.frisby.core.concurrency.EventSource
```

Configuring this one name is all that is needed to control the module's entire log output.

---

### Level Semantics

| Level | What it covers | Volume |
|---|---|---|
| **ERROR** | Delivery failures — a downstream `Target.post()` threw an exception.  Exceptions thrown by user-supplied callbacks (`ErrorOccurredHandler`, `ItemDeliveredHandler`, `ItemPostedHandler`).  Predicate failures in `BranchBlock`.  Supplier failures in `SourceBlock`. | Low — one entry per failure event. |
| **WARNING** | A block received an item but has no downstream target linked yet — posting is blocked until `linkTo()` is called.  Fires at most once per posting thread before the first `linkTo()` call; never fires again once the block is wired. | Very low — one entry per unlinked block per posting thread. |

**ERROR is always emitted** — even if no `errorOccurredHandler` is configured, the failure
is logged so that silent data loss never occurs in an asynchronous block.

Every log statement is guarded by `LOG.isLoggable(level)` before any string formatting
occurs.  Suppressed levels cost nothing at runtime.

---

### Suppressing Output

This module emits only ERROR and WARNING messages.  The recommended production
configuration is `WARNING` (or `ERROR` if you prefer maximum silence), which captures
both delivery failures and the unlinked-block diagnostic while suppressing everything else.

#### Java Util Logging (default)

`System.Logger` delegates to JUL by default.  JUL has no `ERROR` level — the
equivalent is `SEVERE`.  `WARNING` maps directly:

```properties
# Recommended — capture delivery failures and unlinked-block warnings
software.frisby.core.concurrency.EventSource.level = WARNING

# Errors only — suppress the unlinked-block warning
# software.frisby.core.concurrency.EventSource.level = SEVERE

# To suppress everything including errors (not recommended in production):
# software.frisby.core.concurrency.EventSource.level = OFF
```

To supply a `logging.properties` file at startup:

```
java -Djava.util.logging.config.file=/path/to/logging.properties -jar your-app.jar
```

#### Logback (via `slf4j-jdk-platform-logging`)

`System.Logger` routes to SLF4J — and from there to Logback — when the
`slf4j-jdk-platform-logging` artifact is on the classpath.

**Maven dependency:**

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-jdk-platform-logging</artifactId>
    <!-- use the current SLF4J version from Maven Central -->
</dependency>
```

**`logback.xml` or `logback-spring.xml`:**

```xml
<logger name="software.frisby.core.concurrency.EventSource" level="WARN"/>
```

#### Log4j2 (via `log4j-jpl`)

`System.Logger` routes directly to Log4j2 when the `log4j-jpl` artifact is on the
classpath (no JUL bridge needed).

**Maven dependency:**

```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-jpl</artifactId>
    <!-- use the current Log4j2 version from Maven Central -->
</dependency>
```

**`log4j2.xml`:**

```xml
<Logger name="software.frisby.core.concurrency.EventSource" level="WARN" additivity="false"/>
```

**`log4j2.yaml`:**

```yaml
Loggers:
  Logger:
    - name: software.frisby.core.concurrency.EventSource
      level: warn
      additivity: false
```

---

### `errorOccurredHandler` vs. ERROR Logging

These two mechanisms are complementary, not alternatives.

**ERROR logging fires unconditionally.** When a downstream target throws during
delivery, the block logs an ERROR entry and continues processing the next item.  This
is the minimum signal that something went wrong — it requires no configuration and is
always present.

**`errorOccurredHandler` is an optional programmatic callback** that fires *in addition
to* the ERROR log.  Register one when your application needs to *react* to the failure,
not just observe it:

| Use case | Approach |
|---|---|
| See that failures are happening | ERROR log — already there, nothing to add. |
| Increment a metrics counter on failure | Register an `errorOccurredHandler`. |
| Trip a circuit-breaker after N failures | Register an `errorOccurredHandler`. |
| Send failed items to a dead-letter queue | Register an `errorOccurredHandler` — the `item` parameter carries the undelivered item. |
| Retry the item on a transient error | Register an `errorOccurredHandler` — inspect the `error`, re-post if appropriate. |
| Page an on-call engineer | Register an `errorOccurredHandler`. |

If you only need visibility into failures and your logging pipeline already alerts on
ERROR, the automatic log entry is sufficient and no handler is needed.

---

## AI-Assisted Development

When working with an AI coding assistant on code that wires or extends this module,
attach this guide and `docs/ai/concurrency.md` to your session context.  The AI context
file covers exact builder signatures, defaults, routing strategy options, the completion
lifecycle, and common anti-patterns in a compact, attachable format.

If your project uses a persistent instructions file (such as
`.github/copilot-instructions.md`), add the following to tell the assistant about the
concurrency module:

```markdown
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



