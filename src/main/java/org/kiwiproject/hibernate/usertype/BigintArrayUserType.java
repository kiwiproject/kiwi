package org.kiwiproject.hibernate.usertype;

/**
 * A Hibernate user-defined type that maps to/from (Postgres) array column of type {@code BIGINT} mapping to the
 * Java type {@code Long[]}.
 */
public class BigintArrayUserType extends AbstractArrayUserType {

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
    // Suppress IntelliJ and Sonar "Raw types should not be used"; the UserType interface defines it as the raw type
    @SuppressWarnings({"java:S3740", "rawtypes"})
    @Override
    public Class returnedClass() {
        return Long[].class;
    }
}
