package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("KiwiSort")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiSortTest {

    @Test
    void shouldCreateNewInstanceUsingFactoryMethod(SoftAssertions softly) {
        var sort = KiwiSort.of("someProperty", KiwiSort.Direction.DESC);

        softly.assertThat(sort.getProperty()).isEqualTo("someProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("DESC");
        softly.assertThat(sort.isAscending()).isFalse();
        softly.assertThat(sort.isIgnoreCase()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            " , ASC",
            " '' , ASC",
            " ' ' , DESC",
            " lastName, ",
    })
    void shouldValidateArgumentsWhenUsingFactoryMethod(String property, KiwiSort.Direction direction) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSort.of(property, direction));
    }

    @Test
    void shouldSetIgnoringCase(SoftAssertions softly) {
        var sort = KiwiSort.of("otherProperty", KiwiSort.Direction.ASC).ignoringCase();

        softly.assertThat(sort.getProperty()).isEqualTo("otherProperty");
        softly.assertThat(sort.getDirection()).isEqualTo("ASC");
        softly.assertThat(sort.isAscending()).isTrue();
        softly.assertThat(sort.isIgnoreCase()).isTrue();
    }

    @Test
    void shouldCallToString() {
        var sort = KiwiSort.of("someProperty", KiwiSort.Direction.DESC);
        var str = sort.toString();
        assertThat(str).isNotBlank();
    }
}
