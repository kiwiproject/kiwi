package org.kiwiproject.jsch;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.internal.Fixtures.fixture;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;
import java.util.function.BiFunction;

/**
 * @implNote This test has a lot more mocking than I would like, but I have not found an easy way yet to
 * setup an embedded SFTP server for use by tests.
 */
@DisplayName("SftpTransfers")
class SftpTransfersTest {

    private ChannelSftp channelSftp;
    private Session session;
    private SftpConfig config;
    private SftpTransfers sftp;
    private SftpConnector connector;

    @BeforeEach
    void setUp() throws JSchException {
        config = SftpConfig.builder()
                .remoteBasePath("/tmp")
                .errorPath("/tmp/transfer-errors")
                .user("kiwi-user")
                .host("localhost")
                .password("mypass")
                .build();

        // The following mocks out the steps to configure and establish a fake SFTP connection using JSch
        var jsch = mock(JSch.class);
        session = mock(Session.class);
        channelSftp = mock(ChannelSftp.class);

        // Mock known hosts
        var hostKeyRepository = mock(HostKeyRepository.class);
        var key = new HostKey("localhost", HostKey.SSHRSA, new byte[]{});
        when(hostKeyRepository.getHostKey()).thenReturn(new HostKey[]{key});
        when(jsch.getHostKeyRepository()).thenReturn(hostKeyRepository);

        // Mock establishment of a new Session
        when(jsch.getSession(config.getUser(), config.getHost(), config.getPort()))
                .thenReturn(session);
        when(session.openChannel("sftp")).thenReturn(channelSftp);
        when(channelSftp.isConnected()).thenReturn(true);

        // Create new connector and transfer objects
        connector = SftpConnector.setupAndOpenConnection(jsch, config);
        sftp = new SftpTransfers(connector);
    }

    @Nested
    class ConnectionMethodDelegation {

        private SftpConnector connector;

        @BeforeEach
        void setUp() {
            connector = mock(SftpConnector.class);
            sftp = new SftpTransfers(connector);
        }

        @Test
        void shouldDelegateToSftpConnector_WhenConnecting() {
            sftp.connect();
            verify(connector).connect();
        }

        @Test
        void shouldDelegateToSftpConnector_WhenDisconnecting() {
            sftp.disconnect();
            verify(connector).disconnect();
        }
    }

    @Nested
    class Disconnect {

        @BeforeEach
        void setUp() {
            reset(channelSftp, session);
        }

        @Test
        void shouldDisconnectTheChannelAndSession() {
            connector.disconnect();

            verifyDisconnectCalls();
        }

        @Test
        void shouldDoNothing_WhenDisconnected() {
            // Only the first disconnect should do anything...
            connector.disconnect();
            connector.disconnect();
            connector.disconnect();
            connector.disconnect();

            verifyDisconnectCalls();
        }

        private void verifyDisconnectCalls() {
            verify(channelSftp).disconnect();
            verify(session).disconnect();

            verifyNoMoreInteractions(channelSftp, session);
        }
    }

    @Nested
    class PutFile {

        @Test
        void shouldSendTheGivenFileToTheRemotePath() throws SftpException {
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-push.txt");

            sftp.putFile(Path.of(config.getRemoteBasePath()), "test-put-file.txt", inputStream);

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).put(isA(InputStream.class), eq("test-put-file.txt"));
        }

        @Test
        void shouldAttemptToCreateTheRemotePathIfItDoesntExist() throws SftpException {
            doThrow(new SftpException(1, "Doesn't exist")).doNothing().when(channelSftp).cd("/tmp");
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-push.txt");

            sftp.putFile(Path.of(config.getRemoteBasePath()), "test-put-file.txt", inputStream);

            verify(channelSftp, times(2)).cd("/tmp");
            verify(channelSftp).mkdir("/tmp");
            verify(channelSftp).put(isA(InputStream.class), eq("test-put-file.txt"));
        }

        @Test
        void shouldThrowSftpTransfersExceptionWhenPutFails() throws SftpException {
            doThrow(new SftpException(1, "Test exception")).when(channelSftp).put(isA(InputStream.class), eq("test-put-file.txt"));
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-push.txt");
            var remotePath = Path.of(config.getRemoteBasePath());

            assertThatThrownBy(() ->
                    sftp.putFile(remotePath, "test-put-file.txt", inputStream))
                    .isExactlyInstanceOf(SftpTransfersException.class)
                    .hasMessage("1: Test exception");

            verify(channelSftp).cd("/tmp");
        }
    }

    @Nested
    class GetAndStoreFile {

        @TempDir
        Path tempDirForPulls;

        @Test
        void shouldStoreTheRetrievedLocallyWithTheSameName() throws SftpException {
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-pull.txt");
            when(channelSftp.get("test-file-to-pull.txt")).thenReturn(inputStream);

            sftp.getAndStoreFile(Path.of(config.getRemoteBasePath()), tempDirForPulls, "test-file-to-pull.txt");

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).get("test-file-to-pull.txt");

            Path localPath = tempDirForPulls.resolve("test-file-to-pull.txt");
            assertThat(localPath).exists();
        }

        @Test
        void shouldCreateLocalPathIfNecessaryAndStoreLocally() throws SftpException {
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-pull.txt");
            when(channelSftp.get("test-file-to-pull.txt")).thenReturn(inputStream);

            sftp.getAndStoreFile(Path.of(config.getRemoteBasePath()), tempDirForPulls.resolve("shouldBeDeleted"), "test-file-to-pull.txt");

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).get("test-file-to-pull.txt");

            Path localPath = tempDirForPulls.resolve("shouldBeDeleted");
            assertThat(localPath.resolve("test-file-to-pull.txt")).exists();
        }

        @Test
        void shouldRenameTheFileThatIsPulledWhenStoringLocally() throws SftpException {
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-pull.txt");
            when(channelSftp.get("test-file-to-pull.txt")).thenReturn(inputStream);

            sftp.getAndStoreFile(Path.of(config.getRemoteBasePath()), tempDirForPulls, "test-file-to-pull.txt", "new-name.txt");

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).get("test-file-to-pull.txt");

            Path localPath = tempDirForPulls.resolve("new-name.txt");
            assertThat(localPath).exists();
        }

        @Test
        void shouldThrowSftpTransfersExceptionWhenGetFails() throws SftpException {
            doThrow(new SftpException(1, "Test exception")).when(channelSftp).get("test-file-to-pull.txt");
            var remotePath = Path.of(config.getRemoteBasePath());

            assertThatThrownBy(() ->
                    sftp.getAndStoreFile(remotePath, tempDirForPulls, "test-file-to-pull.txt", "new-name.txt"))
                    .isExactlyInstanceOf(SftpTransfersException.class)
                    .hasMessage(("1: Test exception"));

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).get("test-file-to-pull.txt");

            Path localPath = tempDirForPulls.resolve("new-name.txt");
            assertThat(localPath).doesNotExist();
        }

        @Test
        void shouldRecursivelyGetAndStoreFiles() throws SftpException {
            var inputStream = fixtureInputStream("SftpTransfersTest/file-to-pull.txt");
            when(channelSftp.get(anyString())).thenReturn(inputStream);

            var entries = buildEntries();
            when(channelSftp.ls("/tmp")).thenReturn(entries);

            var subEntries = buildSubEntries();
            when(channelSftp.ls("/tmp/test-dir")).thenReturn(subEntries);

            var remotePath = Path.of(config.getRemoteBasePath());
            Path localPath = tempDirForPulls.resolve("sftp-get-test");
            BiFunction<Path, String, Path> localPathSupplier = (root, file) -> {
                if (root.getNameCount() == 1) {
                    return localPath;
                }

                return localPath.resolve(root.subpath(1, root.getNameCount()));
            };
            sftp.getAndStoreAllFiles(remotePath, localPathSupplier, (root, file) -> file);

            verify(channelSftp, times(2)).cd("/tmp");
            verify(channelSftp, times(2)).cd("/tmp/test-dir");
            verify(channelSftp).get("test-file-1.txt");
            verify(channelSftp).get("test-file-2.txt");
            verify(channelSftp).get("sub-test-file-1.txt");
            verify(channelSftp).get("sub-test-file-2.txt");

            assertThat(localPath).exists();
            assertThat(localPath.resolve("test-file-1.txt")).exists();
            assertThat(localPath.resolve("test-file-2.txt")).exists();
            assertThat(localPath.resolve("test-dir")).exists();
            assertThat(localPath.resolve("test-dir").resolve("sub-test-file-1.txt")).exists();
            assertThat(localPath.resolve("test-dir").resolve("sub-test-file-2.txt")).exists();
        }
    }

    private static InputStream fixtureInputStream(String resourceName) {
        return toInputStream(fixture(resourceName), StandardCharsets.UTF_8);
    }

    @Nested
    class GetFileContent {

        @Test
        void shouldReturnFileContentOfPulledFile() throws SftpException {
            var input = fixture("SftpTransfersTest/file-to-pull.txt");
            when(channelSftp.get("test-file-to-pull.txt")).thenReturn(toInputStream(input, StandardCharsets.UTF_8));
            var remoteBasePath = Path.of(config.getRemoteBasePath());

            var fileContent = sftp.getFileContent(remoteBasePath, "test-file-to-pull.txt");

            assertThat(fileContent).isEqualTo(input);

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).get("test-file-to-pull.txt");
        }

        @Test
        void shouldThrowSftpTransfersExceptionWhenGetFileContentFails() throws SftpException {
            doThrow(new SftpException(1, "Test exception")).when(channelSftp).get("test-file-to-pull.txt");
            var remotePath = Path.of(config.getRemoteBasePath());

            assertThatThrownBy(() ->
                    sftp.getFileContent(remotePath, "test-file-to-pull.txt"))
                    .isExactlyInstanceOf(SftpTransfersException.class)
                    .hasMessage("1: Test exception");

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).get("test-file-to-pull.txt");
        }
    }

    @Nested
    class ListFilesAndDirectories {

        @Test
        void shouldReturnAListOfFilesInThePath() throws SftpException {
            var entries = buildEntries();
            when(channelSftp.ls(config.getRemoteBasePath())).thenReturn(entries);

            List<String> files = sftp.listFiles(Path.of(config.getRemoteBasePath()));

            assertThat(files).hasSize(2).contains("test-file-1.txt", "test-file-2.txt");
        }

        @Test
        void shouldReturnAListOfDirectoriesInThePath() throws SftpException {
            var entries = buildEntries();
            when(channelSftp.ls(config.getRemoteBasePath())).thenReturn(entries);

            List<String> files = sftp.listDirectories(Path.of(config.getRemoteBasePath()));

            assertThat(files).hasSize(1).contains("test-dir");
        }

        @Test
        void deleteRemoteFileShouldRemoveFile() throws SftpException {
            sftp.deleteRemoteFile(Path.of(config.getRemoteBasePath()), "test-file-to-pull.txt");

            verify(channelSftp).cd("/tmp");
            verify(channelSftp).rm("test-file-to-pull.txt");
        }
    }

    private Vector<ChannelSftp.LsEntry> buildEntries() {
        var fileEntry1 = mock(ChannelSftp.LsEntry.class);
        var fileEntry2 = mock(ChannelSftp.LsEntry.class);
        var dirEntry1 = mock(ChannelSftp.LsEntry.class);

        var fileAttrs = mock(SftpATTRS.class);
        when(fileAttrs.isDir()).thenReturn(false);

        var dirAttrs = mock(SftpATTRS.class);
        when(dirAttrs.isDir()).thenReturn(true);

        when(fileEntry1.getFilename()).thenReturn("test-file-1.txt");
        when(fileEntry1.getAttrs()).thenReturn(fileAttrs);

        when(fileEntry2.getFilename()).thenReturn("test-file-2.txt");
        when(fileEntry2.getAttrs()).thenReturn(fileAttrs);

        when(dirEntry1.getFilename()).thenReturn("test-dir");
        when(dirEntry1.getAttrs()).thenReturn(dirAttrs);

        return new Vector<>(List.of(fileEntry1, fileEntry2, dirEntry1));
    }

    private Vector<ChannelSftp.LsEntry> buildSubEntries() {
        var fileEntry1 = mock(ChannelSftp.LsEntry.class);
        var fileEntry2 = mock(ChannelSftp.LsEntry.class);

        var fileAttrs = mock(SftpATTRS.class);
        when(fileAttrs.isDir()).thenReturn(false);

        when(fileEntry1.getFilename()).thenReturn("sub-test-file-1.txt");
        when(fileEntry1.getAttrs()).thenReturn(fileAttrs);

        when(fileEntry2.getFilename()).thenReturn("sub-test-file-2.txt");
        when(fileEntry2.getAttrs()).thenReturn(fileAttrs);

        return new Vector<>(List.of(fileEntry1, fileEntry2));
    }
}
