package org.kiwiproject.spring.data;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Sort;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @implNote This test uses Dropwizard's (JAX-RS) test support classes to enable testing of
 * the JAX-RS annotations in {@link PagingRequest}.
 */
@DisplayName("PagingRequest")
@ExtendWith({DropwizardExtensionsSupport.class, SoftAssertionsExtension.class})
class PagingRequestTest {

    private static final PagedResource PAGED_RESOURCE = new PagedResource();

    /**
     * @implNote Do NOT change {@code bootstrapLogging} back to true or remove it. Otherwise, ResourceExtension
     * basically resets all logging to WARN level, which then actually screws up other tests in addition to the fact
     * that you only see WARN-level logging from that point forward. Specifically, the LazyLogParameterSupplierTest
     * and JSchSlf4jLoggerTest tests break in weird and totally unexpected ways with no clear reason.
     */
    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(PAGED_RESOURCE)
            .build();

    @Test
    void shouldCreateInstanceWithoutDefaults(SoftAssertions softly) {
        var pagingRequest = new PagingRequest();

        softly.assertThat(pagingRequest.getPage()).isNull();
        softly.assertThat(pagingRequest.getLimit()).isNull();
        softly.assertThat(pagingRequest.getPrimarySort()).isNull();
        softly.assertThat(pagingRequest.getPrimaryDirection()).isNull();
        softly.assertThat(pagingRequest.getSecondarySort()).isNull();
        softly.assertThat(pagingRequest.getSecondaryDirection()).isNull();
    }

    @Test
    void shouldSetDefaults_FromDefaultValueAnnotations(SoftAssertions softly) {
        var pagingRequest = RESOURCES.client()
                .target("/paging")
                .request()
                .get(PagingRequest.class);

        softly.assertThat(pagingRequest.getPage()).isZero();
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
                .queryParam("limit", 25)
                .queryParam("primarySort", "lastName")
                .queryParam("primaryDirection", Sort.Direction.DESC.name())
                .queryParam("secondarySort", "firstName")
                .queryParam("secondaryDirection", Sort.Direction.ASC.name())
                .request()
                .get(PagingRequest.class);

        softly.assertThat(pagingRequest.getPage()).isEqualTo(5);
        softly.assertThat(pagingRequest.getLimit()).isEqualTo(25);
        softly.assertThat(pagingRequest.getPrimarySort()).isEqualTo("lastName");
        softly.assertThat(pagingRequest.getPrimaryDirection()).isEqualTo(Sort.Direction.DESC);
        softly.assertThat(pagingRequest.getSecondarySort()).isEqualTo("firstName");
        softly.assertThat(pagingRequest.getSecondaryDirection()).isEqualTo(Sort.Direction.ASC);
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
