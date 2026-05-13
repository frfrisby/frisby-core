package software.frisby.core.concurrency;

/**
 * The group retention policy returned by a {@link GroupObserver} after a group is modified.
 *
 * <p>A {@link GroupObserver} can return {@link #RELEASE} to flush a group to the downstream
 * target immediately, or {@link #HOLD} to let the default timeout and idle-timeout windows
 * govern when it is flushed.</p>
 *
 * @see GroupObserver
 * @see GroupBlock
 */
public enum Retention {
    /**
     * Holds the group under the normal timeout policy.  Items continue to accumulate until
     * the group timeout, idle timeout, or size limit triggers a flush.
     */
    HOLD,

    /**
     * Releases the group immediately, bypassing any remaining timeout or idle-timeout window.
     * The group is flushed to the downstream target on the next worker iteration.
     */
    RELEASE
}
