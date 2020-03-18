package org.kiwiproject.base;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility methods for instances of {@link Optional}, and which are not already present in {@link Optional}.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@UtilityClass
public class Optionals {

    /**
     * Takes an action if the {@code optional} is present, otherwise throws a {@link RuntimeException} that is
     * created by the given {@link Supplier}.
     * <p>
     * Note that only {@link RuntimeException}s can be thrown. Use
     * {@link #ifPresentOrElseThrowChecked(Optional, Consumer, Supplier)} if you need to throw a checked exception.
     *
     * @param optional          the {@link Optional} to act upon
     * @param action            action to be performed if a value is present in {@code optional}
     * @param exceptionSupplier supplier for a {@link RuntimeException} that will be thrown if {@code optional} is empty
     * @param <T>               the type parameter
     */
    public static <T> void ifPresentOrElseThrow(
            Optional<T> optional,
            Consumer<? super T> action,
            Supplier<? extends RuntimeException> exceptionSupplier) {

        checkArgumentNotNull(optional);
        checkArgumentNotNull(action);
        checkArgumentNotNull(exceptionSupplier);

        if (optional.isPresent()) {
            action.accept(optional.get());
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Takes an action if the {@code optional} is present, otherwise throws a checked exception that is
     * created by the given {@link Supplier}.
     *
     * @param optional                 the {@link Optional} to act upon
     * @param action                   action to be performed if a value is present in {@code optional}
     * @param checkedExceptionSupplier supplier for the checked exception that will be thrown if {@code optional} is empty
     * @param <T>                      the type parameter
     * @param <E>                      the type of checked exception
     * @throws E if the {@code optional} is empty
     */
    public static <T, E extends Exception> void ifPresentOrElseThrowChecked(
            Optional<T> optional,
            Consumer<? super T> action,
            Supplier<E> checkedExceptionSupplier) throws E {

        checkArgumentNotNull(optional);
        checkArgumentNotNull(action);
        checkArgumentNotNull(checkedExceptionSupplier);

        if (optional.isPresent()) {
            action.accept(optional.get());
        } else {
            throw checkedExceptionSupplier.get();
        }
    }
}
