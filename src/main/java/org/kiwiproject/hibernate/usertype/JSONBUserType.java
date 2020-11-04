package org.kiwiproject.hibernate.usertype;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.format;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * A Hibernate user-defined type that maps to/from Postgres {@code jsonb} columns.
 */
@SuppressWarnings("java:S1130")
public class JSONBUserType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    // Suppress IntelliJ and Sonar "Raw types should not be used"; the UserType interface defines it as the raw type
    @SuppressWarnings({"java:S3740", "rawtypes"})
    @Override
    public Class returnedClass() {
        return Object.class;
    }

    @Override
    public boolean equals(Object ol, Object o2) throws HibernateException {
        return Objects.equals(ol, o2);
    }

    @Override
    public int hashCode(Object obj) throws HibernateException {
        checkArgumentNotNull(obj, "cannot compute hashCode on null object");
        return obj.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {

        var columnName = names[0]; // not a column-column type, so get first (and only) one
        return rs.getString(columnName);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {

        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        var deepCopy = deepCopy(value);
        if (deepCopy instanceof Serializable) {
            return (Serializable) deepCopy;
        }
        throw new SerializationException(format("deepCopy of %s is not serializable", value), null);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
