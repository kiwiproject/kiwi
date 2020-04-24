package org.kiwiproject.base;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

@DisplayName("KiwiBigDecimals")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiBigDecimalsTest {

    @Test
    void shouldConvertToOptionalDouble(SoftAssertions softly) {
        softly.assertThat(KiwiBigDecimals.toOptionalDouble(null)).isEmpty();
        softly.assertThat(KiwiBigDecimals.toOptionalDouble(BigDecimal.ZERO)).hasValue(0.0);
        softly.assertThat(KiwiBigDecimals.toOptionalDouble(BigDecimal.ONE)).hasValue(1.0);
        softly.assertThat(KiwiBigDecimals.toOptionalDouble(BigDecimal.TEN)).hasValue(10.0);
        softly.assertThat(KiwiBigDecimals.toOptionalDouble(new BigDecimal("420.4242"))).hasValue(420.4242);
    }

    @Test
    void shouldConvertToDoubleOrReturnEmptyOptional(SoftAssertions softly) {
        softly.assertThat(KiwiBigDecimals.toOptionalDoubleObject(null)).isEmpty();
        softly.assertThat(KiwiBigDecimals.toOptionalDoubleObject(BigDecimal.ZERO)).hasValue(0.0);
        softly.assertThat(KiwiBigDecimals.toOptionalDoubleObject(BigDecimal.ONE)).hasValue(1.0);
        softly.assertThat(KiwiBigDecimals.toOptionalDoubleObject(BigDecimal.TEN)).hasValue(10.0);
        softly.assertThat(KiwiBigDecimals.toOptionalDoubleObject(new BigDecimal("420.4242"))).hasValue(420.4242);
    }

    @Test
    void shouldConvertToDoubleOrNull(SoftAssertions softly) {
        softly.assertThat(KiwiBigDecimals.toDoubleOrNull(null)).isNull();
        softly.assertThat(KiwiBigDecimals.toDoubleOrNull(BigDecimal.ZERO)).isZero();
        softly.assertThat(KiwiBigDecimals.toDoubleOrNull(BigDecimal.ONE)).isEqualTo(1.0);
        softly.assertThat(KiwiBigDecimals.toDoubleOrNull(BigDecimal.TEN)).isEqualTo(10.0);
        softly.assertThat(KiwiBigDecimals.toDoubleOrNull(new BigDecimal("42000.84"))).isEqualTo(42_000.84);
    }

    @Test
    void shouldRequireDoublePrimitive(SoftAssertions softly) {
        //noinspection ConstantConditions
        softly.assertThatThrownBy(() -> KiwiBigDecimals.requireDouble(null))
                .isExactlyInstanceOf(IllegalArgumentException.class);
        softly.assertThat(KiwiBigDecimals.requireDouble(BigDecimal.ZERO)).isZero();
        softly.assertThat(KiwiBigDecimals.requireDouble(BigDecimal.ONE)).isEqualTo(1.0);
        softly.assertThat(KiwiBigDecimals.requireDouble(BigDecimal.TEN)).isEqualTo(10.0);
    }
}
