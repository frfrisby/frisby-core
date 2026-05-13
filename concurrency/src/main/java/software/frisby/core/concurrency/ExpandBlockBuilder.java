package software.frisby.core.concurrency;

import java.util.List;

/**
 * Builder for constructing an {@link ExpandBlock}.  Obtain an instance via
 * {@link ExpandBlock#builder()}.
 *
 * @param <T> The type of individual elements unpacked from each received list.
 */
public interface ExpandBlockBuilder<T> extends ObservableBlockBuilder<List<T>, T, ExpandBlockBuilder<T>> {
    /**
     * Returns a new {@link ExpandBlock} configured by this builder.
     *
     * @return A new {@link ExpandBlock} instance.
     */
    ExpandBlock<T> build();
}
