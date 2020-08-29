package org.kiwiproject.validation;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kiwiproject.validation.InternalKiwiValidators.TEMPLATE_REQUIRED;
import static org.kiwiproject.validation.InternalKiwiValidators.toComparableOrNull;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for @{@link Range}.
 */
@Slf4j
public class RangeValidator implements ConstraintValidator<Range, Object> {

    private static final String TEMPLATE_BETWEEN = "{org.kiwiproject.validation.Range.between.message}";
    private static final String TEMPLATE_LESS_THAN_OR_EQ = "{org.kiwiproject.validation.Range.lessThanOrEq.message}";
    private static final String TEMPLATE_GREATER_THAN_OR_EQ = "{org.kiwiproject.validation.Range.greaterThanOrEq.message}";

    private Range range;

    @Override
    public void initialize(Range constraintAnnotation) {
        this.range = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Validity validity;
        try {
            validity = checkNull(value, context);

            if (validity == Validity.CONTINUE) {
                //noinspection unchecked,rawtypes
                validity = checkMinMax((Comparable) value, context);
            }
        } catch (Exception e) {
            KiwiValidations.addError(context, "unknown validation error");
            logWarning(value, e);
            validity = Validity.INVALID;
        }

        return validity == Validity.VALID;
    }

    private Validity checkNull(Object value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            if (range.allowNull()) {
                return Validity.VALID;
            }

            KiwiValidations.addError(context, TEMPLATE_REQUIRED);
            return Validity.INVALID;
        }

        return Validity.CONTINUE;
    }

    private Validity checkMinMax(Comparable<Object> value, ConstraintValidatorContext context) {
        verify(nonNull(value), "value should not be null at this point (checkNull should have returned INVALID)!");

        var min = toComparableOrNull(range.min(), value);
        var max = toComparableOrNull(range.max(), value);

        if (hasBothMinAndMax(min, max) && outsideRange(min, max, value)) {
            KiwiValidations.addError(context, TEMPLATE_BETWEEN);
            return Validity.INVALID;
        }

        return checkMinOrMax(context, min, max, value);
    }

    private static boolean hasBothMinAndMax(Comparable<?> min, Comparable<?> max) {
        return nonNull(min) && nonNull(max);
    }

    private static boolean outsideRange(Comparable<?> min, Comparable<?> max, Comparable<Object> value) {
        return value.compareTo(min) < 0 || value.compareTo(max) > 0;
    }

    private static Validity checkMinOrMax(ConstraintValidatorContext context,
                                          Comparable<?> min,
                                          Comparable<?> max,
                                          Comparable<Object> value) {

        if (nonNull(min) && value.compareTo(min) < 0) {
            KiwiValidations.addError(context, TEMPLATE_GREATER_THAN_OR_EQ);
            return Validity.INVALID;
        }

        if (nonNull(max) && value.compareTo(max) > 0) {
            KiwiValidations.addError(context, TEMPLATE_LESS_THAN_OR_EQ);
            return Validity.INVALID;
        }

        return Validity.VALID;
    }

    private static void logWarning(Object value, Exception e) {
        if (value instanceof Comparable) {
            LOG.warn("Error validating Range for value: {} ; considering as invalid", value, e);
        } else {
            LOG.warn("Error validating Range for value that does not implement Comparable: {} ; considering as invalid",
                    value, e);
        }
    }
}
