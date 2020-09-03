package org.kiwiproject.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@ExtendWith(SoftAssertionsExtension.class)
class KiwiResponsesTest {

    @Nested
    class GetMediaType {

        @Test
        void shouldReturnMediaType_WhenResponseContainsMediaType(SoftAssertions softly) {
            softly.assertThat(KiwiResponses.mediaType(newMockResponseWithMediaType(APPLICATION_JSON_TYPE)))
                    .hasValue(APPLICATION_JSON);

            softly.assertThat(KiwiResponses.mediaType(newMockResponseWithMediaType(WILDCARD_TYPE)))
                    .hasValue(WILDCARD);

            softly.assertThat(KiwiResponses.mediaType(newMockResponseWithMediaType(TEXT_XML_TYPE)))
                    .hasValue(TEXT_XML);

            softly.assertThat(KiwiResponses.mediaType(newMockResponseWithMediaType(APPLICATION_OCTET_STREAM_TYPE)))
                    .hasValue(APPLICATION_OCTET_STREAM);
        }

        @Test
        void shouldReturnEmptyOptional_WhenResponseHasNoMediaType() {
            var response = newMockResponseWithMediaType(null);

            assertThat(KiwiResponses.mediaType(response)).isEmpty();
        }
    }

    private static Response newMockResponseWithMediaType(MediaType mediaType) {
        var response = mock(Response.class);
        when(response.getMediaType()).thenReturn(mediaType);
        return response;
    }

    @Nested
    class SuccessCheckMethods {

        @Test
        void shouldCheckSuccessful_ForStatusCodes(SoftAssertions softly) {
            forEachStatus(status -> {
                int statusCode = status.getStatusCode();
                boolean successfulFamily = successful(Family.familyOf(statusCode));
                softly.assertThat(KiwiResponses.successful(statusCode))
                        .describedAs("Status code: %d", statusCode)
                        .isEqualTo(successfulFamily);
            });
        }

        @Test
        void shouldCheckNotSuccessful_ForStatusCodes(SoftAssertions softly) {
            forEachStatus(status -> {
                int statusCode = status.getStatusCode();
                boolean successfulFamily = successful(Family.familyOf(statusCode));
                softly.assertThat(KiwiResponses.notSuccessful(statusCode))
                        .describedAs("Status code: %d", statusCode)
                        .isNotEqualTo(successfulFamily);
            });
        }

        @Test
        void shouldCheckSuccessful_ForStatusObjects(SoftAssertions softly) {
            forEachStatus(status -> {
                boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
                softly.assertThat(KiwiResponses.successful(status))
                        .describedAs("Status: %s (%d)", status, status.getStatusCode())
                        .isEqualTo(successfulFamily);
            });
        }

        @Test
        void shouldCheckNotSuccessful_ForStatusObjects(SoftAssertions softly) {
            forEachStatus(status -> {
                boolean successfulFamily = successful(Family.familyOf(status.getStatusCode()));
                softly.assertThat(KiwiResponses.notSuccessful(status))
                        .describedAs("Status: %s (%d)", status, status.getStatusCode())
                        .isNotEqualTo(successfulFamily);
            });
        }

        @Test
        void shouldCheckSuccessful_ForSuccessfulStatusTypeObjects(SoftAssertions softly) {
            var type = new Response.StatusType() {
                @Override
                public int getStatusCode() {
                    return 200;
                }

                @Override
                public Family getFamily() {
                    return Family.SUCCESSFUL;
                }

                @Override
                public String getReasonPhrase() {
                    return "OK";
                }
            };
            softly.assertThat(KiwiResponses.successful(type)).isTrue();
            softly.assertThat(KiwiResponses.notSuccessful(type)).isFalse();
        }

        @Test
        void shouldCheckSuccessful_ForUnsuccessfulStatusTypeObjects(SoftAssertions softly) {
            var type = new Response.StatusType() {
                @Override
                public int getStatusCode() {
                    return 500;
                }

                @Override
                public Family getFamily() {
                    return Family.SERVER_ERROR;
                }

                @Override
                public String getReasonPhrase() {
                    return "Server Error";
                }
            };
            softly.assertThat(KiwiResponses.successful(type)).isFalse();
            softly.assertThat(KiwiResponses.notSuccessful(type)).isTrue();
        }

        @Test
        void shouldCheckSuccessful_ForResponseObjects(SoftAssertions softly) {
            forEachStatus(status -> {
                var statusCode = status.getStatusCode();
                var response = newMockResponseWithStatusCode(statusCode);
                boolean successfulFamily = successful(Family.familyOf(statusCode));
                softly.assertThat(KiwiResponses.successful(response))
                        .describedAs("Status: %s (%d)", status, statusCode)
                        .isEqualTo(successfulFamily);
                verify(response, never()).close();
            });
        }

        @Test
        void shouldCheckNotSuccessful_ForResponseObjects(SoftAssertions softly) {
            forEachStatus(status -> {
                var statusCode = status.getStatusCode();
                var response = newMockResponseWithStatusCode(statusCode);
                boolean successfulFamily = successful(Family.familyOf(statusCode));
                softly.assertThat(KiwiResponses.notSuccessful(response))
                        .describedAs("Status: %s (%d)", status, statusCode)
                        .isNotEqualTo(successfulFamily);
                verify(response, never()).close();
            });
        }

        @Test
        void shouldCheckSuccessful_ForFamilyObjects(SoftAssertions softly) {
            Arrays.stream(Family.values()).forEach(family -> {
                boolean successfulFamily = successful(family);
                softly.assertThat(KiwiResponses.successful(family))
                        .describedAs("Family: %s", family)
                        .isEqualTo(successfulFamily);
            });
        }

        @Test
        void shouldCheckNotSuccessful_ForFamilyObjects(SoftAssertions softly) {
            Arrays.stream(Family.values()).forEach(family -> {
                boolean successfulFamily = successful(family);
                softly.assertThat(KiwiResponses.notSuccessful(family))
                        .describedAs("Family: %s", family)
                        .isNotEqualTo(successfulFamily);
            });
        }
    }

    private static void forEachStatus(Consumer<Response.Status> statusConsumer) {
        Arrays.stream(Response.Status.values()).forEach(statusConsumer);
    }

    private static boolean successful(Family family) {
        return family == Family.SUCCESSFUL;
    }

    @Nested
    class AlwaysClosingResponseMethods {

        @Test
        void shouldCheckSuccessful_AndCloseResponse_WhenSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.OK);

            assertThat(KiwiResponses.successfulAlwaysClosing(response)).isTrue();

            verify(response).close();
        }

        @Test
        void shouldCheckSuccessful_AndCloseResponse_WhenNotSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.UNAUTHORIZED);

            assertThat(KiwiResponses.successfulAlwaysClosing(response)).isFalse();

            verify(response).close();
        }

        @Test
        void shouldCheckNotSuccessful_AndCloseResponse_WhenSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.NO_CONTENT);

            assertThat(KiwiResponses.notSuccessfulAlwaysClosing(response)).isFalse();

            verify(response).close();
        }

        @Test
        void shouldCheckNotSuccessful_AndCloseResponse_WhenNotSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.MOVED_PERMANENTLY);

            assertThat(KiwiResponses.notSuccessfulAlwaysClosing(response)).isTrue();

            verify(response).close();
        }
    }

    @Nested
    class CloseQuietly {

        @Test
        void shouldCloseNonNullResponses() {
            var response = newMockResponseWithStatus(Response.Status.OK);

            KiwiResponses.closeQuietly(response);

            verify(response).close();
        }

        @Test
        void shouldIgnoreNullResponses() {
            assertThatCode(() -> KiwiResponses.closeQuietly(null))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldIgnoreExceptions() {
            var response = newMockResponseWithStatus(Response.Status.INTERNAL_SERVER_ERROR);
            doThrow(new ProcessingException("error closing")).when(response).close();

            assertThatCode(() -> KiwiResponses.closeQuietly(response))
                    .doesNotThrowAnyException();
        }
    }

    private static Response newMockResponseWithStatus(Response.Status status) {
        return newMockResponseWithStatusCode(status.getStatusCode());
    }

    private static Response newMockResponseWithStatusCode(int statusCode) {
        var response = mock(Response.class);
        when(response.getStatus()).thenReturn(statusCode);
        return response;
    }

    @Nested
    class ResponseTypeMethods {

        @Test
        void shouldCheckOkResponse(SoftAssertions softly) {
            softly.assertThat(KiwiResponses.ok(newResponseWithStatus(Response.Status.OK))).isTrue();
            softly.assertThat(KiwiResponses.ok(newResponseWithStatus(Response.Status.NO_CONTENT))).isFalse();
        }

        @Test
        void shouldCheckCreatedResponse(SoftAssertions softly) {
            softly.assertThat(KiwiResponses.created(newResponseWithStatus(Response.Status.CREATED))).isTrue();
            softly.assertThat(KiwiResponses.created(newResponseWithStatus(Response.Status.OK))).isFalse();
        }

        @Test
        void shouldCheckNotFoundResponse(SoftAssertions softly) {
            softly.assertThat(KiwiResponses.notFound(newResponseWithStatus(Response.Status.NOT_FOUND))).isTrue();
            softly.assertThat(KiwiResponses.notFound(newResponseWithStatus(Response.Status.OK))).isFalse();
            softly.assertThat(KiwiResponses.notFound(newResponseWithStatus(Response.Status.UNAUTHORIZED))).isFalse();
        }

        @Test
        void shouldCheckInternalServerErrorResponse(SoftAssertions softly) {
            softly.assertThat(KiwiResponses.internalServerError(newResponseWithStatus(Response.Status.INTERNAL_SERVER_ERROR))).isTrue();
            softly.assertThat(KiwiResponses.internalServerError(newResponseWithStatus(Response.Status.OK))).isFalse();
            softly.assertThat(KiwiResponses.internalServerError(newResponseWithStatus(Response.Status.NOT_FOUND))).isFalse();
        }
    }

    private static Response newResponseWithStatus(Response.Status status) {
        return Response.status(status).build();
    }

    @Nested
    class FamilyCheckMethods {

        @Test
        void shouldCheckInformational() {
            IntStream.rangeClosed(100, 199).forEach(statusCode ->
                    assertThat(KiwiResponses.informational(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isTrue());

            IntStream.rangeClosed(200, 599).forEach(statusCode ->
                    assertThat(KiwiResponses.informational(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());
        }

        @Test
        void shouldCheckRedirection() {
            IntStream.rangeClosed(100, 299).forEach(statusCode ->
                    assertThat(KiwiResponses.redirection(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());

            IntStream.rangeClosed(300, 399).forEach(statusCode ->
                    assertThat(KiwiResponses.redirection(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isTrue());

            IntStream.rangeClosed(400, 599).forEach(statusCode ->
                    assertThat(KiwiResponses.redirection(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());
        }

        @Test
        void shouldCheckClientError() {
            IntStream.rangeClosed(100, 399).forEach(statusCode ->
                    assertThat(KiwiResponses.clientError(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());

            IntStream.rangeClosed(400, 499).forEach(statusCode ->
                    assertThat(KiwiResponses.clientError(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isTrue());

            IntStream.rangeClosed(500, 599).forEach(statusCode ->
                    assertThat(KiwiResponses.clientError(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());
        }

        @Test
        void shouldCheckServerError() {
            IntStream.rangeClosed(100, 499).forEach(statusCode ->
                    assertThat(KiwiResponses.serverError(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());

            IntStream.rangeClosed(500, 599).forEach(statusCode ->
                    assertThat(KiwiResponses.serverError(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isTrue());
        }

        @Test
        void shouldCheckOther() {
            IntStream.rangeClosed(100, 599).forEach(statusCode ->
                    assertThat(KiwiResponses.otherFamily(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isFalse());

            IntStream.rangeClosed(600, 999).forEach(statusCode ->
                    assertThat(KiwiResponses.otherFamily(newResponseWithStatusCode(statusCode)))
                            .describedAs("Status: %d", statusCode)
                            .isTrue());
        }
    }

    private static Response newResponseWithStatusCode(int status) {
        return Response.status(status).build();
    }
}