package org.kiwiproject.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@DisplayName("UUIDs")
@ExtendWith(SoftAssertionsExtension.class)
class UUIDsTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random();
    }

    @RepeatedTest(25)
    void shouldCreateRandomUUIDStrings() {
        assertThat(UUIDs.randomUUIDString()).isNotBlank();
        assertThat(UUIDs.isValidUUID(UUIDs.randomUUIDString())).isTrue();
    }

    @Nested
    class IsValidUUID {

        @Test
        void shouldBeTrue_ForValidType4UUIDs(SoftAssertions softly) {
            assertValidUUIDs(softly, UUID::randomUUID);
        }

        @Test
        void shouldBeTrue_ForValidType3UUIDs(SoftAssertions softly) {
            assertValidUUIDs(softly, () -> UUID.nameUUIDFromBytes(randomByteArray()));
        }

        @Test
        void shouldBeTrue_ForValidType4UUIDs_UsingUUIDConstructor(SoftAssertions softly) {
            assertValidUUIDs(softly, () -> {
                var validUUID = UUID.randomUUID();
                return new UUID(validUUID.getMostSignificantBits(), validUUID.getLeastSignificantBits());
            });
        }

        @Test
        void shouldBeTrue_ForValidType3UUIDs_UsingUUIDConstructor(SoftAssertions softly) {
            assertValidUUIDs(softly, () -> {
                var validUUID = UUID.nameUUIDFromBytes(randomByteArray());
                return new UUID(validUUID.getMostSignificantBits(), validUUID.getLeastSignificantBits());
            });
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "015186F5-539d-4bcb-b3bc-34718931ef37",
                "015186f5-539D-4bcb-b3bc-34718931ef37",
                "015186f5-539d-4BCB-b3bc-34718931ef37",
                "015186f5-539d-4bcb-B3BC-34718931ef37",
                "015186f5-539d-4bcb-b3bc-34718931EF37",
                "015186F5-539D-4BCB-B3BC-34718931EF37"
        })
        void shouldBeTrue_ForUppercaseUUIDs(String uuid, SoftAssertions softly) {
            assertValidUUIDs(softly, () -> UUID.fromString(uuid));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.base.UUIDsTest#invalidUUIDs")
        void shouldBeFalse_ForInvalidUUIDs(String value) {
            assertThat(UUIDs.isValidUUID(value)).isFalse();
        }

        @RepeatedTest(5)
        void shouldBeFalse_ForTheNilUUID() {
            assertThat(UUIDs.isValidUUID("00000000-0000-0000-0000-000000000000")).isFalse();
        }
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

    static Stream<String> invalidUUIDs() {
        return Stream.of(
                "1",
                "bob",
                "!",
                "abcd-56-efg",
                UUIDs.randomUUIDString().substring(0, 35),
                UUIDs.randomUUIDString().substring(1)
        );
    }

    private byte[] randomByteArray() {
        int length = random.nextInt(100);
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

}