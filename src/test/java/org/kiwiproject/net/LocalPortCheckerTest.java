package org.kiwiproject.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

@DisplayName("LocalPortChecker")
class LocalPortCheckerTest {

    private LocalPortChecker localPortChecker;

    @BeforeEach
    void setUp() {
        localPortChecker = new LocalPortChecker();
    }

    @Test
    void testIsPortAvailable_WherePortIsNotAvailable() {
        assertThat(localPortChecker.isPortAvailable(22))
                .describedAs("The SSH port (22) should always be in use")
                .isFalse();
    }

    @Test
    void testIsPortAvailable_ForPortsThatShouldNotBeInUse() {
        assertThat(localPortChecker.isPortAvailable(15_679)).isTrue();
        assertThat(localPortChecker.isPortAvailable(27_346)).isTrue();
    }

    @Test
    void testFindFirstOpenPortAbove_ForPortThatShouldBeOpen() {
        var portToCheck = 27_346;
        assertThat(localPortChecker.isPortAvailable(portToCheck))
                .describedAs("Halt test as port for test is not open")
                .isTrue();

        assertThat(localPortChecker.findFirstOpenPortAbove(portToCheck - 1)).isEqualTo(OptionalInt.of(portToCheck));
    }

    @Test
    void testFindFirstOpenPortAbove_ForInvalidPorts() {
        softAssertFindFirstOpenPortAboveInvalidPort(-1);
        softAssertFindFirstOpenPortAboveInvalidPort(LocalPortChecker.MAX_PORT);
    }

    private void softAssertFindFirstOpenPortAboveInvalidPort(int port) {
        assertSoftly(softly ->
                softly.assertThatThrownBy(() -> localPortChecker.findFirstOpenPortAbove(port))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Invalid start port")
        );
    }
}
