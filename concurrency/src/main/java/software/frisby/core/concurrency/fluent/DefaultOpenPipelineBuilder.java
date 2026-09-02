package software.frisby.core.concurrency.fluent;

import java.util.concurrent.ExecutorService;

final class DefaultOpenPipelineBuilder implements OpenPipelineBuilder {
    private ExecutorService executor;

    @Override
    public OpenPipelineBuilder executor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public <I, N> OpenChain<I, I, N> from(PipelineStage<I, N> source) {
        return OpenChain.from(source, executor);
    }

    @Override
    public <I, N> OpenChain<I, I, N> from(OpenPipeline<I, N> pipeline) {
        return OpenChain.from(pipeline, executor);
    }
}

