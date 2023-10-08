package org.kiwiproject.util;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @BlankStringSource} is an {@link ArgumentsSource} which provides blank strings.
 * <p>
 * Usage:
 * <pre>
 * {@literal @}ParameterizedTest
 * {@literal @}BlankStringSource
 *  void testThatEachProvidedArgumentIsBlank(String blankString) 1
 *      assertThat(blankString).isBlank();
 *      // or whatever else you need to test where you need a blank String...
 *  }
 * </pre>
 *
 * <p>
 *  <strong>NOTE:</strong> This is a duplicate of the kiwi-test version. It is included here to prevent a circular dependency.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(BlankStringArgumentsProvider.class)
public @interface BlankStringSource {
}
