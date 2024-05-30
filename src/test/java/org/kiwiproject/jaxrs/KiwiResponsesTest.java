package org.kiwiproject.jaxrs;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static jakarta.ws.rs.core.MediaType.WILDCARD;
import static jakarta.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.jaxrs.KiwiResponses.WebCallResult;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@ExtendWith(SoftAssertionsExtension.class)
class KiwiResponsesTest {

    private AtomicInteger successCount;
    private AtomicInteger failureCount;
    private AtomicInteger exceptionCount;

    @BeforeEach
    void setUp() {
        successCount = new AtomicInteger();
        failureCount = new AtomicInteger();
        exceptionCount = new AtomicInteger();
    }

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

    @Nested
    class OnSuccessOrFailure_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.CREATED);
            Supplier<Response> supplier = () -> response;

            KiwiResponses.onSuccessOrFailure(supplier,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet(),
                    supplierException -> exceptionCount.incrementAndGet());

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);
            assertThat(exceptionCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldCallExceptionConsumer_WhenResponseSupplierThrowsException() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            KiwiResponses.onSuccessOrFailure(supplier,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet(),
                    supplierException -> exceptionCount.incrementAndGet());

            assertThat(successCount).hasValue(0);
            assertThat(failureCount).hasValue(0);
            assertThat(exceptionCount).hasValue(1);
        }
    }

    @Nested
    class OnSuccessOrFailure {

        @Test
        void shouldCallSuccessConsumer_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.ACCEPTED);

            KiwiResponses.onSuccessOrFailure(response,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet());

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldCallFailedConsumer_ForSuccessfulResponse_WhenCloseThrowsException() {
            var response = newMockResponseWithStatus(Response.Status.ACCEPTED);

            doThrow(new ProcessingException("error processing...")).when(response).close();

            KiwiResponses.onSuccessOrFailure(response,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet());

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldCallFailedConsumer_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.FORBIDDEN);

            KiwiResponses.onSuccessOrFailure(response,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet());

            assertThat(successCount).hasValue(0);
            assertThat(failureCount).hasValue(1);

            verify(response).close();
        }
    }

    @Nested
    class OnSuccessOrFailureThrow_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.OK);
            Supplier<Response> supplier = () -> response;

            KiwiResponses.onSuccessOrFailureThrow(supplier,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> {
                        failureCount.incrementAndGet();
                        return new CustomKiwiResponsesRuntimeException(failResponse);
                    });

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldRethrowSupplierException_WhenResponseSupplierThrowsException() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            var thrown = catchThrowable(() ->
                    KiwiResponses.onSuccessOrFailureThrow(supplier,
                            successResponse -> successCount.incrementAndGet(),
                            failResponse -> {
                                failureCount.incrementAndGet();
                                return new CustomKiwiResponsesRuntimeException(failResponse);
                            }));

            assertThat(thrown).isExactlyInstanceOf(ProcessingException.class)
                    .hasMessage("request processing failed");

            assertThat(successCount).hasValue(0);
            assertThat(failureCount).hasValue(0);
        }
    }

    @Nested
    class OnSuccessOrFailureThrow {

        @Test
        void shouldCallSuccessConsumer_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.CREATED);

            KiwiResponses.onSuccessOrFailureThrow(response,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> {
                        failureCount.incrementAndGet();
                        return new CustomKiwiResponsesRuntimeException(failResponse);
                    });

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldThrow_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.PAYMENT_REQUIRED);

            var thrown = catchThrowable(() ->
                    KiwiResponses.onSuccessOrFailureThrow(response,
                            successResponse -> successCount.incrementAndGet(),
                            failResponse -> {
                                failureCount.incrementAndGet();
                                return new CustomKiwiResponsesRuntimeException(failResponse);
                            }));

            assertThat(thrown).isExactlyInstanceOf(CustomKiwiResponsesRuntimeException.class);

            assertThat(successCount).hasValue(0);
            assertThat(failureCount).hasValue(1);

            verify(response).close();
        }
    }

    @Nested
    class OnSuccess_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.CREATED);
            Supplier<Response> supplier = () -> response;

            KiwiResponses.onSuccess(supplier, successResponse -> successCount.incrementAndGet());

            assertThat(successCount).hasValue(1);

            verify(response).close();
        }

        @Test
        void shouldIgnoreExceptionsThrownBySupplier() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            KiwiResponses.onSuccess(supplier, successResponse -> successCount.incrementAndGet());

            assertThat(successCount).hasValue(0);
        }
    }

    @Nested
    class OnSuccess {

        @Test
        void shouldCallSuccessConsumer_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.OK);

            KiwiResponses.onSuccess(response, successResponse -> successCount.incrementAndGet());

            assertThat(successCount).hasValue(1);

            verify(response).close();
        }

        @Test
        void shouldNotCallSuccessConsumer_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.GATEWAY_TIMEOUT);

            KiwiResponses.onSuccess(response, successResponse -> successCount.incrementAndGet());

            assertThat(successCount).hasValue(0);

            verify(response).close();
        }
    }

    @Nested
    class OnSuccessWithResult_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.ACCEPTED);
            Supplier<Response> supplier = () -> response;

            Optional<Integer> count = KiwiResponses.onSuccessWithResult(supplier,
                    successResponse -> successCount.incrementAndGet());

            assertThat(count).hasValue(1);
            assertThat(successCount).hasValue(1);

            verify(response).close();
        }

        @Test
        void shouldIgnoreExceptionsThrownBySupplier() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            Optional<Integer> count = KiwiResponses.onSuccessWithResult(supplier,
                    successResponse -> successCount.incrementAndGet());

            assertThat(count).isEmpty();
            assertThat(successCount).hasValue(0);
        }
    }

    @Nested
    class OnSuccessWithResult {

        @Test
        void shouldReturnResult_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.OK);

            Optional<Integer> count = KiwiResponses.onSuccessWithResult(response,
                    successResponse -> successCount.incrementAndGet());

            assertThat(count).hasValue(1);
            assertThat(successCount).hasValue(1);

            verify(response).close();
        }

        @Test
        void shouldReturnEmptyOptional_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.UNSUPPORTED_MEDIA_TYPE);

            Optional<Integer> count = KiwiResponses.onSuccessWithResult(response,
                    successResponse -> successCount.incrementAndGet());

            assertThat(count).isEmpty();
            assertThat(successCount).hasValue(0);

            verify(response).close();
        }
    }

    @Nested
    class OnFailure_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.BAD_REQUEST);
            Supplier<Response> supplier = () -> response;

            KiwiResponses.onFailure(supplier,
                    failResponse -> failureCount.incrementAndGet(),
                    exceptionConsumer -> exceptionCount.incrementAndGet());

            assertThat(failureCount).hasValue(1);
            assertThat(exceptionCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldCallExceptionConsumer_WhenResponseSupplierThrowsException() {
	        Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            KiwiResponses.onFailure(supplier,
                    failResponse -> failureCount.incrementAndGet(),
                    exceptionConsumer -> exceptionCount.incrementAndGet());

            assertThat(failureCount).hasValue(0);
            assertThat(exceptionCount).hasValue(1);
        }
    }

    @Nested
    class OnFailure {

        @Test
        void shouldNotCallFailConsumer_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.CREATED);

            KiwiResponses.onFailure(response, failResponse -> failureCount.incrementAndGet());

            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldCallFailConsumer_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.CONFLICT);

            KiwiResponses.onFailure(response, failResponse -> failureCount.incrementAndGet());

            assertThat(failureCount).hasValue(1);

            verify(response).close();
        }
    }

    @Nested
    class OnFailureThrow_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
	        var response = newMockResponseWithStatus(Response.Status.CREATED);
            Supplier<Response> supplier = () -> response;

            KiwiResponses.onFailureThrow(supplier, failResponse -> {
                failureCount.incrementAndGet();
                return new CustomKiwiResponsesRuntimeException(response);
            });

            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldRethrowSupplierException_WhenResponseSupplierThrowsException() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            var thrown = catchThrowable(() ->
                    KiwiResponses.onFailureThrow(supplier, failResponse -> {
                        failureCount.incrementAndGet();
                        return new RuntimeException("should not be called");
                    }));

            assertThat(thrown).isExactlyInstanceOf(ProcessingException.class)
                        .hasMessage("request processing failed");

            assertThat(failureCount).hasValue(0);
        }
    }

    @Nested
    class OnFailureThrow {

        @Test
        void shouldNotThrow_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.CREATED);

            KiwiResponses.onFailureThrow(response, failResponse -> {
                failureCount.incrementAndGet();
                return new CustomKiwiResponsesRuntimeException(response);
            });

            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldThrow_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.EXPECTATION_FAILED);

            var thrown = catchThrowable(() ->
                    KiwiResponses.onFailureThrow(response, failResponse -> {
                        failureCount.incrementAndGet();
                        return new CustomKiwiResponsesRuntimeException(response);
                    }));

            assertThat(thrown).isExactlyInstanceOf(CustomKiwiResponsesRuntimeException.class);

            assertThat(failureCount).hasValue(1);

            verify(response).close();
        }
    }

    @Nested
    class OnSuccessWithResultOrFailure_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.NO_CONTENT);
            Supplier<Response> supplier = () -> response;

            Optional<Integer> count = KiwiResponses.onSuccessWithResultOrFailure(supplier,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet(),
                    exceptionConsumer -> exceptionCount.incrementAndGet());

            assertThat(count).hasValue(1);

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);
            assertThat(exceptionCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldCallExceptionConsumer_WhenResponseSupplierThrowsException() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            Optional<Integer> count = KiwiResponses.onSuccessWithResultOrFailure(supplier,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet(),
                    exceptionConsumer -> exceptionCount.incrementAndGet());

            assertThat(count).isEmpty();

            assertThat(successCount).hasValue(0);
            assertThat(failureCount).hasValue(0);
            assertThat(exceptionCount).hasValue(1);
        }
    }

    @Nested
    class OnSuccessWithResultOrFailure {

        @Test
        void shouldReturnResult_ForSuccessfulResult() {
            var response = newMockResponseWithStatus(Response.Status.NO_CONTENT);

            Optional<Integer> count = KiwiResponses.onSuccessWithResultOrFailure(response,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet());

            assertThat(count).hasValue(1);

            assertThat(successCount).hasValue(1);
            assertThat(failureCount).hasValue(0);

            verify(response).close();
        }

        @Test
        void shouldReturnEmptyOptional_ForUnsuccessfulResult() {
            var response = newMockResponseWithStatus(Response.Status.METHOD_NOT_ALLOWED);

            Optional<Integer> count = KiwiResponses.onSuccessWithResultOrFailure(response,
                    successResponse -> successCount.incrementAndGet(),
                    failResponse -> failureCount.incrementAndGet());

            assertThat(count).isEmpty();

            assertThat(successCount).hasValue(0);
            assertThat(failureCount).hasValue(1);

            verify(response).close();
        }
    }

    @Nested
    class OnSuccessOrFailureWithResult_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.OK);
            Supplier<Response> supplier = () -> response;

            var result = KiwiResponses.onSuccessOrFailureWithResult(supplier,
                    successResponse -> 42,
                    failResponse -> -1,
                    supplierException -> 84);

            assertThat(result).isEqualTo(42);

            verify(response).close();
        }

        @Test
        void shouldCallExceptionFunction_WhenResponseSupplierThrowsException() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            var result = KiwiResponses.onSuccessOrFailureWithResult(supplier,
                    successResponse -> 42,
                    failResponse -> -1,
                    supplierException -> 84);

            assertThat(result).isEqualTo(84);
        }
    }

    @Nested
    class OnSuccessOrFailureWithResult {

        @Test
        void shouldCallSuccessFunction_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.OK);

            var result = KiwiResponses.onSuccessOrFailureWithResult(response,
                    successResponse -> 42,
                    failResponse -> -1);

            assertThat(result).isEqualTo(42);

            verify(response).close();
        }

        @Test
        void shouldCallFailFunction_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.INTERNAL_SERVER_ERROR);

            var result = KiwiResponses.onSuccessOrFailureWithResult(response,
                    successResponse -> 42,
                    failResponse -> -1);

            assertThat(result).isEqualTo(-1);

            verify(response).close();
        }
    }

    @Nested
    class OnSuccessWithResultOrFailureThrow_UsingResponseSupplier {

        @Test
        void shouldUseResponseFromSupplier() {
            var response = newMockResponseWithStatus(Response.Status.PARTIAL_CONTENT);
            Supplier<Response> supplier = () -> response;

            var result = KiwiResponses.onSuccessWithResultOrFailureThrow(supplier,
                    successResponse -> 42,
                    CustomKiwiResponsesRuntimeException::new);

            assertThat(result).isEqualTo(42);

            verify(response).close();
        }

        @Test
        void shouldRethrowSupplierException_WhenResponseSupplierThrowsException() {
            Supplier<Response> supplier = () -> {
                throw new ProcessingException("request processing failed");
            };

            var thrown = catchThrowable(() ->
                    KiwiResponses.onSuccessWithResultOrFailureThrow(supplier,
                            successResponse -> 42,
                            CustomKiwiResponsesRuntimeException::new));

            assertThat(thrown).isExactlyInstanceOf(ProcessingException.class)
                    .hasMessage("request processing failed");
        }
    }

    @Nested
    class OnSuccessWithResultOrFailureThrow {

        @Test
        void shouldReturnResult_ForSuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.PARTIAL_CONTENT);

            var result = KiwiResponses.onSuccessWithResultOrFailureThrow(response,
                    successResponse -> 42,
                    CustomKiwiResponsesRuntimeException::new);

            assertThat(result).isEqualTo(42);

            verify(response).close();
        }

        @Test
        void shouldThrow_ForUnsuccessfulResponse() {
            var response = newMockResponseWithStatus(Response.Status.NOT_ACCEPTABLE);

            var thrown = catchThrowable(() -> KiwiResponses.onSuccessWithResultOrFailureThrow(response,
                    successResponse -> 42,
                    CustomKiwiResponsesRuntimeException::new));

            assertThat(thrown)
                    .isExactlyInstanceOf(CustomKiwiResponsesRuntimeException.class)
                    .hasMessage("Kiwi received failed response with status: 406");

            verify(response).close();
        }
    }

    @Nested
    class WebCallResultRecord {

        @Test
        void shouldConstructWithResponse() {
            var response = Response.accepted().build();

            var result = new WebCallResult(null, response);

            assertThat(result.hasResponse()).isTrue();
            assertThat(result.response()).isSameAs(response);
            assertThat(result.error()).isNull();
        }

        @Test
        void shouldConstructWithError() {
            var error = new ProcessingException("something failed");

            var result = new WebCallResult(error, null);

            assertThat(result.hasResponse()).isFalse();
            assertThat(result.response()).isNull();
            assertThat(result.error()).isSameAs(error);
        }

        @Test
        void shouldThrowIllegalArgument_WhenResponseAndEror_AreBothNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new WebCallResult(null, null));
        }

        @Test
        void shouldThrowIllegalArgument_WhenResponseAndEror_AreBothNonNull() {
            var error = new ProcessingException("something failed");
            var response = Response.serverError().build();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new WebCallResult(error, response));
        }
    }

    @Nested
    class Accept {

        @Test
        void shouldAccept_SuccessfulResponses() {
            var response = newMockResponseWithStatus(Response.Status.NO_CONTENT);

            assertAccepts(response);
        }

        @Test
        void shouldAccept_UnsuccessfulResponses() {
            var response = newMockResponseWithStatus(Response.Status.BAD_REQUEST);

            assertAccepts(response);
        }

        private void assertAccepts(Response response) {
            KiwiResponses.accept(response, response1 -> successCount.incrementAndGet());

            assertThat(successCount).hasValue(1);

            verify(response).close();
        }
    }

    @Nested
    class Apply {

        @Test
        void shouldApply_SuccessfulResponses() {
            var response = newMockResponseWithStatus(Response.Status.CREATED);

            assertApplies(response);
        }

        @Test
        void shouldApply_UnsuccessfulResponses() {
            var response = newMockResponseWithStatus(Response.Status.UNAUTHORIZED);

            assertApplies(response);
        }

        private void assertApplies(Response response) {
            var result = KiwiResponses.apply(response, response1 -> 84);

            assertThat(result).isEqualTo(84);

            verify(response).close();
        }
    }

    private static class CustomKiwiResponsesRuntimeException extends RuntimeException {
        CustomKiwiResponsesRuntimeException(Response response) {
            super("Kiwi received failed response with status: " + response.getStatus());
        }
    }

    private static Response newResponseWithStatusCode(int status) {
        return Response.status(status).build();
    }
}
