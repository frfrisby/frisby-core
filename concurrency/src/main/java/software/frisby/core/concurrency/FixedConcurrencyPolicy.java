package software.frisby.core.concurrency;

/**
 * A {@link SourceConcurrencyPolicy} that runs a constant number of threads, each calling the
 * supplier concurrently with no throttling.
 *
 * <p>Obtain an instance via {@link SourceConcurrencyPolicy#fixed(int)}.</p>
 *
 * @see SourceConcurrencyPolicy#fixed(int)
 * @see AdaptiveConcurrencyPolicy
 */
public final class FixedConcurrencyPolicy implements SourceConcurrencyPolicy {
    private final int threads;

    FixedConcurrencyPolicy(int threads) {
        this.threads = threads;
    }

    /**
     * Returns the number of concurrent supplier-polling threads.
     *
     * @return The configured thread count.
     */
    @Override
    public int maxThreads() {
        return this.threads;
    }
}

