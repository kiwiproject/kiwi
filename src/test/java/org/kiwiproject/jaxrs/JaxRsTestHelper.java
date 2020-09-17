package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import lombok.experimental.UtilityClass;
import org.kiwiproject.jaxrs.exception.ErrorMessage;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@UtilityClass
public class JaxRsTestHelper {

    public static void assertResponseStatusCode(Response response, Response.Status expectedStatusCode) {
        assertResponseStatusCode(response, expectedStatusCode.getStatusCode());
    }

    public static void assertResponseStatusCode(Response response, int expectedStatusCode) {
        assertThat(response.getStatus()).isEqualTo(expectedStatusCode);
    }

    public static void assertJsonResponseType(Response response) {
        assertResponseType(response, MediaType.APPLICATION_JSON_TYPE);
    }

    public static void assertResponseType(Response response, String expectedType) {
        assertResponseType(response, MediaType.valueOf(expectedType));
    }

    public static void assertResponseType(Response response, MediaType expectedType) {
        assertThat(response.getMediaType()).isEqualTo(expectedType);
    }

    public static void assertCreatedResponseWithLocation(Response response, String expectedLocation) {
        assertResponseStatusCode(response, Response.Status.CREATED);

        assertThat(response.getHeaders().getFirst("Location")).hasToString(expectedLocation);
    }

    public static void assertOkResponse(Response response) {
        assertResponseStatusCode(response, Response.Status.OK);
    }

    public static void assertNoContentResponse(Response response) {
        assertResponseStatusCode(response, Response.Status.NO_CONTENT);
    }

    public static void assertAcceptedResponse(Response response) {
        assertResponseStatusCode(response, Response.Status.ACCEPTED);
    }

    public static void assertNotFoundResponse(Response response) {
        assertResponseStatusCode(response, Response.Status.NOT_FOUND);
    }

    public static void assertBadRequestResponse(Response response) {
        assertResponseStatusCode(response, Response.Status.BAD_REQUEST);
    }

    public static void assertUnauthorizedFoundResponse(Response response) {
        assertResponseStatusCode(response, Response.Status.UNAUTHORIZED);
    }

    public static <T> void assertStatusAndResponseEntity(Response response,
                                                         Response.Status expectedStatus,
                                                         T expectedEntity) {
        assertResponseStatusCode(response, expectedStatus.getStatusCode());
        assertResponseEntity(response, expectedEntity);
    }

    public static <T> void assertResponseEntity(Response response, T expectedEntity) {
        var entity = assertNonNullResponseEntity(response, expectedEntity.getClass());
        assertThat(entity).isSameAs(expectedEntity);
    }

    public static <T> T assertNonNullResponseEntity(Response response, Class<T> expectedType) {
        var entity = response.getEntity();
        assertThat(entity).isInstanceOf(expectedType);

        return expectedType.cast(entity);
    }

    public static void assertCustomHeaderFirstValue(Response response, String headerName, Object expectedValue) {
        assertThat(response.getHeaders().getFirst(headerName)).isEqualTo(expectedValue);
    }

    public static void assertResponseEntityHasOneErrorMessage(Response response,
                                                              int expectedStatusCode,
                                                              String expectedMessage) {
        Map<String, Object> entity = assertHasMapEntity(response);
        assertThat(entity).containsOnly(
                entry("errors", List.of(
                        new ErrorMessage(expectedStatusCode, expectedMessage))
                )
        );
    }

    public static Map<String, Object> assertHasMapEntity(Response response) {
        var entityObj = response.getEntity();
        assertThat(entityObj).isInstanceOf(Map.class);

        //noinspection unchecked
        return (Map<String, Object>) entityObj;
    }
}
