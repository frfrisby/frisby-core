package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BranchBlock} and its builder.  Covers builder validation,
 * routing logic (first-match wins, predicate exception handling, otherwise fallback),
 * the {@code complete()} / {@code completion()} lifecycle, and all delegate handler callbacks.
 */
class BranchBlockTest {
    private static final String NO_WHEN_MSG =
            "The 'BranchBlock' block requires at least one 'when' clause.  Call when(predicate, target) before calling build().";

    private static final String NO_OTHERWISE_MSG =
            "The 'BranchBlock' block requires an 'otherwise' target.  Call otherwise(target) before calling build().";

    private static final String DOUBLE_OTHERWISE_MSG =
            "The 'BranchBlock' block already has an otherwise target configured.  The otherwise() method may only be called once.";

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void noWhenClauses_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> BranchBlock.<String>builder()
                            .otherwise(item -> true)
                            .build()
            );

            assertEquals(NO_WHEN_MSG, ex.getMessage());
        }

        @Test
        void noOtherwiseTarget_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> BranchBlock.<String>builder()
                            .when(item -> true, item -> true)
                            .build()
            );

            assertEquals(NO_OTHERWISE_MSG, ex.getMessage());
        }

        @Test
        void otherwiseCalledTwice_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> BranchBlock.<String>builder()
                            .when(item -> true, item -> true)
                            .otherwise(item -> true)
                            .otherwise(item -> true)
            );

            assertEquals(DOUBLE_OTHERWISE_MSG, ex.getMessage());
        }

        @Test
        void nullPredicateInWhen_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> BranchBlock.<String>builder().when(null, item -> true)
            );
        }

        @Test
        void nullTargetInWhen_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> BranchBlock.<String>builder().when(item -> true, null)
            );
        }

        @Test
        void nullOtherwiseTarget_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> BranchBlock.<String>builder().otherwise(null)
            );
        }

        @Test
        void validConfiguration_buildsSuccessfully() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("a"), item -> true)
                    .otherwise(item -> true)
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
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> true)
                    .otherwise(item -> true)
                    .build();

            assertFalse(block.post(null));
        }

        @Test
        void afterComplete_returnsFalse() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> true)
                    .otherwise(item -> true)
                    .build();

            block.complete();

            assertFalse(block.post("hello"));
        }

        @Test
        void itemMatchingFirstWhen_routedToFirstTarget() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(
                            item -> item.startsWith("a"),
                            item -> {
                                firstReceived.set(true);
                                return true;
                            }
                    )
                    .when(
                            item -> item.startsWith("b"),
                            item -> {
                                secondReceived.set(true);
                                return true;
                            }
                    )
                    .otherwise(item -> true)
                    .build();

            assertTrue(block.post("apple"));
            assertTrue(firstReceived.get());
            assertFalse(secondReceived.get());
        }

        @Test
        void itemMatchingSecondWhen_skipsFirstAndRoutes() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(
                            item -> item.startsWith("a"),
                            item -> {
                                firstReceived.set(true);
                                return true;
                            }
                    )
                    .when(
                            item -> item.startsWith("b"),
                            item -> {
                                secondReceived.set(true);
                                return true;
                            }
                    )
                    .otherwise(item -> true)
                    .build();

            assertTrue(block.post("banana"));
            assertFalse(firstReceived.get());
            assertTrue(secondReceived.get());
        }

        @Test
        void itemMatchingNoWhen_routedToOtherwiseTarget() {
            AtomicBoolean otherwiseReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("a"), item -> true)
                    .otherwise(item -> {
                        otherwiseReceived.set(true);
                        return true;
                    })
                    .build();

            assertTrue(block.post("hello"));
            assertTrue(otherwiseReceived.get());
        }

        @Test
        void whenTargetRejectsItem_returnsFalse() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> false)
                    .otherwise(item -> true)
                    .build();

            assertFalse(block.post("hello"));
        }

        @Test
        void otherwiseTargetRejectsItem_returnsFalse() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> false, item -> true)
                    .otherwise(item -> false)
                    .build();

            assertFalse(block.post("hello"));
        }

        @Test
        void whenPredicateThrows_itemRoutedToOtherwiseTarget() {
            AtomicBoolean otherwiseReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(
                            item -> {
                                throw new RuntimeException("predicate error");
                            },
                            item -> true
                    )
                    .otherwise(item -> {
                        otherwiseReceived.set(true);
                        return true;
                    })
                    .build();

            assertTrue(block.post("hello"));
            assertTrue(otherwiseReceived.get());
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
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, timeoutAwareTarget(true))
                    .otherwise(timeoutAwareTarget(true))
                    .build();

            assertFalse(block.post(null, Duration.ofSeconds(1)));
        }

        @Test
        void afterComplete_returnsFalse() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, timeoutAwareTarget(true))
                    .otherwise(timeoutAwareTarget(true))
                    .build();

            block.complete();

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
        }

        @Test
        void itemMatchingWhen_routedToWhenTarget() {
            AtomicBoolean whenReceived = new AtomicBoolean(false);
            AtomicBoolean otherwiseReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("a"), new Target<String>() {
                        @Override
                        public boolean post(String item) {
                            whenReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            whenReceived.set(true);
                            return true;
                        }
                    })
                    .otherwise(new Target<String>() {
                        @Override
                        public boolean post(String item) {
                            otherwiseReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            otherwiseReceived.set(true);
                            return true;
                        }
                    })
                    .build();

            assertTrue(block.post("apple", Duration.ofSeconds(1)));
            assertTrue(whenReceived.get());
            assertFalse(otherwiseReceived.get());
        }

        @Test
        void itemMatchingNoWhen_routedToOtherwiseTarget() {
            AtomicBoolean otherwiseReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("a"), timeoutAwareTarget(true))
                    .otherwise(new Target<String>() {
                        @Override
                        public boolean post(String item) {
                            otherwiseReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            otherwiseReceived.set(true);
                            return true;
                        }
                    })
                    .build();

            assertTrue(block.post("hello", Duration.ofSeconds(1)));
            assertTrue(otherwiseReceived.get());
        }

        @Test
        void whenPredicateThrows_itemRoutedToOtherwiseTarget() {
            AtomicBoolean otherwiseReceived = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(
                            item -> {
                                throw new RuntimeException("predicate error");
                            },
                            timeoutAwareTarget(true)
                    )
                    .otherwise(new Target<String>() {
                        @Override
                        public boolean post(String item) {
                            otherwiseReceived.set(true);
                            return true;
                        }

                        @Override
                        public boolean post(String item, Duration timeout) {
                            otherwiseReceived.set(true);
                            return true;
                        }
                    })
                    .build();

            assertTrue(block.post("hello", Duration.ofSeconds(1)));
            assertTrue(otherwiseReceived.get());
        }

        @Test
        void whenTargetRejectsItem_returnsFalse() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, timeoutAwareTarget(false))
                    .otherwise(timeoutAwareTarget(true))
                    .build();

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
        }

        @Test
        void otherwiseTargetRejectsItem_returnsFalse() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> false, timeoutAwareTarget(true))
                    .otherwise(timeoutAwareTarget(false))
                    .build();

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
        }

        @Test
        void nullTimeout_throwsNullValueException() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, timeoutAwareTarget(true))
                    .otherwise(timeoutAwareTarget(true))
                    .build();

            assertThrows(NullValueException.class, () -> block.post("hello", null));
        }

        @Test
        void negativeTimeout_throwsDurationOutsideRangeException() {
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, timeoutAwareTarget(true))
                    .otherwise(timeoutAwareTarget(true))
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
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> true)
                    .otherwise(item -> true)
                    .build();

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> block.post("hello", Duration.ofSeconds(1))
            );
        }

        @Test
        void downstreamIsAsyncBuffer_boundedByBufferCapacity() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("BranchBlockTest")
                    .build();

            try {
                BufferBlock<String> buffer = BufferBlock.<String>builder()
                        .capacity(10)
                        .executor(executor)
                        .build();

                CountDownLatch delivered = new CountDownLatch(1);
                AtomicReference<String> received = new AtomicReference<>();

                buffer.linkTo(item -> {
                    received.set(item);
                    delivered.countDown();
                    return true;
                });

                BranchBlock<String> block = BranchBlock.<String>builder()
                        .when(item -> true, buffer)
                        .otherwise(timeoutAwareTarget(true))
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
        void complete_cascadesToAllTargets() throws Exception {
            CountDownLatch branchCompleted = new CountDownLatch(1);
            CountDownLatch otherwiseCompleted = new CountDownLatch(1);

            Target<String> branchTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    branchCompleted.countDown();
                }
            };

            Target<String> otherwiseTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    otherwiseCompleted.countDown();
                }
            };

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("a"), branchTarget)
                    .otherwise(otherwiseTarget)
                    .build();

            block.complete();

            assertTrue(branchCompleted.await(5, TimeUnit.SECONDS));
            assertTrue(otherwiseCompleted.await(5, TimeUnit.SECONDS));
        }

        @Test
        void complete_isIdempotent() throws Exception {
            AtomicInteger completeCount = new AtomicInteger(0);

            Target<String> branchTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    completeCount.incrementAndGet();
                }
            };

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, branchTarget)
                    .otherwise(item -> true)
                    .build();

            block.complete();
            block.complete();

            assertEquals(1, completeCount.get());
        }

        @Test
        void completion_resolvesAfterAllTargetsComplete() throws Exception {
            ActionBlock<String> branchAction = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            ActionBlock<String> otherwiseAction = ActionBlock.<String>builder()
                    .action(item -> {
                    })
                    .build();

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("y"), branchAction)
                    .otherwise(otherwiseAction)
                    .build();

            block.post("yes");
            block.post("no");

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
        void itemPostedHandler_calledWhenRoutedToBranchTarget() {
            AtomicReference<String> postedItem = new AtomicReference<>();
            AtomicBoolean wasAccepted = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> true)
                    .otherwise(item -> true)
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
        void itemPostedHandler_calledWhenRoutedToOtherwiseTarget() {
            AtomicReference<String> postedItem = new AtomicReference<>();
            AtomicBoolean wasAccepted = new AtomicBoolean(false);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> false, item -> true)
                    .otherwise(item -> true)
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
        void itemDeliveredHandler_calledWhenBranchTargetAccepts() {
            AtomicReference<String> deliveredItem = new AtomicReference<>();

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> true)
                    .otherwise(item -> true)
                    .itemDeliveredHandler((source, target, item) -> deliveredItem.set(item))
                    .build();

            block.post("hello");

            assertEquals("hello", deliveredItem.get());
        }

        @Test
        void itemDeliveredHandler_notCalledWhenBranchTargetRejects() {
            AtomicBoolean deliveryCalled = new AtomicBoolean(false);
            AtomicBoolean wasAccepted = new AtomicBoolean(true);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> false)
                    .otherwise(item -> true)
                    .itemDeliveredHandler((source, target, item) -> deliveryCalled.set(true))
                    .itemPostedHandler((source, item, accepted) -> wasAccepted.set(accepted))
                    .build();

            block.post("hello");

            assertFalse(deliveryCalled.get());
            assertFalse(wasAccepted.get());
        }

        @Test
        void itemDeliveredHandler_calledWhenOtherwiseTargetAccepts() {
            AtomicReference<String> deliveredItem = new AtomicReference<>();

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> false, item -> true)
                    .otherwise(item -> true)
                    .itemDeliveredHandler((source, target, item) -> deliveredItem.set(item))
                    .build();

            block.post("hello");

            assertEquals("hello", deliveredItem.get());
        }

        @Test
        void itemDeliveredHandler_notCalledWhenOtherwiseTargetRejects() {
            AtomicBoolean deliveryCalled = new AtomicBoolean(false);
            AtomicBoolean wasAccepted = new AtomicBoolean(true);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> false, item -> true)
                    .otherwise(item -> false)
                    .itemDeliveredHandler((source, target, item) -> deliveryCalled.set(true))
                    .itemPostedHandler((source, item, accepted) -> wasAccepted.set(accepted))
                    .build();

            block.post("hello");

            assertFalse(deliveryCalled.get());
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
            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, item -> true)
                    .otherwise(item -> true)
                    .build();

            assertEquals(0, block.inFlight());
        }

        @Test
        void inFlight_sumsDelegatesAcrossAllArms() {
            // when-arm target reports 10 in-flight; otherwise target reports 5.
            Target<String> whenTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 10;
                }
            };

            Target<String> otherwiseTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 5;
                }
            };

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> true, whenTarget)
                    .otherwise(otherwiseTarget)
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
            AtomicInteger received = new AtomicInteger(0);

            BranchBlock<String> block = BranchBlock.<String>builder()
                    .when(item -> item.startsWith("a"), item -> {
                        received.incrementAndGet();
                        return true;
                    })
                    .otherwise(item -> {
                        received.incrementAndGet();
                        return true;
                    })
                    .build();

            block.onLinked();
            block.onLinked();

            assertTrue(block.post("a1"));

            block.complete();

            assertTrue(block.post("b1"));

            block.complete();

            assertFalse(block.post("c"));
            assertEquals(2, received.get());
        }
    }
}
