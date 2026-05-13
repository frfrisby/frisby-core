package software.frisby.core.validation;

import java.io.Serial;
import java.time.LocalTime;

/**
 * Thrown when a validated {@link LocalTime} value does not satisfy the required
 * constraint.
 *
 * @see LocalTimes
 */
public class LocalTimeOutsideRangeException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with no detail message.
     */
    public LocalTimeOutsideRangeException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public LocalTimeOutsideRangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * <p>Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated in this exception's detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method). A {@code null} value
     *                is permitted, and indicates that the cause is nonexistent or
     *                unknown.
     */
    public LocalTimeOutsideRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}).
     *
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method). A {@code null} value is
     *              permitted, and indicates that the cause is nonexistent or unknown.
     */
    public LocalTimeOutsideRangeException(Throwable cause) {
        super(cause);
    }
}
