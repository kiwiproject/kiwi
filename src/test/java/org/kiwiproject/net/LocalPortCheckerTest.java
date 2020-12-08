package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.OptionalInt;

@DisplayName("LocalPortChecker")
class LocalPortCheckerTest {

    private LocalPortChecker localPortChecker;

    @BeforeEach
    void setUp() {
        localPortChecker = new LocalPortChecker();
    }

    @Test
    void testIsPortAvailable_WherePortIsNotAvailable() throws IOException {
        try (var serverSocket = new ServerSocket(0)) {
            var inUsePort = serverSocket.getLocalPort();

            assertThat(localPortChecker.isPortAvailable(inUsePort))
                    .describedAs("Port %d should be in use by the ServerSocket", inUsePort)
                    .isFalse();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {15_679, 27_346, 65_035})
    void testIsPortAvailable_ForPortsThatShouldNotBeInUse(int port) {
        assertThat(localPortChecker.isPortAvailable(port))
                .describedAs("Port %d was in use, but was not expected to be", port)
                .isTrue();
    }

    @Test
    void testFindFirstOpenPortAbove_ForPortThatShouldBeOpen() {
        var portToCheck = 27_346;
        assertThat(localPortChecker.isPortAvailable(portToCheck))
                .describedAs("Halting test since port %d is not open (pre-condition failed: we assumed it is open)", portToCheck)
                .isTrue();

        assertThat(localPortChecker.findFirstOpenPortAbove(portToCheck - 1)).isEqualTo(OptionalInt.of(portToCheck));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, LocalPortChecker.MAX_PORT})
    void testFindFirstOpenPortAbove_ForInvalidPorts(int port) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> localPortChecker.findFirstOpenPortAbove(port))
                .withMessage("Invalid start port: " + port);

    }

}
