package org.kiwiproject.base;

import java.sql.Time;
import java.sql.Timestamp;
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

public interface KiwiEnvironment {

    Date currentDate();

    Time currentTime();

    Timestamp currentTimestamp();

    Instant currentInstant();

    Instant currentInstant(ZoneId zoneId);

    LocalDate currentLocalDate();

    LocalDate currentLocalDate(ZoneId zoneId);

    LocalTime currentLocalTime();

    LocalTime currentLocalTime(ZoneId zoneId);

    LocalDateTime currentLocalDateTime();

    LocalDateTime currentLocalDateTime(ZoneId zoneId);

    ZonedDateTime currentZonedDateTimeUTC();

    ZonedDateTime currentZonedDateTime(ZoneId zoneId);

    ZonedDateTime currentZonedDateTime();

    long currentTimeMillis();

    long nanoTime();

    int currentProcessId();

    Optional<Integer> tryGetCurrentProcessId();

    void sleep(long milliseconds) throws InterruptedException;

    void sleep(long timeout, TimeUnit unit) throws InterruptedException;

    void sleep(long millis, int nanos) throws InterruptedException;

    boolean sleepQuietly(long milliseconds);

    boolean sleepQuietly(long timeout, TimeUnit unit);

    boolean sleepQuietly(long millis, int nanos);

    String getenv(String name);

    Map<String, String> getenv();

    Properties getProperties();

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

}
