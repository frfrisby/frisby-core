package software.frisby.core.validation;

import java.io.Serial;

/**
 * Thrown when a validated value is not a member of the set of permitted values.
 *
 * <p>This exception signals that the value supplied by the caller exists but is
 * not one of the explicitly allowed options. It is a subtype of
 * {@link IllegalArgumentException} and represents a value failure rather than an
 * API misuse.
 *
 * @see Values
 */
public class DisallowedValueException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with no detail message.
     */
    public DisallowedValueException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public DisallowedValueException(String message) {
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
    public DisallowedValueException(String message, Throwable cause) {
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
    public DisallowedValueException(Throwable cause) {
        super(cause);
    }
}
