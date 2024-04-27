package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("KiwiIntegers")
public class KiwiIntegersTest {

    @ParameterizedTest(name = "Integer: {0}, expect: {1}")
    @CsvSource(textBlock = """
        1, 1
        42, 42
        -10, -10
        null, 0
        """, nullValues = "null")
    void toIntOrZero(Integer integerObject, int expectedResult) {
        assertThat(KiwiIntegers.toIntOrZero(integerObject))
                .isEqualTo(expectedResult);
    }

    @ParameterizedTest(name = "Integer: {0}, default: {1}, expect: {2}")
    @CsvSource(textBlock = """
        10, 0, 10
        42, 11, 42
        -10, 5, -10
        null, 0, 0
        null, 42, 42
        """, nullValues = "null")
    void toIntOrDefault(Integer integerObject, int defaultValue, int expectedResult) {
        assertThat(KiwiIntegers.toIntOrDefault(integerObject, defaultValue))
                .isEqualTo(expectedResult);
    }
}
