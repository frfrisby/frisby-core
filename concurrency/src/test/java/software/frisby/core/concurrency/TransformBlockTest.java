package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullValueException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransformBlock} and its builder.  Covers builder validation, {@code post}
 * behaviour (including null-returning transforms), {@code linkTo}, the
 * completion lifecycle, and both delegate handler callbacks.
 *
 * <p>{@code TransformBlock} is a synchronous pass-through stage: it applies a {@code Function}
 * to each item on the posting thread and forwards the result to the linked downstream target.
 * There is no internal queue and no executor.  {@link TransformBlock#complete()} cascades
 * immediately to all linked downstream targets.</p>
 */
class TransformBlockTest {
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'TransformBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";
    private static final String LINK_TO_SELF_MSG =
            "The 'target' value is invalid.  A block cannot be linked to itself.";

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullTransform_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> TransformBlock.<String, Integer>builder().build()
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
            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(item -> true);

            assertFalse(block.post(null));
        }

        @Test
        void validItem_transformsAndForwardsToTarget() {
            AtomicReference<Integer> received = new AtomicReference<>();

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(item -> {
                received.set(item);
                return true;
            });

            assertTrue(block.post("hello"));
            assertEquals(5, received.get());
        }

        @Test
        void transformReturningNull_doesNotDeliverToTarget_butPostReturnsTrue() {
            AtomicInteger deliveryCount = new AtomicInteger(0);

            TransformBlock<String, String> block = TransformBlock.<String, String>builder()
                    .transform(item -> null)
                    .build();

            block.linkTo(item -> {
                deliveryCount.incrementAndGet();
                return true;
            });

            // post() returns true because the item was accepted; the null result is silently dropped.
            assertTrue(block.post("hello"));
            assertEquals(0, deliveryCount.get());
        }

        @Test
        void afterComplete_returnsFalse() {
            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.complete();

            assertFalse(block.post("hello"));
        }
    }

    // -------------------------------------------------------------------------
    // linkTo
    // -------------------------------------------------------------------------

    @Nested
    class LinkTo {
        @Test
        void withoutCriteria_deliversAllTransformedItems() {
            AtomicInteger count = new AtomicInteger(0);

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(item -> {
                count.incrementAndGet();
                return true;
            });

            block.post("a");
            block.post("bb");
            block.post("ccc");

            assertEquals(3, count.get());
        }

        @Test
        void withCriteria_deliversOnlyMatchingTransformedItems() {
            AtomicInteger matchCount = new AtomicInteger(0);

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            BranchBlock<Integer> branch = BranchBlock.<Integer>builder()
                    .when(
                            item -> item >= 3,
                            item -> {
                                matchCount.incrementAndGet();
                                return true;
                            }
                    )
                    .otherwise(item -> true)
                    .build();

            block.linkTo(branch);

            block.post("a");      // length 1 — filtered out
            block.post("bb");     // length 2 — filtered out
            block.post("ccc");    // length 3 — passes
            block.post("dddd");   // length 4 — passes

            assertEquals(2, matchCount.get());
        }

        @Test
        void linkToCalledTwice_throwsIllegalStateException() {
            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(item -> true);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> block.linkTo(item -> true)
            );

            assertEquals(LINK_TO_CALLED_TWICE_MSG, ex.getMessage());
        }

        @Test
        void linkToSelf_throwsIllegalArgumentException() {
            TransformBlock<String, String> block = TransformBlock.<String, String>builder()
                    .transform(s -> s)
                    .build();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> block.linkTo(block)
            );

            assertEquals(LINK_TO_SELF_MSG, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // complete() / completion()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void complete_cascadesToLinkedTarget() throws Exception {
            AtomicInteger deliveredCount = new AtomicInteger(0);

            // Use an ActionBlock as the downstream terminal so its completion future resolves when
            // complete() cascades from the TransformBlock.
            ActionBlock<Integer> downstream = ActionBlock.<Integer>builder()
                    .action(item -> deliveredCount.incrementAndGet())
                    .build();

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(downstream);

            block.post("hello");
            block.post("world");

            block.complete();

            // Completion cascades to the ActionBlock; its future resolves once complete() propagates.
            downstream.completion().get(5, TimeUnit.SECONDS);

            assertEquals(2, deliveredCount.get());
        }

        @Test
        void complete_withNoLinkedTarget_completesImmediately() throws Exception {
            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.complete();

            // TargetManager returns completedFuture(null) when targets list is empty.
            block.completion().get(1, TimeUnit.SECONDS);

            assertTrue(block.completion().isDone());
        }

        @Test
        void complete_isIdempotent() throws Exception {
            AtomicInteger deliveredCount = new AtomicInteger(0);

            ActionBlock<Integer> downstream = ActionBlock.<Integer>builder()
                    .action(item -> deliveredCount.incrementAndGet())
                    .build();

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(downstream);

            block.post("hello");

            block.complete();
            block.complete();

            downstream.completion().get(5, TimeUnit.SECONDS);

            assertEquals(1, deliveredCount.get());
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

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .itemPostedHandler((source, item, wasAccepted) -> {
                        postedItem.set(item);
                        notified.countDown();
                    })
                    .build();

            block.linkTo(item -> true);
            block.post("hello");

            assertTrue(notified.await(5, TimeUnit.SECONDS));
            assertEquals("hello", postedItem.get());
        }

        @Test
        void itemDeliveredHandler_isCalledOnDelivery() throws Exception {
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<Integer> deliveredItem = new AtomicReference<>();

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .itemDeliveredHandler((source, target, item) -> {
                        deliveredItem.set(item);
                        notified.countDown();
                    })
                    .build();

            block.linkTo(item -> true);
            block.post("hello");

            assertTrue(notified.await(5, TimeUnit.SECONDS));
            assertEquals(5, deliveredItem.get());
        }
    }

    // -------------------------------------------------------------------------
    // Fan-in
    // -------------------------------------------------------------------------

    @Nested
    class FanIn {
        @Test
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() {
            AtomicInteger received = new AtomicInteger();

            TransformBlock<String, Integer> block = TransformBlock.<String, Integer>builder()
                    .transform(String::length)
                    .build();

            block.linkTo(item -> {
                received.addAndGet(item);
                return true;
            });

            block.onLinked();
            block.onLinked();

            assertTrue(block.post("hi"));

            block.complete();

            assertTrue(block.post("hey"));

            block.complete();

            assertFalse(block.post("gone"));
            assertEquals(5, received.get());
        }
    }

    // -------------------------------------------------------------------------
    // inFlight()
    // -------------------------------------------------------------------------

    @Nested
    class InFlight {
        @Test
        void inFlight_noDownstream_returnsZero() {
            TransformBlock<String, String> block = TransformBlock.<String, String>builder()
                    .transform(item -> item)
                    .build();

            assertEquals(0, block.inFlight());
        }

        @Test
        void inFlight_withDownstream_delegatesToDownstream() {
            TransformBlock<String, String> block = TransformBlock.<String, String>builder()
                    .transform(item -> item)
                    .build();

            block.linkTo(new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 7;
                }
            });

            assertEquals(7, block.inFlight());
        }
    }
}

