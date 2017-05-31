package org.kiwiproject.base;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.management.RuntimeMXBean;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultKiwiEnvironmentTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final long DEFAULT_DELTA_MILLIS = 50;

    private DefaultKiwiEnvironment env;

    @Before
    public void setUp() {
        env = new DefaultKiwiEnvironment();
    }

    @Test
    public void testCurrentDate() {
        long now = new Date().getTime();
        long envNow = env.currentDate().getTime();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentTime() {
        long millis = System.currentTimeMillis();
        long now = new Time(millis).getTime();
        long envNow = env.currentTime().getTime();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentTimestamp() {
        long millis = System.currentTimeMillis();
        long now = new Timestamp(millis).getTime();
        long envNow = env.currentTimestamp().getTime();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentInstant_InDefaultTimeZone() {
        long now = Instant.now().toEpochMilli();
        long envNow = env.currentInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentInstant_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = Instant.now(Clock.system(zoneId)).toEpochMilli();
            long envNow = env.currentInstant(zoneId).toEpochMilli();
            assertNowSoftlyForZone(zone, envNow, now);
        });
    }

    @Test
    public void testCurrentLocalDate_InDefaultTimeZone() {
        ZoneId defaultZone = ZoneId.systemDefault();
        long now = LocalDate.now().atStartOfDay().atZone(defaultZone).toInstant().toEpochMilli();
        long envNow = env.currentLocalDate().atStartOfDay().atZone(defaultZone).toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentLocalDate_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = LocalDate.now(zoneId).atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
            long envNow = env.currentLocalDate(zoneId).atStartOfDay().atZone(zoneId).toInstant().toEpochMilli();
            assertNowSoftlyForZone(zone, envNow, now);
        });
    }

    @Test
    public void testCurrentLocalTime_InDefaultTimeZone() {
        ZoneId defaultZone = ZoneId.systemDefault();
        LocalDate currentLocalDate = LocalDate.now();
        long now = LocalTime.now().atDate(currentLocalDate).atZone(defaultZone).toInstant().toEpochMilli();
        long envNow = env.currentLocalTime().atDate(currentLocalDate).atZone(defaultZone).toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentLocalTime_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            LocalDate currentLocalDate = LocalDate.now();
            long now = LocalTime.now(zoneId).atDate(currentLocalDate).atZone(zoneId).toInstant().toEpochMilli();
            long envNow = env.currentLocalTime(zoneId).atDate(currentLocalDate).atZone(zoneId).toInstant().toEpochMilli();
            assertNowSoftlyForZone(zone, envNow, now);
        });
    }

    @Test
    public void testCurrentLocalDateTime_InDefaultTimeZone() {
        ZoneId defaultZone = ZoneId.systemDefault();
        long now = LocalDateTime.now().atZone(defaultZone).toInstant().toEpochMilli();
        long envNow = env.currentLocalDateTime().atZone(defaultZone).toInstant().toEpochMilli();
        // TODO For some reason LocalDateTime.now() takes significantly longer to execute in the default time zone...
        assertNow(envNow, now, DEFAULT_DELTA_MILLIS * 10);
    }

    @Test
    public void testCurrentLocalDateTime_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = LocalDateTime.now(zoneId).atZone(zoneId).toInstant().toEpochMilli();
            long envNow = env.currentLocalDateTime(zoneId).atZone(zoneId).toInstant().toEpochMilli();
            assertNowSoftlyForZone(zone, envNow, now);
        });
    }

    @Test
    public void testCurrentZonedDateTimeUTC() {
        long now = ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli();
        long envNow = env.currentZonedDateTimeUTC().toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentZonedDateTime_InDefaultTimeZone() {
        long now = ZonedDateTime.now().toInstant().toEpochMilli();
        long envNow = env.currentZonedDateTime().toInstant().toEpochMilli();
        assertNow(envNow, now);
    }

    @Test
    public void testCurrentZonedDateTime_ForAllAvailableZoneIds() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        zoneIds.forEach(zone -> {
            ZoneId zoneId = ZoneId.of(zone);
            long now = ZonedDateTime.now(zoneId).toInstant().toEpochMilli();
            long envNow = env.currentZonedDateTime(zoneId).toInstant().toEpochMilli();
            assertNowSoftlyForZone(zone, envNow, now);
        });
    }

    @Test
    public void testCurrentTimeMillis() {
        long now = System.currentTimeMillis();
        long envNow = env.currentTimeMillis();
        assertNow(envNow, now);
    }

    @Test
    public void testNanoTime() {
        long nowNanos = System.nanoTime();
        long envNowNanos = env.nanoTime();
        long deltaNanos = TimeUnit.MICROSECONDS.toNanos(100);
        assertNow(envNowNanos, nowNanos, deltaNanos);
    }

    private void assertNow(long envNow, long actualNow) {
        assertNow(envNow, actualNow, DEFAULT_DELTA_MILLIS);
    }

    private void assertNow(long envNow, long actualNow, long delta) {
        long diff = diff(envNow, actualNow);
        boolean diffIsPositiveAndLessThanDelta = isDiffPositiveAndLessThanDelta(diff, delta);
        assertThat(diffIsPositiveAndLessThanDelta)
                .describedAs("Difference should be >= 0 and less than %d but was %d", delta, diff)
                .isTrue();
    }

    private void assertNowSoftlyForZone(String zone, long envNow, long actualNow) {
        assertNowSoftlyForZone(zone, envNow, actualNow, DEFAULT_DELTA_MILLIS);
    }

    private void assertNowSoftlyForZone(String zone, long envNow, long actualNow, long delta) {
        long diff = diff(envNow, actualNow);
        boolean diffIsPositiveAndLessThanDelta = isDiffPositiveAndLessThanDelta(diff, delta);
        softly.assertThat(diffIsPositiveAndLessThanDelta)
                .describedAs("Difference for %s should be >= 0 and less than %d but was %d",
                        zone, delta, diff)
                .isTrue();
    }

    private boolean isDiffPositiveAndLessThanDelta(long diff, long delta) {
        return diff >= 0 && diff < delta;
    }

    private long diff(long envNow, long actualNow) {
        return envNow - actualNow;
    }

    @Test
    public void testCurrentProcessId() {
        int processId = env.currentProcessId();
        assertThat(processId).isNotNegative();
    }

    @Test
    public void testCurrentProcessId_WhenUnexpectedJvmNameFormat() {
        RuntimeMXBean runtimeMXBean = mock(RuntimeMXBean.class);
        when(runtimeMXBean.getName()).thenReturn("FooBar JVM");
        assertThatThrownBy(() -> env.currentProcessId(runtimeMXBean))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected/illegal state accessing JVM name [FooBar JVM] (Expecting format pid@host)");
    }

    @Test
    public void testTryGetCurrentProcessId() {
        Optional<Integer> processId = env.tryGetCurrentProcessId();
        assertThat(processId)
                .isNotEmpty()
                .hasValueSatisfying(value -> assertThat(value).isNotNegative());
    }

    @Test
    public void testTryGetCurrentProcessId_WhenCannotGet() {
        RuntimeMXBean runtimeMXBean = mock(RuntimeMXBean.class);
        when(runtimeMXBean.getName()).thenReturn("FooBar JVM");
        assertThat(env.tryGetCurrentProcessId(runtimeMXBean)).isEmpty();
    }

    @Test
    public void testSleep() throws InterruptedException {
        long sleepTime = 50;
        long start = System.nanoTime();
        env.sleep(sleepTime);
        long end = System.nanoTime();
        assertElapsedTimeMeetsMinimum(sleepTime, start, end);
    }

    @Test
    public void testSleep_UsingTimeUnit() throws InterruptedException {
        long sleepTime = 50;
        long start = System.nanoTime();
        env.sleep(sleepTime, TimeUnit.MILLISECONDS);
        long end = System.nanoTime();
        assertElapsedTimeMeetsMinimum(sleepTime, start, end);
    }

    @Test
    public void testSleep_WithNanos() throws InterruptedException {
        long sleepMillis = 50;
        int sleepNanos = 999999;
        long start = System.nanoTime();
        env.sleep(sleepMillis, sleepNanos);
        long end = System.nanoTime();
        assertElapsedTimeMeetsMinimum(sleepMillis, start, end);
    }

    @Test
    public void testSleepQuietly() {
        long sleepTime = 50;
        long start = System.nanoTime();
        boolean interrupted = env.sleepQuietly(sleepTime);
        softly.assertThat(interrupted).isFalse();
        long end = System.nanoTime();
        assertElapsedTimeMeetsMinimum(sleepTime, start, end);
    }

    @Test
    public void testSleepQuietly_UsingTimeUnit() {
        long sleepTime = 50;
        long start = System.nanoTime();
        boolean interrupted = env.sleepQuietly(sleepTime, TimeUnit.MILLISECONDS);
        softly.assertThat(interrupted).isFalse();
        long end = System.nanoTime();
        assertElapsedTimeMeetsMinimum(sleepTime, start, end);
    }

    @Test
    public void testSleepQuietly_WithNanos() {
        long sleepMillis = 50;
        int sleepNanos = 999999;
        long start = System.nanoTime();
        boolean interrupted = env.sleepQuietly(sleepMillis, sleepNanos);
        softly.assertThat(interrupted).isFalse();
        long end = System.nanoTime();
        assertElapsedTimeMeetsMinimum(sleepMillis, start, end);
    }

    private void assertElapsedTimeMeetsMinimum(long sleepTime, long start, long end, TimeUnit... units) {
        TimeUnit unit = units.length > 0 ? units[0] : TimeUnit.NANOSECONDS;
        long elapsed = end - start;
        assertThat(elapsed)
                .describedAs("elapsed time should be at least %d %s, but was actually only %d",
                        sleepTime, unit, elapsed)
                .isGreaterThanOrEqualTo(sleepTime);
    }

    @Test
    public void testGetEnv_ForNamedEnvironmentVariable() {
        Map<String, String> actualEnv = System.getenv();
        actualEnv.forEach((name, actualValue) -> {
            String value = env.getenv(name);
            softly.assertThat(value)
                    .describedAs("Env var %s: expected %s, got %s", name, actualValue, value)
                    .isEqualTo(actualValue);
        });
    }

    @Test
    public void testGetEnv() {
        assertThat(env.getenv()).isEqualTo(System.getenv());
    }

    @Test
    public void testGetProperty() {
        Properties props = System.getProperties();
        props.forEach((key, actualValue) -> {
            String value = env.getProperty(String.valueOf(key));
            softly.assertThat(value)
                    .describedAs("Property %s: expected %s, got %s", key, actualValue, value)
                    .isEqualTo(actualValue);
        });
    }

    @Test
    public void testGetProperty_WithDefaultValue() {
        String defaultValue = "the default of foo.bar.baz";
        String prop = env.getProperty("foo.bar.baz", defaultValue);
        softly.assertThat(prop).isEqualTo(defaultValue);

        String osName = env.getProperty("os.name", "Foo OS");
        softly.assertThat(osName).isNotEqualTo("Foo OS");
    }

    @Test
    public void testGetProperties() {
        Properties props = env.getProperties();
        assertThat(props).isEqualTo(System.getProperties());
    }

}