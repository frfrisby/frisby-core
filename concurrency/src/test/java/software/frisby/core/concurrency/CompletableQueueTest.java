package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.mocks.MockInterruptedQueue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CompletableQueueTest {
    private static boolean awaitCompletion(CompletableFuture<Void> completableFuture, Duration timeout) {
        try {
            completableFuture.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (TimeoutException ex) {
            return false;
        } catch (ExecutionException ex) {
            // Completion futures in this library always complete normally, never exceptionally.
            return true;
        }
    }

    @Nested
    class Initialization {
        @Test
        void nullQueue_throwsNullValueException() {
            assertThrows(
                    software.frisby.core.validation.NullValueException.class,
                    () -> new CompletableQueue<>(null)
            );
        }

        @Test
        void capacity() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
        }

        @Test
        void size() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
        }

        @Test
        void isEmpty() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
        }

        @Test
        void isCompleted() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");
        }

        @Test
        void completionFutureNotDoneBeforeComplete() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            assertFalse(completableQueue.completion().isDone(), "The completion future should not be done before complete() is called.");
        }
    }

    @Nested
    class OnLinked {
        @Test
        void onLinkedOnce_singleCompleteTransitions() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.onLinked();

            completableQueue.complete();

            assertTrue(completableQueue.isCompleted(), "The queue should be complete after one onLinked() and one complete().");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The completion future should be done.");
        }

        @Test
        void onLinkedTwice_firstCompleteDoesNotTransition() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.onLinked();
            completableQueue.onLinked();

            completableQueue.complete();

            assertFalse(completableQueue.isCompleted(), "The queue should not be complete after only one of two required complete() calls.");
            assertFalse(completableQueue.completion().isDone(), "The completion future should not be done yet.");
        }

        @Test
        void onLinkedTwice_secondCompleteTransitions() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.onLinked();
            completableQueue.onLinked();

            completableQueue.complete();
            assertFalse(completableQueue.isCompleted(), "The queue should not be complete after the first of two complete() calls.");

            completableQueue.complete();
            assertTrue(completableQueue.isCompleted(), "The queue should be complete after the second complete() call.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The completion future should be done.");
        }

        @Test
        void onLinkedThreeTimes_completesOnlyOnThirdComplete() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.onLinked();
            completableQueue.onLinked();
            completableQueue.onLinked();

            completableQueue.complete();
            assertFalse(completableQueue.isCompleted(), "The queue should not be complete after the first complete().");

            completableQueue.complete();
            assertFalse(completableQueue.isCompleted(), "The queue should not be complete after the second complete().");

            completableQueue.complete();
            assertTrue(completableQueue.isCompleted(), "The queue should be complete after the third complete().");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The completion future should be done.");
        }
    }

    @Nested
    class Sync {
        @Test
        void interruptOnEnqueue_returnsFalse() throws Exception {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(1));

            boolean result = completableQueue.enqueue(101);
            assertTrue(result, "The queue enqueue should return the expected value.");

            CountDownLatch producerExitLatch = new CountDownLatch(1);
            AtomicBoolean enqueueResult = new AtomicBoolean(true);

            Thread producer = new Thread(() -> {
                completableQueue.onLinked();

                enqueueResult.set(completableQueue.enqueue(102));
                producerExitLatch.countDown();
            });
            producer.start();

            // Spin until the producer is confirmed parked in Condition.await().
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (producer.getState() != Thread.State.WAITING &&
                    System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }

            producer.interrupt();
            producer.join();

            assertFalse(enqueueResult.get(), "The queue enqueue should return the expected value.");
        }

        @Test
        void interruptOnEnqueueWithTimeout_returnsFalse() throws Exception {
            MockInterruptedQueue<Integer> mockQueue = new MockInterruptedQueue<>();
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(mockQueue);

            CountDownLatch producerExitLatch = new CountDownLatch(1);
            AtomicBoolean enqueueResult = new AtomicBoolean(true);

            Thread producer = new Thread(() -> {
                completableQueue.onLinked();

                enqueueResult.set(completableQueue.enqueue(102, 10, TimeUnit.SECONDS));
                producerExitLatch.countDown();
            });
            producer.start();
            producer.join();

            boolean signaled = mockQueue.awaitOffer(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer did not invoke offer() within the timeout period.");
            assertEquals(1, mockQueue.offerInvokes(), "The queue offers the expected number of times.");
            assertFalse(enqueueResult.get(), "The queue enqueue should return the expected value.");
        }

        @Test
        void enqueueWithTimeout_returnsFalse() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(1));

            boolean result = completableQueue.enqueue(101);
            assertTrue(result, "The queue enqueue should return the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");

            result = completableQueue.enqueue(102, 100, TimeUnit.MILLISECONDS);
            assertFalse(result, "The queue enqueue should return the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");
        }

        @Test
        void enqueueWithNullValue_returnsFalse() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            boolean result = completableQueue.enqueue(null);
            assertFalse(result, "The queue enqueue should return the expected value.");

            result = completableQueue.enqueue(null, 10, TimeUnit.MILLISECONDS);
            assertFalse(result, "The queue enqueue should return the expected value.");
        }

        @Test
        void enqueueAfterComplete_returnsFalse() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.complete();

            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            boolean result = completableQueue.enqueue(101);
            assertFalse(result, "The queue enqueue should return the expected value.");

            result = completableQueue.enqueue(101, 10, TimeUnit.MILLISECONDS);
            assertFalse(result, "The queue enqueue should return the expected value.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");
        }

        @Test
        void enqueueDequeue_allReceived() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            boolean result = completableQueue.enqueue(101);

            assertTrue(result, "The queue enqueue should return the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            Integer value = completableQueue.dequeue();
            assertEquals(101, value, "The queue value does not match the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            completableQueue.complete();
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            result = completableQueue.enqueue(202);
            assertFalse(result, "The queue enqueue should return the expected value.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");
        }

        @Test
        void notEmptyOnComplete_allReceived() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            boolean result = completableQueue.enqueue(101);
            assertTrue(result, "The queue enqueue should return the expected value.");

            result = completableQueue.enqueue(102);
            assertTrue(result, "The queue enqueue should return the expected value.");

            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(2, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            Integer value = completableQueue.dequeue();
            assertEquals(101, value, "The queue value does not match the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            completableQueue.complete();
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            value = completableQueue.dequeue();
            assertEquals(102, value, "The queue value does not match the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            result = completableQueue.enqueue(202);
            assertFalse(result, "The queue enqueue should return the expected value.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");
        }

        @Test
        void multipleComplete_allReceived() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            boolean result = completableQueue.enqueue(101);

            assertTrue(result, "The queue enqueue should return the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            Integer value = completableQueue.dequeue();
            assertEquals(101, value, "The queue value does not match the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            completableQueue.complete();
            completableQueue.complete();

            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            result = completableQueue.enqueue(202);
            assertFalse(result, "The queue enqueue should return the expected value.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");
        }

        @Test
        void dequeueAfterComplete_allReceived() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            boolean result = completableQueue.enqueue(101);

            assertTrue(result, "The queue enqueue should return the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(1, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            Integer value = completableQueue.dequeue();
            assertEquals(101, value, "The queue value does not match the expected value.");
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");

            completableQueue.complete();
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(0, completableQueue.size(), "The queue size does not match the expected value.");
            assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            result = completableQueue.enqueue(202);
            assertFalse(result, "The queue enqueue should return the expected value.");

            assertNull(completableQueue.dequeue(), "The queue dequeue should return null.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");
        }

        @Test
        void timedDequeueOnNonEmptyQueue_returnsItem() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.enqueue(42);

            Integer result = completableQueue.dequeue(1, TimeUnit.SECONDS);

            assertEquals(42, result, "The timed dequeue should return the enqueued item.");
            assertTrue(completableQueue.isEmpty(), "The queue should be empty after dequeue.");
        }

        @Test
        void timedDequeueOnEmptyNotCompletedQueue_returnsNullAndFutureNotDone() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            Integer result = completableQueue.dequeue(50, TimeUnit.MILLISECONDS);

            assertNull(result, "The timed dequeue should return null when the timeout elapses with no item.");
            assertFalse(completableQueue.completion().isDone(), "The completion future must not be done when the queue is not yet completed.");
        }

        @Test
        void timedDequeueOnEmptyCompletedQueue_returnsNullAndFutureDone() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));
            completableQueue.complete();

            Integer result = completableQueue.dequeue(50, TimeUnit.MILLISECONDS);

            assertNull(result, "The timed dequeue should return null on an empty completed queue.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The completion future should be done after dequeue from an empty completed queue.");
        }

        @Test
        void dequeueAfterCompleteAndNotEmpty_allReceived() {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10));

            for (int i = 0; i < completableQueue.capacity(); i++) {
                boolean result = completableQueue.enqueue(i);
                assertTrue(result, "The queue enqueue should return the expected value.");
                assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
                assertEquals(i + 1, completableQueue.size(), "The queue size does not match the expected value.");
                assertFalse(completableQueue.isEmpty(), "The queue is empty.");
                assertFalse(completableQueue.isCompleted(), "The queue is marked complete.");
            }

            completableQueue.complete();
            assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
            assertEquals(10, completableQueue.size(), "The queue size does not match the expected value.");
            assertFalse(completableQueue.isEmpty(), "The queue is empty.");
            assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");

            boolean result = completableQueue.enqueue(202);
            assertFalse(result, "The queue enqueue should return the expected value.");

            for (int i = 0; i < completableQueue.capacity(); i++) {
                Integer value = completableQueue.dequeue();
                assertEquals(i, value, "The queue value does not match the expected value.");
                assertEquals(10, completableQueue.capacity(), "The queue capacity does not match the expected value.");
                assertEquals(10 - (i + 1), completableQueue.size(), "The queue size does not match the expected value.");

                if (i == 9) {
                    assertTrue(completableQueue.isEmpty(), "The queue is not empty.");
                } else {
                    assertFalse(completableQueue.isEmpty(), "The queue is empty.");
                }

                assertTrue(completableQueue.isCompleted(), "The queue is not marked complete.");
            }

            assertNull(completableQueue.dequeue(), "The queue dequeue should return null.");

            boolean signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");
        }
    }

    @Nested
    class Async {
        @Test
        void interruptOnDequeueAndPoll_allReceived() throws Exception {
            MockInterruptedQueue<Integer> mockQueue = new MockInterruptedQueue<>();
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(mockQueue);

            // Signal fired by the consumer thread immediately before it calls dequeue(), ensuring
            // that the only WAITING state the consumer can be in once the main thread starts
            // spinning is notEmpty.await() inside CompletableQueue.dequeue().  Using a start latch
            // here would introduce a competing WAITING state (the latch park) that the spin loop
            // could mistake for the target condition.
            CountDownLatch aboutToDequeueSignal = new CountDownLatch(1);
            CountDownLatch consumerExitLatch = new CountDownLatch(1);
            AtomicBoolean interrupted = new AtomicBoolean(false);

            Thread consumer = new Thread(() -> {
                aboutToDequeueSignal.countDown();

                completableQueue.dequeue();

                interrupted.set(Thread.currentThread().isInterrupted());
                consumerExitLatch.countDown();
            });

            consumer.start();

            boolean signaled = aboutToDequeueSignal.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer did not signal readiness within the timeout period.");

            // Spin until the consumer is confirmed parked in notEmpty.await() inside dequeue().
            // Since aboutToDequeueSignal has already fired, the only WAITING state reachable from
            // this point is the Condition.await() call — there is no competing latch park.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);

            while (consumer.getState() != Thread.State.WAITING &&
                    System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }

            // Interrupt the consumer thread...
            consumer.interrupt();
            consumer.join();

            signaled = consumerExitLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer did not complete within the timeout period.");

            assertTrue(interrupted.get(), "The interrupt flag was not set on the consumer thread after dequeue() returned.");
            assertTrue(consumer.isInterrupted(), "The consumer thread is not in interrupted state.");

            signaled = mockQueue.awaitPoll(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer did not invoke poll() within the timeout period.");
            assertEquals(1, mockQueue.pollInvokes(), "The queue polls the expected number of times.");
        }

        @Test
        void blockingEnqueue_allReceived() throws Exception {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10, true));

            Set<Integer> received = new HashSet<>();
            CountDownLatch consumerReadyLatch = new CountDownLatch(1);
            CountDownLatch consumerStartLatch = new CountDownLatch(1);
            CountDownLatch consumerExitLatch = new CountDownLatch(1);

            Thread consumer = new Thread(() -> {
                try {
                    boolean exit = false;
                    while (!exit) {
                        consumerReadyLatch.countDown();
                        consumerStartLatch.await();

                        Integer value = completableQueue.dequeue();
                        if (null != value) {
                            received.add(value);
                        } else {
                            exit = completableQueue.isCompleted();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                consumerExitLatch.countDown();
            });

            int totalProducers = 10;
            List<Thread> producers = new ArrayList<>();
            CountDownLatch producerReadyLatch = new CountDownLatch(totalProducers);
            CountDownLatch producerStartLatch = new CountDownLatch(1);
            CountDownLatch producerExitLatch = new CountDownLatch(totalProducers);
            AtomicInteger producerCount = new AtomicInteger(0);
            AtomicInteger blockedCount = new AtomicInteger(0);

            for (int i = 0; i < totalProducers; i++) {
                producers.add(new Thread(() -> {
                    completableQueue.onLinked();

                    try {
                        for (int j = 0; j < 10 * totalProducers; j++) {
                            producerReadyLatch.countDown();
                            producerStartLatch.await();

                            int value = producerCount.getAndIncrement();
                            boolean result = completableQueue.enqueue(value);
                            if (!result) {
                                blockedCount.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    completableQueue.complete();
                    producerExitLatch.countDown();
                }));
            }

            List<Thread> allThreads = new ArrayList<>();
            allThreads.add(consumer);
            allThreads.addAll(producers);

            // Start the consumer and all producer threads...
            for (Thread worker : allThreads) {
                worker.start();
            }

            boolean signaled = consumerReadyLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer was not ready within the timeout period.");

            signaled = producerReadyLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The producers were not ready within the timeout period.");

            consumerStartLatch.countDown();
            producerStartLatch.countDown();

            signaled = consumerExitLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer did not complete within the timeout period.");

            signaled = producerExitLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The producers did not complete within the timeout period.");

            assertTrue(completableQueue.isCompleted(), "The queue is not complete after producers exited.");
            assertFalse(completableQueue.enqueue(-1), "The queue accepted an item after being set to complete.");
            assertEquals(0, blockedCount.get(), "Some producers were blocked when they should not have been.");

            signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");

            int lastValue = producerCount.get();
            assertEquals(lastValue, received.size(), "The number of received items does not match the number of produced items.");

            for (int i = 0; i < 10 * totalProducers; i++) {
                assertTrue(received.contains(i), "The producer value was not received by the consumer.");
            }

            // Wait for all threads to complete...
            for (Thread worker : allThreads) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Test
        void blockingEnqueueWithTimeout_allReceived() throws Exception {
            CompletableQueue<Integer> completableQueue = new CompletableQueue<>(new ArrayBlockingQueue<>(10, true));

            Set<Integer> received = new HashSet<>();
            CountDownLatch consumerReadyLatch = new CountDownLatch(1);
            CountDownLatch consumerStartLatch = new CountDownLatch(1);
            CountDownLatch consumerExitLatch = new CountDownLatch(1);

            Thread consumer = new Thread(() -> {
                try {
                    boolean exit = false;
                    while (!exit) {
                        consumerReadyLatch.countDown();
                        consumerStartLatch.await();

                        Integer value = completableQueue.dequeue();
                        if (null != value) {
                            received.add(value);
                        } else {
                            exit = completableQueue.isCompleted();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                consumerExitLatch.countDown();
            });

            int totalProducers = 10;
            List<Thread> producers = new ArrayList<>();
            CountDownLatch producerReadyLatch = new CountDownLatch(totalProducers);
            CountDownLatch producerStartLatch = new CountDownLatch(1);
            CountDownLatch producerExitLatch = new CountDownLatch(totalProducers);
            AtomicInteger producerCount = new AtomicInteger(0);
            AtomicInteger blockedCount = new AtomicInteger(0);

            for (int i = 0; i < totalProducers; i++) {
                producers.add(new Thread(() -> {
                    completableQueue.onLinked();

                    try {
                        for (int j = 0; j < 10 * totalProducers; j++) {
                            producerReadyLatch.countDown();
                            producerStartLatch.await();

                            int value = producerCount.getAndIncrement();
                            boolean result = completableQueue.enqueue(value, 10, TimeUnit.SECONDS);
                            if (!result) {
                                blockedCount.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    completableQueue.complete();
                    producerExitLatch.countDown();
                }));
            }

            List<Thread> allThreads = new ArrayList<>();
            allThreads.add(consumer);
            allThreads.addAll(producers);

            // Start the consumer and all producer threads...
            for (Thread worker : allThreads) {
                worker.start();
            }

            boolean signaled = consumerReadyLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer was not ready within the timeout period.");

            signaled = producerReadyLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The producers were not ready within the timeout period.");

            consumerStartLatch.countDown();
            producerStartLatch.countDown();

            signaled = consumerExitLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The consumer did not complete within the timeout period.");

            signaled = producerExitLatch.await(10, TimeUnit.SECONDS);
            assertTrue(signaled, "The producers did not complete within the timeout period.");

            assertTrue(completableQueue.isCompleted(), "The queue is not complete after producers exited.");
            assertFalse(completableQueue.enqueue(-1), "The queue accepted an item after being set to complete.");
            assertEquals(0, blockedCount.get(), "Some producers were blocked when they should not have been.");

            signaled = awaitCompletion(completableQueue.completion(), Duration.ofSeconds(10));
            assertTrue(signaled, "The queue manager did not complete within the timeout period.");

            int lastValue = producerCount.get();
            assertEquals(lastValue, received.size(), "The number of received items does not match the number of produced items.");

            for (int i = 0; i < 10 * totalProducers; i++) {
                assertTrue(received.contains(i), "The producer value was not received by the consumer.");
            }

            // Wait for all threads to complete...
            for (Thread worker : allThreads) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
