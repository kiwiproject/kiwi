package org.kiwiproject.jsch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

@DisplayName("SftpConnector")
class SftpConnectorTest {

    private SftpConfig config;
    private JSch jsch;
    private Session session;

    @BeforeEach
    void setUp() {
        jsch = mock(JSch.class);
        session = mock(Session.class);
        config = SftpConfig.builder().build();
    }

    @Nested
    class Connect {

        @Test
        void shouldThrowException() throws JSchException {
            config.setHost("server-1.test");
            config.setKnownHostsFile("/home/users/bob/.ssh/known_hosts");

            var jSchException = new JSchException("cannot set known hosts");
            doThrow(jSchException).when(jsch).setKnownHosts(anyString());

            var connector = new SftpConnector(jsch, config);

            assertThatThrownBy(connector::connect)
                    .isExactlyInstanceOf(SftpTransfersException.class)
                    .hasMessage("Error occurred connecting to server-1.test")
                    .hasCause(jSchException);
        }
    }

    @Nested
    class SetKeyExchangeTypeIfConfiguredOrDetected {

        @Test
        void shouldSetKeyExchangeTypeOnSession_WhenKeyExchangeTypeIsPresent() {
            config.setHost("server-1.test");

            var keyExchangeType = "ecdsa-sha2-nistp256";
            config.setKeyExchangeType(keyExchangeType);

            var connector = new SftpConnector(jsch, config);

            connector.setKeyExchangeTypeIfConfiguredOrDetected(session);

            verify(session).setConfig(anyString(), eq(keyExchangeType));
        }

        @Test
        void shouldNotSetKeyExchangeTypeOnSession_WhenKeyExchangeTypeNotConfiguredOrDetected() {
            config.setHost("server-1.test");

            var hostKeyRepository = mock(HostKeyRepository.class);
            when(hostKeyRepository.getHostKey()).thenReturn(new HostKey[0]);
            when(jsch.getHostKeyRepository()).thenReturn(hostKeyRepository);

            var connector = new SftpConnector(jsch, config);
            
            connector.setKeyExchangeTypeIfConfiguredOrDetected(session);

            verifyNoInteractions(session);
        }
    }

    @Nested
    class GetOrDetectKeyExchangeType {

        @ParameterizedTest
        @ValueSource(strings = {
                "ssh-rsa",
                "ecdsa-sha2-nistp256",
                "ecdsa-sha2-nistp521"
        })
        void shouldReturnOptionalContainingValueFromSftpConfig(String keyExchangeType) {
            config.setKeyExchangeType(keyExchangeType);

            var connector = new SftpConnector(jsch, config);

            assertThat(connector.getOrDetectKeyExchangeType()).hasValue(keyExchangeType);

            verifyNoInteractions(jsch);
        }

        @Test
        void shouldReturnEmptyOptional_WhenNotConfigured_OrDetected() {
            config.setHost("server-1.test");
            config.setKnownHostsFile("/home/users/bob/.ssh/known_hosts");

            var hostKeyRepository = mock(HostKeyRepository.class);
            when(hostKeyRepository.getHostKey()).thenReturn(new HostKey[0]);
            when(jsch.getHostKeyRepository()).thenReturn(hostKeyRepository);

            var connector = new SftpConnector(jsch, config);

            assertThat(connector.getOrDetectKeyExchangeType()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ssh-rsa",
                "ecdsa-sha2-nistp256",
                "ecdsa-sha2-nistp521"
        })
        void shouldReturnOptional_ContainingDetectedValue(String keyExchangeType) {
            config.setHost("server-1.test");
            config.setKnownHostsFile("/home/users/bob/.ssh/known_hosts");

            var hostKeyRepository = mock(HostKeyRepository.class);

            when(jsch.getHostKeyRepository()).thenReturn(hostKeyRepository);

            // Mocks the static KiwiJSchHelpers#detectKeyExchangeTypeForHost method called
            // by getOrDetectKeyExchangeType()
            try (var mockedStatic = mockStatic(KiwiJSchHelpers.class)) {
                mockedStatic.when(() -> KiwiJSchHelpers.detectKeyExchangeTypeForHost(anyString(), any(HostKeyRepository.class)))
                        .thenReturn(Optional.of(keyExchangeType));

                var connector = new SftpConnector(jsch, config);

                assertThat(connector.getOrDetectKeyExchangeType()).hasValue(keyExchangeType);

                mockedStatic.verify(() -> KiwiJSchHelpers.detectKeyExchangeTypeForHost(config.getHost(), hostKeyRepository));
            }
        }
    }

    @Nested
    class AddAuthToSession {

        @Test
        void shouldAddIdentity_WhenGivenPrivateKey() throws JSchException {
            var privateKeyFilePath = "/users/alice/.ssh/id_rsa";
            config.setPrivateKeyFilePath(privateKeyFilePath);

            SftpConnector.addAuthToSession(config, jsch, session);

            verifyIdentityAdded(privateKeyFilePath);
        }

        @Test
        void shouldPreferPrivateKey_WhenGivenPrivateKeyAndPassword() throws JSchException {
            var privateKeyFilePath = "/users/alice/.ssh/id_rsa";
            config.setPrivateKeyFilePath(privateKeyFilePath);
            config.setPassword("my-secret-password-100");

            SftpConnector.addAuthToSession(config, jsch, session);

            verifyIdentityAdded(privateKeyFilePath);
        }

        private void verifyIdentityAdded(String privateKeyFilePath) throws JSchException {
            verify(jsch).addIdentity(privateKeyFilePath);
            verifyNoMoreInteractions(jsch);
            verifyNoInteractions(session);
        }

        @Test
        void shouldSetSessionPassword_WhenGivenPassword() throws JSchException {
            var password = "42-super-secret-8";
            config.setPassword(password);

            SftpConnector.addAuthToSession(config, jsch, session);

            verifyNoInteractions(jsch);
            verify(session).setPassword(password);
            verifyNoMoreInteractions(session);
        }

        @Test
        void shouldThrowException_WhenNoPrivateKeyOrPassword() {
            assertThatThrownBy(() -> SftpConnector.addAuthToSession(config, jsch, session))
                    .isExactlyInstanceOf(SftpTransfersException.class)
                    .hasMessage("Missing a private key and a password; cannot authenticate to the SFTP server");

            verifyNoInteractions(jsch, session);
        }
    }

    @Nested
    class DisableStrictHostKeyCheckingIfConfigured {

        @Test
        void shouldDoNothing_WhenStrictHostKeyCheckingIsEnabled() {
            SftpConnector.disableStrictHostKeyCheckingIfConfigured(config, session);

            verifyNoInteractions(session);
        }

        @Test
        void shouldSetStrictHostKeyChecking_OnSessionConfig_WhenStrictHostKeyCheckingIsEnabled() {
            config.setDisableStrictHostChecking(true);

            SftpConnector.disableStrictHostKeyCheckingIfConfigured(config, session);

            verify(session).setConfig("StrictHostKeyChecking", "no");
            verifyNoMoreInteractions(session);
        }
    }

}
