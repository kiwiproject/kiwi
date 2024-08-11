package org.kiwiproject.retry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.event.Level;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class that can be used to configure {@link SimpleRetryer} instances. This is
 * intended for usage with external configurations, e.g., a YAML configuration file, that will
 * be validated once instantiated. You can construct {@link SimpleRetryer} instances directly
 * from this instance using {@link #newRetryer}.
 * <p>
 * When constructing programmatically, prefer creating directly via {@code SimpleRetryer.builder()}.
 */
@Getter
@Setter
public class SimpleRetryerConfig {

    @Positive
    int maxAttempts = SimpleRetryer.DEFAULT_MAX_ATTEMPTS;

    @Positive
    long retryDelayTime = SimpleRetryer.DEFAULT_RETRY_DELAY_TIME;

    @NotNull
    TimeUnit retryDelayUnit = SimpleRetryer.DEFAULT_RETRY_DELAY_UNIT;

    @NotBlank
    String commonType = SimpleRetryer.DEFAULT_TYPE;

    @NotNull
    Level logLevelForSubsequentAttempts = SimpleRetryer.DEFAULT_RETRY_LOG_LEVEL;

    /**
     * Construct a new instance using the values in this configuration.
     *
     * @return a new retryer instance
     */
    public SimpleRetryer newRetryer() {
        return SimpleRetryer.builder()
                .maxAttempts(maxAttempts)
                .retryDelayTime(retryDelayTime)
                .retryDelayUnit(retryDelayUnit)
                .commonType(commonType)
                .logLevelForSubsequentAttempts(logLevelForSubsequentAttempts)
                .build();
    }
}
