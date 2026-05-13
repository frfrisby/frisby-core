package software.frisby.core.concurrency;

/**
 * Receives a programmatic callback each time an exception is thrown by a downstream target
 * while an asynchronous block attempts to deliver an item.
 *
 * <p>Regardless of whether a handler is configured, the block always logs the error at
 * {@code ERROR} level automatically.  This handler fires <em>after</em> that log entry and
 * is intended for cases where the application also needs to react programmatically — for
 * example, to increment a metric, trigger a circuit-breaker, or re-route the item.</p>
 *
 * <p>This handler is only available on asynchronous blocks ({@link BufferBlock},
 * {@link BatchBlock}, {@link GroupBlock}, {@link DelayBlock}).  On synchronous blocks the
 * posting thread executes delivery inline and any exception propagates naturally up the
 * call stack without requiring an explicit handler.</p>
 *
 * <p>This is a {@link FunctionalInterface} by design.  The method signature must not be
 * changed or augmented with additional abstract methods, as that would break lambda
 * implementations.</p>
 *
 * @param <T> The type of item that was being delivered when the error occurred.
 */
@FunctionalInterface
public interface ErrorOccurredHandler<T> {
    /**
     * Called by the block when an exception is thrown during item delivery.
     *
     * @param source The block in which the error was detected.
     * @param target The downstream target that threw the exception.
     * @param item   The item that was being delivered when the exception was thrown.
     * @param error  The exception that was thrown.
     */
    void onError(Object source, Object target, T item, Throwable error);
}
