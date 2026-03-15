package org.kiwiproject.dropwizard.lifecycle;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.io.IOException;

@DisplayName("ManagedCloseable")
class ManagedCloseableTest {

    private Closeable closeable;

    @BeforeEach
    void setUp() {
        closeable = mock(Closeable.class);
    }

    @Nested
    class Constructor {

        @Test
        void shouldRequireNonNullCloseable() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ManagedCloseable(null));
        }

        @Test
        void shouldNotInteractWithCloseable_WhenStartIsCalled() {
            var managed = new ManagedCloseable(closeable);
            managed.start();
            verifyNoInteractions(closeable);
        }

        @Test
        void shouldCloseCloseable_WhenStopIsCalled() throws Exception {
            var managed = new ManagedCloseable(closeable);
            managed.stop();
            verify(closeable).close();
        }

        @Test
        void shouldPropagateIOException_WhenStopIsCalled_AndCloseThrows() throws Exception {
            doThrow(new IOException("close failed")).when(closeable).close();
            var managed = new ManagedCloseable(closeable);

            assertThatThrownBy(managed::stop)
                    .isInstanceOf(IOException.class)
                    .hasMessage("close failed");
        }
    }

    @Nested
    class Of {

        @Test
        void shouldRequireNonNullCloseable() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ManagedCloseable.of(null));
        }

        @Test
        void shouldCloseCloseable_WhenStopIsCalled() throws Exception {
            var managed = ManagedCloseable.of(closeable);
            managed.stop();
            verify(closeable).close();
        }

        @Test
        void shouldPropagateIOException_WhenStopIsCalled_AndCloseThrows() throws Exception {
            doThrow(new IOException("close failed")).when(closeable).close();
            var managed = ManagedCloseable.of(closeable);

            assertThatThrownBy(managed::stop)
                    .isInstanceOf(IOException.class)
                    .hasMessage("close failed");
        }
    }

    @Nested
    class ClosingQuietly {

        @Test
        void shouldRequireNonNullCloseable() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ManagedCloseable.closingQuietly(null));
        }

        @Test
        void shouldCloseCloseable_WhenStopIsCalled() throws Exception {
            var managed = ManagedCloseable.closingQuietly(closeable);
            managed.stop();
            verify(closeable).close();
        }

        @Test
        void shouldNotPropagateException_WhenStopIsCalled_AndCloseThrows() {
            Closeable throwingCloseable = () -> { throw new IOException("close failed"); };
            var managed = ManagedCloseable.closingQuietly(throwingCloseable);

            assertThatCode(managed::stop).doesNotThrowAnyException();
        }
    }
}
