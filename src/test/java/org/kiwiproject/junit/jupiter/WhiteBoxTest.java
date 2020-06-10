package org.kiwiproject.junit.jupiter;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicator that a test is "white box", generally used to indicate a test is calling a non-public API.
 * This is often useful when testing complex internal logic or exceptions thay are difficult or near impossible
 * to simulate.
 * <p>
 * TODO Should we make this a public part of kiwi?
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
public @interface WhiteBoxTest {
}
