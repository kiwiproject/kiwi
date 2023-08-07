package org.kiwiproject.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class ValidationTestHelper {

    public static final Validator DEFAULT_VALIDATOR = newValidator();

    public static Validator newValidator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    public static void assertNoViolations(Validator validator, Object object) {
        var violations = validator.validate(object);
        assertThat(violations).isEmpty();
    }

    public static void assertViolations(Validator validator, Object object, int numExpectedViolations) {
        var violations = validator.validate(object);
        assertThat(violations).hasSize(numExpectedViolations);
    }

    public static void assertViolations(Validator validator, Object object, String... expectedCombinedMessages) {
        var violations = validator.validate(object);

        var actualCombinedMessages = KiwiConstraintViolations.simpleCombinedErrorMessages(violations);

        var missingMessages = Arrays.stream(expectedCombinedMessages)
                .filter(value -> !actualCombinedMessages.contains(value))
                .toList();

        if (!missingMessages.isEmpty()) {
            fail("Messages [%s] not found in actual messages %s",
                    String.join(",", missingMessages), actualCombinedMessages);
        }
    }

    public static void assertOnePropertyViolation(Object object, String propertyName) {
        assertOnePropertyViolation(DEFAULT_VALIDATOR, object, propertyName);
    }

    public static void assertOnePropertyViolation(Validator validator, Object object, String propertyName) {
        assertPropertyViolations(validator, object, propertyName, 1);
    }

    public static void assertNoPropertyViolations(Object object, String propertyName) {
        assertNoPropertyViolations(DEFAULT_VALIDATOR, object, propertyName);
    }

    public static void assertNoPropertyViolations(Validator validator, Object object, String propertyName) {
        assertPropertyViolations(validator, object, propertyName, 0);
    }

    public static void assertPropertyViolations(Validator validator,
                                                Object object,
                                                String propertyName,
                                                int numExpectedViolations) {
        var violations = validator.validateProperty(object, propertyName);
        assertThat(violations).hasSize(numExpectedViolations);
    }

    public static void assertPropertyViolations(Object object,
                                                String propertyName,
                                                String... expectedMessages) {
        assertPropertyViolations(DEFAULT_VALIDATOR, object, propertyName, expectedMessages);
    }

    public static void assertPropertyViolations(Validator validator,
                                                Object object,
                                                String propertyName,
                                                String... expectedMessages) {

        var violations = validator.validateProperty(object, propertyName);

        var actualMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        var missingMessages = Arrays.stream(expectedMessages)
                .filter(value -> !actualMessages.contains(value))
                .toList();

        if (!missingMessages.isEmpty()) {
            fail("Messages [%s] not found in actual messages %s",
                    String.join(",", missingMessages), actualMessages);
        }
    }
}
