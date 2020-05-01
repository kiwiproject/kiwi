package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_DATE;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.TOKYO_TIME;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.US_EASTERN_TIME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@DisplayName("KiwiDateTimeFormatters")
class KiwiDateTimeFormattersTest {

    @Nested
    class FormatAsIsoLocalDate {

        @Test
        void shouldFormatDate_InUTC() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateUTC(GUY_FAWKES_AS_DATE))
                    .isEqualTo("2005-11-05");
        }

        @Test
        void shouldFormatDate_InGivenZoneId() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_DATE, US_EASTERN_TIME))
                    .isEqualTo("2005-11-05");
        }

        @Test
        void shouldFormatDate_InTimeZone_ThatRollsOverToNextDay() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_DATE, TOKYO_TIME))
                    .isEqualTo("2005-11-06");
        }

        @Test
        void shouldFormatLocalDate() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_LOCAL_DATE))
                    .isEqualTo("2005-11-05");
        }

        @Test
        void shouldFormatLocalDateTime() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST))
                    .isEqualTo("2005-11-05");
        }

        @Test
        void shouldFormatLocalDateTime_ThatRollsOverToNextDay() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_LOCAL_DATE_TIME_IN_TOKYO))
                    .isEqualTo("2005-11-06");
        }

        @Test
        void shouldFormatZonedDateTime() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST))
                    .isEqualTo("2005-11-05");
        }

        @Test
        void shouldFormatZonedDateTime_ThatRollsOverToNextDay() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDate(GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO))
                    .isEqualTo("2005-11-06");
        }
    }

    @Nested
    class FormatAsIsoLocalDateTime {

        @Test
        void shouldFormatDate_InUTC() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateTimeUTC(GUY_FAWKES_AS_DATE))
                    .isEqualTo("2005-11-05T20:00:00");
        }

        @Test
        void shouldFormatDate_InGivenZoneId() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateTime(GUY_FAWKES_AS_DATE, US_EASTERN_TIME))
                    .isEqualTo("2005-11-05T15:00:00");
        }

        @Test
        void shouldFormatDate_InGivenZoneId_ThatRollsOverToNetDay() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateTime(GUY_FAWKES_AS_DATE, TOKYO_TIME))
                    .isEqualTo("2005-11-06T05:00:00");
        }

        @Test
        void shouldFormatLocalDate() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateTime(GUY_FAWKES_AS_LOCAL_DATE))
                    .isEqualTo("2005-11-05T00:00:00");
        }

        @Test
        void shouldFormatLocalDateTime() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateTime(GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC))
                    .isEqualTo("2005-11-05T20:00:00");
        }

        @Test
        void shouldFormatZonedDateTime() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoLocalDateTime(GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO))
                    .isEqualTo("2005-11-06T05:00:00");
        }
    }

    @Nested
    class FormatAsIsoZonedDateTime {

        @Test
        void shouldFormatDate_InUTC() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTimeUTC(GUY_FAWKES_AS_DATE))
                    .isEqualTo("2005-11-05T20:00:00Z");
        }

        @Test
        void shouldFormatDate_InGivenZoneId() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTime(GUY_FAWKES_AS_DATE, US_EASTERN_TIME))
                    .isEqualTo("2005-11-05T15:00:00-05:00[US/Eastern]");
        }

        @Test
        void shouldFormatLocalDate_InUTC() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTimeUTC(GUY_FAWKES_AS_LOCAL_DATE))
                    .isEqualTo("2005-11-05T00:00:00Z");
        }

        @Test
        void shouldFormatLocalDate_InGivenZoneId() {
            // NOTE: the LocalDate is interpreted differently than Date. Don't you love dates, times, and time zones?
            assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTime(GUY_FAWKES_AS_LOCAL_DATE, US_EASTERN_TIME))
                    .isEqualTo("2005-11-04T19:00:00-05:00[US/Eastern]");
        }

        @Test
        void shouldFormatLocalDateTime_InUTC() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTimeUTC(GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC))
                    .isEqualTo("2005-11-05T20:00:00Z");
        }

        @Test
        void shouldFormatLocalDateTime_InGivenZoneId() {
            assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTime(
                    GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC, US_EASTERN_TIME))
                    .isEqualTo("2005-11-05T15:00:00-05:00[US/Eastern]");
        }

        @Test
        void shouldFormatZonedDateTime_InUTC() {
            assertAll(
                    () -> assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTimeUTC(
                            GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST))
                            .isEqualTo("2005-11-05T20:00:00Z"),

                    () -> assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTimeUTC(
                            GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC))
                            .isEqualTo("2005-11-05T20:00:00Z"),

                    () -> assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTimeUTC(
                            GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO))
                            .isEqualTo("2005-11-05T20:00:00Z")
            );
        }

        @Test
        void shouldFormatZonedDatetime_InGivenZoneId() {
            assertAll(
                    () -> assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTime(
                            GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST, US_EASTERN_TIME))
                            .isEqualTo("2005-11-05T15:00:00-05:00[US/Eastern]"),

                    () -> assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTime(
                            GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC, US_EASTERN_TIME))
                            .isEqualTo("2005-11-05T15:00:00-05:00[US/Eastern]"),

                    () -> assertThat(KiwiDateTimeFormatters.formatAsIsoZonedDateTime(
                            GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO, US_EASTERN_TIME))
                            .isEqualTo("2005-11-05T15:00:00-05:00[US/Eastern]")
            );
        }
    }

    @Nested
    class ShouldThrowException {

        @Test
        void whenLocalDate_UsedWithFormatter_HavingTimeSpecification() {
            assertThatThrownBy(() -> KiwiDateTimeFormatters.formatWith(
                    LocalDate.now(), DateTimeFormatter.ofPattern("HH:mm:ss"))
            ).isInstanceOf(DateTimeException.class);
        }

        @Test
        void whenLocalDateTime_UsedWithFormatter_HavingTimeZoneSpecification() {
            assertThatThrownBy(() -> KiwiDateTimeFormatters.formatWith(
                    LocalDateTime.now(), DateTimeFormatter.ofPattern("HH:mm:ss zzz"))
            ).isInstanceOf(DateTimeException.class);
        }
    }

}