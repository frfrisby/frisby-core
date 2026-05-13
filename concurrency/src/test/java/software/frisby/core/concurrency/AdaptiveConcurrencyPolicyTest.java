package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NumericValueOutsideRangeException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdaptiveConcurrencyPolicy} and the
 * {@link SourceConcurrencyPolicy#adaptive(int)} factory method.  Covers input validation,
 * default values, fluent mutators, and immutability guarantees.
 */
class AdaptiveConcurrencyPolicyTest {

    // -------------------------------------------------------------------------
    // Factory — SourceConcurrencyPolicy.adaptive(int)
    // -------------------------------------------------------------------------

    @Nested
    class Factory {
        @Test
        void maxThreadsNegative_throwsNumericValueOutsideRangeException() {
            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> SourceConcurrencyPolicy.adaptive(-1)
            );
        }

        @Test
        void maxThreadsZero_throwsNumericValueOutsideRangeException() {
            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> SourceConcurrencyPolicy.adaptive(0)
            );
        }

        @Test
        void maxThreadsOne_throwsNumericValueOutsideRangeException() {
            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> SourceConcurrencyPolicy.adaptive(1)
            );
        }

        @Test
        void maxThreadsTwo_returnsAdaptiveConcurrencyPolicy() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(2);

            assertNotNull(policy);
        }

        @Test
        void maxThreadsTwo_maxThreadsReturnsTwo() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(2);

            assertEquals(2, policy.maxThreads());
        }

        @Test
        void defaults_minThreadsIsOne() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertEquals(1, policy.minThreads());
        }

        @Test
        void defaults_scaleUpThresholdIsTen() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertEquals(AdaptiveConcurrencyPolicy.DEFAULT_SCALE_UP_THRESHOLD, policy.scaleUpThreshold());
        }
    }

    // -------------------------------------------------------------------------
    // minThreads(int) — validation, immutability, and field preservation
    // -------------------------------------------------------------------------

    @Nested
    class MinThreads {
        @Test
        void minThreadsZero_throwsNumericValueOutsideRangeException() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> policy.minThreads(0)
            );
        }

        @Test
        void minThreadsNegative_throwsNumericValueOutsideRangeException() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> policy.minThreads(-1)
            );
        }

        @Test
        void minThreadsEqualToMax_throwsNumericValueOutsideRangeException() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> policy.minThreads(4)
            );
        }

        @Test
        void minThreadsAboveMax_throwsNumericValueOutsideRangeException() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> policy.minThreads(5)
            );
        }

        @Test
        void validMinThreads_minThreadsReflectsNewValue() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4)
                    .minThreads(2);

            assertEquals(2, policy.minThreads());
        }

        @Test
        void validMinThreads_returnsNewInstance() {
            AdaptiveConcurrencyPolicy original = SourceConcurrencyPolicy.adaptive(4);
            AdaptiveConcurrencyPolicy updated = original.minThreads(2);

            assertNotSame(original, updated);
        }

        @Test
        void validMinThreads_originalIsUnchanged() {
            AdaptiveConcurrencyPolicy original = SourceConcurrencyPolicy.adaptive(4);

            original.minThreads(2);

            assertEquals(1, original.minThreads());
        }

        @Test
        void validMinThreads_maxThreadsAndThresholdArePreserved() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(8)
                    .scaleUpThreshold(5)
                    .minThreads(3);

            assertEquals(8, policy.maxThreads());
            assertEquals(5, policy.scaleUpThreshold());
        }
    }

    // -------------------------------------------------------------------------
    // scaleUpThreshold(int) — validation, immutability, and field preservation
    // -------------------------------------------------------------------------

    @Nested
    class ScaleUpThreshold {
        @Test
        void scaleUpThresholdZero_throwsNumericValueOutsideRangeException() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> policy.scaleUpThreshold(0)
            );
        }

        @Test
        void scaleUpThresholdNegative_throwsNumericValueOutsideRangeException() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4);

            assertThrows(
                    NumericValueOutsideRangeException.class,
                    () -> policy.scaleUpThreshold(-1)
            );
        }

        @Test
        void validScaleUpThreshold_scaleUpThresholdReflectsNewValue() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(4)
                    .scaleUpThreshold(5);

            assertEquals(5, policy.scaleUpThreshold());
        }

        @Test
        void validScaleUpThreshold_returnsNewInstance() {
            AdaptiveConcurrencyPolicy original = SourceConcurrencyPolicy.adaptive(4);
            AdaptiveConcurrencyPolicy updated = original.scaleUpThreshold(5);

            assertNotSame(original, updated);
        }

        @Test
        void validScaleUpThreshold_originalIsUnchanged() {
            AdaptiveConcurrencyPolicy original = SourceConcurrencyPolicy.adaptive(4);

            original.scaleUpThreshold(5);

            assertEquals(AdaptiveConcurrencyPolicy.DEFAULT_SCALE_UP_THRESHOLD, original.scaleUpThreshold());
        }

        @Test
        void validScaleUpThreshold_maxThreadsAndMinThreadsArePreserved() {
            AdaptiveConcurrencyPolicy policy = SourceConcurrencyPolicy.adaptive(8)
                    .minThreads(3)
                    .scaleUpThreshold(5);

            assertEquals(8, policy.maxThreads());
            assertEquals(3, policy.minThreads());
        }
    }
}

