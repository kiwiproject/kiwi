package org.kiwiproject.validation;

import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.Validator;

@DisplayName("Ipv4AndPortValidator")
class Ipv4AndPortValidatorTest {

    private static final String NOT_VALID_IP_AND_PORT_MESSAGE = "is not a valid ipv4:port, e.g. 192.168.1.150:8888";

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class WhenNullValue {

        @Test
        void shouldBeInvalid_ByDefault() {
            var config = new ServerConfig(null);
            assertOnePropertyViolation(validator, config, "hostIp");
        }

        @Test
        void shouldBeValid_WhenAllowingNulls() {
            var config = new ServerConfigAllowingNull(null);
            assertNoViolations(validator, config);
        }
    }

    @Nested
    class ShouldBeValid {

        @ParameterizedTest
        @ValueSource(strings = {
                "1.1.1.1:5000",
                "11.11.11.11:5000",
                "111.111.111.111:5000",
                "10.10.50.255:9500",
                "192.168.1.101:9100",
        })
        void whenValidIpAndPort(String value) {
            var config = new ServerConfig(value);
            assertNoViolations(validator, config);
        }
    }

    @Nested
    class ShouldBeInvalid {

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                " ",
                "1.1.1.1",
                ":9500",
                "1:9500",
                "1.1:9500",
                "1.1.1:9500",
                "1111.1.1.1:9100",
                "1.1111.1.1:9100",
                "1.1.1111.1:9100",
                "1.1.1.1111:9100",
        })
        void whenInvalidIpAndPort(String value) {
            var config = new ServerConfig(value);
            assertOnePropertyViolation(validator, config, "hostIp");
        }
    }

    @Test
    void shouldReportExpectedErrorMessage() {
        var config = new ServerConfig("1111.1.1.1:9500");
        assertPropertyViolations(validator, config, "hostIp", NOT_VALID_IP_AND_PORT_MESSAGE);
    }

    @AllArgsConstructor
    static class ServerConfig {
        @Getter
        @Ipv4AndPort
        private final String hostIp;
    }

    @AllArgsConstructor
    static class ServerConfigAllowingNull {
        @Getter
        @Ipv4AndPort(allowNull = true)
        private final String hostIp;
    }
}
