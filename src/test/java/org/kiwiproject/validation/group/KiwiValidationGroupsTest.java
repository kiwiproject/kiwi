package org.kiwiproject.validation.group;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.groups.Default;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

@DisplayName("KiwiValidationGroups")
class KiwiValidationGroupsTest {

    @Nested
    class NewObjectGroups {

        @Test
        void shouldReturnNewObjectGroups() {
            assertThat(KiwiValidationGroups.newObjectGroups()).containsExactly(
                    NewObject.class,
                    Default.class);
        }

        @Test
        void shouldReturnCopy() {
            var groups1 = KiwiValidationGroups.newObjectGroups();
            var groups2 = KiwiValidationGroups.newObjectGroups();
            var groups3 = KiwiValidationGroups.newObjectGroups();

            assertThat(groups1)
                    .isNotSameAs(groups2)
                    .isNotSameAs(groups3);
        }
    }

    @Nested
    class ExistingObjectGroups {

        @Test
        void shouldReturnExistingObjectGroups() {
            assertThat(KiwiValidationGroups.existingObjectGroups()).containsExactly(
                    ExistingObject.class,
                    Default.class);
        }

        @Test
        void shouldReturnCopy() {
            var groups1 = KiwiValidationGroups.existingObjectGroups();
            var groups2 = KiwiValidationGroups.existingObjectGroups();
            var groups3 = KiwiValidationGroups.existingObjectGroups();

            assertThat(groups1)
                    .isNotSameAs(groups2)
                    .isNotSameAs(groups3);
        }
    }

    @Nested
    class ValidateNewObject {

        @Test
        void shouldValidateEmptyObjects() {
            var user = new User(null, null);

            var invalidPropertyNames = invalidPropertyNamesOf(KiwiValidationGroups.validateNewObject(user));

            assertThat(invalidPropertyNames).containsExactly("userName");
        }

        @Test
        void shouldValidateValidObjects() {
            var user = new User(null, "alice@example.org");

            assertThat(KiwiValidationGroups.validateNewObject(user)).isEmpty();
        }

        @Test
        void shouldRejectObjects_WhenPropertyAnnotatedWithNewObject_IsInvalid() {
            var user = new User(42L, "bob@example.org");

            var invalidPropertyNames = invalidPropertyNamesOf(KiwiValidationGroups.validateNewObject(user));

            assertThat(invalidPropertyNames).containsExactly("id");
        }
    }

    @Nested
    class ValidateExistingObject {

        @Test
        void shouldValidateEmptyObjects() {
            var user = new User(null, null);

            var invalidPropertyNames = invalidPropertyNamesOf(KiwiValidationGroups.validateExistingObject(user));

            assertThat(invalidPropertyNames).containsExactlyInAnyOrder("id", "userName");
        }

        @Test
        void shouldValidateValidObjects() {
            var user = new User(42L, "carlos@example.org");

            assertThat(KiwiValidationGroups.validateExistingObject(user)).isEmpty();
        }

        @Test
        void shouldRejectObjects_WhenPropertyAnnotatedWithExistingObject_IsInvalid() {
            var user = new User(null, "diane@example.org");

            var invalidPropertyNames = invalidPropertyNamesOf(KiwiValidationGroups.validateExistingObject(user));

            assertThat(invalidPropertyNames).containsExactly("id");
        }
    }

    private static <T> List<String> invalidPropertyNamesOf(Set<ConstraintViolation<T>> violations) {
        return violations
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(toUnmodifiableList());
    }

    @Value
    private static class User {

        @Null(groups = NewObject.class)
        @NotNull(groups = ExistingObject.class)
        Long id;

        @NotBlank
        String userName;
    }
}
