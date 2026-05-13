package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Source;
import software.frisby.core.concurrency.Target;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class DefaultOpenPipeline<I, O> implements OpenPipeline<I, O> {
    private final Target<I> head;
    private final List<Source<O>> tails;

    DefaultOpenPipeline(Target<I> head, List<Source<O>> tails) {
        Values.notNull("head", head);
        Sequences.notEmpty("tails", tails);

        this.head = head;
        this.tails = tails;
    }

    @Override
    public boolean post(I item) {
        return this.head.post(item);
    }

    @Override
    public int size() {
        return this.head.size();
    }

    @Override
    public int inFlight() {
        return this.head.inFlight();
    }

    @Override
    public void onLinked() {
        this.head.onLinked();
    }

    @Override
    public void complete() {
        this.head.complete();
    }

    @Override
    public CompletableFuture<Void> completion() {
        return this.head.completion();
    }

    @Override
    public void linkTo(Target<O> target) {
        for (Source<O> tail : this.tails) {
            tail.linkTo(target);
        }
    }
}
