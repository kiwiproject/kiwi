package org.kiwiproject.hibernate.usertype;

/**
 * A Hibernate user-defined type that maps to/from (Postgres) array column of type {@code BIGINT} mapping to the
 * Java type {@code Long[]}.
 */
public class BigintArrayUserType extends AbstractArrayUserType<Long[]> {

    /**
     * @return always returns "bigint"
     */
    @Override
    public String databaseTypeName() {
        return "bigint";
    }

    /**
     * @return a class of type Long array
     */
    @Override
    public Class<Long[]> returnedClass() {
        return Long[].class;
    }
}
