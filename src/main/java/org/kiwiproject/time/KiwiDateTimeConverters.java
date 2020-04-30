package org.kiwiproject.time;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A collection of small utility methods to convert between legacy {@link Date} and the Java 8 date/time API classes.
 * <p>
 * Mainly, these utilities save you some typing and having to remember the chains of method calls you need to make
 * when doing these conversions.
 * <p>
 * All methods throw {@link IllegalArgumentException} if null values are passed into them.
 *
 * @implNote {@link Date}, according to its JavaDoc, is "intended to reflect coordinated universal time (UTC)"
 * though "it may not do so exactly". The utilities in this class convert arguments to to UTC before converting
 * to {@link Date} in order to reflect that intention.
 */
@UtilityClass
public class KiwiDateTimeConverters {

    /**
     * Convert the given {@link LocalDate} into its equivalent {@link Date} (epoch milliseconds using UTC). The time
     * will be set to the start of the day.
     *
     * @param localDate the {@link LocalDate} to convert
     * @return the converted {@link Date}
     */
    public static Date toDate(LocalDate localDate) {
        checkArgumentNotNull(localDate);

        return toDate(localDate.atStartOfDay());
    }

    /**
     * Convert the given {@link LocalDateTime} into its equivalent {@link Date} (epoch milliseconds using UTC).
     *
     * @param localDateTime the {@link LocalDateTime} to convert
     * @return the converted {@link Date}
     */
    public static Date toDate(LocalDateTime localDateTime) {
        checkArgumentNotNull(localDateTime);

        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * Convert the given {@link ZonedDateTime} into its equivalent {@link Date} (epoch milliseconds using UTC).
     *
     * @param zonedDateTime the {@link ZonedDateTime} to convert
     * @return the converted {@link Date}
     */
    public static Date toDate(ZonedDateTime zonedDateTime) {
        checkArgumentNotNull(zonedDateTime);

        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * Convert the given {@link Date} into its equivalent {@link LocalDate} in the UTC time zone.
     *
     * @param date the {@link Date} to convert
     * @return the converted {@link LocalDate}
     */
    public static LocalDate toLocalDateUTC(Date date) {
        return toLocalDate(date, ZoneOffset.UTC);
    }

    /**
     * Convert the given {@link Date} into its equivalent {@link LocalDate} in the specified time zone.
     *
     * @param date   the {@link Date} to convert
     * @param zoneId the time zone as a {@link ZoneId}
     * @return the converted {@link LocalDate}
     */
    public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
        checkArgumentNotNull(date);
        checkArgumentNotNull(zoneId);

        return date.toInstant().atZone(zoneId).toLocalDate();
    }

    /**
     * Convert the given {@link Date} into its equivalent {@link LocalDateTime} in the UTC time zone.
     *
     * @param date the {@link Date} to convert
     * @return the converted {@link LocalDateTime}
     * @see ZoneOffset#UTC
     */
    public static LocalDateTime toLocalDateTimeUTC(Date date) {
        return toLocalDateTime(date, ZoneOffset.UTC);
    }

    /**
     * Convert the given {@link Date} into its equivalent {@link LocalDateTime} in the specified time zone.
     *
     * @param date   the {@link Date} to convert
     * @param zoneId the time zone as a {@link ZoneId}
     * @return the converted {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        checkArgumentNotNull(date);
        checkArgumentNotNull(zoneId);

        return date.toInstant().atZone(zoneId).toLocalDateTime();
    }

    /**
     * Convert the given {@link Date} into its equivalent {@link ZonedDateTime} in the UTC time zone.
     *
     * @param date the {@link Date} to convert
     * @return the converted {@link ZonedDateTime}
     * @see ZoneOffset#UTC
     */
    public static ZonedDateTime toZonedDateTimeUTC(Date date) {
        return toZonedDateTime(date, ZoneOffset.UTC);
    }

    /**
     * Convert the given {@link Date} into its equivalent {@link ZonedDateTime} in the specified time zone.
     *
     * @param date   the {@link Date} to convert
     * @param zoneId the time zone as a {@link ZoneId}
     * @return the converted {@link ZonedDateTime}
     */
    public static ZonedDateTime toZonedDateTime(Date date, ZoneId zoneId) {
        checkArgumentNotNull(date);
        checkArgumentNotNull(zoneId);

        return date.toInstant().atZone(zoneId);
    }
}
