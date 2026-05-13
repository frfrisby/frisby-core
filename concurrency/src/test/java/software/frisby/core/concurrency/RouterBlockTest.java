package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.validation.NullElementException;
import software.frisby.core.validation.NullValueException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RouterBlock} and its builder.  Covers builder validation, routing
 * semantics (custom function and round-robin), the {@code complete()} / {@code completion()}
 * lifecycle, and all delegate handler callbacks.
 */
class RouterBlockTest {
    private static final Target<String> ACCEPT = item -> true;

    private static final String TOO_FEW_TARGETS_MSG =
            "The 'RouterBlock' block requires at least two targets.  Call target() or targets() before calling build().";

    private static final String ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG =
            "The 'RouterBlock' block already has a routing function configured.  Call only one of roundRobin(), balanced(), sticky(...), or routingFunction(...).";

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullTargetInSingleAdd_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> RouterBlock.<String>builder().target(null)
            );
        }


        @Test
        void nullTargetList_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> RouterBlock.<String>builder().targets(null)
            );
        }

        @Test
        void nullElementInTargetList_throwsNullElementException() {
            List<Target<String>> listWithNull = new ArrayList<>();
            listWithNull.add(ACCEPT);
            listWithNull.add(null);

            assertThrows(
                    NullElementException.class,
                    () -> RouterBlock.<String>builder().targets(listWithNull)
            );
        }

        @Test
        void zeroTargets_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> RouterBlock.<String>builder().build()
            );

            assertEquals(TOO_FEW_TARGETS_MSG, ex.getMessage());
        }

        @Test
        void oneTarget_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> RouterBlock.<String>builder()
                            .target(ACCEPT)
                            .build()
            );

            assertEquals(TOO_FEW_TARGETS_MSG, ex.getMessage());
        }

        @Test
        void nullRoutingFunction_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> RouterBlock.<String>builder().routingFunction(null)
            );
        }

        @Test
        void twoTargets_noRoutingFunction_buildsSuccessfully() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            assertNotNull(block);
        }

        @Test
        void targetsViaList_buildsSuccessfully() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .targets(List.of(ACCEPT, ACCEPT, ACCEPT))
                    .build();

            assertNotNull(block);
        }

        @Test
        void customRoutingFunction_buildsSuccessfully() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .routingFunction(item -> 0)
                    .build();

            assertNotNull(block);
        }

        @Test
        void explicitRoundRobin_buildsSuccessfully() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .roundRobin()
                    .build();

            assertNotNull(block);
        }

        @Test
        void balanced_buildsSuccessfully() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .balanced()
                    .build();

            assertNotNull(block);
        }

        @Test
        void roundRobinThenBalanced_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> RouterBlock.<String>builder()
                            .target(ACCEPT)
                            .target(ACCEPT)
                            .roundRobin()
                            .balanced()
            );

            assertEquals(ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }

        @Test
        void balancedThenRoutingFunction_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> RouterBlock.<String>builder()
                            .target(ACCEPT)
                            .target(ACCEPT)
                            .balanced()
                            .routingFunction(item -> 0)
            );

            assertEquals(ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }

        @Test
        void routingFunctionThenRoundRobin_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> RouterBlock.<String>builder()
                            .target(ACCEPT)
                            .target(ACCEPT)
                            .routingFunction(item -> 0)
                            .roundRobin()
            );

            assertEquals(ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }

        @Test
        void nullKeyExtractor_throwsNullValueException() {
            assertThrows(
                    NullValueException.class,
                    () -> RouterBlock.<String>builder().sticky(null)
            );
        }

        @Test
        void sticky_buildsSuccessfully() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .sticky(item -> item)
                    .build();

            assertNotNull(block);
        }

        @Test
        void stickyThenRoundRobin_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> RouterBlock.<String>builder()
                            .target(ACCEPT)
                            .target(ACCEPT)
                            .sticky(item -> item)
                            .roundRobin()
            );

            assertEquals(ROUTING_STRATEGY_ALREADY_CONFIGURED_MSG, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // post(T)
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Test
        void nullItem_returnsFalse() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            assertFalse(block.post(null));
        }

        @Test
        void afterComplete_returnsFalse() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .build();

            block.complete();

            assertFalse(block.post("hello"));
        }

        @Test
        void customRoutingFunction_routesToSpecifiedIndex() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        firstReceived.set(true);
                        return true;
                    })
                    .target(item -> {
                        secondReceived.set(true);
                        return true;
                    })
                    .routingFunction(item -> 1)
                    .build();

            assertTrue(block.post("hello"));
            assertFalse(firstReceived.get());
            assertTrue(secondReceived.get());
        }

        @Test
        void defaultRoundRobin_distributesEvenly() {
            AtomicInteger count1 = new AtomicInteger(0);
            AtomicInteger count2 = new AtomicInteger(0);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        count1.incrementAndGet();
                        return true;
                    })
                    .target(item -> {
                        count2.incrementAndGet();
                        return true;
                    })
                    .build();

            block.post("a");
            block.post("b");
            block.post("c");
            block.post("d");

            assertEquals(2, count1.get());
            assertEquals(2, count2.get());
        }

        @Test
        void whenTargetRejectsItem_returnsFalse() {
            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> false)
                    .target(ACCEPT)
                    .routingFunction(item -> 0)
                    .build();

            assertFalse(block.post("hello"));
        }

        @Test
        void balanced_routesToLeastLoadedTarget() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);

            Target<String> heavyTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    firstReceived.set(true);
                    return true;
                }

                @Override
                public int size() {
                    return 10;
                }
            };

            Target<String> lightTarget = new Target<>() {
                @Override
                public boolean post(String item) {
                    secondReceived.set(true);
                    return true;
                }

                @Override
                public int size() {
                    return 1;
                }
            };

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(heavyTarget)
                    .target(lightTarget)
                    .balanced()
                    .build();

            block.post("hello");

            assertFalse(firstReceived.get());
            assertTrue(secondReceived.get());
        }

        @Test
        void balanced_tieBreaksByLowestIndex() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);
            AtomicBoolean secondReceived = new AtomicBoolean(false);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        firstReceived.set(true);
                        return true;
                    })
                    .target(item -> {
                        secondReceived.set(true);
                        return true;
                    })
                    .balanced()
                    .build();

            block.post("hello");

            assertTrue(firstReceived.get());
            assertFalse(secondReceived.get());
        }

        @Test
        void balanced_routesToTargetWithLowestInFlight() {
            AtomicBoolean heavyReceived = new AtomicBoolean(false);
            AtomicBoolean lightReceived = new AtomicBoolean(false);

            // Both targets have size() == 0 (default); only inFlight() differs.
            // This proves balanced() consults inFlight(), not size().
            Target<String> heavy = new Target<>() {
                @Override
                public boolean post(String item) {
                    heavyReceived.set(true);
                    return true;
                }

                @Override
                public int inFlight() {
                    return 10;
                }
            };

            Target<String> light = new Target<>() {
                @Override
                public boolean post(String item) {
                    lightReceived.set(true);
                    return true;
                }

                @Override
                public int inFlight() {
                    return 1;
                }
            };

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(heavy)
                    .target(light)
                    .balanced()
                    .build();

            block.post("hello");

            assertFalse(heavyReceived.get());
            assertTrue(lightReceived.get());
        }

        @Test
        void balanced_inFlightTakesPrecedenceOverSize() {
            AtomicBoolean deepReceived = new AtomicBoolean(false);
            AtomicBoolean shallowReceived = new AtomicBoolean(false);

            // Both targets report size() == 0 but different inFlight() values, simulating
            // items held deep in each arm's pipeline (not in the head buffer).
            Target<String> deep = new Target<>() {
                @Override
                public boolean post(String item) {
                    deepReceived.set(true);
                    return true;
                }

                @Override
                public int inFlight() {
                    return 100;
                }
            };

            Target<String> shallow = new Target<>() {
                @Override
                public boolean post(String item) {
                    shallowReceived.set(true);
                    return true;
                }

                @Override
                public int inFlight() {
                    return 5;
                }
            };

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(deep)
                    .target(shallow)
                    .balanced()
                    .build();

            block.post("hello");

            assertFalse(deepReceived.get());
            assertTrue(shallowReceived.get());
        }

        @Test
        void sticky_sameKeyAlwaysRoutesToSameTarget() {
            AtomicInteger firstCount = new AtomicInteger(0);
            AtomicInteger secondCount = new AtomicInteger(0);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        firstCount.incrementAndGet();
                        return true;
                    })
                    .target(item -> {
                        secondCount.incrementAndGet();
                        return true;
                    })
                    .sticky(item -> item)
                    .build();

            block.post("customer-1");
            block.post("customer-1");
            block.post("customer-1");

            assertTrue(
                    (firstCount.get() == 3 && secondCount.get() == 0) ||
                            (firstCount.get() == 0 && secondCount.get() == 3)
            );
        }

        @Test
        void sticky_differentKeysRouteToSeparateTargets() {
            AtomicInteger firstCount = new AtomicInteger(0);
            AtomicInteger secondCount = new AtomicInteger(0);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        firstCount.incrementAndGet();
                        return true;
                    })
                    .target(item -> {
                        secondCount.incrementAndGet();
                        return true;
                    })
                    .sticky(item -> item)
                    .build();

            // "a".hashCode() = 97  → floorMod(97, 2) = 1 → second target
            // "b".hashCode() = 98  → floorMod(98, 2) = 0 → first target
            block.post("a");
            block.post("b");

            assertEquals(1, firstCount.get());
            assertEquals(1, secondCount.get());
        }

        @Test
        void sticky_nullKey_routesToFirstTarget() {
            AtomicBoolean firstReceived = new AtomicBoolean(false);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        firstReceived.set(true);
                        return true;
                    })
                    .target(ACCEPT)
                    .sticky(item -> null)
                    .build();

            block.post("hello");

            assertTrue(firstReceived.get());
        }
    }

    // -------------------------------------------------------------------------
    // complete() / completion()
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void complete_cascadesToAllTargets() throws Exception {
            CountDownLatch firstCompleted = new CountDownLatch(1);
            CountDownLatch secondCompleted = new CountDownLatch(1);

            Target<String> t1 = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    firstCompleted.countDown();
                }
            };

            Target<String> t2 = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    secondCompleted.countDown();
                }
            };

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(t1)
                    .target(t2)
                    .build();

            block.complete();

            assertTrue(firstCompleted.await(5, TimeUnit.SECONDS));
            assertTrue(secondCompleted.await(5, TimeUnit.SECONDS));
        }

        @Test
        void complete_isIdempotent() throws Exception {
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

            RouterBlock<String> block = RouterBlock.<String>builder()
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

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(downstream1)
                    .target(downstream2)
                    .build();

            block.post("a");
            block.post("b");

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

            RouterBlock<String> block = RouterBlock.<String>builder()
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
        void itemDeliveredHandler_calledWhenTargetAccepts() {
            AtomicReference<String> deliveredItem = new AtomicReference<>();

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(ACCEPT)
                    .target(ACCEPT)
                    .itemDeliveredHandler((source, target, item) -> deliveredItem.set(item))
                    .build();

            block.post("hello");

            assertEquals("hello", deliveredItem.get());
        }

        @Test
        void itemDeliveredHandler_notCalledWhenTargetRejects() {
            AtomicBoolean deliveryCalled = new AtomicBoolean(false);
            AtomicBoolean wasAccepted = new AtomicBoolean(true);

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> false)
                    .target(ACCEPT)
                    .routingFunction(item -> 0)
                    .itemDeliveredHandler((source, target, item) -> deliveryCalled.set(true))
                    .itemPostedHandler((source, item, accepted) -> wasAccepted.set(accepted))
                    .build();

            block.post("hello");

            assertFalse(deliveryCalled.get());
            assertFalse(wasAccepted.get());
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

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(item -> {
                        received.add(item);
                        return true;
                    })
                    .target(item -> true)
                    .routingFunction(item -> 0)
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

    // =========================================================================
    // inFlight()
    // =========================================================================

    @Nested
    class InFlight {
        @Test
        void inFlight_sumsAllTargets() {
            Target<String> first = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 7;
                }
            };

            Target<String> second = new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 3;
                }
            };

            RouterBlock<String> block = RouterBlock.<String>builder()
                    .target(first)
                    .target(second)
                    .routingFunction(item -> 0)
                    .build();

            assertEquals(10, block.inFlight());
        }
    }
}
