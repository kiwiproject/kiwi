package org.kiwiproject.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
