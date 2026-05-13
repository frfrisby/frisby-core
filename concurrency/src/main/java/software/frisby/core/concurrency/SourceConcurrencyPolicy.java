package software.frisby.core.concurrency;

import software.frisby.core.validation.Numbers;

/**
 * Defines the threading strategy used by a {@link SourceBlock} to invoke its configured supplier.
 *
 * <p>Two implementations are available:</p>
 * <ul>
 *   <li>{@link FixedConcurrencyPolicy} — a constant number of threads each call the supplier
 *       concurrently with no throttling.  Constructed via {@link #fixed(int)}.</li>
 *   <li>{@link AdaptiveConcurrencyPolicy} — starts at a configurable floor and scales up toward
 *       a ceiling as the supplier consistently returns results, then scales back down when the
 *       supplier begins returning empty results.  Constructed via {@link #adaptive(int)}.</li>
 * </ul>
 *
 * <p>When no policy is configured on a {@link SourceBlockBuilder}, the block defaults to a single
 * thread with no throttling — equivalent to {@code SourceConcurrencyPolicy.fixed(1)}.</p>
 *
 * <h2>Thread-safety requirement</h2>
 *
 * <p>When {@link #maxThreads()} is greater than {@code 1} the configured supplier will be called
 * concurrently by multiple threads.  The supplier <em>must</em> be thread-safe, and the first
 * downstream stage should typically be an async block that owns an internal queue — such as
 * {@link BatchBlock}, {@link BufferBlock}, {@link DelayBlock}, {@link GroupBlock}, or
 * {@link PriorityBufferBlock} — so that concurrent posts are absorbed safely without blocking
 * the supplier threads.</p>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * // Fixed — always run exactly 4 threads
 * SourceConcurrencyPolicy.fixed(4)
 *
 * // Adaptive — start at 1, grow up to 16, default scaleUpThreshold of 10
 * SourceConcurrencyPolicy.adaptive(16)
 *
 * // Adaptive — custom floor and threshold
 * SourceConcurrencyPolicy.adaptive(16)
 *         .minThreads(2)
 *         .scaleUpThreshold(5)
 * }</pre>
 *
 * @see FixedConcurrencyPolicy
 * @see AdaptiveConcurrencyPolicy
 * @see SourceBlockBuilder#concurrencyPolicy(SourceConcurrencyPolicy)
 */
public interface SourceConcurrencyPolicy {
    /**
     * Returns a fixed-concurrency policy that runs exactly {@code threads} threads, each calling
     * the supplier concurrently with no throttling.
     *
     * @param threads The number of threads; must be at least {@code 1}.
     * @return A new {@link FixedConcurrencyPolicy} configured for fixed concurrency.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code threads} is less than {@code 1}.
     */
    static FixedConcurrencyPolicy fixed(int threads) {
        Numbers.positive("threads", threads);

        return new FixedConcurrencyPolicy(threads);
    }

    /**
     * Returns an adaptive-concurrency policy that starts at {@code 1} active thread and scales up
     * toward {@code maxThreads} as the supplier consistently returns results.  The active count
     * scales back down when the supplier begins returning empty results.
     *
     * <p>Defaults: {@code minThreads = 1}, {@code scaleUpThreshold = 10}.  Override these with
     * {@link AdaptiveConcurrencyPolicy#minThreads(int)} and
     * {@link AdaptiveConcurrencyPolicy#scaleUpThreshold(int)}.</p>
     *
     * @param maxThreads The maximum number of concurrent threads; must be at least {@code 2}.
     * @return A new {@link AdaptiveConcurrencyPolicy} configured for adaptive concurrency.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code maxThreads} is less than {@code 2}.
     */
    static AdaptiveConcurrencyPolicy adaptive(int maxThreads) {
        Numbers.min("maxThreads", maxThreads, 2);

        return new AdaptiveConcurrencyPolicy(1, maxThreads, AdaptiveConcurrencyPolicy.DEFAULT_SCALE_UP_THRESHOLD);
    }

    /**
     * Returns the maximum number of concurrent supplier-polling threads.
     *
     * @return The maximum thread count.
     */
    int maxThreads();
}

