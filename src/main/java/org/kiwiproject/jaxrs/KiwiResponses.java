package org.kiwiproject.jaxrs;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.checkOnlyOneArgumentIsNull;

import com.google.common.annotations.VisibleForTesting;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Static utilities related to evaluating and acting upon Jakarta REST responses. For example, this class contains
 * utilities to determine whether responses are successful or not, whether they are a specific type of response, and to
 * perform actions (or throw exceptions) based on success or failure.
 * <p>
 * These utilities are intended mainly to be used in classes that make HTTP requests and need to evaluate and/or take
 * action with the HTTP responses.
 *
 * @see KiwiResources
 * @see KiwiStandardResponses
 */
@UtilityClass
@Slf4j
public class KiwiResponses {

    private static final Consumer<Response> NO_OP_RESPONSE_CONSUMER = new NoOpResponseConsumer();

    /**
     * Return a media type suitable for use as the value of a corresponding HTTP header. This will consist
     * of the primary type and subtype, e.g. {@code application/json}.
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

    /**
     * Given a {@link Response} Supplier, perform an action depending on whether it was
     * successful ({@code successConsumer}), failed ({@code failureConsumer}), or if the
     * Supplier threw an exception ({@code exceptionConsumer}).
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier  a Supplier that provides the response
     * @param successConsumer   the action to run if the response is successful
     * @param failureConsumer    the action to run if the response is not successful
     * @param exceptionConsumer the action to run if the Supplier throws an exception
     */
    public static void onSuccessOrFailure(Supplier<Response> responseSupplier,
                                          Consumer<Response> successConsumer,
                                          Consumer<Response> failureConsumer,
                                          Consumer<RuntimeException> exceptionConsumer) {

        checkArgumentNotNull(responseSupplier);
        checkArgumentNotNull(exceptionConsumer);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            onSuccessOrFailure(result.response(), successConsumer, failureConsumer);
        } else {
            exceptionConsumer.accept(result.error());
        }
    }

    /**
     * Given a {@link Response}, perform an action depending on whether it was successful ({@code successConsumer})
     * or failed ({@code failureConsumer}).
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response        the response object
     * @param successConsumer the action to run if the response is successful
     * @param failureConsumer  the action to run if the response is not successful
     */
    public static void onSuccessOrFailure(Response response,
                                          Consumer<Response> successConsumer,
                                          Consumer<Response> failureConsumer) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(successConsumer);
        checkArgumentNotNull(failureConsumer);

        try {
            if (successful(response)) {
                successConsumer.accept(response);
            } else {
                failureConsumer.accept(response);
            }
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Given a {@link Response} Supplier, perform an action if it was successful ({@code successConsumer}.
     * If the response was unsuccessful, throw the exception supplied by {@code throwingFun}.
     * If the Supplier throws an exception, then that exception is rethrown.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param successConsumer  the action to run if the response is successful
     * @param throwingFun      a function that creates an appropriate (subclass of) RuntimeException
     * @throws RuntimeException the result of {@code throwingFun}, or the exception thrown by the Supplier
     */
    public static void onSuccessOrFailureThrow(Supplier<Response> responseSupplier,
                                               Consumer<Response> successConsumer,
                                               Function<Response, ? extends RuntimeException> throwingFun) {

        checkArgumentNotNull(responseSupplier);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            onSuccessOrFailureThrow(responseSupplier.get(), successConsumer, throwingFun);
        } else {
            throw result.error();
        }
    }

    /**
     * Given a {@link Response}, perform an action if it was successful ({@code successConsumer} or throw an
     * exception supplied by {@code throwingFun}.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response        the response object
     * @param successConsumer the action to run if the response is successful
     * @param throwingFun     a function that creates an appropriate (subclass of) RuntimeException
     * @throws RuntimeException the result of {@code throwingFun}
     */
    public static void onSuccessOrFailureThrow(Response response,
                                               Consumer<Response> successConsumer,
                                               Function<Response, ? extends RuntimeException> throwingFun) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(successConsumer);
        checkArgumentNotNull(throwingFun);

        try {
            if (successful(response)) {
                successConsumer.accept(response);
            } else {
                throw throwingFun.apply(response);
            }
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Given a {@link Response} Supplier, perform an action only if it was successful ({@code successConsumer}.
     * <p>
     * <em>No action is performed for an unsuccessful response, and exceptions thrown by the Supplier are ignored.</em>
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param successConsumer  the action to run if the response is successful
     */
    public static void onSuccess(Supplier<Response> responseSupplier,
                                 Consumer<Response> successConsumer) {

        checkArgumentNotNull(responseSupplier);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            onSuccess(result.response(), successConsumer);
        }
    }

    /**
     * Given a {@link Response}, perform an action only if it was successful ({@code successConsumer}.
     * <p>
     * <em>No action is performed for an unsuccessful response.</em>
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response        the response object
     * @param successConsumer the action to run if the response is successful
     */
    public static void onSuccess(Response response, Consumer<Response> successConsumer) {
        onSuccessOrFailure(response, successConsumer, NO_OP_RESPONSE_CONSUMER);
    }

    /**
     * Given a {@link Response} Supplier, perform an action that returns a result only if it was
     * successful ({@code successFun}).
     * <p>
     * <em>No action is performed for an unsuccessful response, and exceptions
     * thrown by the Supplier are ignored.</em>
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param successFun       the function to apply if the response is successful
     * @param <T>              the result type
     * @return an Optional containing a result for successful responses, or an empty Optional
     */
    public static <T> Optional<T> onSuccessWithResult(Supplier<Response> responseSupplier,
                                                      Function<Response, T> successFun) {

        checkArgumentNotNull(responseSupplier);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            return onSuccessWithResult(result.response(), successFun);
        }

        return Optional.empty();
    }

    /**
     * Given a {@link Response}, perform an action that returns a result only if it was successful ({@code successFun}).
     * <p>
     * <em>No action is performed for an unsuccessful response.</em>
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response   the response object
     * @param successFun the function to apply if the response is successful
     * @param <T>        the result type
     * @return an Optional containing a result for successful responses, or an empty Optional
     */
    public static <T> Optional<T> onSuccessWithResult(Response response, Function<Response, T> successFun) {
        return onSuccessWithResultOrFailure(response, successFun, NO_OP_RESPONSE_CONSUMER);
    }

    /**
     * Given a {@link Response} Supplier, perform an action only if it was
     * <em>not</em> successful ({@code failureConsumer}), or if the Supplier
     * threw an exception ({@code exceptionConsumer}).
     * <p>
     * No action is performed for a successful response.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier  a Supplier that provides the response
     * @param failureConsumer    the action to run if the response is not successful
     * @param exceptionConsumer the action to run if the Supplier throws an exception
     */
    public static void onFailure(Supplier<Response> responseSupplier,
                                 Consumer<Response> failureConsumer,
                                 Consumer<RuntimeException> exceptionConsumer) {

        checkArgumentNotNull(responseSupplier);
        checkArgumentNotNull(exceptionConsumer);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            onFailure(result.response(), failureConsumer);
        } else {
            exceptionConsumer.accept(result.error());
        }
    }

    /**
     * Given a {@link Response}, perform an action only if it was <em>not</em> successful ({@code failureConsumer}).
     * <p>
     * No action is performed for a successful response.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response       the response object
     * @param failureConsumer the action to run if the response is not successful
     */
    public static void onFailure(Response response, Consumer<Response> failureConsumer) {
        onSuccessOrFailure(response, NO_OP_RESPONSE_CONSUMER, failureConsumer);
    }

    /**
     * Given a {@link Response} Supplier, throw a (subclass of) {@link RuntimeException} for failed
     * responses using {@code throwingFun}.
     * If the Supplier throws an exception, that exception is rethrown.
     * <p>
     * No action is performed for a successful response.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param throwingFun      function that creates an appropriate (subclass of) RuntimeException
     * @throws RuntimeException the result of {@code throwingFun}, or the exception thrown by the Supplier
     */
    public static void onFailureThrow(Supplier<Response> responseSupplier,
                                      Function<Response, ? extends RuntimeException> throwingFun) {

        checkArgumentNotNull(responseSupplier);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            onFailureThrow(result.response(), throwingFun);
        } else {
            throw result.error();
        }
    }

    /**
     * Given a {@link Response}, throw a (subclass of) {@link RuntimeException} for failed responses using
     * {@code throwingFun}.
     * <p>
     * No action is performed for a successful response.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response    the response object
     * @param throwingFun a function that creates an appropriate (subclass of) RuntimeException
     * @throws RuntimeException the result of {@code throwingFun}
     */
    public static void onFailureThrow(Response response,
                                      Function<Response, ? extends RuntimeException> throwingFun) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(throwingFun);

        try {
            if (notSuccessful(response)) {
                throw throwingFun.apply(response);
            }
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Given a {@link Response} Supplier, perform an action that returns a result if the response was
     * successful ({@code successFun}). Perform an action if the response was unsuccessful ({@code failureConsumer},
     * or if the Supplier threw an exception ({@code exceptionConsumer}).
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier  a Supplier that provides the response
     * @param successFun        the function to apply if the response is successful
     * @param failureConsumer    the action to run if the response is not successful
     * @param exceptionConsumer the action to run if the Supplier throws an exception
     * @param <T>               the result type
     * @return the result from {@code successFun} for successful responses, or an empty Optional
     * for unsuccessful responses or if the Supplier throws an exception
     */
    public static <T> Optional<T> onSuccessWithResultOrFailure(Supplier<Response> responseSupplier,
                                                               Function<Response, T> successFun,
                                                               Consumer<Response> failureConsumer,
                                                               Consumer<RuntimeException> exceptionConsumer) {

        checkArgumentNotNull(responseSupplier);
        checkArgumentNotNull(exceptionConsumer);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            return onSuccessWithResultOrFailure(result.response(), successFun, failureConsumer);
        } else {
            exceptionConsumer.accept(result.error());
        }

        return Optional.empty();
    }

    /**
     * Given a {@link Response}, perform an action that returns a result if the response was
     * successful ({@code successFun}) or perform an action if the response was unsuccessful ({@code failureConsumer}.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response       the response object
     * @param successFun     the function to apply if the response is successful
     * @param failureConsumer the action to run if the response is not successful
     * @param <T>            the result type
     * @return the result from {@code successFun} for successful responses, or an empty Optional for unsuccessful ones
     */
    public static <T> Optional<T> onSuccessWithResultOrFailure(Response response,
                                                               Function<Response, T> successFun,
                                                               Consumer<Response> failureConsumer) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(successFun);
        checkArgumentNotNull(failureConsumer);

        T result = null;
        try {
            if (successful(response)) {
                result = successFun.apply(response);
            } else {
                failureConsumer.accept(response);
            }
        } finally {
            closeQuietly(response);
        }

        return Optional.ofNullable(result);
    }

    /**
     * Given a {@link Response} Supplier, perform an action that returns a result if the response was
     * successful ({@code successFun}. If the response was not successful return the result
     * of a function ({@code failedFun}). If the Supplier threw an exception, return the result
     * of a different function ({@code exceptionFun}).
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param successFun       the function to apply if the response is successful
     * @param failedFun        the function to apply if the response is not successful
     * @param exceptionFun     the function to apply if the Supplier throws an exception
     * @param <T>              the result type
     * @return the result from applying {@code successFun}, {@code failedFun}, or {@code exceptionFun}
     */
    public static <T> T onSuccessOrFailureWithResult(Supplier<Response> responseSupplier,
                                                     Function<Response, T> successFun,
                                                     Function<Response, T> failedFun,
                                                     Function<RuntimeException, T> exceptionFun) {

        checkArgumentNotNull(responseSupplier);
        checkArgumentNotNull(exceptionFun);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            return onSuccessOrFailureWithResult(result.response(), successFun, failedFun);
        }

        return exceptionFun.apply(result.error());
    }

    /**
     * Given a {@link Response}, perform an action that returns a result if the response was
     * successful ({@code successFun}) or if not successful ({@code failedFun}).
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response   the response object
     * @param successFun the function to apply if the response is successful
     * @param failedFun  the function to apply if the response is not successful
     * @param <T>        the result type
     * @return the result from applying either {@code successFun} or {@code failedFun}
     */
    public static <T> T onSuccessOrFailureWithResult(Response response,
                                                     Function<Response, T> successFun,
                                                     Function<Response, T> failedFun) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(successFun);
        checkArgumentNotNull(failedFun);

        try {
            return successful(response) ? successFun.apply(response) : failedFun.apply(response);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Given a {@link Response} Supplier, perform an action that returns a result if it was
     * successful ({@code successFun} or throw a (subclass of) {@link RuntimeException} if it
     * failed ({@code throwingFun}).
     * If the Supplier threw an exception, then that exception is rethrown.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param successFun       the function to apply if the response is successful
     * @param throwingFun      a function that creates an appropriate (subclass of) RuntimeException
     * @param <T>              the result type
     * @return the result from applying {@code successFun}
     * @throws RuntimeException the result of {@code throwingFun} or the exception thrown by the Supplier
     */
    public static <T> T onSuccessWithResultOrFailureThrow(Supplier<Response> responseSupplier,
                                                          Function<Response, T> successFun,
                                                          Function<Response, ? extends RuntimeException> throwingFun) {

        checkArgumentNotNull(responseSupplier);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            return onSuccessWithResultOrFailureThrow(result.response(), successFun, throwingFun);
        }

        throw result.error();
    }

    /**
     * Given a {@link Response}, perform an action that returns a result if it was successful ({@code successFun}
     * or throw a (subclass of) {@link RuntimeException} if it failed ({@code throwingFun}).
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response    the response object
     * @param successFun  the function to apply if the response is successful
     * @param throwingFun a function that creates an appropriate (subclass of) RuntimeException
     * @param <T>         the result type
     * @return the result from applying {@code successFun}
     * @throws RuntimeException the result of {@code throwingFun}
     */
    public static <T> T onSuccessWithResultOrFailureThrow(Response response,
                                                          Function<Response, T> successFun,
                                                          Function<Response, ? extends RuntimeException> throwingFun) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(successFun);
        checkArgumentNotNull(throwingFun);

        try {
            if (successful(response)) {
                return successFun.apply(response);
            }

            throw throwingFun.apply(response);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Given a {@link Response} Supplier, perform some action using one of the supplied consumers.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier  a Supplier that provides the response
     * @param responseConsumer  the action to run on any response
     * @param exceptionConsumer the action to run if the Supplier throws an exception
     */
    public static void accept(Supplier<Response> responseSupplier,
                              Consumer<Response> responseConsumer,
                              Consumer<RuntimeException> exceptionConsumer) {

        checkArgumentNotNull(responseSupplier);
        checkArgumentNotNull(exceptionConsumer);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            accept(result.response(), responseConsumer);
        } else {
            exceptionConsumer.accept(result.error());
        }
    }

    /**
     * Given a {@link Response}, perform some action using the supplied consumer.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response         the response object
     * @param responseConsumer the action to run
     */
    public static void accept(Response response, Consumer<Response> responseConsumer) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(responseConsumer);

        try {
            responseConsumer.accept(response);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Given a {@link Response} Supplier, perform an action tha returns a result using one of the given functions.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param responseSupplier a Supplier that provides the response
     * @param fun              the function to apply to the response
     * @param exceptionFun     the function to apply if the Supplier throws an exception
     * @param <T>              the result type
     * @return the result of applying the given function
     */
    public static <T> T apply(Supplier<Response> responseSupplier,
                              Function<Response, T> fun,
                              Function<RuntimeException, T> exceptionFun) {

        checkArgumentNotNull(responseSupplier);
        checkArgumentNotNull(exceptionFun);

        var result = getResponse(responseSupplier);

        if (result.hasResponse()) {
            return apply(result.response(), fun);
        }

        return exceptionFun.apply(result.error());
    }

    /**
     * Given a {@link Response}, perform an action tha returns a result using the given function.
     * <p>
     * Ensures the response is closed after performing the action.
     *
     * @param response the response object
     * @param fun      the function to apply to the response
     * @param <T>      the result type
     * @return the result of applying the given function
     */
    public static <T> T apply(Response response, Function<Response, T> fun) {
        checkArgumentNotNull(response);
        checkArgumentNotNull(fun);

        try {
            return fun.apply(response);
        } finally {
            closeQuietly(response);
        }
    }

    @VisibleForTesting
    record WebCallResult(RuntimeException error, Response response) {

        // This is really an "either" type...which sadly, Java does not have.

        WebCallResult {
            checkOnlyOneArgumentIsNull(error, response,
                    "Either the Response or the RuntimeException can be null, but not both");
        }

        static WebCallResult ofResponse(Response response) {
            return new WebCallResult(null, response);
        }

        static WebCallResult ofError(RuntimeException error) {
            return new WebCallResult(error, null);
        }

        boolean hasResponse() {
            return nonNull(response);
        }
    }

    @VisibleForTesting
    static WebCallResult getResponse(Supplier<Response> responseSupplier) {
        Response response = null;
        RuntimeException error = null;
        try {
            response = responseSupplier.get();
        } catch (RuntimeException e) {
            error = e;
        }

        if (nonNull(response)) {
            return WebCallResult.ofResponse(response);
        }

        if (nonNull(error)) {
            logResponseSupplierException(LOG, error);
            return WebCallResult.ofError(error);
        }

        LOG.warn("Response Supplier returned a null Response, which is not permitted");
        throw new IllegalStateException("Response returned by Supplier must not be null");
    }

    @VisibleForTesting
    static void logResponseSupplierException(Logger logger, RuntimeException error) {
        if (logger.isTraceEnabled()) {
            logger.trace("Response Supplier threw an exception", error);
        } else {
            logger.warn("Response Supplier unexpectedly threw: {}: {} (enable TRACE level to see stack trace)",
                    error.getClass().getName(), error.getMessage());
        }
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

    private static class NoOpResponseConsumer implements Consumer<Response> {
        @Override
        public void accept(Response response) {
            // no-op
        }
    }
}
