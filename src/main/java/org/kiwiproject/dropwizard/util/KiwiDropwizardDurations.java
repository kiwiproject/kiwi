package org.kiwiproject.dropwizard.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;

/**
 * Utility that maps Dropwizard {@link io.dropwizard.util.Duration} to Java {@link java.time.Duration}
 */
@UtilityClass
public class KiwiDropwizardDurations {

    public static Duration fromDropwizardDuration(io.dropwizard.util.Duration duration) {
        var unit = duration.getUnit();
        var quantity = duration.getQuantity();
        return Duration.of(quantity, unit.toChronoUnit());
    }
}
