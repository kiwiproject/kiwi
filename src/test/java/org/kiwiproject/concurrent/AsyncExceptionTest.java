package org.kiwiproject.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

class AsyncExceptionTest {

    @Test
    void shouldConstructWithMessageAndFuture() {
        var future = CompletableFuture.failedFuture(new RuntimeException("crack!"));
        var ex = new AsyncException("boom!", future);

        assertThat(ex.getMessage()).isEqualTo("boom!");
        assertThat(ex.getCause()).isNull();
        assertThat(ex.getFuture()).isSameAs(future);
    }

    @Test
    void shouldConstructWithMessageAndCauseAndFuture() {
        var cause = new RuntimeException("crack!");
        var future = CompletableFuture.failedFuture(cause);
        var ex = new AsyncException("boom!", cause, future);

        assertThat(ex.getMessage()).isEqualTo("boom!");
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.hasFuture()).isTrue();
        assertThat(ex.getFuture()).isSameAs(future);
    }

    @Test
    void shouldPermitNullFuture() {
        var ex = new AsyncException("kabloom!", null);
        assertThat(ex.getFuture()).isNull();
        assertThat(ex.hasFuture()).isFalse();
    }

    @Test
    void shouldReturnTypedFuture() {
        var future = newFailingCompletableFutureWithStringType();

        var ex = new AsyncException("oop", future);

        CompletableFuture<String> typedFuture = ex.getFuture();

        assertThat(typedFuture)
                .hasFailedWithThrowableThat()
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("this didn't work!");
    }

    private static CompletableFuture<String> newFailingCompletableFutureWithStringType() {
        return CompletableFuture.failedFuture(new RuntimeException("this didn't work!"));
    }
}