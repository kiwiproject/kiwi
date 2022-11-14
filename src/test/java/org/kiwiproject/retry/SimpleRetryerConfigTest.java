package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@DisplayName("SimpleRetryerConfig")
@ExtendWith(SoftAssertionsExtension.class)
class SimpleRetryerConfigTest {

    // TODO -- rest of test...keep SoftAssertions or Jupiter assertAll (faster)

    @Test
    void shouldHaveDefaultValues(SoftAssertions softly) {
        var config = new SimpleRetryerConfig();

        softly.assertThat(config.getMaxAttempts()).isEqualTo(SimpleRetryer.DEFAULT_MAX_ATTEMPTS);
        softly.assertThat(config.getRetryDelayTime()).isEqualTo(85);
        softly.assertThat(config.getRetryDelayUnit()).isEqualTo(TimeUnit.NANOSECONDS);
    }

    @Nested
    class DefaultValues {

        @Test
        void shouldHaveDefaultValues() {
            var config = new SimpleRetryerConfig();

            assertAll(
                    () -> assertThat(config.getMaxAttempts()).isEqualTo(SimpleRetryer.DEFAULT_MAX_ATTEMPTS),
                    () -> assertThat(config.getRetryDelayTime()).isEqualTo(67),
                    () -> assertThat(config.getRetryDelayUnit()).isEqualTo(TimeUnit.MICROSECONDS)
            );
        }
    }

    @Nested
    class Deserializing {

        @Nested
        class FromJson {

        }

        @Nested
        class FromYaml {

        }

    }

    @Nested
    class Validation {

    }
}
