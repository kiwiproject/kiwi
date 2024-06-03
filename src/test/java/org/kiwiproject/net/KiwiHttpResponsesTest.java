package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.params.IntRangeSource;

@DisplayName("KiwiHttpResponses")
class KiwiHttpResponsesTest {

    @ParameterizedTest
    @IntRangeSource(from = 0, to = 700, step = 1, closed = true)
    void shouldPassStatusCodeChecks(int statusCode) {
        assertAll(

            () -> assertThat(KiwiHttpResponses.informational(statusCode))
                    .isEqualTo(firstDigitInFamily(statusCode) == 1),

            () -> assertThat(KiwiHttpResponses.successful(statusCode))
                    .isEqualTo(firstDigitInFamily(statusCode) == 2),

            () -> assertThat(KiwiHttpResponses.notSuccessful(statusCode))
                    .isNotEqualTo(firstDigitInFamily(statusCode) == 2),

            () -> assertThat(KiwiHttpResponses.redirection(statusCode))
                    .isEqualTo(firstDigitInFamily(statusCode) == 3),

            () -> assertThat(KiwiHttpResponses.clientError(statusCode))
                    .isEqualTo(firstDigitInFamily(statusCode) == 4),

            () -> assertThat(KiwiHttpResponses.serverError(statusCode))
                    .isEqualTo(firstDigitInFamily(statusCode) == 5),

            () -> assertThat(KiwiHttpResponses.otherFamily(statusCode))
                    .isEqualTo(statusCode < 100 || statusCode > 599),

            () -> assertThat(KiwiHttpResponses.ok(statusCode))
                    .isEqualTo(statusCode == 200),

            () -> assertThat(KiwiHttpResponses.created(statusCode))
                    .isEqualTo(statusCode == 201),

            () -> assertThat(KiwiHttpResponses.accepted(statusCode))
                    .isEqualTo(statusCode == 202),

            () -> assertThat(KiwiHttpResponses.noContent(statusCode))
                    .isEqualTo(statusCode == 204),

            () -> assertThat(KiwiHttpResponses.resetContent(statusCode))
                    .isEqualTo(statusCode == 205),

            () -> assertThat(KiwiHttpResponses.partialContent(statusCode))
                    .isEqualTo(statusCode == 206),

            () -> assertThat(KiwiHttpResponses.movedPermanently(statusCode))
                    .isEqualTo(statusCode == 301),

            () -> assertThat(KiwiHttpResponses.found(statusCode))
                    .isEqualTo(statusCode == 302),

            () -> assertThat(KiwiHttpResponses.seeOther(statusCode))
                    .isEqualTo(statusCode == 303),

            () -> assertThat(KiwiHttpResponses.notModified(statusCode))
                    .isEqualTo(statusCode == 304),

            () -> assertThat(KiwiHttpResponses.temporaryRedirect(statusCode))
                    .isEqualTo(statusCode == 307),

            () -> assertThat(KiwiHttpResponses.permanentRedirect(statusCode))
                    .isEqualTo(statusCode == 308),

            () -> assertThat(KiwiHttpResponses.badRequest(statusCode))
                    .isEqualTo(statusCode == 400),

            () -> assertThat(KiwiHttpResponses.unauthorized(statusCode))
                    .isEqualTo(statusCode == 401),

            () -> assertThat(KiwiHttpResponses.paymentRequired(statusCode))
                    .isEqualTo(statusCode == 402),

            () -> assertThat(KiwiHttpResponses.forbidden(statusCode))
                    .isEqualTo(statusCode == 403),

            () -> assertThat(KiwiHttpResponses.notFound(statusCode))
                    .isEqualTo(statusCode == 404),

            () -> assertThat(KiwiHttpResponses.methodNotAllowed(statusCode))
                    .isEqualTo(statusCode == 405),

            () -> assertThat(KiwiHttpResponses.notAcceptable(statusCode))
                    .isEqualTo(statusCode == 406),

            () -> assertThat(KiwiHttpResponses.proxyAuthenticationRequired(statusCode))
                    .isEqualTo(statusCode == 407),

            () -> assertThat(KiwiHttpResponses.requestTimeout(statusCode))
                    .isEqualTo(statusCode == 408),

            () -> assertThat(KiwiHttpResponses.conflict(statusCode))
                    .isEqualTo(statusCode == 409),

            () -> assertThat(KiwiHttpResponses.gone(statusCode))
                    .isEqualTo(statusCode == 410),

            () -> assertThat(KiwiHttpResponses.lengthRequired(statusCode))
                    .isEqualTo(statusCode == 411),

            () -> assertThat(KiwiHttpResponses.preconditionFailed(statusCode))
                    .isEqualTo(statusCode == 412),

            () -> assertThat(KiwiHttpResponses.requestEntityTooLarge(statusCode))
                    .isEqualTo(statusCode == 413),

            () -> assertThat(KiwiHttpResponses.requestUriTooLong(statusCode))
                    .isEqualTo(statusCode == 414),

            () -> assertThat(KiwiHttpResponses.unsupportedMediaType(statusCode))
                    .isEqualTo(statusCode == 415),

            () -> assertThat(KiwiHttpResponses.requestedRangeNotSatisfiable(statusCode))
                    .isEqualTo(statusCode == 416),

            () -> assertThat(KiwiHttpResponses.expectationFailed(statusCode))
                    .isEqualTo(statusCode == 417),

            () -> assertThat(KiwiHttpResponses.iAmATeapot(statusCode))
                    .isEqualTo(statusCode == 418),

            () -> assertThat(KiwiHttpResponses.unprocessableContent(statusCode))
                    .isEqualTo(statusCode == 422),

            () -> assertThat(KiwiHttpResponses.upgradeRequired(statusCode))
                    .isEqualTo(statusCode == 426),

            () -> assertThat(KiwiHttpResponses.preconditionRequired(statusCode))
                    .isEqualTo(statusCode == 428),

            () -> assertThat(KiwiHttpResponses.tooManyRequests(statusCode))
                    .isEqualTo(statusCode == 429),

            () -> assertThat(KiwiHttpResponses.requestHeaderFieldsTooLarge(statusCode))
                    .isEqualTo(statusCode == 431),

            () -> assertThat(KiwiHttpResponses.internalServerError(statusCode))
                    .isEqualTo(statusCode == 500),

            () -> assertThat(KiwiHttpResponses.notImplemented(statusCode))
                    .isEqualTo(statusCode == 501),

            () -> assertThat(KiwiHttpResponses.badGateway(statusCode))
                    .isEqualTo(statusCode == 502),

            () -> assertThat(KiwiHttpResponses.serviceUnavailable(statusCode))
                    .isEqualTo(statusCode == 503),

            () -> assertThat(KiwiHttpResponses.gatewayTimeout(statusCode))
                    .isEqualTo(statusCode == 504),

            () -> assertThat(KiwiHttpResponses.httpVersionNotSupported(statusCode))
                    .isEqualTo(statusCode == 505),

            () -> assertThat(KiwiHttpResponses.networkAuthenticationRequired(statusCode))
                    .isEqualTo(statusCode == 511)
        );
    }

    private static int firstDigitInFamily(int statusCode) {
        return statusCode / 100;
    }
}
