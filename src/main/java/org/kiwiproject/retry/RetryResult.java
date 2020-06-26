package org.kiwiproject.retry;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiLists.last;

import lombok.Getter;
import org.kiwiproject.base.UUIDs;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a result of one or more attempts to get some type of object. The list of errors should reflect the order
 * in which errors occurred, e.g. the first error should be the error for the first attempt, second error should be
 * the error for the second attempt, etc.
 * <p>
 * A unique identifier is automatically assigned to new {@link RetryResult} instances for easy identification, e.g.
 * when logging retry exceptions.
 * <p>
 * Note that there is nothing precluding a {@link RetryResult} from representing a failure but not actually have any
 * errors. This could happen, for example, if retry code executes a {@link java.util.function.Supplier}
 * and that supplier is implemented to catch all exceptions and return {@code null} to force a retry. We generally
 * recommend to implement Suppliers to throw exceptions when errors occur. They will need to be subclasses of
 * {@link RuntimeException} since {@link java.util.function.Supplier} doesn't permit checked exceptions, plus we
 * generally prefer unchecked exceptions.
 *
 * @param <T> the result type
 */
@Getter
public class RetryResult<T> {

    private final String resultUuid;
    private final int numAttemptsMade;
    private final int maxAttempts;
    private final T object;
    private final List<Exception> errors;

    /**
     * Create new instance.
     *
     * @param numAttemptsMade the number of attempts (must be less than or equal to max attempts)
     * @param maxAttempts     the maximum number of attempts
     * @param object          the result, or {@code null}
     * @param errors          a non-null list containing any errors that occurred on attempts, or empty list if no
     *                        errors occurred
     */
    public RetryResult(int numAttemptsMade, int maxAttempts, T object, List<Exception> errors) {
        checkArgument(numAttemptsMade <= maxAttempts,
                "numAttemptsMade (%s) is not less or equal to maxAttempts (%s)", numAttemptsMade, maxAttempts);
        checkArgumentNotNull(errors, "errors cannot be null; pass empty list if there are no errors");

        this.resultUuid = UUIDs.randomUUIDString();
        this.numAttemptsMade = numAttemptsMade;
        this.maxAttempts = maxAttempts;
        this.object = object;
        this.errors = List.copyOf(errors);
    }

    /**
     * The result succeeded if this result has an object.
     *
     * @return true if the result object is not null, otherwise false
     */
    public boolean succeeded() {
        return hasObject();
    }

    /**
     * The result failed if this result does not have an object.
     *
     * @return true if the result object is null, otherwise false
     */
    public boolean failed() {
        return !succeeded();
    }

    /**
     * Does this result have an object that was successfully retrieved before the maximum number of attempts?
     *
     * @return true if the result object is not null, otherwise false
     */
    public boolean hasObject() {
        return nonNull(object);
    }

    /**
     * Assumes there is an object an returns it, otherwise throws an exception.
     * <p>
     * You should check if an object exists before calling this. Or, consider using {@link #getObjectIfPresent()}.
     *
     * @return the result object
     * @throws IllegalStateException if this result does not have an object
     */
    public T getObject() {
        return getObjectIfPresent()
                .orElseThrow(() -> new IllegalStateException("No object is present in this result"));
    }

    /**
     * Returns an {@link Optional} that contains the object if this result was successful, otherwise it will be empty.
     *
     * @return an Optional that may contain a result, or be empty
     */
    public Optional<T> getObjectIfPresent() {
        return Optional.ofNullable(object);
    }

    /**
     * Whether more than one attempt was made to retrieve the object.
     *
     * @return true if the number of attempts is more than one
     */
    public boolean hasMoreThanOneAttempt() {
        return getNumAttemptsMade() > 1;
    }

    /**
     * Did any attempts fail with an exception? The overall result can still be successful if an object is obtained
     * before the maximum number of attempts is reached.
     *
     * @return true if there are any errors that occurred (independent of overall success/failure)
     */
    public boolean hasAnyErrors() {
        return !errors.isEmpty();
    }

    /**
     * The number of failed attempts, which can be zero up to the maximum number of attempts. A number greater than
     * zero does <em>not</em> represent overall failure, however, since a result could have been obtained even if
     * there were errors.
     *
     * @return the number of errors
     */
    public int getNumErrors() {
        return errors.size();
    }

    /**
     * Assumes there is at least one error, and returns the last one that was thrown regardless of the number
     * of attempts.
     * <p>
     * You should check if there are any errors, e.g. using {@link #hasAnyErrors()}, before calling this method.
     * Alternatively consider using {@link #getLastErrorIfPresent}.
     *
     * @return the most recent exception that occurred
     * @throws IllegalStateException if this result does not have any errors
     */
    public Exception getLastError() {
        return getLastErrorIfPresent()
                .orElseThrow(() -> new IllegalStateException("No errors exist in this result"));
    }

    /**
     * Returns an {@link Optional} that contains the last error if this result has any errors, otherwise empty.
     *
     * @return an Optional that may contain an error, or be empty
     */
    public Optional<Exception> getLastErrorIfPresent() {
        return errors.isEmpty() ? Optional.empty() : Optional.of(last(errors));
    }

    /**
     * Return a set containing the unique error types in this result. There is NO guarantee whatsoever about the
     * order of the unique error types, so don't make any assumptions. That's why the return type is {@link Set}.
     *
     * @return set of unique error types (class names)
     */
    public Set<String> getUniqueErrorTypes() {
        return errors.stream()
                .map(e -> e.getClass().getName())
                .collect(toSet());
    }
}
