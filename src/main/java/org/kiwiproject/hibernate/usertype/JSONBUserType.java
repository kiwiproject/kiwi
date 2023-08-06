package org.kiwiproject.hibernate.usertype;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
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
public class JSONBUserType implements UserType<String> {

    @Override
    public int getSqlType() {
        return SqlTypes.JAVA_OBJECT;
    }

    @Override
    public Class<String> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(String ol, String o2) throws HibernateException {
        return Objects.equals(ol, o2);
    }

    @Override
    public int hashCode(String obj) throws HibernateException {
        checkArgumentNotNull(obj, "cannot compute hashCode on null object");
        return obj.hashCode();
    }

    @Override
    public String nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {

        return rs.getString(position);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, String value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {

        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public String deepCopy(String value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(String value) throws HibernateException {
        return deepCopy(value);
    }

    @Override
    public String assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy((String) cached);
    }

    @Override
    public String replace(String original, String target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
