package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Group}.  Covers all public accessor methods exposed to
 * {@link GroupObserver} implementations: {@code key()}, {@code created()},
 * {@code lastModified()}, {@code size()}, {@code get()}, and {@code iterator()}.
 */
class GroupTest {
    // -------------------------------------------------------------------------
    // key()
    // -------------------------------------------------------------------------

    @Nested
    class Key {
        @Test
        void key_returnsProvidedKey() {
            Group<String, String> group = new Group<>("my-key");

            assertEquals("my-key", group.key());
        }
    }

    // -------------------------------------------------------------------------
    // created()
    // -------------------------------------------------------------------------

    @Nested
    class Created {
        @Test
        void created_returnsInstantAtConstructionTime() {
            // Created: same clock source for brackets as Group uses internally

            long before = System.currentTimeMillis();
            Group<String, String> group = new Group<>("k");
            long after = System.currentTimeMillis();

            long createdMs = group.created().toEpochMilli();

            assertTrue(createdMs >= before);
            assertTrue(createdMs <= after);
        }
    }

    // -------------------------------------------------------------------------
    // lastModified()
    // -------------------------------------------------------------------------

    @Nested
    class LastModified {
        @Test
        void lastModified_initiallyMatchesCreated() {
            Group<String, String> group = new Group<>("k");

            assertEquals(group.created(), group.lastModified());
        }

        @Test
        void lastModified_isNotBeforeCreatedAfterAdd() {
            Group<String, String> group = new Group<>("k");

            group.add("item");

            assertFalse(group.lastModified().isBefore(group.created()));
        }
    }

    // -------------------------------------------------------------------------
    // size()
    // -------------------------------------------------------------------------

    @Nested
    class Size {
        @Test
        void size_returnsZeroInitially() {
            Group<String, String> group = new Group<>("k");

            assertEquals(0, group.size());
        }

        @Test
        void size_reflectsEachAdd() {
            Group<String, String> group = new Group<>("k");

            group.add("a");
            assertEquals(1, group.size());

            group.add("b");
            assertEquals(2, group.size());

            group.add("c");
            assertEquals(3, group.size());
        }
    }

    // -------------------------------------------------------------------------
    // get()
    // -------------------------------------------------------------------------

    @Nested
    class Get {
        @Test
        void get_returnsItemAtIndex() {
            Group<String, String> group = new Group<>("k");

            group.add("first");
            group.add("second");
            group.add("third");

            assertEquals("first", group.get(0));
            assertEquals("second", group.get(1));
            assertEquals("third", group.get(2));
        }
    }

    // -------------------------------------------------------------------------
    // iterator()
    // -------------------------------------------------------------------------

    @Nested
    class Iterator {
        @Test
        void iterator_iteratesAllItemsInInsertionOrder() {
            Group<String, String> group = new Group<>("k");

            group.add("a");
            group.add("b");
            group.add("c");

            List<String> collected = new ArrayList<>();

            for (String item : group) {
                collected.add(item);
            }

            assertEquals(List.of("a", "b", "c"), collected);
        }

        @Test
        void iterator_onEmptyGroup_hasNoElements() {
            Group<String, String> group = new Group<>("k");

            assertFalse(group.iterator().hasNext());
        }
    }
}

