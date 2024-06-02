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
     * Check if the given status code is 304 Not Modified.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 304, otherwise false
     */
    public static boolean notModified(int statusCode) {
        return statusCode == 304;
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
     * Check if the given status code is 409 Conflict.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 409, otherwise false
     */
    public static boolean conflict(int statusCode) {
        return statusCode == 409;
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
}
