package org.kiwiproject.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PaginatedResult")
class PaginatedResultTest {

    @ParameterizedTest(name = "[{index}] totalCount={0}, pageSize={1} -> expectedPages={2}")
    @CsvSource(textBlock = """
            0,1,0
            0,10,0
            1,1,1
            1,10,1
            9,10,1
            10,10,1
            11,10,2
            99,10,10
            100,10,10
            101,10,11
            25,7,4
            24,7,4
            22,7,4
            21,7,3
            20,7,3
            15,7,3
            14,7,2
            200,33,7
            199,33,7
            198,33,6
            5,2,3
            2,5,1
            500000,50000,10
            500000,50,10000
            500001,50,10001
            """)
    void shouldCalculateTotalPages(long totalCount, int pageSize, int expectedPages) {
        var result = newPaginatedResult(totalCount, pageSize);
        assertThat(result.getTotalPages()).isEqualTo(expectedPages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -5, Integer.MIN_VALUE })
    void shouldThrowIllegalArgumentException_WhenPageSizeIsInvalid(int pageSize) {
        var result = newPaginatedResult(100, pageSize);

        assertThatThrownBy(result::getTotalPages)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pageSize"); // adjust if KiwiSearching uses different wording
    }

    private static PaginatedResult newPaginatedResult(long totalCount, int pageSize) {
        return new PaginatedResult() {
            @Override
            public long getTotalCount() {
                return totalCount;
            }

            @Override
            public int getPageNumber() {
                return 1;  // not relevant for getTotalPages() tests
            }

            @Override
            public int getPageSize() {
                return pageSize;
            }
        };
    }
}
