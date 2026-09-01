package software.frisby.core.concurrency;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.frisby.core.concurrency.fluent.Tap;
import software.frisby.core.validation.DurationOutsideRangeException;
import software.frisby.core.validation.NullValueException;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TapBlock} and its builder.  Covers builder validation, {@code post}
 * behavior (including null item, consumer invocation, pass-through, and exception propagation),
 * the {@code inFlight} delegation, completion lifecycle, and the {@link Tap} fluent builder.
 *
 * <p>{@code TapBlock} is a synchronous pass-through stage: it invokes a {@code Consumer}
 * on each item on the posting thread and then forwards the same unchanged item to the linked
 * downstream target.  There is no internal queue and no executor.
 * {@link TapBlock#complete()} cascades immediately to the linked downstream target.</p>
 */
class TapBlockTest {
    private static final String NO_CONSUMER_MSG =
            "The 'TapBlock' block requires a consumer. Call consumer(consumer) before calling build().";
    private static final String LINK_TO_SELF_MSG =
            "The 'target' value is invalid. A block cannot be linked to itself.";
    private static final String LINK_TO_CALLED_TWICE_MSG =
            "The 'TapBlock' block already has a linked target. A single-target block may only be linked to one downstream target.";

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    class Builder {
        @Test
        void nullConsumer_throwsIllegalStateException() {
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> TapBlock.<String>builder().build()
            );

            assertEquals(NO_CONSUMER_MSG, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // post(T)
    // -------------------------------------------------------------------------

    @Nested
    class Post {
        @Test
        void nullItem_consumerNotInvoked_returnsFalse() {
            AtomicInteger consumerCount = new AtomicInteger();
            AtomicInteger downstreamCount = new AtomicInteger();

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> consumerCount.incrementAndGet())
                    .build();

            block.linkTo(item -> {
                downstreamCount.incrementAndGet();
                return true;
            });

            assertFalse(block.post(null));
            assertEquals(0, consumerCount.get());
            assertEquals(0, downstreamCount.get());
        }

        @Test
        void validItem_consumerInvoked_withItem() {
            AtomicReference<String> consumerReceived = new AtomicReference<>();

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(consumerReceived::set)
                    .build();

            block.linkTo(item -> true);

            block.post("hello");

            assertEquals("hello", consumerReceived.get());
        }

        @Test
        void validItem_forwardedUnchangedToDownstream() {
            AtomicReference<String> downstreamReceived = new AtomicReference<>();
            String original = "hello";

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(item -> {
                downstreamReceived.set(item);
                return true;
            });

            block.post(original);

            assertSame(original, downstreamReceived.get());
        }

        @Test
        void consumerThrows_exceptionPropagatesToCallingThread() {
            AtomicInteger downstreamCount = new AtomicInteger();

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                        throw new RuntimeException("consumer failure");
                    })
                    .build();

            block.linkTo(item -> {
                downstreamCount.incrementAndGet();
                return true;
            });

            assertThrows(
                    RuntimeException.class,
                    () -> block.post("hello")
            );

            assertEquals(0, downstreamCount.get());
        }

        @Test
        void afterComplete_returnsFalse() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(item -> true);
            block.complete();

            assertFalse(block.post("hello"));
        }

        @Test
        void returnsTrue_whenItemAccepted() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(item -> true);

            assertTrue(block.post("hello"));
        }
    }

    // -------------------------------------------------------------------------
    // post(T, Duration)
    // -------------------------------------------------------------------------

    @Nested
    class PostWithTimeout {
        // A Target that also implements post(T, Duration) — plain lambda targets only
        // implement the SAM and would hit Target's throwing default otherwise.
        private static Target<String> timeoutAwareTarget(boolean accepted, AtomicReference<String> received) {
            return new Target<>() {
                @Override
                public boolean post(String item) {
                    received.set(item);
                    return accepted;
                }

                @Override
                public boolean post(String item, Duration timeout) {
                    received.set(item);
                    return accepted;
                }
            };
        }

        @Test
        void nullItem_consumerNotInvoked_returnsFalse() {
            AtomicInteger consumerCount = new AtomicInteger();
            AtomicReference<String> received = new AtomicReference<>();

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> consumerCount.incrementAndGet())
                    .build();

            block.linkTo(timeoutAwareTarget(true, received));

            assertFalse(block.post(null, Duration.ofSeconds(1)));
            assertEquals(0, consumerCount.get());
            assertNull(received.get());
        }

        @Test
        void validItem_consumerInvokedAndForwardedUnchanged_returnsTrue() {
            AtomicReference<String> consumerReceived = new AtomicReference<>();
            AtomicReference<String> downstreamReceived = new AtomicReference<>();
            String original = "hello";

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(consumerReceived::set)
                    .build();

            block.linkTo(timeoutAwareTarget(true, downstreamReceived));

            assertTrue(block.post(original, Duration.ofSeconds(1)));
            assertEquals(original, consumerReceived.get());
            assertSame(original, downstreamReceived.get());
        }

        @Test
        void downstreamRejects_returnsFalse() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(timeoutAwareTarget(false, new AtomicReference<>()));

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
        }

        @Test
        void consumerThrows_exceptionPropagatesToCallingThread() {
            AtomicInteger downstreamCount = new AtomicInteger();

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                        throw new RuntimeException("consumer failure");
                    })
                    .build();

            block.linkTo(new Target<>() {
                @Override
                public boolean post(String item) {
                    downstreamCount.incrementAndGet();
                    return true;
                }

                @Override
                public boolean post(String item, Duration timeout) {
                    downstreamCount.incrementAndGet();
                    return true;
                }
            });

            assertThrows(
                    RuntimeException.class,
                    () -> block.post("hello", Duration.ofSeconds(1))
            );

            assertEquals(0, downstreamCount.get());
        }

        @Test
        void afterComplete_returnsFalse() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(timeoutAwareTarget(true, new AtomicReference<>()));
            block.complete();

            assertFalse(block.post("hello", Duration.ofSeconds(1)));
        }

        @Test
        void nullTimeout_throwsNullValueException() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            assertThrows(NullValueException.class, () -> block.post("hello", null));
        }

        @Test
        void negativeTimeout_throwsDurationOutsideRangeException() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            assertThrows(
                    DurationOutsideRangeException.class,
                    () -> block.post("hello", Duration.ofSeconds(-1))
            );
        }

        @Test
        void downstreamDoesNotSupportTimeout_throwsUnsupportedOperationException() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            // A plain lambda target only implements the SAM post(T) — it has not opted into
            // bounded-wait posting, so the inherited Target default must throw.
            block.linkTo(item -> true);

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> block.post("hello", Duration.ofSeconds(1))
            );
        }

        @Test
        void downstreamIsAsyncBuffer_boundedByBufferCapacity() throws Exception {
            NamedExecutorService executor = NamedExecutorService.builder()
                    .threadPrefix("TapBlockTest")
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

                TapBlock<String> block = TapBlock.<String>builder()
                        .consumer(item -> {
                        })
                        .build();

                block.linkTo(buffer);

                assertTrue(block.post("hello", Duration.ofSeconds(5)));
                assertTrue(delivered.await(5, TimeUnit.SECONDS));
                assertEquals("hello", received.get());
            } finally {
                executor.shutdown();
            }
        }
    }

    // -------------------------------------------------------------------------
    // linkTo
    // -------------------------------------------------------------------------

    @Nested
    class LinkTo {
        @Test
        void selfLink_throwsIllegalArgumentException() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> block.linkTo(block)
            );

            assertEquals(LINK_TO_SELF_MSG, ex.getMessage());
        }

        @Test
        void secondLink_throwsIllegalStateException() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(item -> true);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> block.linkTo(item -> true)
            );

            assertEquals(LINK_TO_CALLED_TWICE_MSG, ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // inFlight()
    // -------------------------------------------------------------------------

    @Nested
    class InFlight {
        @Test
        void inFlight_noDownstream_returnsZero() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(item -> true);

            assertEquals(0, block.inFlight());
        }

        @Test
        void inFlight_withDownstream_delegatesToDownstream() {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public int inFlight() {
                    return 42;
                }
            });

            assertEquals(42, block.inFlight());
        }
    }

    // -------------------------------------------------------------------------
    // Completion lifecycle
    // -------------------------------------------------------------------------

    @Nested
    class Completion {
        @Test
        void complete_cascadesToDownstream() throws InterruptedException {
            CountDownLatch downstreamCompleted = new CountDownLatch(1);

            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(new Target<>() {
                @Override
                public boolean post(String item) {
                    return true;
                }

                @Override
                public void complete() {
                    downstreamCompleted.countDown();
                }
            });

            block.complete();

            assertTrue(downstreamCompleted.await(5, TimeUnit.SECONDS));
        }

        @Test
        void completion_resolvesAfterComplete() throws Exception {
            TapBlock<String> block = TapBlock.<String>builder()
                    .consumer(item -> {
                    })
                    .build();

            block.linkTo(item -> true);

            block.complete();

            block.completion().get(5, TimeUnit.SECONDS);
        }
    }

    // -------------------------------------------------------------------------
    // Tap fluent builder
    // -------------------------------------------------------------------------

    @Nested
    class FluentTap {
        @Test
        void of_withClassToken_returnsBuilder() {
            assertNotNull(
                    Tap.of(String.class)
                            .consumer(item -> {
                            })
            );
        }

        @Test
        void of_withGenericTypeToken_returnsBuilder() {
            assertNotNull(
                    Tap.of(new GenericType<String>() {
                            })
                            .consumer(item -> {
                            })
            );
        }

        @Test
        void itemPostedHandler_isAccepted() {
            assertNotNull(
                    Tap.of(String.class)
                            .consumer(item -> {
                            })
                            .itemPostedHandler((source, item, accepted) -> {
                            })
            );
        }

        @Test
        void itemDeliveredHandler_isAccepted() {
            assertNotNull(
                    Tap.of(String.class)
                            .consumer(item -> {
                            })
                            .itemDeliveredHandler((source, target, item) -> {
                            })
            );
        }

        @Test
        void toTarget_returnsBlock() {
            assertNotNull(
                    Tap.of(String.class)
                            .consumer(item -> {
                            })
                            .toTarget()
            );
        }

        @Test
        void toSource_returnsBlock() {
            assertNotNull(
                    Tap.of(String.class)
                            .consumer(item -> {
                            })
                            .toSource()
            );
        }

        @Test
        void nullConsumer_throwsIllegalStateException() {
            assertThrows(
                    IllegalStateException.class,
                    () -> Tap.of(String.class).toTarget()
            );
        }
    }
}

