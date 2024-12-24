package org.kiwiproject.xml;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Static utilities for converting to/from XML data types, e.g. {@link XMLGregorianCalendar}.
 */
@UtilityClass
@Slf4j
public class KiwiXmlConverters {

    private static final DatatypeFactory FACTORY;

    static {
        try {
            FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.warn("Unable to create DatatypeFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Create a new {@link XMLGregorianCalendar} in the default time zone initialized to the current date/time.
     *
     * @return a new instance representing the current date/time
     */
    public static XMLGregorianCalendar newXMLGregorianCalendar() {
        return newXMLGregorianCalendar(ZoneId.systemDefault());
    }

    /**
     * Create a new {@link XMLGregorianCalendar} in UTC initialized to the current date/time.
     *
     * @return a new instance representing the current date/time
     */
    public static XMLGregorianCalendar newXMLGregorianCalendarUTC() {
        return newXMLGregorianCalendar(ZoneOffset.UTC);
    }

    /**
     * Create a new {@link XMLGregorianCalendar} in the given time zone initialized to the current date/time.
     *
     * @param zoneId the time zone for the new GregorianCalendar instance
     * @return a new instance representing the current date/time
     */
    public static XMLGregorianCalendar newXMLGregorianCalendar(ZoneId zoneId) {
        return FACTORY.newXMLGregorianCalendar(new GregorianCalendar(TimeZone.getTimeZone(zoneId)));
    }

    /**
     * Converts milliseconds since the epoch to an {@link XMLGregorianCalendar} object in the default time zone.
     *
     * @param epochMillis number of milliseconds since the epoch
     * @return a new instance representing the given number of epoch milliseconds
     */
    public static XMLGregorianCalendar epochMillisToXMLGregorianCalendar(long epochMillis) {
        return epochMillisToXMLGregorianCalendar(epochMillis, ZoneId.systemDefault());
    }

    /**
     * Converts milliseconds since the epoch to an {@link XMLGregorianCalendar} object in UTC.
     *
     * @param epochMillis number of milliseconds since the epoch
     * @return a new instance representing the given number of epoch milliseconds
     */
    public static XMLGregorianCalendar epochMillisToXMLGregorianCalendarUTC(long epochMillis) {
        return epochMillisToXMLGregorianCalendar(epochMillis, ZoneOffset.UTC);
    }

    /**
     * Converts milliseconds since the epoch to an {@link XMLGregorianCalendar} object in the given time zone.
     *
     * @param epochMillis number of milliseconds since the epoch
     * @param zoneId      the time zone for the new GregorianCalendar instance
     * @return a new instance representing the given number of epoch milliseconds
     */
    public static XMLGregorianCalendar epochMillisToXMLGregorianCalendar(long epochMillis, ZoneId zoneId) {
        var calendar = new GregorianCalendar(TimeZone.getTimeZone(zoneId));
        calendar.setTimeInMillis(epochMillis);

        return FACTORY.newXMLGregorianCalendar(calendar);
    }

    /**
     * Converts an {@link Instant} object to an {@link XMLGregorianCalendar} in the default time zone.
     *
     * @param instant the instant object to convert
     * @return a new instance representing the given instant in time
     */
    public static XMLGregorianCalendar instantToXMLGregorianCalendar(Instant instant) {
        return instantToXMLGregorianCalendar(instant, ZoneId.systemDefault());
    }

    /**
     * Converts an {@link Instant} object to an {@link XMLGregorianCalendar} in UTC.
     *
     * @param instant the instant object to convert
     * @return a new instance representing the given instant in time
     */
    public static XMLGregorianCalendar instantToXMLGregorianCalendarUTC(Instant instant) {
        return instantToXMLGregorianCalendar(instant, ZoneOffset.UTC);
    }

    /**
     * Converts an {@link Instant} object to an {@link XMLGregorianCalendar} in the given time zone.
     *
     * @param instant the instant object to convert
     * @param zoneId  the time zone for the new GregorianCalendar instance
     * @return a new instance representing the given instant in time
     */
    public static XMLGregorianCalendar instantToXMLGregorianCalendar(Instant instant, ZoneId zoneId) {
        checkArgumentNotNull(instant);

        var calendar = new GregorianCalendar(TimeZone.getTimeZone(zoneId));
        calendar.setTimeInMillis(instant.toEpochMilli());

        return FACTORY.newXMLGregorianCalendar(calendar);
    }

    /**
     * Converts a {@link Date} to an {@link XMLGregorianCalendar} in the default time zone.
     *
     * @param date the date object to convert
     * @return a new instance representing the given date
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date) {
        return dateToXMLGregorianCalendar(date, ZoneId.systemDefault());
    }

    /**
     * Converts a {@link Date} to an {@link XMLGregorianCalendar} in UTC.
     *
     * @param date the date object to convert
     * @return a new instance representing the given date
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendarUTC(Date date) {
        return dateToXMLGregorianCalendar(date, ZoneOffset.UTC);
    }

    /**
     * Converts a {@link Date} to an {@link XMLGregorianCalendar} in the given time zone.
     *
     * @param date   the date object to convert
     * @param zoneId the time zone for the new GregorianCalendar instance
     * @return a new instance representing the given date
     */
    public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date, ZoneId zoneId) {
        checkArgumentNotNull(date);

        var calendar = new GregorianCalendar(TimeZone.getTimeZone(zoneId));
        calendar.setTime(date);

        return FACTORY.newXMLGregorianCalendar(calendar);
    }

    /**
     * Converts an {@link XMLGregorianCalendar} to an {@link Instant}.
     *
     * @param xmlGregorianCalendar the {@link XMLGregorianCalendar} to convert
     * @return the instant representing the given {@link XMLGregorianCalendar}
     */
    public static Instant xmlGregorianCalendarToInstant(XMLGregorianCalendar xmlGregorianCalendar) {
        checkArgumentNotNull(xmlGregorianCalendar);

        return xmlGregorianCalendar.toGregorianCalendar().toInstant();
    }

    /**
     * Converts an {@link XMLGregorianCalendar} to a {@link LocalDateTime} in the default time zone.
     *
     * @param xmlGregorianCalendar the {@link XMLGregorianCalendar} to convert
     * @return the local date/time representing the given {@link XMLGregorianCalendar}
     */
    public static LocalDateTime xmlGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendarToLocalDateTime(xmlGregorianCalendar, ZoneId.systemDefault());
    }

    /**
     * Converts an {@link XMLGregorianCalendar} to a {@link LocalDateTime} using the given {@link ZoneId}.
     *
     * @param xmlGregorianCalendar the {@link XMLGregorianCalendar} to convert
     * @param zoneId               the time-zone, which may be an offset, not null
     * @return the local date/time representing the given {@link XMLGregorianCalendar}
     */
    public static LocalDateTime xmlGregorianCalendarToLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar,
                                                                    ZoneId zoneId) {
        checkArgumentNotNull(xmlGregorianCalendar);
        checkArgumentNotNull(zoneId);

        return LocalDateTime.ofInstant(xmlGregorianCalendarToInstant(xmlGregorianCalendar), zoneId);
    }

    /**
     * Given an {@link XMLGregorianCalendar}, return its value in milliseconds since the epoch.
     *
     * @param xmlGregorianCalendar the {@link XMLGregorianCalendar} to convert
     * @return the epoch milliseconds representing the given {@link XMLGregorianCalendar}
     */
    public static long xmlGregorianCalendarToEpochMillis(XMLGregorianCalendar xmlGregorianCalendar) {
        return xmlGregorianCalendarToInstant(xmlGregorianCalendar).toEpochMilli();
    }

    /**
     * Given an {@link XMLGregorianCalendar}, return its value as a {@link Date}.
     *
     * @param xmlGregorianCalendar the {@link XMLGregorianCalendar} to convert
     * @return the date representing the given {@link XMLGregorianCalendar}
     */
    public static Date xmlGregorianCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
        var epochMillis = xmlGregorianCalendarToEpochMillis(xmlGregorianCalendar);
        return new Date(epochMillis);
    }
}
