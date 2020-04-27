package org.kiwiproject.dropwizard.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.io.TimeBasedDirectoryCleaner;
import org.kiwiproject.io.TimeBasedDirectoryCleaner.DeleteError;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("TimeBasedDirectoryCleanerHealthCheck")
class TimeBasedDirectoryCleanerHealthCheckTest {

    private TimeBasedDirectoryCleanerHealthCheck healthCheck;
    private TimeBasedDirectoryCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner = mock(TimeBasedDirectoryCleaner.class);
    }

    @Test
    void testCheck_WhenNoDeleteErrors_AreWithinWarningThreshold() {
        healthCheck = new TimeBasedDirectoryCleanerHealthCheck(cleaner, Duration.ofMinutes(1));

        var twoMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);

        var errors = List.of(
                DeleteError.of("folder1").withTimestamp(twoMinutesAgo),
                DeleteError.of("folder2").withTimestamp(twoMinutesAgo),
                DeleteError.of("folder3").withTimestamp(twoMinutesAgo),
                DeleteError.of("folder4").withTimestamp(twoMinutesAgo)
        );

        when(cleaner.getRecentDeleteErrors()).thenReturn(errors);
        when(cleaner.getDeleteErrorCount()).thenReturn(25);

        var result = healthCheck.check();

        assertThat(result.isHealthy()).isTrue();
        assertThat(result.getMessage()).startsWith("No delete errors in last 1 minute (25 total errors since");
    }

    @Test
    void testCheck_WhenSomeDeleteErrors_AreWithinWarningThreshold() {
        healthCheck = new TimeBasedDirectoryCleanerHealthCheck(cleaner);

        var moreThanOneHourAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(61);
        var thirtyMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30);

        var errors = List.of(
                DeleteError.of("folder1").withTimestamp(moreThanOneHourAgo),
                DeleteError.of("folder2").withTimestamp(moreThanOneHourAgo),
                DeleteError.of(new RuntimeException("oops")).withTimestamp(moreThanOneHourAgo),
                DeleteError.of("folder4").withTimestamp(thirtyMinutesAgo)
        );

        when(cleaner.getRecentDeleteErrors()).thenReturn(errors);
        when(cleaner.getDeleteErrorCount()).thenReturn(35);

        var result = healthCheck.check();

        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getMessage()).startsWith("1 delete error(s) in last 1 hour (35 total errors since");
    }
}
