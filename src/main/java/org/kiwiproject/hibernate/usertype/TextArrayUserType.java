package org.kiwiproject.hibernate.usertype;

/**
 * A Hibernate user-defined type that maps to/from (Postgres) array column of a text type, e.g. {@code TEXT[]}
 * or {@code VARCHAR[]}, mapping to the Java type {@code String[]}.
 */
public class TextArrayUserType extends AbstractArrayUserType {

    /**
     * @return always returns "text" even though the actual column type might be different, e.g. varchar
     */
    @Override
    public String databaseTypeName() {
        return "text";
    }

    /**
     * @return a class of type String array
     */
    // Suppress IntelliJ and Sonar "Raw types should not be used"; the UserType interface defines it as the raw type
    @SuppressWarnings({"java:S3740", "rawtypes"})
    @Override
    public Class returnedClass() {
        return String[].class;
    }
}
