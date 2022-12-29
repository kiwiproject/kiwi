package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kiwiproject.search.KiwiSearching.PageNumberingScheme;

@DisplayName("PagingParams")
class PagingParamsTest {

    private PagingParams pagingParams;

    @BeforeEach
    void setUp() {
        pagingParams = new PagingRequest();
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "1, ONE_BASED, 10, true",
                    "0, ZERO_BASED, 15, true",
                    "null, null, null, false",
                    "null, ZERO_BASED, 25, false",
                    "0, null, 15, false",
                    "0, ZERO_BASED, null, false",
                    "null, null, 10, false",
                    "null, ONE_BASED, null, false",
                    "1, null, null, false",
                    "1, null, 50, false",
            },
            nullValues = "null"
    )
    void shouldHavePaginationProperties(
            Integer page, PageNumberingScheme numbering, Integer limit, boolean isExpectedToHavePaginationProperties) {

        pagingParams.setPage(page);
        pagingParams.setNumbering(numbering);
        pagingParams.setLimit(limit);

        assertThat(pagingParams.hasPaginationProperties()).isEqualTo(isExpectedToHavePaginationProperties);
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
