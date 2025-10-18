package org.kiwiproject.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.params.IntRangeSource;
import org.kiwiproject.json.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@DisplayName("SimplePaginatedResultPage")
class SimplePaginatedResultPageTest {

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    @Test
    void shouldCreateInstanceUsingConstructor() {
        var pageNumber = 1;
        var pageSize = 2;
        var totalCount = 10;
        var items = newContentOfSize(pageSize);
        var page = new SimplePaginatedResultPage<>(pageNumber, pageSize, totalCount, items);

        assertAll(
                () -> assertThat(page.getPageNumber()).isEqualTo(pageNumber),
                () -> assertThat(page.getPageSize()).isEqualTo(pageSize),
                () -> assertThat(page.getTotalCount()).isEqualTo(totalCount),
                () -> assertThat(page.getTotalPages()).isEqualTo(5),
                () -> assertThat(page.getContent()).isEqualTo(items)
        );
    }

    @Test
    void shouldCreateInstanceUsingBuilder() {
        var pageNumber = 1;
        var pageSize = 3;
        var totalCount = 21;
        var items = newContentOfSize(pageSize);
        var page = SimplePaginatedResultPage.<Item>builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalCount(totalCount)
                .content(items)
                .build();

        assertAll(
                () -> assertThat(page.getPageNumber()).isEqualTo(pageNumber),
                () -> assertThat(page.getPageSize()).isEqualTo(pageSize),
                () -> assertThat(page.getTotalCount()).isEqualTo(totalCount),
                () -> assertThat(page.getTotalPages()).isEqualTo(7),
                () -> assertThat(page.getContent()).isEqualTo(items)
        );
    }

    @Test
    void shouldCreatePageWithNoContent() {
        var pageNumber = 0;
        var pageSize = 20;
        var totalCount = 0;
        var page = new SimplePaginatedResultPage<Item>(pageNumber, pageSize, totalCount, List.of());

        assertAll(
                () -> assertThat(page.getPageNumber()).isEqualTo(pageNumber),
                () -> assertThat(page.getPageSize()).isEqualTo(pageSize),
                () -> assertThat(page.getTotalCount()).isEqualTo(totalCount),
                () -> assertThat(page.getTotalPages()).isZero(),
                () -> assertThat(page.getContent()).isEmpty()
        );
    }

    @Test
    void shouldAllowContentSizeEqualToPageSizeOnLastPage() {
        var pageSize = 5;
        var page = new SimplePaginatedResultPage<>(4, pageSize, 20, newContentOfSize(pageSize));
        assertThat(page.getContent()).hasSize(pageSize);
    }

    @Test
    void shouldDefensivelyCopyContent() {
        var mutable = new ArrayList<>(List.of(new Item("A", 1, 1.0)));
        var page = new SimplePaginatedResultPage<>(0, 10, 1, mutable);

        // mutating the original list after construction should not affect the content
        mutable.add(new Item("B", 1, 1.75));
        assertThat(page.getContent())
                .describedAs("page content should not be changed")
                .hasSize(1);

        // the content list should be unmodifiable
        var newItem = new Item("C", 1, 1.25);
        var content = page.getContent();

        //noinspection DataFlowIssue
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .describedAs("page content should not be modifiable")
                .isThrownBy(() -> content.add(newItem));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 10, 1
            50, 25, 2
            1000, 50, 20
            """)
    void shouldAllowPagesBeyondTheLastPage_UsingZeroBasedPageNumbering(int totalCount, int pageSize, int pageNumber) {
        var page = new SimplePaginatedResultPage<Item>(pageNumber, pageSize, totalCount, List.of());

        assertAll(
                () -> assertThat(page.getPageNumber()).isEqualTo(pageNumber),
                () -> assertThat(page.getPageSize()).isEqualTo(pageSize),
                () -> assertThat(page.getTotalCount()).isEqualTo(totalCount),
                () -> assertThat(page.getContent()).isEmpty(),
                () -> assertThat(page.getZeroBasedOffsetForZeroBasedPaging())
                        .isEqualTo(expectedOffsetForZeroBasedPaging(pageNumber, pageSize))
        );
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            10, 10, 2
            50, 25, 3
            1000, 50, 21
            """)
    void shouldAllowPagesBeyondTheLastPage_UsingOneBasedPageNumbering(int totalCount, int pageSize, int pageNumber) {
        var page = new SimplePaginatedResultPage<Item>(pageNumber, pageSize, totalCount, List.of());

        assertAll(
                () -> assertThat(page.getPageNumber()).isEqualTo(pageNumber),
                () -> assertThat(page.getPageSize()).isEqualTo(pageSize),
                () -> assertThat(page.getTotalCount()).isEqualTo(totalCount),
                () -> assertThat(page.getContent()).isEmpty(),
                () -> assertThat(page.getZeroBasedOffsetForOneBasedPaging())
                        .isEqualTo(expectedOffsetForOneBasedPaging(pageNumber, pageSize))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, -5, -1 })
    void shouldRequireZeroOrPositivePageNumber(int pageNumber) {
        assertThatIllegalStateException()
                .isThrownBy(() -> new SimplePaginatedResultPage<Item>(pageNumber, 10, 95, List.of()))
                .withMessage("pageNumber must be zero or positive");
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, -5, -1 })
    void shouldRequirePositivePageSize(int pageSize) {
        assertThatIllegalStateException()
                .isThrownBy(() -> new SimplePaginatedResultPage<Item>(0, pageSize, 95, List.of()))
                .withMessage("pageSize must be positive");
    }

    @ParameterizedTest
    @ValueSource(ints = { -10, -5, -1 })
    void shouldRequireZeroOrPositiveTotalCount(int totalCount) {
        assertThatIllegalStateException()
                .isThrownBy(() -> new SimplePaginatedResultPage<Item>(0, 10, totalCount, List.of()))
                .withMessage("totalCount must be zero or positive");
    }

    @Test
    void shouldRequireNonNullContent() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SimplePaginatedResultPage<Item>(0, 10, 50, null))
                .withMessage("content must not be null");
    }

    @Test
    void shouldRequireContentSize_LessThanOrEqualToPageSize() {
        var pageSize = 2;
        var content = newContentOfSize(pageSize + 1);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SimplePaginatedResultPage<>(0, pageSize, 10, content))
                .withMessage("content must not contain more elements than pageSize");
    }

    @ParameterizedTest(name = "zero-based pageNumber = {arguments}")
    @IntRangeSource(from = 0, to = 50)
    void shouldGetZeroBasedOffsetForZeroBasedPaging(int pageNumber) {
        var pageSize = 10;
        var page = SimplePaginatedResultPage.<Item>builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalCount(1_000)
                .content(newContentOfSize(pageSize))
                .build();

        assertThat(page.getZeroBasedOffsetForZeroBasedPaging())
                .isEqualTo(expectedOffsetForZeroBasedPaging(pageNumber, pageSize));
    }

    private static long expectedOffsetForZeroBasedPaging(long pageNumber, int pageSize) {
        return pageNumber * pageSize;
    }

    @ParameterizedTest(name = "one-based pageNumber = {arguments}")
    @IntRangeSource(from = 1, to = 50, closed = true)
    void shouldGetOneBasedOffsetForOneBasedPaging(int pageNumber) {
        var pageSize = 10;
        var page = SimplePaginatedResultPage.<Item>builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalCount(1_000)
                .content(newContentOfSize(pageSize))
                .build();

        assertThat(page.getZeroBasedOffsetForOneBasedPaging())
                .isEqualTo(expectedOffsetForOneBasedPaging(pageNumber, pageSize));
    }

    private static long expectedOffsetForOneBasedPaging(int pageNumber, int pageSize) {
        return (long) (pageNumber - 1) * pageSize;
    }

    @Test
    void shouldNotIncludeContentInStringRepresentation() {
        var pageSize = 10;
        var page = new SimplePaginatedResultPage<>(
                1,
                pageSize,
                50,
                newContentOfSize(pageSize)
        );

        assertThat(page.toString())
                .contains("totalCount=50")
                .contains("pageNumber=1")
                .contains("pageSize=10")
                .doesNotContain("content")
                .doesNotContain("Item");
    }

    @Test
    void shouldNotIncludeZeroBasedOffsets_WhenSerializedToJson() {
        var pageSize = 10;
        var page = new SimplePaginatedResultPage<>(
                2,
                pageSize,
                75,
                newContentOfSize(pageSize)
        );

        var json = JSON_HELPER.toJson(page);
        assertThat(json).doesNotContain("zeroBasedOffset");
    }

    @Test
    void shouldSerializeEmptyPageToJson() {
        var page = new SimplePaginatedResultPage<>(
                1,
                10,
                0,
                List.of()
        );

        var json = JSON_HELPER.toJson(page);

        var roundTripPage = JSON_HELPER.toObject(json, new TypeReference<SimplePaginatedResultPage<Item>>() {
        });

        assertThat(roundTripPage).usingRecursiveComparison().isEqualTo(page);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 20, 25})
    void shouldSerializePageToJson(int pageSize) {
        var page = new SimplePaginatedResultPage<>(
                2,
                pageSize,
                75,
                newContentOfSize(pageSize)
        );

        var json = JSON_HELPER.toJson(page);

        var roundTripPage = JSON_HELPER.toObject(json, new TypeReference<SimplePaginatedResultPage<Item>>() {
        });

        assertThat(roundTripPage).usingRecursiveComparison().isEqualTo(page);
    }

    @Test
    void shouldNotIncludeJsonIgnoredFieldsInSerializedJson() {
        var pageSize = 25;
        var page = new SimplePaginatedResultPage<>(
                2,
                pageSize,
                75,
                newContentOfSize(pageSize)
        );

        var json = JSON_HELPER.toJson(page, JsonHelper.OutputFormat.PRETTY);

        assertThat(json).doesNotContain("zeroBasedOffset");
    }

    static List<Item> newContentOfSize(int pageSize) {
        return IntStream.rangeClosed(1, pageSize)
                .mapToObj(value -> new Item("Item" + value, 1, 1.50))
                .toList();
    }

    record Item(String name, int quantity, double unitPrice) {
    }
}
