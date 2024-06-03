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
     * Check if the given status code is 308 Permanent Redirect.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 308, otherwise false
     */
    public static boolean permanentRedirect(int statusCode) {
        return statusCode == 308;
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
     * Check if the given status code is 402 Payment Required.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 402, otherwise false
     */
    public static boolean paymentRequired(int statusCode) {
        return statusCode == 402;
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
     * Check if the given status code is 413 Payload Too Large.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 413, otherwise false
     */
    public static boolean payloadTooLarge(int statusCode) {
        return statusCode == 413;
    }

    /**
     * Check if the given status code is 414 URI Too Long.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 414, otherwise false
     */
    public static boolean uriTooLong(int statusCode) {
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
     * Check if the given status code is 416 Range Not Satisfiable.
     *
     * @param statusCode the status code to check
     * @return true if the status code is 416, otherwise false
     */
    public static boolean rangeNotSatisfiable(int statusCode) {
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

    /**
     * Get the human-readable (though not necessarily understandable)
     * "reason phrase" for the given status code.
     *
     * @param statusCode the status code
     * @return the reason phrase of the status code, or "Unknown" if the
     * code is not known (it may or may not be a valid HTTP status code,
     * or could be a custom code)
     */
    public static String reasonPhraseOf(int statusCode) {
        return switch (statusCode) {

            // Informational responses
            case 100 -> "Continue";
            case 101 -> "Switching Protocols";
            case 102 -> "Processing";  // WebDAV
            case 103 -> "Early Hints";

            // Successful responses
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 203 -> "Non-Authoritative Information";
            case 204 -> "No Content";
            case 205 -> "Reset Content";
            case 206 -> "Partial Content";
            case 207 -> "Multi-Status";  // WebDAV
            case 208 -> "Already Reported";  // WebDAV
            case 226 -> "IM Used";  // HTTP Delta encoding

            // Redirection messages
            case 300 -> "Multiple Choices";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 303 -> "See Other";
            case 304 -> "Not Modified";
            case 305 -> "Use Proxy";  // deprecated
            case 306 -> "unused";  // no longer used, but still reserved
            case 307 -> "Temporary Redirect";
            case 308 -> "Permanent Redirect";

            // Client error responses
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 402 -> "Payment Required";  // experimental
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 406 -> "Not Acceptable";
            case 407 -> "Proxy Authentication Required";
            case 408 -> "Request Timeout";
            case 409 -> "Conflict";
            case 410 -> "Gone";
            case 411 -> "Length Required";
            case 412 -> "Precondition Failed";
            case 413 -> "Payload Too Large";
            case 414 -> "URI Too Long";
            case 415 -> "Unsupported Media Type";
            case 416 -> "Range Not Satisfiable";
            case 417 -> "Expectation Failed";
            case 418 -> "I'm a teapot";
            case 421 -> "Misdirected Request";
            case 422 -> "Unprocessable Content";  // WebDAV
            case 423 -> "Locked";  // WebDAV
            case 424 -> "Failed Dependency";  // WebDAV
            case 425 -> "Too Early";  // experimental
            case 426 -> "Upgrade Required";
            case 428 -> "Precondition Required";
            case 429 -> "Too Many Requests";
            case 431 -> "Request Header Fields Too Large";
            case 451 -> "Unavailable For Legal Reasons";

            // Server error responses
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            case 504 -> "Gateway Timeout";
            case 505 -> "HTTP Version Not Supported";
            case 506 -> "Variant Also Negotiates";
            case 507 -> "Insufficient Storage";  // WebDAV
            case 508 -> "Loop Detected";  // WebDAV
            case 510 -> "Not Extended";
            case 511 -> "Network Authentication Required";

            // Something else that might have been added...
            default -> "Unknown";
        };
    }
}
