package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("KiwiBooleans")
class KiwiBooleansTest {

    @ParameterizedTest(name = "Boolean: {0}, expect: {1}")
    @CsvSource(textBlock = """
        TRUE, true,
        FALSE, false,
        null, true
        """, nullValues = "null")
    void toBooleanOrTrue(Boolean booleanObject, boolean expectedResult) {
        assertThat(KiwiBooleans.toBooleanOrTrue(booleanObject))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest(name = "Boolean: {0}, expect: {1}")
    @CsvSource(textBlock = """
        TRUE, true,
        FALSE, false,
        null, false
        """, nullValues = "null")
    void toBooleanOrFalse(Boolean booleanObject, boolean expectedResult) {
        assertThat(KiwiBooleans.toBooleanOrFalse(booleanObject))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest(name = "Boolean: {0}, default: {1}, expect: {2}")
    @CsvSource(textBlock = """
        TRUE, true, true
        TRUE, false, true
        FALSE, true, false
        FALSE, true, false
        null, true, true
        null, false, false
        """, nullValues = "null")
    void toBooleanOrDefault(Boolean booleanObject, boolean defaultValue, boolean expectedResult) {
        assertThat(KiwiBooleans.toBooleanOrDefault(booleanObject, defaultValue))
                .isEqualTo(expectedResult);
    }
}
