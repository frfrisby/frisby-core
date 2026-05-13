package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullElementException;
import software.frisby.core.validation.NullValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BroadcastBlock} and its builder.  Covers builder validation, fan-out
 * semantics, optional cloning, the {@code complete()} / {@code completion()} lifecycle, and all
 * delegate handler callbacks.
 */
class BroadcastBlockTest {
    private static final Target<String> ACCEPT = item -> true;

    private static final String TOO_FEW_TARGETS_MSG =
            "The 'BroadcastBlock' block requires at least two targets.  Call target() or targets() before calling build().";

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullTargetInSingleAdd_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> BroadcastBlock.<String>builder().target(null)
            );
        }


        @Test
        void nullTargetList_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> BroadcastBlock.<String>builder().targets(null)
            );
        }

        @Test
        void nullElementInTargetList_throwsNullElementException() {
            List<Target<String>> listWithNull = new ArrayList<>();
            listWithNull.add(ACCEPT);
            listWithNull.add(null);

            assertThrows(
                    NullElementException.class,
                    () -> BroadcastBlock.<String>builder().targets(listWithNull)
            );
        }

        @Test
        void zeroTargets_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> BroadcastBlock.<String>builder().build()
            );

            assertEquals(TOO_FEW_TARGETS_MSG, ex.getMessage());
        }

        @Test
        void oneTarget_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> BroadcastBlock.<String>builder()
                            .target(ACCEPT)
                            .build()
            );

            assertEquals(TOO_FEW_TARGETS_MSG, ex.getMessage());
        }

        @Test
        void twoTargets_buildsSuccessfully() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            assertNotNull(block);
        }

        @Test
        void targetsViaList_buildsSuccessfully() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .targets(List.of(ACCEPT, ACCEPT, ACCEPT))
                    .build();

            assertNotNull(block);
        }

        @Test
        void withCloningFunction_buildsSuccessfully() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .cloningFunction(s -> s)
                    .build();

            assertNotNull(block);
        }
    }

    // -------------------------------------------------------------------------
    // post(T)
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Test
        void nullItem_returnsFalse() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            assertFalse(block.post(null));
        }

        @Test
        void afterComplete_returnsFalse() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            block.complete();

            assertFalse(block.post("hello"));
        }

        @Test
        void validItem_deliversToAllTargets() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);
            AtomicBoolean thirdReceived = new AtomicBoolean(false);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(item -> {
                        firstReceived.set(true);
                        return true;
                    })
                    .target(item -> {
                        secondReceived.set(true);
                        return true;
                    })
                    .target(item -> {
                        thirdReceived.set(true);
                        return true;
                    })
                    .build();

            assertTrue(block.post("hello"));
            assertTrue(firstReceived.get());
            assertTrue(secondReceived.get());
            assertTrue(thirdReceived.get());
        }

        @Test
        void whenOneTargetRejects_allOtherTargetsStillReceiveItem() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean thirdReceived = new AtomicBoolean(false);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(item -> {
                        firstReceived.set(true);
                        return true;
                    })
                    .target(item -> false)
                    .target(item -> {
                        thirdReceived.set(true);
                        return true;
                    })
                    .build();

            assertFalse(block.post("hello"));
            assertTrue(firstReceived.get());
            assertTrue(thirdReceived.get());
        }

        @Test
        void noCloningFunction_allTargetsReceiveSameReference() {
            AtomicReference<String> firstReceived = new AtomicReference<>();
            AtomicReference<String> secondReceived = new AtomicReference<>();

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(item -> {
                        firstReceived.set(item);
                        return true;
                    })
                    .target(item -> {
                        secondReceived.set(item);
                        return true;
                    })
                    .build();

            String original = "hello";

            block.post(original);

            assertSame(original, firstReceived.get());
            assertSame(original, secondReceived.get());
        }

        @Test
        void cloningFunction_eachTargetReceivesIndependentCopy() {
            AtomicReference<int[]> firstReceived = new AtomicReference<>();
            AtomicReference<int[]> secondReceived = new AtomicReference<>();

            BroadcastBlock<int[]> block = BroadcastBlock.<int[]>builder()
                    .target(item -> {
                        firstReceived.set(item);
                        return true;
                    })
                    .target(item -> {
                        secondReceived.set(item);
                        return true;
                    })
                    .cloningFunction(int[]::clone)
                    .build();

            int[] original = {1, 2, 3};

            block.post(original);

            assertNotSame(original, firstReceived.get());
            assertNotSame(original, secondReceived.get());
            assertNotSame(firstReceived.get(), secondReceived.get());
            assertArrayEquals(original, firstReceived.get());
            assertArrayEquals(original, secondReceived.get());
        }
    }

    // -------------------------------------------------------------------------
    // complete() / completion()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void complete_cascadesToAllTargets() {
            AtomicInteger completeCount = new AtomicInteger(0);

            Target<String> counting = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    completeCount.incrementAndGet();
                }
            };

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(counting)
                    .target(counting)
                    .target(counting)
                    .build();

            block.complete();

            assertEquals(3, completeCount.get());
        }

        @Test
        void complete_isIdempotent() {
            AtomicInteger completeCount = new AtomicInteger(0);

            Target<String> counting = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    completeCount.incrementAndGet();
                }
            };

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(counting)
                    .target(ACCEPT)
                    .build();

            block.complete();
            block.complete();

            assertEquals(1, completeCount.get());
        }

        @Test
        void completion_resolvesAfterAllTargetsComplete() throws Exception {
            ActionBlock<String> downstream1 = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            ActionBlock<String> downstream2 = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(downstream1)
                    .target(downstream2)
                    .build();

            block.post("a");
            block.complete();
            block.completion().get(5, TimeUnit.SECONDS);

            assertTrue(block.completion().isDone());
        }
    }

    // -------------------------------------------------------------------------
    // Delegate handlers
    // -------------------------------------------------------------------------

    @Nested
    class Delegates {
        @Test
        void itemPostedHandler_calledOnPost() {
            AtomicReference<String> postedItem = new AtomicReference<>();
            AtomicBoolean wasAccepted = new AtomicBoolean(false);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .itemPostedHandler((source, item, accepted) -> {
                        postedItem.set(item);
                        wasAccepted.set(accepted);
                    })
                    .build();

            block.post("hello");

            assertEquals("hello", postedItem.get());
            assertTrue(wasAccepted.get());
        }

        @Test
        void itemDeliveredHandler_calledForEachAcceptingTarget() {
            AtomicInteger deliveryCount = new AtomicInteger(0);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .itemDeliveredHandler((source, target, item) -> deliveryCount.incrementAndGet())
                    .build();

            block.post("hello");

            assertEquals(3, deliveryCount.get());
        }

        @Test
        void itemDeliveredHandler_notCalledForRejectingTarget() {

            AtomicInteger deliveryCount = new AtomicInteger(0);
            AtomicBoolean wasAccepted = new AtomicBoolean(true);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(item -> false)
                    .itemDeliveredHandler((source, target, item) -> deliveryCount.incrementAndGet())
                    .itemPostedHandler((source, item, accepted) -> wasAccepted.set(accepted))
                    .build();

            block.post("hello");

            assertEquals(1, deliveryCount.get());
            assertFalse(wasAccepted.get());
        }
    }

    // -------------------------------------------------------------------------
    // inFlight()
    // -------------------------------------------------------------------------

    @Nested
    class InFlight {
        @Test
        void inFlight_returnsZero_whenAllDownstreamTargetsAreIdle() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            assertEquals(0, block.inFlight());
        }

        @Test
        void inFlight_sumsDelegatesAcrossAllTargets() {
            // Each item is broadcast to all targets, so a load of 10 on target1
            // and 5 on target2 yields a total of 15.
            Target<String> target1 = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 10;
                }
            };

            Target<String> target2 = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 5;
                }
            };

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(target1)
                    .target(target2)
                    .build();

            assertEquals(15, block.inFlight());
        }
    }

    // -------------------------------------------------------------------------
    // Fan-in
    // -------------------------------------------------------------------------

    @Nested
    class FanIn {
        @Test
        void twoUpstreams_delaysCompletionUntilBothUpstreamsDone() {
            List<String> received = new java.util.concurrent.CopyOnWriteArrayList<>();

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(item -> {
                        received.add(item);
                        return true;
                    })
                    .target(item -> true)
                    .build();

            block.onLinked();
            block.onLinked();

            assertTrue(block.post("a"));

            block.complete();

            assertTrue(block.post("b"));

            block.complete();

            assertFalse(block.post("c"));
            assertEquals(List.of("a", "b"), received);
        }
    }
}
