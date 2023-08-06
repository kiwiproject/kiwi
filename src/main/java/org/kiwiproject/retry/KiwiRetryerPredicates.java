package org.kiwiproject.retry;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.apache.commons.lang3.exception.ExceptionUtils.indexOfType;

import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.function.Predicate;

/**
 * Some potentially useful predicates that can be used out of the box with {@link KiwiRetryer} or directly with
 * {@link org.kiwiproject.retry.RetryerBuilder}, or anything else that accepts a {@link Predicate}.
 * <p>
 * Note that the Jakarta RS-API needs to be available at runtime to use the {@link Response} predicates.
 */
@UtilityClass
public class KiwiRetryerPredicates {

    /**
     * Check if a given {@link Exception} is or has a root cause of {@link UnknownHostException}.
     *
     * @implNote UnknownHostException does not have a cause, which is why we check only the instance and root cause
     */
    public static final Predicate<Exception> UNKNOWN_HOST = ex ->
            ex instanceof UnknownHostException || getRootCause(ex) instanceof UnknownHostException;


    /**
     * Check if a given {@link Exception} is or has a root cause of {@link ConnectException}.
     *
     * @implNote ConnectException does not have a cause, which is why we check only the instance and root cause
     */
    public static final Predicate<Exception> CONNECTION_ERROR = ex ->
            ex instanceof ConnectException || getRootCause(ex) instanceof ConnectException;


    /**
     * Check if a given {@link Exception} is or has a root cause of {@link SocketTimeoutException}.
     *
     * @implNote SocketTimeoutException does not have a cause, which is why we check only the instance and root cause
     */
    public static final Predicate<Exception> SOCKET_TIMEOUT = ex ->
            ex instanceof SocketTimeoutException || getRootCause(ex) instanceof SocketTimeoutException;


    /**
     * Check if a given {@link Exception} is or contains a {@link javax.net.ssl.SSLHandshakeException} somewhere in
     * the causal chain.
     */
    public static final Predicate<Exception> SSL_HANDSHAKE_ERROR = ex ->
            ex instanceof SSLHandshakeException || indexOfType(ex, SSLHandshakeException.class) > -1;


    /**
     * Check if a given {@link Exception} is or has a root cause of {@link NoRouteToHostException}.
     *
     * @implNote NoRouteToHostException does not have a cause, which is why we check only the instance and root cause
     */
    public static final Predicate<Exception> NO_ROUTE_TO_HOST = ex ->
            ex instanceof NoRouteToHostException || getRootCause(ex) instanceof NoRouteToHostException;

    /**
     * Check if a given JAX-RS {@link Response} is a client error (4xx).
     */
    public static final Predicate<Response> IS_HTTP_400s = response ->
            nonNull(response) && Response.Status.Family.CLIENT_ERROR == response.getStatusInfo().getFamily();

    /**
     * Check if a given JAX-RS {@link Response} is a server error (5xx).
     */
    public static final Predicate<Response> IS_HTTP_500s = response ->
            nonNull(response) && Response.Status.Family.SERVER_ERROR == response.getStatusInfo().getFamily();

}
