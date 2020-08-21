package org.kiwiproject.validation;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.json.JsonHelper;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;

/**
 * Validator for @{@link Range}.
 *
 * @implNote A singleton JsonHelper created using {@link JsonHelper#newDropwizardJsonHelper()} is used for JSON parsing.
 */
@Slf4j
public class RangeValidator implements ConstraintValidator<Range, Object> {

    private static final String ERROR_REQUIRED = "{org.kiwiproject.validation.Required.message}";
    private static final String TEMPLATE_BETWEEN = "{org.kiwiproject.validation.Range.between.message}";
    private static final String TEMPLATE_LESS_THAN_OR_EQ = "{org.kiwiproject.validation.Range.lessThanOrEq.message}";
    private static final String TEMPLATE_GREATER_THAN_OR_EQ = "{org.kiwiproject.validation.Range.greaterThanOrEq.message}";

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

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

            KiwiValidations.addError(context, ERROR_REQUIRED);
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

    /**
     * @implNote This is non-ideal with the massive if/else if/else, but since we have to check each type we
     * support, I cannot think of a "better" or "cleaner" way to do this without it becoming so abstract that
     * it becomes unreadable. Interestingly, neither IntelliJ not Sonar is complaining...maybe we don't have the
     * appropriate rules enabled. Suggestions for improvement welcome!
     */
    private static Comparable<?> toComparableOrNull(String minOrMax, Comparable<?> value) {
        if (isBlank(minOrMax) || isNull(value)) {
            return null;
        }

        Comparable<?> typedValue;

        if (value instanceof Double) {
            typedValue = Double.valueOf(minOrMax);
        } else if (value instanceof Float) {
            typedValue = Float.valueOf(minOrMax);
        } else if (value instanceof Byte) {
            typedValue = Byte.valueOf(minOrMax);
        } else if (value instanceof Short) {
            typedValue = Short.valueOf(minOrMax);
        } else if (value instanceof Integer) {
            typedValue = Integer.valueOf(minOrMax);
        } else if (value instanceof Long) {
            typedValue = Long.valueOf(minOrMax);
        } else if (value instanceof BigDecimal) {
            typedValue = new BigDecimal(minOrMax);
        } else if (value instanceof BigInteger) {
            typedValue = new BigInteger(minOrMax);
        } else if (value instanceof Date) {
            typedValue = new Date(Long.parseLong(minOrMax));
        } else if (value instanceof Instant) {
            typedValue = Instant.ofEpochMilli(Long.parseLong(minOrMax));
        } else if (minOrMax.stripLeading().startsWith("{")) {
            typedValue = JSON_HELPER.toObject(minOrMax, value.getClass());
        } else {
            var message = "This validators does not support validating objects of type: " + value.getClass().getName();
            throw new IllegalArgumentException(message);
        }

        return typedValue;
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
