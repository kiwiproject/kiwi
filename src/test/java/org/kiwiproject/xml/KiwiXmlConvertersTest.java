package org.kiwiproject.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Date;
import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

@DisplayName("KiwiXmlConverters")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiXmlConvertersTest {

    @Nested
    class NewXMLGregorianCalendar {

        @Test
        void shouldCreateNewInstance() {
            var xmlGregorianCalendar = KiwiXmlConverters.newXMLGregorianCalendar();
            assertEpochMillis(xmlGregorianCalendar);
        }

        @Test
        void shouldCreateNewInstanceInUTC() {
            var xmlGregorianCalendar = KiwiXmlConverters.newXMLGregorianCalendarUTC();
            assertTimezoneIsUTC(xmlGregorianCalendar);
            assertEpochMillis(xmlGregorianCalendar);
        }

        private void assertEpochMillis(XMLGregorianCalendar xmlGregorianCalendar) {
            var epochMilli = xmlGregorianCalendar.toGregorianCalendar().toInstant().toEpochMilli();
            long diffInMillis = System.currentTimeMillis() - epochMilli;
            var allowedDifference = 500;
            assertThat(diffInMillis)
                    .describedAs("%d millis diff between now and new XMLGregorianCalendar is more than allowed %d millis",
                            diffInMillis, allowedDifference)
                    .isLessThanOrEqualTo(allowedDifference);
        }
    }


    @Nested
    class EpochMillisToXMLGregorianCalendar {

        @Test
        void shouldConvert() {
            var epochMillis = System.currentTimeMillis();
            var xmlGregorianCalendar = KiwiXmlConverters.epochMillisToXMLGregorianCalendar(epochMillis);

            assertThat(xmlGregorianCalendar.toGregorianCalendar().getTimeInMillis())
                    .isEqualTo(epochMillis);
        }

        @Test
        void shouldConvertUTC() {
            var epochMillis = System.currentTimeMillis();
            var xmlGregorianCalendar = KiwiXmlConverters.epochMillisToXMLGregorianCalendarUTC(epochMillis);

            assertTimezoneIsUTC(xmlGregorianCalendar);
            assertThat(xmlGregorianCalendar.toGregorianCalendar().getTimeInMillis())
                    .isEqualTo(epochMillis);
        }
    }

    @Nested
    class InstantToXMLGregorianCalendar {

        @Test
        void shouldNotAllowNullArg() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.instantToXMLGregorianCalendar(null));
        }

        @Test
        void shouldConvert(SoftAssertions softly) {
            var zonedDateTime = ZonedDateTime.of(2018, 1, 1, 0, 0, 0, 0, ZoneOffset.systemDefault());
            var xmlGregorianCalendar = KiwiXmlConverters.instantToXMLGregorianCalendar(zonedDateTime.toInstant());
            xmlGregorianCalendar.setTimezone(0);

            softly.assertThat(xmlGregorianCalendar.toXMLFormat()).isEqualTo("2018-01-01T00:00:00.000Z");

            softly.assertThat(xmlGregorianCalendar.getDay()).isEqualTo(zonedDateTime.getDayOfMonth());
            softly.assertThat(xmlGregorianCalendar.getMonth()).isEqualTo(zonedDateTime.getMonthValue());
            softly.assertThat(xmlGregorianCalendar.getYear()).isEqualTo(zonedDateTime.getYear());
            softly.assertThat(xmlGregorianCalendar.getHour()).isEqualTo(zonedDateTime.getHour());
            softly.assertThat(xmlGregorianCalendar.getMinute()).isEqualTo(zonedDateTime.getMinute());
            softly.assertThat(xmlGregorianCalendar.getSecond()).isEqualTo(zonedDateTime.getSecond());
        }

        @Test
        void shouldConvertUTC() {
            var now = Instant.now();
            var xmlGregorianCalendar = KiwiXmlConverters.instantToXMLGregorianCalendarUTC(now);

            assertTimezoneIsUTC(xmlGregorianCalendar);
        }
    }

    @Nested
    class DateToXMLGregorianCalendar {

        @Test
        void shouldNotAllowNullArg() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.dateToXMLGregorianCalendar(null));
        }

        @Test
        void shouldConvert(SoftAssertions softly) {
            var calendar = new GregorianCalendar(2019, Calendar.DECEMBER, 26, 14, 30, 15);

            var xmlGregorianCalendar = KiwiXmlConverters.dateToXMLGregorianCalendar(calendar.getTime());
            xmlGregorianCalendar.setTimezone(0);

            softly.assertThat(xmlGregorianCalendar.toXMLFormat()).isEqualTo("2019-12-26T14:30:15.000Z");

            softly.assertThat(xmlGregorianCalendar.getDay()).isEqualTo(calendar.get(Calendar.DAY_OF_MONTH));
            softly.assertThat(xmlGregorianCalendar.getMonth()).isEqualTo(calendar.get(Calendar.MONTH) + 1);
            softly.assertThat(xmlGregorianCalendar.getYear()).isEqualTo(calendar.get(Calendar.YEAR));
            softly.assertThat(xmlGregorianCalendar.getHour()).isEqualTo(calendar.get(Calendar.HOUR_OF_DAY));
            softly.assertThat(xmlGregorianCalendar.getMinute()).isEqualTo(calendar.get(Calendar.MINUTE));
            softly.assertThat(xmlGregorianCalendar.getSecond()).isEqualTo(calendar.get(Calendar.SECOND));
            softly.assertThat(xmlGregorianCalendar.getMillisecond()).isEqualTo(calendar.get(Calendar.MILLISECOND));
        }

        @Test
        void shouldConvertUTC() {
            var now = Date.from(Instant.now());
            var xmlGregorianCalendar = KiwiXmlConverters.dateToXMLGregorianCalendarUTC(now);

            assertTimezoneIsUTC(xmlGregorianCalendar);
        }
    }

    @Nested
    class XmlGregorianCalendarToInstant {

        @Test
        void shouldNotAllowNullArg() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.xmlGregorianCalendarToInstant(null));
        }

        @Test
        void shouldConvert() {
            var calendar = Calendar.getInstance();

            var xmlGregorianCalendar = KiwiXmlConverters.dateToXMLGregorianCalendar(calendar.getTime());

            var instant = KiwiXmlConverters.xmlGregorianCalendarToInstant(xmlGregorianCalendar);
            assertThat(instant).isEqualTo(calendar.getTime().toInstant());
        }
    }

    @Nested
    class XmlGregorianCalendarToLocalDateTime {

        @Test
        void shouldNotAllowNullArgs() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.xmlGregorianCalendarToLocalDateTime(null));

            var xmlGregorianCalendar = KiwiXmlConverters.epochMillisToXMLGregorianCalendar(System.currentTimeMillis());
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.xmlGregorianCalendarToLocalDateTime(xmlGregorianCalendar, null));
        }

        @Test
        void shouldConvert(SoftAssertions softly) {
            var calendar = Calendar.getInstance();
            var xmlGregorianCalendar = KiwiXmlConverters.epochMillisToXMLGregorianCalendar(calendar.getTimeInMillis());

            var localDateTime = KiwiXmlConverters.xmlGregorianCalendarToLocalDateTime(xmlGregorianCalendar);

            softly.assertThat(localDateTime.getDayOfMonth()).isEqualTo(xmlGregorianCalendar.getDay());
            softly.assertThat(localDateTime.getMonthValue()).isEqualTo(xmlGregorianCalendar.getMonth());
            softly.assertThat(localDateTime.getYear()).isEqualTo(xmlGregorianCalendar.getYear());
            softly.assertThat(localDateTime.getHour()).isEqualTo(xmlGregorianCalendar.getHour());
            softly.assertThat(localDateTime.getMinute()).isEqualTo(xmlGregorianCalendar.getMinute());
            softly.assertThat(localDateTime.getSecond()).isEqualTo(xmlGregorianCalendar.getSecond());
        }
    }

    @Nested
    class XmlGregorianCalendarToEpochMillis {

        @Test
        void shouldNotAllowNullArg() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.xmlGregorianCalendarToEpochMillis(null));
        }

        @Test
        void shouldConvert() throws DatatypeConfigurationException {
            var cal = new GregorianCalendar(2018, Calendar.FEBRUARY, 27, 15, 54, 19);
            var xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

            var epochMillis = KiwiXmlConverters.xmlGregorianCalendarToEpochMillis(xmlGregorianCalendar);

            var expected = ZonedDateTime.of(2018, Month.FEBRUARY.getValue(), 27, 15, 54, 19, 0, ZoneId.systemDefault());
            assertThat(epochMillis).isEqualTo(expected.toInstant().toEpochMilli());
        }
    }

    @Nested
    class XmlGregorianCalendarToDate {

        @Test
        void shouldNotAllowNullArg() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiXmlConverters.xmlGregorianCalendarToDate(null));
        }

        @Test
        void shouldConvert() throws DatatypeConfigurationException {
            var cal = new GregorianCalendar(2019, Calendar.SEPTEMBER, 8, 7, 47, 7);
            var xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

            var date = KiwiXmlConverters.xmlGregorianCalendarToDate(xmlGregorianCalendar);

            var expected = ZonedDateTime.of(2019, Month.SEPTEMBER.getValue(), 8, 7, 47, 7, 0, ZoneId.systemDefault());
            assertThat(date).isEqualTo(Date.from(expected.toInstant()));
        }
    }

    private void assertTimezoneIsUTC(XMLGregorianCalendar xmlGregorianCalendar) {
        assertThat(xmlGregorianCalendar.getTimezone()).isZero();
    }
}
