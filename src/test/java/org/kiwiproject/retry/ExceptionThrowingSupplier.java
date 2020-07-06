package org.kiwiproject.retry;

import lombok.Getter;

class ExceptionThrowingSupplier<T> implements InvocationCountingSupplier<T> {

    private final T value;
    private int timesToThrowException;

    @Getter
    private int count;

    ExceptionThrowingSupplier(T value) {
        this.value = value;
    }

    ExceptionThrowingSupplier<T> withTimesToThrowException(int times) {
        this.timesToThrowException = times;
        return this;
    }

    @Override
    public T get() {
        ++count;
        if (count <= timesToThrowException) {
            throw new RuntimeException("error on attempt " + count);
        }
        return value;
    }
}
