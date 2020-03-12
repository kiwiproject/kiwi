package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ExtendWith(SoftAssertionsExtension.class)
public class UUIDsTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random();
    }

    @Test
    public void testRandomUUIDString() {
        assertThat(UUIDs.randomUUIDString()).isNotNull();
        assertThat(UUIDs.isValidUUID(UUIDs.randomUUIDString())).isTrue();
    }

    @Test
    public void testIsValidUUID_ForValidType4UUIDs(SoftAssertions softly) {
        assertValidUUIDs(softly, UUID::randomUUID);
    }

    @Test
    public void testIsValidUUID_ForValidType3UUIDs(SoftAssertions softly) {
        assertValidUUIDs(softly, () -> UUID.nameUUIDFromBytes(randomByteArray()));
    }

    @Test
    public void testIsValidUUID_ForValidType4UUIDs_UsingUUIDConstructor(SoftAssertions softly) {
        assertValidUUIDs(softly, () -> {
            var validUUID = UUID.randomUUID();
            return new UUID(validUUID.getMostSignificantBits(), validUUID.getLeastSignificantBits());
        });
    }

    @Test
    public void testIsValidUUID_ForValidType3UUIDs_UsingUUIDConstructor(SoftAssertions softly) {
        assertValidUUIDs(softly, () -> {
            var validUUID = UUID.nameUUIDFromBytes(randomByteArray());
            return new UUID(validUUID.getMostSignificantBits(), validUUID.getLeastSignificantBits());
        });
    }

    private void assertValidUUIDs(SoftAssertions softly, Supplier<UUID> supplier) {
        IntStream.range(0, 1000)
                .mapToObj(value -> supplier.get())
                .forEach(candidate ->
                        softly.assertThat(UUIDs.isValidUUID(candidate.toString()))
                                .describedAs("candidate %s should be valid", candidate)
                                .isTrue()
                );
    }

    @ParameterizedTest
    @MethodSource("invalidUUIDs")
    void testIsValidUUID_ForInvalidUUIDs(String value) {
        assertThat(UUIDs.isValidUUID(value)).isFalse();
    }

    static Stream<String> invalidUUIDs() {
        return Stream.of(
                "1",
                "bob",
                "!",
                "abcd-56-efg",
                UUIDs.randomUUIDString().substring(0, 35),
                UUIDs.randomUUIDString().substring(1),
                UUIDs.randomUUIDString().toUpperCase(Locale.ENGLISH)
        );
    }

    private byte[] randomByteArray() {
        int length = random.nextInt(100);
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

}