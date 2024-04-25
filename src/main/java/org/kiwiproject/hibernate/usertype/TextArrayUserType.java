package org.kiwiproject.hibernate.usertype;

import org.kiwiproject.base.KiwiDeprecated;
import org.kiwiproject.base.KiwiDeprecated.Severity;

/**
 * A Hibernate user-defined type that maps to/from (Postgres) array column of a text type, e.g. {@code TEXT[]}
 * or {@code VARCHAR[]}, mapping to the Java type {@code String[]}.
 *
 * @deprecated Replaced by native arrary support in Hibernate
 */
@Deprecated(since = "3.4.0", forRemoval = true)
@KiwiDeprecated(removeAt = "4.0.0",
                replacedBy = "Native array support in Hibernate",
                usageSeverity = Severity.SEVERE,
                reference = "https://github.com/kiwiproject/kiwi/issues/1117")
public class TextArrayUserType extends AbstractArrayUserType<String[]> {

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
    @Override
    public Class<String[]> returnedClass() {
        return String[].class;
    }
}
