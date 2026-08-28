package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Errors#throwIfFatal(Throwable)}.  Every async worker in this module
 * relies on this method to distinguish a fatal JVM condition (which must propagate and kill the
 * worker thread) from an ordinary application bug (which must be logged and swallowed so the
 * worker survives).
 */
class ErrorsTest {
    @Nested
    class ThrowIfFatal {
        @Test
        void virtualMachineError_isRethrown() {
            StackOverflowError original = new StackOverflowError("boom");

            StackOverflowError rethrown = assertThrows(
                    StackOverflowError.class,
                    () -> Errors.throwIfFatal(original)
            );

            assertSame(original, rethrown);
        }

        @Test
        void outOfMemoryError_isRethrown() {
            OutOfMemoryError original = new OutOfMemoryError("boom");

            OutOfMemoryError rethrown = assertThrows(
                    OutOfMemoryError.class,
                    () -> Errors.throwIfFatal(original)
            );

            assertSame(original, rethrown);
        }

        @Test
        void linkageError_isRethrown() {
            NoClassDefFoundError original = new NoClassDefFoundError("boom");

            NoClassDefFoundError rethrown = assertThrows(
                    NoClassDefFoundError.class,
                    () -> Errors.throwIfFatal(original)
            );

            assertSame(original, rethrown);
        }

        @Test
        void nonFatalError_returnsNormally() {
            // AssertionError is a plain Error — not a VirtualMachineError or LinkageError — and
            // is therefore considered recoverable: the caller logs it and the worker continues.
            assertDoesNotThrow(() -> Errors.throwIfFatal(new AssertionError("boom")));
        }

        @Test
        void runtimeException_returnsNormally() {
            assertDoesNotThrow(() -> Errors.throwIfFatal(new RuntimeException("boom")));
        }

        @Test
        void checkedException_returnsNormally() {
            assertDoesNotThrow(() -> Errors.throwIfFatal(new java.io.IOException("boom")));
        }
    }
}

