package org.kiwiproject.time;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.Date;

/**
 * A collection of small utilities to parse strings into various types of Java date/time classes, both the legacy
 * {@link java.util.Date} and the Java 8 date/time API classes in {@code java.time}.
 * <p>
 * Please make sure you actually read the method documentation so you know the format(s) that are expected or
 * allowed.
 * <p>
 * While the JDK has decided to keep the formatting and parsing methods together in {@link DateTimeFormatter},
 * we decided to split them into separate utilities. So if you are looking for utilities to format date/time objects
 * into strings, see {@link KiwiDateTimeFormatters}.
 * <p>
 * None of these are difficult to implement, but if you are constantly doing them, the time and code adds up
 * over time.
 * <p>
 * All methods throw {@link IllegalArgumentException} if {@code null} or blank arguments are passed to them.
 */
@UtilityClass
public class KiwiDateTimeParsers {

    /**
     * Converts the given date/time string into a {@link Date} using {@link DateTimeFormatter#ISO_ZONED_DATE_TIME}
     * as the formatter.
     *
     * @param dateTimeString the date/time string to parse
     * @return the parsed {@link Date}
     * @implNote If you need to parse a string into a custom format, you'll need to use
     * {@link java.text.SimpleDateFormat} (which is NOT thread safe, so make sure to use in a {@link ThreadLocal}
     * or simply instantiate a new one each time you need to parse)
     */
    public static Date parseAsDate(String dateTimeString) {
        checkArgumentNotBlank(dateTimeString);

        return KiwiDateTimeConverters.toDate(parseAsZonedDateTime(dateTimeString));
    }

    /**
     * Converts the given date string into a {@link LocalDate} using {@link DateTimeFormatter#ISO_LOCAL_DATE}
     * as the formatter.
     *
     * @param dateString the date string to parse
     * @return the parsed {@link LocalDate}
     */
    public static LocalDate parseAsLocalDate(String dateString) {
        return parseAsLocalDate(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Converts the given date string into a {@link LocalDate} using the given formatter.
     *
     * @param dateString the date string to parse
     * @param formatter  the formatter to use
     * @return the parsed {@link LocalDate}
     * @see DateTimeFormatter#parse(CharSequence, TemporalQuery)
     */
    public static LocalDate parseAsLocalDate(String dateString, DateTimeFormatter formatter) {
        checkArgumentNotBlank(dateString);
        checkArgumentNotNull(formatter);

        return formatter.parse(dateString, LocalDate::from);
    }

    /**
     * Converts the given date/time string into a {@link LocalDateTime} using
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} as the formatter.
     *
     * @param dateTimeString the date/time string to parse
     * @return the parsed {@link LocalDate}
     */
    public static LocalDateTime parseAsLocalDateTime(String dateTimeString) {
        return parseAsLocalDateTime(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Converts the given date/time string into a {@link LocalDateTime} using the given formatter.
     *
     * @param dateTimeString the date/time string to parse
     * @param formatter      the formatter to use
     * @return the parsed {@link LocalDateTime}
     * @see DateTimeFormatter#parse(CharSequence, TemporalQuery)
     */
    public static LocalDateTime parseAsLocalDateTime(String dateTimeString, DateTimeFormatter formatter) {
        checkArgumentNotBlank(dateTimeString);
        checkArgumentNotNull(formatter);

        return formatter.parse(dateTimeString, LocalDateTime::from);
    }

    /**
     * Converts the given date/time string into a {@link ZonedDateTime} using
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} as the formatter.
     *
     * @param dateTimeString the date/time string to parse
     * @return the parsed {@link ZonedDateTime}
     */
    public static ZonedDateTime parseAsZonedDateTime(String dateTimeString) {
        return parseAsZonedDateTime(dateTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Converts the given date/time string into a {@link ZonedDateTime} using the specified {@link DateTimeFormatter}.
     *
     * @param dateTimeString the date/time string to parse
     * @param formatter      the formatter to use
     * @return the parsed {@link ZonedDateTime}
     * @see DateTimeFormatter#parse(CharSequence, TemporalQuery)
     */
    public static ZonedDateTime parseAsZonedDateTime(String dateTimeString, DateTimeFormatter formatter) {
        checkArgumentNotBlank(dateTimeString);
        checkArgumentNotNull(formatter);

        return formatter.parse(dateTimeString, ZonedDateTime::from);
    }
}
