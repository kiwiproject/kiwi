package org.kiwiproject.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.kiwiproject.base.KiwiStrings.f;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.util.BlankStringSource;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

        @Test
        void shouldEvaluateExpressionLanguage_WhenCustomValidator_EnablesIt() {
            var person = new Person();
            person.setZipCode("12345-xyz");

            var violations = KiwiValidations.validate(person);

            assertThat(violations).hasSize(1);

            var violation = violations.stream().findFirst().orElseThrow();
            assertThat(violation.getPropertyPath()).hasToString("zipCode");
            assertThat(violation.getMessage())
                    .startsWith("'12345-xyz' is not a valid ZIP code as of ")
                    .endsWith(String.valueOf(LocalDate.now().getYear()));
        }
    }

    @Nested
    class ValidateThrowing {

        @Test
        void shouldValidate_ValidObjects() {
            var contactDetails = new SampleContactDetails("alice@example.org", "444-555-1212");
            var alice = new SamplePerson("Alice", "Jones", null, contactDetails);

            assertThatCode(() -> KiwiValidations.validateThrowing(alice)).doesNotThrowAnyException();
        }

        @Test
        void shouldValidate_InvalidObjects() {
            var contactDetails = new SampleContactDetails("bob@example.org", "");
            var bob = new SamplePerson("Bob", null, null, contactDetails);

            var exception = catchThrowableOfType(() -> KiwiValidations.validateThrowing(bob), ConstraintViolationException.class);
            var violations = exception.getConstraintViolations();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsOnly(
                            "lastName",
                            "contactDetails.mobileNumber"
                    );
        }
    }

    @Nested
    class ValidateThrowingWithGroups {

        @Test
        void shouldValidate_ValidObjects() {
            var contactDetails = new SampleContactDetails("alice@example.org", "444-555-1212");
            var alice = new SamplePerson("Alice", "Jones", "123-45-6789", contactDetails);

            assertThatCode(() -> KiwiValidations.validateThrowing(alice, Default.class, Secret.class)).doesNotThrowAnyException();
        }

        @Test
        void shouldValidate_InvalidObjects() {
            var contactDetails = new SampleContactDetails("bob@example.org", "");
            var bob = new SamplePerson(null, "S", null, contactDetails);

            var exception = catchThrowableOfType(() -> KiwiValidations.validateThrowing(bob, Default.class, Secret.class), ConstraintViolationException.class);
            var violations = exception.getConstraintViolations();
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
    class ValidateWithGroups {

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
    class CheckArgumentValid {

        private SamplePerson validPerson;
        private SamplePerson invalidPerson;

        @BeforeEach
        void setUp() {
            var validContactDetails = new SampleContactDetails("bob@sacamano.org", "703-555-6677");
            validPerson = new SamplePerson("Bob", "Sacamano", "123-45-6789", validContactDetails);

            invalidPerson = new SamplePerson("Alice", "", null, null);
        }

        @Nested
        class WithNoMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson)).doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                var violations = KiwiValidations.validate(invalidPerson);
                var expectedMessage = KiwiConstraintViolations.simpleCombinedErrorMessage(violations);
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson))
                        .withMessage(expectedMessage);
            }
        }

        @Nested
        class WithMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson, "person is invalid"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson, "person is invalid"))
                        .withMessage("person is invalid");
            }
        }

        @Nested
        class WithMessageTemplate {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson, "invalid person: {}", "bob"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson, "invalid person: {}", "bob"))
                        .withMessage("invalid person: bob");
            }
        }

        @Nested
        class WithMessageCreator {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson,
                        constraintViolations -> f("person has {} errors", constraintViolations.size())))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson,
                                constraintViolations -> f("please fix {} error(s)", constraintViolations.size())))
                        .withMessage("please fix 3 error(s)");
            }
        }
    }

    @Nested
    class CheckArgumentValidWithGroups {

        private SamplePerson validPerson;
        private SamplePerson invalidPerson;
        private Class<?>[] groups;

        @BeforeEach
        void setUp() {
            var validContactDetails = new SampleContactDetails("bob@sacamano.org", "703-555-6677");
            validPerson = new SamplePerson("Bob", "Sacamano", "123-45-6789", validContactDetails);

            invalidPerson = new SamplePerson("Susan", "Ross", "", null);

            groups = new Class[]{Default.class, Secret.class};
        }

        @Nested
        class WithNoMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson, groups))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                var violations = KiwiValidations.validate(invalidPerson, groups);
                var expectedMessage = KiwiConstraintViolations.simpleCombinedErrorMessage(violations);
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson, groups))
                        .withMessage(expectedMessage);
            }
        }

        @Nested
        class WithMessage {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson, "person is invalid", groups))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson, "person is invalid", groups))
                        .withMessage("person is invalid");
            }
        }

        @Nested
        class WithMessageTemplate {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson, "invalid person: {}", List.of("bob"), groups))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson, "invalid person: {}", List.of("bob"), groups))
                        .withMessage("invalid person: bob");
            }
        }

        @Nested
        class WithMessageCreator {

            @Test
            void shouldNotThrow_WhenArgumentIsValid() {
                assertThatCode(() -> KiwiValidations.checkArgumentValid(validPerson,
                        constraintViolations -> f("person has {} errors", constraintViolations.size()), groups))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenArgumentIsNotValid() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentValid(invalidPerson,
                                constraintViolations -> f("please fix {} error(s)", constraintViolations.size()), groups))
                        .withMessage("please fix 2 error(s)");
            }
        }
    }

    @Nested
    class CheckArgumentNoViolations {

        private Set<ConstraintViolation<SamplePerson>> violations;

        @BeforeEach
        void setUp() {
            violations = getConstraintViolations();
        }

        private Set<ConstraintViolation<SamplePerson>> getConstraintViolations() {
            return KiwiValidations.validate(new SamplePerson("", "", "", null));
        }

        @Nested
        class WithNoMessage {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldNotThrow_WhenNullOrEmptyArgument(Set<ConstraintViolation<Person>> violations) {
                assertThatCode(() -> KiwiValidations.checkArgumentNoViolations(violations)).doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenNonEmptyArgument() {
                var expectedMessage = KiwiConstraintViolations.simpleCombinedErrorMessage(violations);
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentNoViolations(violations))
                        .withMessage(expectedMessage);
            }
        }

        @Nested
        class WithMessage {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldNotThrow_WhenNullOrEmptyArgument(Set<ConstraintViolation<Person>> violations) {
                assertThatCode(() -> KiwiValidations.checkArgumentNoViolations(violations, "invalid argument"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenNonEmptyArgument() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentNoViolations(violations, "invalid argument"))
                        .withMessage("invalid argument");
            }
        }

        @Nested
        class WithMessageTemplate {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldNotThrow_WhenNullOrEmptyArgument(Set<ConstraintViolation<Person>> violations) {
                assertThatCode(() -> KiwiValidations.checkArgumentNoViolations(violations, "invalid {} argument: {}", "foo", "bar"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenNonEmptyArgument() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentNoViolations(violations, "invalid {} argument: {}", "foo", "bar"))
                        .withMessage("invalid foo argument: bar");
            }
        }

        @Nested
        class WithMessageCreator {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldNotThrow_WhenNullOrEmptyArgument(Set<ConstraintViolation<Person>> violations) {
                assertThatCode(() -> KiwiValidations.checkArgumentNoViolations(violations, constraintViolations -> "created message"))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrowIllegalArgument_WhenNonEmptyArgument() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentNoViolations(violations, constraintViolations -> "num violations: " + constraintViolations.size()))
                        .withMessage("num violations: %d", violations.size());
            }

            @Test
            void shouldThrowIllegalArgument_WhenNonEmptyArgument_CatchingExceptionsThrownByFunction() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> KiwiValidations.checkArgumentNoViolations(violations, constraintViolations -> {
                            throw new RuntimeException("error creating message");
                        }))
                        .withMessage(KiwiConstraintViolations.simpleCombinedErrorMessage(violations));
            }
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
            assertThat(violation.getMessage()).isEqualTo("will always be regarded as invalid! (sorry)");
        }

        @Test
        void shouldAddErrorFromGivenTemplateAndPropertyName() {
            var altThing = new AlternateThing();
            altThing.setName("The Awesome");

            var violations = KiwiValidations.validate(altThing);

            assertThat(violations).hasSize(1);

            var violation = violations.stream().findFirst().orElseThrow();
            assertThat(violation.getPropertyPath()).hasToString("theProperty");
            assertThat(violation.getMessage()).isEqualTo("will always be regarded as invalid! (oops)");
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
            KiwiValidations.addError(context, "will always be regarded as invalid! {suffix}");

            return false;
        }
    }

    /**
     * Used to demonstrate enabling EL in custom validators.
     */
    @Documented
    @Constraint(validatedBy = AlwaysInvalidZipCodeValidator.class)
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @interface AlwaysInvalidZipCode {
        String message() default "";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Used to demonstrate enabling EL in custom validators.
     * <p>
     * <strong>Must be public (otherwise Hibernate Validator cannot instantiate it)</strong>
     */
    public static class AlwaysInvalidZipCodeValidator implements ConstraintValidator<AlwaysInvalidZipCode, CharSequence> {

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            // This is specific to Hibernate Validator (as of 2022-06-09), so we have to go outside
            // the standard Bean Validation APIs and use the Hibernate Validator API directly.
            var hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);

            hibernateContext.disableDefaultConstraintViolation();
            hibernateContext.addExpressionVariable("validatedValue", value)
                    .addExpressionVariable("now", LocalDate.now().getYear())
                    .buildConstraintViolationWithTemplate("'${validatedValue}' is not a valid ZIP code as of ${now}")
                    .enableExpressionLanguage()
                    .addConstraintViolation();

            return false;
        }
    }

    /**
     * Used with {@link AddError}.
     */
    @Documented
    @Constraint(validatedBy = AlwaysInvalidTypeValidator.class)
    @Target({TYPE})
    @Retention(RUNTIME)
    @interface AlwaysInvalidType {
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
    public static class AlwaysInvalidTypeValidator implements ConstraintValidator<AlwaysInvalidType, AlternateThing> {

        @Override
        public boolean isValid(AlternateThing value, ConstraintValidatorContext context) {
            KiwiValidations.addError(context,
                    "will always be regarded as invalid! {suffix}",
                    "theProperty");

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

    /**
     * Used with {@link AddError}.
     */
    @Data
    @AlwaysInvalidType
    static class AlternateThing {

        private String name;
    }

    @Data
    static class Person {

        @AlwaysInvalidZipCode
        private String zipCode;
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

        @Test
        void shouldReturnNull_WhenBeanIsNull() {
            assertThat(KiwiValidations.getPropertyValue(null, "lastName")).isNull();
        }

        @ParameterizedTest
        @BlankStringSource
        void shouldReturnNull_WhenFieldNameIsBlank(String input) {
            var contactDetails = new SampleContactDetails("bob@gmail.com", "703-555-1212");

            assertThat(KiwiValidations.getPropertyValue(contactDetails, input)).isNull();
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
