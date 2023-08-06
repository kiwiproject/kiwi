package org.kiwiproject.jaxrs.exception;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exception representing a 422 status code that extends {@link JaxrsException} to use Kiwi's {@link ErrorMessage}.
 */
public class JaxrsValidationException extends JaxrsException {

    /**
     * The status code for all instances of this exception.
     */
    public static final int CODE = 422;

    private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";

    /**
     * New instance with given item ID and error messages.
     * <p>
     * Each map contained in {@code errorMessageMaps} must have entries with at least two keys:
     * {@link ErrorMessage#KEY_FIELD_NAME} and {@link ErrorMessage#KEY_MESSAGE}. In other words, the
     * maps should contain an entry whose key is {@link ErrorMessage#KEY_FIELD_NAME} and another entry
     * whose key is {@link ErrorMessage#KEY_MESSAGE}. Any other entries are ignored. The value of these
     * two entries become the field name and message in the resulting {@link ErrorMessage} objects.
     *
     * @param itemId           the unique ID of the item that caused this error
     * @param errorMessageMaps a list containing maps containing
     */
    public JaxrsValidationException(String itemId, List<Map<String, String>> errorMessageMaps) {
        super(VALIDATION_FAILED_MESSAGE, CODE);

        checkArgumentNotNull(errorMessageMaps);
        var errorMessages = errorMessageMaps.stream()
                .map(entry -> {
                    var fieldName = entry.get(ErrorMessage.KEY_FIELD_NAME);
                    var message = entry.get(ErrorMessage.KEY_MESSAGE);
                    return new ErrorMessage(itemId, CODE, message, fieldName);
                }).toList();
        setErrors(errorMessages);
    }

    /**
     * New instance with given item ID and constraint violations.
     *
     * @param itemId     the unique ID of the item that caused this error
     * @param violations the constraint violations to transform into {@link ErrorMessage} objects
     */
    public JaxrsValidationException(String itemId, Set<? extends ConstraintViolation<?>> violations) {
        super(VALIDATION_FAILED_MESSAGE, CODE);

        checkArgumentNotNull(violations);
        var errorMessages = violations.stream()
                .map(violation -> buildErrorMessage(itemId, violation))
                .toList();
        setErrors(errorMessages);
    }

    /**
     * New instance with given item ID, constraint violations, and a map containing entries whose keys are the
     * property path of the {@link ConstraintViolation} and values are the field/property name that should be
     * used in place of the property path.
     * <p>
     * For example, {@code propertyPathMappings} might contain an entry with key "firstName" and value
     * "First Name". In the resulting instance, the corresponding {@link ErrorMessage} object contained in the
     * list returned by {@link #getErrors()} will have "First Name" as the field name.
     *
     * @param itemId               the unique ID of the item that caused this error
     * @param violations           the constraint violations to transform into {@link ErrorMessage} objects
     * @param propertyPathMappings mappings from property path of a {@link ConstraintViolation} to the field name
     *                             to use in the {@link ErrorMessage} objects
     */
    public JaxrsValidationException(String itemId,
                                    Set<? extends ConstraintViolation<?>> violations,
                                    Map<String, String> propertyPathMappings) {
        super(VALIDATION_FAILED_MESSAGE, CODE);

        checkArgumentNotNull(violations);
        var errorMessages = violations.stream()
                .map(violation -> {
                    var propertyPath = violation.getPropertyPath().toString();
                    var fieldNameOrNull = propertyPathMappings.get(propertyPath);
                    return buildErrorMessage(itemId, violation, fieldNameOrNull);
                }).toList();
        setErrors(errorMessages);
    }

    /**
     * New instance with given {@link ErrorMessage} objects.
     * <p>
     * It is assumed but not checked that the {@link ErrorMessage} objects are due to 422 errors.
     * Therefore it is possible to instantiate an instance with errors that have some other status
     * code.
     *
     * @param errorMessages non-null list of error messages
     */
    public JaxrsValidationException(List<ErrorMessage> errorMessages) {
        super(VALIDATION_FAILED_MESSAGE, CODE);

        checkArgumentNotNull(errorMessages);
        setErrors(errorMessages);
    }

    private static ErrorMessage buildErrorMessage(String itemId,
                                                  @NotNull ConstraintViolation<?> violation) {
        checkArgumentNotNull(violation, "violation cannot be null");
        return buildErrorMessage(itemId, violation, violation.getPropertyPath().toString());
    }

    @VisibleForTesting
    static ErrorMessage buildErrorMessage(String itemId,
                                          @NotNull ConstraintViolation<?> violation,
                                          String fieldName) {
        checkArgumentNotNull(violation, "violation cannot be null");
        var fieldNameOrPropertyPath = nonNull(fieldName) ? fieldName : violation.getPropertyPath().toString();
        return new ErrorMessage(itemId, CODE, violation.getMessage(), fieldNameOrPropertyPath);
    }

    /**
     * Factory method to create validation exception from a non-null list of {@link ErrorMessage}.
     *
     * @param errorMessages non-null list of error messages
     * @return a new {@link JaxrsValidationException} instance
     * @see #JaxrsValidationException(List)
     */
    public static JaxrsValidationException ofErrorMessages(List<ErrorMessage> errorMessages) {
        return new JaxrsValidationException(errorMessages);
    }
}
