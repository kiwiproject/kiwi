package org.kiwiproject.retry;

import lombok.Getter;

class NullReturningSupplier<T> implements InvocationCountingSupplier<T> {

    private final T value;
    private int timesToReturnNull;

    @Getter
    private int count;

    NullReturningSupplier(T value) {
        this.value = value;
    }

    NullReturningSupplier<T> withTimesToReturnNull(int times) {
        this.timesToReturnNull = times;
        return this;
    }

    @Override
    public T get() {
        ++count;
        if (count <= timesToReturnNull) {
            return null;
        }
        return value;
    }
}
