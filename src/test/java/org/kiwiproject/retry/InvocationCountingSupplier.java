package org.kiwiproject.retry;

import java.util.function.Supplier;

interface InvocationCountingSupplier<T> extends Supplier<T> {

    int getCount();
}
