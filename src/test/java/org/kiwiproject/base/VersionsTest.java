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
            "2.0.1, 1.6.12, 1",
            "1.1.1-SNAPSHOT, 1.1.1-SNAPSHOT, 0",
            "1.0.1, 1.1.1, -1",
            "1.1.10, 1.1.2, 1"
    })
    void testVersions_SlightlyComplex(String left, String right, int expectedResult) {
        assertThat(Versions.versionCompare(left, right)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0.1a-SNAPSHOT, 1.0.1a-SNAPSHOT, 0",
            "0.a.1a, 0.a.1a, 0",
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
            "5.4.1.Final, 5.4.1.Final, 0",
            "5.4.2.Final, 5.4.1.Final, 1",
            "5.5.2.Final, 5.4.1.Final, 1",
            "5.4.0.Final, 5.4.1.Final, -1",
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
            "1.0.0-beta, 1.0.0-Alpha, 1"
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