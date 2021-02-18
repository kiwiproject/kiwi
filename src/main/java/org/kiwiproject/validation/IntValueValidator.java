package org.kiwiproject.validation;

import static java.util.Objects.isNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that a string value is an integer value, i.e. that it can be converted to an int or {@link Integer}.
 */
public class IntValueValidator implements ConstraintValidator<IntValue, CharSequence> {

    private IntValue intValue;

    @Override
    public void initialize(IntValue constraintAnnotation) {
        this.intValue = constraintAnnotation;
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return intValue.allowNull();
        }

        try {
            Integer.parseInt(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
