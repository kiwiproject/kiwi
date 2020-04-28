package org.kiwiproject.dropwizard.config;

import static com.google.common.base.Preconditions.checkState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.TEN_SECONDS;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.newValidator;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.dropwizard.metrics.health.TimeBasedDirectoryCleanerHealthCheck;
import org.kiwiproject.io.TimeBasedDirectoryCleaner;
import org.kiwiproject.io.TimeBasedDirectoryCleanerTestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@DisplayName("TimeBasedDirectoryCleanerConfig")
@Slf4j
class TimeBasedDirectoryCleanerConfigTest {

    @TempDir
    Path temporaryPath;

    private TimeBasedDirectoryCleanerTestHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TimeBasedDirectoryCleanerTestHelper(temporaryPath);
    }

    @Test
    void testValidations() {
        var validator = newValidator();
        var config = new TimeBasedDirectoryCleanerConfig();

        assertOnePropertyViolation(validator, config, "directoryPath");
        assertOnePropertyViolation(validator, config, "retentionThreshold");
        assertOnePropertyViolation(validator, config, "cleanupInterval");
        assertNoPropertyViolations(validator, config, "healthCheckWarningDuration");

        config.setHealthCheckWarningDuration(null);
        assertOnePropertyViolation(validator, config, "healthCheckWarningDuration");

        config.setDirectoryPath("/some/path");
        config.setRetentionThreshold(Duration.hours(36));
        config.setCleanupInterval(Duration.hours(4));
        config.setHealthCheckWarningDuration(Duration.minutes(30));

        assertNoViolations(validator, config);
    }

    @Test
    void testScheduleCleanup_WithScheduleExecutor_IntegrationTest() throws InterruptedException {
        var executorService = Executors.newScheduledThreadPool(1);

        try {
            var cleanerConfig = new TimeBasedDirectoryCleanerConfig();
            cleanerConfig.setDirectoryPath(temporaryPath.toString());
            cleanerConfig.setCleanupInterval(Duration.seconds(1));
            cleanerConfig.setRetentionThreshold(Duration.seconds(3));

            testHelper.createDirectoriesWithFiles(1, 20);

            var cleaner = cleanerConfig.scheduleCleanupUsing(executorService);

            testHelper.createDirectoriesWithFiles(21, 40);
            testHelper.createDirectoriesWithFiles(41, 60);

            waitUntilNoFilesInTempFolder();

            testHelper.createDirectoriesWithFiles(61, 80);
            testHelper.createDirectoriesWithFiles(81, 100);

            waitUntilNoFilesInTempFolder();

            assertSoftly(softly -> {
                softly.assertThat(cleaner.getDeleteCount()).isEqualTo(100);
                softly.assertThat(cleaner.getDeleteErrorCount()).isZero();
            });
        } finally {
            shutdownAndAwaitTermination(executorService);
        }
    }

    private void waitUntilNoFilesInTempFolder() {
        await().atMost(10, TimeUnit.SECONDS).until(() -> numFilesInTempFolder() == 0);
    }

    private int numFilesInTempFolder() throws IOException {
        return testHelper.filesInTempFolder().size();
    }

    private void shutdownAndAwaitTermination(ScheduledExecutorService executorService) throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * Test two concurrent cleaners, first with a low number (500) of files, and then with a high number (2000).
     * <p>
     * With the low number, neither cleaner reaches the maximum number of recent delete errors stored by
     * {@link org.kiwiproject.io.TimeBasedDirectoryCleaner}. With the higher number, each cleaner should exceed the maximum number of
     * recent delete errors. This lets us verify both of those cases.
     *
     * @see TimeBasedDirectoryCleaner#capacityOfRecentDeleteErrors()
     */
    @ParameterizedTest
    @ValueSource(ints = { 500, 2000 })
    @EnabledOnOs(OS.LINUX)
    void testScheduleCleanup_WithScheduledExecutor_UsingMultipleConcurrentCleaners_IntegrationTest(int totalFileCount) throws InterruptedException {
        assertThat(TimeBasedDirectoryCleaner.capacityOfRecentDeleteErrors())
                .describedAs("Assumption of cleaner error queue capacity of 500 is invalid; @ValueSource values need to be adjusted")
                .isEqualTo(500);

        var executorService1 = Executors.newScheduledThreadPool(1);
        var executorService2 = Executors.newScheduledThreadPool(1);

        try {
            var cleanerConfig = new TimeBasedDirectoryCleanerConfig();
            cleanerConfig.setDirectoryPath(temporaryPath.toString());
            cleanerConfig.setCleanupInterval(Duration.seconds(1));
            cleanerConfig.setRetentionThreshold(Duration.seconds(3));
            cleanerConfig.setDeleteErrorLogLevel(Level.INFO.toString());

            testHelper.createDirectoriesWithFiles(1, 100);

            var cleaner1 = cleanerConfig.scheduleCleanupUsing(executorService1);
            var cleaner2 = cleanerConfig.scheduleCleanupUsing(executorService2);

            testHelper.createDirectoriesWithFiles(101, 200);
            testHelper.createDirectoriesWithFiles(201, 300);

            waitUntilReachExpectedDeleteCount(cleaner1, cleaner2, 300);

            testHelper.createDirectoriesWithFiles(301, 400);
            testHelper.createDirectoriesWithFiles(401, totalFileCount);

            waitUntilReachExpectedDeleteCount(cleaner1, cleaner2, totalFileCount);

            logCleanerStats("cleaner1", cleaner1, totalFileCount);
            logCleanerStats("cleaner2", cleaner2, totalFileCount);
            logAggregateCleanerStats(totalFileCount, cleaner1, cleaner2);

            assertSoftly(softly -> {
                softly.assertThat(cleaner1.getDeleteCount() + cleaner2.getDeleteCount())
                        .describedAs("sum of delete count for both cleaners should equal total file count")
                        .isEqualTo(totalFileCount);

                softAssertConcurrentCleanerDeleteCounts(softly, cleaner1, totalFileCount);
                softAssertConcurrentCleanerDeleteCounts(softly, cleaner2, totalFileCount);
            });
        } finally {
            shutdownAndAwaitTermination(executorService1);
            shutdownAndAwaitTermination(executorService2);
        }
    }

    private void waitUntilReachExpectedDeleteCount(TimeBasedDirectoryCleaner cleaner1,
                                                   TimeBasedDirectoryCleaner cleaner2,
                                                   int expectedDeleteCount) {
        await().atMost(TEN_SECONDS).until(() -> cleaner1.getDeleteCount() + cleaner2.getDeleteCount() == expectedDeleteCount);
    }

    private void logCleanerStats(String name, TimeBasedDirectoryCleaner cleaner, int totalFileCount) {
        LOG.info("Stats for cleaner: {} (totalFileCount: {})", name, totalFileCount);
        LOG.info("----------------------------------------");
        LOG.info("\tdeletes  : {}", cleaner.getDeleteCount());
        LOG.info("\terrors   : {}", cleaner.getDeleteErrorCount());
        LOG.info("\n");
    }

    private void logAggregateCleanerStats(int totalFileCount, TimeBasedDirectoryCleaner... cleaners) {
        var totalDeletes = Arrays.stream(cleaners)
                .map(TimeBasedDirectoryCleaner::getDeleteCount)
                .reduce(Long::sum)
                .orElse(0L);

        var totalErrors = Arrays.stream(cleaners)
                .map(TimeBasedDirectoryCleaner::getDeleteErrorCount)
                .reduce(Integer::sum)
                .orElse(0);

        LOG.info("Aggregate stats for {} cleaners (totalFileCount: {})", cleaners.length, totalFileCount);
        LOG.info("----------------------------------------");
        LOG.info("\tdeletes  : {}", totalDeletes);
        LOG.info("\terrors   : {}", totalErrors);
        LOG.info("\n");
    }

    private static void softAssertConcurrentCleanerDeleteCounts(SoftAssertions softly, TimeBasedDirectoryCleaner cleaner, int totalFileCount) {
        softly.assertThat(cleaner.getDeleteCount())
                .describedAs("delete count should be less than total file count")
                .isLessThan(totalFileCount);

        var deleteErrorCount = cleaner.getDeleteErrorCount();
        softly.assertThat(deleteErrorCount)
                .describedAs("delete error count should be less than total file count")
                .isLessThan(totalFileCount);

        var maxRecentFileErrors = TimeBasedDirectoryCleaner.capacityOfRecentDeleteErrors();

        if (deleteErrorCount > maxRecentFileErrors) {
            softAssertDeleteErrorCountGreaterThanMaxQueueSize(softly, cleaner, deleteErrorCount, maxRecentFileErrors);
        } else {
            softAssertDeleteErrorCountLessThanOrEqualToMaxQueueSize(softly, cleaner, deleteErrorCount, maxRecentFileErrors);
        }
    }

    private static void softAssertDeleteErrorCountGreaterThanMaxQueueSize(SoftAssertions softly,
                                                                          TimeBasedDirectoryCleaner cleaner,
                                                                          int deleteErrorCount,
                                                                          int maxRecentFileErrors) {

        checkState(deleteErrorCount > maxRecentFileErrors);

        LOG.info("deleteErrorCount {} > maxRecentFileErrors {}", deleteErrorCount, maxRecentFileErrors);
        softly.assertThat(cleaner.getRecentDeleteErrors())
                .describedAs("recent error queue should be at max size %d when error count (%d) is higher than max size",
                        maxRecentFileErrors, deleteErrorCount)
                .hasSize(maxRecentFileErrors);
    }

    private static void softAssertDeleteErrorCountLessThanOrEqualToMaxQueueSize(SoftAssertions softly,
                                                                                TimeBasedDirectoryCleaner cleaner,
                                                                                int deleteErrorCount,
                                                                                int maxRecentFileErrors) {

        checkState(deleteErrorCount <= maxRecentFileErrors);

        LOG.info("deleteErrorCount {} <= maxRecentFileErrors {}", deleteErrorCount, maxRecentFileErrors);
        softly.assertThat(cleaner.getRecentDeleteErrors())
                .describedAs("recent error queue should have same size as error count %d when less than max queue size %d",
                        deleteErrorCount, maxRecentFileErrors)
                .hasSize(deleteErrorCount);

    }

    @Test
    void testScheduleCleanup_WithDropwizardEnvironment_IntegrationTest() throws InterruptedException {
        var executorService = Executors.newScheduledThreadPool(1);

        try {
            var cleanerConfig = new TimeBasedDirectoryCleanerConfig();
            cleanerConfig.setDirectoryPath(temporaryPath.toString());
            cleanerConfig.setCleanupInterval(Duration.seconds(1));
            cleanerConfig.setRetentionThreshold(Duration.seconds(3));

            var environment = mock(Environment.class);
            var lifecycleSpy = spyLifecycleEnvironment(environment);
            var healthChecks = mockHealthCheckRegistry(environment);

            testHelper.createDirectoriesWithFiles(1, 20);

            var cleaner = cleanerConfig.scheduleCleanupUsing(environment);

            testHelper.createDirectoriesWithFiles(21, 40);
            testHelper.createDirectoriesWithFiles(41, 60);

            waitUntilNoFilesInTempFolder();

            testHelper.createDirectoriesWithFiles(61, 80);

            waitUntilNoFilesInTempFolder();

            assertSoftly(softly -> {
                softly.assertThat(cleaner.getDeleteCount()).isEqualTo(80);
                softly.assertThat(cleaner.getDeleteErrorCount()).isZero();
            });

            verify(healthChecks).register(
                    eq("timeBasedDirectoryCleaner(" + temporaryPath + ")"),
                    isA(TimeBasedDirectoryCleanerHealthCheck.class));

            verify(lifecycleSpy).scheduledExecutorService(
                    eq("timeBasedDirectoryCleaner(" + temporaryPath + ")-%d"),
                    eq(true));
        } finally {
            shutdownAndAwaitTermination(executorService);
        }
    }

    private static LifecycleEnvironment spyLifecycleEnvironment(Environment mockEnv) {
        var lifecycleEnvironment = new LifecycleEnvironment(new MetricRegistry());
        var spy = spy(lifecycleEnvironment);
        when(mockEnv.lifecycle()).thenReturn(spy);
        return spy;
    }

    private static HealthCheckRegistry mockHealthCheckRegistry(Environment mockEnv) {
        var healthChecks = mock(HealthCheckRegistry.class);
        when(mockEnv.healthChecks()).thenReturn(healthChecks);
        return healthChecks;
    }
}
