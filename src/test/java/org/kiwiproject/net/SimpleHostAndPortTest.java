package org.kiwiproject.net;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.kiwiproject.util.BlankStringArgumentsProvider;

@DisplayName("SimpleHostAndPort")
class SimpleHostAndPortTest {

    @ParameterizedTest
    @ArgumentsSource(BlankStringArgumentsProvider.class)
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

    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest
    @ArgumentsSource(BlankStringArgumentsProvider.class)
    void testFromWithNoDefaults_WithBlankStrings(String input) {
        if (isNull(input)) {
            assertThatThrownBy(() -> SimpleHostAndPort.from(null))
                    .isExactlyInstanceOf(NullPointerException.class);
        } else {
            assertThatThrownBy(() -> SimpleHostAndPort.from(input))
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessageEndingWith("is not in format host:port");
        }
    }

    @Test
    void testFromWithNoDefaults_WithValidHostPortString() {
        var hostAndPort = SimpleHostAndPort.from("192.168.1.101:8011");
        assertThat(hostAndPort.getHost()).isEqualTo("192.168.1.101");
        assertThat(hostAndPort.getPort()).isEqualTo(8011);
    }

    @Test
    void testCustomToString() {
        var hostAndPortString = "192.168.1.101:8900";
        var hostAndPort = SimpleHostAndPort.from(hostAndPortString, "127.0.0.1", 8500);
        assertThat(hostAndPort.toString()).isEqualTo(hostAndPortString);
    }

    @Test
    void testEqualsAndHashCode_ForSanityCheck() {
        var hostAndPort1 = SimpleHostAndPort.from("192.168.1.101:8900");
        var hostAndPort2 = SimpleHostAndPort.from("192.168.1.101:8900");

        assertThat(hostAndPort1).isEqualTo(hostAndPort2);
        assertThat(hostAndPort1.hashCode()).isEqualTo(hostAndPort2.hashCode());
    }
}
