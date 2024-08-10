package org.kiwiproject.jaxrs;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.collect.KiwiSets.isNotNullOrEmpty;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.groups.Default;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;
import org.kiwiproject.jaxrs.exception.JaxrsValidationException;
import org.kiwiproject.validation.KiwiConstraintViolations;
import org.kiwiproject.validation.KiwiValidations;

import java.util.Map;
import java.util.Set;

/**
 * Static utility methods that perform validation on an object or value, and throw an appropriate
 * subclass of {@link org.kiwiproject.jaxrs.exception.JaxrsException JaxrsException} if there are validation errors.
 */
@UtilityClass
@Slf4j
public class KiwiJaxrsValidations {

    @VisibleForTesting
    static final String MISSING_VALUE_MESSAGE = "Missing required value";

    /**
     * Assert that the given object is valid using the default validation group.
     *
     * @param id  the unique identity or ID of the object being validated
     * @param obj the object to validate
     * @param <T> the type of object
     * @throws JaxrsValidationException if there are any validation errors
     * @see Default
     */
    public static <T> void assertValid(String id, T obj) {
        assertValid(id, obj, Default.class);
    }

    /**
     * Assert that the given object is valid using the specified validation group classes.
     * <p>
     * Note that if you want the default validation group included, it must be in {@code groupClasses}.
     *
     * @param id           the unique identity or ID of the object being validated
     * @param obj          the object to validate
     * @param groupClasses validation groups to apply during validation
     * @param <T>          the type of object
     * @throws JaxrsValidationException if there are any validation errors
     */
    public static <T> void assertValid(String id, T obj, Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(obj, groupClasses);
        if (isNotNullOrEmpty(violations)) {
            debugLogValidationErrors(id, obj, violations);
            throw new JaxrsValidationException(id, violations);
        }
    }

    /**
     * Assert that the given object is valid using the default validation group and the specified property path
     * map, which allows callers to translate property paths (e.g., firstName) to a human-readable
     * names (e.g., First Name).
     *
     * @param id                   the unique identity or ID of the object being validated
     * @param obj                  the object to validate
     * @param propertyPathMappings mapping from property path to (human-readable) field name
     * @param <T>                  the type of object
     * @throws JaxrsValidationException if there are any validation errors
     * @see Default
     */
    public static <T> void assertValid(String id, T obj, Map<String, String> propertyPathMappings) {
        assertValid(id, obj, propertyPathMappings, Default.class);
    }

    /**
     * Assert that the given object is valid using the specified validation group classes and property path
     * map.
     * <p>
     * Note that if you want the default validation group included, it must be in {@code groupClasses}.
     *
     * @param id                   the unique identity or ID of the object being validated
     * @param obj                  the object to validate
     * @param propertyPathMappings mapping from property path to (human-readable) field name
     * @param groupClasses         validation groups to apply during validation
     * @param <T>                  the type of object
     * @throws JaxrsValidationException if there are any validation errors
     */
    public static <T> void assertValid(String id,
                                       T obj,
                                       Map<String, String> propertyPathMappings,
                                       Class<?>... groupClasses) {
        var violations = KiwiValidations.validate(obj, groupClasses);
        if (isNotNullOrEmpty(violations)) {
            debugLogValidationErrors(id, obj, violations);
            throw new JaxrsValidationException(id, violations, propertyPathMappings);
        }
    }

    private static <T> void debugLogValidationErrors(String id, T obj, Set<ConstraintViolation<T>> violations) {
        LOG.debug("Encountered validation errors for {} (id: {}): {}",
                obj.getClass().getSimpleName(),
                id,
                lazy(() -> KiwiConstraintViolations.simpleCombinedErrorMessage(violations)));
    }

    /**
     * Assert that the given String value is not blank.
     *
     * @param fieldName the field name being validated
     * @param value     the String value to check
     * @throws JaxrsBadRequestException if the value is blank
     * @implNote Blank check is performed using {@link org.apache.commons.lang3.StringUtils#isBlank(CharSequence)})
     */
    public static void assertNotBlank(String fieldName, String value) {
        if (isBlank(value)) {
            throw new JaxrsBadRequestException(MISSING_VALUE_MESSAGE, fieldName);
        }
    }

    /**
     * Assert that the given Object is not null.
     *
     * @param fieldName the field name being validated
     * @param value     the value to check
     * @throws JaxrsBadRequestException if the value is null
     */
    public static void assertNotNull(String fieldName, Object value) {
        if (isNull(value)) {
            throw new JaxrsBadRequestException(MISSING_VALUE_MESSAGE, fieldName);
        }
    }

    /**
     * Asserts that the given boolean value is true.
     *
     * @param value   the value to check
     * @param message the message to use in the thrown exception
     * @throws JaxrsBadRequestException if the value is false
     */
    public static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new JaxrsBadRequestException(message);
        }
    }
}
