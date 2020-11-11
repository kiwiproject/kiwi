package org.kiwiproject.jsch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("KiwiJSchHelpers")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiJSchHelpersTest {

    private HostKeyRepository knownHosts;
    private Session session;

    @BeforeEach
    void setUp() {
        knownHosts = mock(HostKeyRepository.class);
        session = mock(Session.class);
    }

    @Nested
    class DetectKeyExchangeTypeForHost {

        @Test
        void shouldBeEmpty_WhenEmptyKnownHosts() {
            when(knownHosts.getHostKey()).thenReturn(new HostKey[0]);
            assertThat(KiwiJSchHelpers.detectKeyExchangeTypeForHost("server.test", knownHosts)).isEmpty();
        }

        @ParameterizedTest
        @CsvSource({
                " server.test, server.test",
                " 'server.test,192.168.1.150', server.test ",
                " 'server.test,192.168.1.150', 192.168.1.150",
        })
        void shouldFindMatch(String knownHostEntry, String searchValue) throws JSchException {
            var hostKey = new HostKey(knownHostEntry, HostKey.ECDSA256, new byte[0]);

            when(knownHosts.getHostKey()).thenReturn(new HostKey[]{hostKey});

            assertThat(KiwiJSchHelpers.detectKeyExchangeTypeForHost(searchValue, knownHosts))
                    .isPresent()
                    .contains(hostKey.getType());
        }

        @Test
        void shouldReturnFirstMatch_WhenMultipleMatchingHostsExist() throws JSchException {
            var hostKey1 = new HostKey("server.test", HostKey.ECDSA256, new byte[0]);
            var hostKey2 = new HostKey("server.test", HostKey.SSHRSA, new byte[0]);

            when(knownHosts.getHostKey()).thenReturn(new HostKey[]{hostKey1, hostKey2});

            assertThat(KiwiJSchHelpers.detectKeyExchangeTypeForHost("server.test", knownHosts))
                    .isPresent()
                    .contains(hostKey1.getType());
        }

        @Test
        void shouldThrowException_WhenGivenInvalidKnownHostEntry() throws JSchException {
            var hostKey = new HostKey("server.test,", HostKey.ECDSA256, new byte[0]);
            when(knownHosts.getHostKey()).thenReturn(new HostKey[]{hostKey});

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiJSchHelpers.detectKeyExchangeTypeForHost("server.test", knownHosts))
                    .withMessage("Expecting host key to be in format: hostName,IP");
        }
    }

    @Nested
    class SetSessionKeyExchangeType {

        @Test
        void shouldSetSessionKeyExchangeType() {
            KiwiJSchHelpers.setSessionKeyExchangeType(session, "ssh-rsa");
            verify(session).setConfig("server_host_key", "ssh-rsa");
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenInvalidArgument(SoftAssertions softly) {
            softly.assertThatThrownBy(() ->
                    KiwiJSchHelpers.setSessionKeyExchangeType(mock(Session.class), null))
                    .describedAs("null")
                    .isExactlyInstanceOf(IllegalArgumentException.class);

            softly.assertThatThrownBy(() ->
                    KiwiJSchHelpers.setSessionKeyExchangeType(mock(Session.class), ""))
                    .describedAs("empty string")
                    .isExactlyInstanceOf(IllegalArgumentException.class);

            softly.assertThatThrownBy(() ->
                    KiwiJSchHelpers.setSessionKeyExchangeType(mock(Session.class), " "))
                    .describedAs("blank string")
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }
}
