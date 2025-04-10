package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.kiwiproject.base.KiwiStrings.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.kiwiproject.collect.KiwiMaps;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@DisplayName("DefaultKiwiEnvironment")
class DefaultEnvironmentTest {

    private static final long DEFAULT_DELTA_MILLIS = 50;

    private DefaultEnvironment env;

    @BeforeEach
    void setUp() {
        env = new DefaultEnvironment();
    }

    @Test
    void testCurrentDate() {
        long now = new Date().getTime();
        long envNow = env.currentDate().getTime();
        assertNow(envNow, now);
    }

    @Test
    void testCurrentTime() {
        long millis = System.currentTimeMillis();
        long now = new Time(millis).getTime();
        long envTime = env.currentTime().getTime();
        assertNow(envTime, now);
    }

    @Test
    void testCurrentTimestamp() {
        long millis = System.currentTimeMillis();
        long now = new Timestamp(millis).getTime();
        long envTime = env.currentTimestamp().getTime();
        assertNow(envTime, now);
    }

    @Test
    void testCurrentInstant_InDefaultTimeZone() {
        long now = Instant.now().toEpochMilli();
        long envTime = env.currentInstant().toEpochMilli();
        assertNow(envTime, now);
    }

    @Test
    void testCurrentInstant_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = Instant.now(Clock.system(zoneId)).toEpochMilli();
            long envTime = env.currentInstant(zoneId).toEpochMilli();
            assertNow(envTime, now);
        });
    }

    @Test
    void testCurrentLocalDate_InDefaultTimeZone() {
        ZoneId defaultZone = ZoneId.systemDefault();
        long now = LocalDate.now().atStartOfDay().atZone(defaultZone).toInstant().toEpochMilli();
        long envNow = env.currentLocalDate().atStartOfDay().atZone(defaultZone).toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    void testCurrentLocalDate_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = LocalDate.now(zoneId).atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
            long envTime = env.currentLocalDate(zoneId).atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
            assertNow(envTime, now);
        });
    }

    @Test
    void testCurrentLocalTime_InDefaultTimeZone() {
        ZoneId defaultZone = ZoneId.systemDefault();
        LocalDate currentLocalDate = LocalDate.now();
        long now = LocalTime.now().atDate(currentLocalDate).atZone(defaultZone).toInstant().toEpochMilli();
        long envNow = env.currentLocalTime().atDate(currentLocalDate).atZone(defaultZone).toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    void testCurrentLocalTime_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            LocalDate currentLocalDate = LocalDate.now();
            long now = LocalTime.now(zoneId).atDate(currentLocalDate).atZone(zoneId).toInstant().toEpochMilli();
            long envTime = env.currentLocalTime(zoneId).atDate(currentLocalDate).atZone(zoneId).toInstant().toEpochMilli();
            assertNow(envTime, now);
        });
    }

    @Test
    void testCurrentLocalDateTime_InDefaultTimeZone() {
        ZoneId defaultZone = ZoneId.systemDefault();
        long now = LocalDateTime.now().atZone(defaultZone).toInstant().toEpochMilli();
        long envTime = env.currentLocalDateTime().atZone(defaultZone).toInstant().toEpochMilli();
        assertNow(envTime, now);
    }

    @Test
    void testCurrentLocalDateTime_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = LocalDateTime.now(zoneId).atZone(zoneId).toInstant().toEpochMilli();
            long envTime = env.currentLocalDateTime(zoneId).atZone(zoneId).toInstant().toEpochMilli();
            assertNow(envTime, now);
        });
    }

    @Test
    void testCurrentZonedDateTimeUTC() {
        long now = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
        long envTime = env.currentZonedDateTimeUTC().toInstant().toEpochMilli();
        assertNow(envTime, now);
    }

    @Test
    void testCurrentZonedDateTime_InDefaultTimeZone() {
        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        long envNow = env.currentZonedDateTime().toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    void testCurrentZonedDateTime_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = ZonedDateTime.now(zoneId).toInstant().toEpochMilli();
            long envNow = env.currentZonedDateTime(zoneId).toInstant().toEpochMilli();
            assertNow(envNow, now);
        });
    }

    @Test
    void testCurrentTimeMillis() {
        long now = System.currentTimeMillis();
        long envNow = env.currentTimeMillis();
        assertNow(envNow, now);
    }

    @Test
    void testNanoTime() {
        long now = System.nanoTime();
        long envNano = env.nanoTime();
        long deltaNanos = TimeUnit.MICROSECONDS.toNanos(100);
        assertNow(envNano, now, deltaNanos);
    }

    private void assertNow(long envNow, long now) {
        assertNow(envNow, now, DEFAULT_DELTA_MILLIS);
    }

    private void assertNow(long envNow, long now, long delta) {
        long diff = envNow - now;

        assertThat(diff)
                .as("Difference should be less than %d but was %d", delta, diff)
                .isNotNegative()
                .isLessThan(delta);
    }

    @Nested
    class CurrentPid {

        @Test
        void shouldGetCurrentPid() {
            assertThatCode(() -> env.currentPid()).doesNotThrowAnyException();
        }

        @Test
        void shouldAllowUnsupportedExceptionsToEscape() {
            var envSpy = spy(env);
            var errorMessage = "this fake JVM doesn't support getting the current pid for some reason";
            doThrow(new UnsupportedOperationException(errorMessage))
                    .when(envSpy)
                    .currentPid();

            assertThatThrownBy(envSpy::currentPid)
                    .isExactlyInstanceOf(UnsupportedOperationException.class)
                    .hasMessage(errorMessage);
        }
    }

    @Nested
    class TryGetCurrentPid {

        @Test
        void shouldReturnOptionalContainingPid() {
            var optionalPid = env.tryGetCurrentPid();
            assertThat(optionalPid).isPresent();
        }

        @Test
        void shouldReturnEmptyOptional_WhenCurrentPidCannotBeObtained() {
            var envSpy = spy(env);
            doThrow(new UnsupportedOperationException("this fake JVM doesn't support getting the current pid for some reason"))
                    .when(envSpy)
                    .currentPid();

            var optionalPid = envSpy.tryGetCurrentPid();
            assertThat(optionalPid).isEmpty();
        }
    }

    @Nested
    class CurrentProcessHandle {

        @Test
        void shouldReturnProcessHandle() {
            var currentProcessHandle = env.currentProcessHandle();
            assertThat(currentProcessHandle).isNotNull();
        }

        @Test
        void shouldAllowUnsupportedExceptionsToEscape() {
            var envSpy = spy(env);
            var errorMessage = "this fake JVM doesn't support getting the current process handle";
            doThrow(new UnsupportedOperationException(errorMessage))
                    .when(envSpy)
                    .currentProcessHandle();

            assertThatThrownBy(envSpy::currentProcessHandle)
                    .isExactlyInstanceOf(UnsupportedOperationException.class)
                    .hasMessage(errorMessage);
        }
    }

    @Nested
    class ProcessHandleOfPid {

        @Test
        void shouldReturnProcessHandle_ForAnExistingProcess() {
            var pid = ProcessHandle.current().pid();
            var processHandle = env.processHandleOfPid(pid).orElseThrow();
            assertThat(processHandle).isEqualTo(ProcessHandle.current());
        }

        @RepeatedTest(5)
        void shouldReturnEmptyOptional_WhenProcessDoesNotExist() {
            // use a range for pids that should never exist
            var randomPid = ThreadLocalRandom.current().nextLong(100_000, 200_001);
            assumeTrue(ProcessHandle.of(randomPid).isEmpty(),
                    () -> format("Expected process having pid {} not to exist, but it does", randomPid));

            assertThat(env.processHandleOfPid(randomPid)).isEmpty();
        }
    }

    @Test
    void shouldReturnCurrentThread() {
        assertThat(env.currentThread()).isSameAs(Thread.currentThread());
    }

    @Test
    void testSleep() throws InterruptedException {
        long sleepTime = 50;
        long start = System.currentTimeMillis();
        env.sleep(sleepTime);
        long end = System.currentTimeMillis();
        assertElapsedTimeInMillisMeetsMinimum(sleepTime, start, end);
    }

    @Test
    void testSleep_UsingTimeUnit() throws InterruptedException {
        long sleepTime = 50;
        long start = System.currentTimeMillis();
        env.sleep(sleepTime, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        assertElapsedTimeInMillisMeetsMinimum(sleepTime, start, end);
    }

    @Test
    void testSleep_WithNanos() throws InterruptedException {
        long sleepMillis = 50;
        int sleepNanos = 999_999;
        long start = System.currentTimeMillis();
        env.sleep(sleepMillis, sleepNanos);
        long end = System.currentTimeMillis();
        assertElapsedTimeInMillisMeetsMinimum(sleepMillis, start, end);
    }

    @Test
    void shouldSleep_WithDuration() throws InterruptedException {
        var sleepMillis = 50;
        var start = System.currentTimeMillis();
        env.sleep(Duration.ofMillis(sleepMillis));
        var end = System.currentTimeMillis();
        assertElapsedTimeInMillisMeetsMinimum(sleepMillis, start, end);
    }

    @Test
    void testSleepQuietly() {
        long sleepTime = 50;
        long start = System.currentTimeMillis();
        boolean interrupted = env.sleepQuietly(sleepTime);
        long end = System.currentTimeMillis();
        assertThat(interrupted).isFalse();
        assertElapsedTimeInMillisMeetsMinimum(sleepTime, start, end);
    }

    @Test
    void testSleepQuietly_WhenThrowsInterruptedException() throws InterruptedException {
        var envSpy = spy(env);
        doThrow(new InterruptedException())
                .when(envSpy)
                .sleep(anyLong());

        var interrupted = envSpy.sleepQuietly(2500L);
        assertThat(interrupted).isTrue();

        verify(envSpy).sleep(2500L);
    }

    @Test
    void testSleepQuietly_UsingTimeUnit() {
        long sleepTime = 50;
        long start = System.currentTimeMillis();
        boolean interrupted = env.sleepQuietly(sleepTime, TimeUnit.MILLISECONDS);
        long end = System.currentTimeMillis();
        assertThat(interrupted).isFalse();
        assertElapsedTimeInMillisMeetsMinimum(sleepTime, start, end);
    }

    @Test
    void testSleepQuietly_UsingTimeUnit_WhenThrowsInterruptedException() throws InterruptedException {
        var envSpy = spy(env);
        doThrow(new InterruptedException())
                .when(envSpy)
                .sleep(anyLong(), any(TimeUnit.class));

        var timeout = 5;
        var timeUnit = TimeUnit.SECONDS;
        var interrupted = envSpy.sleepQuietly(timeout, timeUnit);

        assertThat(interrupted).isTrue();

        verify(envSpy).sleepQuietly(timeout, timeUnit);
    }

    @Test
    void testSleepQuietly_WithNanos() {
        long sleepMillis = 50;
        int sleepNanos = 999_999;
        long start = System.currentTimeMillis();
        boolean interrupted = env.sleepQuietly(sleepMillis, sleepNanos);
        long end = System.currentTimeMillis();
        assertThat(interrupted).isFalse();
        assertElapsedTimeInMillisMeetsMinimum(sleepMillis, start, end);
    }

    @Test
    void testSleepQuietly_WithNanos_WhenThrowsInterruptedException() throws InterruptedException {
        var envSpy = spy(env);
        doThrow(new InterruptedException())
                .when(envSpy)
                .sleep(anyLong(), anyInt());

        var millis = 50L;
        var nanos = 100_000;
        var interrupted = envSpy.sleepQuietly(millis, nanos);

        assertThat(interrupted).isTrue();

        verify(envSpy).sleepQuietly(millis, nanos);
    }

    @Test
    void shouldSleepQuietly_WithDuration() {
        var sleepMillis = 50;
        var start = System.currentTimeMillis();
        var interrupted = env.sleepQuietly(Duration.ofMillis(sleepMillis));
        var end = System.currentTimeMillis();
        assertThat(interrupted).isFalse();
        assertElapsedTimeInMillisMeetsMinimum(sleepMillis, start, end);
    }

    @Test
    void shouldSleepQuietly_WithDuration_WhenThrowsInterruptedException() throws InterruptedException {
        var envSpy = spy(env);
        doThrow(new InterruptedException())
                .when(envSpy)
                .sleep(any(Duration.class));

        var duration = Duration.ofMillis(50);
        var interrupted = envSpy.sleepQuietly(duration);

        assertThat(interrupted).isTrue();

        verify(envSpy).sleepQuietly(duration);
    }

    /**
     * @implNote Assumes times are in milliseconds
     */
    private void assertElapsedTimeInMillisMeetsMinimum(long sleepTimeMs, long startMs, long endMs) {
        long elapsedMs = endMs - startMs;
        assertThat(elapsedMs)
                .describedAs("elapsed time should be at least %d millis, but was actually only %d", sleepTimeMs, elapsedMs)
                .isGreaterThanOrEqualTo(sleepTimeMs);
    }

    @Test
    void testGetEnv_ForSingleNamedEnvironmentVariable() {
        Map<String, String> actualEnv = System.getenv();
        actualEnv.forEach((name, actualValue) -> {
            String value = env.getenv(name);
            assertThat(value)
                    .describedAs("Env var %s: expected %s, got %s", name, actualValue, value)
                    .isEqualTo(actualValue);
        });
    }

    @Test
    void testGetEnv_ToRetrieveAllEnvironmentVariablesAsMap() {
        assertThat(env.getenv()).isEqualTo(System.getenv());
    }

    @Test
    void testGetProperty() {
        Properties props = System.getProperties();
        props.forEach((key, actualValue) -> {
            String value = env.getProperty(String.valueOf(key));
            assertThat(value)
                    .describedAs("Property %s: expected %s, got %s", key, actualValue, value)
                    .isEqualTo(actualValue);
        });
    }

    @Test
    void testGetProperty_WithDefaultValue() {
        KiwiMaps.<String, String>newHashMap(
                "foo", "the default of foo",
                "bar", "the default of bar",
                "foo.bar.baz", "the default of foo.bar.baz"
        ).forEach((k, v) -> {
            String prop = env.getProperty(k, v);
            assertThat(prop).isEqualTo(System.getProperty(k, v));
        });

        String defaultValue = "the default of foo.bar.baz";
        String prop = env.getProperty("foo.bar.baz", defaultValue);
        assertThat(prop).isEqualTo(defaultValue);

        String osName = env.getProperty("os.name", "Foo OS");
        assertThat(osName).isNotEqualTo("Foo OS");
    }

    @Test
    void testGetProperties() {
        Properties props = env.getProperties();
        assertThat(props).isEqualTo(System.getProperties());
    }

}
