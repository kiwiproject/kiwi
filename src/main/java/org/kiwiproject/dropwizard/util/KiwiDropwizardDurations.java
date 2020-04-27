package org.kiwiproject.dropwizard.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * Utility that maps Dropwizard {@link io.dropwizard.util.Duration} to Java {@link java.time.Duration}
 */
@UtilityClass
public class KiwiDropwizardDurations {

    public static Duration fromDropwizardDuration(io.dropwizard.util.Duration duration) {
        var javaDuration = Duration.ofMillis(duration.toMilliseconds());

        if (javaDuration.isZero()) {
            return Duration.ofNanos(duration.toNanoseconds());
        }

        return javaDuration;
    }
}
