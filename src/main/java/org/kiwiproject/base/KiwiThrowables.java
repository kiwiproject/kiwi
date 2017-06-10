package org.kiwiproject.base;

import com.google.common.base.Throwables;
import com.google.errorprone.annotations.Immutable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class KiwiThrowables {

    @Immutable
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ThrowableInfo {

        public final String type;
        public final String message;
        public final String stackTrace;
        public final Throwable cause;

        public static ThrowableInfo of(Throwable throwable) {
            return new ThrowableInfo(
                    typeOf(throwable).orElse(null),
                    messageOf(throwable).orElse(null),
                    stackTraceOf(throwable).orElse(null),
                    causeOf(throwable).orElse(null));
        }

        public Optional<String> getType() {
            return Optional.ofNullable(type);
        }

        public Optional<String> getMessage() {
            return Optional.ofNullable(message);
        }

        public Optional<String> getStackTrace() {
            return Optional.ofNullable(stackTrace);
        }

        public Optional<Throwable> getCause() {
            return Optional.ofNullable(cause);
        }
    }

    public static Optional<String> typeOf(Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(Object::getClass)
                .map(Class::getName);
    }

    public static Optional<String> messageOf(Throwable throwable) {
        return Optional.ofNullable(throwable).map(Throwable::getMessage);
    }

    public static Optional<String> stackTraceOf(Throwable throwable) {
        return Optional.ofNullable(throwable)
                .map(Throwables::getStackTraceAsString);
    }

    public static Optional<Throwable> causeOf(Throwable throwable) {
        return Optional.ofNullable(throwable).map(Throwable::getCause);
    }

}
