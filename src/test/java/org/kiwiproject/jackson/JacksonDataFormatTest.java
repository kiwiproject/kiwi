package org.kiwiproject.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("JacksonDataFormat")
class JacksonDataFormatTest {

    @Nested
    class FromFactoryMethod {

        @ParameterizedTest
        @EnumSource(JacksonDataFormat.class)
        void shouldReturnExpectedEnumConstant(JacksonDataFormat dataFormat) {
            var formatName = dataFormat.getFormatName();

            assertThat(JacksonDataFormat.from(formatName)).isSameAs(dataFormat);
        }

        @ParameterizedTest
        @ValueSource(strings = {"HTML", "foo", "BAR", "CSV", "UNKNOWN", "RANDOM"})
        void shouldReturnUnknown(String value) {
            assertThat(JacksonDataFormat.from(value)).isSameAs(JacksonDataFormat.UNKNOWN);
        }
    }
}