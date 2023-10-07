package org.kiwiproject.jdbc;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * JDBC utilities.
 */
@UtilityClass
public class KiwiJdbc {

    /**
     * Convert the timestamp column given by {@code columnName} in the {@link ResultSet} to milliseconds
     * from the epoch.
     *
     * @param rs         the ResultSet
     * @param columnName the timestamp column name
     * @return epoch milliseconds
     * @throws SQLException             if there is a database problem
     * @throws IllegalArgumentException if the timestamp returned from the ResultSet is null
     * @see Instant#toEpochMilli()
     */
    public static long epochMillisFromTimestamp(ResultSet rs, String columnName) throws SQLException {
        return epochMillisFromTimestamp(rs.getTimestamp(columnName));
    }

    /**
     * Convert the given {@link Timestamp} to milliseconds from the epoch.
     *
     * @param timestamp the timestamp to convert
     * @return epoch milliseconds
     * @throws IllegalArgumentException if timestamp is null
     * @see Instant#toEpochMilli()
     */
    public static long epochMillisFromTimestamp(Timestamp timestamp) {
        checkArgumentNotNull(timestamp, "timestamp cannot be null");
        return timestamp.toInstant().toEpochMilli();
    }

    /**
     * Returns a {@link Date} from the specified {@link Timestamp} column in the given {@link ResultSet}.
     *
     * @param rs         the ResultSet
     * @param columnName the timestamp column name
     * @return the converted Date or {@code null} if the column was {@code NULL}
     * @throws SQLException if there is a database problem
     */
    public static Date dateFromTimestamp(ResultSet rs, String columnName) throws SQLException {
        return dateFromTimestamp(rs.getTimestamp(columnName));
    }

    /**
     * Returns a {@link Date} from the given {@link Timestamp}.
     *
     * @param timestamp the timestamp to convert
     * @return a {@link Date} or null if the timestamp is null
     */
    public static Date dateFromTimestamp(Timestamp timestamp) {
        return isNull(timestamp) ? null : Date.from(timestamp.toInstant());
    }

    /**
     * Returns a {@link Instant} from the specified {@link Timestamp} column in the given {@link ResultSet}.
     *
     * @param rs         the ResultSet
     * @param columnName the timestamp column name
     * @return the converted Instant or {@code null} if the column was {@code NULL}
     * @throws SQLException if there is a database problem
     */
    public static Instant instantFromTimestamp(ResultSet rs, String columnName) throws SQLException {
        return instantFromTimestamp(rs.getTimestamp(columnName));
    }

    /**
     * Returns an {@link Instant} from the given {@link Timestamp}.
     *
     * @param timestamp the timestamp to convert
     * @return an Instant or {@code null} if the timestamp is {@code null}
     */
    public static Instant instantFromTimestamp(Timestamp timestamp) {
        return isNull(timestamp) ? null : timestamp.toInstant();
    }

    /**
     * Returns a {@link LocalDateTime} from the specified {@link Timestamp} column in the given {@link ResultSet}.
     *
     * @param rs         the ResultSet
     * @param columnName the timestamp column name
     * @return the converted LocalDateTime or {@code null} if the column was {@code NULL}
     * @throws SQLException if there is a database problem
     */
    public static LocalDateTime localDateTimeFromTimestamp(ResultSet rs, String columnName) throws SQLException {
        return localDateTimeFromTimestamp(rs.getTimestamp(columnName));
    }

    /**
     * Returns a {@link LocalDateTime} from the given {@link Timestamp}.
     *
     * @param timestamp the timestamp to convert
     * @return a LocalDateTime or {@code null} if the timestamp is {@code null}
     */
    public static LocalDateTime localDateTimeFromTimestamp(Timestamp timestamp) {
        return isNull(timestamp) ? null : timestamp.toLocalDateTime();
    }

    /**
     * Returns a {@link LocalDate} from the specified {@link java.sql.Date} column in the given {@link ResultSet}.
     *
     * @param rs         the ResultSet
     * @param columnName the date column name
     * @return the converted LocalDate or {@code null} if the column was {@code NULL}
     * @throws SQLException if there is problem getting the date
     */
    @Nullable
    public static LocalDate localDateFromDateOrNull(ResultSet rs, String columnName) throws SQLException {
        return localDateFromDateOrNull(rs.getDate(columnName));
    }

    /**
     * Returns a {@link LocalDateTime} from the given {@link java.sql.Date}.
     *
     * @param date the date to convert
     * @return the converted LocalDate or {@code null} if the date is {@code null}
     */
    @Nullable
    public static LocalDate localDateFromDateOrNull(java.sql.@Nullable Date date) {
        return isNull(date) ? null : date.toLocalDate();
    }

    /**
     * Returns a {@link ZonedDateTime} in UTC from the specified column in the {@link ResultSet}.
     *
     * @param rs         the ResultSet
     * @param columnName the timestamp column name
     * @return a UTC ZonedDateTime or {@code null} if the column was {@code NULL}
     * @throws SQLException if there is a database problem
     */
    public static ZonedDateTime utcZonedDateTimeFromTimestamp(ResultSet rs, String columnName)
            throws SQLException {
        return zonedDateTimeFromTimestamp(rs, columnName, ZoneOffset.UTC);
    }

    /**
     * Returns a {@link ZonedDateTime} in the specified time zone from the specified column in the {@link ResultSet}.
     *
     * @param rs         the ResultSet
     * @param columnName the timestamp column name
     * @param zoneId     the time zone ID
     * @return a ZonedDateTime in the specified zone, or {@code null} if the column was {@code NULL}
     * @throws SQLException if there is a database problem
     */
    public static ZonedDateTime zonedDateTimeFromTimestamp(ResultSet rs, String columnName, ZoneId zoneId)
            throws SQLException {
        return zonedDateTimeFromTimestamp(rs.getTimestamp(columnName), zoneId);
    }

    /**
     * Returns a {@link ZonedDateTime} in UTC from the specified {@link Timestamp}.
     *
     * @param timestamp the timestamp to convert
     * @return a UTC ZonedDateTime or {@code null} if the timestamp is {@code null}
     */
    public static ZonedDateTime utcZonedDateTimeFromTimestamp(Timestamp timestamp) {
        return zonedDateTimeFromTimestamp(timestamp, ZoneOffset.UTC);
    }

    /**
     * Returns a {@link ZonedDateTime} in UTC from the given number of milliseconds since the epoch.
     * <p>
     * <em>Note that this method will <strong>always</strong> have zero as the nano-of-second.</em>
     *
     * @param epochMilli number of milliseconds since the epoch
     * @return a UTC ZonedDateTime
     */
    public static ZonedDateTime utcZonedDateTimeFromEpochMilli(long epochMilli) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
    }

    /**
     * Returns a {@link ZonedDateTime} in the specified time zone from the given {@link Timestamp}.
     *
     * @param timestamp the timestamp to convert
     * @param zoneId    the time zone ID
     * @return a ZonedDateTime in the specified zone, or {@code null} if the timestamp is {@code null}
     */
    public static ZonedDateTime zonedDateTimeFromTimestamp(Timestamp timestamp, ZoneId zoneId) {
        return isNull(timestamp) ? null : ZonedDateTime.ofInstant(timestamp.toInstant(), zoneId);
    }

    /**
     * Returns a {@link Timestamp} from the given {@link Instant}.
     *
     * @param instant the Instant to convert
     * @return an Instant or {@code null} if the instant is {@code null}
     */
    public static Timestamp timestampFromInstant(Instant instant) {
        return isNull(instant) ? null : Timestamp.from(instant);
    }

    /**
     * Returns a {@link Timestamp} from the given {@link ZonedDateTime}.
     *
     * @param zonedDateTime the ZonedDateTime to convert
     * @return a ZonedDateTime or {@code null} if the zonedDateTime is {@code null}
     */
    public static Timestamp timestampFromZonedDateTime(ZonedDateTime zonedDateTime) {
        return isNull(zonedDateTime) ? null : Timestamp.from(zonedDateTime.toInstant());
    }

    /**
     * Simplifies the JDBC silliness whereby getting a long value that is actually {@code null} returns zero, and you
     * have to check if the last value was actually {@code null} using the {@link ResultSet#wasNull()} method.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @return a {@link Long} or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static Long longValueOrNull(ResultSet rs, String columnName) throws SQLException {
        var value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Simplifies the JDBC silliness whereby getting an int value that is actually {@code null} returns zero, and you
     * have to check if the last value was actually {@code null} using the {@link ResultSet#wasNull()} method.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @return an {@link Integer} or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static Integer intValueOrNull(ResultSet rs, String columnName) throws SQLException {
        var value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Simplifies the JDBC silliness whereby getting a double value that is actually {@code null} returns zero, and you
     * have to check if the last value was actually {@code null} using the {@link ResultSet#wasNull()} method.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @return a {@link Long} or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static Double doubleValueOrNull(ResultSet rs, String columnName) throws SQLException {
        var value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Returns an enum constant of the given type from the specified column in the result set.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @param enumType   the enum class
     * @param <T>        the enum type parameter
     * @return an enum constant of type {@code enumType} or {@code null} if the database value was NULL
     * @throws SQLException             if there is a database problem
     * @throws IllegalArgumentException if the value from the ResultSet is an invalid enum constant
     * @see Enum#valueOf(Class, String)
     */
    public static <T extends Enum<T>> T enumValueOrNull(ResultSet rs, String columnName, Class<T> enumType)
            throws SQLException {
        return enumValueOrEmpty(rs, columnName, enumType).orElse(null);
    }

    /**
     * Returns an enum constant of the given type from the specified column in the result set.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @param enumType   the enum class
     * @param <T>        the enum type parameter
     * @return an enum constant of type {@code enumType} or an empty {@link Optional} if the database value was NULL
     * @throws SQLException             if there is a database problem
     * @throws IllegalArgumentException if the value from the ResultSet is an invalid enum constant
     * @see Enum#valueOf(Class, String)
     */
    public static <T extends Enum<T>> Optional<T> enumValueOrEmpty(ResultSet rs, String columnName, Class<T> enumType)
            throws SQLException {
        var enumName = rs.getString(columnName);
        return isNull(enumName) ? Optional.empty() : Optional.of(Enum.valueOf(enumType, enumName));
    }

    /**
     * Sets the {@link Timestamp} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Uses {@link Types#TIMESTAMP} as the SQL type.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static void nullSafeSetTimestamp(PreparedStatement ps, int parameterIndex, Timestamp value)
            throws SQLException {
        nullSafeSetTimestamp(ps, parameterIndex, value, Types.TIMESTAMP);
    }

    /**
     * Sets the {@link Timestamp} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Specify the exact SQL timestamp type using one of the values in {@link Types}.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @param type           the SQL type to use when setting a {@code null} value
     * @throws SQLException if there is a database problem
     * @see Types
     */
    public static void nullSafeSetTimestamp(PreparedStatement ps, int parameterIndex, Timestamp value, int type)
            throws SQLException {
        if (isNull(value)) {
            ps.setNull(parameterIndex, type);
        } else {
            ps.setTimestamp(parameterIndex, value);
        }
    }

    /**
     * Sets the {@link Date} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Uses {@link Types#TIMESTAMP} as the SQL type.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static void nullSafeSetDateAsTimestamp(PreparedStatement ps, int parameterIndex, Date value)
            throws SQLException {
        nullSafeSetDateAsTimestamp(ps, parameterIndex, value, Types.TIMESTAMP);
    }

    /**
     * Sets the {@link Date} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Specify the exact SQL timestamp type using one of the values in {@link Types}.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @param type           the SQL type to use when setting a {@code null} value
     * @throws SQLException if there is a database problem
     * @see Types
     */
    public static void nullSafeSetDateAsTimestamp(PreparedStatement ps, int parameterIndex, Date value, int type)
            throws SQLException {
        if (isNull(value)) {
            ps.setNull(parameterIndex, type);
        } else {
            ps.setTimestamp(parameterIndex, new Timestamp(value.getTime()));
        }
    }

    /**
     * Sets the {@link Integer} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Uses {@link Types#INTEGER} as the SQL type.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static void nullSafeSetInt(PreparedStatement ps, int parameterIndex, Integer value) throws SQLException {
        nullSafeSetInt(ps, parameterIndex, value, Types.INTEGER);
    }

    /**
     * Sets the {@link Integer} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Specify the exact SQL int type using one of the values in {@link Types}.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @param type           the SQL type to use when setting a {@code null} value
     * @throws SQLException if there is a database problem
     * @see Types
     */
    public static void nullSafeSetInt(PreparedStatement ps, int parameterIndex, Integer value, int type)
            throws SQLException {
        if (isNull(value)) {
            ps.setNull(parameterIndex, type);
        } else {
            ps.setInt(parameterIndex, value);
        }
    }

    /**
     * Sets the {@link Long} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Uses {@link Types#BIGINT} as the SQL type.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static void nullSafeSetLong(PreparedStatement ps, int parameterIndex, Long value) throws SQLException {
        nullSafeSetLong(ps, parameterIndex, value, Types.BIGINT);
    }

    /**
     * Sets the {@link Long} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Specify the exact SQL long type using one of the values in {@link Types}.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @param type           the SQL type to use when setting a {@code null} value
     * @throws SQLException if there is a database problem
     * @see Types
     */
    public static void nullSafeSetLong(PreparedStatement ps, int parameterIndex, Long value, int type)
            throws SQLException {
        if (isNull(value)) {
            ps.setNull(parameterIndex, type);
        } else {
            ps.setLong(parameterIndex, value);
        }
    }

    /**
     * Sets the {@link Double} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Uses {@link Types#DOUBLE} as the SQL type.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static void nullSafeSetDouble(PreparedStatement ps, int parameterIndex, Double value) throws SQLException {
        nullSafeSetDouble(ps, parameterIndex, value, Types.DOUBLE);
    }

    /**
     * Sets the {@link Double} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Specify the exact SQL double type using one of the values in {@link Types}.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @param type           the SQL type to use when setting a {@code null} value
     * @throws SQLException if there is a database problem
     * @see Types
     */
    public static void nullSafeSetDouble(PreparedStatement ps, int parameterIndex, Double value, int type)
            throws SQLException {
        if (isNull(value)) {
            ps.setNull(parameterIndex, type);
        } else {
            ps.setDouble(parameterIndex, value);
        }
    }

    /**
     * Sets the {@link String} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Uses {@link Types#VARCHAR} as the SQL type.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @throws SQLException if there is a database problem
     */
    public static void nullSafeSetString(PreparedStatement ps, int parameterIndex, String value) throws SQLException {
        nullSafeSetString(ps, parameterIndex, value, Types.VARCHAR);
    }

    /**
     * Sets the {@link String} value in a null-safe manner by using the {@link PreparedStatement#setNull(int, int)}
     * method for {@code null} values. Specify the exact SQL string type using one of the values in {@link Types}.
     *
     * @param ps             the PreparedStatement to set the value on
     * @param parameterIndex the positional index of the value in the SQL statement
     * @param value          the value to set, or {@code null}
     * @param type           the SQL type as an int, see {@link java.sql.SQLType}
     * @throws SQLException if there is a database problem
     * @see java.sql.SQLType
     * @see java.sql.JDBCType
     */
    public static void nullSafeSetString(PreparedStatement ps, int parameterIndex, String value, int type)
            throws SQLException {
        if (isNull(value)) {
            ps.setNull(parameterIndex, type);
        } else {
            ps.setString(parameterIndex, value);
        }
    }
}
