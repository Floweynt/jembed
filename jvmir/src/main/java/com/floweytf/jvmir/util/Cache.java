package com.floweytf.jvmir.util;

import java.util.function.Supplier;

/**
 * A cache that lazily evaluates a supplier and saves the result until {@link Cache#markDirty()}.
 *
 * @param <T>
 */
public class Cache<T> implements Supplier<T> {
    private T impl;
    private final Supplier<T> supplier;

    /**
     * Constructs a cache from a supplier.
     *
     * @param supplier A supplier that provides the data for this cache
     */
    public Cache(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if(this.impl == null) {
            this.impl = supplier.get();
        }

        return impl;
    }

    /**
     * Marks the cache as dirty, so the next call to {@link Cache#get()} will call the supplier.
     */
    public void markDirty() {
        this.impl = null;
    }
}
