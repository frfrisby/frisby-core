package software.frisby.core.concurrency;

import java.util.function.Consumer;

final class DefaultActionBlockBuilder<T> implements ActionBlockBuilder<T> {
    private Consumer<T> action;
    private ItemPostedHandler<T> itemPostedHandler;

    DefaultActionBlockBuilder() {
    }

    @Override
    public ActionBlockBuilder<T> action(Consumer<T> action) {
        this.action = action;
        return this;
    }

    @Override
    public ActionBlockBuilder<T> itemPostedHandler(ItemPostedHandler<T> handler) {
        this.itemPostedHandler = handler;
        return this;
    }

    @Override
    public ActionBlock<T> build() {
        return new DefaultActionBlock<>(this.action, this.itemPostedHandler);
    }
}
