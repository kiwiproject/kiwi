package org.kiwiproject.jsch;

import static java.util.stream.Collectors.toList;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A simple wrapper around a {@link JSch} instance that handles some basic SFTP operations.
 *
 * @implNote This requires JSch being available at runtime.
 */
@Slf4j
public class SftpTransfers {

    private final SftpConnector connector;

    static {
        JSch.setLogger(new JSchSlf4jLogger(LOG));
    }

    public SftpTransfers(SftpConnector connector) {
        this.connector = connector;
    }

    /**
     * Connects to the remote SFTP server.
     *
     * @apiNote This is a convenience method that delegates to the internal {@link SftpConnector#connect()}
     */
    public void connect() {
        connector.connect();
    }

    /**
     * Disconnects from the remote SFTP server.
     *
     * @apiNote This is a convenience method that delegates to the internal {@link SftpConnector#disconnect()}
     */
    public void disconnect() {
        connector.disconnect();
    }

    /**
     * Pushes a stream of data to a remote SFTP server in the given path and with the given filename.
     *
     * @param remotePath the path on the remote server where the file will be created
     * @param filename   the filename to give the file on the remote server
     * @param data       the stream of data to write to the remote server
     */
    public void putFile(Path remotePath, String filename, InputStream data) {
        connector.runCommand(channel -> {
            changeOrCreateRemoteDirectory(channel, remotePath);
            channel.put(data, filename);
        });
    }

    /**
     * Will change the remote directory or create it if it doesn't exist.
     *
     * @implNote One way to determine if the directory already exists is to attempt to change to it. If an exception
     * is thrown, catch it and attempt to create the directory and then change to it. There doesn't seem to be any
     * (obvious) way to check existence in JSch otherwise.
     */
    private static void changeOrCreateRemoteDirectory(ChannelSftp channel, Path path) throws SftpException {
        try {
            changeToRemoteDirectory(channel, path);
        } catch (SftpException e) {
            LOG.debug("Directory {} did not exist. Will create it", path, e);
            channel.mkdir(path.toString());
            changeToRemoteDirectory(channel, path);
        }
    }

    /**
     * Recursively gets files off of a remote server starting in the given path and stores the files locally in the
     * given path and given filename. The local path will be determined through the given {@code BiFunction} supplier
     * which is provided the current remote path and current remote filename. The local filename will be determined
     * through the given {@code BiFunction} supplier which is provided the remote path and remote filename.
     *
     * @param remotePath            path on the remote server where the file is located
     * @param localPathSupplier     supplier that calculates the path on the local machine where the file will be written
     * @param localFilenameSupplier supplier that calculates the name of the file that will be written locally
     */
    public void getAndStoreAllFiles(Path remotePath,
                                    BiFunction<Path, String, Path> localPathSupplier,
                                    BiFunction<Path, String, String> localFilenameSupplier) {

        // First store off the files
        listFiles(remotePath).forEach(filename -> getAndStoreFile(remotePath, localPathSupplier, filename, localFilenameSupplier));

        // Recursively go through the directories
        listDirectories(remotePath).forEach(directory ->
                getAndStoreAllFiles(remotePath.resolve(directory), localPathSupplier, localFilenameSupplier));
    }

    /**
     * Gets a file off of a remote server in the given path and with the given filename and stores the file locally in
     * a given path and the original (remote) filename.
     *
     * @param remotePath path on the remote server where the file is located
     * @param localPath  path on the local machine where the file will be written
     * @param filename   name of the file to pull from the remote server (This name is used as the local file name)
     */
    public void getAndStoreFile(Path remotePath, Path localPath, String filename) {
        getAndStoreFile(remotePath, localPath, filename, filename);
    }

    /**
     * Gets a file off of a remote server in the given path and with the given filename and stores the file locally
     * in a given path and the given filename.
     *
     * @param remotePath     path on the remote server where the file is located
     * @param localPath      path on the local machine where the file will be written
     * @param remoteFilename name of the file to pull from the remote server
     * @param localFilename  name of the file that will be written locally
     */
    public void getAndStoreFile(Path remotePath, Path localPath, String remoteFilename, String localFilename) {
        getAndStoreFile(remotePath, (rPath, rFile) -> localPath, remoteFilename, (rPath, rFile) -> localFilename);
    }

    /**
     * Gets a file off of a remote server in the given path and with the given filename and stores the file locally in
     * a given path and the given filename. The local path will be determined through the given {@code BiFunction}
     * supplier which is provided the remote path and remote filename. The local filename will be determined through
     * the given {@code BiFunction} supplier which is provided the remote path and remote filename.
     *
     * @param remotePath            path on the remote server where the file is located
     * @param localPathSupplier     supplier that calculates the path on the local machine where the file will be written
     * @param remoteFilename        name of the file to pull from the remote server
     * @param localFilenameSupplier supplier that calculates the name of the file that will be written locally
     */
    public void getAndStoreFile(Path remotePath,
                                BiFunction<Path, String, Path> localPathSupplier,
                                String remoteFilename,
                                BiFunction<Path, String, String> localFilenameSupplier) {

        connector.runCommand(channel -> {
            changeToRemoteDirectory(channel, remotePath);

            Path localPath = localPathSupplier.apply(remotePath, remoteFilename);
            ensureLocalDirectoryExists(localPath);

            try (InputStream inputStream = channel.get(remoteFilename)) {
                var resolvedLocalPath = localPath.resolve(localFilenameSupplier.apply(remotePath, remoteFilename));
                Files.copy(inputStream, resolvedLocalPath, StandardCopyOption.REPLACE_EXISTING);
            }
        });
    }

    private static void ensureLocalDirectoryExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            LOG.debug("Local storage directory {} doesn't exist. Creating.", path);
            Files.createDirectories(path);
        }
    }

    /**
     * Gets a file off of a remote server with the given path and given filename and returns the contents of the file
     * as a {@code String}.
     *
     * @param remotePath     path on the remote server where the file is located
     * @param remoteFilename name of the file to pull from the remote server
     * @return contents of the retrieved file as a {@code String}
     */
    public String getFileContent(Path remotePath, String remoteFilename) {
        return connector.runCommandWithResponse(channel -> {
            changeToRemoteDirectory(channel, remotePath);

            try (InputStream inputStream = channel.get(remoteFilename)) {
                var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                var stringWriter = new StringWriter();
                streamReader.transferTo(stringWriter);
                return stringWriter.toString();
            }
        });
    }

    /**
     * Returns a list of files that exist in the given path on the remote server.
     *
     * @param remotePath path on the remote server to list files
     * @return a list of filenames that exist in the given path
     */
    public List<String> listFiles(Path remotePath) {
        return listRemoteItems(remotePath, file -> !file.getAttrs().isDir());
    }

    /**
     * Returns a list of directories that exist in the given path on the remote server.
     *
     * @param remotePath path on the remote server to list directories
     * @return a list of directories that exist in the given path
     */
    public List<String> listDirectories(Path remotePath) {
        return listRemoteItems(remotePath, file -> file.getAttrs().isDir());
    }

    private List<String> listRemoteItems(Path remotePath, Predicate<ChannelSftp.LsEntry> filterFunction) {
        return connector.runCommandWithResponse(channel -> ls(channel, remotePath)
                .stream()
                .filter(filterFunction)
                .map(ChannelSftp.LsEntry::getFilename)
                .collect(toList()));
    }

    // Suppress Sonar warning about using Vectors. JSch returns a raw Vector which we cannot do anything
    // about, and this is a private method, so it's not worth worrying about.
    @SuppressWarnings({"unchecked", "java:S1149"})
    private static Vector<ChannelSftp.LsEntry> ls(ChannelSftp channel, Path remotePath) throws SftpException {
        return channel.ls(remotePath.toString());
    }

    /**
     * Deletes a given file from the given path on a remote server.
     *
     * @param remotePath     path on the remote server where the file is located
     * @param remoteFilename name of the file to delete from the remote server
     */
    public void deleteRemoteFile(Path remotePath, String remoteFilename) {
        connector.runCommand(channel -> {
            changeToRemoteDirectory(channel, remotePath);
            channel.rm(remoteFilename);
        });
    }

    private static void changeToRemoteDirectory(ChannelSftp channel, Path path) throws SftpException {
        LOG.debug("Attempting to change to {} on the remote host", path);
        channel.cd(path.toString());
        LOG.debug("Successfully changed directory on the remote host");
    }
}
