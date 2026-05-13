package software.frisby.core.concurrency;

import software.frisby.core.validation.Numbers;

/**
 * A {@link SourceConcurrencyPolicy} that starts at a configurable floor and scales the number of
 * active supplier-polling threads up and down automatically based on whether the supplier is
 * returning results.
 *
 * <p>Obtain an instance via {@link SourceConcurrencyPolicy#adaptive(int)}.  The returned instance
 * uses default values of {@code minThreads = 1} and
 * {@code scaleUpThreshold = }{@value #DEFAULT_SCALE_UP_THRESHOLD}.  Override these with
 * {@link #minThreads(int)} and {@link #scaleUpThreshold(int)}, both of which return a new
 * immutable instance.</p>
 *
 * <h2>Scale signal semantics</h2>
 *
 * <p>An iteration is counted as a <em>miss</em> (scale-down trigger) when zero items were
 * forwarded downstream — that is, when a single-item supplier returns {@code null}, or a batch
 * supplier returns {@code null} or an empty list.  An iteration is counted as a
 * <em>success</em> when at least one item was forwarded.  After {@link #scaleUpThreshold}
 * consecutive successes the active thread count increases by one and the counter resets.  Any
 * miss resets the counter and decreases the active thread count by one (down to
 * {@link #minThreads}).</p>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // Defaults: minThreads = 1, scaleUpThreshold = 10
 * SourceConcurrencyPolicy.adaptive(16)
 *
 * // Custom floor and threshold
 * SourceConcurrencyPolicy.adaptive(16)
 *         .minThreads(2)
 *         .scaleUpThreshold(5)
 * }</pre>
 *
 * @see SourceConcurrencyPolicy#adaptive(int)
 * @see FixedConcurrencyPolicy
 */
public final class AdaptiveConcurrencyPolicy implements SourceConcurrencyPolicy {
    static final int DEFAULT_SCALE_UP_THRESHOLD = 10;

    private final int minThreads;
    private final int maxThreads;
    private final int scaleUpThreshold;

    AdaptiveConcurrencyPolicy(int minThreads, int maxThreads, int scaleUpThreshold) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.scaleUpThreshold = scaleUpThreshold;
    }

    /**
     * Returns a new policy with the specified minimum thread floor.  The active thread count will
     * never drop below this value, even when the supplier consistently returns empty results.
     *
     * @param minThreads The minimum number of active threads; must be at least {@code 1} and
     *                   strictly less than {@link #maxThreads()}.
     * @return A new {@code AdaptiveConcurrencyPolicy} with the updated minimum thread count.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code minThreads} is not in the range
     *                                                                           {@code [1, maxThreads - 1]}.
     */
    public AdaptiveConcurrencyPolicy minThreads(int minThreads) {
        Numbers.range("minThreads", minThreads, 1, this.maxThreads - 1);

        return new AdaptiveConcurrencyPolicy(minThreads, this.maxThreads, this.scaleUpThreshold);
    }

    /**
     * Returns a new policy with the specified scale-up threshold.  After this many consecutive
     * successful iterations (each forwarding at least one item downstream), the active thread
     * count is increased by one and the counter resets.
     *
     * @param scaleUpThreshold The number of consecutive successful iterations required to add one
     *                         thread; must be at least {@code 1}.
     * @return A new {@code AdaptiveConcurrencyPolicy} with the updated scale-up threshold.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code scaleUpThreshold} is less than {@code 1}.
     */
    public AdaptiveConcurrencyPolicy scaleUpThreshold(int scaleUpThreshold) {
        Numbers.positive("scaleUpThreshold", scaleUpThreshold);

        return new AdaptiveConcurrencyPolicy(this.minThreads, this.maxThreads, scaleUpThreshold);
    }

    /**
     * Returns the minimum number of active supplier-polling threads.
     *
     * @return The configured minimum thread count.
     */
    public int minThreads() {
        return this.minThreads;
    }

    /**
     * Returns the maximum number of concurrent supplier-polling threads.
     *
     * @return The configured maximum thread count.
     */
    @Override
    public int maxThreads() {
        return this.maxThreads;
    }

    /**
     * Returns the number of consecutive successful iterations required before the active thread
     * count is increased by one.
     *
     * @return The configured scale-up threshold.
     */
    public int scaleUpThreshold() {
        return this.scaleUpThreshold;
    }
}

