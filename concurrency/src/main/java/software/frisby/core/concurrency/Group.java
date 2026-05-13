package software.frisby.core.concurrency;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a time-windowed group of related items that share the same key, as managed by a
 * {@link GroupBlock}.
 *
 * <p>Groups are created internally by {@link GroupBlock} and are passed read-only to a
 * {@link GroupObserver} on each modification.  The observer may inspect the group's key,
 * size, and timestamps to decide whether to release it early via {@link Retention#RELEASE}.</p>
 *
 * <p>{@code Group} implements {@link Iterable} so observers can iterate over the accumulated
 * items directly.</p>
 *
 * @param <T> The type of items held in this group.
 * @param <K> The type of the key shared by all items in this group.
 * @see GroupBlock
 * @see GroupObserver
 */
public final class Group<T, K> implements Iterable<T> {
    private final K key;
    private final long created;
    private final ArrayList<T> items;

    private long lastModified;

    Group(K key) {
        this.created = System.currentTimeMillis();
        this.lastModified = this.created;

        this.key = key;
        this.items = new ArrayList<>();
    }

    /**
     * Returns the key shared by all items in this group.
     *
     * @return The key that identifies this group.
     */
    public K key() {
        return this.key;
    }

    /**
     * Returns the instant at which this group was first created.
     *
     * @return The creation time of this group.
     */
    public Instant created() {
        return Instant.ofEpochMilli(this.created);
    }

    /**
     * Returns the instant at which this group was most recently modified.
     *
     * @return The time at which the most recent item was added to this group.
     */
    public Instant lastModified() {
        return Instant.ofEpochMilli(this.lastModified);
    }

    /**
     * Returns the number of items currently accumulated in this group.
     *
     * @return The number of items in this group.
     */
    public int size() {
        return this.items.size();
    }

    /**
     * Returns the item at the specified zero-based index within this group.
     *
     * @param index The zero-based index of the item to return.
     * @return The item at the specified position.
     * @throws IndexOutOfBoundsException if {@code index} is out of range.
     */
    public T get(int index) {
        return this.items.get(index);
    }

    /**
     * Returns an iterator over the items in this group in insertion order.
     *
     * @return An iterator over the items in this group.
     */
    @Override
    public Iterator<T> iterator() {
        return this.items.iterator();
    }

    long createdAtMs() {
        return this.created;
    }

    long lastModifiedMs() {
        return this.lastModified;
    }

    void add(T item) {
        this.lastModified = System.currentTimeMillis();
        this.items.add(item);
    }

    List<T> toList() {
        return this.items;
    }
}
