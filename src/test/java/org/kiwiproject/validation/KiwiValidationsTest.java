package org.kiwiproject.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.Data;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@DisplayName("KiwiValidations")
class KiwiValidationsTest {

    @Nested
    class NewValidator {

        @Test
        void shouldAlwaysReturnNewInstance() {
            var validator1 = KiwiValidations.newValidator();
            var validator2 = KiwiValidations.newValidator();
            var validator3 = KiwiValidations.newValidator();
            var validator4 = KiwiValidations.newValidator();
            var validator5 = KiwiValidations.newValidator();

            assertThat(validator1)
                    .isNotSameAs(validator2)
                    .isNotSameAs(validator3)
                    .isNotSameAs(validator4)
                    .isNotSameAs(validator5);
        }
    }

    @Nested
    class GetValidator {

        @Test
        void shouldAlwaysReturnSameInstance() {
            var validator1 = KiwiValidations.getValidator();
            var validator2 = KiwiValidations.getValidator();
            var validator3 = KiwiValidations.getValidator();
            var validator4 = KiwiValidations.getValidator();
            var validator5 = KiwiValidations.getValidator();

            assertThat(validator1)
                    .isSameAs(validator2)
                    .isSameAs(validator3)
                    .isSameAs(validator4)
                    .isSameAs(validator5);
        }
    }

    @Nested
    class SetValidator {

        @Test
        void shouldChangeValidatorInstance() {
            var validator1 = KiwiValidations.getValidator();
            var validator2 = KiwiValidations.newValidator();
            assertThat(validator2).isNotSameAs(validator1);

            KiwiValidations.setValidator(validator2);

            assertThat(KiwiValidations.getValidator()).isSameAs(validator2);
        }
    }

    @Nested
    class Validate {

        @Test
        void shouldValidate_ValidObjects() {
            var contactDetails = new SampleContactDetails("alice@example.org", "444-555-1212");
            var alice = new SamplePerson("Alice", "Jones", null, contactDetails);

            var violations = KiwiValidations.validate(alice);
            assertThat(violations).isEmpty();
        }

        @Test
        void shouldValidate_InvalidObjects() {
            var contactDetails = new SampleContactDetails("bob@example.org", "");
            var bob = new SamplePerson("Bob", null, null, contactDetails);

            var violations = KiwiValidations.validate(bob);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsOnly(
                            "lastName",
                            "contactDetails.mobileNumber"
                    );
        }
    }

    @Nested
    class ViolateWithGroups {

        @Test
        void shouldValidate_ValidObjects() {
            var contactDetails = new SampleContactDetails("alice@example.org", "444-555-1212");
            var alice = new SamplePerson("Alice", "Jones", "123-45-6789", contactDetails);

            var violations = KiwiValidations.validate(alice, Default.class, Secret.class);
            assertThat(violations).isEmpty();
        }

        @Test
        void shouldValidate_InvalidObjects() {
            var contactDetails = new SampleContactDetails("bob@example.org", "");
            var bob = new SamplePerson(null, "S", null, contactDetails);

            var violations = KiwiValidations.validate(bob, Default.class, Secret.class);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsOnly(
                            "firstName",
                            "lastName",
                            "ssn",
                            "contactDetails.mobileNumber");
        }
    }

    @Nested
    class AddError {

        @Test
        void shouldAddErrorFromGivenTemplate() {
            var thing = new Thing();
            thing.setName("The Awesome");

            var violations = KiwiValidations.validate(thing);

            assertThat(violations).hasSize(1);

            var violation = violations.stream().findFirst().orElseThrow();
            assertThat(violation.getPropertyPath()).hasToString("name");
            assertThat(violation.getMessage()).isEqualTo("'The Awesome' will always be regarded as invalid! (sorry)");
        }
    }

    /**
     * Used with {@link AddError}.
     */
    @Documented
    @Constraint(validatedBy = AlwaysInvalidValidator.class)
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @interface AlwaysInvalid {
        String message() default "";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

        String suffix() default "(oops)";
    }

    /**
     * Used with {@link AddError}.
     * <p>
     * <strong>Must be public (otherwise Hibernate Validator cannot instantiate it)</strong>
     */
    public static class AlwaysInvalidValidator implements ConstraintValidator<AlwaysInvalid, CharSequence> {

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            KiwiValidations.addError(context, "'${validatedValue}' will always be regarded as invalid! {suffix}");

            return false;
        }
    }

    /**
     * Used with {@link AddError}.
     */
    @Data
    static class Thing {

        @AlwaysInvalid(suffix = "(sorry)")
        private String name;
    }

    @Nested
    class GetPropertyValue {

        @Test
        void shouldGetValue() {
            var contactDetails = new SampleContactDetails("alice@example.org", "444-555-1212");
            var alice = new SamplePerson("Alice", "Jones", "123-45-6789", contactDetails);

            var lastName = KiwiValidations.getPropertyValue(alice, "lastName");
            assertThat(lastName).isEqualTo(alice.getLastName());

            var aliceDetails = KiwiValidations.getPropertyValue(alice, "contactDetails");
            assertThat(aliceDetails)
                    .isNotNull()
                    .isInstanceOf(SampleContactDetails.class);
            assertThat(((SampleContactDetails) aliceDetails).getEmailAddress())
                    .isEqualTo(contactDetails.getEmailAddress());
        }

        @Test
        void shouldReturnNull_WhenCannotGetValue() {
            var alice = new SamplePerson("Alice", "Jones", "123-45-6789", null);

            assertThat(KiwiValidations.getPropertyValue(alice, "dne")).isNull();
        }
    }

    @Value
    static class SampleContactDetails {

        @NotBlank
        @Email
        String emailAddress;

        @NotBlank
        String mobileNumber;
    }

    @Value
    static class SamplePerson {

        @NotBlank
        @Length(max = 32)
        String firstName;

        @NotBlank
        @Length(min = 2, max = 32)
        String lastName;

        @NotBlank(groups = Secret.class)
        String ssn;

        @NotNull
        @Valid
        SampleContactDetails contactDetails;
    }

    interface Secret {
    }
}
