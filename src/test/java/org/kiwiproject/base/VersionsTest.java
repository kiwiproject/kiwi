package org.kiwiproject.base;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Versions")
class VersionsTest {

    /**
     * @implNote Get null strings by putting nothing in the CSV
     */
    @ParameterizedTest
    @CsvSource({
            ",",
            ",1.0.0",
            "1.0.0,",
    })
    void testVersions_DoesNotPermitNullArguments(String left, String right) {
        checkState(isNull(left) || isNull(right));

        assertThatThrownBy(() -> Versions.versionCompare(left, right))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version cannot be blank");
    }

    /**
     * @implNote Get blank strings by using '' in the CSV
     */
    @ParameterizedTest
    @CsvSource({
            "'',''",
            "'',1.0.0",
            "1.0.0,''",
    })
    void testVersions_DoesNotPermitBlankArguments(String left, String right) {
        checkState(left.isEmpty() || right.isEmpty());

        assertThatThrownBy(() -> Versions.versionCompare(left, right))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version cannot be blank");
    }


    @ParameterizedTest
    @CsvSource({
            "1, 1, 0",
            "0, 1, -1",
            "1, 0, 1"
    })
    void testVersions_Simple(String left, String right, int expectedResult) {
        assertThat(Versions.versionCompare(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "2.0.1, 2.0.1, 0",
            "1.6.12, 1.6.12, 0",
            "2.0.1, 1.6.12, 1",
            "1.6.12, 2.0.1, -1",
            "1.1.1-SNAPSHOT, 1.1.1-SNAPSHOT, 0",
            "1.1.1-SNAPSHOT, 1.1.1-snapshot, 0",
            "1.1.1-snapshot, 1.1.1-SNAPSHOT, 0",
            "1.0.1, 1.1.1, -1",
            "1.1.10, 1.1.2, 1",
            "1.4.2, 1.4.2, 0",
            "1.4-2, 1.4.2, 0",
            "1.4.2, 1.4-2, 0",
            "2.0.0.0, 2.0.0.0, 0",
            "2.0.0.1, 2.0.0.0, 1",
            "2.0.0.0, 2.0.0.1, -1",
            "2.5.10.1, 2.5.10.1, 0",
            "2.5.10.2, 2.5.10.1, 1",
            "2.5.10.1, 2.5.10.2, -1",
    })
    void testVersions_SlightlyComplex(String left, String right, int expectedResult) {
        assertThat(Versions.versionCompare(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0.1a-SNAPSHOT, 1.0.1a-SNAPSHOT, 0",
            "0.a.1a, 0.a.1a, 0",
            "0.a-1a, 0.a.1a, 0",
            "0.a.1a, 0.a-1a, 0",
            "0.a.1a, 0.a.1b, -1",
            "0.a.1b, 0.a.1a, 1",
            "a.10.1, a.2.1, 1",
            "a.2.1, a.10.1, -1",
            "a.10.1, a.10.1, 0",
            "a.b.c, a.b.d, -1",
            "a.b.d, a.b.c, 1",
            "a.b.C, a.b.d, -1",
            "a.b.d, a.b.C, 1",
            "1.2.3.a, 1.2.b, -1",
            "1.2.b, 1.2.3.a, 1",
            "2.5.0.a, 2.5.0.a, 0",
            "2.5.0.b, 2.5.0.a, 1",
            "2.5.0.a, 2.5.0.b, -1",
            "5.4.1.FINAL, 5.4.1.final, 0",
            "5.4.1.Final, 5.4.1.Final, 0",
            "5.4.2.Final, 5.4.1.Final, 1",
            "5.5.2.Final, 5.4.1.Final, 1",
            "5.4.0.Final, 5.4.1.Final, -1",
            "6.0.0.Alpha7, 5.4.30.Final, 1",
            "5.4.30.Final, 6.0.0.Alpha7, -1",
            "1.0.0-alpha, 1.0.0-alpha, 0",
            "1.1.0-alpha, 1.0.0-alpha, 1",
            "1.0.0-alpha, 1.1.0-alpha, -1",
            "5.0.0-beta.0, 5.0.0-beta.0, 0",
            "5.0.0-beta.1, 5.0.0-beta.0, 1",
            "5.0.0-beta.0, 5.0.0-beta.1, -1",
            "1.0.0-alpha, 1.0.0-beta, -1",
            "1.0.0-beta, 1.0.0-alpha, 1",
            "1.0.0-Alpha, 1.0.0-Beta, -1",
            "1.0.0-Beta, 1.0.0-Alpha, 1",
            "1.0.0-alpha, 1.0.0-Beta, -1",
            "1.0.0-beta, 1.0.0-Alpha, 1",
            "2.0.0-alpha2, 2.0.0-alpha1, 1",
            "2.0.0-alpha1, 2.0.0-alpha2, -1",
            "2.0.0-alpha2, 2.0.0-alpha2, 0",
            "2.0.0-alpha.2, 2.0.0-alpha.1, 1",
            "2.0.0-alpha.1, 2.0.0-alpha.2, -1",
            "1.0.0-beta.2, 1.0.0-beta.1, 1",
            "1.0.0-beta.1, 2.0.0-beta.2, -1",
            "1.0.0-beta.2, 1.0.0-beta-1, 1",
            "1.0.0-beta-1, 2.0.0-beta-2, -1",
            "11.0.0-alpha0, 11.0.0-alpha0, 0",
            "11.0.0-alpha1, 11.0.0-alpha0, 1",
            "11.0.0-alpha0, 11.0.0-alpha1, -1",
            "11.0.0-beta0, 11.0.0-alpha3, 1",
            "11.0.0-beta2, 11.0.0-beta3, -1",
            "5.8.0-M2, 5.8.0-M1, 1",
            "5.8.0-M1, 5.8.0-M2, -1",
            "2.0.0-Alpha.2, 2.0.0-alpha.1, 1",
            "2.0.0-alpha.1, 2.0.0-Alpha.2, -1",
            "1.0.0, 1.0.0-alpha, 1",
            "1.0.0-alpha, 1.0.0, -1",
            "2.0.0, 2.0.0-beta, 1",
            "2.0.0-beta, 2.0.0, -1",
            "2.0.0-beta1, 2.0.0-beta, 1",
            "2.0.0-beta, 2.0.0-beta1, -1",
            "2.0.0-beta.1, 2.0.0.beta.0, 1",
            "2.0.0-beta.0, 2.0.0-beta.1, -1",
            "2.0.0-beta.b, 2.0.0-beta.a, 1",
            "2.0.0-beta.a, 2.0.0-beta.b, -1",
            "2.0.0, 2.0.0-beta.1, 1",
            "2.0.0-beta.1, 2.0.0, -1",
            "2.0.0-beta.3, 2.0.0-beta.3, 0",
            "2.0.0-beta.3, 2.0.0-beta-3, 0",
            "2.0.0-beta-3, 2.0.0-beta.3, 0",
            "1.0.0, 1.0.0-SNAPSHOT, 1",
            "1.0.0-SNAPSHOT, 1.0.0, -1",
            "5.8.0, 5.8.0-M1, 1",
            "5.8.0-M1, 5.8.0-M1, 0",
            "5.8.0-M1, 5.8.0, -1",
            "10.4.39.v20210325, 9.4.39.v20210325, 1",
            "9.4.39.v20210325, 10.4.39.v20210325, -1",
            "9.3.40.v20210325, 9.3.39.v20210325, 1",
            "9.3.39.v20210325, 9.3.40.v20210325, -1",
            "9.4.39.v20210325, 9.4.39.v20210325, 0",
            "9.4.39.v20210326, 9.4.39.v20210325, 1",
            "9.4.39.v20210325, 9.4.39.v20210326, -1",
            "42.2.19.jre7, 42.2.19.jre7, 0",
            "42.2.19, 42.2.19.jre7, 1",
            "42.2.19.jre7, 42.2.19, -1",
            "30.1.1-jre, 30.1.1-jre, 0",
            "30.1.2-jre, 30.1.1-jre, 1",
            "30.1.1-jre, 30.1.2-jre, -1",
            "30.2-jre, 30.1.1-jre, 1",
            "30.1.1-jre, 30.2-jre, -1",
            "2.2.11.RELEASE, 2.2.11.RELEASE, 0",
            "2.2.12.RELEASE, 2.2.11.RELEASE, 1",
            "2.2.11.RELEASE, 2.2.12.RELEASE, -1",
            "3.0.0, 2.2.12.RELEASE, 1",
            "2.2.12.RELEASE, 3.0.0, -1",
    })
    void testVersions_ComplexWithAlphanumerics(String left, String right, int expectedResult) {
        assertThat(Versions.versionCompare(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 1, 1",
            "2.0.0, 2.0, 1",
            "2.0, 2.0.0, -1",
            "2.0.0, 2.1, -1",
            "2.1, 2.0.42, 1",
            "2.0.42, 2.1, -1",
            "1.0.0.1, 1.0.0, 1",
            "1.0.0, 1.0.0.1, -1",
            "1.0.0.0, 1.0.0, 1",
            "1.0.0, 1.0.0.0, -1"
    })
    void testVersions_WithDifferingLengths(String left, String right, int expectedResult) {
        assertThat(Versions.versionCompare(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, 2",
            "1.0.0, 1.0.0, 1.0.0",
            "1.0.0, 1.1.0, 1.1.0",
            "1.2.0, 1.1.0, 1.2.0",
            "1.0.0-SNAPSHOT, 2.0.0-SNAPSHOT, 2.0.0-SNAPSHOT"
    })
    void testHigherVersion(String left, String right, String expectedVersion) {
        assertThat(Versions.higherVersion(left, right)).isEqualTo(expectedVersion);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, true",
            "1.0.0, 1.0.0, false",
            "1.0.0, 1.1.0, false",
            "1.2.0, 1.1.0, true",
            "1.0.0-SNAPSHOT, 2.0.0-SNAPSHOT, false"
    })
    void testIsStrictlyHigherVersion(String left, String right, boolean expectedResult) {
        assertThat(Versions.isStrictlyHigherVersion(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, true",
            "1.0.0, 1.0.0, true",
            "1.0.0, 1.1.0, false",
            "1.2.0, 1.1.0, true",
            "1.0.0-SNAPSHOT, 2.0.0-SNAPSHOT, false"
    })
    void testIsHigherOrSameVersion(String left, String right, boolean expectedResult) {
        assertThat(Versions.isHigherOrSameVersion(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, false",
            "1.0.0, 1.0.0, false",
            "1.0.0, 1.1.0, true",
            "1.2.0, 1.1.0, false",
            "1.0.0-SNAPSHOT, 2.0.0-SNAPSHOT, true"
    })
    void testIsStrictlyLowerVersion(String left, String right, boolean expectedResult) {
        assertThat(Versions.isStrictlyLowerVersion(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, false",
            "1.0.0, 1.0.0, true",
            "1.0.0, 1.1.0, true",
            "1.2.0, 1.1.0, false",
            "1.0.0-SNAPSHOT, 2.0.0-SNAPSHOT, true"
    })
    void testIsLowerOrSameVersion(String left, String right, boolean expectedResult) {
        assertThat(Versions.isLowerOrSameVersion(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1, false",
            "1.0.0, 1.0.0, true",
            "1.0.0, 1.1.0, false",
            "1.2.0, 1.1.0, false",
            "1.0.0-SNAPSHOT, 2.0.0-SNAPSHOT, false"
    })
    void testIsSameVersion(String left, String right, boolean expectedResult) {
        assertThat(Versions.isSameVersion(left, right)).isEqualTo(expectedResult);
    }
}