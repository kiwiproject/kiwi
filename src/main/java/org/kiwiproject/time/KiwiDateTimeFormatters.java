package org.kiwiproject.time;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * A collection of small utilities to format various types of Java date/time classes, both the legacy
 * {@link java.util.Date} and the Java 8 date/time API classes in {@code java.time}.
 * <p>
 * None of these are difficult to implement, but if you are constantly doing them, the time and code adds up
 * over time.
 * <p>
 * All methods throw {@link IllegalArgumentException} if {@code null} arguments are passed to them.
 */
@UtilityClass
public class KiwiDateTimeFormatters {

    /**
     * Converts the given {@link Date} in the UTC time zone into a string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE} formatter.
     *
     * @param date the date to convert
     * @return the formatted date string
     */
    public static String formatAsIsoLocalDateUTC(Date date) {
        return formatWithUTC(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Converts the given {@link Date} in the given time zone into a string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE} formatter.
     *
     * @param date         the date to convert
     * @param targetZoneId the {@link ZoneId} where the output string should say that the date resides
     * @return the formatted date string
     */
    public static String formatAsIsoLocalDate(Date date, ZoneId targetZoneId) {
        return formatWith(date, targetZoneId, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Converts the given {@link TemporalAccessor} into a string using the {@link DateTimeFormatter#ISO_LOCAL_DATE}
     * formatter.
     *
     * @param dateTime the date/time to convert
     * @return the formatted date string
     */
    public static String formatAsIsoLocalDate(TemporalAccessor dateTime) {
        return formatWith(dateTime, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Converts the given {@link Date} in the UTC time zone into a string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} formatter.
     *
     * @param date the date to convert
     * @return the formatted date/time string
     */
    public static String formatAsIsoLocalDateTimeUTC(Date date) {
        return formatWithUTC(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Converts the given {@link Date} in the given time zone into a string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} formatter.
     *
     * @param date         the date to convert
     * @param targetZoneId the {@link ZoneId} where the output string should say that the date resides
     * @return the formatted date/time string
     */
    public static String formatAsIsoLocalDateTime(Date date, ZoneId targetZoneId) {
        return formatWith(date, targetZoneId, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Converts the given {@link LocalDate} (at the start of the day) into a string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} formatter.
     *
     * @param localDate the date to convert
     * @return the formatted date/time string
     * @see LocalDate#atStartOfDay()
     */
    public static String formatAsIsoLocalDateTime(LocalDate localDate) {
        return formatWith(localDate.atStartOfDay(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Converts the given {@link TemporalAccessor} (at the start of the day) into a string using the
     * {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} formatter.
     *
     * @param dateTime the date/time to convert
     * @return the formatted date/time string
     */
    public static String formatAsIsoLocalDateTime(TemporalAccessor dateTime) {
        return formatWith(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Converts the given {@link Date} in the UTC time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param date the date to convert
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTimeUTC(Date date) {
        return formatAsIsoZonedDateTime(date, ZoneOffset.UTC);
    }

    /**
     * Converts the given {@link Date} in the given time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param date         the date to convert
     * @param targetZoneId the {@link ZoneId} where the output string should say that the date resides
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTime(Date date, ZoneId targetZoneId) {
        return formatWith(date, targetZoneId, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Converts the given {@link LocalDate} (at the start of the day) in the UTC time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param localDate the date to convert
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTimeUTC(LocalDate localDate) {
        return formatAsIsoZonedDateTimeUTC(localDate.atStartOfDay());
    }

    /**
     * Converts the given {@link LocalDate} (at the start of the day) in the given time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param localDate    the date to convert
     * @param targetZoneId the {@link ZoneId} where the output string should say that the date resides
     * @return the formatted date/time string
     * @see LocalDate#atStartOfDay()
     */
    public static String formatAsIsoZonedDateTime(LocalDate localDate, ZoneId targetZoneId) {
        return formatAsIsoZonedDateTime(localDate.atStartOfDay(), targetZoneId);
    }

    /**
     * Converts the given {@link LocalDateTime} in the UTC time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param localDateTime the date/time to convert
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTimeUTC(LocalDateTime localDateTime) {
        return formatAsIsoZonedDateTime(localDateTime, ZoneOffset.UTC);
    }

    /**
     * Converts the given {@link LocalDateTime} in the given time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param localDateTime the date/time to convert
     * @param targetZoneId  the {@link ZoneId} where the output string should say that the date resides
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTime(LocalDateTime localDateTime, ZoneId targetZoneId) {
        return formatWith(localDateTime, targetZoneId, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Converts the given {@link ZonedDateTime} in the UTC time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param zonedDateTime the date/time to convert
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTimeUTC(ZonedDateTime zonedDateTime) {
        return formatAsIsoZonedDateTime(zonedDateTime, ZoneOffset.UTC);
    }

    /**
     * Converts the given {@link ZonedDateTime} in the given time zone into a string using the
     * {@link DateTimeFormatter#ISO_ZONED_DATE_TIME} formatter.
     *
     * @param zonedDateTime the date/time to convert
     * @param targetZoneId  the {@link ZoneId} where the output string should say that the date resides
     * @return the formatted date/time string
     */
    public static String formatAsIsoZonedDateTime(ZonedDateTime zonedDateTime, ZoneId targetZoneId) {
        return formatWith(zonedDateTime, targetZoneId, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Converts the given {@link Date} in the UTC time zone into a string using the given {@link DateTimeFormatter}.
     *
     * @param date      the date to convert
     * @param formatter the specific formatter to use
     * @return the formatted date string
     */
    public static String formatWithUTC(Date date, DateTimeFormatter formatter) {
        return formatWith(date, ZoneOffset.UTC, formatter);
    }

    /**
     * Converts the given {@link Date} in the given time zone into a string using the given {@link DateTimeFormatter}.
     *
     * @param date         the date to convert
     * @param targetZoneId the {@link ZoneId} where the output string should say that the date resides
     * @param formatter    the specific formatter to use
     * @return the formatted date string
     */
    public static String formatWith(Date date, ZoneId targetZoneId, DateTimeFormatter formatter) {
        checkArgumentNotNull(date);
        checkArgumentNotNull(targetZoneId);
        checkArgumentNotNull(formatter);

        var utcZonedDateTime = date.toInstant().atZone(ZoneOffset.UTC);
        return formatter.withZone(targetZoneId).format(utcZonedDateTime);
    }

    /**
     * Converts the given {@link LocalDateTime} (assuming it is in the UTC time zone) into a string using the
     * given {@link DateTimeFormatter}.
     *
     * @param localDateTime the date/time to convert
     * @param targetZoneId  the {@link ZoneId} where the output string should say that the date resides
     * @param formatter     the specific formatter to use
     * @return the formatted date string
     */
    public static String formatWith(LocalDateTime localDateTime, ZoneId targetZoneId, DateTimeFormatter formatter) {
        return formatWith(localDateTime, ZoneOffset.UTC, targetZoneId, formatter);
    }

    /**
     * Converts the given {@link LocalDateTime} (assuming it is in the given {@code localZoneId} time zone) into a
     * string using the given {@link DateTimeFormatter}.
     *
     * @param localDateTime the date/time to convert
     * @param localZoneId   the {@link ZoneId} to set the {@link LocalDateTime} in before converting to the target zone
     * @param targetZoneId  the {@link ZoneId} where the output string should say that the date resides
     * @param formatter     the specific formatter to use
     * @return the formatted date string
     */
    public static String formatWith(LocalDateTime localDateTime,
                                    ZoneId localZoneId,
                                    ZoneId targetZoneId,
                                    DateTimeFormatter formatter) {
        checkArgumentNotNull(localDateTime);
        checkArgumentNotNull(localZoneId);
        checkArgumentNotNull(targetZoneId);
        checkArgumentNotNull(formatter);

        var zonedDateTime = localDateTime.atZone(localZoneId);
        return formatter.withZone(targetZoneId).format(zonedDateTime);
    }

    /**
     * Converts the given {@link ZonedDateTime} in the given time zone into a string using the
     * given {@link DateTimeFormatter}.
     *
     * @param zonedDateTime the date/time to convert
     * @param targetZoneId  the {@link ZoneId} where the output string should say that the date resides
     * @param formatter     the specific formatter to use
     * @return the formatted date string
     */
    public static String formatWith(ZonedDateTime zonedDateTime, ZoneId targetZoneId, DateTimeFormatter formatter) {
        checkArgumentNotNull(zonedDateTime);
        checkArgumentNotNull(targetZoneId);
        checkArgumentNotNull(formatter);

        return formatter.withZone(targetZoneId).format(zonedDateTime);
    }

    /**
     * Converts the given {@link TemporalAccessor} into a string using the given {@link DateTimeFormatter}. Assumes
     * that the caller has already set the appropriate time zone on the {@link TemporalAccessor}.
     * <p>
     * This is just a static wrapper around {@link DateTimeFormatter#format(TemporalAccessor)}.
     *
     * @param temporalAccessor the date/time to convert
     * @param formatter        the specific formatter to use
     * @return the formatted date/time string
     */
    public static String formatWith(TemporalAccessor temporalAccessor, DateTimeFormatter formatter) {
        checkArgumentNotNull(temporalAccessor);
        checkArgumentNotNull(formatter);

        return formatter.format(temporalAccessor);
    }

}
