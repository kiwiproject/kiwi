package org.kiwiproject.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Stream;

@DisplayName("InternalKiwiValidators")
class InternalKiwiValidatorsTest {

    @ParameterizedTest
    @CsvSource({
            "'', 42",
            "' ', 42",
            ", 84"
    })
    void shouldReturnNull_WhenBlankCompareValue(String compareValue, Comparable<?> value) {
        assertThat(InternalKiwiValidators.toComparableOrNull(compareValue, value)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "12,",
            "42,",
            "abcd,"
    })
    void shouldReturnNull_WhenNullValue(String compareValue, Comparable<?> value) {
        assertThat(InternalKiwiValidators.toComparableOrNull(compareValue, value)).isNull();
    }

    @ParameterizedTest
    @MethodSource("unsupportedTypeArguments")
    void shouldThrow_WhenGivenUnsupportedType(String compareValue, Comparable<?> value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> InternalKiwiValidators.toComparableOrNull(compareValue, value))
                .withMessage("This validator does not support validating objects of type: " + value.getClass().getName());
    }

    private static Stream<Arguments> unsupportedTypeArguments() {
        return Stream.of(
                Arguments.of("1234", ZonedDateTime.now()),
                Arguments.of("foo", new Person("Bob", "Smith")),
                Arguments.of("true", Boolean.TRUE),
                Arguments.of("bar", Path.of("/data", "src", "my_project"))
        );
    }

    @ParameterizedTest
    @MethodSource("conversionArguments")
    void shouldConvertToComparableOfSameType(String compareValue,
                                             Comparable<?> value,
                                             Class<?> expectedType,
                                             Object expectedResult) {

        var comparable = InternalKiwiValidators.toComparableOrNull(compareValue, value);
        assertThat(comparable)
                .isExactlyInstanceOf(expectedType)
                .isEqualTo(expectedResult);
    }

    private static Stream<Arguments> conversionArguments() {
        return Stream.of(
                Arguments.of("42.84", 60.0, Double.class, 42.84),
                Arguments.of("24.567", 50.0F, Float.class, 24.567F),
                Arguments.of("64", (byte) 127, Byte.class, (byte) 64),
                Arguments.of("1024", (short) 32_767, Short.class, (short) 1024),
                Arguments.of("255000", 500_000, Integer.class, 255_000),
                Arguments.of("3000000000", 8_000_000_000L, Long.class, 3_000_000_000L),
                Arguments.of("10000", new BigDecimal("200000.0"), BigDecimal.class, new BigDecimal("10000")),
                Arguments.of("100", new BigInteger("10000"), BigInteger.class, new BigInteger("100")),
                Arguments.of("1598850000000", Date.from(Instant.now()), Date.class, new Date(1598850000000L)),
                Arguments.of("1598850000000", Instant.now(), Instant.class, Instant.ofEpochMilli(1598850000000L)),
                Arguments.of("{ \"firstName\": \"Alice\", \"lastName\": \"Jones\" }",
                        new Person("Alice", "Smith"), Person.class, new Person("Alice", "Jones"))
        );
    }

    @Value
    private static class Person implements Comparable<Person> {

        String firstName;
        String lastName;

        String getNormalizedName() {
            return f("{}, {}", lastName, firstName);
        }

        @Override
        public int compareTo(Person other) {
            return this.getNormalizedName().compareTo(other.getNormalizedName());
        }
    }
}
