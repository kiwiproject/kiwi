package org.kiwiproject.base;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
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
import java.util.concurrent.TimeUnit;

import static org.kiwiproject.base.KiwiStrings.f;

public class DefaultKiwiEnvironment implements KiwiEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultKiwiEnvironment.class);

    @Override
    public Date currentDate() {
        return new Date();
    }

    @Override
    public Time currentTime() {
        return new Time(System.currentTimeMillis());
    }

    @Override
    public Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    @Override
    public Instant currentInstant() {
        return Instant.now();
    }

    @Override
    public Instant currentInstant(ZoneId zoneId) {
        return Instant.now(Clock.system(zoneId));
    }

    @Override
    public LocalDate currentLocalDate() {
        return LocalDate.now();
    }

    @Override
    public LocalDate currentLocalDate(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    @Override
    public LocalTime currentLocalTime() {
        return LocalTime.now();
    }

    @Override
    public LocalTime currentLocalTime(ZoneId zoneId) {
        return LocalTime.now(zoneId);
    }

    @Override
    public LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime currentLocalDateTime(ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }

    @Override
    public ZonedDateTime currentZonedDateTimeUTC() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @Override
    public ZonedDateTime currentZonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }

    @Override
    public ZonedDateTime currentZonedDateTime() {
        return ZonedDateTime.now();
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public int currentProcessId() {
        return currentProcessId(getRuntimeMXBean());
    }

    @VisibleForTesting
    int currentProcessId(RuntimeMXBean runtimeMXBean) {
        String jvmName = runtimeMXBean.getName();
        try {
            return Integer.parseInt(jvmName.split("@")[0]);
        } catch (Exception e) {
            String message = f("Unexpected/illegal state accessing JVM name [{}] (Expecting format pid@host)", jvmName);
            throw new IllegalStateException(message, e);
        }
    }

    @Override
    public Optional<Integer> tryGetCurrentProcessId() {
        return tryGetCurrentProcessId(getRuntimeMXBean());
    }

    private RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    @VisibleForTesting
    Optional<Integer> tryGetCurrentProcessId(RuntimeMXBean runtimeMXBean) {
        try {
            int value = currentProcessId(runtimeMXBean);
            return Optional.of(value);
        } catch (Exception e) {
            LOG.trace("Unable to get the current process ID", e);
            return Optional.empty();
        }
    }

    @Override
    public void sleep(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }

    @Override
    public void sleep(long timeout, TimeUnit unit) throws InterruptedException {
        unit.sleep(timeout);
    }

    @Override
    public void sleep(long millis, int nanos) throws InterruptedException {
        Thread.sleep(millis, nanos);
    }

    @Override
    public boolean sleepQuietly(long milliseconds) {
        try {
            sleep(milliseconds);
            return false;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted sleeping for {} milliseconds", milliseconds);
            Thread.currentThread().interrupt();
            return true;
        }
    }

    @Override
    public boolean sleepQuietly(long timeout, TimeUnit unit) {
        try {
            sleep(timeout, unit);
            return false;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted sleeping for {} {}", timeout, unit);
            Thread.currentThread().interrupt();
            return true;
        }
    }

    @Override
    public boolean sleepQuietly(long millis, int nanos) {
        try {
            sleep(millis, nanos);
            return false;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted sleeping for {} milliseconds with {} nanos", millis, nanos);
            Thread.currentThread().interrupt();
            return true;
        }
    }

    @Override
    public String getenv(String name) {
        return System.getenv(name);
    }

    @Override
    public Map<String, String> getenv() {
        return System.getenv();
    }

    @Override
    public Properties getProperties() {
        return System.getProperties();
    }

    @Override
    public String getProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
