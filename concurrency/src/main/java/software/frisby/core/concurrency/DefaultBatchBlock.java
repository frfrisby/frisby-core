package software.frisby.core.concurrency;


import software.frisby.core.validation.Durations;
import software.frisby.core.validation.Numbers;
import software.frisby.core.validation.Values;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class DefaultBatchBlock<T> implements BatchBlock<T> {
    static final int DEFAULT_CAPACITY = 1024;
    static final int DEFAULT_BATCH_SIZE = 128;
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final AsyncBatch<T> batch;
    private final TargetManager<List<T>> targetManager;
    private final int capacity;

    private final ItemPostedManager<T> postedManager;
    private final AtomicBoolean completed;
    private final AtomicInteger pendingCompletes;
    private final CompletableFuture<Void> completionFuture;

    DefaultBatchBlock(int capacity,
                      int batchSize,
                      Duration timeout,
                      Executor executor,
                      ItemPostedHandler<T> itemPostedHandler,
                      ItemDeliveredHandler<List<T>> itemDeliveredHandler,
                      ErrorOccurredHandler<List<T>> errorOccurredHandler) {
        Numbers.positive("capacity", capacity);
        Numbers.positive("batchSize", batchSize);
        Durations.positive("timeout", timeout);
        Values.notNull("executor", executor);

        EventSource eventSource = new EventSource(BatchBlock.class.getSimpleName());
        this.targetManager = new TargetManager<>(this, eventSource, itemDeliveredHandler, errorOccurredHandler);
        this.postedManager = new ItemPostedManager<>(this, eventSource, itemPostedHandler);

        this.batch = new AsyncBatch<>(new ArrayBlockingQueue<>(capacity), capacity, this.targetManager::postToTarget, batchSize, timeout, executor);

        this.capacity = capacity;

        this.completed = new AtomicBoolean(false);
        this.pendingCompletes = new AtomicInteger(0);
        this.completionFuture = new CompletableFuture<>();
    }

    @Override
    public int capacity() {
        return this.capacity;
    }

    @Override
    public int batchSize() {
        return this.batch.batchSize();
    }

    @Override
    public Duration timeout() {
        return this.batch.timeout();
    }

    @Override
    public int size() {
        return this.batch.size();
    }

    @Override
    public int inFlight() {
        return size() + this.targetManager.inFlight();
    }

    @Override
    public boolean post(T item) {
        if (this.completed.get()) {
            return false;
        }

        this.targetManager.awaitTargets();

        if (this.batch.post(item)) {
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

        if (this.batch.post(item, timeout)) {
            this.postedManager.sendOnPostedNotification(item, true);
            return true;
        }

        return false;
    }

    @Override
    public void linkTo(Target<List<T>> target) {
        this.targetManager.add(target);
    }

    boolean isRunning() {
        return this.batch.isRunning();
    }

    @Override
    public void onLinked() {
        this.pendingCompletes.incrementAndGet();
    }

    @Override
    public void complete() {
        if (this.pendingCompletes.decrementAndGet() <= 0) {
            if (this.completed.compareAndSet(false, true)) {
                this.batch.complete()
                        .thenRun(() -> {
                            this.targetManager.complete();
                            this.targetManager.completion()
                                    .thenAccept(v -> this.completionFuture.complete(null));
                        });
            }
        }
    }

    @Override
    public CompletableFuture<Void> completion() {
        return this.completionFuture;
    }
}
