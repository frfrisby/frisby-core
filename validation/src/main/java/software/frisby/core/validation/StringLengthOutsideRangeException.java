package software.frisby.core.validation;

import java.io.Serial;

/**
 * Thrown when a validated {@code String} value's length does not satisfy the
 * required constraint.
 *
 * <p>Length is measured in Unicode code points, not Java {@code char} values.
 *
 * @see Strings
 * @see TrimmedStrings
 * @see StringSequences
 */
public class StringLengthOutsideRangeException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with no detail message.
     */
    public StringLengthOutsideRangeException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public StringLengthOutsideRangeException(String message) {
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
    public StringLengthOutsideRangeException(String message, Throwable cause) {
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
    public StringLengthOutsideRangeException(Throwable cause) {
        super(cause);
    }
}
