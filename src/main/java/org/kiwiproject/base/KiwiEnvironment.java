package org.kiwiproject.base;

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
import java.util.OptionalLong;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Interface that defines methods related to the external environment, for example, getting the current time in
 * milliseconds, getting the process ID, and sleeping quietly for a specified time.
 * <p>
 * The main advantage of this over simply using things like {@link System#currentTimeMillis()} is to allow for easier
 * testing. Since this is an interface, it can easily be replaced with a mock object in unit tests that deal with
 * time-based or external environment-based code.
 */
public interface KiwiEnvironment {

    /**
     * Returns the current date.
     *
     * @return the current date as a {@link Date} object
     * @see Date#Date()
     */
    Date currentDate();

    /**
     * Returns the current time. For use with the JDBC API.
     *
     * @return the current time as a {@link Time} object
     * @see Time
     */
    Time currentTime();

    /**
     * Returns the current timestamp. For use with the JDBC API.
     *
     * @return the current timestamp as a {@link Timestamp} object
     * @see Timestamp
     */
    Timestamp currentTimestamp();

    /**
     * Returns the current {@link Instant} in the default time zone.
     *
     * @return the current instant
     * @see Instant#now()
     */
    Instant currentInstant();

    /**
     * Returns the current {@link Instant} in the specified time zone.
     *
     * @param zone the zone ID to use, not null
     * @return the current instant
     * @see Instant#now(Clock)
     */
    Instant currentInstant(ZoneId zone);

    /**
     * Returns the current {@link LocalDate} in the default time zone.
     *
     * @return the current local date
     * @see LocalDate
     */
    LocalDate currentLocalDate();

    /**
     * Returns the current {@link LocalDate} in the specified time zone.
     *
     * @param zone the zone ID to use, not null
     * @return the current local date
     * @see LocalDate#now(ZoneId)
     */
    LocalDate currentLocalDate(ZoneId zone);

    /**
     * Returns the current {@link LocalTime} in the default time zone.
     *
     * @return the current local time
     * @see LocalTime#now()
     */
    LocalTime currentLocalTime();

    /**
     * Returns the current {@link LocalTime} in the specified time zone.
     *
     * @param zone the zone ID to use, not null
     * @return the current local time
     * @see LocalTime#now(ZoneId)
     */
    LocalTime currentLocalTime(ZoneId zone);

    /**
     * Returns the current {@link LocalDateTime} in the default time zone.
     *
     * @return the current local date/time
     * @see LocalDateTime#now()
     */
    LocalDateTime currentLocalDateTime();

    /**
     * Returns the current {@link LocalDateTime} in the specified time zone.
     *
     * @param zone the zone ID to use, not null
     * @return the current local date/time
     * @see LocalDateTime#now(ZoneId)
     */
    LocalDateTime currentLocalDateTime(ZoneId zone);

    /**
     * Returns the current {@link ZonedDateTime} with the {@link java.time.ZoneOffset} as {@link java.time.ZoneOffset#UTC}.
     *
     * @return the current date/time in UTC
     * @see ZonedDateTime#now(ZoneId)
     */
    ZonedDateTime currentZonedDateTimeUTC();

    /**
     * Returns the current {@link ZonedDateTime} in the specified time zone.
     *
     * @param zone the zone ID to use, not null
     * @return a {@link ZonedDateTime} representing the current date/time in the specified zone
     * @see ZonedDateTime#now(ZoneId)
     */
    ZonedDateTime currentZonedDateTime(ZoneId zone);

    /**
     * Returns the current {@link ZonedDateTime} in the default time zone.
     *
     * @return a {@link ZonedDateTime} representing the current date/time in the default time zone
     * @see ZonedDateTime#now()
     */
    ZonedDateTime currentZonedDateTime();

    /**
     * Returns the current time in milliseconds since the epoch.
     *
     * @return the current time in milliseconds
     * @see System#currentTimeMillis()
     */
    long currentTimeMillis();

    /**
     * Returns the current value of the running JVM's time source, in nanoseconds.
     * <p>
     * Only used to measure elapsed time, per {@link System#nanoTime()}.
     *
     * @return the current time in nanoseconds
     * @see System#nanoTime()
     */
    long nanoTime();

    /**
     * Returns the process ID of the currently executing JVM. This method does not perform any error checking. Use
     * {@link #tryGetCurrentPid()} for a version that returns an empty optional if it is unable to get the pid
     * for any reason.
     *
     * @return the pid of the current process
     * @see ProcessHandle#current()
     * @see ProcessHandle#pid()
     */
    long currentPid();

    /**
     * Tries to get the process ID of the currently executing JVM. If any problem occurs, it is caught and an
     * empty optional is returned.
     *
     * @return an optional containing the pid of the current process, or empty if <em>any</em> problem occurred
     * @see ProcessHandle#current()
     * @see ProcessHandle#pid()
     */
    OptionalLong tryGetCurrentPid();

    /**
     * Returns a ProcessHandle for the current process.
     *
     * @return a handle to the current process
     * @see ProcessHandle#current()
     */
    ProcessHandle currentProcessHandle();

    /**
     * Returns a reference to the currently executing thread object.
     *
     * @return the currently executing thread
     * @see Thread#currentThread()
     */
    Thread currentThread();

    /**
     * Tries to obtain a {@link ProcessHandle} for a process with the given ID. If the process does not exist, then
     * an empty Optional is returned.
     *
     * @param pid the process ID
     * @return an Optional containing a ProcessHandle for the given process ID, or an empty Optional if the process
     * does not exist
     * @see ProcessHandle#of(long)
     * @implNote Implementations may throw the same exceptions as {@link ProcessHandle#of(long)}, but are not required
     * to do so.
     */
    Optional<ProcessHandle> processHandleOfPid(long pid);

    /**
     * Sleep for the given number of milliseconds.
     *
     * @param milliseconds the number of milliseconds to sleep
     * @throws InterruptedException if interrupted
     * @see Thread#sleep(long)
     */
    void sleep(long milliseconds) throws InterruptedException;

    /**
     * Sleep for a specific amount of time given by the specified {@code timeout} and {@link TimeUnit}.
     *
     * @param timeout the value to sleep
     * @param unit    the unit to sleep, e.g. {@link TimeUnit#SECONDS}
     * @throws InterruptedException if interrupted
     * @see TimeUnit#sleep(long)
     */
    void sleep(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Sleep for the specified number of milliseconds plus nanoseconds.
     *
     * @param millis the number of milliseconds to sleep
     * @param nanos  {@code 0-999999} additional nanoseconds to sleep
     * @throws InterruptedException if interrupted
     * @see Thread#sleep(long, int)
     */
    void sleep(long millis, int nanos) throws InterruptedException;

    /**
     * Sleep for the given number of milliseconds. Will never throw an {@link InterruptedException}.
     *
     * @param milliseconds the number of milliseconds to sleep
     * @return false if the sleep was not interrupted, and true if it was interrupted
     * @see Thread#sleep(long)
     */
    boolean sleepQuietly(long milliseconds);

    /**
     * Sleep for a specific amount of time given by the specified {@code timeout} and {@link TimeUnit}.
     * Will never throw an {@link InterruptedException}.
     *
     * @param timeout the value to sleep
     * @param unit    the unit to sleep, e.g. {@link TimeUnit#SECONDS}
     * @return false if the sleep was not interrupted, and true if it was interrupted
     * @see TimeUnit#sleep(long)
     */
    boolean sleepQuietly(long timeout, TimeUnit unit);

    /**
     * Sleep for the given number of milliseconds plus nanoseconds. Will never throw an {@link InterruptedException}.
     *
     * @param millis the number of milliseconds to sleep
     * @param nanos  {@code 0-999999} additional nanoseconds to sleep
     * @return false if the sleep was not interrupted, and true if it was interrupted
     * @see Thread#sleep(long, int)
     */
    boolean sleepQuietly(long millis, int nanos);

    /**
     * Gets the value of the specified environment variable.
     *
     * @param name the name of the environment variable
     * @return the string value of the variable, or {@code null} if the variable is not defined in this environment
     * @see System#getenv(String)
     */
    String getenv(String name);

    /**
     * Returns an unmodifiable string map view of the environment.
     *
     * @return the environment as a map of variable names to values
     * @see System#getenv()
     */
    Map<String, String> getenv();

    /**
     * Returns the current system properties.
     *
     * @return the system properties
     * @see System#getProperties()
     */
    Properties getProperties();

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param key the name of the system property
     * @return the string value of the system property
     * @see System#getProperty(String)
     */
    String getProperty(String key);

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param key          the name of the system property
     * @param defaultValue a default value
     * @return the string value of the system property, or the given default value if there is no property with that key
     * @see System#getProperty(String, String)
     */
    String getProperty(String key, String defaultValue);

}
