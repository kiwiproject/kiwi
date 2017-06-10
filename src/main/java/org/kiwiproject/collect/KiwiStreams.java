package org.kiwiproject.collect;

import lombok.experimental.UtilityClass;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

@UtilityClass
public class KiwiStreams {

    public static <A, B, C> Stream<C> zip(Stream<A> aStream,
                                          Stream<B> bStream,
                                          BiFunction<? super A, ? super B, C> zipFunction) {

        requireNonNull(aStream);
        requireNonNull(bStream);
        requireNonNull(zipFunction);

        Iterator<A> aIterator = aStream.iterator();
        Iterator<B> bIterator = bStream.iterator();
        Iterator<C> cIterator = makeZippingIterator(aIterator, bIterator, zipFunction);
        Iterable<C> iterable = toIterable(cIterator);

        return StreamSupport.stream(iterable.spliterator(), areBothParallel(aStream, bStream));
    }

    private static <A, B, C> Iterator<C> makeZippingIterator(Iterator<A> aIterator,
                                                             Iterator<B> bIterator,
                                                             BiFunction<? super A, ? super B, C> zipFunction) {
        return new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public C next() {
                return zipFunction.apply(aIterator.next(), bIterator.next());
            }
        };
    }

    private static <C> Iterable<C> toIterable(Iterator<C> cIterator) {
        return () -> cIterator;
    }

    private static <A, B> boolean areBothParallel(Stream<A> aStream, Stream<B> bStream) {
        return aStream.isParallel() && bStream.isParallel();
    }

}
