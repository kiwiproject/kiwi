package org.kiwiproject.jsch;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple wrapper around a {@link JSch} instance that handles connecting and disconnecting using the
 * configuration specified in an {@link SftpConfig}.
 *
 * @implNote This requires JSch being available at runtime.
 */
@Slf4j
public class SftpConnector {

    private static final String SFTP_NOT_CONNECTED = "Sftp is not connected. Call connect first";

    private final SftpConfig config;
    private final JSch jsch;
    private Session session;
    private ChannelSftp sftpChannel;

    static {
        JSch.setLogger(new JSchSlf4jLogger(LOG));
    }

    /**
     * Construct using the given {@link SftpConfig}.
     * <p>
     * The instance is <em>not</em> connected; call {@link #connect()} to open a connection.
     *
     * @param config the SFTP configuration
     */
    public SftpConnector(SftpConfig config) {
        this(new JSch(), config);
    }

    /**
     * Construct using the given {@link JSch} and {@link SftpConfig}.
     * <p>
     * The instance is <em>not</em> connected; call {@link #connect()} to open a connection.
     *
     * @param jsch   the {@link JSch} instance to use
     * @param config the SFTP configuration
     */
    public SftpConnector(JSch jsch, SftpConfig config) {
        this.config = requireNotNull(config, "SftpConfig is required");
        this.jsch = requireNotNull(jsch, "JSch is required");
    }

    /**
     * Creates a new connector and immediately opens a new connection.
     *
     * @param config The configuration used for setting up the connection
     * @return The initialized and opened sftp connection
     * @apiNote This is a convenience method to not have to call connect after initialization.
     */
    public static SftpConnector setupAndOpenConnection(SftpConfig config) {
        return setupAndOpenConnection(new JSch(), config);
    }

    @VisibleForTesting
    static SftpConnector setupAndOpenConnection(JSch jsch, SftpConfig config) {
        var connector = new SftpConnector(jsch, config);
        connector.connect();
        return connector;
    }

    /**
     * Opens a connection to the remote SFTP server.
     * <p>
     * Applies the following configurations:
     * <ul>
     *     <li>Known hosts ({@link JSch#setKnownHosts(String)})</li>
     *     <li>User, host, and port (to create a {@link Session} via {@link JSch#getSession(String, String, int)})</li>
     *     <li>Session timeout ({@link Session#setTimeout(int)}</li>
     *     <li>PreferredAuthentications (via {@link Session#setConfig(String, String)})</li>
     *     <li>Key exchange type (detected from known hosts, see {@link KiwiJSchHelpers}</li>
     *     <li>Private key or password ({@link JSch#addIdentity(String)} or {@link Session#setPassword(String)}</li>
     *     <li>Enable/disable StrictHostKeyChecking (via {@link Session#setConfig(String, String)}, default is enabled)</li>
     * </ul>
     */
    public void connect() {
        try {
            LOG.trace("Entering connect()");

            LOG.trace("Setting known hosts to {}", config.getKnownHostsFile());
            jsch.setKnownHosts(config.getKnownHostsFile());

            LOG.trace("Creating JSch session; connecting to: {}@{}:{}",
                    config.getUser(), config.getHost(), config.getPort());
            session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());

            LOG.trace("Setting timeout to {} milliseconds", config.getTimeout().toMilliseconds());
            session.setTimeout(Ints.checkedCast(config.getTimeout().toMilliseconds()));

            LOG.trace("Setting preferred authentications to: {}", config.getPreferredAuthentications());
            session.setConfig("PreferredAuthentications", config.getPreferredAuthentications());

            LOG.trace("Detecting key exchange type with host");
            KiwiJSchHelpers.detectKeyExchangeTypeForHost(config.getHost(),
                    jsch.getHostKeyRepository())
                    .ifPresentOrElse(
                            keyExchangeType -> setSessionKeyExchangeType(session, keyExchangeType),
                            () -> LOG.trace("Did not detect key exchange type for host: {}", config.getHost()));

            addAuthToSession();

            disableStrictHostKeyCheckingIfConfigured();

            LOG.debug("Attempt session connect using timeout: {} millis", session.getTimeout());
            session.connect();

            LOG.debug("Session connected: {}", session.isConnected());
            Channel channel = session.openChannel("sftp");

            LOG.debug("Attempt openChannel using timeout: {} millis", config.getTimeout().toMilliseconds());
            channel.connect(Ints.checkedCast(config.getTimeout().toMilliseconds()));

            LOG.debug("Channel connected: {}", channel.isConnected());

            checkState(channel instanceof ChannelSftp,
                    "Expected channel to be a ChannelSftp, but was a: %s", channel.getClass());
            sftpChannel = (ChannelSftp) channel;
            LOG.trace("Ready sftpChannel: {}", sftpChannel);
        } catch (JSchException ex) {
            throw new SftpTransfersException("Error occurred connecting to " + config.getHost(), ex);
        }
    }

    private void disableStrictHostKeyCheckingIfConfigured() {
        disableStrictHostKeyCheckingIfConfigured(config, session);
    }

    @VisibleForTesting
    static void disableStrictHostKeyCheckingIfConfigured(SftpConfig config, Session session) {
        if (config.isDisableStrictHostChecking()) {
            LOG.warn("Disabling strict host checking - This should only be used for testing purposes!");
            session.setConfig("StrictHostKeyChecking", "no");
        }
    }

    /**
     * Closes and cleans up the connection to the remote SFTP server.
     * <p>
     * Once disconnected, calling this method again will have no effect unless {@link #connect()} is called again.
     */
    public void disconnect() {
        if (nonNull(sftpChannel)) {
            sftpChannel.disconnect();
            sftpChannel = null;
        }

        if (nonNull(session)) {
            session.disconnect();
            session = null;
        }
    }

    void runCommand(ThrowingConsumer<ChannelSftp, Exception> consumer) {
        validateSftpIsConnected();
        try {
            consumer.accept(sftpChannel);
        } catch (Exception e) {
            throw new SftpTransfersException(e);
        }
    }

    <T> T runCommandWithResponse(ThrowingFunction<ChannelSftp, T, Exception> function) {
        validateSftpIsConnected();
        try {
            return function.apply(sftpChannel);
        } catch (Exception e) {
            throw new SftpTransfersException(e);
        }
    }

    private void validateSftpIsConnected() {
        checkState(nonNull(sftpChannel), SFTP_NOT_CONNECTED);
        checkState(sftpChannel.isConnected(), SFTP_NOT_CONNECTED);
    }

    private void setSessionKeyExchangeType(Session session, String keyExchangeType) {
        KiwiJSchHelpers.setSessionKeyExchangeType(session, keyExchangeType);
        LOG.debug("Set key exchange type [{}] for host {}", keyExchangeType, config.getHost());
    }

    private void addAuthToSession() throws JSchException {
        addAuthToSession(config, jsch, session);
    }

    @VisibleForTesting
    static void addAuthToSession(SftpConfig config, JSch jsch, Session session) throws JSchException {
        if (isNotBlank(config.getPrivateKeyFilePath())) {
            LOG.debug("Using private key '{}' to connect", config.getPrivateKeyFilePath());
            jsch.addIdentity(config.getPrivateKeyFilePath());

        } else if (isNotBlank(config.getPassword())) {
            LOG.debug("Using password to connect");
            session.setPassword(config.getPassword());

        } else {
            throw new SftpTransfersException("Missing a private key and a password; cannot authenticate to the SFTP server");
        }
    }
}
