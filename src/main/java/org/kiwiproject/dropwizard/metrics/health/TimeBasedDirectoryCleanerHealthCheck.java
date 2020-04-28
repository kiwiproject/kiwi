package org.kiwiproject.dropwizard.metrics.health;

import static org.kiwiproject.io.TimeBasedDirectoryCleaner.DeleteError;

import com.codahale.metrics.health.HealthCheck;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.kiwiproject.io.TimeBasedDirectoryCleaner;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A health check for {@link TimeBasedDirectoryCleaner} that checks if there have been any delete errors within
 * a specific duration of time. Errors older than the warning threshold will not cause this to report as unhealthy;
 * it will report unhealthy only if there are errors within the warning threshold.
 */
public class TimeBasedDirectoryCleanerHealthCheck extends HealthCheck {

    public static final Duration DEFAULT_DELETE_ERROR_THRESHOLD = Duration.ofHours(1);

    private static final boolean SUPPRESS_LEADING_ZERO_ELEMENTS = true;
    private static final boolean SUPPRESS_TRAILING_ZERO_ELEMENTS = true;

    private final TimeBasedDirectoryCleaner cleaner;
    private final long warningThresholdInMillis;
    private final String warningThresholdDescription;
    private final String intializedAt;

    /**
     * Create with specified {@link TimeBasedDirectoryCleaner} and the default warning threshold.
     *
     * @see #DEFAULT_DELETE_ERROR_THRESHOLD
     */
    public TimeBasedDirectoryCleanerHealthCheck(TimeBasedDirectoryCleaner cleaner) {
        this(cleaner, DEFAULT_DELETE_ERROR_THRESHOLD);
    }

    /**
     * Create with the specified {@link TimeBasedDirectoryCleaner} and warning threshold.
     */
    public TimeBasedDirectoryCleanerHealthCheck(TimeBasedDirectoryCleaner cleaner, Duration warningThreshold) {
        this.cleaner = cleaner;
        this.warningThresholdInMillis = warningThreshold.toMillis();
        this.warningThresholdDescription = DurationFormatUtils.formatDurationWords(warningThresholdInMillis,
                SUPPRESS_LEADING_ZERO_ELEMENTS,
                SUPPRESS_TRAILING_ZERO_ELEMENTS);

        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");
        this.intializedAt = ZonedDateTime.now().format(formatter);
    }

    @Override
    protected Result check() {
        var now = System.currentTimeMillis();
        var errorsWithinThreshold = cleaner.getRecentDeleteErrors().stream()
                .filter(error -> errorHasOccurredWithinWarningThreshold(now, error))
                .count();

        if (errorsWithinThreshold == 0) {
            return Result.healthy("No delete errors in last %s (%d total errors since %s)",
                    warningThresholdDescription,
                    cleaner.getDeleteErrorCount(),
                    intializedAt);
        }

        return Result.unhealthy("%d delete error(s) in last %s (%d total errors since %s)",
                errorsWithinThreshold,
                warningThresholdDescription,
                cleaner.getDeleteErrorCount(),
                intializedAt);
    }

    private boolean errorHasOccurredWithinWarningThreshold(long now, DeleteError error) {
        var ageInMillis = now - error.getTimestamp();
        return ageInMillis <  warningThresholdInMillis;
    }
}
