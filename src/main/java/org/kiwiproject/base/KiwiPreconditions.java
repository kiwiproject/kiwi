package org.kiwiproject.base;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.function.Supplier;

import static com.google.common.math.IntMath.mod;

@UtilityClass
public class KiwiPreconditions {

    @SafeVarargs
    public static <T> void checkEvenItemCount(T... items) {
        checkEvenItemCount(() -> items.length);
    }

    public static <T> void checkEvenItemCount(Collection<T> items) {
        checkEvenItemCount(items::size);
    }

    public static void checkEvenItemCount(Supplier<Integer> countSupplier) {
        Integer count = countSupplier.get();
        Preconditions.checkArgument(mod(count, 2) == 0, "must be an even number of items (received %s)", count);
    }

    @SneakyThrows
    public static <T extends Throwable> void checkArgument(boolean expression, Class<T> exceptionType) {
        if (!expression) {
            throw exceptionType.newInstance();
        }
    }

    @SneakyThrows
    public static <T extends Throwable> void checkArgument(boolean expression,
                                                           Class<T> exceptionType,
                                                           String errorMessage) {
        if (!expression) {
            Constructor<T> constructor = exceptionType.getConstructor(String.class);
            throw constructor.newInstance(errorMessage);
        }
    }

    @SneakyThrows
    public static <T extends Throwable> void checkArgument(boolean expression,
                                                           Class<T> exceptionType,
                                                           String errorMessageTemplate,
                                                           Object... errorMessageArgs) {
        if (!expression) {
            Constructor<T> constructor = exceptionType.getConstructor(String.class);
            String message = KiwiStrings.format(errorMessageTemplate, errorMessageArgs);
            throw constructor.newInstance(message);
        }
    }

}
