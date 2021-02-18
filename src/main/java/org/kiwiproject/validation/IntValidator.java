package org.kiwiproject.validation;

import static java.util.Objects.isNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IntValidator implements ConstraintValidator<Int, CharSequence> {

    private Int anInt;

    @Override
    public void initialize(Int constraintAnnotation) {
        this.anInt = constraintAnnotation;
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return anInt.allowNull();
        }

        try {
            Integer.parseInt(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
