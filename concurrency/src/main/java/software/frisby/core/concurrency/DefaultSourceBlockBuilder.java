package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

final class DefaultSourceBlockBuilder<T> implements SourceBlockBuilder<T> {
    private static final String SUPPLIER_ALREADY_CONFIGURED_MSG =
            "The 'SourceBlock' block already has a supplier configured.  Call only one of supplier(...) or batchSupplier(...).";

    private Supplier<T> singleItemSupplier;
    private Supplier<List<T>> batchSupplier;
    private Executor executor;
    private ItemDeliveredHandler<T> itemDeliveredHandler;
    private ErrorOccurredHandler<T> errorOccurredHandler;
    private SourceConcurrencyPolicy concurrencyPolicy;
    private boolean supplierConfigured;

    DefaultSourceBlockBuilder() {
    }

    @Override
    public SourceBlockBuilder<T> supplier(Supplier<T> supplier) {
        throwIfSupplierConfigured();

        this.singleItemSupplier = supplier;
        this.supplierConfigured = true;

        return this;
    }

    @Override
    public SourceBlockBuilder<T> batchSupplier(Supplier<List<T>> supplier) {
        throwIfSupplierConfigured();

        this.batchSupplier = supplier;
        this.supplierConfigured = true;

        return this;
    }

    @Override
    public SourceBlockBuilder<T> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public SourceBlockBuilder<T> itemDeliveredHandler(ItemDeliveredHandler<T> handler) {
        this.itemDeliveredHandler = handler;
        return this;
    }

    @Override
    public SourceBlockBuilder<T> errorOccurredHandler(ErrorOccurredHandler<T> handler) {
        this.errorOccurredHandler = handler;
        return this;
    }

    @Override
    public SourceBlockBuilder<T> concurrencyPolicy(SourceConcurrencyPolicy policy) {
        Values.notNull("policy", policy);

        this.concurrencyPolicy = policy;
        return this;
    }

    @Override
    public SourceBlock<T> build() {
        if (null == this.singleItemSupplier && null == this.batchSupplier) {
            throw new IllegalStateException(
                    "A supplier must be configured.  Call supplier(Supplier<T>) for single-item mode or supplier(Supplier<List<T>>) for batch mode."
            );
        }

        return new DefaultSourceBlock<>(
                this.singleItemSupplier,
                this.batchSupplier,
                this.concurrencyPolicy,
                this.executor,
                this.itemDeliveredHandler,
                this.errorOccurredHandler
        );
    }

    private void throwIfSupplierConfigured() {
        if (this.supplierConfigured) {
            throw new IllegalStateException(SUPPLIER_ALREADY_CONFIGURED_MSG);
        }
    }
}
