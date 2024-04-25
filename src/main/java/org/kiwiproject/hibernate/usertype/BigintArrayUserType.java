package org.kiwiproject.hibernate.usertype;

import org.kiwiproject.base.KiwiDeprecated;
import org.kiwiproject.base.KiwiDeprecated.Severity;

/**
 * A Hibernate user-defined type that maps to/from (Postgres) array column of type {@code BIGINT} mapping to the
 * Java type {@code Long[]}.
 */
@Deprecated(since = "3.4.0", forRemoval = true)
@KiwiDeprecated(removeAt = "4.0.0",
                replacedBy = "Native array support in Hibernate",
                usageSeverity = Severity.SEVERE,
                reference = "https://github.com/kiwiproject/kiwi/issues/1117")
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
