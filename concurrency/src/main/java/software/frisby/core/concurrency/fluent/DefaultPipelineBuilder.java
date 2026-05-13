package software.frisby.core.concurrency.fluent;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

final class DefaultPipelineBuilder<T> implements PipelineBuilder<T> {
    private Executor executor;

    @Override
    public PipelineBuilder<T> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public <N> Chain<T, N> from(PipelineStage<T, N> source) {
        return Chain.from(source, executor);
    }

    @Override
    public <N> Chain<T, N> from(OpenPipeline<T, N> pipeline) {
        return Chain.from(pipeline, executor);
    }

    @Override
    public Pipeline<T> from(PipelineTarget<T> terminal) {
        return new DefaultPipeline<>(terminal.toTarget());
    }

    @Override
    public Pipeline<T> from(Consumer<T> consumer) {
        return from(Action.<T>of().action(consumer));
    }
}
