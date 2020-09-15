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

    public static void assertStatusCode(Response response, int expectedStatusCode) {
        assertThat(response.getStatus()).isEqualTo(expectedStatusCode);
    }

    public static void assertResponseMediaType(Response response, String expectedType) {
        assertResponseMediaType(response, MediaType.valueOf(expectedType));
    }

    public static void assertResponseMediaType(Response response, MediaType expectedType) {
        assertThat(response.getMediaType()).isEqualTo(expectedType);
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
