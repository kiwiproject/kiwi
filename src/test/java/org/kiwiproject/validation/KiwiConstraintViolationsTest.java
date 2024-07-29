package org.kiwiproject.validation;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMultimap;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.assertj.guava.api.Assertions;
import org.hibernate.validator.constraints.Length;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDate;
import java.util.List;
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
                    .toList();

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

            // We cannot rely on a deterministic violation order (since it is a Set), so check
            // that the message contains both expected messages and that there is a comma separator
            var fullNameErrorMessage = errorMessages.get("Full Name");
            assertThat(fullNameErrorMessage)
                    .contains(first(nameViolations))
                    .contains(", ")
                    .contains(second(nameViolations));
        }
    }

    @Nested
    class AsMap {

        @Test
        void shouldReturnEmptyMap_WhenGivenEmptySet() {
            assertThat(KiwiConstraintViolations.asMap(Set.of()))
                    .isUnmodifiable()
                    .isEmpty();
        }

        @Test
        void shouldThrowIllegalState_WhenMoreThanOneViolation_ForSomeProperty() {
            var person = new Person("@", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiConstraintViolations.asMap(violations));
        }

        @Test
        void shouldCollectViolationsIntoMapKeyedByPropertyPath() {
            var person = new Person("X", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMap = KiwiConstraintViolations.asMap(violations);

            assertAll(
                    () -> assertThat(violationMap).isUnmodifiable(),
                    () -> assertThat(violationMap).containsOnlyKeys("fullName", "birthDate", "contactInfo.email.address"),

                    () -> assertThat(violationMap).extractingByKey("fullName")
                            .extracting("message")
                            .isEqualTo("length must be between 2 and 2147483647"),

                    () -> assertThat(violationMap).extractingByKey("birthDate")
                            .extracting("message")
                            .isEqualTo("must not be null"),

                    () -> assertThat(violationMap).extractingByKey("contactInfo.email.address")
                            .extracting("message")
                            .isEqualTo("must be a well-formed email address")
            );
        }

        @Test
        void shouldCollectViolationsIntoMap_UsingCustomPathTransformerFunction() {
            var person = new Person("X", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMap = KiwiConstraintViolations.asMap(violations, KiwiConstraintViolations::humanize);

            assertAll(
                    () -> assertThat(violationMap).isUnmodifiable(),
                    () -> assertThat(violationMap).containsOnlyKeys("Full Name", "Birth Date", "Contact Info / Email / Address")
            );
        }
    }

    @Nested
    class AsSingleValuedMap {

        @Test
        void shouldReturnEmptyMap_WhenGivenEmptySet() {
            assertThat(KiwiConstraintViolations.asSingleValuedMap(Set.of()))
                    .isUnmodifiable()
                    .isEmpty();
        }

        @Test
        void shouldNotThrowIllegalState_WhenMoreThanOneViolation_ForSomeProperty() {
            var person = new Person("@", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            assertThatCode(() -> KiwiConstraintViolations.asSingleValuedMap(violations))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldCollectViolationsIntoMapKeyedByPropertyPath() {
            var person = new Person("!", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMap = KiwiConstraintViolations.asSingleValuedMap(violations);

            assertAll(
                    () -> assertThat(violationMap).isUnmodifiable(),

                    () -> assertThat(violationMap).containsOnlyKeys("fullName", "birthDate", "contactInfo.email.address"),

                    () -> assertThat(violationMap).extractingByKey("fullName")
                            .extracting("message")
                            .describedAs("Order is non-deterministic, so must be one of the messages")
                            .isIn("must not be empty",
                                    "length must be between 2 and 2147483647",
                                    "must include only alphabetic characters (upper or lower case)"),

                    () -> assertThat(violationMap).extractingByKey("birthDate")
                            .extracting("message")
                            .isEqualTo("must not be null"),

                    () -> assertThat(violationMap).extractingByKey("contactInfo.email.address")
                            .extracting("message")
                            .isEqualTo("must be a well-formed email address")
            );
        }

        @Test
        void shouldCollectViolationsIntoMap_UsingCustomPathTransformerFunction() {
            var person = new Person("!", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMap = KiwiConstraintViolations.asSingleValuedMap(violations, KiwiConstraintViolations::humanize);

            assertAll(
                    () -> assertThat(violationMap).isUnmodifiable(),
                    () -> assertThat(violationMap).containsOnlyKeys("Full Name", "Birth Date", "Contact Info / Email / Address")
            );
        }
    }

    @Nested
    class AsMultiValuedMap {

        @Test
        void shouldReturnEmptyMap_WhenGivenEmptySet() {
            assertThat(KiwiConstraintViolations.asMultiValuedMap(Set.of()))
                    .isUnmodifiable()
                    .isEmpty();
        }

        @Test
        void shouldCollectViolationsIntoMapKeyedByPropertyPath() {
            var person = new Person("!", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMap = KiwiConstraintViolations.asMultiValuedMap(violations);

            assertAll(
                    () -> assertThat(violationMap).isUnmodifiable(),

                    () -> assertThat(violationMap).containsOnlyKeys("fullName", "birthDate", "contactInfo.email.address"),

                    () -> assertThat(violationMap.get("fullName"))
                            .hasSize(2)
                            .extracting("message")
                            .contains(
                                    "length must be between 2 and 2147483647",
                                    "must include only alphabetic characters (upper or lower case)"),

                    () -> assertThat(violationMap.get("birthDate"))
                            .hasSize(1)
                            .extracting("message")
                            .contains("must not be null"),

                    () -> assertThat(violationMap.get("contactInfo.email.address"))
                            .hasSize(1)
                            .extracting("message")
                            .contains("must be a well-formed email address")
            );
        }

        @Test
        void shouldCollectViolationsIntoMap_UsingCustomPathTransformerFunction() {
            var person = new Person("!", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMap = KiwiConstraintViolations.asMultiValuedMap(violations, KiwiConstraintViolations::humanize);

            assertAll(
                    () -> assertThat(violationMap).isUnmodifiable(),
                    () -> assertThat(violationMap).containsOnlyKeys("Full Name", "Birth Date", "Contact Info / Email / Address")
            );
        }
    }

    @Nested
    class AsMultimap {

        @Test
        void shouldReturnEmptyMap_WhenGivenEmptySet() {
            var multimap = KiwiConstraintViolations.asMultimap(Set.of());
            assertAll(
                    () -> assertThat(multimap).isInstanceOf(ImmutableMultimap.class),
                    () -> assertThat(multimap.size()).isZero()
            );
        }

        @Test
        void shouldCollectViolationsIntoMultimapKeyedByPropertyPath() {
            var person = new Person("!", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMultimap = KiwiConstraintViolations.asMultimap(violations);

            assertAll(
                    () -> assertThat(violationMultimap).isInstanceOf(ImmutableMultimap.class),

                    () -> Assertions.assertThat(violationMultimap)
                            .containsKeys("fullName", "birthDate", "contactInfo.email.address"),

                    () -> assertThat(violationMultimap.get("fullName"))
                            .extracting("message")
                            .hasSize(2)
                            .contains(
                                    "length must be between 2 and 2147483647",
                                    "must include only alphabetic characters (upper or lower case)"),

                    () -> assertThat(violationMultimap.get("birthDate"))
                            .extracting("message")
                            .hasSize(1)
                            .contains("must not be null"),

                    () -> assertThat(violationMultimap.get("contactInfo.email.address"))
                            .extracting("message")
                            .hasSize(1)
                            .contains("must be a well-formed email address")
            );
        }

        @Test
        void shouldCollectViolationsIntoMultimap_UsingCustomPathTransformerFunction() {
            var person = new Person("!", 42, null, new ContactInfo(new EmailAddress("bob")));
            var violations = validator.validate(person);

            var violationMultimap = KiwiConstraintViolations.asMultimap(violations, KiwiConstraintViolations::humanize);

            assertAll(
                    () -> assertThat(violationMultimap).isInstanceOf(ImmutableMultimap.class),
                    () -> Assertions.assertThat(violationMultimap)
                            .containsKeys("Full Name", "Birth Date", "Contact Info / Email / Address")
            );
        }
    }

    @Nested
    class PathStringOf {

        @Test
        void shouldGetPathForSimpleProperties() {
            assertAll(
                    () -> assertPersonPropertyPathString("fullName", "X"),
                    () -> assertPersonPropertyPathString("age", -1),
                    () -> assertPersonPropertyPathString("age", 151),
                    () -> assertPersonPropertyPathString("birthDate", null),
                    () -> assertPersonPropertyPathString("contactInfo.email", null),
                    () -> assertPersonPropertyPathString("contactInfo.email.address", null),
                    () -> assertPersonPropertyPathString("contactInfo.email.address", "")
            );
        }

        private void assertPersonPropertyPathString(String propertyName, Object value) {
            var violations = validator.validateValue(Person.class, propertyName, value);
            assertThat(violations)
                    .describedAs("Expected only one violation on %s but found %d", propertyName, violations.size())
                    .hasSize(1);

            var violation = violations.iterator().next();
            assertThat(KiwiConstraintViolations.pathStringOf(violation)).isEqualTo(propertyName);
        }

        @Test
        void shouldGetPathForIndexedProperties() {
            var passwordHints = List.of(new Hint(""), new Hint(null));
            var nicknames = Set.of("bobby", "booby jo");
            var user = new User("bob_jones", "monkey-123-456", passwordHints, nicknames);
            var violations = validateAndFilterByPropertyPathContains(user, "passwordHints", 2);

            assertAll(
                    () -> assertThat(KiwiConstraintViolations.pathStringOf(first(violations)))
                            .isEqualTo("passwordHints[0].text"),

                    () -> assertThat(KiwiConstraintViolations.pathStringOf(second(violations)))
                            .isEqualTo("passwordHints[1].text")
            );
        }

        @Test
        void shouldGetPathForIterableProperties() {
            var passwordHints = List.of(new Hint("bananas"));
            var nicknames = Set.of("", " ", "    ");
            var user = new User("bob_jones", "monkey-123-456", passwordHints, nicknames);
            var violations = validateAndFilterByPropertyPathContains(user, "nicknames", 3);

            var expectedPropertyPath = "nicknames[].<iterable element>";
            assertAll(
                    () -> assertThat(KiwiConstraintViolations.pathStringOf(first(violations)))
                            .isEqualTo(expectedPropertyPath),

                    () -> assertThat(KiwiConstraintViolations.pathStringOf(second(violations)))
                            .isEqualTo(expectedPropertyPath),

                    () -> assertThat(KiwiConstraintViolations.pathStringOf(third(violations)))
                            .isEqualTo(expectedPropertyPath)
            );
        }

        private List<ConstraintViolation<User>> validateAndFilterByPropertyPathContains(
                User user,
                String pathSubstring,
                int expectedViolations) {

            var violations = validator.validate(user).stream()
                    .filter(violation -> {
                        var path = violation.getPropertyPath().toString();
                        return path.contains(pathSubstring);
                    })
                    .sorted(comparing(violation -> violation.getPropertyPath().toString()))
                    .toList();

            assertThat(violations)
                    .describedAs("Precondition failed: expected %d violations on %s", expectedViolations, pathSubstring)
                    .hasSize(expectedViolations);

            return violations;
        }

        record User(
                @NotBlank
                @Length(min = 6)
                String userName,

                @NotBlank
                @Length(min = 12)
                String password,

                @NotEmpty
                List<@NotNull @Valid Hint> passwordHints,

                @NotNull
                Set<@NotBlank String> nicknames
        ) {
        }

        record Hint(@NotBlank String text) {
        }
    }

    private <T> ConstraintViolation<T> firstViolation(T object, String property) {
        return validator.validateProperty(object, property).iterator().next();
    }

    @Getter
    @AllArgsConstructor
    private static class Person {

        @NotEmpty(message = "must not be empty")
        @Length(min = 2, message = "length must be between 2 and 2147483647")
        @Pattern(regexp = "\\p{Alpha}+", message = "must include only alphabetic characters (upper or lower case)")
        private final String fullName;

        @NotNull(message = "must not be null")
        @Min(value = 0, message = "must be greater than zero")
        @Max(value = 150, message = "must be less than 150")
        private final Integer age;

        @NotNull(message = "must not be null")
        private final LocalDate birthDate;

        @Valid
        private final ContactInfo contactInfo;
    }

    @Getter
    @AllArgsConstructor
    private static class ContactInfo {

        @NotNull(message = "must not be null")
        @Valid
        private final EmailAddress email;
    }

    @Getter
    @AllArgsConstructor
    private static class EmailAddress {

        @NotEmpty(message = "must not be blank")
        @Email(message = "must be a well-formed email address")
        private final String address;
    }
}
