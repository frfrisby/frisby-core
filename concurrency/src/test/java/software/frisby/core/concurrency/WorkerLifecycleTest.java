package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class WorkerLifecycleTest {
    @Nested
    class Construction {
        @Test
        void isRunning_returnsFalseInitially() {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            assertFalse(lifecycle.isRunning(), "A newly constructed lifecycle must not be running.");
        }

        @Test
        void completion_notDoneInitially() {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            assertFalse(lifecycle.completion().isDone(), "The completion future must not be done before finish() is called.");
        }
    }

    @Nested
    class Lifecycle {
        @Test
        void start_setsIsRunningToTrue() {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            lifecycle.start();

            assertTrue(lifecycle.isRunning(), "isRunning() must return true after start() is called.");
        }

        @Test
        void finish_setsIsRunningToFalse() {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            lifecycle.start();
            lifecycle.finish();

            assertFalse(lifecycle.isRunning(), "isRunning() must return false after finish() is called.");
        }

        @Test
        void finish_completesTheFuture() throws ExecutionException, InterruptedException, TimeoutException {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            lifecycle.finish();

            lifecycle.completion().get(1, TimeUnit.SECONDS);
            assertTrue(lifecycle.completion().isDone(), "The completion future must be done after finish() is called.");
        }

        @Test
        void finish_calledTwice_doesNotThrow() {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            lifecycle.finish();

            assertDoesNotThrow(lifecycle::finish, "Calling finish() a second time must not throw.");
        }

        @Test
        void finish_beforeStart_producesConsistentState() throws ExecutionException, InterruptedException, TimeoutException {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            lifecycle.finish();

            lifecycle.completion().get(1, TimeUnit.SECONDS);
            assertFalse(lifecycle.isRunning(), "isRunning() must return false when finish() is called before start().");
            assertTrue(lifecycle.completion().isDone(), "The completion future must be done when finish() is called before start().");
        }
    }

    @Nested
    class OrderingInvariant {
        // The critical invariant: isRunning must be false BEFORE the completion future
        // resolves.  Any thread unblocked by completion().get() must always observe
        // isRunning() == false — never true.  This is enforced by the ordering in
        // finish(): isRunning = false is written before completionFuture.complete(null).
        // Repeated 20 times to maximize the probability of hitting the race window.

        @RepeatedTest(20)
        void callerUnblockedByCompletion_observesIsRunningFalse() throws ExecutionException, InterruptedException, TimeoutException {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            Thread worker = new Thread(() -> {
                lifecycle.start();
                lifecycle.finish();
            });

            worker.start();
            lifecycle.completion().get(5, TimeUnit.SECONDS);

            assertFalse(lifecycle.isRunning(),
                    "A thread unblocked by completion().get() must observe isRunning() == false.");
        }

        @RepeatedTest(20)
        void completionFuture_neverResolvesBefore_isRunningFalse() throws InterruptedException {
            WorkerLifecycle lifecycle = new WorkerLifecycle();

            // Register a thenRun action that captures the isRunning value at the moment
            // the future completes.  Because thenRun fires in the completing thread or
            // a follow-up async thread, and volatile guarantees visibility, the value
            // must always be false.
            boolean[] isRunningAtCompletion = {true};

            lifecycle.completion().thenRun(() -> isRunningAtCompletion[0] = lifecycle.isRunning());

            Thread worker = new Thread(() -> {
                lifecycle.start();
                lifecycle.finish();
            });

            worker.start();
            worker.join(5_000);

            assertFalse(isRunningAtCompletion[0],
                    "The thenRun callback must observe isRunning() == false.");
        }
    }

    @Nested
    class CompletableQueueBridge {
        // Workers use CompletableQueue for item delivery and call lifecycle.finish() directly
        // at the end of run(), after the dequeue() loop exits.  dequeue() returns null when the
        // queue is both completed and empty, guaranteeing all items were processed first.
        // These tests verify that lifecycle.completion() resolves only after the full worker
        // run sequence (all items processed + lifecycle.finish() called).

        @Test
        void workerPattern_lifecycleCompletionResolvesAfterAllItemsProcessed() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableQueue<String> queue = new CompletableQueue<>(new ArrayBlockingQueue<>(4));
            WorkerLifecycle lifecycle = new WorkerLifecycle();
            List<String> processed = new java.util.concurrent.CopyOnWriteArrayList<>();

            // Simulate the worker run() pattern.
            Thread workerThread = new Thread(() -> {
                lifecycle.start();
                String item;
                while (null != (item = queue.dequeue())) {
                    processed.add(item);
                }
                lifecycle.finish();
            });

            workerThread.start();

            queue.enqueue("a");
            queue.enqueue("b");
            queue.enqueue("c");
            queue.complete();

            lifecycle.completion().get(5, TimeUnit.SECONDS);

            assertEquals(3, processed.size(), "All three items must be processed before lifecycle.completion() resolves.");
            assertFalse(lifecycle.isRunning(), "isRunning() must be false after completion resolves.");
        }

        @Test
        void workerPattern_completionNotResolvedWhileItemsRemain() throws InterruptedException {
            CompletableQueue<String> queue = new CompletableQueue<>(new ArrayBlockingQueue<>(4));
            WorkerLifecycle lifecycle = new WorkerLifecycle();
            java.util.concurrent.CountDownLatch pauseLatch = new java.util.concurrent.CountDownLatch(1);

            // Simulate a worker that pauses after processing the first item.
            Thread workerThread = new Thread(() -> {
                lifecycle.start();
                String item;
                while (null != (item = queue.dequeue())) {
                    if ("a".equals(item)) {
                        try {
                            pauseLatch.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                lifecycle.finish();
            });

            workerThread.start();

            queue.enqueue("a");
            queue.enqueue("b");
            queue.complete();

            // Give the worker time to pick up "a" and pause.
            long deadline = System.currentTimeMillis() + 5_000;
            while (!lifecycle.isRunning() && System.currentTimeMillis() < deadline) {
                Thread.sleep(5);
            }

            assertFalse(lifecycle.completion().isDone(),
                    "lifecycle.completion() must not resolve while worker is still processing.");

            pauseLatch.countDown();

            long completionDeadline = System.currentTimeMillis() + 5_000;
            while (!lifecycle.completion().isDone() && System.currentTimeMillis() < completionDeadline) {
                Thread.sleep(5);
            }

            assertTrue(lifecycle.completion().isDone(),
                    "lifecycle.completion() must resolve after all items are processed.");
        }

        @Test
        void drainBeforeWorkerStarts_workerExitsCleanly() throws ExecutionException, InterruptedException, TimeoutException {
            CompletableQueue<String> queue = new CompletableQueue<>(new ArrayBlockingQueue<>(4));
            WorkerLifecycle lifecycle = new WorkerLifecycle();
            java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);

            Thread workerThread = new Thread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                lifecycle.start();
                String item;
                while (null != (item = queue.dequeue())) {
                    // nothing to process
                }
                lifecycle.finish();
            });

            workerThread.start();

            // Complete the queue before the worker starts.
            queue.complete();
            startLatch.countDown();

            lifecycle.completion().get(5, TimeUnit.SECONDS);

            assertFalse(lifecycle.isRunning(),
                    "isRunning() must be false after the worker exits with an already-completed empty queue.");
        }
    }
}

