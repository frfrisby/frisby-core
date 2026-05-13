package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DelayedEntry}.
 */
class DelayedEntryTest {
    @Nested
    class Item {
        @Test
        void item_returnsConstructedItem() {
            DelayedEntry<String> entry = new DelayedEntry<>("hello", Duration.ofSeconds(1));

            assertEquals("hello", entry.item());
        }
    }

    @Nested
    class GetDelay {
        @Test
        void getDelay_futureEntry_returnsPositiveValue() {
            DelayedEntry<String> entry = new DelayedEntry<>("hello", Duration.ofSeconds(60));

            assertTrue(entry.getDelay(TimeUnit.MILLISECONDS) > 0);
        }

        @Test
        void getDelay_expiredEntry_returnsNonPositiveValue() {
            DelayedEntry<String> entry = new DelayedEntry<>("hello", Duration.ZERO);

            assertTrue(entry.getDelay(TimeUnit.MILLISECONDS) <= 0);
        }
    }

    @Nested
    class CompareTo {
        @Test
        void compareTo_earlierEntryIsLessThanLaterEntry() {
            DelayedEntry<String> earlier = new DelayedEntry<>("a", Duration.ofMillis(100));
            DelayedEntry<String> later = new DelayedEntry<>("b", Duration.ofSeconds(60));

            assertTrue(earlier.compareTo(later) < 0);
        }

        @Test
        void compareTo_laterEntryIsGreaterThanEarlierEntry() {
            DelayedEntry<String> earlier = new DelayedEntry<>("a", Duration.ofMillis(100));
            DelayedEntry<String> later = new DelayedEntry<>("b", Duration.ofSeconds(60));

            assertTrue(later.compareTo(earlier) > 0);
        }
    }

    @Nested
    class ToString {
        @Test
        void toString_containsRemainingMsAndItem() {
            DelayedEntry<String> entry = new DelayedEntry<>("hello", Duration.ofSeconds(60));

            String result = entry.toString();

            assertTrue(result.startsWith("DelayedEntry{remainingMs="));
            assertTrue(result.contains(", item=hello}"));
        }
    }
}

