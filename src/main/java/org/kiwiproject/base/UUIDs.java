package org.kiwiproject.base;

import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public class UUIDs {

    private static final Pattern RFC4122_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");

    public static String randomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValidUUID(String value) {
        return RFC4122_PATTERN.matcher(value).matches();
    }

}
