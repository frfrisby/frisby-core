package software.frisby.core.concurrency;

/**
 * Builder for constructing a {@link NamedExecutorService}.  Obtain an instance via
 * {@link NamedExecutorService#builder()}.
 */
public interface NamedExecutorServiceBuilder {
    /**
     * Optional. Sets the prefix applied to the name of each background thread created by the executor.
     * For example, a prefix of {@code "DevicePipeline"} produces threads named
     * {@code "DevicePipeline-1"}, {@code "DevicePipeline-2"}, and so on.
     * Defaults to {@code "Pipeline"} if not set.
     *
     * @param threadPrefix The prefix applied to every thread name.
     * @return This builder, for method chaining.
     */
    NamedExecutorServiceBuilder threadPrefix(String threadPrefix);

    /**
     * Returns a new {@link NamedExecutorService} configured by this builder.
     *
     * @return A new {@link NamedExecutorService} instance.
     */
    NamedExecutorService build();
}
