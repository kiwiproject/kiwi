package org.kiwiproject.hibernate.usertype;

import static java.util.Objects.isNull;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserType;
import org.kiwiproject.base.KiwiDeprecated;
import org.kiwiproject.base.KiwiDeprecated.Severity;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

/**
 * Abstract base class for custom Hibernate user-defined array types.
 *
 * @implNote Suppress Sonar "'throws' declarations should not be superfluous" warning since the signatures
 * come directly from UserType, and we are just preserving them.
 */
@SuppressWarnings("java:S1130")
@Deprecated(since = "3.4.0", forRemoval = true)
@KiwiDeprecated(removeAt = "4.0.0",
                replacedBy = "Native array support in Hibernate",
                usageSeverity = Severity.SEVERE,
                reference = "https://github.com/kiwiproject/kiwi/issues/1117")
public abstract class AbstractArrayUserType<T> implements UserType<T> {

    private static final int[] SQL_TYPES = {Types.ARRAY};

    /**
     * Implementors should return the specific database type name that the array contains, e.g. {@code TEXT} if the
     * database array type is {@code TEXT[]}.
     *
     * @return the database type name
     */
    public abstract String databaseTypeName();

    @Override
    public int getSqlType() {
        return SqlTypes.ARRAY;
    }

    @Override
    public boolean equals(T ol, T o2) throws HibernateException {
        return Arrays.equals((Object[]) ol, (Object[]) o2);
    }

    @Override
    public int hashCode(T obj) throws HibernateException {
        return isNull(obj) ? 0 : obj.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T nullSafeGet(ResultSet resultSet,
                         int position,
                         SharedSessionContractImplementor session,
                         Object owner)
            throws HibernateException, SQLException {

        if (isNull(resultSet) || isNull(resultSet.getArray(position))) {
            return null;
        }

        return (T) resultSet.getArray(position).getArray();
    }

    @Override
    public void nullSafeSet(PreparedStatement statement,
                            Object value,
                            int index,
                            SharedSessionContractImplementor session)
            throws HibernateException, SQLException {

        if (isNull(value)) {
            statement.setNull(index, SQL_TYPES[0]);
        } else {
            var castObject = (Object[]) value;
            var sqlArray = session.getJdbcConnectionAccess().obtainConnection().createArrayOf(databaseTypeName(), castObject);
            statement.setArray(index, sqlArray);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deepCopy(Object value) throws HibernateException {
        return isNull(value) ? null : (T) value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T assemble(Serializable cached, Object owner) throws HibernateException {
        return (T) cached;
    }

    @Override
    public T replace(T original, T target, Object owner) throws HibernateException {
        return original;
    }
}
