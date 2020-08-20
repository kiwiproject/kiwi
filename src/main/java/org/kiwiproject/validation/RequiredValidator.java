package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Check that a validated value is required.
 */
@Slf4j
public class RequiredValidator implements ConstraintValidator<Required, Object> {

    private boolean allowBlank;
    private boolean allowEmpty;

    @Override
    public void initialize(Required constraintAnnotation) {
        allowBlank = constraintAnnotation.allowBlank();
        allowEmpty = constraintAnnotation.allowEmpty();
    }

    /**
     * Perform the validation on the given value. See {@link Required} for the validation logic.
     *
     * @param value   the value to validate
     * @param context context in which the constraint is evaluated
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        //noinspection RedundantIfStatement
        if (isNull(value) || notAllowingBlanksButIsBlank(value) || notAllowingEmptyButIsEmpty(value)) {
            return false;
        }

        return true;
    }

    private boolean notAllowingBlanksButIsBlank(Object value) {
        return !allowBlank && isBlank(value);
    }

    private static boolean isBlank(Object value) {
        return value instanceof CharSequence && StringUtils.isBlank((CharSequence) value);
    }

    private boolean notAllowingEmptyButIsEmpty(Object value) {
        return !allowEmpty && isEmpty(value);
    }

    private static boolean isEmpty(@Nonnull Object value) {
        requireNonNull(value);

        if (value instanceof CharSequence) {
            return StringUtils.isEmpty((CharSequence) value);
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }

        try {
            var isEmptyMethod = findIsEmptyMethod(value).orElse(null);

            if (isNull(isEmptyMethod)) {
                return false;
            }

            return (boolean) isEmptyMethod.invoke(value);
        } catch (Exception e) {
            LOG.trace("Error invoking isEmpty method; assuming not empty", e);
        }

        // Nothing we checked above matched; assume not empty
        return false;
    }

    /**
     * Find public isEmpty() method that returns a primitive {@code boolean}.
     */
    private static Optional<Method> findIsEmptyMethod(Object value) {
        return Arrays.stream(value.getClass().getMethods())
                .filter(method -> method.getName().equals("isEmpty"))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> method.getReturnType().equals(Boolean.TYPE))
                .findFirst();
    }
}
