package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.kiwiproject.jaxrs.exception.ErrorMessage;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;
import org.kiwiproject.jaxrs.exception.JaxrsValidationException;
import org.kiwiproject.util.BlankStringArgumentsProvider;
import org.kiwiproject.validation.group.ExistingObject;
import org.kiwiproject.validation.group.KiwiValidationGroups;
import org.kiwiproject.validation.group.NewObject;

import java.util.Map;

@DisplayName("KiwiJaxrsValidations")
class KiwiJaxrsValidationsTest {

    private static final Map<String, String> PROPERTY_PATH_MAP = Map.of(
            "id", "ID",
            "emailAddress", "Email",
            "mobileNumber", "Mobile phone"
    );

    @Nested
    class AssertValid {

        @Test
        void shouldNotThrow_WhenNoValidationErrors() {
            var contact = ContactInfo.builder()
                    .emailAddress("alice@example.org")
                    .mobileNumber("703-555-1212")
                    .build();

            assertThatCode(() -> KiwiJaxrsValidations.assertValid(null, contact))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldThrow_WhenValidationErrorsExist_UsingDefaultValidationGroup() {
            var contact = ContactInfo.builder()
                    .emailAddress("alice")
                    .build();

            var thrown = catchThrowable(() -> KiwiJaxrsValidations.assertValid(null, contact));

            assertThat(thrown).isExactlyInstanceOf(JaxrsValidationException.class);
            var validationException = (JaxrsValidationException) thrown;

            var statusCode = 422;
            assertThat(validationException.getStatusCode()).isEqualTo(statusCode);
            assertThat(validationException.getOtherData()).isEmpty();
            assertThat(validationException.getErrors()).containsExactlyInAnyOrder(
                    new ErrorMessage(statusCode, "must be a well-formed email address", "emailAddress"),
                    new ErrorMessage(statusCode, "must not be blank", "mobileNumber")
            );
        }

        @Test
        void shouldThrow_WhenValidationErrorsExist_UsingSpecifiedValidationGroups() {
            var contact = ContactInfo.builder()
                    .id(1L)
                    .emailAddress("alice@example.org")
                    .build();

            var thrown = catchThrowable(() ->
                    KiwiJaxrsValidations.assertValid("1", contact, KiwiValidationGroups.newObjectGroups()));

            assertThat(thrown).isExactlyInstanceOf(JaxrsValidationException.class);
            var validationException = (JaxrsValidationException) thrown;

            assertThat(validationException.getErrors())
                    .extracting(ErrorMessage::getFieldName)
                    .containsExactlyInAnyOrder("id", "mobileNumber");
        }
    }

    @Nested
    class AssertValidUsingPropertyPathMap {

        @Test
        void shouldNotThrow_WhenNoValidationErrors() {
            var contact = ContactInfo.builder()
                    .emailAddress("alice@example.org")
                    .mobileNumber("703-555-1212")
                    .build();

            assertThatCode(() -> KiwiJaxrsValidations.assertValid(null, contact, PROPERTY_PATH_MAP))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldThrow_WhenValidationErrorsExist_UsingDefaultValidationGroup() {
            var id = "42";
            var contact = ContactInfo.builder()
                    .id(Long.valueOf(id))
                    .emailAddress("")
                    .mobileNumber("703-555-1212")
                    .build();

            var thrown = catchThrowable(() -> KiwiJaxrsValidations.assertValid(id, contact, PROPERTY_PATH_MAP));

            assertThat(thrown).isExactlyInstanceOf(JaxrsValidationException.class);
            var validationException = (JaxrsValidationException) thrown;

            var statusCode = 422;
            assertThat(validationException.getStatusCode()).isEqualTo(statusCode);
            assertThat(validationException.getOtherData()).isEmpty();
            assertThat(validationException.getErrors()).containsExactlyInAnyOrder(
                    new ErrorMessage(id, statusCode, "must not be blank", "Email")
            );
        }

        @Test
        void shouldThrow_WhenValidationErrorsExist_UsingSpecifiedValidationGroups() {
            var contact = ContactInfo.builder()
                    .emailAddress("alice@example.org")
                    .build();

            var thrown = catchThrowable(() ->
                    KiwiJaxrsValidations.assertValid(null, contact, KiwiValidationGroups.existingObjectGroups()));

            assertThat(thrown).isExactlyInstanceOf(JaxrsValidationException.class);
            var validationException = (JaxrsValidationException) thrown;

            assertThat(validationException.getErrors())
                    .extracting(ErrorMessage::getFieldName)
                    .containsExactlyInAnyOrder("id", "mobileNumber");
        }
    }

    @Nested
    class AssertNotBlank {

        @Test
        void shouldNotThrow_WhenStringIsNotBlank() {
            assertThatCode(() ->
                    KiwiJaxrsValidations.assertNotBlank("emailAddress", "diane@example.org"))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ArgumentsSource(BlankStringArgumentsProvider.class)
        void shouldThrow_WhenStringIsBlank(String input) {
            var thrown = catchThrowable(() ->
                    KiwiJaxrsValidations.assertNotBlank("mobileNumber", input));

            assertThat(thrown).isExactlyInstanceOf(JaxrsBadRequestException.class);
            var badRequestException = (JaxrsBadRequestException) thrown;

            var statusCode = 400;
            assertThat(badRequestException.getStatusCode()).isEqualTo(statusCode);
            assertThat(badRequestException.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, KiwiJaxrsValidations.MISSING_VALUE_MESSAGE, "mobileNumber")
            );
        }
    }

    @Nested
    class AssertNotNull {

        @Test
        void shouldNotThrow_WhenObjectIsNotNull() {
            assertThatCode(() ->
                    KiwiJaxrsValidations.assertNotNull("mobileNumber", "703-555-1212"))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldThrow_WhenObjectIsNull() {
            var thrown = catchThrowable(() ->
                    KiwiJaxrsValidations.assertNotNull("emailAddress", null));

            assertThat(thrown).isExactlyInstanceOf(JaxrsBadRequestException.class);
            var badRequestException = (JaxrsBadRequestException) thrown;

            var statusCode = 400;
            assertThat(badRequestException.getStatusCode()).isEqualTo(statusCode);
            assertThat(badRequestException.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, KiwiJaxrsValidations.MISSING_VALUE_MESSAGE, "emailAddress")
            );
        }
    }

    @Nested
    class AssertTrue {

        @Test
        void shouldNotThrow_WhenValueIsTrue() {
            assertThatCode(() ->
                    KiwiJaxrsValidations.assertTrue(true, "It must be true"))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldThrow_WhenValueIsFalse() {
            //noinspection ConstantConditions
            var thrown = catchThrowable(() ->
                    KiwiJaxrsValidations.assertTrue(false, "It must be true"));

            assertThat(thrown).isExactlyInstanceOf(JaxrsBadRequestException.class);
            var badRequestException = (JaxrsBadRequestException) thrown;

            var statusCode = 400;
            assertThat(badRequestException.getStatusCode()).isEqualTo(statusCode);
            assertThat(badRequestException.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, "It must be true")
            );
        }
    }

    @Value
    @Builder
    private static class ContactInfo {

        @Null(groups = NewObject.class)
        @NotNull(groups = ExistingObject.class)
        Long id;

        @NotBlank
        @Email
        String emailAddress;

        @NotBlank
        String mobileNumber;
    }
}
