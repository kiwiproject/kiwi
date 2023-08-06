package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.isNullOrEmpty;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Locale;

/**
 * Validates that a string value is in the list of enum constants, or a value derived from an enum constant.
 */
public class InEnumValidator implements ConstraintValidator<InEnum, String> {

    private InEnum inEnum;
    private List<String> allowableValues;
    private String errorMessage;

    /**
     * @param constraintAnnotation the InEnum annotation
     * @throws IllegalStateException if the enum has no constants or if using a valueMethod which throws an exception
     *                               when called
     */
    @Override
    public void initialize(InEnum constraintAnnotation) {
        this.inEnum = constraintAnnotation;

        var constants = List.of(inEnum.enumClass().getEnumConstants());
        this.allowableValues = constants.stream()
                .map(anEnum -> enumConstantOrStringValue(inEnum, anEnum))
                .map(stringValue -> uppercaseIfIgnoringCase(inEnum, stringValue))
                .toList();
        checkAllowableValuesExist();

        this.errorMessage = inEnum.message() + " " + allowableValues;
    }

    private String enumConstantOrStringValue(InEnum inEnum, Enum<?> enumValue) {
        var valueMethod = inEnum.valueMethod();
        if (isBlank(valueMethod)) {
            return enumValue.name();
        }

        var enumClass = enumValue.getClass();
        try {
            var method = enumClass.getDeclaredMethod(valueMethod);
            return String.valueOf(method.invoke(enumValue));
        } catch (Exception e) {
            var message = f("Unable to invoke valueMethod '{}' on class {}. Is it a public no-arg method?",
                    valueMethod, enumClass.getName());
            throw new IllegalStateException(message);
        }
    }

    private String uppercaseIfIgnoringCase(InEnum inEnum, String value) {
        if (inEnum.ignoreCase()) {
            return uppercase(value);
        }

        return value;
    }

    private String uppercase(String value) {
        return Strings.nullToEmpty(value).toUpperCase(Locale.getDefault());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        checkAllowableValuesExist();  // just in case

        if (isNull(value)) {
            if (inEnum.allowNull()) {
                return true;
            }

            KiwiValidations.addError(context, errorMessage);
            return false;
        }

        var nonNullValue = valueToCompare(value);
        if (allowableValues.contains(nonNullValue)) {
            return true;
        }

        KiwiValidations.addError(context, errorMessage);
        return false;
    }

    private void checkAllowableValuesExist() {
        if (isNullOrEmpty(allowableValues)) {
            var message = f("Enum {} has no values to validate against!", inEnum.enumClass().getSimpleName());
            throw new IllegalStateException(message);
        }
    }

    private String valueToCompare(String value) {
        requireNonNull(value);
        return inEnum.ignoreCase() ? uppercase(value) : value;
    }

}
