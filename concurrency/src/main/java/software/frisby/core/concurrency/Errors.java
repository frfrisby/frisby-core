package software.frisby.core.concurrency;

/**
 * Classifies a {@link Throwable} caught during item delivery as fatal or non-fatal.
 *
 * <p>Every async worker in this module wraps its delivery call in a broad
 * {@code catch (Throwable t)} so that an application bug in a downstream callback never
 * silently and permanently kills the worker thread.  {@link #throwIfFatal(Throwable)} is
 * called first inside that catch block: a {@link VirtualMachineError} (e.g.
 * {@link OutOfMemoryError}, {@link StackOverflowError}) or a {@link LinkageError} indicates
 * the JVM itself is in a compromised or inconsistent state, so it is rethrown immediately,
 * becoming a genuine uncaught exception on the worker thread.  Everything else — including
 * ordinary {@link RuntimeException}s and non-fatal {@link Error}s such as
 * {@link AssertionError} — is considered recoverable: the caller logs it and the worker
 * continues processing the next item.</p>
 */
final class Errors {
    private Errors() {
    }

    /**
     * Rethrows {@code t} if it represents a fatal JVM condition; otherwise returns normally
     * so the caller can log it and continue.
     *
     * @param t The throwable to classify.
     * @throws VirtualMachineError if {@code t} is a {@code VirtualMachineError}.
     * @throws LinkageError        if {@code t} is a {@code LinkageError}.
     */
    static void throwIfFatal(Throwable t) {
        if (t instanceof VirtualMachineError vme) {
            throw vme;
        }

        if (t instanceof LinkageError le) {
            throw le;
        }
    }
}
