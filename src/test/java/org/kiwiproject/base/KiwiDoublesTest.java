package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;

@ExtendWith(SoftAssertionsExtension.class)
class KiwiDoublesTest {

    @ParameterizedTest
    @MethodSource("doublesCloseToZero")
    void isZero_WhenCloseToZero(double value) {
        assertThat(KiwiDoubles.isZero(value)).isFalse();
        assertThat(KiwiDoubles.isNotZero(value)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("doublesCloseToZero")
    void isCloseToZero_WhenCloseToZero_ButNotWithinTolerance(double value) {
        assertThat(KiwiDoubles.isCloseToZero(value)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("doublesCloseToZeroWithinDefaultTolerance")
    void isCloseToZero_WhenCloseToZero_AndWithinTolerance(double value) {
        assertThat(KiwiDoubles.isCloseToZero(value)).isTrue();
    }

    @Test
    void areEqual() {
        assertThat(KiwiDoubles.areEqual(0.0012345, 0.0012345)).isTrue();
        assertThat(KiwiDoubles.areNotEqual(0.0012345, 0.0012345)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("randomDoubles")
    void areEqual(double value) {
        assertThat(KiwiDoubles.areEqual(value, value)).isTrue();
        assertThat(KiwiDoubles.areNotEqual(value, value)).isFalse();
    }

    private static DoubleStream randomDoubles() {
        return DoubleStream.generate(() -> ThreadLocalRandom.current().nextDouble()).limit(100);
    }

    @Test
    void areClose_UsingDefaultTolerance(SoftAssertions softly) {
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.0012345)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.001234500000001)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123450000001)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.0012345000001)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.001234500001)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123450001)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.0012345001)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.001234501)).isFalse();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123451)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("randomDoubles")
    void areClose_UsingDefaultTolerance(double value) {
        double closeValue = value + 1E-10;
        double diff = value - closeValue;
        assertThat(KiwiDoubles.areClose(value, closeValue))
                .describedAs("value: %f ; otherValue: %f ; diff: %f", value, closeValue, diff)
                .isTrue();
    }

    @Test
    void areClose_UsingCustomTolerance(SoftAssertions softly) {
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.0012345001, 1E-10)).isFalse();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.0012345001, 1E-9)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123451, 1E-9)).isFalse();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123451, 1E-8)).isFalse();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123451, 1E-7)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123451, 1E-6)).isTrue();
        softly.assertThat(KiwiDoubles.areClose(0.0012345, 0.00123451, 1E-5)).isTrue();
    }

    private static DoubleStream doublesCloseToZero() {
        return DoubleStream.of(
                -1.0,
                -1E-1,
                -1E-2,
                -1E-3,
                -1E-4,
                -1E-5,
                -1E-6,
                -1E-7,
                -1E-8,
                1E-8,
                1E-7,
                1E-6,
                1E-5,
                1E-4,
                1E-3,
                1E-2,
                1E-1,
                1.0
        );
    }

    private static DoubleStream doublesCloseToZeroWithinDefaultTolerance() {
        return DoubleStream.of(
                -1E-9,
                -1E-10,
                -1E-11,
                1E-11,
                1E-10,
                1E-9
        );
    }
}