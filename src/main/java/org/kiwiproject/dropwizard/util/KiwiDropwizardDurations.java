package org.kiwiproject.dropwizard.util;

import lombok.experimental.UtilityClass;

import org.kiwiproject.base.KiwiDeprecated;

import java.time.Duration;

/**
 * Utility that maps between Dropwizard {@link io.dropwizard.util.Duration} and Java {@link java.time.Duration}.
 */
@UtilityClass
public class KiwiDropwizardDurations {

    /**
     * Convert a {@link io.dropwizard.util.Duration} to a  Java {@link java.time.Duration}.
     *
     * @param duration the Dropwizard Duration
     * @return a Java Duration
     * @deprecated use {@link io.dropwizard.util.Duration#toJavaDuration()} instead
     */
    @Deprecated(since = "2.6.0")
    @KiwiDeprecated(replacedBy = "io.dropwizard.util.Duration#toJavaDuration",
                    reference = "https://github.com/kiwiproject/kiwi/issues/921")
    public static Duration fromDropwizardDuration(io.dropwizard.util.Duration duration) {
        var unit = duration.getUnit();
        var quantity = duration.getQuantity();
        return Duration.of(quantity, unit.toChronoUnit());
    }
}
