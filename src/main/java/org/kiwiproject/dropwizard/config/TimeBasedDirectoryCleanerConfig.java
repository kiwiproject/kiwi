package org.kiwiproject.dropwizard.config;

import static org.kiwiproject.base.KiwiStrings.format;

import com.google.common.base.CaseFormat;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.util.Duration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.dropwizard.metrics.health.TimeBasedDirectoryCleanerHealthCheck;
import org.kiwiproject.io.TimeBasedDirectoryCleaner;
import org.slf4j.event.Level;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A (Dropwizard) configuration class to configure and schedule a {@link org.kiwiproject.io.TimeBasedDirectoryCleaner}.
 */
@Getter
@Setter
@Slf4j
public class TimeBasedDirectoryCleanerConfig {

    public static final Duration DEFAULT_INITIAL_DELAY_MILLIS = Duration.seconds(1);
    public static final Duration SHUTDOWN_DURATION = Duration.seconds(10);

    @NotBlank
    private String directoryPath;

    @NotNull
    private Duration retentionThreshold;

    @NotNull
    private Duration initialCleanupDelay = DEFAULT_INITIAL_DELAY_MILLIS;

    @NotNull
    private Duration cleanupInterval;

    @NotNull
    private Duration healthCheckWarningDuration = Duration.hours(1);

    @NotBlank
    private String deleteErrorLogLevel = Level.WARN.toString();

    /**
     * Schedules directory cleanup using the given {@link ScheduledExecutorService}, returning the
     * {@link TimeBasedDirectoryCleaner} instance that was scheduled. It can then be used to retrieve delete
     * error information.
     *
     * @param cleanupExecutor the scheduled executor to use
     * @return a new TimeBasedDirectoryCleaner using the given executor
     * @implNote It is assumed that the {@link ScheduledExecutorService} is externally managed and has only one thread.
     */
    public TimeBasedDirectoryCleaner scheduleCleanupUsing(ScheduledExecutorService cleanupExecutor) {

        var intervalMillis = cleanupInterval.toMilliseconds();

        var cleaner = TimeBasedDirectoryCleaner.builder()
                .directoryPath(directoryPath)
                .retentionThreshold(retentionThreshold.toJavaDuration())
                .deleteErrorLogLevel(deleteErrorLogLevel)
                .build();

        cleanupExecutor.scheduleWithFixedDelay(cleaner, initialCleanupDelay.toMilliseconds(), intervalMillis, TimeUnit.MILLISECONDS);

        LOG.info("Scheduled cleanup of {} with retention threshold {}; initial delay {}; interval {}",
                directoryPath, retentionThreshold, initialCleanupDelay, cleanupInterval);

        return cleaner;
    }

    /**
     * Schedules directory cleanup and registers a {@link TimeBasedDirectoryCleanerHealthCheck} health check using the
     * given {@link Environment}, returning the {@link TimeBasedDirectoryCleaner} instance that was scheduled. It can
     * then be used to retrieve delete error information.
     *
     * @param environment the Dropwizard Environment to use
     * @return a new TimeBasedDirectoryCleaner using the given environment
     */
    public TimeBasedDirectoryCleaner scheduleCleanupUsing(Environment environment) {
        var cleanupExecutor = environment.lifecycle()
                .scheduledExecutorService(nameFormatFor(directoryPath), true)
                .threads(1)
                .shutdownTime(SHUTDOWN_DURATION)
                .build();

        var cleaner = scheduleCleanupUsing(cleanupExecutor);

        environment.healthChecks().register(
                format("{}({})", lowerCamelCaseCleanerClassName(), directoryPath),
                new TimeBasedDirectoryCleanerHealthCheck(cleaner, healthCheckWarningDuration.toJavaDuration())
        );

        LOG.info("Registered health check for {} directory cleaner with warning duration {}", directoryPath, healthCheckWarningDuration);

        return cleaner;
    }

    private static String nameFormatFor(String directoryPath) {
        return format("{}({})-%d", lowerCamelCaseCleanerClassName(), directoryPath);
    }

    private static String lowerCamelCaseCleanerClassName() {
        var simpleName = TimeBasedDirectoryCleaner.class.getSimpleName();
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(simpleName);
    }
}
