package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents the receiving end of a pipeline connection: any object that can accept
 * posted items of type {@code T}.
 *
 * <p>{@code Target} is a {@link FunctionalInterface} (SAM = {@link #post}).  Pipeline
 * stages are wired together by passing a {@code Target} to {@link Source#linkTo}.  The
 * interface also carries a complete lifecycle contract — {@link #complete()},
 * {@link #completion()}, {@link #awaitCompletion()}, and {@link #awaitCompletion(Duration)}
 * — whose default implementations are suitable for synchronous lambda targets and
 * are overridden by buffered blocks that must drain before stopping.</p>
 *
 * <p>This is a Single Abstract Method (SAM) interface by design.  The {@code default}
 * methods below do not affect the SAM, and {@code @FunctionalInterface} is preserved.</p>
 *
 * @param <T> The type of item accepted by this target.
 */
@FunctionalInterface
public interface Target<T> {
    /**
     * Posts an item to this target.
     *
     * @param item The item to post.
     * @return {@code true} if the item was accepted; {@code false} if it was rejected
     * (for example, because the target's internal queue is full or the target
     * has already been completed).
     */
    boolean post(T item);

    /**
     * Returns the number of items currently waiting in this target's immediate ingress queue.
     *
     * <p>The default implementation returns {@code 0}, which is correct for lambda targets and
     * synchronous blocks that process items inline and hold no internal queue.  Async buffered
     * blocks ({@link BufferBlock}, {@link BatchBlock}, {@link PriorityBufferBlock},
     * {@link DelayBlock}) override this to return their actual queue depth.</p>
     *
     * <p>For capacity monitoring, use {@link #inFlight()} instead, which accounts for items
     * held anywhere across all stages of a pipeline arm — not just the head buffer.</p>
     *
     * @return The number of items currently queued in this target's ingress buffer; {@code 0} if
     * this target holds no internal buffer.
     */
    default int size() {
        return 0;
    }

    /**
     * Returns the total number of items currently in-flight within this target and all of its
     * downstream pipeline stages.
     *
     * <p>For a plain block ({@link BufferBlock}, {@link BatchBlock}, {@link GroupBlock},
     * {@link DelayBlock}), the in-flight count equals {@link #size()} because each block's
     * capacity gate tracks every item from acceptance to delivery, covering both the ingress
     * queue and any items held in an internal accumulator (batch buffer, key-grouped map,
     * delay queue).</p>
     *
     * <p>This value is used by {@link RouterBlockBuilder#balanced()} to route each posted item
     * to the least-loaded downstream target.  It is a point-in-time snapshot and may change
     * between the routing decision and the actual {@link #post} call; callers should treat it
     * as a best-effort approximation rather than a precise count.</p>
     *
     * <p>The default implementation delegates to {@link #size()}, which is correct for any
     * target that is not a multi-stage pipeline.</p>
     *
     * @return The total number of items currently in-flight across this target and all of its
     * downstream pipeline stages; {@code 0} if this target holds no internal buffer.
     */
    default int inFlight() {
        return size();
    }

    /**
     * Notifies this target that an upstream source has linked to it.  Each upstream source
     * that calls {@link Source#linkTo} on itself triggers this method exactly once on the
     * downstream target.  Fan-out blocks ({@link RouterBlock}, {@link BroadcastBlock},
     * {@link BranchBlock}) also invoke this on each of their pre-wired targets at
     * construction time.
     *
     * <p>Concrete block implementations use this notification to implement reference-counted
     * completion: {@link #complete()} fires the drain only after every registered upstream
     * source has called {@code complete()}, preventing premature shutdown in fan-in
     * topologies where multiple upstream blocks feed a single downstream block.</p>
     *
     * <p>The default implementation is a no-op and is suitable for lambda targets that do
     * not require fan-in coordination.  Buffered blocks override this to increment an
     * internal pending-completes counter.</p>
     */
    default void onLinked() {
    }

    /**
     * Signals that no more items will be posted to this target.  The target will finish
     * processing any already-queued items and then stop cleanly.  Subsequent calls to
     * {@link #post(Object)} return {@code false} immediately.
     *
     * <p>The default implementation is a no-op and is suitable for lambda targets and
     * synchronous blocks that process items inline.  Buffered blocks override this method
     * to initiate a graceful drain.</p>
     */
    default void complete() {
    }

    /**
     * Returns a {@link CompletableFuture} that resolves when this target has finished
     * processing all items queued before {@link #complete()} was called, and all downstream
     * targets have also completed.
     *
     * <p>The default returns an already-resolved future.  Buffered blocks override this with
     * a future that resolves after the drain is complete.</p>
     *
     * @return A {@link CompletableFuture} that resolves when processing is complete.
     */
    default CompletableFuture<Void> completion() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Blocks the calling thread until all items queued before {@link #complete()} was called
     * have been processed and all downstream targets have also completed.
     *
     * <p>{@link #complete()} must be called before invoking this method; otherwise the calling
     * thread will block indefinitely.  If the calling thread is interrupted while waiting, the
     * interrupt status is restored and this method returns immediately.</p>
     */
    default void awaitCompletion() {
        try {
            completion().get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            // Completion futures in this library always complete normally, never exceptionally.
        }
    }

    /**
     * Blocks the calling thread until all items queued before {@link #complete()} was called
     * have been processed and all downstream targets have also completed, or until the
     * specified timeout expires.
     *
     * <p>{@link #complete()} must be called before invoking this method; otherwise the calling
     * thread will block until the timeout expires.  If the calling thread is interrupted while
     * waiting, the interrupt status is restored and this method returns {@code false}.</p>
     *
     * @param timeout The maximum time to wait for the pipeline to finish draining.
     * @return {@code true} if the pipeline drained within the timeout; {@code false} if the
     * timeout expired or the calling thread was interrupted before draining completed.
     * @throws NullPointerException if {@code timeout} is null.
     */
    default boolean awaitCompletion(Duration timeout) {
        try {
            completion().get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (TimeoutException ex) {
            return false;
        } catch (ExecutionException ex) {
            // Completion futures in this library always complete normally, never exceptionally.
            return true;
        }
    }
}
