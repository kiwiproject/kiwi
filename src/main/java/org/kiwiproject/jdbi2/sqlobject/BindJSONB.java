package org.kiwiproject.jdbi2.sqlobject;

import org.kiwiproject.jdbc.postgres.KiwiPostgres;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows binding String values to JSONB columns in Postgres when using JDBI version 2.
 * <p>
 * Usage: annotate a parameter in a JDBI SQLObject data access object with this annotation. Example:
 * <pre>
 *    {@literal @}SqlUpdate("insert into articles (uuid, content) values (:uuid, :content)")
 *     void insertContent(@Bind("uuid") String uuid, @BindJSONB("content") String content)
 * </pre>
 * <p>
 * Note that both jdbi (version 2) and postgres dependencies must be available at runtime.
 */
@BindingAnnotation(BindJSONB.JsonBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface BindJSONB {

    String value();

    class JsonBinderFactory implements BinderFactory<BindJSONB> {
        @SuppressWarnings("rawtypes")  // BinderFactory defines the method with a raw type, so we have to as well
        @Override
        public Binder build(BindJSONB annotation) {
            return (Binder<BindJSONB, String>) (sqlStatement, bind, json) -> {
                var pgObject = KiwiPostgres.newJSONBObject(json);
                sqlStatement.bind(bind.value(), pgObject);
            };
        }
    }
}
