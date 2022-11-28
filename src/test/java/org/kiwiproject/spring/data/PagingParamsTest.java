package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("PagingParams")
class PagingParamsTest {

    private PagingParams pagingParams;

    @BeforeEach
    void setUp() {
        pagingParams = new PagingRequest();
    }

    @ParameterizedTest
    @CsvSource({
            "lastName, firstName, true",
            "lastName, , true",
            " , firstName, true",
            " , , false",
            " '', '', false",
    })
    void shouldHaveSort(String primarySort, String secondarySort, boolean isExpectedToHaveSort) {
        pagingParams.setPrimarySort(primarySort);
        pagingParams.setSecondarySort(secondarySort);
        assertThat(pagingParams.hasSort()).isEqualTo(isExpectedToHaveSort);
    }

    @ParameterizedTest
    @CsvSource({
            "lastName, true",
            " , false",
            " '', false",
            " ' ', false",
    })
    void shouldHavePrimarySort(String primarySort, boolean isExpectedToHavePrimarySort) {
        pagingParams.setPrimarySort(primarySort);
        assertThat(pagingParams.hasPrimarySort()).isEqualTo(isExpectedToHavePrimarySort);
    }

    @ParameterizedTest
    @CsvSource({
            "lastName, true",
            " , false",
            " '', false",
            " ' ', false",
    })
    void shouldHaveSecondarySort(String secondarySort, boolean isExpectedToHaveSecondarySort) {
        pagingParams.setSecondarySort(secondarySort);
        assertThat(pagingParams.hasSecondarySort()).isEqualTo(isExpectedToHaveSecondarySort);
    }
}
