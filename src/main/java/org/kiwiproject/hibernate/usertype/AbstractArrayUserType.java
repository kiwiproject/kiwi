package org.kiwiproject.hibernate.usertype;

import static java.util.Objects.isNull;
import static org.kiwiproject.collect.KiwiArrays.isNullOrEmpty;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Abstract base class for custom Hibernate user-defined array types.
 *
 * @implNote Suppress Sonar "'throws' declarations should not be superfluous" warning since the signatures
 * come directly from UserType and we are just preserving them.
 */
@SuppressWarnings("java:S1130")
public abstract class AbstractArrayUserType implements UserType {

    private static final int[] SQL_TYPES = {Types.ARRAY};

    /**
     * Implementors should return the specific database type name that the array contains, e.g. {@code TEXT} if the
     * database array type is {@code TEXT[]}.
     *
     * @return the database type name
     */
    public abstract String databaseTypeName();

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public boolean equals(Object ol, Object o2) throws HibernateException {
        return Objects.equals(ol, o2);
    }

    @Override
    public int hashCode(Object obj) throws HibernateException {
        return isNull(obj) ? 0 : obj.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet,
                              String[] names,
                              SharedSessionContractImplementor session,
                              Object owner)
            throws HibernateException, SQLException {

        if (isNullOrEmpty(names) || isNull(resultSet) || isNull(resultSet.getArray(names[0]))) {
            return null;
        }

        return resultSet.getArray(names[0]).getArray();
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
            var sqlArray = session.connection().createArrayOf(databaseTypeName(), castObject);
            statement.setArray(index, sqlArray);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return isNull(value) ? null : ((Object[]) value).clone();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
