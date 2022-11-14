package org.kiwiproject.retry;

import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.index.qual.Positive;
import org.slf4j.event.Level;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class that can be used to configure {@link SimpleRetryer} instances. This is
 * intended for usage with external configurations, e.g. a YAML configuration file, that will
 * be validated once instantiated.
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
}
