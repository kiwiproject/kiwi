package org.kiwiproject.base;

import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for working with {@link UUID} instances.
 */
@UtilityClass
public class UUIDs {

    private static final String NIL_UUID = "00000000-0000-0000-0000-000000000000";

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-9a-fA-F][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    /**
     * Creates a new version 4 (pseudo randomly generated) UUID and then returns it as a string.
     *
     * @return a new random UUID as a String
     * @see UUID#randomUUID()
     */
    public static String randomUUIDString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if the {@code value} is a valid UUID conforming to <a href="https://www.rfc-editor.org/rfc/rfc9562">RFC 9562</a>.
     * The general form is 8-4-4-4-12 where all the digits are hexadecimal.
     * Example: {@code e94c302e-e684-4d72-9060-a66461f858d6}
     * <p>
     * Note specifically that this method <em>does not support</em> the
     * <a href="https://www.rfc-editor.org/rfc/rfc9562#section-5.9">Nil UUID</a>
     * and will always return false if given a Nil UUID.
     *
     * @param value the string to check
     * @return {@code true} if a valid UUID, {@code false} otherwise
     * @see UUID
     */
    public static boolean isValidUUID(String value) {
        return UUID_PATTERN.matcher(value).matches();
    }

    /**
     * Checks if the {@code value} is a valid UUID conforming to
     * <a href="https://www.rfc-editor.org/rfc/rfc9562">RFC 9562</a> (including the Nil UUID). The general form is
     * 8-4-4-4-12 where all the digits are hexadecimal. Example: {@code e94c302e-e684-4d72-9060-a66461f858d6}, or the
     * nil UUID {@code 00000000-0000-0000-0000-000000000000}.
     *
     * @param value the string to check
     * @return {@code true} if a valid UUID, {@code false} otherwise
     * @see UUID
     */
    public static boolean isValidUUIDAllowingNil(String value) {
        return NIL_UUID.equals(value) || isValidUUID(value);
    }
}
