package org.kiwiproject.net;

import lombok.experimental.UtilityClass;

/**
 * Static utilities related to HTTP responses, mainly for checking
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">status codes</a>.
 * <p>
 * The utilities here mainly make reading code more pleasant, since they
 * use words instead of just HTTP status codes.
 * <p>
 * This has no dependencies on any specific web or REST libraries.
 * If you are using Jakarta RESTful Web Services, you can also use
 * {@link org.kiwiproject.jaxrs.KiwiResponses}.
 */
@UtilityClass
public class KiwiHttpResponses {

    /**
     * Check if the given status code is in the informational family (1xx codes).
     *
     * @param statusCode the status code to check
     * @return true if the status code is 100-199, otherwise false
     */
    public static boolean informational(int statusCode) {
        return isInFamily(statusCode, 1);
    }

    /**
     * Check if the given status code is in the successful family (2xx codes).
     *
     * @param statusCode the status code to check
     * @return true if the status code is 200-299, otherwise false
     */
    public static boolean successful(int statusCode) {
        return isInFamily(statusCode, 2);
    }

    /**
     * Check if the given status code is not in the successful family (2xx codes).
     *
     * @param statusCode the status code to check
     * @return true if the status code is outside the range 200-299, otherwise false
     */
    public static boolean notSuccessful(int statusCode) {
        return !successful(statusCode);
    }

        /**
     * Check if the given status code is in the redirection family (3xx codes).
     *
     * @param statusCode the status code to check
     * @return true if the status code is 300-399, otherwise false
     */
    public static boolean redirection(int statusCode) {
        return isInFamily(statusCode, 3);
    }

    /**
     * Check if the given status code is in the client error family (4xx codes).
     *
     * @param statusCode the status code to check
     * @return true if the status code is 400-499, otherwise false
     */
    public static boolean clientError(int statusCode) {
        return isInFamily(statusCode, 4);
    }

    /**
     * Check if the given status code is in the server error family (5xx codes).
     *
     * @param statusCode the status code to check
     * @return true if the status code is 500-599, otherwise false
     */
    public static boolean serverError(int statusCode) {
        return isInFamily(statusCode, 5);
    }

    /**
     * Check if the given status code is not in a known family.
     *
     * @param statusCode the status code to check
     * @return true if the status code is outside the range 100-599, otherwise false
     */
    public static boolean otherFamily(int statusCode) {
        var firstDigit = firstDigitInFamily(statusCode);
        return firstDigit < 1 || firstDigit > 5;
    }

    private static boolean isInFamily(int statusCode, int expectedFirstDigit) {
        return firstDigitInFamily(statusCode) == expectedFirstDigit;
    }

    private static int firstDigitInFamily(int statusCode) {
        return statusCode / 100;
    }

    /**
     * Check if the given status code is 200 OK.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 200, otherwise false
     */
    public static boolean ok(int statusCode) {
        return statusCode == 200;
    }

    /**
     * Check if the given status code is 201 Created.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 201, otherwise false
     */
    public static boolean created(int statusCode) {
        return statusCode == 201;
    }

    /**
     * Check if the given status code is 202 Accepted.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 202, otherwise false
     */
    public static boolean accepted(int statusCode) {
        return statusCode == 202;
    }

    /**
     * Check if the given status code is 204 No Content.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 204, otherwise false
     */
    public static boolean noContent(int statusCode) {
        return statusCode == 204;
    }

    /**
     * Check if the given status code is 205 Reset Content.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 205, otherwise false
     */
    public static boolean resetContent(int statusCode) {
        return statusCode == 205;
    }

    /**
     * Check if the given status code is 206 Partial Content.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 206, otherwise false
     */
    public static boolean partialContent(int statusCode) {
        return statusCode == 206;
    }

    /**
     * Check if the given status code is 301 Moved Permanently.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 301, otherwise false
     */
    public static boolean movedPermanently(int statusCode) {
        return statusCode == 301;
    }

    /**
     * Check if the given status code is 302 Found.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 302, otherwise false
     */
    public static boolean found(int statusCode) {
        return statusCode == 302;
    }

    /**
     * Check if the given status code is 303 See Other.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 303, otherwise false
     */
    public static boolean seeOther(int statusCode) {
        return statusCode == 303;
    }

    /**
     * Check if the given status code is 304 Not Modified.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 304, otherwise false
     */
    public static boolean notModified(int statusCode) {
        return statusCode == 304;
    }

    /**
     * Check if the given status code is 307 Temporary Redirect.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 307, otherwise false
     */
    public static boolean temporaryRedirect(int statusCode) {
        return statusCode == 307;
    }

    /**
     * Check if the given status code is 400 Bad Request.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 400, otherwise false
     */
    public static boolean badRequest(int statusCode) {
        return statusCode == 400;
    }

    /**
     * Check if the given status code is 401 Unauthorized.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 401, otherwise false
     */
    public static boolean unauthorized(int statusCode) {
        return statusCode == 401;
    }

    /**
     * Check if the given status code is 403 Forbidden.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 403, otherwise false
     */
    public static boolean forbidden(int statusCode) {
        return statusCode == 403;
    }

    /**
     * Check if the given status code is 404 Not Found.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 404, otherwise false
     */
    public static boolean notFound(int statusCode) {
        return statusCode == 404;
    }

    /**
     * Check if the given status code is 405 Method Not Allowed.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 405, otherwise false
     */
    public static boolean methodNotAllowed(int statusCode) {
        return statusCode == 405;
    }

    /**
     * Check if the given status code is 406 Not Acceptable.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 406, otherwise false
     */
    public static boolean notAcceptable(int statusCode) {
        return statusCode == 406;
    }

    /**
     * Check if the given status code is 407 Proxy Authentication Required.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 407, otherwise false
     */
    public static boolean proxyAuthenticationRequired(int statusCode) {
        return statusCode == 407;
    }

    /**
     * Check if the given status code is 408 Request Timeout.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 408, otherwise false
     */
    public static boolean requestTimeout(int statusCode) {
        return statusCode == 408;
    }

    /**
     * Check if the given status code is 409 Conflict.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 409, otherwise false
     */
    public static boolean conflict(int statusCode) {
        return statusCode == 409;
    }

    /**
     * Check if the given status code is 410 Gone.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 410, otherwise false
     */
    public static boolean gone(int statusCode) {
        return statusCode == 410;
    }

    /**
     * Check if the given status code is 411 Length Required.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 411, otherwise false
     */
    public static boolean lengthRequired(int statusCode) {
        return statusCode == 411;
    }

    /**
     * Check if the given status code is 412 Precondition Failed.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 412, otherwise false
     */
    public static boolean preconditionFailed(int statusCode) {
        return statusCode == 412;
    }

    /**
     * Check if the given status code is 413 Request Entity Too Large.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 413, otherwise false
     */
    public static boolean requestEntityTooLarge(int statusCode) {
        return statusCode == 413;
    }

    /**
     * Check if the given status code is 414 Request-URI Too Long.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 414, otherwise false
     */
    public static boolean requestUriTooLong(int statusCode) {
        return statusCode == 414;
    }

    /**
     * Check if the given status code is 415 Unsupported Media Type.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 415, otherwise false
     */
    public static boolean unsupportedMediaType(int statusCode) {
        return statusCode == 415;
    }

    /**
     * Check if the given status code is 416 Requested Range Not Satisfiable.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 416, otherwise false
     */
    public static boolean requestedRangeNotSatisfiable(int statusCode) {
        return statusCode == 416;
    }

    /**
     * Check if the given status code is 417 Expectation Failed.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 417, otherwise false
     */
    public static boolean expectationFailed(int statusCode) {
        return statusCode == 417;
    }

    /**
     * Check if the given status code is 418 I'm a teapot.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 418, otherwise false
     */
    public static boolean iAmATeapot(int statusCode) {
        return statusCode == 418;
    }

    /**
     * Check if the given status code is 422 Unprocessable Content.
     * <p>
     * This is technically a WebDAV code, but is used by many web
     * frameworks to indicate an input validation failure.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 422, otherwise false
     */
    public static boolean unprocessableContent(int statusCode) {
        return statusCode == 422;
    }

    /**
     * Check if the given status code is 426 Upgrade Required.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 426, otherwise false
     */
    public static boolean upgradeRequired(int statusCode) {
        return statusCode == 426;
    }

    /**
     * Check if the given status code is 428 Precondition Required.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 428, otherwise false
     */
    public static boolean preconditionRequired(int statusCode) {
        return statusCode == 428;
    }

    /**
     * Check if the given status code is 429 Too Many Requests.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 429, otherwise false
     */
    public static boolean tooManyRequests(int statusCode) {
        return statusCode == 429;
    }

    /**
     * Check if the given status code is 431 Request Header Fields Too Large.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 431, otherwise false
     */
    public static boolean requestHeaderFieldsTooLarge(int statusCode) {
        return statusCode == 431;
    }

    /**
     * Check if the given status code is 500 Internal Server Error.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 500, otherwise false
     */
    public static boolean internalServerError(int statusCode) {
        return statusCode == 500;
    }

     /**
     * Check if the given status code is 501 Not Implemented.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 501, otherwise false
     */
    public static boolean notImplemented(int statusCode) {
        return statusCode == 501;
    }

    /**
     * Check if the given status code is 502 Bad Gateway.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 502, otherwise false
     */
    public static boolean badGateway(int statusCode) {
        return statusCode == 502;
    }

    /**
     * Check if the given status code is 503 Service Unavailable.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 503, otherwise false
     */
    public static boolean serviceUnavailable(int statusCode) {
        return statusCode == 503;
    }

    /**
     * Check if the given status code is 504 Gateway Timeout.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 504, otherwise false
     */
    public static boolean gatewayTimeout(int statusCode) {
        return statusCode == 504;
    }

    /**
     * Check if the given status code is 505 HTTP Version Not Supported.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 505, otherwise false
     */
    public static boolean httpVersionNotSupported(int statusCode) {
        return statusCode == 505;
    }

    /**
     * Check if the given status code is 511 Network Authentication Required.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 511, otherwise false
     */
    public static boolean networkAuthenticationRequired(int statusCode) {
        return statusCode == 511;
    }
}
