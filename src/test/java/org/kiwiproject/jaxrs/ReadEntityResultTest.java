package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReadEntityResult")
class ReadEntityResultTest {

    @Nested
    class Construction {

        @Test
        void shouldAllowNullEntity() {
            var result = new ReadEntityResult<>(null, new RuntimeException("oops"));

            assertAll(
                    () -> assertThat(result.entity()).isNull(),
                    () -> assertThat(result.exception()).isNotNull()
            );
        }

        @Test
        void shouldAllowNullException() {
            var result = new ReadEntityResult<>("the entity", null);

            assertAll(
                    () -> assertThat(result.entity()).isEqualTo("the entity"),
                    () -> assertThat(result.exception()).isNull()
            );
        }

        @Test
        void shouldAllowBothNull_WhenResponseHadNoBody() {
            var result = new ReadEntityResult<>(null, null);

            assertAll(
                    () -> assertThat(result.entity()).isNull(),
                    () -> assertThat(result.exception()).isNull()
            );
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenBothAreNonNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new ReadEntityResult<>("the entity", new RuntimeException("oops")));
        }
    }

    @Nested
    class HasEntity {

        @Test
        void shouldReturnTrue_WhenEntityIsPresent() {
            var result = new ReadEntityResult<>("the entity", null);
            assertThat(result.hasEntity()).isTrue();
        }

        @Test
        void shouldReturnTrue_WhenBothAreNull_SuchAsA204Response() {
            var result = new ReadEntityResult<>(null, null);
            assertThat(result.hasEntity()).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenExceptionIsPresent() {
            var result = new ReadEntityResult<>(null, new RuntimeException("oops"));
            assertThat(result.hasEntity()).isFalse();
        }
    }

    @Nested
    class HasException {

        @Test
        void shouldReturnTrue_WhenExceptionIsPresent() {
            var result = new ReadEntityResult<>(null, new RuntimeException("oops"));
            assertThat(result.hasException()).isTrue();
        }

        @Test
        void shouldReturnFalse_WhenEntityIsPresent() {
            var result = new ReadEntityResult<>("the entity", null);
            assertThat(result.hasException()).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenBothAreNull_SuchAsA204Response() {
            var result = new ReadEntityResult<>(null, null);
            assertThat(result.hasException()).isFalse();
        }
    }
}
