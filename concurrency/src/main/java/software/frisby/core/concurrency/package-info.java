/**
 * Asynchronous pipeline building blocks for constructing high-throughput, low-latency
 * data-processing pipelines.
 *
 * <p>A pipeline is assembled by connecting blocks through the
 * {@link software.frisby.core.concurrency.Target} interface: each block exposes a
 * {@code linkTo()} method that wires its output to the next block's {@code post()} method.
 * Items enter the pipeline at a head block, flow through transforms, filters, and
 * accumulators, and exit at a terminal block such as
 * {@link software.frisby.core.concurrency.ActionBlock}.  Every block supports an orderly
 * shutdown via {@code complete()} and a {@link java.util.concurrent.CompletableFuture}
 * returned by {@code completion()} that resolves when all buffered items have been
 * delivered.</p>
 *
 * <h2>Logging</h2>
 *
 * <p>All diagnostic output is emitted through a single {@link java.lang.System.Logger} named:
 *
 * <pre>    software.frisby.core.concurrency.EventSource</pre>
 * <p>
 * Three levels are used:
 *
 * <ul>
 *   <li><b>ERROR</b> — emitted whenever a downstream {@link software.frisby.core.concurrency.Target}
 *       throws during delivery, or when a user-supplied callback
 *       ({@link software.frisby.core.concurrency.ErrorOccurredHandler},
 *       {@link software.frisby.core.concurrency.ItemDeliveredHandler},
 *       {@link software.frisby.core.concurrency.ItemPostedHandler}) throws unexpectedly.
 *       Also emitted for predicate failures in
 *       {@link software.frisby.core.concurrency.BranchBlock} and supplier failures in
 *       {@link software.frisby.core.concurrency.SourceBlock}.  These entries are written
 *       unconditionally — regardless of whether an
 *       {@link software.frisby.core.concurrency.ErrorOccurredHandler} is also registered.</li>
 *   <li><b>DEBUG</b> — one entry per {@code linkTo()} call, written at pipeline-construction
 *       time.  Low volume; useful during development to confirm block wiring is correct.</li>
 *   <li><b>TRACE</b> — one or more entries per item posted: receipt at {@code post()},
 *       forwarding to a downstream target, and each delegate-handler invocation.  High
 *       volume under any real load; intended for tracing an individual item's path through
 *       a pipeline during development or debugging.</li>
 * </ul>
 *
 * <p>Every log statement is guarded by {@code LOG.isLoggable(level)} before any string
 * formatting occurs, so suppressed levels incur no runtime overhead.
 *
 * <p>For configuration instructions — including copy-paste snippets for JUL, Logback, and
 * Log4j2 — see the <b>Logging &amp; Diagnostics</b> section of
 * {@code docs/concurrency-guide.md} in the project repository.
 */
package software.frisby.core.concurrency;

