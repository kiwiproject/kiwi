package org.kiwiproject.xml;

import static org.kiwiproject.xml.KiwiXmlConverters.dateToXMLGregorianCalendar;
import static org.kiwiproject.xml.KiwiXmlConverters.dateToXMLGregorianCalendarUTC;
import static org.kiwiproject.xml.KiwiXmlConverters.epochMillisToXMLGregorianCalendar;
import static org.kiwiproject.xml.KiwiXmlConverters.epochMillisToXMLGregorianCalendarUTC;
import static org.kiwiproject.xml.KiwiXmlConverters.instantToXMLGregorianCalendar;
import static org.kiwiproject.xml.KiwiXmlConverters.instantToXMLGregorianCalendarUTC;
import static org.kiwiproject.xml.KiwiXmlConverters.newXMLGregorianCalendar;
import static org.kiwiproject.xml.KiwiXmlConverters.newXMLGregorianCalendarUTC;
import static org.kiwiproject.xml.KiwiXmlConverters.xmlGregorianCalendarToDate;
import static org.kiwiproject.xml.KiwiXmlConverters.xmlGregorianCalendarToEpochMillis;
import static org.kiwiproject.xml.KiwiXmlConverters.xmlGregorianCalendarToInstant;
import static org.kiwiproject.xml.KiwiXmlConverters.xmlGregorianCalendarToLocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;

/**
 * This is not a test per se. It is here to provide some examples of using KiwiXmlConverters to view the output
 * of the various methods in various time zones.
 */
@Slf4j
class KiwiXmlConvertersSamplesTest {

    // Suppress Sonar "Tests should include assertions"
    @SuppressWarnings("java:S2699")
    @Test
    void samples() {
        LOG.info("KiwiXmlConverters Samples:");
        LOG.info("");

        var chicagoZone = ZoneId.of("America/Chicago");
        LOG.info("ZoneId.systemDefault(): {}", ZoneId.systemDefault());
        LOG.info("");

        LOG.info("newXMLGregorianCalendarUTC: {}", newXMLGregorianCalendarUTC().toXMLFormat());
        LOG.info("newXMLGregorianCalendar(): {}", newXMLGregorianCalendar().toXMLFormat());
        LOG.info("newXMLGregorianCalendar(chicagoZone): {}", newXMLGregorianCalendar(chicagoZone).toXMLFormat());
        LOG.info("");

        var nowMillis = System.currentTimeMillis();
        LOG.info("epochMillisToXMLGregorianCalendarUTC(nowMillis): {}", epochMillisToXMLGregorianCalendarUTC(nowMillis));
        LOG.info("epochMillisToXMLGregorianCalendar(nowMillis): {}", epochMillisToXMLGregorianCalendar(nowMillis));
        LOG.info("epochMillisToXMLGregorianCalendar(nowMillis, chicagoZone): {}", epochMillisToXMLGregorianCalendar(nowMillis, chicagoZone));
        LOG.info("");

        var nowInstant = Instant.ofEpochMilli(nowMillis);
        LOG.info("instantToXMLGregorianCalendarUTC(nowInstant): {}", instantToXMLGregorianCalendarUTC(nowInstant));
        LOG.info("instantToXMLGregorianCalendar(nowInstant): {}", instantToXMLGregorianCalendar(nowInstant));
        LOG.info("instantToXMLGregorianCalendar(nowInstant, chicagoZone): {}", instantToXMLGregorianCalendar(nowInstant, chicagoZone));
        LOG.info("");

        var nowDate = new Date(nowMillis);
        LOG.info("dateToXMLGregorianCalendarUTC(nowDate): {}", dateToXMLGregorianCalendarUTC(nowDate));
        LOG.info("dateToXMLGregorianCalendar(nowDate): {}", dateToXMLGregorianCalendar(nowDate));
        LOG.info("dateToXMLGregorianCalendar(nowDate, chicagoZone): {}", dateToXMLGregorianCalendar(nowDate, chicagoZone));
        LOG.info("");

        LOG.info("xmlGregorianCalendarToInstant(calUTC): {}", xmlGregorianCalendarToInstant(instantToXMLGregorianCalendarUTC(nowInstant)));
        LOG.info("xmlGregorianCalendarToInstant(calDefaultZone): {}", xmlGregorianCalendarToInstant(instantToXMLGregorianCalendar(nowInstant)));
        LOG.info("xmlGregorianCalendarToInstant(calChicagoZone): {}", xmlGregorianCalendarToInstant(instantToXMLGregorianCalendar(nowInstant, chicagoZone)));
        LOG.info("");

        LOG.info("xmlGregorianCalendarToLocalDateTime(calUTC): {}", xmlGregorianCalendarToLocalDateTime(instantToXMLGregorianCalendarUTC(nowInstant)));
        LOG.info("xmlGregorianCalendarToLocalDateTime(calUTC, chicagoZone): {}", xmlGregorianCalendarToLocalDateTime(instantToXMLGregorianCalendarUTC(nowInstant), chicagoZone));
        LOG.info("xmlGregorianCalendarToLocalDateTime(calDefaultZone, chicagoZone): {}", xmlGregorianCalendarToLocalDateTime(instantToXMLGregorianCalendar(nowInstant), chicagoZone));
        LOG.info("xmlGregorianCalendarToLocalDateTime(calChicago, defaultZone): {}", xmlGregorianCalendarToLocalDateTime(instantToXMLGregorianCalendar(nowInstant, chicagoZone), ZoneId.systemDefault()));
        LOG.info("");

        LOG.info("xmlGregorianCalendarToEpochMillis(calUTC): {}", xmlGregorianCalendarToEpochMillis(instantToXMLGregorianCalendarUTC(nowInstant)));
        LOG.info("xmlGregorianCalendarToEpochMillis(calDefaultZone): {}", xmlGregorianCalendarToEpochMillis(instantToXMLGregorianCalendar(nowInstant)));
        LOG.info("xmlGregorianCalendarToEpochMillis(calChicago): {}", xmlGregorianCalendarToEpochMillis(instantToXMLGregorianCalendar(nowInstant, chicagoZone)));
        LOG.info("");

        LOG.info("xmlGregorianCalendarToDate(calUTC): {}", xmlGregorianCalendarToDate(instantToXMLGregorianCalendarUTC(nowInstant)));
        LOG.info("xmlGregorianCalendarToDate(calDefaultZone): {}", xmlGregorianCalendarToDate(instantToXMLGregorianCalendar(nowInstant)));
        LOG.info("xmlGregorianCalendarToDate(calChicago): {}", xmlGregorianCalendarToDate(instantToXMLGregorianCalendar(nowInstant, chicagoZone)));
        LOG.info("");
    }
}
