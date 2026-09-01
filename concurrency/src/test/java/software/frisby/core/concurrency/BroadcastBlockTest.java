package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullElementException;
import software.frisby.core.validation.NullValueException;

import java.time.Duration;
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
            "The 'BroadcastBlock' block requires at least two targets. Call target() or targets() before calling build().";

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
    // post(T, Duration)
    // -------------------------------------------------------------------------

    @Nested
    class PostWithTimeout {
        // A Target that also implements post(T, Duration) — plain lambda targets only
        // implement the SAM and would hit Target's throwing default otherwise.
        private static Target<String> timeoutAwareTarget(boolean accepted) {
            return new Target<>() {
                @Override
                public boolean post(String item) {
                    return accepted;
                }

                @Override
                public boolean post(String item, Duration timeout) {
                    return accepted;
                }
            };
        }

        @Test
        void nullItem_returnsFalse() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(timeoutAwareTarget(true))
                    .target(timeoutAwareTarget(true))
                    .build();

            assertFalse(block.post(null, Duration.ofSeconds(1)));
        }

        @Test
        void afterComplete_returnsFalse() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(timeoutAwareTarget(true))
                    .target(timeoutAwareTarget(true))
                    .build();

            block.complete();

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
        }

        @Test
        void validItem_deliversToAllTargets_returnsTrue() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);
            AtomicBoolean thirdReceived = new AtomicBoolean(false);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(new Target<>() {
                        @Override
                        public boolean post(String item) {
                            firstReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            firstReceived.set(true);
                            return true;
                        }
                    })
                    .target(new Target<>() {
                        @Override
                        public boolean post(String item) {
                            secondReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            secondReceived.set(true);
                            return true;
                        }
                    })
                    .target(new Target<>() {
                        @Override
                        public boolean post(String item) {
                            thirdReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            thirdReceived.set(true);
                            return true;
                        }
                    })
                    .build();

            assertTrue(block.post("hello", Duration.ofSeconds(1)));
            assertTrue(firstReceived.get());
            assertTrue(secondReceived.get());
            assertTrue(thirdReceived.get());
        }

        @Test
        void whenOneTargetRejects_allOtherTargetsStillReceiveItem_returnsFalse() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean thirdReceived = new AtomicBoolean(false);

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(new Target<>() {
                        @Override
                        public boolean post(String item) {
                            firstReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            firstReceived.set(true);
                            return true;
                        }
                    })
                    .target(timeoutAwareTarget(false))
                    .target(new Target<>() {
                        @Override
                        public boolean post(String item) {
                            thirdReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            thirdReceived.set(true);
                            return true;
                        }
                    })
                    .build();

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
            assertTrue(firstReceived.get());
            assertTrue(thirdReceived.get());
        }

        @Test
        void nullTimeout_throwsNullValueException() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(timeoutAwareTarget(true))
                    .target(timeoutAwareTarget(true))
                    .build();

            assertThrows(NullValueException.class, () -> block.post("hello", null));
        }

        @Test
        void negativeTimeout_throwsDurationOutsideRangeException() {
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(timeoutAwareTarget(true))
                    .target(timeoutAwareTarget(true))
                    .build();

            assertThrows(
                    DurationOutsideRangeException.class,
                    () -> block.post("hello", Duration.ofSeconds(-1))
            );
        }

        @Test
        void downstreamDoesNotSupportTimeout_throwsUnsupportedOperationException() {
            // A plain lambda target only implements the SAM post(T) — it has not opted into
            // bounded-wait posting, so the inherited Target default must throw.
            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> block.post("hello", Duration.ofSeconds(1))
            );
        }

        @Test
        void eachTargetIndependentlyHonorsFullTimeout_worstCaseIsNTimesTimeout() {
            // Documented, deliberate semantic: the same plain Duration is forwarded to every
            // sequential target, each getting its own fresh budget — not a single shared budget
            // across the whole broadcast call. Proven here by having two targets each block for
            // the *entire* timeout window before rejecting; the total elapsed time must be at
            // least 2 x timeout, not ~1 x timeout.
            Duration timeout = Duration.ofMillis(100);

            Target<String> alwaysTimesOut = new Target<>() {
                @Override
                public boolean post(String item) {
                    return false;
                }

                @Override
                public boolean post(String item, Duration t) {
                    try {
                        Thread.sleep(t.toMillis());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    return false;
                }
            };

            BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                    .target(alwaysTimesOut)
                    .target(alwaysTimesOut)
                    .build();

            long start = System.nanoTime();
            assertFalse(block.post("hello", timeout));
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

            assertTrue(elapsedMillis >= 2 * timeout.toMillis());
        }

        @Test
        void cloningFunction_eachTargetReceivesIndependentCopy() {
            AtomicReference<int[]> firstReceived = new AtomicReference<>();
            AtomicReference<int[]> secondReceived = new AtomicReference<>();

            BroadcastBlock<int[]> block = BroadcastBlock.<int[]>builder()
                    .target(new Target<>() {
                        @Override
                        public boolean post(int[] item) {
                            firstReceived.set(item);
                            return true;
                        }

                        @Override
                        public boolean post(int[] item, Duration timeout) {
                            firstReceived.set(item);
                            return true;
                        }
                    })
                    .target(new Target<>() {
                        @Override
                        public boolean post(int[] item) {
                            secondReceived.set(item);
                            return true;
                        }

                        @Override
                        public boolean post(int[] item, Duration timeout) {
                            secondReceived.set(item);
                            return true;
                        }
                    })
                    .cloningFunction(int[]::clone)
                    .build();

            int[] original = {1, 2, 3};

            assertTrue(block.post(original, Duration.ofSeconds(1)));

            assertNotSame(original, firstReceived.get());
            assertNotSame(original, secondReceived.get());
            assertNotSame(firstReceived.get(), secondReceived.get());
            assertArrayEquals(original, firstReceived.get());
            assertArrayEquals(original, secondReceived.get());
        }

        @Test
        void downstreamIsAsyncBuffer_boundedByBufferCapacity() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("BroadcastBlockTest")
                    .build();

            try {
                BufferBlock<String> buffer = BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                java.util.concurrent.CountDownLatch delivered = new java.util.concurrent.CountDownLatch(1);
                AtomicReference<String> received = new AtomicReference<>();

                buffer.linkTo(item -> {
                    received.set(item);
                    delivered.countDown();
                    return true;
                });

                BroadcastBlock<String> block = BroadcastBlock.<String>builder()
                        .target(buffer)
                        .target(timeoutAwareTarget(true))
                        .build();

                assertTrue(block.post("hello", Duration.ofSeconds(5)));
                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals("hello", received.get());
            } finally {
                executor.shutdown();
            }
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
