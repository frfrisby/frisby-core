package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullValueException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ActionBlock} and its builder.  Covers builder validation, {@code post}
 * behaviour, the completion lifecycle, and the {@code itemPostedHandler} delegate callback.
 *
 * <p>{@code ActionBlock} is a terminal synchronous consumer: it invokes its {@code Consumer} for
 * every accepted item on the posting thread and has no downstream target or internal queue.
 * {@link ActionBlock#complete()} resolves its own {@link java.util.concurrent.CompletableFuture}
 * immediately — there is nothing to drain.</p>
 */
class ActionBlockTest {

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullAction_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> ActionBlock.<String>builder().build()
            );
        }
    }

    // -------------------------------------------------------------------------
    // post(T)
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Test
        void nullItem_returnsFalse() {
            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            assertFalse(block.post(null));
        }

        @Test
        void validItem_invokesActionAndReturnsTrue() {
            AtomicReference<String> received = new AtomicReference<>();

            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(received::set)
                    .build();

            assertTrue(block.post("hello"));
            assertEquals("hello", received.get());
        }

        @Test
        void multipleItems_actionInvokedForEachItem() {
            AtomicInteger count = new AtomicInteger(0);

            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> count.incrementAndGet())
                    .build();

            block.post("a");
            block.post("b");
            block.post("c");

            assertEquals(3, count.get());
        }

        @Test
        void afterComplete_returnsFalse() {
            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            block.complete();

            assertFalse(block.post("hello"));
        }
    }

    // -------------------------------------------------------------------------
    // complete() / completion()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void completionFuture_isPendingBeforeComplete() {
            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            assertFalse(block.completion().isDone());
        }

        @Test
        void complete_resolvesCompletionFutureImmediately() throws Exception {
            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            block.complete();

            // Synchronous block — future must resolve without any delay.
            block.completion().get(1, TimeUnit.SECONDS);

            assertTrue(block.completion().isDone());
        }

        @Test
        void complete_isIdempotent() throws Exception {
            AtomicInteger count = new AtomicInteger(0);

            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> count.incrementAndGet())
                    .build();

            block.post("a");

            block.complete();
            block.complete();

            block.completion().get(1, TimeUnit.SECONDS);

            // Exactly one item was delivered; complete() was called twice without error.
            assertEquals(1, count.get());
        }
    }

    // -------------------------------------------------------------------------
    // Delegate handlers
    // -------------------------------------------------------------------------

    @Nested
    class Delegates {
        @Test
        void itemPostedHandler_isCalledOnPost() throws Exception {
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<String> postedItem = new AtomicReference<>();
            AtomicBoolean accepted = new AtomicBoolean(false);

            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .itemPostedHandler((source, item, wasAccepted) -> {
                        postedItem.set(item);
                        accepted.set(wasAccepted);
                        notified.countDown();
                    })
                    .build();

            block.post("hello");

            assertTrue(notified.await(5, TimeUnit.SECONDS));
            assertEquals("hello", postedItem.get());
            assertTrue(accepted.get());
        }
    }

    // -------------------------------------------------------------------------
    // Fan-in
    // -------------------------------------------------------------------------

    @Nested
    class FanIn {
        @Test
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() {
            List<String> received = new CopyOnWriteArrayList<>();

            ActionBlock<String> block = ActionBlock.<String>builder()
                    .action(received::add)
                    .build();

            block.onLinked();
            block.onLinked();

            assertTrue(block.post("a"));

            block.complete();

            assertTrue(block.post("b"));
            assertFalse(block.completion().isDone());

            block.complete();

            assertTrue(block.completion().isDone());
            assertFalse(block.post("c"));
            assertEquals(List.of("a", "b"), received);
        }
    }
}

