package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@DisplayName("KiwiDate")
class KiwiDatesTest {

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
    }

    @Nested
    class DateFrom {

        @Test
        void shouldConvertInstantToDate() {
            assertThat(KiwiDates.dateFrom(now)).isEqualTo(Date.from(now));
        }
    }

    @Nested
    class DateFromInstantOrNull {

        @Test
        void shouldReturnNullWhenGivenNull() {
            assertThat(KiwiDates.dateFromInstantOrNull(null)).isNull();
        }

        @Test
        void shouldConvertNonNullInstant() {
            var instant = Instant.now();
            var date = KiwiDates.dateFromInstantOrNull(instant);

            assertThat(date).isEqualTo(instant);
        }
    }

    @Nested
    class MinuteAdjusters {

        private Instant aMinuteAgo;

        @BeforeEach
        void setUp() {
            aMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
        }

        @Test
        void shouldAddMinutes() {
            assertThat(KiwiDates.dateFromInstantPlusMinutes(aMinuteAgo, 5))
                    .isEqualTo(Date.from(now.plus(4, ChronoUnit.MINUTES)));
        }

        @Test
        void shouldSubtractMinutes() {
            assertThat(KiwiDates.dateFromInstantMinusMinutes(aMinuteAgo, 5))
                    .isEqualTo(Date.from(now.minus(6, ChronoUnit.MINUTES)));
        }
    }

    @Nested
    class HourAdjusters {

        private Instant anHourAgo;

        @BeforeEach
        void setUp() {
            anHourAgo = now.minus(1, ChronoUnit.HOURS);
        }

        @Test
        void shouldAddHours() {
            assertThat(KiwiDates.dateFromInstantPlusHours(anHourAgo, 2))
                    .isEqualTo(Date.from(now.plus(1, ChronoUnit.HOURS)));
        }

        @Test
        void shouldSubtractHours() {
            assertThat(KiwiDates.dateFromInstantMinusHours(anHourAgo, 2))
                    .isEqualTo(Date.from(now.minus(3, ChronoUnit.HOURS)));
        }
    }

    @Nested
    class DayAdjusters {

        private Instant aDayAgo;

        @BeforeEach
        void setUp() {
            aDayAgo = now.minus(1, ChronoUnit.DAYS);
        }

        @Test
        void shouldAddDays() {
            assertThat(KiwiDates.dateFromInstantPlusDays(aDayAgo, 2))
                    .isEqualTo(Date.from(now.plus(1, ChronoUnit.DAYS)));
        }

        @Test
        void shouldSubtractDays() {
            assertThat(KiwiDates.dateFromInstantMinusDays(aDayAgo, 2))
                    .isEqualTo(Date.from(now.minus(3, ChronoUnit.DAYS)));
        }
    }

    /**
     * @implNote Because {@link Instant} does not deal with {@link TemporalUnit} values greater than
     * {@link ChronoUnit#DAYS}, these tests use {@link ZonedDateTime} to adjust the time scales.
     */
    @Nested
    class MonthAdjusters {

        private ZonedDateTime zdtNow;
        private Instant aMonthAgo;

        @BeforeEach
        void setUp() {
            zdtNow = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);

            // Adjustment to handle months having different lengths (so tests pass at end of month)
            while (zdtNow.getDayOfMonth() > 28) {
                zdtNow = zdtNow.minusDays(1);
            }

            aMonthAgo = zdtNow.minusMonths(1).toInstant();
        }

        @Test
        void shouldAddMonths() {
            assertThat(KiwiDates.dateFromInstantPlusMonths(aMonthAgo, 2))
                    .isEqualTo(Date.from(zdtNow.plusMonths(1).toInstant()));
        }

        @Test
        void shouldSubtractMonths() {
            assertThat(KiwiDates.dateFromInstantMinusMonths(aMonthAgo, 2))
                    .isEqualTo(Date.from(zdtNow.minusMonths(3).toInstant()));
        }
    }

    /**
     * @implNote Because {@link Instant} does not deal with {@link TemporalUnit} values greater than
     * {@link ChronoUnit#DAYS}, these tests use {@link ZonedDateTime} to adjust the time scales.
     */
    @Nested
    class YearsAdjusters {

        private ZonedDateTime zdtNow;
        private Instant aYearAgo;

        @BeforeEach
        void setUp() {
            zdtNow = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);
            aYearAgo = zdtNow.minusYears(1).toInstant();
        }

        @Test
        void shouldAddYears() {
            assertThat(KiwiDates.dateFromInstantPlusYears(aYearAgo, 2))
                    .isEqualTo(Date.from(zdtNow.plusYears(1).toInstant()));
        }

        @Test
        void shouldSubtractYears() {
            assertThat(KiwiDates.dateFromInstantMinusYears(aYearAgo, 2))
                    .isEqualTo(Date.from(zdtNow.minusYears(3).toInstant()));
        }
    }
}
