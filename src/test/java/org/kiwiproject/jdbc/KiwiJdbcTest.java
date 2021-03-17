package org.kiwiproject.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

@DisplayName("KiwiJdbc")
class KiwiJdbcTest {

    @Nested
    class EpochMillisFromTimestamp {

        @Test
        void shouldConvertFromTimestamp() {
            var now = System.currentTimeMillis();
            var timestamp = new Timestamp(now);

            assertThat(KiwiJdbc.epochMillisFromTimestamp(timestamp)).isEqualTo(now);
        }

        @Test
        void shouldThrowException_WhenGivenNullTimestamp() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc.epochMillisFromTimestamp(null));
        }

        @Test
        void shouldConvertFromResultSet() throws SQLException {
            var originalDateTime = LocalDateTime.of(2016, 1, 14, 16, 52, 12);
            var timestamp = Timestamp.valueOf(originalDateTime);

            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("created_at")).thenReturn(timestamp);

            assertThat(KiwiJdbc.epochMillisFromTimestamp(resultSet, "created_at"))
                    .isEqualTo(timestamp.getTime());

            verify(resultSet).getTimestamp("created_at");
        }

        @Test
        void shouldThrowException_WhenResultSet_ReturnsNullTimestamp() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("updated_at")).thenReturn(null);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc.epochMillisFromTimestamp(resultSet, "updated_at"))
                    .withMessage("timestamp cannot be null");

            verify(resultSet).getTimestamp("updated_at");
        }
    }

    @Nested
    class DateFromTimestamp {

        @Test
        void shouldConvertFromTimestamp() {
            var originalDate = Date.from(LocalDateTime.of(2016, 1, 14, 16, 52, 12).toInstant(ZoneOffset.UTC));
            var timestamp = new Timestamp(originalDate.getTime());

            assertThat(KiwiJdbc.dateFromTimestamp(timestamp)).isEqualTo(originalDate);
        }

        @Test
        void shouldReturnNull_WhenGivenNullTimestamp() {
            assertThat(KiwiJdbc.dateFromTimestamp(null)).isNull();
        }

        @Test
        void shouldConvertFromResultSet() throws SQLException {
            var originalDate = Date.from(LocalDateTime.of(2016, 1, 14, 16, 52, 12).toInstant(ZoneOffset.UTC));
            var timestamp = new Timestamp(originalDate.getTime());

            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("created_at")).thenReturn(timestamp);

            assertThat(KiwiJdbc.dateFromTimestamp(resultSet, "created_at"))
                    .isEqualTo(originalDate);

            verify(resultSet).getTimestamp("created_at");
        }

        @Test
        void shouldReturnNull_WhenResultSet_ReturnsNullTimestamp() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("updated_at")).thenReturn(null);

            assertThat(KiwiJdbc.dateFromTimestamp(resultSet, "updated_at")).isNull();

            verify(resultSet).getTimestamp("updated_at");
        }
    }

    @Nested
    class InstantFromTimestamp {

        @Test
        void shouldConvertFromTimestamp() {
            var originalInstant = Instant.from(LocalDateTime.of(2019, 3, 15, 17, 43, 20).toInstant(ZoneOffset.UTC));
            var timestamp = Timestamp.from(originalInstant);

            assertThat(KiwiJdbc.instantFromTimestamp(timestamp)).isEqualTo(originalInstant);
        }

        @Test
        void shouldReturnNull_WhenGivenNullTimestamp() {
            assertThat(KiwiJdbc.instantFromTimestamp(null)).isNull();
        }

        @Test
        void shouldConvertFromResultSet() throws SQLException {
            var originalInstant = Instant.from(LocalDateTime.of(2019, 3, 15, 17, 43, 20).toInstant(ZoneOffset.UTC));
            var timestamp = Timestamp.from(originalInstant);

            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("received_at")).thenReturn(timestamp);

            assertThat(KiwiJdbc.instantFromTimestamp(resultSet, "received_at")).isEqualTo(originalInstant);

            verify(resultSet).getTimestamp("received_at");
        }

        @Test
        void shouldReturnNull_WhenResultSet_ReturnsNullTimestamp() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("received_at")).thenReturn(null);

            assertThat(KiwiJdbc.instantFromTimestamp(resultSet, "received_at")).isNull();

            verify(resultSet).getTimestamp("received_at");
        }
    }

    @Nested
    class LocalDateTimeFromTimestamp {

        @Test
        void shouldConvertFromTimestamp() {
            var originalDateTime = LocalDateTime.of(2019, 3, 15, 17, 43, 20);
            var timestamp = Timestamp.valueOf(originalDateTime);

            assertThat(KiwiJdbc.localDateTimeFromTimestamp(timestamp)).isEqualTo(originalDateTime);
        }

        @Test
        void shouldReturnNull_WhenGivenNullTimestamp() {
            assertThat(KiwiJdbc.localDateTimeFromTimestamp(null)).isNull();
        }

        @Test
        void shouldConvertFromResultSet() throws SQLException {
            var originalDateTime = LocalDateTime.of(2019, 3, 15, 17, 43, 20);
            var timestamp = Timestamp.valueOf(originalDateTime);

            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("received_at")).thenReturn(timestamp);

            assertThat(KiwiJdbc.localDateTimeFromTimestamp(resultSet, "received_at"))
                    .isEqualTo(originalDateTime);

            verify(resultSet).getTimestamp("received_at");
        }

        @Test
        void shouldReturnNull_WhenResultSet_ReturnsNullTimestamp() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("received_at")).thenReturn(null);

            assertThat(KiwiJdbc.localDateTimeFromTimestamp(resultSet, "received_at")).isNull();

            verify(resultSet).getTimestamp("received_at");
        }
    }

    @Nested
    class UtcZonedDateTimeFromTimestamp {

        @Test
        void shouldConvertFromTimestamp() {
            var originalDateTime = ZonedDateTime.of(2015, 12, 1, 9, 30, 20, 100, ZoneOffset.UTC);
            var timestamp = Timestamp.from(originalDateTime.toInstant());

            assertThat(KiwiJdbc.utcZonedDateTimeFromTimestamp(timestamp)).isEqualTo(originalDateTime);
        }

        @Test
        void shouldReturnNull_WhenGivenNullTimestamp() {
            assertThat(KiwiJdbc.utcZonedDateTimeFromTimestamp(null)).isNull();
        }

        @Test
        void shouldConvertFromResultSet() throws SQLException {
            var originalDateTime = ZonedDateTime.of(2015, 12, 1, 9, 30, 20, 100, ZoneOffset.UTC);
            var timestamp = Timestamp.from(originalDateTime.toInstant());

            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("sent_at")).thenReturn(timestamp);

            assertThat(KiwiJdbc.utcZonedDateTimeFromTimestamp(resultSet, "sent_at"))
                    .isEqualTo(originalDateTime);

            verify(resultSet).getTimestamp("sent_at");
        }

        @Test
        void shouldReturnNull_WhenResultSet_ReturnsNullTimestamp() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("received_at")).thenReturn(null);

            assertThat(KiwiJdbc.utcZonedDateTimeFromTimestamp(resultSet, "received_at")).isNull();

            verify(resultSet).getTimestamp("received_at");
        }
    }

    @Nested
    class UtcZonedDateTimeFromEpochMilli {

        @Test
        void shouldConvertFromEpochMilli() {
            int nanoOfSecond = 100;
            var originalDateTime = ZonedDateTime.of(2018, 12, 26, 9, 45, 12, nanoOfSecond, ZoneId.of("US/Eastern"));
            var epochMilli = originalDateTime.toInstant().toEpochMilli();

            var dateTime = KiwiJdbc.utcZonedDateTimeFromEpochMilli(epochMilli);

            assertThat(dateTime).isEqualTo(originalDateTime.minusNanos(nanoOfSecond));
        }
    }

    @Nested
    class ZonedDateTimeFromTimestamp {

        @Test
        void shouldConvertFromTimestamp() {
            var zoneId = ZoneId.of("-05:00");
            var originalDateTime = ZonedDateTime.of(2015, 12, 1, 9, 30, 20, 100, zoneId);
            var timestamp = Timestamp.from(originalDateTime.toInstant());

            assertThat(KiwiJdbc.zonedDateTimeFromTimestamp(timestamp, zoneId)).isEqualTo(originalDateTime);
        }

        @Test
        void shouldReturnNull_WhenGivenNullTimestamp() {
            assertThat(KiwiJdbc.zonedDateTimeFromTimestamp(null, ZoneId.of("-05:00"))).isNull();
        }

        @Test
        void shouldConvertFromResultSet() throws SQLException {
            var zoneId = ZoneId.of("-05:00");
            var originalDateTime = ZonedDateTime.of(2015, 12, 1, 9, 30, 20, 100, zoneId);
            var timestamp = Timestamp.from(originalDateTime.toInstant());

            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("sent_at")).thenReturn(timestamp);

            assertThat(KiwiJdbc.zonedDateTimeFromTimestamp(resultSet, "sent_at", zoneId))
                    .isEqualTo(originalDateTime);

            verify(resultSet).getTimestamp("sent_at");
        }

        @Test
        void shouldReturnNull_WhenResultSet_ReturnsNullTimestamp() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getTimestamp("received_at")).thenReturn(null);

            assertThat(KiwiJdbc.zonedDateTimeFromTimestamp(resultSet, "received_at", ZoneId.of("-05:00"))).isNull();

            verify(resultSet).getTimestamp("received_at");
        }
    }

    @Nested
    class TimestampFromInstant {

        @Test
        void shouldConvertFromInstant() {
            var originalInstant = Instant.parse("2020-02-24T10:19:11.000000100Z");
            var timestamp = KiwiJdbc.timestampFromInstant(originalInstant);

            assertThat(timestamp).isNotNull();
            assertThat(timestamp.toInstant()).isEqualTo(originalInstant);
        }

        @Test
        void shouldReturnNull_WhenGivenNullInstant() {
            assertThat(KiwiJdbc.timestampFromInstant(null)).isNull();
        }
    }

    @Nested
    class TimestampFromZoneDateTime {

        @Test
        void shouldConvertFromZonedDateTime() {
            var zoneId = ZoneId.of("+01:00");
            var originalDateTime = ZonedDateTime.of(2017, 4, 6, 10, 23, 11, 150, zoneId);
            var timestamp = KiwiJdbc.timestampFromZonedDateTime(originalDateTime);

            assertThat(timestamp).isNotNull();
            assertThat(timestamp.toInstant().atZone(zoneId)).isEqualTo(originalDateTime);
        }

        @Test
        void shouldReturnNUll_WhenGivenNullZonedDateTime() {
            assertThat(KiwiJdbc.timestampFromZonedDateTime(null)).isNull();
        }
    }

    @Nested
    class LongValueOrNull {

        @Test
        void shouldReturnLong() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getLong("some_number")).thenReturn(42L);
            when(resultSet.wasNull()).thenReturn(false);

            assertThat(KiwiJdbc.longValueOrNull(resultSet, "some_number")).isEqualTo(42L);
            verify(resultSet).getLong("some_number");
        }

        @Test
        void shouldReturnNull_WhenWasNullSQLValue() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getLong("some_number")).thenReturn(0L);
            when(resultSet.wasNull()).thenReturn(true);

            assertThat(KiwiJdbc.longValueOrNull(resultSet, "some_number")).isNull();
            verify(resultSet).getLong("some_number");
        }
    }

    @Nested
    class IntValueOrNull {

        @Test
        void shouldReturnInteger() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getInt("some_number")).thenReturn(84);
            when(resultSet.wasNull()).thenReturn(false);

            assertThat(KiwiJdbc.intValueOrNull(resultSet, "some_number")).isEqualTo(84);

            verify(resultSet).getInt("some_number");
        }

        @Test
        void shouldReturnNull_WhenWasNullSQLValue() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getInt("some_number")).thenReturn(0);
            when(resultSet.wasNull()).thenReturn(true);

            assertThat(KiwiJdbc.intValueOrNull(resultSet, "some_number")).isNull();

            verify(resultSet).getInt("some_number");
        }
    }

    @Nested
    class DoubleValueOrNull {

        @Test
        void shouldReturnDouble() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getDouble("some_number")).thenReturn(12345.0);
            when(resultSet.wasNull()).thenReturn(false);

            assertThat(KiwiJdbc.doubleValueOrNull(resultSet, "some_number")).isEqualTo(12345.0);

            verify(resultSet).getDouble("some_number");
        }

        @Test
        void shouldReturnNull_WhenWasNullSQLValue() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getDouble("some_number")).thenReturn(0.0);
            when(resultSet.wasNull()).thenReturn(true);

            assertThat(KiwiJdbc.doubleValueOrNull(resultSet, "some_number")).isNull();

            verify(resultSet).getDouble("some_number");
        }
    }

    @Nested
    class EnumValueOrNull {

        @ParameterizedTest
        @EnumSource(Season.class)
        void shouldReturnEnumConstant(Season season) throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getString("season")).thenReturn(season.name());

            assertThat(KiwiJdbc.enumValueOrNull(resultSet, "season", Season.class)).isEqualTo(season);
        }

        @Test
        void shouldReturnNull_WhenWasNullSQLValue() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getString("season")).thenReturn(null);

            assertThat(KiwiJdbc.enumValueOrNull(resultSet, "season", Season.class)).isNull();

            verify(resultSet).getString("season");
            verifyNoMoreInteractions(resultSet);
        }

        @Test
        void shouldThrowIllegalArgument_WhenResultSetValue_IsInvalidEnumConstantName() throws SQLException {
            var resultSet = newMockResultSet();
            var invalidName = "winter";
            when(resultSet.getString("season")).thenReturn(invalidName);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc.enumValueOrNull(resultSet, "season", Season.class))
                    .withMessageContaining(invalidName);
        }
    }

    @Nested
    class EnumValueOrEmpty {

        @ParameterizedTest
        @EnumSource(Season.class)
        void shouldReturnOptionalContainingEnumConstant(Season season) throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getString("season")).thenReturn(season.name());

            assertThat(KiwiJdbc.enumValueOrEmpty(resultSet, "season", Season.class)).contains(season);
        }

        @Test
        void shouldReturnEmptyOptional_WhenWasNullSQLValue() throws SQLException {
            var resultSet = newMockResultSet();
            when(resultSet.getString("season")).thenReturn(null);

            assertThat(KiwiJdbc.enumValueOrEmpty(resultSet, "season", Season.class)).isEmpty();

            verify(resultSet).getString("season");
            verifyNoMoreInteractions(resultSet);
        }

        @Test
        void shouldThrowIllegalArgument_WhenResultSetValue_IsInvalidEnumConstantName() throws SQLException {
            var resultSet = newMockResultSet();
            var invalidName = "winter";
            when(resultSet.getString("season")).thenReturn(invalidName);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc.enumValueOrEmpty(resultSet, "season", Season.class))
                    .withMessageContaining(invalidName);
        }
    }

    enum Season {
        WINTER, SPRING, SUMMER, FALL
    }

    @Nested
    class NullSafeSetInt {

        @Test
        void shouldSetIntegerValue() throws SQLException {
            var parameterIndex = 2;
            var value = 12;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetInt(statement, parameterIndex, value);

            verify(statement).setInt(parameterIndex, value);
        }

        @Test
        void shouldSetNull_WhenIntegerValueIsNull() throws SQLException {
            var parameterIndex = 1;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetInt(statement, parameterIndex, null);

            verify(statement).setNull(parameterIndex, Types.INTEGER);
        }
    }

    @Nested
    class NullSafeSetLong {

        @Test
        void shouldSetLongValue() throws SQLException {
            var parameterIndex = 2;
            var value = 36L;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetLong(statement, parameterIndex, value);

            verify(statement).setLong(parameterIndex, value);
        }

        @Test
        void shouldSetNull_WhenLongValueIsNull() throws SQLException {
            var parameterIndex = 1;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetLong(statement, parameterIndex, null);

            verify(statement).setNull(parameterIndex, Types.BIGINT);
        }
    }

    @Nested
    class NullSafeSetDouble {

        @Test
        void shouldSetDoubleValue() throws SQLException {
            var parameterIndex = 2;
            var value = 42.5;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetDouble(statement, parameterIndex, value);

            // In this case it should be safe to verify the exact double value, since no calculation took place
            verify(statement).setDouble(parameterIndex, value);
        }

        @Test
        void shouldSetNull_WhenDoubleValueIsNull() throws SQLException {
            var parameterIndex = 1;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetDouble(statement, parameterIndex, null);

            verify(statement).setNull(parameterIndex, Types.DOUBLE);
        }
    }

    @Nested
    class NullSafeSetTimestamp {

        @Test
        void shouldSetTimestampValue() throws SQLException {
            var parameterIndex = 2;
            var value = Timestamp.from(Instant.now());
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetTimestamp(statement, parameterIndex, value);

            verify(statement).setTimestamp(parameterIndex, value);
        }

        @Test
        void shouldSetNull_WhenTimestampValueIsNull() throws SQLException {
            var parameterIndex = 1;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetTimestamp(statement, parameterIndex, null);

            verify(statement).setNull(parameterIndex, Types.TIMESTAMP);
        }
    }

    @Nested
    class NullSafeSetDateAsTimestamp {

        @Test
        void shouldSetTimestampValue() throws SQLException {
            var parameterIndex = 2;
            var value = new Date();
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetDateAsTimestamp(statement, parameterIndex, value);

            verify(statement).setTimestamp(parameterIndex, Timestamp.from(value.toInstant()));
        }

        @Test
        void shouldSetNull_WhenTimestampValueIsNull() throws SQLException {
            var parameterIndex = 1;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetDateAsTimestamp(statement, parameterIndex, null);

            verify(statement).setNull(parameterIndex, Types.TIMESTAMP);
        }
    }

    @Nested
    class NullSafeSetString {

        @Test
        void shouldSetStringValue() throws SQLException {
            var parameterIndex = 8;
            var value = "theValue";
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetString(statement, parameterIndex, value);

            verify(statement).setString(parameterIndex, value);
        }

        @Test
        void shouldSetNull_WhenStringValueIsNull() throws SQLException {
            var parameterIndex = 4;
            var statement = newMockPreparedStatement();

            KiwiJdbc.nullSafeSetString(statement, parameterIndex, null);

            verify(statement).setNull(parameterIndex, Types.VARCHAR);
        }
    }

    private static ResultSet newMockResultSet() {
        return mock(ResultSet.class);
    }

    private PreparedStatement newMockPreparedStatement() {
        return mock(PreparedStatement.class);
    }
}