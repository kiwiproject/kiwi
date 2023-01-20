package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.net.LocalPortChecker.MAX_PORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

@DisplayName("LocalPortChecker")
class LocalPortCheckerTest {

    private LocalPortChecker localPortChecker;

    @BeforeEach
    void setUp() {
        localPortChecker = new LocalPortChecker();
    }

    @Nested
    class IsPortAvailable {

        @RepeatedTest(5)
        void shouldBeFalse_WhenPortIsNotAvailable() throws IOException {
            try (var serverSocket = newServerSocketOnRandomPort()) {
                var inUsePort = serverSocket.getLocalPort();

                assertThat(localPortChecker.isPortAvailable(inUsePort))
                        .describedAs("Port %d should be in use by the ServerSocket", inUsePort)
                        .isFalse();
            }
        }

        private ServerSocket newServerSocketOnRandomPort() throws IOException {
            return new ServerSocket(0);
        }

        @ParameterizedTest
        @ValueSource(ints = {15_679, 27_346, 65_035, MAX_PORT})
        void shouldBeTrue_ForPortsThatShouldNotBeInUse(int port) {
            assertThat(localPortChecker.isPortAvailable(port))
                    .describedAs("Port %d was in use, but was not expected to be", port)
                    .isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1024, -1, 0, (MAX_PORT + 1), 500_000})
        void shouldThrowIllegalArgumentException_ForInvalidPorts(int port) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> localPortChecker.isPortAvailable(port))
                    .withMessage("Invalid port: %d", port);
        }
    }

    @Nested
    class FindFirstOpenPortAbove {

        @ParameterizedTest
        @ValueSource(ints = {27_346, (MAX_PORT - 1)})
        void shouldBeTrue_ForPortThatShouldBeOpen(int port) {
            assertThat(localPortChecker.isPortAvailable(port))
                    .describedAs("Halting test since port %d is not open (pre-condition failed: we assumed it is open)", port)
                    .isTrue();

            assertThat(localPortChecker.findFirstOpenPortAbove(port - 1)).isEqualTo(OptionalInt.of(port));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, MAX_PORT})
        void shouldThrowIllegalArgumentException_ForInvalidPorts(int port) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> localPortChecker.findFirstOpenPortAbove(port))
                    .withMessage("Invalid start port: " + port);
        }
    }

    @Nested
    class FindFirstOpenPortFrom {

        @ParameterizedTest
        @ValueSource(ints = {27_346, MAX_PORT})
        void shouldBeTrue_ForPortThatShouldBeOpen(int port) {
            assertThat(localPortChecker.isPortAvailable(port))
                    .describedAs("Halting test since port %d is not open (pre-condition failed: we assumed it is open)", port)
                    .isTrue();

            assertThat(localPortChecker.findFirstOpenPortFrom(port)).isEqualTo(OptionalInt.of(port));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, (MAX_PORT + 1)})
        void shouldThrowIllegalArgumentException_ForInvalidPorts(int port) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> localPortChecker.findFirstOpenPortFrom(port))
                    .withMessage("Invalid start port: " + port);
        }
    }

    @Nested
    class FindRandomOpenPort {

        @RepeatedTest(25)
        void shouldFindAnOpenPort() {
            var port = localPortChecker.findRandomOpenPort().orElseThrow();
            assertThat(port).isPositive().isLessThanOrEqualTo(MAX_PORT);
        }
    }

    @Nested
    class FindRandomOpenPortFrom {

        @RepeatedTest(25)
        void shouldFindAnOpenPortFromStartPort() {
            var startPort = ThreadLocalRandom.current().nextInt(1, MAX_PORT);
            var port = localPortChecker.findRandomOpenPortFrom(startPort).orElseThrow();
            assertThat(port).isPositive().isLessThanOrEqualTo(MAX_PORT);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1024, -1, 0, (MAX_PORT + 1), 500_000})
        void shouldThrowIllegalArgumentException_ForInvalidPorts(int port) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> localPortChecker.findRandomOpenPortFrom(port))
                    .withMessage("Invalid start port: %d", port);
        }
    }

    @Nested
    class FindRandomOpenPortAbove {

        @RepeatedTest(25)
        void shouldFindAnOpenPortFromStartPort() {
            var startPort = ThreadLocalRandom.current().nextInt(0, MAX_PORT - 1);
            var port = localPortChecker.findRandomOpenPortAbove(startPort).orElseThrow();
            assertThat(port).isPositive().isLessThanOrEqualTo(MAX_PORT);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1024, -1, (MAX_PORT + 1), 500_000})
        void shouldThrowIllegalArgumentException_ForInvalidPorts(int port) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> localPortChecker.findRandomOpenPortAbove(port))
                    .withMessage("Invalid start port: %d", port);
        }
    }
}
