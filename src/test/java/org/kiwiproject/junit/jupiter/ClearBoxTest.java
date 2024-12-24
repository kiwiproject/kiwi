package org.kiwiproject.junit.jupiter;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicator that a test is "clear box", generally used to indicate a test is calling a non-public API.
 * This is often useful when testing complex internal logic or exceptions that are challenging or near impossible
 * to simulate.
 * <p>
 * This is also in kiwi-test as part of its public API. However, because we don't want to create a cycle between
 * kiwi and kiwi-test (or create yet another library that both depend on), it is duplicated here in kiwi.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
public @interface ClearBoxTest {
}
