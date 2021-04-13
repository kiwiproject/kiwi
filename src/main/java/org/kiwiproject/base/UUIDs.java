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

    private static final Pattern RFC4122_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    /**
     * Creates a new type 4 (pseudo randomly generated) UUID, and then returns it as a string.
     *
     * @return a new random UUID as a String
     * @see UUID#randomUUID()
     */
    public static String randomUUIDString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Checks if the {@code value} is a valid UUID conforming to RFC 4122. The general form is 8-4-4-4-12 where all the
     * digits are hexadecimal. Example: {@code e94c302e-e684-4d72-9060-a66461f858d6}
     * <p>
     * Note specifically that this method <em>does not support</em> the <a href="https://tools.ietf.org/html/rfc4122#section-4.1.7">Nil UUID</a>
     * and will always return false if given a Nil UUID.
     * <p>
     * This method has been tested with UUIDs generated using {@link UUID#randomUUID()} and
     * {@link UUID#nameUUIDFromBytes(byte[])}. It has also been tested with UUIDs created using the constructor
     * {@link UUID#UUID(long, long)} <em>with the caveat that the most and least significant bits came from valid
     * type 3 and 4 UUIDs.</em>
     *
     * @param value the string to check
     * @return {@code true} if a valid UUID, {@code false} otherwise
     * @see UUID
     */
    public static boolean isValidUUID(String value) {
        return RFC4122_PATTERN.matcher(value).matches();
    }

    /**
     * Checks if the {@code value} is a valid UUID conforming to RFC 4122 (including the Nil UUID). The general form is
     * 8-4-4-4-12 where all the digits are hexadecimal. Example: {@code e94c302e-e684-4d72-9060-a66461f858d6}, or the
     * nil UUID {@code 00000000-0000-0000-0000-000000000000}.
     * <p>
     * This method has been tested with UUIDs generated using {@link UUID#randomUUID()} and
     * {@link UUID#nameUUIDFromBytes(byte[])}. It has also been tested with UUIDs created using the constructor
     * {@link UUID#UUID(long, long)} <em>with the caveat that the most and least significant bits came from valid
     * type 3 and 4 UUIDs.</em>
     *
     * @param value the string to check
     * @return {@code true} if a valid UUID, {@code false} otherwise
     * @see UUID
     */
    public static boolean isValidUUIDAllowingNil(String value) {
        return NIL_UUID.equals(value) || isValidUUID(value);
    }
}
