package org.kiwiproject.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.search.KiwiSearching.PAGE_SIZE_ERROR;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.search.KiwiSearching.PageNumberingScheme;

@DisplayName("KiwiSearching")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiSearchingTest {

    @Test
    void shouldNotChangeDefaultPageSizeConstantsUnlessBothAreChanged() {
        assertThat(KiwiSearching.DEFAULT_PAGE_SIZE_AS_STRING)
                .isEqualTo(String.valueOf(KiwiSearching.DEFAULT_PAGE_SIZE));
    }

    @Nested
    class CheckPageSize {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 10, Integer.MAX_VALUE})
        void shouldNotThrowException_WhenValidPageSize(int pageSize) {
            assertThatCode(() -> KiwiSearching.checkPageSize(pageSize))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(ints = {Integer.MIN_VALUE, -10, -1, 0})
        void shouldThrowException_WhenInvalidPageSize(int pageSize) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSearching.checkPageSize(pageSize))
                    .withMessage(PAGE_SIZE_ERROR);
        }
    }

    @Nested
    class CheckPageNumber {

        @Nested
        class UsingOneBasedNumberingScheme {

            @ParameterizedTest
            @ValueSource(ints = {1, 2, 10, 100, Integer.MAX_VALUE})
            void shouldNotThrowException_WhenValidPageNumber(int pageNumber) {
                assertThatCode(() -> KiwiSearching.checkPageNumber(pageNumber))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @ValueSource(ints = {Integer.MIN_VALUE, -1000, -100, -1, 0})
            void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.checkPageNumber(pageNumber))
                        .withMessage("pageNumber starts at 1");
            }
        }

        @Nested
        class UsingZeroBasedNumberingScheme {

            @ParameterizedTest
            @ValueSource(ints = {0, 1, 2, 10, 100, Integer.MAX_VALUE})
            void shouldNotThrowException_WhenValidPageNumber(int pageNumber) {
                assertThatCode(() -> KiwiSearching.checkPageNumber(pageNumber, PageNumberingScheme.ZERO_BASED))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @ValueSource(ints = {Integer.MIN_VALUE, -1000, -100, -1})
            void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.checkPageNumber(pageNumber, PageNumberingScheme.ZERO_BASED))
                        .withMessage("pageNumber starts at 0");
            }
        }
    }

    @Nested
    class ZeroBasedOffset {

        @Nested
        class UsingOneBasedNumberingScheme {

            @Test
            void shouldReturnExpectedZeroBasedOffset(SoftAssertions softly) {
                softly.assertThat(KiwiSearching.zeroBasedOffset(1, 25)).isEqualTo(0);
                softly.assertThat(KiwiSearching.zeroBasedOffset(10, 25)).isEqualTo(9 * 25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(100, 25)).isEqualTo(99 * 25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(1_000, 25)).isEqualTo(999 * 25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(1_000, 20)).isEqualTo(999 * 20);
            }

            @ParameterizedTest
            @ValueSource(ints = {-10, -1, 0})
            void shouldThrowException_WhenInvalidPageSize(int pageSize) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.zeroBasedOffset(1, pageSize));
            }

            @ParameterizedTest
            @ValueSource(ints = {-100, -2, -1, 0})
            void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.zeroBasedOffset(pageNumber, 10))
                        .withMessage(PageNumberingScheme.ONE_BASED.getPageNumberError());
            }
        }

        @Nested
        class UsingZeroBasedNumberingScheme {

            @Test
            void shouldReturnExpectedZeroBasedOffset(SoftAssertions softly) {
                softly.assertThat(KiwiSearching.zeroBasedOffset(0, PageNumberingScheme.ZERO_BASED, 25)).isEqualTo(0);
                softly.assertThat(KiwiSearching.zeroBasedOffset(1, PageNumberingScheme.ZERO_BASED, 25)).isEqualTo(25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(10, PageNumberingScheme.ZERO_BASED, 25)).isEqualTo(10 * 25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(100, PageNumberingScheme.ZERO_BASED, 25)).isEqualTo(100 * 25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(1_000, PageNumberingScheme.ZERO_BASED, 25)).isEqualTo(1_000 * 25);
                softly.assertThat(KiwiSearching.zeroBasedOffset(1_000, PageNumberingScheme.ZERO_BASED, 20)).isEqualTo(1_000 * 20);
            }

            @ParameterizedTest
            @ValueSource(ints = {-10, -1, 0})
            void shouldThrowException_WhenInvalidPageSize(int pageSize) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.zeroBasedOffset(1, PageNumberingScheme.ZERO_BASED, pageSize));
            }

            @ParameterizedTest
            @ValueSource(ints = {-100, -2, -1})
            void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.zeroBasedOffset(pageNumber, PageNumberingScheme.ZERO_BASED, 10))
                        .withMessage(PageNumberingScheme.ZERO_BASED.getPageNumberError());
            }
        }
    }

    @Nested
    class ZeroBasedOffsetForOneBasedPaging {

        @Test
        void shouldReturnExpectedZeroBasedOffset(SoftAssertions softly) {
            softly.assertThat(KiwiSearching.zeroBasedOffsetForOneBasedPaging(1, 25)).isEqualTo(0);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForOneBasedPaging(10, 25)).isEqualTo(9 * 25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForOneBasedPaging(100, 25)).isEqualTo(99 * 25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForOneBasedPaging(1_000, 25)).isEqualTo(999 * 25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForOneBasedPaging(1_000, 20)).isEqualTo(999 * 20);
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 0})
        void shouldThrowException_WhenInvalidPageSize(int pageSize) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSearching.zeroBasedOffsetForOneBasedPaging(1, pageSize));
        }

        @ParameterizedTest
        @ValueSource(ints = {-100, -2, -1, 0})
        void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSearching.zeroBasedOffsetForOneBasedPaging(pageNumber, 10))
                    .withMessage(PageNumberingScheme.ONE_BASED.getPageNumberError());
        }
    }

    @Nested
    class ZeroBasedOffsetForZeroBasedPaging {

        @Test
        void shouldReturnExpectedZeroBasedOffset(SoftAssertions softly) {
            softly.assertThat(KiwiSearching.zeroBasedOffsetForZeroBasedPaging(0, 25)).isEqualTo(0);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForZeroBasedPaging(1, 25)).isEqualTo(25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForZeroBasedPaging(10, 25)).isEqualTo(10 * 25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForZeroBasedPaging(100, 25)).isEqualTo(100 * 25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForZeroBasedPaging(1_000, 25)).isEqualTo(1_000 * 25);
            softly.assertThat(KiwiSearching.zeroBasedOffsetForZeroBasedPaging(1_000, 20)).isEqualTo(1_000 * 20);
        }

        @ParameterizedTest
        @ValueSource(ints = { -10, -1, 0 })
        void shouldThrowException_WhenInvalidPageSize(int pageSize) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSearching.zeroBasedOffsetForZeroBasedPaging(1, pageSize));
        }

        @ParameterizedTest
        @ValueSource(ints = { -100, -2, -1 })
        void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSearching.zeroBasedOffsetForZeroBasedPaging(pageNumber, 10))
                    .withMessage(PageNumberingScheme.ZERO_BASED.getPageNumberError());
        }
    }

    @Nested
    class NumberOfPages {

        @Test
        void shouldReturnExpectedNumberOfPages(SoftAssertions softly) {
            softly.assertThat(KiwiSearching.numberOfPages(0, 20)).isEqualTo(0);
            softly.assertThat(KiwiSearching.numberOfPages(7, 20)).isEqualTo(1);
            softly.assertThat(KiwiSearching.numberOfPages(10, 10)).isEqualTo(1);
            softly.assertThat(KiwiSearching.numberOfPages(100, 20)).isEqualTo(5);
            softly.assertThat(KiwiSearching.numberOfPages(101, 20)).isEqualTo(6);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 0})
        void shouldThrowException_WhenInvalidPageSize(int pageSize) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiSearching.numberOfPages(100, pageSize))
                    .withMessage(PAGE_SIZE_ERROR);
        }
    }

    @Nested
    class NumberOnPage {

        @Nested
        class UsingOneBasedNumberingScheme {

            @Test
            void shouldReturnExpectedNumberOnPage(SoftAssertions softly) {
                softly.assertThat(KiwiSearching.numberOnPage(0, 20, 1)).isEqualTo(0);

                softly.assertThat(KiwiSearching.numberOnPage(7, 20, 1)).isEqualTo(7);
                softly.assertThat(KiwiSearching.numberOnPage(7, 20, 2)).isEqualTo(0);

                softly.assertThat(KiwiSearching.numberOnPage(19, 20, 1)).isEqualTo(19);
                softly.assertThat(KiwiSearching.numberOnPage(20, 20, 1)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(21, 20, 1)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(21, 20, 2)).isEqualTo(1);

                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 1)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 2)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 3)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 4)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 5)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 6)).isEqualTo(1);
            }

            @ParameterizedTest
            @ValueSource(ints = {-100, -2, -1, 0})
            void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.numberOnPage(100, 20, pageNumber))
                        .withMessage(PageNumberingScheme.ONE_BASED.pageNumberError);
            }

            @ParameterizedTest
            @ValueSource(ints = {-10, -1, 0})
            void shouldThrowException_WhenInvalidPageSize(int pageSize) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.numberOnPage(100, pageSize, 1))
                        .withMessage(PAGE_SIZE_ERROR);
            }
        }

        @Nested
        class UsingZeroBasedNumberingScheme {

            @Test
            void shouldReturnExpectedNumberOnPage(SoftAssertions softly) {
                softly.assertThat(KiwiSearching.numberOnPage(0, 20, 0, PageNumberingScheme.ZERO_BASED)).isEqualTo(0);

                softly.assertThat(KiwiSearching.numberOnPage(7, 20, 0, PageNumberingScheme.ZERO_BASED)).isEqualTo(7);
                softly.assertThat(KiwiSearching.numberOnPage(7, 20, 1, PageNumberingScheme.ZERO_BASED)).isEqualTo(0);

                softly.assertThat(KiwiSearching.numberOnPage(19, 20, 0, PageNumberingScheme.ZERO_BASED)).isEqualTo(19);
                softly.assertThat(KiwiSearching.numberOnPage(20, 20, 0, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(21, 20, 0, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(21, 20, 1, PageNumberingScheme.ZERO_BASED)).isEqualTo(1);

                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 0, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 1, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 2, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 3, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 4, PageNumberingScheme.ZERO_BASED)).isEqualTo(20);
                softly.assertThat(KiwiSearching.numberOnPage(101, 20, 5, PageNumberingScheme.ZERO_BASED)).isEqualTo(1);
            }

            @ParameterizedTest
            @ValueSource(ints = {-100, -2, -1, 0})
            void shouldThrowException_WhenInvalidPageNumber(int pageNumber) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.numberOnPage(100, 20, pageNumber))
                        .withMessage(PageNumberingScheme.ONE_BASED.pageNumberError);
            }

            @ParameterizedTest
            @ValueSource(ints = {-10, -1, 0})
            void shouldThrowException_WhenInvalidPageSize(int pageSize) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiSearching.numberOnPage(100, pageSize, 1))
                        .withMessage(PAGE_SIZE_ERROR);
            }
        }
    }

    @Nested
    class PageNumberingSchemeEnum {

        @Test
        void shouldHaveZeroAsMinimumPage_ForZeroBasedNumbering() {
            assertThat(PageNumberingScheme.ZERO_BASED.minimumPageNumber).isZero();
            assertThat(PageNumberingScheme.ZERO_BASED.getMinimumPageNumber()).isZero();
        }

        @Test
        void shouldHaveOneAsMinimumPage_ForOneBasedNumbering() {
            assertThat(PageNumberingScheme.ONE_BASED.minimumPageNumber).isOne();
            assertThat(PageNumberingScheme.ONE_BASED.getMinimumPageNumber()).isOne();
        }
    }
}
