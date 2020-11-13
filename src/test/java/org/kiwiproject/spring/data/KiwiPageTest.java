package org.kiwiproject.spring.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

@DisplayName("KiwiPage")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiPageTest {

    @Nested
    class FactoryMethod {

        @Test
        void shouldCreateNewInstance(SoftAssertions softly) {
            var contentList = List.of(1, 2, 3, 4, 5);
            var page = KiwiPage.of(0, 5, 102, contentList);

            softly.assertThat(page.getPagingStartsWith()).isZero();
            softly.assertThat(page.getContent()).isEqualTo(contentList);
            softly.assertThat(page.getSize()).isEqualTo(5);
            softly.assertThat(page.getNumber()).isZero();
            softly.assertThat(page.getNumberOfElements()).isEqualTo(contentList.size());
            softly.assertThat(page.getTotalPages()).isEqualTo(21);
            softly.assertThat(page.getTotalElements()).isEqualTo(102);
            softly.assertThat(page.getSort()).isNull();
            softly.assertThat(page.isSorted()).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
                "-1, 25, 100",
                "0, 0, 100",
                "0, -1, 100",
                "0, 25, -1"
        })
        void shouldValidateNumericArguments(long pageNum, long pageSizeLimit, long totalElements) {
            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiPage.of(pageNum, pageSizeLimit, totalElements, List.of(1, 2, 3)));
        }

        @Test
        void shouldNotAllowNullContentList() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiPage.of(0, 10, 125, null));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "10, 100, 10",
            "10, 101, 11",
            "25, 100, 4",
            "25, 101, 5",
            "10, 5, 1",
            "10, 10, 1",
    })
    void shouldCalculateTotalPages(long pageSizeLimit, long totalElements, long expectedTotalPages) {
        var page = KiwiPage.of(0, pageSizeLimit, totalElements, List.of(1, 2, 3));
        assertThat(page.getTotalPages()).isEqualTo(expectedTotalPages);
    }

    @Test
    void shouldAddKiwiSort(SoftAssertions softly) {
        var contentList = List.of(1, 2, 3, 4, 5);
        var page = KiwiPage.of(0, 5, 25, contentList)
                .addKiwiSort(KiwiSort.of("someProperty", KiwiSort.Direction.DESC));

        softly.assertThat(page.isSorted()).isTrue();
        softly.assertThat(page.getSort()).usingRecursiveComparison()
                .isEqualTo(KiwiSort.of("someProperty", KiwiSort.Direction.DESC));
    }

    @Nested
    class UsingZeroAsFirstPage {

        @Test
        void shouldReportFirstPageCorrectly(SoftAssertions softly) {
            var firstPage = KiwiPage.of(0, 5, 25, List.of(1, 2, 3, 4, 5))
                    .usingZeroAsFirstPage();

            softly.assertThat(firstPage.getPagingStartsWith()).isZero();
            softly.assertThat(firstPage.isFirst()).isTrue();
            softly.assertThat(firstPage.isLast()).isFalse();
        }

        @Test
        void shouldReportLastPageCorrectly(SoftAssertions softly) {
            var lastPage = KiwiPage.of(4, 5, 25, List.of(1, 2, 3, 4, 5))
                    .usingZeroAsFirstPage();

            softly.assertThat(lastPage.getPagingStartsWith()).isZero();
            softly.assertThat(lastPage.isFirst()).isFalse();
            softly.assertThat(lastPage.isLast()).isTrue();
        }

        @Test
        void shouldHandleWhenOnlyOnePage(SoftAssertions softly) {
            var onlyPage = KiwiPage.of(0, 15, 3, List.of(1, 2, 3))
                    .usingZeroAsFirstPage();

            softly.assertThat(onlyPage.getPagingStartsWith()).isZero();
            softly.assertThat(onlyPage.isFirst()).isTrue();
            softly.assertThat(onlyPage.isLast()).isTrue();
            softly.assertThat(onlyPage.getNumber()).isZero();
            softly.assertThat(onlyPage.getNumberOfElements()).isEqualTo(3);
        }
    }

    @Nested
    class UsingOneAsFirstPage {

        @Test
        void shouldReportFirstPageCorrectly(SoftAssertions softly) {
            var firstPage = KiwiPage.of(1, 5, 25, List.of(1, 2, 3, 4, 5))
                    .usingOneAsFirstPage();

            softly.assertThat(firstPage.getPagingStartsWith()).isOne();
            softly.assertThat(firstPage.isFirst()).isTrue();
            softly.assertThat(firstPage.isLast()).isFalse();
        }

        @Test
        void shouldReportLastPageCorrectly(SoftAssertions softly) {
            var lastPage = KiwiPage.of(5, 5, 25, List.of(1, 2, 3, 4, 5))
                    .usingOneAsFirstPage();

            softly.assertThat(lastPage.getPagingStartsWith()).isOne();
            softly.assertThat(lastPage.isFirst()).isFalse();
            softly.assertThat(lastPage.isLast()).isTrue();
        }

        @Test
        void shouldHandleWhenOnlyOnePage(SoftAssertions softly) {
            var onlyPage = KiwiPage.of(1, 10, 4, List.of(1, 2, 3, 4))
                    .usingOneAsFirstPage();

            softly.assertThat(onlyPage.getPagingStartsWith()).isOne();
            softly.assertThat(onlyPage.isFirst()).isTrue();
            softly.assertThat(onlyPage.isLast()).isTrue();
            softly.assertThat(onlyPage.getNumber()).isOne();
            softly.assertThat(onlyPage.getNumberOfElements()).isEqualTo(4);
        }
    }

    @Test
    void shouldCallToString() {
        var page = KiwiPage.of(3, 5, 1_000, List.of(1, 2, 3, 4, 5));
        var str = page.toString();
        assertThat(str).isNotBlank();
    }
}
