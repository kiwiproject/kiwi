package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiwiproject.util.BlankStringSource;

@DisplayName("SimpleHostAndPort")
class SimpleHostAndPortTest {

    @ParameterizedTest
    @BlankStringSource
    void testFromWithDefaults_WithBlankStrings(String input) {
        var hostAndPort = SimpleHostAndPort.from(input, "192.168.1.101", 8900);

        assertThat(hostAndPort.getHost()).isEqualTo("192.168.1.101");
        assertThat(hostAndPort.getPort()).isEqualTo(8900);
    }

    @Test
    void testFromWithDefaults_WhenValidHostPortString() {
        var hostAndPort = SimpleHostAndPort.from("192.168.1.101:8900", "127.0.0.1", 8500);

        assertThat(hostAndPort.getHost()).isEqualTo("192.168.1.101");
        assertThat(hostAndPort.getPort()).isEqualTo(8900);
    }

    @Test
    void testFromWithDefaults_WhenInvalidHostPortString() {
        assertThatThrownBy(() -> SimpleHostAndPort.from("blah", "127.0.0.1", 8500))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("blah is not in format host:port");
    }

    @Test
    void testFromWithDefaults_WhenHostOnlyString() {
        assertThatThrownBy(() -> SimpleHostAndPort.from("192.168.1.101", "127.0.0.1", 8500))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("192.168.1.101 is not in format host:port");
    }

    @ParameterizedTest
    @BlankStringSource
    void testFromWithNoDefaults_WithBlankStrings(String input) {
        assertThatThrownBy(() -> SimpleHostAndPort.from(input))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("hostAndPortString must not be blank");
    }

    @Test
    void testFromWithNoDefaults_WithValidHostPortString() {
        var hostAndPort = SimpleHostAndPort.from("192.168.1.101:8011");
        assertThat(hostAndPort.getHost()).isEqualTo("192.168.1.101");
        assertThat(hostAndPort.getPort()).isEqualTo(8011);
    }

    @Test
    void testFromWithNoDefaults_WithInvalidPort() {
        assertThatThrownBy(() -> SimpleHostAndPort.from("192.168.1.101:abc"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testCustomToString() {
        var hostAndPortString = "192.168.1.101:8900";
        var hostAndPort = SimpleHostAndPort.from(hostAndPortString, "127.0.0.1", 8500);
        assertThat(hostAndPort).hasToString(hostAndPortString);
    }

    @Test
    void testEqualsAndHashCode_ForSanityCheck() {
        var hostAndPort1 = SimpleHostAndPort.from("192.168.1.101:8900");
        var hostAndPort2 = SimpleHostAndPort.from("192.168.1.101:8900");

        assertThat(hostAndPort1).isEqualTo(hostAndPort2).hasSameHashCodeAs(hostAndPort2);

        var hostAndPort3 = SimpleHostAndPort.from("192.168.1.201:8900");
        var hostAndPort4 = SimpleHostAndPort.from("192.168.1.101:8500");

        assertThat(hostAndPort1)
                .isNotEqualTo(hostAndPort3)
                .isNotEqualTo(hostAndPort4);
        assertThat(hostAndPort1.hashCode()).isNotEqualTo(hostAndPort3.hashCode());
        assertThat(hostAndPort1.hashCode()).isNotEqualTo(hostAndPort4.hashCode());
    }
}
