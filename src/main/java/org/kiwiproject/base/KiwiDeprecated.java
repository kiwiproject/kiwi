package org.kiwiproject.base;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Kiwi-flavored version of {@link Deprecated} that includes the {@code since} attribute added in JDK 9 plus
 * additional attributes we think are useful.
 * <p>
 * At the very least, users should populate the {@code since} and {@code removeAt} attributes.
 *
 * @implNote JDK 9 added a {@code forRemoval} attribute, but since this is intended for things we plan to remove, and
 * because we have the {@code removeAt} attribute, we omitted {@code forRemoval} as redundant.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE})
public @interface KiwiDeprecated {

    /**
     * The version the annotated element became deprecated.
     *
     * @return the version when the annotated was deprecated
     */
    String since() default "";

    /**
     * The upcoming version at which the annotated element will be removed.
     *
     * @return the upcoming version when the annotated element will be removed
     */
    String removeAt() default "";

    /**
     * A description of what feature(s) replaces the one being deprecated, if any.
     * <p>
     * For example, a text description of a replacement REST endpoint such as: {@code GET /foo/bar/{id}} or the
     * name of one or more replacement methods.
     *
     * @return a description of the replacement, if any
     */
    String[] replacedBy() default "";

    /**
     * The issue number or another reference or descriptor which caused or is related to the deprecation, if any.
     * <p>
     * For example, one or more JIRA issue numbers.
     *
     * @return a reference to an issue, ticket, or another descriptor
     */
    String[] reference() default "";

    enum Severity {
        WARN, SEVERE
    }

    /**
     * Indication of potential for problems if the deprecated feature continues to be used by callers, clients, etc.
     *
     * @return the severity of continued usage
     */
    Severity usageSeverity() default Severity.WARN;
}
