package org.kiwiproject.validation;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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

    private static final String TEMPLATE_BETWEEN_MIN_MAX_VALUES = "{org.kiwiproject.validation.FieldRange.between.message.minMaxValues}";
    private static final String TEMPLATE_BETWEEN_MIN_MAX_LABELS = "{org.kiwiproject.validation.FieldRange.between.message.minMaxLabels}";
    private static final String TEMPLATE_MIN_ONLY_MIN_MAX_VALUES = "{org.kiwiproject.validation.FieldRange.minOnly.message.minMaxValues}";
    private static final String TEMPLATE_MIN_ONLY_MIN_MAX_LABELS = "{org.kiwiproject.validation.FieldRange.minOnly.message.minMaxLabels}";
    private static final String TEMPLATE_MAX_ONLY_MIN_MAX_VALUES = "{org.kiwiproject.validation.FieldRange.maxOnly.message.minMaxValues}";
    private static final String TEMPLATE_MAX_ONLY_MIN_MAX_LABELS = "{org.kiwiproject.validation.FieldRange.maxOnly.message.minMaxLabels}";
    private static final String TEMPLATE_AFTER_EXCLUSIVE_MIN_MAX_VALUES = "{org.kiwiproject.validation.FieldRange.afterExclusive.message.minMaxValues}";
    private static final String TEMPLATE_AFTER_EXCLUSIVE_MIN_MAX_LABELS = "{org.kiwiproject.validation.FieldRange.afterExclusive.message.minMaxLabels}";
    private static final String TEMPLATE_AFTER_INCLUSIVE_MIN_MAX_VALUES = "{org.kiwiproject.validation.FieldRange.afterInclusive.message.minMaxValues}";
    private static final String TEMPLATE_AFTER_INCLUSIVE_MIN_MAX_LABELS = "{org.kiwiproject.validation.FieldRange.afterInclusive.message.minMaxLabels}";

    private FieldRange fieldRange;
    private String templateBetween;
    private String templateMinOnly;
    private String templateMaxOnly;
    private String templateAfterExclusive;
    private String templateAfterInclusive;

    /**
     * @param constraintAnnotation annotation instance for a given constraint declaration
     * @implNote if <em>either</em> {@link FieldRange#minLabel()} or {@link FieldRange#maxLabel()} is present, this selects
     * the template containing labels. This design is specifically due to the change in Hibernate Validator 6.2.x which
     * disables EL (expression language) by default for custom validators, and allows this (custom) validator to
     * operate correctly without EL enabled.
     */
    @Override
    public void initialize(FieldRange constraintAnnotation) {
        this.fieldRange = constraintAnnotation;

        var useLabels = isNotBlank(constraintAnnotation.minLabel()) || isNotBlank(constraintAnnotation.maxLabel());
        this.templateBetween = useLabels ? TEMPLATE_BETWEEN_MIN_MAX_LABELS : TEMPLATE_BETWEEN_MIN_MAX_VALUES;
        this.templateMinOnly = useLabels ? TEMPLATE_MIN_ONLY_MIN_MAX_LABELS : TEMPLATE_MIN_ONLY_MIN_MAX_VALUES;
        this.templateMaxOnly = useLabels ? TEMPLATE_MAX_ONLY_MIN_MAX_LABELS : TEMPLATE_MAX_ONLY_MIN_MAX_VALUES;
        this.templateAfterExclusive = useLabels ? TEMPLATE_AFTER_EXCLUSIVE_MIN_MAX_LABELS : TEMPLATE_AFTER_EXCLUSIVE_MIN_MAX_VALUES;
        this.templateAfterInclusive = useLabels ? TEMPLATE_AFTER_INCLUSIVE_MIN_MAX_LABELS : TEMPLATE_AFTER_INCLUSIVE_MIN_MAX_VALUES;
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
            var template = isBlank(fieldRange.max()) ? templateMinOnly : templateBetween;
            KiwiValidations.addError(context, template, fieldRange.startField());
            validity = Validity.INVALID;
        }

        var max = toComparableOrNull(fieldRange.max(), end);
        if (nonNull(max) && end.compareTo(max) > 0) {
            var template = isBlank(fieldRange.min()) ? templateMaxOnly : templateBetween;
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
                KiwiValidations.addError(context, templateAfterInclusive, fieldRange.startField());
                validity = Validity.INVALID;
            }
        } else if (start.compareTo(end) >= 0) {
            KiwiValidations.addError(context, templateAfterExclusive, fieldRange.startField());
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
