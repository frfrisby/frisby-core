package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Encapsulates the in-flight / downstream-completion race guard shared by every
 * synchronous block.
 *
 * <p>Every synchronous block must solve the same concurrent race: the {@code complete()}
 * signal and the last in-flight {@code post()} can both observe
 * {@code inFlight == 0 && completed == true} simultaneously.  Without a guard, both
 * would attempt to fire the downstream {@code complete()} — producing a double-fire.
 * {@code SyncCompletionGuard} solves this once, tested in isolation, so each block
 * composes the correct behavior by construction.
 *
 * <h2>Usage pattern</h2>
 *
 * <pre>{@code
 * // Construction — pass the action to fire exactly once when done.
 * this.guard = new SyncCompletionGuard(this::signalDownstream);
 *
 * // In post() — fast-path gate before any side-effecting work:
 * if (this.guard.isCompleted()) {
 *     return false;
 * }
 *
 * // ... null-item check, event logging, etc. ...
 *
 * this.guard.begin();
 * try {
 *     // ... deliver item downstream ...
 *     return true;
 * } finally {
 *     this.guard.end();
 * }
 *
 * // Delegation from the block's onLinked() and complete() methods:
 * public void onLinked() { this.guard.onLinked(); }
 * public void complete() { this.guard.complete(); }
 * }</pre>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>The downstream action fires exactly once regardless of concurrent races between
 *       {@code complete()} and {@code end()}.</li>
 *   <li>The downstream action does not fire until <em>both</em>
 *       {@code pendingCompletes &le; 0} (i.e. {@code completed == true}) and
 *       {@code inFlight == 0}.</li>
 *   <li>When {@code onLinked()} has been called {@code N} times, {@code complete()} must
 *       be called {@code N} times before the block transitions — matching the fan-in
 *       reference-counting protocol used across the module.</li>
 * </ul>
 */
final class SyncCompletionGuard {
    private final AtomicInteger pendingCompletes;
    private final AtomicBoolean completed;
    private final AtomicInteger inFlight;
    private final AtomicBoolean downstreamSignaled;
    private final Runnable downstream;

    /**
     * Constructs a new guard.
     *
     * @param downstream The action to invoke exactly once when both {@code completed}
     *                   transitions to {@code true} and {@code inFlight} reaches zero.
     *                   Must not be {@code null}.
     */
    SyncCompletionGuard(Runnable downstream) {
        Values.notNull("downstream", downstream);

        this.downstream = downstream;
        this.pendingCompletes = new AtomicInteger(0);
        this.completed = new AtomicBoolean(false);
        this.inFlight = new AtomicInteger(0);
        this.downstreamSignaled = new AtomicBoolean(false);
    }

    /**
     * Returns {@code true} if this guard has been completed, {@code false} otherwise.
     *
     * <p>Used as the fast-path gate at the top of a block's {@code post()} method,
     * before any side-effecting work is performed.
     *
     * @return {@code true} if completed.
     */
    boolean isCompleted() {
        return this.completed.get();
    }

    /**
     * Increments the pending-completes reference count, indicating that one additional
     * upstream source has been linked to this block.
     *
     * <p>Must be called once per upstream source, typically from the block's own
     * {@code onLinked()} implementation.
     */
    void onLinked() {
        this.pendingCompletes.incrementAndGet();
    }

    /**
     * Decrements the pending-completes reference count.  When the count reaches zero,
     * attempts to transition this guard to the completed state and fires the downstream
     * action if no items are currently in-flight.
     *
     * <p>Maps directly to the block's own {@code complete()} implementation.
     */
    void complete() {
        if (this.pendingCompletes.decrementAndGet() <= 0) {
            if (this.completed.compareAndSet(false, true)) {
                if (this.inFlight.get() == 0) {
                    signalDownstream();
                }
            }
        }
    }

    /**
     * Records that one item has entered in-flight processing.
     *
     * <p>Must be called after the null-item guard passes and immediately before the
     * block performs its downstream work.  Must be paired with a matching {@link #end()}
     * call in a {@code finally} block.
     */
    void begin() {
        this.inFlight.incrementAndGet();
    }

    /**
     * Records that one in-flight item has finished processing.
     *
     * <p>When this call reduces {@code inFlight} to zero and the guard is already
     * completed, fires the downstream action.
     */
    void end() {
        if (this.inFlight.decrementAndGet() == 0 && this.completed.get()) {
            signalDownstream();
        }
    }

    private void signalDownstream() {
        if (this.downstreamSignaled.compareAndSet(false, true)) {
            this.downstream.run();
        }
    }
}

