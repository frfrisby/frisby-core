package software.frisby.core.concurrency;

import software.frisby.core.validation.Durations;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class DefaultBufferBlock<T> implements BufferBlock<T> {
    static final int DEFAULT_CAPACITY = 1024;

    private final AsyncBuffer<T> buffer;
    private final TargetManager<T> targetManager;
    private final int capacity;

    private final ItemPostedManager<T> postedManager;
    private final AtomicBoolean completed;
    private final AtomicInteger pendingCompletes;
    private final CompletableFuture<Void> completionFuture;

    DefaultBufferBlock(int capacity,
                       Executor executor,
                       ItemPostedHandler<T> itemPostedHandler,
                       ItemDeliveredHandler<T> itemDeliveredHandler,
                       ErrorOccurredHandler<T> errorOccurredHandler) {
        Numbers.positive("capacity", capacity);
        Values.notNull("executor", executor);

        EventSource eventSource = new EventSource(BufferBlock.class.getSimpleName());

        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler, errorOccurredHandler);
        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);

        this.buffer = new AsyncBuffer<>(new ArrayBlockingQueue<>(capacity), capacity, this.targetManager::postToTarget, executor);

        this.capacity = capacity;

        this.completed = new AtomicBoolean(false);
        this.pendingCompletes = new AtomicInteger(0);
        this.completionFuture = new CompletableFuture<>();
    }

    @Override
    public boolean post(T item) {
        if (this.completed.get()) {
            return false;
        }

        this.targetManager.awaitTargets();

        if (this.buffer.post(item)) {
            this.postedManager.sendOnPostedNotification(item, true);
            return true;
        }

        return false;
    }

    @Override
    public boolean post(T item, Duration timeout) {
        if (this.completed.get()) {
            return false;
        }

        Durations.positive("timeout", timeout);

        this.targetManager.awaitTargets();

        if (this.buffer.post(item, timeout)) {
            this.postedManager.sendOnPostedNotification(item, true);
            return true;
        }

        return false;
    }

    @Override
    public void linkTo(Target<T> target) {
        this.targetManager.add(target);
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    @Override
    public int size() {
        return this.buffer.size();
    }

    @Override
    public int inFlight() {
        return size() + this.targetManager.inFlight();
    }

    @Override
    public void onLinked() {
        this.pendingCompletes.incrementAndGet();
    }

    @Override
    public void complete() {
        if (this.pendingCompletes.decrementAndGet() <= 0 &&
                this.completed.compareAndSet(false, true)) {
            this.buffer.complete()
                    .thenRun(() -> {
                        this.targetManager.complete();
                        this.targetManager.completion()
                                .thenAccept(v -> this.completionFuture.complete(null));
                    });
        }
    }

    @Override
    public CompletableFuture<Void> completion() {
        return this.completionFuture;
    }

    boolean isRunning() {
        return this.buffer.isRunning();
    }
}
