package org.kiwiproject.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("PropertyMaskingOptions")
class PropertyMaskingOptionsTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void shouldNotAllowNullMaskedFieldRegexps() {
        assertThatNullPointerException()
                .isThrownBy(() -> PropertyMaskingOptions.builder()
                        .maskedFieldRegexps(null)
                        .build());
    }

    @Test
    void shouldHaveDefaultValues() {
        var defaultOptions = PropertyMaskingOptions.builder().build();

        assertThat(defaultOptions.getMaskedFieldRegexps()).isEmpty();
        assertThat(defaultOptions.getMaskedFieldReplacementText()).isEqualTo("********");
        assertThat(defaultOptions.getSerializationErrorReplacementText()).isEqualTo("(unable to serialize field)");
    }

    @Test
    void shouldNotMakeMaskedFieldRegexpsUnmodifiableByDefault() {
        var options = PropertyMaskingOptions.builder().build();

        options.getMaskedFieldRegexps().add(".*password.*");

        assertThat(options.getMaskedFieldRegexps()).containsExactly(".*password.*");
    }

    @Test
    void shouldAllowCustomValues() {
        var options = PropertyMaskingOptions.builder()
                .maskedFieldRegexps(List.of(".*password.*"))
                .maskedFieldReplacementText("-----")
                .serializationErrorReplacementText("(error serializing field)")
                .build();

        assertAll(
                () -> assertThat(options.getMaskedFieldRegexps()).containsExactly(".*password.*"),
                () -> assertThat(options.getMaskedFieldRegexps()).isUnmodifiable(),
                () -> assertThat(options.getMaskedFieldReplacementText()).isEqualTo("-----"),
                () -> assertThat(options.getSerializationErrorReplacementText()).isEqualTo("(error serializing field)")
        );
    }
}
