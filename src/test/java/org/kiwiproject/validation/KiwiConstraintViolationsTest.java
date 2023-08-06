package org.kiwiproject.validation;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@DisplayName("KiwiConstraintViolations")
class KiwiConstraintViolationsTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class Humanize {

        @ParameterizedTest
        @CsvSource({
                "age, Age",
                "fullName, Full Name",
                "SSN, SSN",
                "socialSecurityNumber, Social Security Number",
                "employee.address, Employee / Address",
                "contactInfo.email.address, Contact Info / Email / Address"
        })
        void shouldReturnHumanReadableString(String propertyPath, String expectedResult) {
            assertThat(KiwiConstraintViolations.humanize(pathFor(propertyPath))).isEqualTo(expectedResult);
        }

        @ParameterizedTest
        @CsvSource({
                "age, Age",
                "fullName, Full Name",
                "SSN, SSN",
                "socialSecurityNumber, Social Security Number",
                "employee.address, Employee :: Address",
                "contactInfo.email.address, Contact Info :: Email :: Address"
        })
        void shouldSupportCustomPathSeparator(String propertyPath, String expectedResult) {
            assertThat(KiwiConstraintViolations.humanize(pathFor(propertyPath), "::")).isEqualTo(expectedResult);
        }
    }

    private static Path pathFor(String pathStr) {
        var path = mock(Path.class);
        when(path.toString()).thenReturn(pathStr);
        return path;
    }

    @Nested
    class SimpleCombinedErrorMessage {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiConstraintViolations.simpleCombinedErrorMessage(set));
        }

        @Test
        void shouldSortPropertiesInErrorMessages() {
            assertSimpleCombinedErrorMessage(KiwiConstraintViolations::simpleCombinedErrorMessage);
        }
    }

    @Nested
    class SimpleCombinedErrorMessageOrNull {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_ForNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.simpleCombinedErrorMessageOrNull(set)).isNull();
        }

        @Test
        void shouldSortPropertiesInErrorMessages() {
            assertSimpleCombinedErrorMessage(KiwiConstraintViolations::simpleCombinedErrorMessageOrNull);
        }
    }

    @Nested
    class SimpleCombinedErrorMessageOrEmpty {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_ForNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.simpleCombinedErrorMessageOrEmpty(set)).isEmpty();
        }

        @Test
        void shouldSortPropertiesInErrorMessages() {
            assertSimpleCombinedErrorMessage(violations ->
                    KiwiConstraintViolations.simpleCombinedErrorMessageOrEmpty(violations).orElseThrow());
        }
    }

    private void assertSimpleCombinedErrorMessage(Function<Set<ConstraintViolation<Person>>, String> fn) {
        var bob = new Person("B", 151, null, null);
        var nameViolation = firstViolation(bob, "fullName");
        var ageViolation = firstViolation(bob, "age");
        var birthDateViolation = firstViolation(bob, "birthDate");

        var violations = validator.validate(bob);

        var result = fn.apply(violations);
        assertThat(result).isEqualTo("age " + ageViolation.getMessage()
                + ", birthDate " + birthDateViolation.getMessage()
                + ", fullName " + nameViolation.getMessage());
    }

    @Nested
    class PrettyCombinedErrorMessage {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiConstraintViolations.prettyCombinedErrorMessage(set));
        }

        @Test
        void shouldSortPropertiesInErrorMessage() {
            assertPrettyCombinedErrorMessage(KiwiConstraintViolations::prettyCombinedErrorMessage);
        }
    }

    @Nested
    class PrettyCombinedErrorMessageOrNull {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_ForNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.prettyCombinedErrorMessageOrNull(set)).isNull();
        }

        @Test
        void shouldSortPropertiesInErrorMessage() {
            assertPrettyCombinedErrorMessage(KiwiConstraintViolations::prettyCombinedErrorMessageOrNull);
        }
    }

    @Nested
    class PrettyCombinedErrorMessageOrEmpty {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional_ForNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.prettyCombinedErrorMessageOrEmpty(set)).isEmpty();
        }

        @Test
        void shouldSortPropertiesInErrorMessage() {
            assertPrettyCombinedErrorMessage(violations ->
                    KiwiConstraintViolations.prettyCombinedErrorMessageOrEmpty(violations).orElseThrow());
        }
    }

    private void assertPrettyCombinedErrorMessage(Function<Set<ConstraintViolation<Person>>, String> fn) {
        var bob = new Person("B", 151, null, null);
        var nameViolation = firstViolation(bob, "fullName");
        var ageViolation = firstViolation(bob, "age");
        var birthDateViolation = firstViolation(bob, "birthDate");

        var violations = validator.validate(bob);

        var result = fn.apply(violations);
        assertThat(result).isEqualTo("Age " + ageViolation.getMessage()
                + ", Birth Date " + birthDateViolation.getMessage()
                + ", Full Name " + nameViolation.getMessage());
    }

    @Nested
    class CombinedErrorMessage {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiConstraintViolations.combinedErrorMessage(set, Object::toString))
                    .withMessage("There are no violations to combine");
        }

        @Test
        void shouldBuildMessage_ForOnlyOneViolation() {
            var bob = new Person("Bob", -1, LocalDate.of(1973, 9, 8), null);
            var ageViolation = firstViolation(bob, "age");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.combinedErrorMessage(violations, KiwiConstraintViolations::humanize))
                    .isEqualTo("Age " + ageViolation.getMessage());
        }

        @Test
        void shouldPrettifyAndSortPropertiesInMessage() {
            var bob = new Person("B", 151, null, null);
            var nameViolation = firstViolation(bob, "fullName");
            var ageViolation = firstViolation(bob, "age");
            var birthDateViolation = firstViolation(bob, "birthDate");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.combinedErrorMessage(violations, KiwiConstraintViolations::humanize))
                    .isEqualTo("Age " + ageViolation.getMessage()
                            + ", Birth Date " + birthDateViolation.getMessage()
                            + ", Full Name " + nameViolation.getMessage());
        }

        @Test
        void shouldWorkWithCascadedValidation() {
            var contactInfo = new ContactInfo(new EmailAddress(""));
            var bob = new Person("Bob", 47, LocalDate.of(1972, 12, 26), contactInfo);

            var emailViolation = firstViolation(bob, "contactInfo.email.address");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.combinedErrorMessage(violations, KiwiConstraintViolations::humanize))
                    .isEqualTo("Contact Info / Email / Address " + emailViolation.getMessage());
        }
    }

    @Nested
    class CombinedErrorMessageOrNull {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_WhenNoViolationsExist(Set<ConstraintViolation<Person>> violations) {
            var combined = KiwiConstraintViolations.combinedErrorMessageOrNull(violations, Object::toString);
            assertThat(combined).isNull();
        }

        @Test
        void shouldReturnCombinedMessage_WhenViolationsExist() {
            var bob = new Person("B", -1, LocalDate.of(1973, 9, 8), null);
            var violations = validator.validate(bob);

            var ageViolation = firstViolation(bob, "age");
            var nameViolation = firstViolation(bob, "fullName");
            assertThat(KiwiConstraintViolations.combinedErrorMessageOrNull(violations, Object::toString))
                    .isEqualTo("age %s, fullName %s", ageViolation.getMessage(), nameViolation.getMessage());
        }
    }

    @Nested
    class CombinedErrorMessageOrEmpty {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_WhenNoViolationsExist(Set<ConstraintViolation<Person>> violations) {
            var combined = KiwiConstraintViolations.combinedErrorMessageOrEmpty(violations, Object::toString);
            assertThat(combined).isEmpty();
        }

        @Test
        void shouldReturnCombinedMessage_WhenViolationsExist() {
            var alice = new Person("A", -1, null, null);
            var violations = validator.validate(alice);

            var ageViolation = firstViolation(alice, "age");
            var birthDateViolation = firstViolation(alice, "birthDate");
            var nameViolation = firstViolation(alice, "fullName");
            var message = KiwiConstraintViolations.combinedErrorMessageOrEmpty(violations, Object::toString);

            var expectedMessage = f("age {}, birthDate {}, fullName {}",
                    ageViolation.getMessage(), birthDateViolation.getMessage(), nameViolation.getMessage());
            assertThat(message).contains(expectedMessage);
        }
    }

    @Nested
    class SimpleCombinedErrorMessages {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.simpleCombinedErrorMessages(set)).isEmpty();
        }

        @Test
        void shouldSortMessagesByPropertyName() {
            var bob = new Person("B", -1, LocalDate.of(1973, 9, 8), null);
            var nameViolation = firstViolation(bob, "fullName");
            var ageViolation = firstViolation(bob, "age");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.simpleCombinedErrorMessages(violations))
                    .containsExactly(
                            "age " + ageViolation.getMessage(),
                            "fullName " + nameViolation.getMessage());
        }
    }

    @Nested
    class PrettyCombinedErrorMessages {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.prettyCombinedErrorMessages(set)).isEmpty();
        }

        @Test
        void shouldPrettifyAndSortPropertiesInMessage() {
            var bob = new Person("B", 151, null, null);
            var nameViolation = firstViolation(bob, "fullName");
            var ageViolation = firstViolation(bob, "age");
            var birthDateViolation = firstViolation(bob, "birthDate");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.prettyCombinedErrorMessages(violations))
                    .containsExactly(
                            "Age " + ageViolation.getMessage(),
                            "Birth Date " + birthDateViolation.getMessage(),
                            "Full Name " + nameViolation.getMessage()
                    );
        }
    }

    @Nested
    class CombinedErrorMessages {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.combinedErrorMessages(set, Object::toString)).isEmpty();
        }

        @Test
        void shouldBuildMessages_ForOnlyOneViolation() {
            var bob = new Person("Bob", -1, LocalDate.of(1973, 9, 8), null);
            var ageViolation = firstViolation(bob, "age");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.combinedErrorMessages(violations, Objects::toString))
                    .containsExactly("age " + ageViolation.getMessage());
        }

        @Test
        void shouldBuildMessages_ByPropertyName() {
            var bob = new Person("B", 151, null, null);
            var nameViolation = firstViolation(bob, "fullName");
            var ageViolation = firstViolation(bob, "age");
            var birthDateViolation = firstViolation(bob, "birthDate");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.combinedErrorMessages(violations, Objects::toString))
                    .containsExactly(
                            "age " + ageViolation.getMessage(),
                            "birthDate " + birthDateViolation.getMessage(),
                            "fullName " + nameViolation.getMessage()
                    );
        }

        @Test
        void shouldWorkWithCascadedValidation() {
            var contactInfo = new ContactInfo(new EmailAddress(""));
            var bob = new Person("Bob", 47, LocalDate.of(1972, 12, 26), contactInfo);

            var emailViolation = firstViolation(bob, "contactInfo.email.address");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.combinedErrorMessages(violations, Objects::toString))
                    .containsExactly("contactInfo.email.address " + emailViolation.getMessage());
        }
    }

    @Nested
    class SimpleCombineErrorMessagesIntoMap {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.simpleCombineErrorMessagesIntoMap(set)).isEmpty();
        }

        @Test
        void shouldBuildMap() {
            var bob = new Person("B", 151, null, null);
            var nameViolation = firstViolation(bob, "fullName");
            var ageViolation = firstViolation(bob, "age");
            var birthDateViolation = firstViolation(bob, "birthDate");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.simpleCombineErrorMessagesIntoMap(violations))
                    .containsOnly(
                            entry("age", ageViolation.getMessage()),
                            entry("birthDate", birthDateViolation.getMessage()),
                            entry("fullName", nameViolation.getMessage())
                    );
        }
    }

    @Nested
    class PrettyCombineErrorMessagesIntoMap {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.prettyCombineErrorMessagesIntoMap(set)).isEmpty();
        }

        @Test
        void shouldBuildMap() {
            var bob = new Person("B", 151, null, null);
            var nameViolation = firstViolation(bob, "fullName");
            var ageViolation = firstViolation(bob, "age");
            var birthDateViolation = firstViolation(bob, "birthDate");

            var violations = validator.validate(bob);

            assertThat(KiwiConstraintViolations.prettyCombineErrorMessagesIntoMap(violations))
                    .containsOnly(
                            entry("Age", ageViolation.getMessage()),
                            entry("Birth Date", birthDateViolation.getMessage()),
                            entry("Full Name", nameViolation.getMessage())
                    );
        }
    }

    @Nested
    class CombineErrorMessagesIntoMap {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldAllowNullOrEmptyArgument(Set<ConstraintViolation<Object>> set) {
            assertThat(KiwiConstraintViolations.combineErrorMessagesIntoMap(set, Objects::toString)).isEmpty();
        }

        @Test
        void shouldBuildMap() {
            var contactInfo = new ContactInfo(new EmailAddress(""));
            var bob = new Person("7", -1, LocalDate.of(1972, 12, 26), contactInfo);
            var ageViolation = firstViolation(bob, "age");
            var emailViolation = firstViolation(bob, "contactInfo.email.address");
            var nameViolations = validator.validateProperty(bob, "fullName").stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(toList());

            var violations = validator.validate(bob);

            Function<Path, String> transformer = path -> KiwiConstraintViolations.humanize(path, "//");
            var errorMessages = KiwiConstraintViolations.combineErrorMessagesIntoMap(violations, transformer);

            assertThat(errorMessages)
                    .hasSize(3)
                    .containsKey("Full Name")
                    .contains(
                            entry("Age", ageViolation.getMessage()),
                            entry("Contact Info // Email // Address", emailViolation.getMessage())
                    );

            // We cannot rely on a deterministic violation order (since it is a Set), so just check
            // that the message contains both expected messages and that there is a comma separator
            var fullNameErrorMessage = errorMessages.get("Full Name");
            assertThat(fullNameErrorMessage)
                    .contains(first(nameViolations))
                    .contains(", ")
                    .contains(second(nameViolations));
        }
    }

    private <T> ConstraintViolation<T> firstViolation(T object, String property) {
        return validator.validateProperty(object, property).iterator().next();
    }

    @Getter
    @AllArgsConstructor
    private static class Person {

        @NotEmpty
        @Length(min = 2)
        @Pattern(regexp = "\\p{Alpha}+", message = "must include only alphabetic characters (upper or lower case)")
        private final String fullName;

        @NotNull
        @Min(0)
        @Max(150)
        private final Integer age;

        @NotNull
        private final LocalDate birthDate;

        @Valid
        private final ContactInfo contactInfo;
    }

    @Getter
    @AllArgsConstructor
    private static class ContactInfo {

        @Valid
        private final EmailAddress email;
    }

    @Getter
    @AllArgsConstructor
    private static class EmailAddress {

        @NotEmpty
        @Email
        private final String address;
    }
}
