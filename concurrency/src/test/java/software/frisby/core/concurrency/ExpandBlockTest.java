package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullValueException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ExpandBlock} and its builder.  Covers {@code post} behaviour (null list,
 * empty list, null elements, normal delivery), {@code linkTo}, the completion lifecycle, and both
 * delegate handler callbacks.
 *
 * <p>{@code ExpandBlock} is a synchronous pass-through stage: it unpacks a {@code List<T>} into
 * individual {@code T} items on the calling thread and forwards each non-null element to the linked
 * downstream target.  There is no internal queue and no executor.  {@link ExpandBlock#complete()}
 * cascades immediately to the linked downstream target.</p>
 */
class ExpandBlockTest {
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'ExpandBlock' block already has a linked target.  A single-target block may only be linked to one downstream target.";

    // -------------------------------------------------------------------------
    // post(List<T>)
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Test
        void nullList_returnsFalse() {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> true);

            assertFalse(block.post(null));
        }

        @Test
        void emptyList_returnsTrueAndNothingForwarded() {
            AtomicInteger deliveryCount = new AtomicInteger(0);

            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> {
                deliveryCount.incrementAndGet();
                return true;
            });

            assertTrue(block.post(List.of()));
            assertEquals(0, deliveryCount.get());
        }

        @Test
        void listWithNullElements_nullsSkipped_nonNullDelivered() {
            AtomicInteger deliveryCount = new AtomicInteger(0);
            AtomicReference<String> received = new AtomicReference<>();

            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> {
                received.set(item);
                deliveryCount.incrementAndGet();
                return true;
            });

            // Arrays.asList allows null elements; List.of does not.
            assertTrue(block.post(Arrays.asList(null, "hello", null)));
            assertEquals(1, deliveryCount.get());
            assertEquals("hello", received.get());
        }

        @Test
        void validList_eachElementForwardedIndividually() {
            AtomicInteger deliveryCount = new AtomicInteger(0);

            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> {
                deliveryCount.incrementAndGet();
                return true;
            });

            assertTrue(block.post(List.of("a", "b", "c")));
            assertEquals(3, deliveryCount.get());
        }

        @Test
        void afterComplete_returnsFalse() {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.complete();

            assertFalse(block.post(List.of("hello")));
        }

        @Test
        void targetRejectsItem_postStillReturnsTrue() {
            // TargetManager.postToFirst() does not propagate the target's boolean return value;
            // ExpandBlock.post() therefore always returns true for a non-null, non-empty list,
            // consistent with TransformBlock behavior.
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> false);  // target always rejects

            assertTrue(block.post(List.of("a", "b")));
        }
    }

    // -------------------------------------------------------------------------
    // linkTo
    // -------------------------------------------------------------------------

    @Nested
    class LinkTo {
        @Test
        void nullTarget_throwsNullValueException() {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            assertThrows(
                    NullValueException.class,
                    () -> block.linkTo(null)
            );
        }

        @Test
        void withoutCriteria_deliversAllElementsToTarget() {
            AtomicInteger count = new AtomicInteger(0);

            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> {
                count.incrementAndGet();
                return true;
            });

            block.post(List.of("a", "b"));
            block.post(List.of("c"));

            assertEquals(3, count.get());
        }

        @Test
        void linkToCalledTwice_throwsIllegalStateException() {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.linkTo(item -> true);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> block.linkTo(item -> true)
            );

            assertEquals(LINK_TO_CALLED_TWICE_MSG, ex.getMessage());
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

            // Use an ActionBlock as the downstream terminal so its completion future resolves
            // when complete() cascades from the ExpandBlock.
            ActionBlock<String> downstream = ActionBlock.<String>builder()
                    .action(item -> deliveredCount.incrementAndGet())
                    .build();

            ExpandBlock<String> block = ExpandBlock.<String>builder().build();
            block.linkTo(downstream);

            block.post(List.of("hello", "world"));
            block.complete();

            downstream.completion().get(5, TimeUnit.SECONDS);

            assertEquals(2, deliveredCount.get());
        }

        @Test
        void complete_withNoLinkedTarget_completesImmediately() throws Exception {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            block.complete();

            // TargetManager returns completedFuture(null) when the targets list is empty.
            block.completion().get(1, TimeUnit.SECONDS);

            assertTrue(block.completion().isDone());
        }

        @Test
        void complete_isIdempotent() throws Exception {
            AtomicInteger deliveredCount = new AtomicInteger(0);

            ActionBlock<String> downstream = ActionBlock.<String>builder()
                    .action(item -> deliveredCount.incrementAndGet())
                    .build();

            ExpandBlock<String> block = ExpandBlock.<String>builder().build();
            block.linkTo(downstream);

            block.post(List.of("hello"));

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
        void itemPostedHandler_isCalledOncePerList() throws Exception {
            CountDownLatch notified = new CountDownLatch(1);
            AtomicReference<List<String>> postedList = new AtomicReference<>();

            ExpandBlock<String> block = ExpandBlock.<String>builder()
                    .itemPostedHandler((source, list, wasAccepted) -> {
                        postedList.set(list);
                        notified.countDown();
                    })
                    .build();

            block.linkTo(item -> true);
            block.post(List.of("a", "b", "c"));

            assertTrue(notified.await(5, TimeUnit.SECONDS));
            assertEquals(List.of("a", "b", "c"), postedList.get());
        }

        @Test
        void itemDeliveredHandler_isCalledOncePerElement() throws Exception {
            CountDownLatch notified = new CountDownLatch(3);
            AtomicInteger deliveredCount = new AtomicInteger(0);

            ExpandBlock<String> block = ExpandBlock.<String>builder()
                    .itemDeliveredHandler((source, target, item) -> {
                        deliveredCount.incrementAndGet();
                        notified.countDown();
                    })
                    .build();

            block.linkTo(item -> true);
            block.post(List.of("a", "b", "c"));

            assertTrue(notified.await(5, TimeUnit.SECONDS));
            assertEquals(3, deliveredCount.get());
        }
    }

    // -------------------------------------------------------------------------
    // Fan-in
    // -------------------------------------------------------------------------

    @Nested
    class FanIn {
        @Test
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() {
            AtomicInteger received = new AtomicInteger(0);

            ExpandBlock<String> block = ExpandBlock.<String>builder()
                    .build();

            block.linkTo(item -> {
                received.incrementAndGet();
                return true;
            });

            block.onLinked();
            block.onLinked();

            assertTrue(block.post(List.of("a")));

            block.complete();

            assertTrue(block.post(List.of("b")));

            block.complete();

            assertFalse(block.post(List.of("c")));
            assertEquals(2, received.get());
        }
    }

    // -------------------------------------------------------------------------
    // inFlight()
    // -------------------------------------------------------------------------

    @Nested
    class InFlight {
        @Test
        void inFlight_noDownstream_returnsZero() {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

            assertEquals(0, block.inFlight());
        }

        @Test
        void inFlight_withDownstream_delegatesToDownstream() {
            ExpandBlock<String> block = ExpandBlock.<String>builder().build();

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
