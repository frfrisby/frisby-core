package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Allows implementers to provide a function that a {@link RouterBlock} uses to determine which
 * target a posted item will be forwarded to.
 * <p>
 * The function returns a zero-based index.  The {@link RouterBlock} maps it to an actual target
 * using {@link Math#floorMod}, so both positive and negative return values resolve safely to a
 * valid index.
 * <p>
 * Two built-in factory methods are provided: {@link #roundRobin()} and {@link #sticky(Function)}.
 * Load-balanced routing — where each item is sent to the target with the smallest current queue
 * depth — is available exclusively via {@link RouterBlockBuilder#balanced()}, which injects the
 * block's own target list at build time and prevents mismatches.
 * <p>
 * This is a {@link FunctionalInterface} by design.  Additional <em>abstract instance</em> methods
 * must never be added, as they would break lambda implementations.  Static factory methods
 * (such as {@link #roundRobin()} and {@link #sticky(Function)}) are permitted and do not affect
 * the SAM contract.
 *
 * @param <T> The type of item that will be evaluated by the routing function.
 */
@FunctionalInterface
public interface RoutingFunction<T> {
    /**
     * Returns a new {@link RoutingFunction} that distributes items across routes in round-robin
     * order.  Each call to {@link #route} increments an internal counter; the counter is private
     * to the returned function instance, so two callers that each obtain a round-robin function
     * from this method maintain independent sequences.
     *
     * @param <T> The type of item evaluated by the function.
     * @return A new round-robin {@link RoutingFunction}.
     */
    static <T> RoutingFunction<T> roundRobin() {
        AtomicInteger counter = new AtomicInteger(0);
        return item -> counter.getAndIncrement();
    }

    /**
     * Returns a new {@link RoutingFunction} that routes all items sharing the same key to the
     * same target, using the {@link Object#hashCode()} of the extracted key to select the index.
     * Items with the same key always resolve to the same target regardless of how many times they
     * are posted, providing stable key affinity across the lifetime of the router.
     * <p>
     * Typical use cases include sharding by customer identifier, device identifier, or any other
     * domain key where all items belonging to the same entity must be processed in order by the
     * same downstream target.
     * <p>
     * A {@code null} key is routed to index {@code 0}.
     *
     * @param <T>          The type of item evaluated by the function.
     * @param keyExtractor A function that extracts the routing key from each item.
     * @return A new sticky {@link RoutingFunction}.
     * @throws software.frisby.core.validation.NullValueException if {@code keyExtractor} is null.
     */
    static <T> RoutingFunction<T> sticky(Function<T, ?> keyExtractor) {
        Values.notNull("keyExtractor", keyExtractor);

        return item -> {
            Object key = keyExtractor.apply(item);
            return null == key ? 0 : key.hashCode();
        };
    }

    /**
     * Returns a zero-based route index for the provided item.
     *
     * @param item The item to evaluate for routing.
     * @return An integer index identifying the target route that will receive {@code item}.
     */
    int route(T item);
}
