package org.kiwiproject.base;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class KiwiPrimitivesTest {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testFirstNonZero_Int(SoftAssertions softly) {
        softly.assertThat(KiwiPrimitives.firstNonZero(42, 84)).isEqualTo(42);
        softly.assertThat(KiwiPrimitives.firstNonZero(0, 42)).isEqualTo(42);
        softly.assertThatThrownBy(() -> KiwiPrimitives.firstNonZero(0, 0))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testFirstNonZero_Long(SoftAssertions softly) {
        softly.assertThat(KiwiPrimitives.firstNonZero(42L, 84L)).isEqualTo(42L);
        softly.assertThat(KiwiPrimitives.firstNonZero(0L, 42L)).isEqualTo(42L);
        softly.assertThatThrownBy(() -> KiwiPrimitives.firstNonZero(0L, 0L))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}