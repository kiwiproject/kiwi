package org.kiwiproject.reflect.sub;

import lombok.Value;

/**
 * This is for testing accessibility when not in the same package and class as the test code.
 */
@Value
public class OtherPerson {
    String firstName;
    String lastName;
    Integer age;
}
