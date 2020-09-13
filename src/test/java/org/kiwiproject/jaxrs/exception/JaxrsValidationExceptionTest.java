package org.kiwiproject.jaxrs.exception;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.validation.KiwiValidations;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @implNote We are relying on the exact messages in the Beans Validation properties
 * file ({@code ValidationMessages.properties}). If the messages ever change, then this test will break.
 * It could be made more robust by looking the messages up in the properties file, but since the
 * messages changing seems unlikely, we are hard coding them in this test.
 */
@DisplayName("JaxrsValidationException")
@ExtendWith(SoftAssertionsExtension.class)
class JaxrsValidationExceptionTest {

    @Test
    void shouldConstructFromListOfMaps(SoftAssertions softly) {
        var errorMessageMaps = List.of(
                Map.of(ErrorMessage.KEY_FIELD_NAME, "firstName", ErrorMessage.KEY_MESSAGE, "must not be blank"),
                Map.of(ErrorMessage.KEY_FIELD_NAME, "emailAddress", ErrorMessage.KEY_MESSAGE, "must be a well-formed email address")
        );
        var ex = new JaxrsValidationException("42", errorMessageMaps);

        softly.assertThat(ex.getStatusCode()).isEqualTo(422);
        softly.assertThat(ex.getErrors()).hasSize(2);

        var firstNameError = first(ex.getErrors());
        softly.assertThat(firstNameError.getItemId()).isEqualTo("42");
        softly.assertThat(firstNameError.getCode()).isEqualTo(422);
        softly.assertThat(firstNameError.getFieldName()).isEqualTo("firstName");
        softly.assertThat(firstNameError.getMessage()).isEqualTo("must not be blank");

        var emailAddressError = second(ex.getErrors());
        softly.assertThat(emailAddressError.getItemId()).isEqualTo("42");
        softly.assertThat(emailAddressError.getCode()).isEqualTo(422);
        softly.assertThat(emailAddressError.getFieldName()).isEqualTo("emailAddress");
        softly.assertThat(emailAddressError.getMessage()).isEqualTo("must be a well-formed email address");
    }

    @Test
    void shouldConstructFromConstraintViolations(SoftAssertions softly) {
        var person = new Person("", "X", new ContactDetail("x"));
        var sortedViolations = validateAndSort(person);

        var ex = new JaxrsValidationException("84", sortedViolations);

        softly.assertThat(ex.getStatusCode()).isEqualTo(422);
        softly.assertThat(ex.getErrors()).hasSize(3);

        var emailError = first(ex.getErrors());
        softly.assertThat(emailError.getItemId()).isEqualTo("84");
        softly.assertThat(emailError.getCode()).isEqualTo(422);
        softly.assertThat(emailError.getFieldName()).isEqualTo("contact.emailAddress");
        softly.assertThat(emailError.getMessage()).isEqualTo("must be a well-formed email address");

        var firstNameError = second(ex.getErrors());
        softly.assertThat(firstNameError.getItemId()).isEqualTo("84");
        softly.assertThat(firstNameError.getCode()).isEqualTo(422);
        softly.assertThat(firstNameError.getFieldName()).isEqualTo("firstName");
        softly.assertThat(firstNameError.getMessage()).isEqualTo("must not be blank");

        var lastNameError = third(ex.getErrors());
        softly.assertThat(lastNameError.getItemId()).isEqualTo("84");
        softly.assertThat(lastNameError.getCode()).isEqualTo(422);
        softly.assertThat(lastNameError.getFieldName()).isEqualTo("lastName");
        softly.assertThat(lastNameError.getMessage()).isEqualTo("length must be between 2 and 255");
    }

    @Test
    void shouldConstructFromConstraintViolations_WithFieldNameOverrides(SoftAssertions softly) {
        var person = new Person("", "X", new ContactDetail("x"));
        TreeSet<ConstraintViolation<Person>> sortedViolations = validateAndSort(person);

        var propertyPathMappings = Map.of(
                "contact.emailAddress", "Email",
                "firstName", "First Name",
                "lastName", "Last Name"
        );
        var ex = new JaxrsValidationException("84", sortedViolations, propertyPathMappings);

        softly.assertThat(ex.getStatusCode()).isEqualTo(422);
        softly.assertThat(ex.getErrors()).hasSize(3);

        var emailError = first(ex.getErrors());
        softly.assertThat(emailError.getItemId()).isEqualTo("84");
        softly.assertThat(emailError.getCode()).isEqualTo(422);
        softly.assertThat(emailError.getFieldName()).isEqualTo("Email");
        softly.assertThat(emailError.getMessage()).isEqualTo("must be a well-formed email address");

        var firstNameError = second(ex.getErrors());
        softly.assertThat(firstNameError.getItemId()).isEqualTo("84");
        softly.assertThat(firstNameError.getCode()).isEqualTo(422);
        softly.assertThat(firstNameError.getFieldName()).isEqualTo("First Name");
        softly.assertThat(firstNameError.getMessage()).isEqualTo("must not be blank");

        var lastNameError = third(ex.getErrors());
        softly.assertThat(lastNameError.getItemId()).isEqualTo("84");
        softly.assertThat(lastNameError.getCode()).isEqualTo(422);
        softly.assertThat(lastNameError.getFieldName()).isEqualTo("Last Name");
        softly.assertThat(lastNameError.getMessage()).isEqualTo("length must be between 2 and 255");
    }

    private static TreeSet<ConstraintViolation<Person>> validateAndSort(Person person) {
        var violations = KiwiValidations.validate(person);
        var sortedViolations =
                new TreeSet<ConstraintViolation<Person>>(comparing(violation -> violation.getPropertyPath().toString()));
        sortedViolations.addAll(violations);
        return sortedViolations;
    }

    @Nested
    class BuildErrorMessage {

        @Test
        void shouldUseGivenFieldName() {
            var bob = new Person("Bob", "", null);
            var violation = KiwiValidations.validate(bob).iterator().next();

            var errorMessage = JaxrsValidationException.buildErrorMessage("42", violation, "Email address");

            assertThat(errorMessage.getFieldName()).isEqualTo("Email address");
        }

        @Test
        void shouldUsePropertyPath_WhenFieldNameIsNull() {
            var contactDetail = new ContactDetail("bob");
            var violation = KiwiValidations.validate(contactDetail).iterator().next();

            var errorMessage = JaxrsValidationException.buildErrorMessage("42", violation, null);

            assertThat(errorMessage.getFieldName()).isEqualTo("emailAddress");
        }
    }

    @AllArgsConstructor
    @Getter
    private static class ContactDetail {
        @Email
        private final String emailAddress;
    }

    @AllArgsConstructor
    @Getter
    private static class Person {

        @NotBlank
        private final String firstName;

        @NotBlank
        @Length(min = 2, max = 255)
        private final String lastName;

        @Valid
        private final ContactDetail contact;
    }
}
