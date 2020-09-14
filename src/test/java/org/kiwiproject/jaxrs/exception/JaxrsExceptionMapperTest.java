package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Lists.newArrayList;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseEntityHasOneErrorMessage;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertResponseMediaType;
import static org.kiwiproject.jaxrs.JaxRsTestHelper.assertStatusCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.json.JsonHelper;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@DisplayName("JaxrsExceptionMapper")
class JaxrsExceptionMapperTest {

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    private JaxrsExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JaxrsExceptionMapper();
    }

    @Nested
    class ToResponse {

        @Test
        void shouldCreateResponse_FromMinimalJaxrsException() {
            var ex = new JaxrsException((String) null);
            var response = mapper.toResponse(ex);

            assertStatusCode(response, ErrorMessage.DEFAULT_CODE);
            assertResponseMediaType(response, MediaType.APPLICATION_JSON);
            assertResponseEntityHasOneErrorMessage(response, ErrorMessage.DEFAULT_CODE, ErrorMessage.DEFAULT_MSG);
        }

        @Test
        void shouldIncludeErrors_WhenPresent() {
            var itemId = "42";
            var statusCode = 422;
            var lastNameError = new ErrorMessage(itemId, statusCode, "must not be blank", "lastName");
            var emailError = new ErrorMessage(itemId, statusCode, "not a well-formed email address", "email");
            var ex = new JaxrsException(List.of(lastNameError, emailError), statusCode);

            var response = mapper.toResponse(ex);

            assertStatusCode(response, statusCode);
            assertResponseMediaType(response, MediaType.APPLICATION_JSON);

            var entityObj = response.getEntity();
            assertThat(entityObj).isInstanceOf(Map.class);

            //noinspection unchecked
            var entity = (Map<String, Object>) entityObj;
            assertThat(entity).containsOnly(
                    entry("errors", List.of(lastNameError, emailError))
            );
        }

        @Test
        void shouldIncludeOtherData_WhenPresent() {
            var itemId = "42";
            var statusCode = 422;
            var lastNameError = new ErrorMessage(itemId, statusCode, "must not be blank", "lastName");
            var emailError = new ErrorMessage(itemId, statusCode, "not a well-formed email address", "email");
            var ex = new JaxrsException(List.of(lastNameError, emailError), statusCode);
            ex.setOtherData(Map.of("other1", 1, "other2", 2));

            var response = mapper.toResponse(ex);

            assertStatusCode(response, statusCode);
            assertResponseMediaType(response, MediaType.APPLICATION_JSON);

            var entityObj = response.getEntity();
            assertThat(entityObj).isInstanceOf(Map.class);

            //noinspection unchecked
            var entity = (Map<String, Object>) entityObj;
            assertThat(entity).containsOnly(
                    entry("errors", List.of(lastNameError, emailError)),
                    entry("other1", 1),
                    entry("other2", 2)
            );
        }
    }

    @Nested
    class ToJaxrsExceptionFromResponse {

        @Test
        void shouldReturnNull_WhenNullResponse() {
            assertThat(JaxrsExceptionMapper.toJaxrsException(null)).isNull();
        }

        @Test
        void whenResponseContainsNoEntity() {
            var response = mock(Response.class);
            var statusCode = 500;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(false);
            when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
            );
        }

        @Test
        void whenResponseContainsBlankEntity() {
            var response = mock(Response.class);
            var statusCode = 500;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            when(response.readEntity(String.class)).thenReturn("  ");  // whitespace-only
            when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
            );
        }

        @Test
        void whenErrorReadingResponseEntity() {
            var response = mock(Response.class);
            var statusCode = 500;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            when(response.readEntity(String.class)).thenThrow(new ProcessingException("oop"));

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, ErrorMessage.DEFAULT_MSG)
            );
        }

        @Test
        void whenResponseContainsNonMapEntity() {
            var response = mock(Response.class);
            var statusCode = 500;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            when(response.readEntity(String.class)).thenReturn("An error occurred");

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, "An error occurred")
            );
        }

        @Test
        void whenResponseContainsMapEntity_WithoutErrorsKey() {
            var response = mock(Response.class);
            var statusCode = 500;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            var json = JSON_HELPER.toJsonFromKeyValuePairs("key1", "value1", "key2", "value2");
            when(response.readEntity(String.class)).thenReturn(json);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);

            assertThat(ex.getErrors()).containsExactly(
                    new ErrorMessage(ErrorMessage.DEFAULT_CODE, ErrorMessage.DEFAULT_MSG)
            );
            assertThat(ex.getOtherData()).containsOnly(
                    entry("key1", "value1"),
                    entry("key2", "value2")
            );
        }

        @Test
        void whenResponseContainsMapEntity_WithErrorsKey() {
            var response = mock(Response.class);
            var statusCode = 422;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            var itemId = "42";
            var firstNameError = new ErrorMessage(itemId, statusCode, "must not be blank", "firstName");
            var lastNameError = new ErrorMessage(itemId, statusCode, "must not be blank", "lastName");
            var errors = List.of(firstNameError, lastNameError);
            var json = JSON_HELPER.toJsonFromKeyValuePairs(
                    "key1", "value1", "key2", "value2", "errors", errors);
            when(response.readEntity(String.class)).thenReturn(json);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsOnly(firstNameError, lastNameError);
            assertThat(ex.getOtherData()).containsOnly(
                    entry("key1", "value1"),
                    entry("key2", "value2")
            );
        }

        @Test
        void whenResponseContainsMapEntity_WithErrorsValue_ThatIsNotAList() {
            var response = mock(Response.class);
            var statusCode = 400;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            var json = JSON_HELPER.toJsonFromKeyValuePairs("errors", "what is this???");
            when(response.readEntity(String.class)).thenReturn(json);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsOnly(
                    new ErrorMessage(statusCode, ErrorMessage.DEFAULT_MSG)
            );
            assertThat(ex.getOtherData()).isEmpty();
        }

        @Test
        void whenResponseContainsMapEntity_WithErrorsValueHavingNullsAndErrorMessages() {
            var response = mock(Response.class);
            var statusCode = 400;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            var error1 = new ErrorMessage(400, "You submitted invalid data");
            var error2 = new ErrorMessage("42", 401, "You are not authorized to change this data", "emailAddress");
            var json = JSON_HELPER.toJsonFromKeyValuePairs(
                    "errors", newArrayList(null, error1, null, error2, null));

            when(response.readEntity(String.class)).thenReturn(json);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsOnly(error1, error2);
            assertThat(ex.getOtherData()).isEmpty();
        }

        @Test
        void whenResponseContainsMapEntity_WithErrorsValueHavingInvalidStructure() {
            var response = mock(Response.class);
            var statusCode = 400;
            when(response.getStatus()).thenReturn(statusCode);
            when(response.hasEntity()).thenReturn(true);
            var invalidErrors = List.of(
                    Map.of("foo", 42, "bar", 84, "code", "not a valid HTTP status code")
            );
            var json = JSON_HELPER.toJsonFromKeyValuePairs("errors", invalidErrors);

            when(response.readEntity(String.class)).thenReturn(json);

            var ex = JaxrsExceptionMapper.toJaxrsException(response);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsOnly(
                    new ErrorMessage(ErrorMessage.DEFAULT_CODE, ErrorMessage.DEFAULT_MSG)
            );
            assertThat(ex.getOtherData()).isEmpty();
        }
    }

    @Nested
    class ToJaxrsExceptionFromMap {

        @Test
        void whenMapIsEmpty() {
            var statusCode = 400;
            var ex = JaxrsExceptionMapper.toJaxrsException(statusCode, Map.of());

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);

            assertThat(ex.getErrors()).containsExactly(
                    new ErrorMessage(statusCode, ErrorMessage.DEFAULT_MSG)
            );
            assertThat(ex.getOtherData()).isEmpty();
        }

        @Test
        void whenMapHasNullsAndErrorMessages() {
            var statusCode = 400;
            var error1 = new ErrorMessage(400, "You submitted invalid data");
            var error2 = new ErrorMessage("42", 401, "You are not authorized to change this data", "emailAddress");
            var entity = Map.<String, Object>of(
                    "errors", newArrayList(null, error1, null, error2, null),
                    "someValue", "foo"
            );

            var ex = JaxrsExceptionMapper.toJaxrsException(statusCode, entity);

            assertThat(ex).isNotNull();
            assertThat(ex.getStatusCode()).isEqualTo(statusCode);
            assertThat(ex.getErrors()).containsOnly(error1, error2);
            assertThat(ex.getOtherData()).containsOnly(
                    entry("someValue", "foo")
            );
        }
    }

}