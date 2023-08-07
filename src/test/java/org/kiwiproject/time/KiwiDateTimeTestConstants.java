package org.kiwiproject.time;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Used in several date/time tests.
 */
@UtilityClass
class KiwiDateTimeTestConstants {

    static final ZoneId US_EASTERN_TIME = ZoneId.of("US/Eastern");
    static final ZoneId TOKYO_TIME = ZoneId.of("Asia/Tokyo");

    // Remember, remember the 5th of November...now go watch "V for Vendetta"
    // November 5, 2005, 8PM GMT (400th anniversary of Guy Fawkes Day)

    static final Date GUY_FAWKES_AS_DATE =
            Date.from(Instant.ofEpochSecond(1131220800L));

    static final Date GUY_FAWKES_AS_DATE_AT_MIDNIGHT =
            Date.from(Instant.ofEpochSecond(1131148800L));

    static final LocalDate GUY_FAWKES_AS_LOCAL_DATE =
            LocalDate.of(2005, 11, 5);

    static final LocalDate GUY_FAWKES_AS_LOCAL_DATE_IN_TOKYO =
            LocalDate.of(2005, 11, 6);

    static final LocalDateTime GUY_FAWKES_AS_LOCAL_DATE_TIME_AT_UTC =
            LocalDateTime.of(2005, 11, 5, 20, 0, 0, 0);

    static final LocalDateTime GUY_FAWKES_AS_LOCAL_DATE_TIME_ON_USA_EAST_COAST =
            LocalDateTime.of(2005, 11, 5, 15, 0, 0, 0);

    static final LocalDateTime GUY_FAWKES_AS_LOCAL_DATE_TIME_IN_TOKYO =
            LocalDateTime.of(2005, 11, 6, 5, 0, 0, 0);

    static final ZonedDateTime GUY_FAWKES_AS_ZONED_DATE_TIME_AT_UTC =
            ZonedDateTime.of(2005, 11, 5, 20, 0, 0, 0, ZoneOffset.UTC);

    static final ZonedDateTime GUY_FAWKES_AS_ZONED_DATE_TIME_ON_USA_EAST_COAST =
            ZonedDateTime.of(2005, 11, 5, 15, 0, 0, 0, US_EASTERN_TIME);

    static final ZonedDateTime GUY_FAWKES_AS_ZONED_DATE_TIME_IN_TOKYO =
            ZonedDateTime.of(2005, 11, 6, 5, 0, 0, 0, TOKYO_TIME);

}
