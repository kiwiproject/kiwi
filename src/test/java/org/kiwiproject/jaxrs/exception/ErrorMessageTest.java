package org.kiwiproject.jaxrs.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.kiwiproject.collect.KiwiMaps;
import org.kiwiproject.internal.Fixtures;
import org.kiwiproject.json.JsonHelper;
import org.kiwiproject.util.BlankStringArgumentsProvider;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@DisplayName("ErrorMessage")
@ExtendWith(SoftAssertionsExtension.class)
class ErrorMessageTest {

    private static final String MESSAGE = "the error message";
    private static final String UNKNOWN_ERROR_MESSAGE = "Unknown error";

    private static final int ERROR_CODE = 404;
    private static final int DEFAULT_ERROR_CODE = 500;

    private static final String FIELD_NAME = "the field name";
    private static final String ITEM_ID = "the item id";
    private static final String CODE_KEY = "code";
    private static final String FIELD_NAME_KEY = "fieldName";
    private static final String ITEM_ID_KEY = "itemId";
    private static final String MESSAGE_KEY = "message";

    @Nested
    class ShouldConstruct {

        @ParameterizedTest
        @ArgumentsSource(BlankStringArgumentsProvider.class)
        void withBlankString(String blankString) {
            var errorMessage = new ErrorMessage(blankString);

            assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            assertThat(errorMessage.getFieldName()).isNull();
            assertThat(errorMessage.getItemId()).isNull();
            assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }

        @Test
        void withNonBlankString(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(MESSAGE);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void withResponseStatusAndNonBlankString(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(Response.Status.NOT_FOUND, MESSAGE);

            softly.assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void withZeroStatusCodeAndNonBlankString(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(0, MESSAGE);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void withValidStatusCodeAndNonBlankString(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(ERROR_CODE, MESSAGE);

            softly.assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void withStatusCodeAndMessageAndFieldName(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(ERROR_CODE, MESSAGE, FIELD_NAME);

            softly.assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void withAllArguments(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(ITEM_ID, ERROR_CODE, MESSAGE, FIELD_NAME);

            softly.assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            softly.assertThat(errorMessage.getItemId()).isEqualTo(ITEM_ID);
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }
    }

    @Nested
    class ValueOf {

        @Test
        void whenMapIsEmpty(SoftAssertions softly) {
            var errorMessage = ErrorMessage.valueOf(new HashMap<>());

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }

        @Test
        void whenMapContainsNullProperties(SoftAssertions softly) {
            // Map.of does not permit null values
            Map<String, Object> props = KiwiMaps.newHashMap(
                    CODE_KEY, null,
                    FIELD_NAME_KEY, null,
                    ITEM_ID_KEY, null,
                    MESSAGE_KEY, null
            );

            var errorMessage = ErrorMessage.valueOf(props);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }

        @Test
        void whenMapContainsAllProperties(SoftAssertions softly) {
            Map<String, Object> props = Map.of(
                    CODE_KEY, ERROR_CODE,
                    FIELD_NAME_KEY, FIELD_NAME,
                    ITEM_ID_KEY, ITEM_ID,
                    MESSAGE_KEY, MESSAGE
            );

            var errorMessage = ErrorMessage.valueOf(props);

            softly.assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            softly.assertThat(errorMessage.getItemId()).isEqualTo(ITEM_ID);
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void whenMapIsMissingMessageProperty(SoftAssertions softly) {
            Map<String, Object> props = Map.of(
                    CODE_KEY, ERROR_CODE,
                    FIELD_NAME_KEY, FIELD_NAME,
                    ITEM_ID_KEY, ITEM_ID
            );

            var errorMessage = ErrorMessage.valueOf(props);

            softly.assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            softly.assertThat(errorMessage.getItemId()).isEqualTo(ITEM_ID);
            softly.assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }

        @Test
        void whenMapIsMissingErrorCodeProperty(SoftAssertions softly) {
            Map<String, Object> props = Map.of(
                    FIELD_NAME_KEY, FIELD_NAME,
                    ITEM_ID_KEY, ITEM_ID,
                    MESSAGE_KEY, MESSAGE
            );

            var errorMessage = ErrorMessage.valueOf(props);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            softly.assertThat(errorMessage.getItemId()).isEqualTo(ITEM_ID);
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @Test
        void whenMapContainsInvalidStatusCode(SoftAssertions softly) {
            Map<String, Object> props = Map.of(
                    CODE_KEY, "invalid code",
                    FIELD_NAME_KEY, FIELD_NAME,
                    ITEM_ID_KEY, ITEM_ID,
                    MESSAGE_KEY, MESSAGE
            );

            var errorMessage = ErrorMessage.valueOf(props);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            softly.assertThat(errorMessage.getItemId()).isEqualTo(ITEM_ID);
            softly.assertThat(errorMessage.getMessage()).isEqualTo(MESSAGE);
        }

        @ParameterizedTest
        @ArgumentsSource(BlankStringArgumentsProvider.class)
        void whenMapContainsBlankMessage(String blankMessage) {
            // Map.of does not permit null values; use KiwiMaps instead
            Map<String, Object> props = KiwiMaps.newHashMap(
                    CODE_KEY, ERROR_CODE,
                    FIELD_NAME_KEY, FIELD_NAME,
                    ITEM_ID_KEY, ITEM_ID,
                    MESSAGE_KEY, blankMessage
            );

            var errorMessage = ErrorMessage.valueOf(props);

            assertThat(errorMessage.getCode()).isEqualTo(ERROR_CODE);
            assertThat(errorMessage.getFieldName()).isEqualTo(FIELD_NAME);
            assertThat(errorMessage.getItemId()).isEqualTo(ITEM_ID);
            assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }
    }

    @Nested
    class ToMap {

        @Test
        void shouldConvertToMap() {
            var errorMessage = new ErrorMessage(ITEM_ID, ERROR_CODE, MESSAGE, FIELD_NAME);

            assertThat(errorMessage.toMap()).containsOnly(
                    entry(ITEM_ID_KEY, ITEM_ID),
                    entry(CODE_KEY, ERROR_CODE),
                    entry(MESSAGE_KEY, MESSAGE),
                    entry(FIELD_NAME_KEY, FIELD_NAME)
            );
        }

        @Test
        void shouldConvertToMap_WhenErrorMessage_ContainsNullValues() {
            var errorMessage = new ErrorMessage(null, 0, null, null);

            assertThat(errorMessage.toMap()).containsOnly(
                    entry(ITEM_ID_KEY, null),
                    entry(CODE_KEY, DEFAULT_ERROR_CODE),
                    entry(MESSAGE_KEY, UNKNOWN_ERROR_MESSAGE),
                    entry(FIELD_NAME_KEY, null)
            );
        }

        @Test
        void shouldReturnUnmodifiableMap(SoftAssertions softly) {
            var errorMap = new ErrorMessage(ITEM_ID, ERROR_CODE, MESSAGE, FIELD_NAME).toMap();

            softly.assertThatThrownBy(() -> errorMap.put("newKey", "newValue"))
                    .isExactlyInstanceOf(UnsupportedOperationException.class);

            softly.assertThatThrownBy(() -> errorMap.remove(MESSAGE_KEY))
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class OverriddenObjectMethods {

        /**
         * @implNote This will break is Guava changes their {@link com.google.common.base.MoreObjects.ToStringHelper}
         * implementation. This seems unlikely.
         */
        @Test
        void shouldHaveToString() {
            var errorMessage = new ErrorMessage("42", 422, "validation failed", null);

            assertThat(errorMessage)
                    .hasToString("ErrorMessage{code=422, message=validation failed, fieldName=null, itemId=42}");
        }
    }

    @Nested
    class JsonSerialization {

        private JsonHelper jsonHelper;

        @BeforeEach
        void setUp() {
            jsonHelper = JsonHelper.newDropwizardJsonHelper();
        }

        @Test
        void shouldSerializeAndDeserialize(SoftAssertions softly) {
            var errorMessage = new ErrorMessage(ITEM_ID, 400, MESSAGE, FIELD_NAME);
            var errorMessageAsJson = jsonHelper.toJson(errorMessage);

            var deserializedErrorMessage = jsonHelper.toObject(errorMessageAsJson, ErrorMessage.class);

            softly.assertThat(deserializedErrorMessage).isEqualTo(errorMessage);
            softly.assertThat(deserializedErrorMessage).hasSameHashCodeAs(errorMessage);
        }

        @Test
        void shouldDeserializeWithAllFieldsPresent(SoftAssertions softly) {
            var json = Fixtures.fixture("ErrorMessageTest/errorMessage42.json");
            var errorMessage = jsonHelper.toObject(json, ErrorMessage.class);

            softly.assertThat(errorMessage.getCode()).isEqualTo(422);
            softly.assertThat(errorMessage.getFieldName()).isEqualTo("firstName");
            softly.assertThat(errorMessage.getItemId()).isEqualTo("42");
            softly.assertThat(errorMessage.getMessage()).isEqualTo("must not be null");
        }

        @Test
        void shouldDeserializeWithAllFieldsNull(SoftAssertions softly) {
            var json = Fixtures.fixture("ErrorMessageTest/errorMessageAllNulls.json");
            var errorMessage = jsonHelper.toObject(json, ErrorMessage.class);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }

        @Test
        void shouldDeserializeFromEmptyJson(SoftAssertions softly) {
            var errorMessage = jsonHelper.toObject("{}", ErrorMessage.class);

            softly.assertThat(errorMessage.getCode()).isEqualTo(DEFAULT_ERROR_CODE);
            softly.assertThat(errorMessage.getFieldName()).isNull();
            softly.assertThat(errorMessage.getItemId()).isNull();
            softly.assertThat(errorMessage.getMessage()).isEqualTo(UNKNOWN_ERROR_MESSAGE);
        }
    }
}
