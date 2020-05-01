package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_DATE;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO;
import static org.kiwiproject.time.KiwiDateTimeTestConstants.GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

@DisplayName("KiwiDateTimeParsers")
class KiwiDateTimeParsersTest {

    @Nested
    class ParseAsDate {

        @Test
        void shouldParseString_FormattedAs_ISO_ZonedDateTime() {
            assertAll(
                    () -> assertThat(KiwiDateTimeParsers.parseAsDate("2005-11-05T20:00:00Z"))
                            .isEqualTo(GUY_FAWKES_AS_DATE),
                    () -> assertThat(KiwiDateTimeParsers.parseAsDate("2005-11-05T20:00:00-00:00"))
                            .isEqualTo(GUY_FAWKES_AS_DATE)
            );
        }
    }

    @Nested
    class ParseAsLocalDate {

        @Test
        void shouldParseString_UsingDefaultFormatter() {
            assertThat(KiwiDateTimeParsers.parseAsLocalDate("2005-11-05"))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE);
        }

        @Test
        void shouldParseString_UsingExplicitFormatter() {
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            assertThat(KiwiDateTimeParsers.parseAsLocalDate("2005-11-06", formatter))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_IN_TOKYO);
        }
    }

    @Nested
    class ParseAsLocalDateTime {

        @Test
        void shouldParseString_UsingDefaultFormatter() {
            assertThat(KiwiDateTimeParsers.parseAsLocalDateTime("2005-11-05T20:00:00"))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC);
        }

        @Test
        void shouldParseString_UsingCustomFormatter() {
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            assertThat(KiwiDateTimeParsers.parseAsLocalDateTime("2005-11-05T15:00:00", formatter))
                    .isEqualTo(GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST);
        }
    }

    @Nested
    class ParseAsZonedDateTime {

        @Test
        void shouldParseString_UsingDefaultFormatter() {
            assertThat(KiwiDateTimeParsers.parseAsZonedDateTime("2005-11-05T20:00:00Z"))
                    .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC);
        }

        @Test
        void shouldParseString_UsingCustomFormatter() {
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV");
            assertThat(KiwiDateTimeParsers.parseAsZonedDateTime("2005-11-06T05:00:00+09:00", formatter))
                    .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO);
        }

        @Test
        void shouldParseString_UsingCustomFormatter_WithOptionalZoneIdSection() {
            var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV['['z']']");

            assertAll(
                    () -> assertThat(KiwiDateTimeParsers.parseAsZonedDateTime(
                            "2005-11-05T20:00:00Z", formatter))
                            .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC),
                    () -> assertThat(KiwiDateTimeParsers.parseAsZonedDateTime(
                            "2005-11-05T20:00:00-00:00", formatter))
                            .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC),
                    () -> assertThat(KiwiDateTimeParsers.parseAsZonedDateTime(
                            "2005-11-05T15:00:00-05:00", formatter))
                            .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST),
                    () -> assertThat(KiwiDateTimeParsers.parseAsZonedDateTime(
                            "2005-11-05T15:00:00-05:00[US/Eastern]", formatter))
                            .isEqualTo(GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST)
            );
        }
    }
}
