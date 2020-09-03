package org.kiwiproject.jaxrs;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Optional;

/**
 * Static utilities related to JAX-RS responses.
 */
@UtilityClass
@Slf4j
public class KiwiResponses {

    /**
     * Return a media type suitable for use as the value of a corresponding HTTP header. This will consist
     * of the primary type and and subtype, e.g. {@code application/json}.
     *
     * @param response the response object
     * @return Optional that may or may not contain a media type
     * @see MediaType#toString()
     * @see MediaType#getType()
     * @see MediaType#getSubtype()
     */
    public static Optional<String> mediaType(Response response) {
        checkArgumentNotNull(response);

        return Optional.ofNullable(response.getMediaType()).map(Object::toString);
    }

    /**
     * Check if the given response has a status code in {@link Family#SUCCESSFUL}.
     * <p>
     * <strong>NOTE:</strong> This does <em>not</em> close the {@link Response} or read the response entity.
     *
     * @param response the response object
     * @return true if the response is successful, false otherwise
     * @see Family#SUCCESSFUL
     */
    public static boolean successful(Response response) {
        return successful(response.getStatus());
    }

    /**
     * Check if the given response has a status code that is <em>not</em> in {@link Family#SUCCESSFUL}.
     * <p>
     * <strong>NOTE:</strong> This does <em>not</em> close the {@link Response} or read the response entity.
     *
     * @param response the response object
     * @return true if the response is <em>not</em> successful, false otherwise
     */
    public static boolean notSuccessful(Response response) {
        return notSuccessful(response.getStatus());
    }

    /**
     * Check if the given status code is in {@link Family#SUCCESSFUL}.
     *
     * @param status the response Status object
     * @return true if the status indicates success, false otherwise
     */
    public static boolean successful(Response.Status status) {
        return successful(status.getStatusCode());
    }

    /**
     * Check if the given status code is <em>not</em> in {@link Family#SUCCESSFUL}.
     *
     * @param status the response Status object
     * @return true if the status does <em>not</em> indicate success, false otherwise
     */
    public static boolean notSuccessful(Response.Status status) {
        return notSuccessful(status.getStatusCode());
    }

    /**
     * Check if the given status type is in {@link Family#SUCCESSFUL}.
     *
     * @param status the response StatusType object
     * @return true if the status indicates success, false otherwise
     */
    public static boolean successful(Response.StatusType status) {
        return successful(status.getStatusCode());
    }

    /**
     * Check if the given status type is <em>not</em> in {@link Family#SUCCESSFUL}.
     *
     * @param status the response StatusType object
     * @return true if the status does <em>not</em> indicate success, false otherwise
     */
    public static boolean notSuccessful(Response.StatusType status) {
        return notSuccessful(status.getStatusCode());
    }

    /**
     * Check if the given status code is in {@link Family#SUCCESSFUL}.
     *
     * @param statusCode the response status code
     * @return true if the status code indicates success, false otherwise
     */
    public static boolean successful(int statusCode) {
        return successful(Family.familyOf(statusCode));
    }

    /**
     * Check if the given status code is <em>not</em> in {@link Family#SUCCESSFUL}.
     *
     * @param statusCode the response status code
     * @return true if the status code doe <em>not</em> indicate success, false otherwise
     */
    public static boolean notSuccessful(int statusCode) {
        return !successful(statusCode);
    }

    /**
     * Check if the given response family is {@link Family#SUCCESSFUL}.
     *
     * @param family the response family
     * @return true if the family is successful, false otherwise
     */
    public static boolean successful(Family family) {
        return family == Family.SUCCESSFUL;
    }

    /**
     * Check if the given response family is <em>not</em> {@link Family#SUCCESSFUL}.
     *
     * @param family the response family
     * @return true if the family is <em>not</em> successful, false otherwise
     */
    public static boolean notSuccessful(Family family) {
        return !successful(family);
    }

    /**
     * Check if the given response has a status code in {@link Family#SUCCESSFUL}, <em>then close the response</em>.
     * <p>
     * <strong>NOTE:</strong> Closes the response after performing the check.
     *
     * @param response the response object
     * @return true if the response is successful, false otherwise
     */
    public static boolean successfulAlwaysClosing(Response response) {
        checkArgumentNotNull(response, "response cannot be null");
        try {
            return successful(response);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Check if the given response has a status code that is <em>not</em> in {@link Family#SUCCESSFUL},
     * <em>then close the response</em>.
     * <p>
     * <strong>NOTE:</strong> Closes the response after performing the check.
     *
     * @param response the response object
     * @return true if the response is <em>not</em> successful, false otherwise
     */
    public static boolean notSuccessfulAlwaysClosing(Response response) {
        return !successfulAlwaysClosing(response);
    }

    /**
     * Closes the given {@link Response}, which can be {@code null}, swallowing any exceptions and logging them
     * at INFO level.
     *
     * @param response the response object
     */
    public static void closeQuietly(Response response) {
        if (nonNull(response)) {
            try {
                response.close();
            } catch (Exception e) {
                LOG.info("Error closing response", e);
            }
        }
    }

    /**
     * Check if the given response has status 200 OK.
     *
     * @param response the response object
     * @return true if the response status is 200 OK, false otherwise
     */
    public static boolean ok(Response response) {
        return hasStatus(response, Response.Status.OK);
    }

    /**
     * Check if the given response has status 201 Created.
     *
     * @param response the response object
     * @return true if the response status is 201 Created, false otherwise
     */
    public static boolean created(Response response) {
        return hasStatus(response, Response.Status.CREATED);
    }

    /**
     * Check if the given response has status 404 Not Found.
     *
     * @param response the response object
     * @return true if the response status is 404 Not Found, false otherwise
     */
    public static boolean notFound(Response response) {
        return hasStatus(response, Response.Status.NOT_FOUND);
    }

    /**
     * Check if the given response has status 500 Internal Server Error.
     *
     * @param response the response object
     * @return true if the response status is 500 Internal Server Error, false otherwise
     */
    public static boolean internalServerError(Response response) {
        return hasStatus(response, Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Check if the given response has the expected status.
     *
     * @param response the response object
     * @param status   the expected status
     * @return true if the response has the given status, false otherwise
     */
    public static boolean hasStatus(Response response, Response.Status status) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(status);

        return response.getStatusInfo().getStatusCode() == status.getStatusCode();
    }

    /**
     * Check if the given response is in the INFORMATIONAL (1xx codes) family.
     *
     * @param response the response object
     * @return true if the response is in the INFORMATIONAL family, false otherwise
     * @see Family#INFORMATIONAL
     */
    public static boolean informational(Response response) {
        return hasFamily(response, Family.INFORMATIONAL);
    }

    /**
     * Check if the given response is in the REDIRECTION (3xx codes) family.
     *
     * @param response the response object
     * @return true if the response is in the REDIRECTION family, false otherwise
     * @see Family#REDIRECTION
     */
    public static boolean redirection(Response response) {
        return hasFamily(response, Family.REDIRECTION);
    }

    /**
     * Check if the given response is in the CLIENT_ERROR (4xx codes) family.
     *
     * @param response the response object
     * @return true if the response is in the CLIENT_ERROR family, false otherwise
     * @see Family#CLIENT_ERROR
     */
    public static boolean clientError(Response response) {
        return hasFamily(response, Family.CLIENT_ERROR);
    }

    /**
     * Check if the given response is in the SERVER_ERROR (5xx codes) family.
     *
     * @param response the response object
     * @return true if the response is in the SERVER_ERROR family, false otherwise
     * @see Family#SERVER_ERROR
     */
    public static boolean serverError(Response response) {
        return hasFamily(response, Family.SERVER_ERROR);
    }

    /**
     * Check if the given response is in the OTHER (unrecognized status codes) family.
     *
     * @param response the response object
     * @return true if the response is in the OTHER family, false otherwise
     * @see Family#OTHER
     */
    public static boolean otherFamily(Response response) {
        return hasFamily(response, Family.OTHER);
    }

    /**
     * Check if the given response has the expected family.
     *
     * @param response the response object
     * @param family   the expected family
     * @return true if the response has the given status, false otherwise
     */
    public static boolean hasFamily(Response response, Family family) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(family);

        return response.getStatusInfo().getFamily() == family;
    }

}
