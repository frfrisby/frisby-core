package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullValueException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link RoutingFunction}.
 */
class RoutingFunctionTest {
    // -------------------------------------------------------------------------
    // roundRobin()
    // -------------------------------------------------------------------------

    @Nested
    class RoundRobin {
        @Test
        void roundRobin_sequentialItems_cyclesThroughTargetCount() {
            RoutingFunction<String> fn = RoutingFunction.roundRobin();

            // RouterBlock applies Math.floorMod(index, targetCount) — we just verify
            // the raw counter increments in order.
            assertEquals(0, fn.route("a"));
            assertEquals(1, fn.route("b"));
            assertEquals(2, fn.route("c"));
            assertEquals(3, fn.route("d"));
            assertEquals(4, fn.route("e"));
            assertEquals(5, fn.route("f"));
        }

        @Test
        void roundRobin_differentInstances_areIndependent() {
            RoutingFunction<String> first = RoutingFunction.roundRobin();
            RoutingFunction<String> second = RoutingFunction.roundRobin();

            first.route("a");
            first.route("b");

            // Second instance has its own counter — should still be at 0.
            assertEquals(0, second.route("a"));
        }
    }

    // -------------------------------------------------------------------------
    // sticky()
    // -------------------------------------------------------------------------

    @Nested
    class Sticky {
        @Test
        void sticky_sameKey_alwaysReturnsSameIndex() {
            RoutingFunction<String> fn = RoutingFunction.sticky(String::toString);

            int first = fn.route("customer-1");
            int second = fn.route("customer-1");

            assertEquals(first, second);
        }

        @Test
        void sticky_differentKeys_produceConsistentIndices() {
            RoutingFunction<String> fn = RoutingFunction.sticky(String::toString);

            // Calling twice with the same key must return the same value both times.
            int indexA1 = fn.route("key-A");
            int indexB1 = fn.route("key-B");
            int indexA2 = fn.route("key-A");
            int indexB2 = fn.route("key-B");

            assertEquals(indexA1, indexA2);
            assertEquals(indexB1, indexB2);
        }

        @Test
        void sticky_nullKey_routesToIndexZero() {
            RoutingFunction<String> fn = RoutingFunction.sticky(item -> null);

            assertEquals(0, fn.route("anything"));
        }

        @Test
        void sticky_nullKeyExtractor_throwsNullValueException() {
            assertThrows(NullValueException.class, () -> RoutingFunction.sticky(null));
        }
    }
}

