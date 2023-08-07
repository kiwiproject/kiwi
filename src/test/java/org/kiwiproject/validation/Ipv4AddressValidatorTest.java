package org.kiwiproject.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoPropertyViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Ipv4AddressValidator")
class Ipv4AddressValidatorTest {

    private static final String NOT_A_VALID_IPV_4_ADDRESS_MESSAGE = "is not a valid IPv4 address";
    private static final int SENTINEL_INVALID_SEGMENT = -1;

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class WhenNullValue {

        @Test
        void shouldBeInvalid_ByDefault() {
            var config = new Config(null);
            assertPropertyViolations(validator, config, "ipAddress", NOT_A_VALID_IPV_4_ADDRESS_MESSAGE);
        }

        @Test
        void shouldBeValid_WhenAllowingNulls() {
            var config = new ConfigAllowingNull(null);
            assertNoViolations(validator, config);
        }
    }

    @Nested
    class ShouldBeValid {

        @ParameterizedTest
        @ValueSource(strings = {
                "0.0.0.0",
                "255.0.0.0",
                "0.255.0.0",
                "0.0.255.0",
                "0.0.0.255",
                "255.255.255.255",
                "255.255.0.0",
                "255.255.255.0",
                "0.255.255.0",
                "0.255.255.255",
                "0.0.255.255",
        })
        void whenBoundaryConditionIPs(String ip) {
            var config = new Config(ip);
            assertNoPropertyViolations(validator, config, "ipAddress");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "127.0.0.1",
                "1.1.1.1",
                "11.11.11.11",
                "111.111.111.111",
                "10.10.50.255",
                "192.168.1.101",
                "192.168.111.1",
                "192.168.111.11",
                "192.168.111.111",
                "5.0.0.128",
        })
        void whenValidIps(String ip) {
            var config = new Config(ip);
            assertNoPropertyViolations(validator, config, "ipAddress");
        }
    }

    @Nested
    class ShouldBeInvalid {

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                " ",
                "1",
                "1.",
                "1.1",
                "1.1.",
                "1.1.1",
                "1.1.1.",
                "256.1.1.1",
                "1.256.1.1",
                "1.1.256.1",
                "1.1.1.256",
                "256.256.256.256",
                "-1.1.1.1",
                "1.-1.1.1",
                "1.1.-1.1",
                "1.1.1.-1",
                "a.1.1.1",
                "1.a.1.1",
                "1.1.a.1",
                "1.1.1.a",
                "a.b.c.d",
        })
        void whenTheIpsAreNotValid(String ip) {
            var config = new Config(ip);
            assertPropertyViolations(validator, config, "ipAddress", NOT_A_VALID_IPV_4_ADDRESS_MESSAGE);
        }
    }

    @Nested
    class IpSegmentAsInt {

        @ParameterizedTest
        @ValueSource(strings = {
                "0",
                "1",
                "192",
                "255",
                "256",
        })
        void shouldReturnInteger_WhenSegmentIsAnInteger(String segment) {
            assertThat(Ipv4AddressValidator.ipSegmentAsInt(segment))
                    .isEqualTo(Integer.parseInt(segment));
        }

        @Nested
        class ShouldReturnSentinelInvalidSegment {

            @ParameterizedTest
            @ValueSource(strings = {
                    "a",
                    "ab",
                    "abcd",
            })
            void whenSegmentIsNotAnInteger(String segment) {
                assertThat(Ipv4AddressValidator.ipSegmentAsInt(segment)).isEqualTo(SENTINEL_INVALID_SEGMENT);
            }

            @ParameterizedTest
            @NullAndEmptySource
            void whenSegmentIsEmpty(String segment) {
                assertThat(Ipv4AddressValidator.ipSegmentAsInt(segment)).isEqualTo(SENTINEL_INVALID_SEGMENT);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    " ",
                    "  "
            })
            void whenSegmentIsBlank(String segment) {
                assertThat(Ipv4AddressValidator.ipSegmentAsInt(segment)).isEqualTo(SENTINEL_INVALID_SEGMENT);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "01",
                    "010",
                    "001",
            })
            void whenSegmentStartsWithZero(String segment) {
                assertThat(Ipv4AddressValidator.ipSegmentAsInt(segment)).isEqualTo(SENTINEL_INVALID_SEGMENT);
            }
        }
    }

    @Nested
    class IsInvalidIpSegment {

        @ParameterizedTest
        @CsvSource({
                "0, false",
                "1, false",
                "192, false",
                "255, false",
                "-1, true",
                "-2, true",
                "-192, true",
                "-2147483648, true",
                "256, true",
                "512, true",
                "2147483647, true",
        })
        void shouldReturnExpectedResult(int ipSegment, boolean expectedResult) {
            assertThat(Ipv4AddressValidator.isInvalidIpSegment(ipSegment)).isEqualTo(expectedResult);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Config {

        @Setter
        @Ipv4Address
        private String ipAddress;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ConfigAllowingNull {

        @Setter
        @Ipv4Address(allowNull = true)
        private String ipAddress;
    }
}
