package org.kiwiproject.base;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class UUIDsTest {

    private static final Random RANDOM = new Random();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void testRandomUUIDString() {
        assertThat(UUIDs.randomUUIDString()).isNotNull();
        assertThat(UUIDs.isValidUUID(UUIDs.randomUUIDString())).isTrue();
    }

    @Test
    public void testIsValidUUID_ForValidType4UUIDs() {
        assertValidUUIDs(UUID::randomUUID);
    }

    @Test
    public void testIsValidUUID_ForValidType3UUIDs() {
        assertValidUUIDs(() -> UUID.nameUUIDFromBytes(randomByteArray()));
    }

    @Test
    public void testIsValidUUID_ForValidType4UUIDs_UsingUUIDConstructor() {
        assertValidUUIDs(() -> {
            UUID validUUID = UUID.randomUUID();
            return new UUID(validUUID.getMostSignificantBits(), validUUID.getLeastSignificantBits());
        });
    }

    @Test
    public void testIsValidUUID_ForValidType3UUIDs_UsingUUIDConstructor() {
        assertValidUUIDs(() -> {
            UUID validUUID = UUID.nameUUIDFromBytes(randomByteArray());
            return new UUID(validUUID.getMostSignificantBits(), validUUID.getLeastSignificantBits());
        });
    }

    private void assertValidUUIDs(Supplier<UUID> supplier) {
        IntStream.range(0, 1000)
                .mapToObj(value -> supplier.get())
                .forEach(candidate ->
                        softly.assertThat(UUIDs.isValidUUID(candidate.toString()))
                                .describedAs("candidate %s should be valid", candidate)
                                .isTrue()
                );
    }

    private static byte[] randomByteArray() {
        int length = RANDOM.nextInt(100);
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

}