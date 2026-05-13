package software.frisby.core.concurrency.fluent;

import software.frisby.core.concurrency.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Fluent builder for an async delay stage.
 *
 * <p>Each item posted to the block is held for a configurable duration before being forwarded
 * to the downstream target on a background thread.  The delay may be fixed for all items or
 * computed per-item by a caller-supplied function.  Acts as a {@code T → T} stage.</p>
 *
 * <pre>{@code
 * Pipeline<Task> pipeline = Pipeline.<Task>builder()
 *         .from(Buffer.of(Task.class))
 *         .then(Delay.of(Task.class)
 *                 .delay(task -> task.scheduledDelay()))
 *         .then(task -> execute(task));
 * }</pre>
 *
 * @param <T> The type of items passed through this stage.
 * @see Buffer
 */
public final class Delay<T> implements PipelineStage<T, T>, ExecutorAwareStage, AsyncObservableBlockBuilder<T, T, Delay<T>> {
    private Duration delay;
    private Function<T, Duration> delayFunction;
    private Integer capacity;
    private Executor executor;
    private ErrorOccurredHandler<T> errorOccurredHandler;
    private ItemPostedHandler<T> itemPostedHandler;
    private ItemDeliveredHandler<T> itemDeliveredHandler;

    private DelayBlock<T> block;

    private Delay() {
    }

    /**
     * Returns a new {@code Delay} builder.
     *
     * @param <T> The type of items to delay.
     * @return A new {@code Delay} instance.
     */
    public static <T> Delay<T> of() {
        return new Delay<>();
    }

    /**
     * Returns a new {@code Delay} builder.  {@code ignored} is used solely for type
     * inference at the call site; it is not stored.
     *
     * @param <T>     The type of items to delay.
     * @param ignored The item type class; used for inference only.
     * @return A new {@code Delay} instance.
     */
    public static <T> Delay<T> of(Class<T> ignored) {
        return of();
    }

    /**
     * Returns a new {@code Delay} builder.  {@code itemType} is used solely for type
     * inference at the call site; it is not stored.  Use this overload when {@code T} is
     * itself a generic type (e.g. {@code List<Message>}) and a {@code Class} literal cannot
     * capture the full type.
     *
     * @param <T>      The type of items to delay.
     * @param itemType The generic type token; used for inference only.
     * @return A new {@code Delay} instance.
     */
    public static <T> Delay<T> of(GenericType<T> itemType) {
        return of(itemType.getRawType());
    }

    /**
     * Returns a new {@code Delay<List<T>>} builder for pipelines that carry lists as their
     * item type.  {@code ignored} is used solely for type inference; it is not stored.
     *
     * @param <T>     The element type of the lists to delay.
     * @param ignored The element type class; used for inference only.
     * @return A new {@code Delay<List<T>>} instance.
     */
    public static <T> Delay<List<T>> ofLists(Class<T> ignored) {
        return of(new GenericType<>() {
        });
    }

    /**
     * Sets a fixed delay that will be applied to every item posted to the block.
     *
     * @param delay The duration each item will be held before being forwarded to the linked
     *              target; must be positive.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException            if {@code delay} is null.
     * @throws software.frisby.core.validation.DurationOutsideRangeException if {@code delay} is not positive.
     */
    public Delay<T> delay(Duration delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Sets a function that computes a per-item delay.  The function receives each posted item
     * and returns the duration for which it should be held before being forwarded.
     *
     * <pre>{@code
     * builder.delay(task -> task.scheduledDelay())
     * }</pre>
     *
     * @param delayFunction The function that computes the delay for each posted item.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NullValueException if {@code delayFunction} is null.
     */
    public Delay<T> delay(Function<T, Duration> delayFunction) {
        this.delayFunction = delayFunction;
        return this;
    }

    /**
     * Optional. Sets the maximum number of items the internal delay queue can hold before backpressure
     * is applied to posting threads.  Defaults to {@code 1024} if not set.
     *
     * @param capacity The maximum queue capacity; must be at least {@code 1}.
     * @return This builder, for method chaining.
     * @throws software.frisby.core.validation.NumericValueOutsideRangeException if {@code capacity} is less than {@code 1}.
     */
    public Delay<T> capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    @Override
    public Delay<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public Delay<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public Delay<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public Source<T> toSource() {
        if (null == this.block) {
            this.block = toBlock();
        }

        return this.block;
    }

    @Override
    public Target<T> toTarget() {
        if (null == this.block) {
            this.block = toBlock();
        }

        return block;
    }

    @Override
    public void executor(Executor executor) {
        this.executor = executor;
    }

    private DelayBlock<T> toBlock() {
        DelayBlockBuilder<T> builder = DelayBlock.<T>builder()
                .executor(executor)
                .itemPostedHandler(itemPostedHandler)
                .itemDeliveredHandler(itemDeliveredHandler)
                .errorOccurredHandler(errorOccurredHandler);

        if (null != delayFunction) {
            builder.delay(delayFunction);
        } else {
            builder.delay(delay);
        }

        if (null != capacity) {
            builder.capacity(capacity);
        }

        return builder.build();
    }
}
