package software.frisby.core.concurrency;

final class DefaultNamedExecutorServiceBuilder implements NamedExecutorServiceBuilder {
    private String threadPrefix;

    DefaultNamedExecutorServiceBuilder() {
        this.threadPrefix = "Pipeline";
    }

    @Override
    public NamedExecutorServiceBuilder threadPrefix(String threadPrefix) {
        this.threadPrefix = threadPrefix;
        return this;
    }

    @Override
    public NamedExecutorService build() {
        return new DefaultNamedExecutorService(this.threadPrefix);
    }
}
