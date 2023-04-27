package org.kiwiproject.jaxrs.exception;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.base.KiwiStrings.format;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.hasOneElement;
import static org.kiwiproject.collect.KiwiLists.isNotNullOrEmpty;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.collect.KiwiMaps;
import org.kiwiproject.collect.KiwiSets;

import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a JAX-RS exception that uses the Kiwi {@link ErrorMessage} to describe
 * the errors causing this exception.
 * <p>
 * This class is concrete but also can be subclassed to represent specific HTTP status error codes.
 */
public class JaxrsException extends RuntimeException {

    private static final String ROLLUP_MESSAGE = "Rollup of %s exceptions.";

    /**
     * @implNote The IllegalMonitorStateException is here mainly for testing purposes. We don't expect this
     * to ever really be the cause, but if it was then a 409 Conflict isn't a totally horrible response code.
     */
    private static final Set<String> CONFLICT_EXCEPTION_TYPES = Set.of(
            "org.springframework.dao.OptimisticLockingFailureException",
            "org.springframework.dao.PessimisticLockingFailureException",
            "org.springframework.dao.DataIntegrityViolationException",
            "org.hibernate.dialect.lock.OptimisticEntityLockException",
            "org.hibernate.dialect.lock.OptimisticLockException",
            "org.hibernate.dialect.lock.PessimisticEntityLockException",
            "java.lang.IllegalMonitorStateException"  // see @implNote
    );

    private final Integer status;
    private final transient List<ErrorMessage> errors = new ArrayList<>();
    private final transient Map<String, Object> otherData = new HashMap<>();

    /**
     * New instance with given cause.
     *
     * @param cause the cause of this exception
     */
    public JaxrsException(Throwable cause) {
        this(cause, getErrorCode(cause));
    }

    /**
     * New instance with given message and default status code.
     *
     * @param message the message for this exception
     */
    public JaxrsException(String message) {
        this(new ErrorMessage(message));
    }

    /**
     * New instance with given message and status code.
     *
     * @param message    the message for this exception
     * @param statusCode the status code for this exception
     */
    public JaxrsException(String message, int statusCode) {
        this(new ErrorMessage(statusCode, message));
    }

    /**
     * New instance with given message and cause.
     *
     * @param message the message for this exception
     * @param cause   the cause of this exception
     */
    public JaxrsException(String message, Throwable cause) {
        this(message, cause, getErrorCode(cause));
    }

    /**
     * New instance with given message, cause, and status code.
     *
     * @param message    the message for this exception
     * @param cause      the cause of this exception
     * @param statusCode the status code for this exception
     */
    public JaxrsException(String message, Throwable cause, int statusCode) {
        this(new ErrorMessage(statusCode, message), cause);
    }

    /**
     * New instance with given ErrorMessage.
     *
     * @param error the ErrorMessage cause of this exception
     */
    public JaxrsException(ErrorMessage error) {
        this(error, null);
    }

    /**
     * New instance with given cause and status code.
     *
     * @param cause      the cause of this exception
     * @param statusCode the status code for this exception
     */
    public JaxrsException(Throwable cause, int statusCode) {
        this(new ErrorMessage(statusCode, nullSafeMessage(cause)), cause);
    }

    /**
     * New instance with given ErrorMessage and Throwable.
     *
     * @param error the ErrorMessage cause of this exception
     * @param cause the cause of this exception
     */
    public JaxrsException(ErrorMessage error, Throwable cause) {
        super(nonNullMessage(error), cause);
        this.status = null;
        errors.add(nonNullError(error, cause));
    }

    private static ErrorMessage nonNullError(ErrorMessage error, Throwable cause) {
        return isNull(error) ? new ErrorMessage(nullSafeMessage(cause)) : error;
    }

    private static String nullSafeMessage(Throwable throwable) {
        return isNull(throwable) ? null : throwable.getMessage();
    }

    /**
     * New "aggregate" instance with given list of ErrorMessage objects. The {@code statusCode} can be
     * {@code null} if a "rolled up" overall status is desired, or an explicit code be be given to represent
     * all the errors. The message of this exception is take from the first ErrorMessage.
     *
     * @param errors     a list containing multiple ErrorMessages as the underlying cause of this exception
     * @param statusCode the overall status code to use, or {@code null} (overall status will be rolled up as max
     *                   of all the ErrorMessage objects)
     */
    public JaxrsException(List<ErrorMessage> errors, @Nullable Integer statusCode) {
        super(nonNullMessage(firstErrorOrNull(errors)));
        this.status = statusCode;
        setErrors(errors);
    }

    private static String nonNullMessage(ErrorMessage error) {
        return isNull(error) ? ErrorMessage.DEFAULT_MSG : error.getMessage();
    }

    private static ErrorMessage firstErrorOrNull(List<ErrorMessage> errors) {
        return isNotNullOrEmpty(errors) ? first(errors) : null;
    }

    /**
     * New "aggregate" instance with given list of JaxrsException objects.
     *
     * @param exceptions the JaxrsException objects that caused this exception
     */
    public JaxrsException(List<JaxrsException> exceptions) {
        super(rollupMessageOrDefault(exceptions));
        this.status = null;

        if (isNotNullOrEmpty(exceptions)) {
            exceptions.forEach(jaxrsException -> {
                errors.addAll(jaxrsException.getErrors());
                addOtherData(jaxrsException.getOtherData());
            });
        }
    }

    private static String rollupMessageOrDefault(List<JaxrsException> exceptions) {
        return isNullOrEmpty(exceptions) ? ErrorMessage.DEFAULT_MSG : format(ROLLUP_MESSAGE, exceptions.size());
    }

    /**
     * Static factory to construct a new instance from the given {@link Throwable}.
     *
     * @param throwable the cause to use for the JaxrsException
     * @return new JaxrsException instance
     */
    public static JaxrsException buildJaxrsException(Throwable throwable) {
        return throwable instanceof JaxrsException ? (JaxrsException) throwable : new JaxrsException(throwable);
    }

    /**
     * @return an unmodifiable list of {@link ErrorMessage}s
     */
    public List<ErrorMessage> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Change the {@link ErrorMessage} objects contained in this exception.
     * <p>
     * <strong>NOTE:</strong> If the given {@code errorMessages} is null or empty, it is ignored in order to
     * prevent clearing out all errors.
     *
     * @param errorMessages the new {@link ErrorMessage}s to set
     * @implNote This is final because it is used in some sub-class constructors. It is a big no-no to call
     * overridable methods in constructors. It <em>can</em> cause very strange behavior like NPEs. Also see
     * Effective Java (3rd Edition) Item #19 "Design and document for inheritance or else prohibit it" and
     * Sonar rule java:S1699 "Constructors should only call non-overridable methods".
     */
    protected final void setErrors(List<ErrorMessage> errorMessages) {
        if (isNotNullOrEmpty(errorMessages)) {
            errors.clear();
            errors.addAll(errorMessages);
        }
    }

    /**
     * @return the overall status or a "roll up" status if there are multiple errors
     * @see #getRollUpStatus()
     */
    public int getStatusCode() {
        return nonNull(status) ? status : getRollUpStatus();
    }

    /**
     * Calculates an overall status code as the "roll up" of the status codes in the ErrorMessage objects contained
     * in this exception.
     * <p>
     * If there are no ErrorMessage objects a default status code is returned. If there is exactly one ErrorMessage,
     * then its status code is returned.
     * <p>
     * If there are multiple ErrorMessage objects, and they all have the same status code, then the overall status
     * is just that status code.
     * <p>
     * Last, if there are multiple ErrorMessage objects, and some have different status codes, then the overall status
     * is calculated to be the base status code (e.g. 400) of the highest error family (e.g. 4xx). For example, if
     * there are multiple 4xx errors then the overall status is considered as the base of the 4xx series, or 400.
     * Or if there are both 4xx and 5xx errors, the overall status is 500 (the base of the 5xx series). This is
     * obviously a lossy "algorithm" and is meant as an overall indication of the error family. Inspection of all
     * contained errors is required to fully determine the causes.
     *
     * @return the "rolled up" status code
     */
    public int getRollUpStatus() {
        if (isNullOrEmpty(errors)) {
            return ErrorMessage.DEFAULT_CODE;
        } else if (hasOneElement(errors)) {
            return first(errors).getCode();
        }

        verify(errors.size() > 1, "Expecting more than one error at this point");
        var uniqueStatusCodes = errors.stream().map(ErrorMessage::getCode).collect(toSet());
        if (KiwiSets.hasOneElement(uniqueStatusCodes)) {
            return uniqueStatusCodes.iterator().next();
        }

        return uniqueStatusCodes.stream()
                .max(Integer::compareTo)
                .map(highestCode -> (highestCode / 100) * 100)
                .orElseThrow();
    }

    /**
     * @return an unmodifiable map of additional data about this exception
     */
    public Map<String, Object> getOtherData() {
        return Collections.unmodifiableMap(otherData);
    }

    /**
     * <em>Appends</em> the entries contained in the given map to the existing {@code otherData}. Or, if the
     * given map is null or empty, clears the existing {@code otherData} (but will never set it to null).
     * <p>
     * <strong>NOTE:</strong> If the given map contains a key named "errors", it will be ignored when the
     * JaxrsExceptionMapper creates the response, because "errors" is reserved for the list of {@link ErrorMessage}
     * objects.
     *
     * @param newDataToAppend map containing additional data
     * @apiNote This method is poorly named, but since we have existing code that uses it, we don't plan to
     * change it. Whenever Jakarta EE 9 is released, we will look into a re-design of this class and package.
     */
    public void setOtherData(Map<String, Object> newDataToAppend) {
        if (KiwiMaps.isNullOrEmpty(newDataToAppend)) {
            otherData.clear();
        } else {
            otherData.putAll(newDataToAppend);
        }
    }

    private void addOtherData(Map<String, Object> data) {
        if (KiwiMaps.isNullOrEmpty(data)) {
            return;
        }

        otherData.putAll(data);
    }

    /**
     * Determine an "appropriate" HTTP status code for the given {@link Throwable}.
     *
     * @param throwable the Throwable to inspect
     * @return an HTTP error status code
     */
    public static int getErrorCode(Throwable throwable) {
        if (isNull(throwable)) {
            return 500;
        }

        if (throwable instanceof JaxrsException) {
            return ((JaxrsException) throwable).getStatusCode();
        }

        if (throwable instanceof WebApplicationException) {
            return ((WebApplicationException) throwable).getResponse().getStatus();
        }

        if (throwable instanceof IllegalArgumentException) {
            return 400;
        }

        if (throwable instanceof IllegalStateException) {
            return 409;
        }

        var canonicalName = throwable.getClass().getCanonicalName();
        var className = Optional.ofNullable(canonicalName).orElse("NoCanonicalClassName");
        if (CONFLICT_EXCEPTION_TYPES.contains(className)) {
            return 409;
        }

        return 500;
    }
}
