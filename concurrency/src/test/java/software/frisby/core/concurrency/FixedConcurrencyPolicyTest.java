package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FixedConcurrencyPolicy} and the {@link SourceConcurrencyPolicy#fixed(int)}
 * factory method.  Covers input validation and accessor behaviour.
 */
class FixedConcurrencyPolicyTest {

    // -------------------------------------------------------------------------
    // Factory — SourceConcurrencyPolicy.fixed(int)
    // -------------------------------------------------------------------------

    @Nested
    class Factory {
        @Test
        void threadsZero_throwsNumericValueOutsideRangeException() {
            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> SourceConcurrencyPolicy.fixed(0)
            );
        }

        @Test
        void threadsNegative_throwsNumericValueOutsideRangeException() {
            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> SourceConcurrencyPolicy.fixed(-1)
            );
        }

        @Test
        void threadsOne_returnsFixedConcurrencyPolicy() {
            FixedConcurrencyPolicy policy = SourceConcurrencyPolicy.fixed(1);

            assertNotNull(policy);
        }

        @Test
        void threadsOne_maxThreadsReturnsOne() {
            FixedConcurrencyPolicy policy = SourceConcurrencyPolicy.fixed(1);

            assertEquals(1, policy.maxThreads());
        }

        @Test
        void threadsMany_maxThreadsReflectsConfiguredValue() {
            FixedConcurrencyPolicy policy = SourceConcurrencyPolicy.fixed(8);

            assertEquals(8, policy.maxThreads());
        }
    }
}

