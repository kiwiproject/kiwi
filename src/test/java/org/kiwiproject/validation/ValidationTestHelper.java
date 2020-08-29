package org.kiwiproject.validation;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import lombok.experimental.UtilityClass;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
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
                .collect(toUnmodifiableList());

        if (!missingMessages.isEmpty()) {
            fail("Messages [%s] not found in actual messages %s",
                    String.join(",", missingMessages), actualCombinedMessages);
        }
    }

    public static void assertOnePropertyViolation(Validator validator, Object object, String propertyName) {
        assertPropertyViolations(validator, object, propertyName, 1);
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

    public static void assertPropertyViolations(Validator validator,
                                                Object object,
                                                String propertyName,
                                                String... expectedMessages) {

        var violations = validator.validateProperty(object, propertyName);

        var actualMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(toUnmodifiableList());

        var missingMessages = Arrays.stream(expectedMessages)
                .filter(value -> !actualMessages.contains(value))
                .collect(toUnmodifiableList());

        if (!missingMessages.isEmpty()) {
            fail("Messages [%s] not found in actual messages %s",
                    String.join(",", missingMessages), actualMessages);
        }
    }
}
