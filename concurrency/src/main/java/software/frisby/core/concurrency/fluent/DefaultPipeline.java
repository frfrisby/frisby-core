package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.Target;
import software.frisby.core.validation.Values;

import java.util.concurrent.CompletableFuture;

final class DefaultPipeline<T> implements Pipeline<T> {
    private final Target<T> head;

    DefaultPipeline(Target<T> head) {
        Values.notNull("head", head);
        this.head = head;
    }

    @Override
    public boolean post(T item) {
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
}
