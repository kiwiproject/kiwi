package org.kiwiproject.validation;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.validation.InternalKiwiValidators.TEMPLATE_REQUIRED;
import static org.kiwiproject.validation.InternalKiwiValidators.toComparableOrNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for @{@link Range}.
 */
@Slf4j
public class RangeValidator implements ConstraintValidator<Range, Object> {

    private static final String TEMPLATE_BETWEEN_MIN_MAX_VALUES = "{org.kiwiproject.validation.Range.between.message.minMaxValues}";
    private static final String TEMPLATE_BETWEEN_MIN_MAX_LABELS = "{org.kiwiproject.validation.Range.between.message.minMaxLabels}";
    private static final String TEMPLATE_LESS_THAN_OR_EQ_MIN_MAX_VALUES = "{org.kiwiproject.validation.Range.lessThanOrEq.message.minMaxValues}";
    private static final String TEMPLATE_LESS_THAN_OR_EQ_MIN_MAX_LABELS = "{org.kiwiproject.validation.Range.lessThanOrEq.message.minMaxLabels}";
    private static final String TEMPLATE_GREATER_THAN_OR_EQ_MIN_MAX_VALUES = "{org.kiwiproject.validation.Range.greaterThanOrEq.message.minMaxValues}";
    private static final String TEMPLATE_GREATER_THAN_OR_EQ_MIN_MAX_LABELS = "{org.kiwiproject.validation.Range.greaterThanOrEq.message.minMaxLabels}";
    private static final String TEMPLATE_UNKNOWN_ERROR = "{org.kiwiproject.validation.FieldRange.unknownError.message}";

    private Range range;
    private String templateBetween;
    private String templateLessThanOrEq;
    private String templateGreaterThanOrEq;

    /**
     * @param constraintAnnotation annotation instance for a given constraint declaration
     * @implNote if <em>either</em> {@link Range#minLabel()} or {@link Range#maxLabel()} is present, this selects
     * the template containing labels. This design is specifically due to the change in Hibernate Validator 6.2.x which
     * disables EL (expression language) by default for custom validators, and allows this (custom) validator to
     * operate correctly without EL enabled.
     */
    @Override
    public void initialize(Range constraintAnnotation) {
        checkState(hasMinOrMax(constraintAnnotation), "@Range must specify at least one of 'min' or 'max'");

        this.range = constraintAnnotation;

        var useLabels = isNotBlank(constraintAnnotation.minLabel()) || isNotBlank(constraintAnnotation.maxLabel());
        LOG.trace("minLabel and/or maxLabel exist, so label-based messages will be used");

        this.templateBetween = useLabels ? TEMPLATE_BETWEEN_MIN_MAX_LABELS : TEMPLATE_BETWEEN_MIN_MAX_VALUES;
        this.templateLessThanOrEq = useLabels ? TEMPLATE_LESS_THAN_OR_EQ_MIN_MAX_LABELS : TEMPLATE_LESS_THAN_OR_EQ_MIN_MAX_VALUES;
        this.templateGreaterThanOrEq = useLabels ? TEMPLATE_GREATER_THAN_OR_EQ_MIN_MAX_LABELS : TEMPLATE_GREATER_THAN_OR_EQ_MIN_MAX_VALUES;
    }

    private static boolean hasMinOrMax(Range constraintAnnotation) {
        return isNotBlank(constraintAnnotation.min()) || isNotBlank(constraintAnnotation.max());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Validity validity;
        try {
            validity = checkNull(value, context);

            if (validity == Validity.CONTINUE) {
                // noinspection unchecked
                validity = checkMinMax((Comparable<Object>) value, context);
            }
        } catch (Exception e) {
            KiwiValidations.addError(context, TEMPLATE_UNKNOWN_ERROR);
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
            KiwiValidations.addError(context, templateBetween);
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

    private Validity checkMinOrMax(ConstraintValidatorContext context,
                                   Comparable<?> min,
                                   Comparable<?> max,
                                   Comparable<Object> value) {

        if (nonNull(min) && value.compareTo(min) < 0) {
            KiwiValidations.addError(context, templateGreaterThanOrEq);
            return Validity.INVALID;
        }

        if (nonNull(max) && value.compareTo(max) > 0) {
            KiwiValidations.addError(context, templateLessThanOrEq);
            return Validity.INVALID;
        }

        return Validity.VALID;
    }

    private static void logWarning(Object value, Exception e) {
        var type = isNull(value) ? null : value.getClass().getName();
        if (value instanceof Comparable) {
            LOG.warn("Error validating Range for value of type {} (which may not be supported) ; considering as invalid", type, e);
        } else {
            LOG.warn("Error validating Range for value of type {} that does not implement Comparable ; considering as invalid",
                    type, e);
        }
    }
}
