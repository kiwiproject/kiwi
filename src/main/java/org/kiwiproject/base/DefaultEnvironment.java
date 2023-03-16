package org.kiwiproject.base;

import lombok.extern.slf4j.Slf4j;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A default implementation of the {@link KiwiEnvironment} interface. Normal usage is to define a private
 * {@link KiwiEnvironment} and initialize it to a new instance of this class, for example in a constructor:
 * <p>
 * Then wherever you would normally call things like {@link System#currentTimeMillis()}, use the corresponding method
 * from {@link KiwiEnvironment} instead, e.g. {@code env.currentTimeMillis()}.
 * <p>
 * For testing environment-related code, inject a mock instance via a constructor. A common pattern is to provide
 * a separate constructor that accepts a {@link KiwiEnvironment} specifically for test code to use; often this should
 * be made package-private (default scope). Other constructors will generally call this constructor. For example:
 * <pre>
 *  private KiwiEnvironment env;
 *
 *  public Foo() {
 *      this(new DefaultKiwiEnvironment());
 *  }
 *
 *  Foo(KiwiEnvironment env) {
 *      this.env = env;
 *  }
 *  </pre>
 */
@Slf4j
public class DefaultEnvironment implements KiwiEnvironment {

    @Override
    public Date currentDate() {
        return Date.from(Instant.now());
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
    public Instant currentInstant(ZoneId zone) {
        return Instant.now(Clock.system(zone));
    }

    @Override
    public LocalDate currentLocalDate() {
        return LocalDate.now();
    }

    @Override
    public LocalDate currentLocalDate(ZoneId zone) {
        return LocalDate.now(zone);
    }

    @Override
    public LocalTime currentLocalTime() {
        return LocalTime.now();
    }

    @Override
    public LocalTime currentLocalTime(ZoneId zone) {
        return LocalTime.now(zone);
    }

    @Override
    public LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime currentLocalDateTime(ZoneId zone) {
        return LocalDateTime.now(zone);
    }

    @Override
    public ZonedDateTime currentZonedDateTimeUTC() {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public ZonedDateTime currentZonedDateTime(ZoneId zone) {
        return ZonedDateTime.now(zone);
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
    public long currentPid() {
        return ProcessHandle.current().pid();
    }

    @Override
    public OptionalLong tryGetCurrentPid() {
        try {
            return OptionalLong.of(currentPid());
        } catch (Exception e) {
            LOG.trace("Unable to get current process ID", e);
            return OptionalLong.empty();
        }
    }

    @Override
    public ProcessHandle currentProcessHandle() {
        return ProcessHandle.current();
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
