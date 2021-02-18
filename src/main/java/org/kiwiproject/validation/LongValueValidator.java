package org.kiwiproject.validation;

import static java.util.Objects.isNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that a string value is a long value, i.e. that it can be converted to a long or {@link Long}.
 */
public class LongValueValidator implements ConstraintValidator<LongValue, CharSequence> {

    private LongValue longValue;

    @Override
    public void initialize(LongValue constraintAnnotation) {
        this.longValue = constraintAnnotation;
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return longValue.allowNull();
        }

        try {
            Long.parseLong(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
