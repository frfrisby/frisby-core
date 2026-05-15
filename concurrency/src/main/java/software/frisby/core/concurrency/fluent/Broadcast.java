package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;
import software.frisby.core.validation.Sequences;
import software.frisby.core.validation.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Fluent builder for a terminal broadcast stage.
 *
 * <p>Each item posted to this block is forwarded to every configured downstream
 * {@link Pipeline} target.  An optional cloning function can be provided to produce a
 * fresh copy of each item before delivery, which is recommended when the downstream pipelines
 * may mutate their inputs.  Acts as a terminal, divergent stage — items fan out to all targets
 * without merging back.</p>
 *
 * <pre>{@code
 * Pipeline<Event> pipeline = Pipeline.<Event>builder()
 *         .from(Buffer.of(Event.class))
 *         .to(Broadcast.of(Event.class)
 *                 .cloningFunction(event -> event.copy())
 *                 .targets(List.of(auditPipeline, analyticsPipeline, notificationPipeline)));
 * }</pre>
 *
 * @param <T> The type of items broadcast to all downstream targets.
 * @see Branch
 * @see Router
 */
public final class Broadcast<T> implements PipelineTarget<T>, ObservableBlockBuilder<T, T, Broadcast<T>> {
    private final List<Pipeline<T>> targets;
    private UnaryOperator<T> cloningFunction;

    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private BroadcastBlock<T> block;

    private Broadcast() {
        this.targets = new ArrayList<>();
    }

    /**
     * Returns a new {@code Broadcast} builder.
     *
     * @param <T> The type of items to broadcast.
     * @return A new {@code Broadcast} instance.
     */
    public static <T> Broadcast<T> of() {
        return new Broadcast<>();
    }

    /**
     * Returns a new {@code Broadcast} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to broadcast.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Broadcast} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Broadcast<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Broadcast} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to broadcast.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Broadcast} instance.
     */
    public static <T> Broadcast<T> of(GenericType<T> itemType) {
        return of(itemType.getRawType());
    }

    /**
     * Returns a new {@code Broadcast<List<T>>} builder for pipelines whose items are lists.
     * {@code ignored} is used solely for type inference at the call site; it is not stored.
     *
     * @param <T>     The element type of the lists to broadcast.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Broadcast<List<T>>} instance.
     */
    @SuppressWarnings("java:S1172")
    public static <T> Broadcast<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Optional. Sets the function that will be invoked to produce a fresh copy of each item
     * before it is delivered to each downstream target. If not configured, the original item
     * reference is passed to every target unchanged — appropriate for pipelines that carry
     * immutable messages.
     *
     * @param cloningFunction The function that clones each posted item before delivery.
     * @return The current builder instance.
     */
    public Broadcast<T> cloningFunction(UnaryOperator<T> cloningFunction) {
        this.cloningFunction = cloningFunction;
        return this;
    }

    /**
     * Adds a single downstream target.  This method is additive; each call appends one target
     * to the list.  Suitable for loop-based wiring of dynamically-sized target pools.
     *
     * @param target The downstream target to add.
     * @return The current builder instance.
     * @throws software.frisby.core.validation.NullValueException if {@code target} is null.
     */
    public Broadcast<T> target(Pipeline<T> target) {
        Values.notNull("target", target);
        this.targets.add(target);
        return this;
    }

    /**
     * Adds two or more downstream targets.  This method is additive; each call appends to the
     * accumulated target list.  For a single target, use {@link #target(Pipeline)} instead.
     *
     * @param targets The list of downstream targets to add.
     * @return The current builder instance.
     * @throws software.frisby.core.validation.NullValueException       if {@code targets} is null.
     * @throws software.frisby.core.validation.MissingElementsException if {@code targets} is empty.
     * @throws software.frisby.core.validation.NullElementException     if {@code targets} contains a
     *                                                                  null element.
     */
    public Broadcast<T> targets(List<Pipeline<T>> targets) {
        Sequences.notEmpty("targets", targets);
        this.targets.addAll(targets);
        return this;
    }

    @Override
    public Broadcast<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Broadcast<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Target<T> toTarget() {
        return toBlock();
    }

    private BroadcastBlock<T> toBlock() {
        if (null == this.block) {
            BroadcastBlockBuilder<T> builder = BroadcastBlock.<T>builder()
                    .itemPostedHandler(itemPostedHandler)
                    .itemDeliveredHandler(itemDeliveredHandler)
                    .cloningFunction(cloningFunction);

            for (Pipeline<T> target : targets) {
                builder.target(target);
            }

            this.block = builder.build();
        }

        return this.block;
    }
}
