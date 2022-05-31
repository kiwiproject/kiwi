package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

class OptionalsTest {

    @Test
    void testIfPresentOrElseThrow_WhenPresent() {
        var stringOptional = Optional.of("42");
        var called = new AtomicBoolean();

        assertThatCode(() ->
                Optionals.ifPresentOrElseThrow(stringOptional,
                        value -> called.compareAndSet(false, true),
                        IllegalStateException::new)
        ).doesNotThrowAnyException();

        assertThat(called).isTrue();
    }

    @Test
    void testIfPresentOrElseThrow_WhenEmpty() {
        Optional<String> emptyOptional = Optional.empty();
        var called = new AtomicBoolean();

        assertThatThrownBy(() ->
                Optionals.ifPresentOrElseThrow(emptyOptional,
                        value -> called.set(true),
                        () -> new IllegalStateException("oops"))
        ).isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("oops");

        assertThat(called).isFalse();
    }

    static class MyCustomException extends Exception {
        MyCustomException(String message) {
            super(message);
        }
    }

    @Test
    void testIfPresentOrElseThrowChecked_WhenPresent() {
        var stringOptional = Optional.of("42");
        var called = new AtomicBoolean();

        assertThatCode(() ->
                Optionals.ifPresentOrElseThrowChecked(stringOptional,
                        value -> called.compareAndSet(false, true),
                        () -> new MyCustomException("bad things..."))
        ).doesNotThrowAnyException();

        assertThat(called).isTrue();
    }

    @Test
    void testIfPresentOrElseThrowChecked_WhenEmpty() {
        Optional<String> emptyOptional = Optional.empty();
        var called = new AtomicBoolean();

        assertThatThrownBy(() ->
                Optionals.ifPresentOrElseThrowChecked(emptyOptional,
                        value -> called.set(true),
                        () -> new MyCustomException("bad things..."))
        ).isExactlyInstanceOf(MyCustomException.class)
                .hasMessage("bad things...");

        assertThat(called).isFalse();
    }
}
