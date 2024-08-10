package org.kiwiproject.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@DisplayName("KiwiInstants")
class KiwiInstantsTest {

    /**
     * date = 400th anniversary of Shakespeare's death
     * hours = 18 comedies
     * minutes = 10 tragedies
     * seconds = 8 romances
     */
    private static final Instant SHAKESPEARE_ANNIVERSARY = Instant.parse("2016-04-23T18:10:08Z");

    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
    }

    @Nested
    @DisplayName("get[*] using specified Instant")
    class GetMethods {

        @Test
        void shouldReturnSecond() {
            assertThat(KiwiInstants.getSecond(SHAKESPEARE_ANNIVERSARY)).isEqualTo(8);
        }

        @Test
        void shouldReturnMinute() {
            assertThat(KiwiInstants.getMinute(SHAKESPEARE_ANNIVERSARY)).isEqualTo(10);
        }

        @Test
        void shouldReturnHour() {
            assertThat(KiwiInstants.getHour(SHAKESPEARE_ANNIVERSARY)).isEqualTo(18);
        }

        @Test
        void shouldReturnDayOfMonth() {
            assertThat(KiwiInstants.getDayOfMonth(SHAKESPEARE_ANNIVERSARY)).isEqualTo(23);
        }

        @Test
        void shouldReturnMonth() {
            assertThat(KiwiInstants.getMonth(SHAKESPEARE_ANNIVERSARY)).isEqualTo(Month.APRIL);
        }

        @Test
        void shouldReturnMonthValue() {
            assertThat(KiwiInstants.getMonthValue(SHAKESPEARE_ANNIVERSARY)).isEqualTo(4);
        }

        @Test
        void shouldReturnYear() {
            assertThat(KiwiInstants.getYear(SHAKESPEARE_ANNIVERSARY)).isEqualTo(2016);
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
            assertThat(KiwiInstants.plusMinutes(aMinuteAgo, 5))
                    .isEqualTo(now.plus(4, ChronoUnit.MINUTES));
        }

        @Test
        void shouldSubtractMinutes() {
            assertThat(KiwiInstants.minusMinutes(aMinuteAgo, 5))
                    .isEqualTo(now.minus(6, ChronoUnit.MINUTES));
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
            assertThat(KiwiInstants.plusHours(anHourAgo, 2))
                    .isEqualTo(now.plus(1, ChronoUnit.HOURS));
        }

        @Test
        void shouldSubtractHours() {
            assertThat(KiwiInstants.minusHours(anHourAgo, 2))
                    .isEqualTo(now.minus(3, ChronoUnit.HOURS));
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
            assertThat(KiwiInstants.plusDays(aDayAgo, 2))
                    .isEqualTo(now.plus(1, ChronoUnit.DAYS));
        }

        @Test
        void shouldSubtractDays() {
            assertThat(KiwiInstants.minusDays(aDayAgo, 2))
                    .isEqualTo(now.minus(3, ChronoUnit.DAYS));
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

            // Adjustment to handle months having different lengths (so tests pass at the end of month)
            while (zdtNow.getDayOfMonth() > 28) {
                zdtNow = zdtNow.minusDays(1);
            }

            aMonthAgo = zdtNow.minusMonths(1).toInstant();
        }

        @Test
        void shouldAddMonths() {
            assertThat(KiwiInstants.plusMonths(aMonthAgo, 2))
                    .isEqualTo(zdtNow.plusMonths(1).toInstant());
        }

        @Test
        void shouldSubtractMonths() {
            assertThat(KiwiInstants.minusMonths(aMonthAgo, 2))
                    .isEqualTo(zdtNow.minusMonths(3).toInstant());
        }
    }


    /**
     * @implNote Because {@link Instant} does not deal with {@link TemporalUnit} values greater than
     * {@link ChronoUnit#DAYS}, these tests use {@link ZonedDateTime} to adjust the time scales.
     */
    @Nested
    class YearAdjusters {

        private ZonedDateTime zdtNow;
        private Instant aYearAgo;

        @BeforeEach
        void setUp() {
            zdtNow = ZonedDateTime.ofInstant(now, ZoneOffset.UTC);
            aYearAgo = zdtNow.minusYears(1).toInstant();
        }

        @Test
        void shouldAddYears() {
            assertThat(KiwiInstants.plusYears(aYearAgo, 2))
                    .isEqualTo(zdtNow.plusYears(1).toInstant());
        }

        @Test
        void shouldSubtractYears() {
            assertThat(KiwiInstants.minusYears(aYearAgo, 2))
                    .isEqualTo(zdtNow.minusYears(3).toInstant());
        }
    }
}
