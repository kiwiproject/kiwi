package org.kiwiproject.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Holder for multiple {@link FieldRange} annotations.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface FieldRanges {

    FieldRange[] value();
}
