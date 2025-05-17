package org.kiwiproject.spring.data;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.search.KiwiSearching.PageNumberingScheme;
import org.springframework.data.domain.Sort;

/**
 * @implNote This test uses Dropwizard's (Jakarta REST) test support classes to enable testing of
 * the Jakarta REST annotations in {@link PagingRequest}.
 */
@DisplayName("PagingRequest")
@ExtendWith({DropwizardExtensionsSupport.class, SoftAssertionsExtension.class})
class PagingRequestTest {

    private static final PagedResource PAGED_RESOURCE = new PagedResource();

    /**
     * @implNote Do NOT change {@code bootstrapLogging} back to true or remove it. Otherwise, ResourceExtension
     * basically resets all log levels to WARN, which then actually screws up other tests in addition to the fact
     * that you only see WARN-level logging from that point forward. Specifically, the LazyLogParameterSupplierTest
     * and JSchSlf4jLoggerTest tests break in odd and totally unexpected ways with no clear reason.
     */
    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(PAGED_RESOURCE)
            .build();

    @Test
    void shouldCreateInstanceWithExpectedDefaults(SoftAssertions softly) {
        var pagingRequest = new PagingRequest();

        softly.assertThat(pagingRequest.getPage()).isZero();
        softly.assertThat(pagingRequest.getNumbering()).isEqualTo(PageNumberingScheme.ZERO_BASED);
        softly.assertThat(pagingRequest.getLimit()).isEqualTo(100);
        softly.assertThat(pagingRequest.getPrimarySort()).isNull();
        softly.assertThat(pagingRequest.getPrimaryDirection()).isEqualTo(Sort.Direction.ASC);
        softly.assertThat(pagingRequest.getSecondarySort()).isNull();
        softly.assertThat(pagingRequest.getSecondaryDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldSetDefaults_FromDefaultValueAnnotations(SoftAssertions softly) {
        var pagingRequest = RESOURCES.client()
                .target("/paging")
                .request()
                .get(PagingRequest.class);

        softly.assertThat(pagingRequest.getPage()).isZero();
        softly.assertThat(pagingRequest.getNumbering()).isEqualTo(PageNumberingScheme.ZERO_BASED);
        softly.assertThat(pagingRequest.getLimit()).isEqualTo(PagingRequest.DEFAULT_MAX_LIMIT);
        softly.assertThat(pagingRequest.getPrimaryDirection()).isEqualTo(Sort.Direction.ASC);
        softly.assertThat(pagingRequest.getSecondaryDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldNotProvide_DefaultSortProperties(SoftAssertions softly) {
        var pagingRequest = RESOURCES.client()
                .target("/paging")
                .request()
                .get(PagingRequest.class);

        softly.assertThat(pagingRequest.getPrimarySort()).isNull();
        softly.assertThat(pagingRequest.getSecondarySort()).isNull();
    }

    @Test
    void shouldAccept_AllPagingParameters(SoftAssertions softly) {
        var pagingRequest = RESOURCES.client().target("/paging")
                .queryParam("page", 5)
                .queryParam("numbering", "ONE_BASED")
                .queryParam("limit", 25)
                .queryParam("primarySort", "lastName")
                .queryParam("primaryDirection", Sort.Direction.DESC.name())
                .queryParam("secondarySort", "firstName")
                .queryParam("secondaryDirection", Sort.Direction.ASC.name())
                .request()
                .get(PagingRequest.class);

        softly.assertThat(pagingRequest.getPage()).isEqualTo(5);
        softly.assertThat(pagingRequest.getNumbering()).isEqualTo(PageNumberingScheme.ONE_BASED);
        softly.assertThat(pagingRequest.getLimit()).isEqualTo(25);
        softly.assertThat(pagingRequest.getPrimarySort()).isEqualTo("lastName");
        softly.assertThat(pagingRequest.getPrimaryDirection()).isEqualTo(Sort.Direction.DESC);
        softly.assertThat(pagingRequest.getSecondarySort()).isEqualTo("firstName");
        softly.assertThat(pagingRequest.getSecondaryDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldHandleEmptyPageAndLimitAsDefaults(SoftAssertions softly) {
        var pagingRequest = RESOURCES.client().target("/paging")
                .queryParam("page", "")
                .queryParam("numbering", "")
                .queryParam("limit", "")
                .request()
                .get(PagingRequest.class);

        softly.assertThat(pagingRequest.getPage()).isZero();
        softly.assertThat(pagingRequest.getNumbering()).isEqualTo(PageNumberingScheme.ZERO_BASED);
        softly.assertThat(pagingRequest.getLimit()).isEqualTo(PagingRequest.DEFAULT_MAX_LIMIT);
    }

    @Test
    void shouldNotAllowNullArgument_ToCopyOf() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PagingRequest.copyOf(null))
                .withMessage("PagingRequest to copy must not be null");
    }

    @Test
    void shouldCreateCopy() {
        var original = new PagingRequest();
        original.setPage(2);
        original.setNumbering(PageNumberingScheme.ONE_BASED);
        original.setLimit(20);
        original.setPrimarySort("lastName");
        original.setPrimaryDirection(Sort.Direction.DESC);
        original.setSecondarySort("firstName");
        original.setSecondaryDirection(Sort.Direction.DESC);

        var copy = PagingRequest.copyOf(original);

        assertThat(copy).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    void shouldCreateCopy_FromInstance() {
        var original = new PagingRequest();
        original.setPage(1);
        original.setNumbering(PageNumberingScheme.ONE_BASED);
        original.setLimit(10);
        original.setPrimarySort("age");
        original.setPrimaryDirection(Sort.Direction.DESC);

        var copy = original.copyOf();

        assertThat(copy).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    void shouldEnsurePaginationProperties_AndChangeNothing_IfHasPaginationProperties() {
        var request = new PagingRequest();
        request.setPage(3);
        request.setNumbering(PageNumberingScheme.ONE_BASED);
        request.setLimit(15);

        assertThat(PagingRequest.ensurePaginationProperties(request)).isFalse();
    }

    @Test
    void shouldEnsurePaginationProperties_AndChangePage_IfMissing() {
        var request = new PagingRequest();
        request.setPage(null);
        request.setNumbering(PageNumberingScheme.ONE_BASED);

        assertThat(PagingRequest.ensurePaginationProperties(request)).isTrue();
        assertThat(request.getPage()).isOne();
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1})
    void shouldEnsurePaginationProperties_AndChangePage_IfInvalid(int page) {
        var request = new PagingRequest();
        request.setPage(page);

        assertThat(PagingRequest.ensurePaginationProperties(request)).isTrue();
        assertThat(request.getPage()).isZero();
    }

    @Test
    void shouldEnsurePaginationProperties_AndChangeNumbering_IfMissing() {
        var request = new PagingRequest();
        request.setNumbering(null);

        assertThat(PagingRequest.ensurePaginationProperties(request)).isTrue();
        assertThat(request.getNumbering()).isEqualTo(PageNumberingScheme.ZERO_BASED);
    }

    @Test
    void shouldEnsurePaginationProperties_AndChangeLimit_IfMissing() {
        var request = new PagingRequest();
        request.setLimit(null);

        assertThat(PagingRequest.ensurePaginationProperties(request)).isTrue();
        assertThat(request.getLimit()).isEqualTo(PagingRequest.DEFAULT_MAX_LIMIT);
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 0})
    void shouldEnsurePaginationProperties_AndChangeLimit_IfInvalid(int limit) {
        var request = new PagingRequest();
        request.setLimit(limit);

        assertThat(PagingRequest.ensurePaginationProperties(request)).isTrue();
        assertThat(request.getLimit()).isEqualTo(PagingRequest.DEFAULT_MAX_LIMIT);
    }

    @Test
    void shouldNotAllowNullPagingRequest_To_static_withPaginationProperties() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PagingRequest.withPaginationProperties(null))
                .withMessage("PagingRequest must not be null");
    }

    @Test
    void shouldReturnSameInstance_When_static_withPaginationProperties_IsGivenPagingRequest_HavingPaginationProperties() {
        var request = new PagingRequest();
        request.setPage(1);
        request.setNumbering(PageNumberingScheme.ONE_BASED);
        request.setLimit(25);

        assertThat(PagingRequest.withPaginationProperties(request)).isSameAs(request);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "null, ONE_BASED, 10",
                    "1, null, 25",
                    "0, ZERO_BASED, null",
                    "null, null, null"
            },
            nullValues = "null"
    )
    void shouldReturnNewInstance_When_static_withPaginationProperties_IsGivenInvalidPagingRequest(
            Integer page, PageNumberingScheme numbering, Integer limit) {

        var request = new PagingRequest();
        request.setPage(page);
        request.setNumbering(numbering);
        request.setLimit(limit);

        var expectedRequest = buildExpectedPagingRequest(page, numbering, limit);

        assertThat(PagingRequest.withPaginationProperties(request))
                .usingRecursiveComparison()
                .isEqualTo(expectedRequest);
    }

    private static PagingRequest buildExpectedPagingRequest(@Nullable Integer page,
                                                            @Nullable PageNumberingScheme numbering,
                                                            @Nullable Integer limit) {
        var expectedRequest = new PagingRequest();

        var expectedNumbering = isNull(numbering) ? PageNumberingScheme.ZERO_BASED : numbering;
        expectedRequest.setNumbering(expectedNumbering);

        var expectedPage = isNull(page) ? expectedNumbering.getMinimumPageNumber() : page;
        expectedRequest.setPage(expectedPage);

        var expectedLimit = isNull(limit) ? PagingRequest.DEFAULT_MAX_LIMIT : limit;
        expectedRequest.setLimit(expectedLimit);
        return expectedRequest;
    }

    @Test
    void shouldReturnSameInstance_When_withPaginationProperties_IsGivenPagingRequest_HavingPaginationProperties() {
        var request = new PagingRequest();
        request.setPage(1);
        request.setNumbering(PageNumberingScheme.ONE_BASED);
        request.setLimit(25);

        assertThat(request.withPaginationProperties()).isSameAs(request);
    }

    @Test
    void shouldReturnNewInstance_When_withPaginationProperties_IsGivenInvalidPagingRequest() {
        var request = new PagingRequest();
        request.setPage(null);
        request.setNumbering(null);
        request.setLimit(null);

        var expectedRequest = new PagingRequest();

        assertThat(request.withPaginationProperties())
                .usingRecursiveComparison()
                .isEqualTo(expectedRequest);
    }

    @Path("/paging")
    @Produces({MediaType.APPLICATION_JSON})
    @Slf4j
    public static class PagedResource {

        @GET
        public Response page(@BeanParam PagingRequest pagingRequest) {
            LOG.debug("Received PagingRequest: {}", pagingRequest);
            return Response.ok(pagingRequest).build();
        }
    }
}
