package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_DATE;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_DATE_AT_MIDNIGHT;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.TOKYO_TIME;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.US_EASTERN_TIME;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @implNote Uses constants defined in {@link KiwiDateTimeTestConstants}.
 */
@DisplayName("KiwiDateTimeConverters")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiDateTimeConvertersTest {

    @Nested
    class ToDateFromLocalDate {

        @Test
        void shouldNotAllowNullLocalDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toDate((LocalDate) null));
        }

        @Test
        void shouldConvertToDate() {
            assertThat(KiwiDateTimeConverters.toDate(GUY_FAWKES_AS_LOCAL_DATE))
                    .isEqualTo(GUY_FAWKES_AS_DATE_AT_MIDNIGHT);
        }
    }

    @Nested
    class ToDateFromLocalDateTime {

        @Test
        void shouldNotAllowNullLocalDateTime() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toDate((LocalDateTime) null));
        }

        @Test
        void shouldConvertToDate() {
            assertThat(KiwiDateTimeConverters.toDate(GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC))
                    .isEqualTo(GUY_FAWKES_AS_DATE);
        }
    }

    @Nested
    class ToDateFromZonedDateTime {

        @Test
        void shouldNotAllowNullZonedDateTime() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toDate((ZonedDateTime) null));
        }

        @Test
        void shouldConvertToDate_FromDifferentTimeZones(SoftAssertions softly) {
            softly.assertThat(KiwiDateTimeConverters.toDate(GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST))
                    .isEqualTo(GUY_FAWKES_AS_DATE);

            softly.assertThat(KiwiDateTimeConverters.toDate(GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC))
                    .isEqualTo(GUY_FAWKES_AS_DATE);

            softly.assertThat(KiwiDateTimeConverters.toDate(GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO))
                    .isEqualTo(GUY_FAWKES_AS_DATE);
        }
    }

    @Nested
    class ToLocalDateUTC {

        @Test
        void shouldNotAllowNullDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toLocalDateUTC(null));
        }

        @Test
        void shouldConvertToLocalDate() {
            assertThat(KiwiDateTimeConverters.toLocalDateUTC(GUY_FAWKES_AS_DATE))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE);
        }
    }

    @Nested
    class ToLocalDate {

        @Test
        void shouldNotAllowNullDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toLocalDate(null, US_EASTERN_TIME));
        }

        @Test
        void shouldNotAllowNullZoneId() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toLocalDate(GUY_FAWKES_AS_DATE, null));
        }

        @Test
        void shouldConvertToLocalDate() {
            assertThat(KiwiDateTimeConverters.toLocalDate(GUY_FAWKES_AS_DATE, US_EASTERN_TIME))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE);
        }

        @Test
        void shouldConvertToLocalDate_WhenInNextDayBasedOnTimeZone() {
            assertThat(KiwiDateTimeConverters.toLocalDate(GUY_FAWKES_AS_DATE, TOKYO_TIME))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_IN_TOKYO);
        }
    }

    @Nested
    class ToLocalDateTimeUTC {

        @Test
        void shouldNotAllowNullDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toLocalDateTimeUTC(null));
        }

        @Test
        void shouldConvertToLocalDate() {
            assertThat(KiwiDateTimeConverters.toLocalDateTimeUTC(GUY_FAWKES_AS_DATE))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC);
        }
    }

    @Nested
    class ToLocalDateTime {

        @Test
        void shouldNotAllowNullDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toLocalDateTime(null, US_EASTERN_TIME));
        }

        @Test
        void shouldNotAllowNullZoneId() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toLocalDateTime(GUY_FAWKES_AS_DATE, null));
        }

        @Test
        void shouldConvertToLocalDateTime_FromDifferentTimeZones(SoftAssertions softly) {
            softly.assertThat(KiwiDateTimeConverters.toLocalDateTime(GUY_FAWKES_AS_DATE, US_EASTERN_TIME))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST);

            softly.assertThat(KiwiDateTimeConverters.toLocalDateTime(GUY_FAWKES_AS_DATE, ZoneOffset.UTC))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC);

            softly.assertThat(KiwiDateTimeConverters.toLocalDateTime(GUY_FAWKES_AS_DATE, TOKYO_TIME))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_TIME_IN_TOKYO);
        }
    }

    @Nested
    class ToZonedDateTimeUTC {

        @Test
        void shouldNotAllowNullDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toZonedDateTimeUTC(null));
        }

        @Test
        void shouldConvertToZonedDateTime() {
            assertThat(KiwiDateTimeConverters.toZonedDateTimeUTC(GUY_FAWKES_AS_DATE))
                    .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC);
        }
    }

    @Nested
    class ToZonedDateTime {

        @Test
        void shouldNotAllowNullDate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toZonedDateTime(null, US_EASTERN_TIME));
        }

        @Test
        void shouldNotAllowNullTimeZone() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiDateTimeConverters.toZonedDateTime(GUY_FAWKES_AS_DATE, null));
        }

        @Test
        void shouldConvertToZonedDateTime_FromDifferentTimeZones(SoftAssertions softly) {
            softly.assertThat(KiwiDateTimeConverters.toZonedDateTime(GUY_FAWKES_AS_DATE, US_EASTERN_TIME))
                    .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST);

            softly.assertThat(KiwiDateTimeConverters.toZonedDateTime(GUY_FAWKES_AS_DATE, ZoneOffset.UTC))
                    .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC);

            softly.assertThat(KiwiDateTimeConverters.toZonedDateTime(GUY_FAWKES_AS_DATE, TOKYO_TIME))
                    .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO);
        }
    }
}