package software.frisby.core.concurrency;

/**
 * Receives a notification each time a group within a {@link GroupBlock} is created or modified,
 * and returns a {@link Retention} policy indicating whether the group should be flushed
 * immediately or held until the configured timeout.
 *
 * <p>This provides an early-release mechanism: when the observer returns
 * {@link Retention#RELEASE}, the group is flushed to the downstream target immediately,
 * bypassing the normal timeout and idle-timeout windows.  Returning {@link Retention#HOLD}
 * leaves the default behavior unchanged.</p>
 *
 * <p>This is a {@link FunctionalInterface} by design.  The method signature must not be
 * changed or augmented with additional abstract methods, as that would break lambda
 * implementations.</p>
 *
 * @param <T> The type of items held in the group.
 * @param <K> The type of the key that identifies the group.
 * @see GroupBlock
 * @see Retention
 */
@FunctionalInterface
public interface GroupObserver<T, K> {
    /**
     * Called by the block after a group is created or modified.
     *
     * @param group The group that was created or modified.
     * @return {@link Retention#RELEASE} to flush the group to the downstream target immediately;
     * {@link Retention#HOLD} to continue accumulating items under the default policy.
     */
    Retention onModified(Group<T, K> group);
}
