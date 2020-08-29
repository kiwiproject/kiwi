package org.kiwiproject.validation;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.validation.InternalKiwiValidators.TEMPLATE_REQUIRED;
import static org.kiwiproject.validation.InternalKiwiValidators.toComparableOrNull;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator class for {@link FieldRange}.
 */
@Slf4j
public class FieldRangeValidator implements ConstraintValidator<FieldRange, Object> {

    private static final String TEMPLATE_BETWEEN = "{org.kiwiproject.validation.FieldRange.between.message}";
    private static final String TEMPLATE_MIN_ONLY = "{org.kiwiproject.validation.FieldRange.minOnly.message}";
    private static final String TEMPLATE_MAX_ONLY = "{org.kiwiproject.validation.FieldRange.maxOnly.message}";
    private static final String TEMPLATE_AFTER_EXCLUSIVE = "{org.kiwiproject.validation.FieldRange.afterExclusive.message}";
    private static final String TEMPLATE_AFTER_INCLUSIVE = "{org.kiwiproject.validation.FieldRange.afterInclusive.message}";

    private FieldRange fieldRange;

    @Override
    public void initialize(FieldRange constraintAnnotation) {
        this.fieldRange = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Validity validity;
        try {
            var start = getFieldValueAsComparable(value, fieldRange.startField());
            var end = getFieldValueAsComparable(value, fieldRange.endField());

            validity = checkValidity(start, end, context);
        } catch (Exception e) {
            addUnknownErrorConstraintViolation(context, fieldRange);
            logWarning(value, fieldRange, e);
            validity = Validity.INVALID;
        }

        return validity == Validity.VALID;
    }

    @SuppressWarnings("unchecked")
    private static Comparable<Object> getFieldValueAsComparable(Object value, String fieldName) {
        return (Comparable<Object>) KiwiValidations.getPropertyValue(value, fieldName);
    }

    private Validity checkValidity(Comparable<Object> start, Comparable<Object> end, ConstraintValidatorContext context) {
        var validity = checkMinMax(start, end, context);

        if (validity == Validity.CONTINUE) {
            // check null first as checkRange checks won't work if null
            validity = checkNull(start, end, context);

            if (validity == Validity.CONTINUE) {
                validity = checkRange(start, end, context);
            }
        }

        return validity;
    }

    private Validity checkMinMax(Comparable<Object> start, Comparable<Object> end, ConstraintValidatorContext context) {
        var validity = Validity.CONTINUE;

        var min = toComparableOrNull(fieldRange.min(), start);
        if (nonNull(min) && start.compareTo(min) < 0) {
            var template = isBlank(fieldRange.max()) ? TEMPLATE_MIN_ONLY : TEMPLATE_BETWEEN;
            KiwiValidations.addError(context, template, fieldRange.startField());
            validity = Validity.INVALID;
        }

        var max = toComparableOrNull(fieldRange.max(), end);
        if (nonNull(max) && end.compareTo(max) > 0) {
            var template = isBlank(fieldRange.min()) ? TEMPLATE_MAX_ONLY : TEMPLATE_BETWEEN;
            KiwiValidations.addError(context, template, fieldRange.endField());
            validity = Validity.INVALID;
        }

        return validity;
    }

    private Validity checkNull(Comparable<Object> start, Comparable<Object> end, ConstraintValidatorContext context) {
        var startValidity = checkFieldNullity(start, fieldRange.startField(), fieldRange.allowNullStart(), context);
        var endValidity = checkFieldNullity(end, fieldRange.endField(), fieldRange.allowNullEnd(), context);

        if (startValidity == Validity.INVALID || endValidity == Validity.INVALID) {
            return Validity.INVALID;
        }

        if (isNull(start) || isNull(end)) {
            return Validity.VALID;
        }

        return Validity.CONTINUE;
    }

    private static Validity checkFieldNullity(Comparable<Object> comparable,
                                              String field,
                                              boolean allowNull,
                                              ConstraintValidatorContext context) {

        if (nonNull(comparable) || allowNull) {
            return Validity.CONTINUE;
        }

        KiwiValidations.addError(context, TEMPLATE_REQUIRED, field);
        return Validity.INVALID;
    }

    private Validity checkRange(Comparable<Object> start, Comparable<Object> end, ConstraintValidatorContext context) {
        verify(nonNull(start), "start should not be null at this point; checkNull should not have returned CONTINUE!");
        verify(nonNull(end), "end should not be null at this point; checkNull should not have returned CONTINUE!");

        var validity = Validity.VALID;

        if (fieldRange.allowStartToEqualEnd()) {
            if (start.compareTo(end) > 0) {
                KiwiValidations.addError(context, TEMPLATE_AFTER_INCLUSIVE, fieldRange.startField());
                validity = Validity.INVALID;
            }
        } else if (start.compareTo(end) >= 0) {
            KiwiValidations.addError(context, TEMPLATE_AFTER_EXCLUSIVE, fieldRange.startField());
            validity = Validity.INVALID;
        }

        return validity;
    }

    private static void addUnknownErrorConstraintViolation(ConstraintValidatorContext context, FieldRange fieldRange) {
        KiwiValidations.addError(context, "unknown validation error", fieldRange.startField());
    }

    private static void logWarning(Object value, FieldRange fieldRange, Exception e) {
        var valueAsString = isNull(value) ? "null" : value.getClass().getName();
        LOG.warn("Error validating FieldRange with startField: {}, endField: {}, for value: {}; considering invalid",
                fieldRange.startField(), fieldRange.endField(), valueAsString, e);
    }
}
