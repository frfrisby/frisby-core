package software.frisby.core.concurrency;

import java.time.Duration;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

final class DelayedEntry<T> implements Delayed {
    private final long releaseWhen;
    private final T item;

    DelayedEntry(T item, Duration duration) {
        this.item = item;
        this.releaseWhen = System.currentTimeMillis() + duration.toMillis();
    }

    T item() {
        return this.item;
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        return timeUnit.convert(this.releaseWhen - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed delayed) {
        return Long.compare(
                this.getDelay(TimeUnit.MILLISECONDS),
                delayed.getDelay(TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public String toString() {
        long remainingMs = this.releaseWhen - System.currentTimeMillis();

        return "DelayedEntry{remainingMs=" + remainingMs + ", item=" + this.item + "}";
    }
}