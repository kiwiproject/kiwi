package org.kiwiproject.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;

import com.google.common.collect.Iterables;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DisplayName("RequiredValidator")
class RequiredValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Test
    void shouldReturnExpectedErrorMessage() {
        var obj = new RequiredDefaults();
        var violations = validator.validate(obj);
        var violation = Iterables.getOnlyElement(violations);
        assertThat(violation.getPropertyPath()).hasToString("value");
        assertThat(violation.getMessage()).isEqualTo("is required");
    }

    @Nested
    class ValidatingStrings {

        @ParameterizedTest
        @ValueSource(strings = {"a", "aa", "a string"})
        void shouldValidateNonBlankStrings(String value) {
            var obj = new RequiredDefaults(value);
            assertNoPropertyViolations(validator, obj, "value");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRequireNonBlankOrEmptyString(String value) {
            var obj = new RequiredDefaults(value);
            assertOnePropertyViolation(validator, obj, "value");
        }

        @Test
        void shouldPermitBlankString() {
            var obj = new AllowsBlankString(" ");
            assertNoPropertyViolations(validator, obj, "value");
        }

        @Test
        void shouldPermitEmptyString() {
            var obj = new AllowsEmptyAndBlankString("");
            assertNoPropertyViolations(validator, obj, "value");
        }
    }

    @Nested
    class ValidatingCollections {

        @Test
        void shouldValidateNonEmptyCollections() {
            var obj = HasCollectionsAndMaps.builder()
                    .requiredList(List.of("a", "b", "c"))
                    .requiredOrEmptyList(List.of(1, 2, 3))
                    .requiredSet(Set.of("one", "two", "three"))
                    .requiredOrEmptySet(Set.of(4, 5, 6))
                    .requiredMap(Map.of("a", 1, "b", 2, "c", 3))
                    .requiredOrEmptyMap(Map.of("d", 4))
                    .build();
            assertNoViolations(validator, obj);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRequireNonEmptyList(List<String> list) {
            var obj = HasCollectionsAndMaps.builder().requiredList(list).build();
            assertOnePropertyViolation(validator, obj, "requiredList");
        }

        @Test
        void shouldAllowEmptyLists() {
            var obj = HasCollectionsAndMaps.builder().requiredOrEmptyList(List.of()).build();
            assertNoPropertyViolations(validator, obj, "requiredOrEmptyList");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRequireNonEmptySet(Set<String> set) {
            var obj = HasCollectionsAndMaps.builder().requiredSet(set).build();
            assertOnePropertyViolation(validator, obj, "requiredSet");
        }

        @Test
        void shouldAllowEmptySets() {
            var obj = HasCollectionsAndMaps.builder().requiredOrEmptySet(Set.of()).build();
            assertNoPropertyViolations(validator, obj, "requiredOrEmptySet");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldRequireNonEmptyMap(Map<String, Integer> map) {
            var obj = HasCollectionsAndMaps.builder().requiredMap(map).build();
            assertOnePropertyViolation(validator, obj, "requiredMap");
        }

        @Test
        void shouldAllowEmptyMaps() {
            var obj = HasCollectionsAndMaps.builder().requiredOrEmptyMap(Map.of()).build();
            assertNoPropertyViolations(validator, obj, "requiredOrEmptyMap");
        }
    }

    @Nested
    class ValidatingCustomObjects {

        @Nested
        class WhenHasIsEmptyMethod {

            @Test
            void shouldRequireNonEmpty() {
                var user = User.builder().requiredPrefs(new Preferences()).build();
                assertOnePropertyViolation(validator, user, "requiredPrefs");
            }

            @Test
            void shouldAllowEmpty() {
                var user = User.builder().requiredOrEmptyPrefs(new Preferences()).build();
                assertNoPropertyViolations(validator, user, "requiredOrEmptyPrefs");
            }
        }

        @Nested
        class WhenHasNoIsEmptyMethod {

            @Test
            void shouldBeValid_WhenGivenNonNullObject() {
                var person = new Person("Alice", new ContactInfo());
                assertNoViolations(validator, person);
            }

            @Test
            void shouldNotBeValid_WhenGivenNullObject() {
                var person = new Person();
                assertOnePropertyViolation(validator, person, "name");
                assertOnePropertyViolation(validator, person, "contactInfo");
            }
        }

        @Nested
        class WhenHasMisbehavingIsEmptyMethod {

            @Test
            void shouldBeValid() {
                var user = User.builder().customizations(new Customizations()).build();
                assertNoPropertyViolations(validator, user, "customizations");
            }
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class RequiredDefaults {
        @Required
        String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class AllowsBlankString {
        @Required(allowBlank = true)
        String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class AllowsEmptyAndBlankString {
        @Required(allowBlank = true, allowEmpty = true)
        String value;
    }

    @Builder
    static class HasCollectionsAndMaps {
        @Required
        List<String> requiredList;

        @Required(allowEmpty = true)
        List<Integer> requiredOrEmptyList;

        @Required
        Set<String> requiredSet;

        @Required(allowEmpty = true)
        Set<Integer> requiredOrEmptySet;

        @Required
        Map<String, Integer> requiredMap;

        @Required(allowEmpty = true)
        Map<String, Integer> requiredOrEmptyMap;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    static class Person {
        @Required
        String name;

        @Required
        ContactInfo contactInfo;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class ContactInfo {
        String email;
        String mobileNumber;
    }

    @Builder
    static class User {
        @Required
        String username;

        @Builder.Default
        @Required
        Preferences requiredPrefs = new Preferences();

        @Builder.Default
        @Required(allowEmpty = true)
        Preferences requiredOrEmptyPrefs = new Preferences();

        @Builder.Default
        @Required
        Customizations customizations = new Customizations();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class Preferences {

        Map<String, String> values = new HashMap<>();

        public boolean isEmpty() {
            return values.isEmpty();
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static class Customizations {

        Map<String, String> values = new HashMap<>();

        public boolean isEmpty() {
            throw new RuntimeException("well, this is unexpected...");
        }
    }
}
