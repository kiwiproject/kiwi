package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

@DisplayName("KiwiBigDecimals")
class KiwiBigDecimalsTest {

    @Test
    void shouldConvertToOptionalDouble() {
        assertAll(
                () -> assertThat(KiwiBigDecimals.toOptionalDouble(null)).isEmpty(),
                () -> assertThat(KiwiBigDecimals.toOptionalDouble(BigDecimal.ZERO)).hasValue(0.0),
                () -> assertThat(KiwiBigDecimals.toOptionalDouble(BigDecimal.ONE)).hasValue(1.0),
                () -> assertThat(KiwiBigDecimals.toOptionalDouble(BigDecimal.TEN)).hasValue(10.0),
                () -> assertThat(KiwiBigDecimals.toOptionalDouble(new BigDecimal("420.4242"))).hasValue(420.4242)
        );
    }

    @Test
    void shouldConvertToDoubleOrReturnEmptyOptional() {
        assertAll(
                () -> assertThat(KiwiBigDecimals.toOptionalDoubleObject(null)).isEmpty(),
                () -> assertThat(KiwiBigDecimals.toOptionalDoubleObject(BigDecimal.ZERO)).hasValue(0.0),
                () -> assertThat(KiwiBigDecimals.toOptionalDoubleObject(BigDecimal.ONE)).hasValue(1.0),
                () -> assertThat(KiwiBigDecimals.toOptionalDoubleObject(BigDecimal.TEN)).hasValue(10.0),
                () -> assertThat(KiwiBigDecimals.toOptionalDoubleObject(new BigDecimal("420.4242"))).hasValue(420.4242)
        );
    }

    @Test
    void shouldConvertToDoubleOrNull() {
        assertAll(
                () -> assertThat(KiwiBigDecimals.toDoubleOrNull(null)).isNull(),
                () -> assertThat(KiwiBigDecimals.toDoubleOrNull(BigDecimal.ZERO)).isZero(),
                () -> assertThat(KiwiBigDecimals.toDoubleOrNull(BigDecimal.ONE)).isEqualTo(1.0),
                () -> assertThat(KiwiBigDecimals.toDoubleOrNull(BigDecimal.TEN)).isEqualTo(10.0),
                () -> assertThat(KiwiBigDecimals.toDoubleOrNull(new BigDecimal("42000.84"))).isEqualTo(42_000.84)
        );
    }

    @Test
    void shouldRequireDoublePrimitive() {
        assertAll(
                //noinspection DataFlowIssue
                () -> assertThatThrownBy(() -> KiwiBigDecimals.requireDouble(null))
                        .isExactlyInstanceOf(IllegalArgumentException.class),
                () -> assertThat(KiwiBigDecimals.requireDouble(BigDecimal.ZERO)).isZero(),
                () -> assertThat(KiwiBigDecimals.requireDouble(BigDecimal.ONE)).isEqualTo(1.0),
                () -> assertThat(KiwiBigDecimals.requireDouble(BigDecimal.TEN)).isEqualTo(10.0)
        );
    }

    @Test
    void shouldRequireDoublePrimitiveWithCustomMessage() {
        assertAll(
                //noinspection DataFlowIssue
                () -> assertThatThrownBy(() -> KiwiBigDecimals.requireDouble(null, "price cannot be null"))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("price cannot be null"),
                () -> assertThat(KiwiBigDecimals.requireDouble(BigDecimal.ZERO, "price cannot be null")).isZero(),
                () -> assertThat(KiwiBigDecimals.requireDouble(BigDecimal.ONE, "price cannot be null")).isEqualTo(1.0),
                () -> assertThat(KiwiBigDecimals.requireDouble(BigDecimal.TEN, "price cannot be null")).isEqualTo(10.0)
        );
    }
}
