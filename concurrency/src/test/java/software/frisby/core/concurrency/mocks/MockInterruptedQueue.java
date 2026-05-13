package software.frisby.core.concurrency.mocks;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class MockInterruptedQueue<T> implements BlockingQueue<T> {
    private final CountDownLatch putLatch;
    private final CountDownLatch offerLatch;
    private final CountDownLatch takeLatch;
    private final CountDownLatch pollLatch;

    private final AtomicInteger putInvokes;
    private final AtomicInteger offerInvokes;
    private final AtomicInteger takeInvokes;
    private final AtomicInteger pollInvokes;

    public MockInterruptedQueue() {
        this.putLatch = new CountDownLatch(1);
        this.offerLatch = new CountDownLatch(1);
        this.takeLatch = new CountDownLatch(1);
        this.pollLatch = new CountDownLatch(1);

        this.putInvokes = new AtomicInteger(0);
        this.offerInvokes = new AtomicInteger(0);
        this.takeInvokes = new AtomicInteger(0);
        this.pollInvokes = new AtomicInteger(0);
    }

    public int putInvokes() {
        return this.putInvokes.get();
    }

    public int offerInvokes() {
        return this.offerInvokes.get();
    }

    public int takeInvokes() {
        return this.takeInvokes.get();
    }

    public int pollInvokes() {
        return this.pollInvokes.get();
    }

    public boolean awaitPut(long timeout, TimeUnit unit) throws InterruptedException {
        return this.putLatch.await(timeout, unit);
    }

    public boolean awaitOffer(long timeout, TimeUnit unit) throws InterruptedException {
        return this.offerLatch.await(timeout, unit);
    }

    public boolean awaitTake(long timeout, TimeUnit unit) throws InterruptedException {
        return this.takeLatch.await(timeout, unit);
    }

    public boolean awaitPoll(long timeout, TimeUnit unit) throws InterruptedException {
        return this.pollLatch.await(timeout, unit);
    }

    // The following methods are implemented to track invocations of the put(), offer(), take(), and poll() methods
    // that will throw InterruptedException to simulate blocking behavior.

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void put(T t) throws InterruptedException {
        this.putInvokes.incrementAndGet();
        this.putLatch.countDown();

        throw new InterruptedException();
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        this.offerInvokes.incrementAndGet();
        this.offerLatch.countDown();

        throw new InterruptedException();
    }

    @Override
    public T take() throws InterruptedException {
        this.takeInvokes.incrementAndGet();
        this.takeLatch.countDown();

        throw new InterruptedException();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        this.pollInvokes.incrementAndGet();
        this.pollLatch.countDown();

        throw new InterruptedException();
    }

    @Override
    public int remainingCapacity() {
        return 10;
    }

    // The remaining methods are not relevant to the test and will throw UnsupportedOperationException if invoked.

    @Override
    public boolean offer(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<T> stream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<T> parallelStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T peek() {
        throw new UnsupportedOperationException();
    }
}
