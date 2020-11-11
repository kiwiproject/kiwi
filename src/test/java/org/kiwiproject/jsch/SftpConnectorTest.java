package org.kiwiproject.jsch;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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