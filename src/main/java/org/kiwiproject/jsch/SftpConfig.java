package org.kiwiproject.jsch;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.PortRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.beans.ConstructorProperties;
import java.util.concurrent.TimeUnit;

/**
 * This (Dropwizard) config class allows for common configuration for SFTP access to remote hosts.
 * <p>
 * One note on the {@code password} and {@code privateKeyFilePath} fields: the SFTP connector will use either the
 * password or the private key but not both. If both are specified, the private key will take precedence. This allows
 * for the greatest flexibility when connecting to different SFTP locations.
 *
 * @implNote This uses Dropwizard's {@link Duration} and several Dropwizard validation annotations in addition to the
 * standard Java Beans Validation annotations, so you will need to ensure the appropriate Dropwizard JAR files are
 * on the class/module path.
 */
@Getter
@Setter
@NoArgsConstructor
@Builder
public class SftpConfig {

    private static final int DEFAULT_PORT = 22;
    private static final String DEFAULT_PREFERRED_AUTHENTICATIONS = "publickey,password";
    private static final Duration DEFAULT_TIMEOUT = Duration.seconds(5);

    /**
     * The SFTP port. Default is 22.
     */
    @PortRange
    private int port = DEFAULT_PORT;

    /**
     * The remote host to connect to via SFTP.
     */
    @NotBlank
    private String host;

    /**
     * The remote user to connect with via SFTP.
     */
    @NotBlank
    private String user;

    /**
     * The password, only used if password authentication is used.
     */
    private String password;

    /**
     * The path to the private key file, only used if public key authentication is used.
     */
    private String privateKeyFilePath;

    /**
     * The comma-separated list of preferred authentication mechanisms. Equivalent to the
     * {@code -o PreferredAuthentications=[gssapi-with-mic|hostbased|publickey|keyboard-interactive|password]} option.
     * <p>
     * The default is {@code publickey,password} which differs from the default order in the SFTP command. See
     * the {@code ssh_info} man page for details.
     */
    @NotBlank
    private String preferredAuthentications = DEFAULT_PREFERRED_AUTHENTICATIONS;

    /**
     * The root directory of the remote SFTP location, provided as a convenience to store the remote path in the same
     * place as the other SFTP properties.
     * <p>
     * It is not required nor currently used by {@link SftpConnector} or {@link SftpTransfers}.
     */
    private String remoteBasePath;

    /**
     * The local directory to write out any errors if SFTP fails.
     */
    @NotBlank
    private String errorPath;

    /**
     * The path to the known hosts file.
     */
    @NotBlank
    private String knownHostsFile;

    /**
     * Provides an option to disable strict host key checking, equivalent to the
     * {@code -o StrictHostKeyChecking=[yes|no]} option.
     * <p>
     * The default is false. See the {@code ssh_info} man page for more details.
     */
    private boolean disableStrictHostChecking;

    /**
     * SFTP connection timeout. Default is 5 seconds.
     *
     * @implNote The minimum duration annotation of 50 milliseconds is somewhat arbitrary, and is really just intended
     * to ensure a positive value.
     */
    @NotNull
    @MinDuration(value = 50, unit = TimeUnit.MILLISECONDS)
    private Duration timeout = DEFAULT_TIMEOUT;

    /**
     * All-args constructor.
     *
     * @param port                      the remote port
     * @param host                      the remote host
     * @param user                      the SFTP username
     * @param password                  the SFTP password
     * @param privateKeyFilePath        path to the private key file
     * @param preferredAuthentications  comma-separated list of authentications to attempt
     * @param remoteBasePath            root directory of the remote SFTP location
     * @param errorPath                 local directory to write out any errors if SFTP fails
     * @param knownHostsFile            path to the known hosts file
     * @param disableStrictHostChecking if true, we will set {@code StrictHostKeyChecking=no}
     * @param timeout                   the SFTP connection timeout
     * @implNote This is intentionally not using Lombok because using {@link lombok.AllArgsConstructor} together
     * with @{@link lombok.Builder} results in an all-args constructor that does not respect {@link lombok.Builder.Default}.
     * As a result, we need to handle the defaults ourselves. This is intended to be used during deserialization
     * from an external configuration file (e.g., a Dropwizard YAML configuration file, or from JSON). Prefer the
     * builder when constructing programmatically.
     */
    // Suppress Sonar's "Methods should not have too many parameters". We need this for the builder and for
    // deserialization from YAML or JSON configurations.
    @SuppressWarnings({"java:S107"})
    @ConstructorProperties({
            "port", "host", "user", "password",
            "privateKeyFilePath", "preferredAuthentications",
            "remoteBasePath", "errorPath", "knownHostsFile",
            "disableStrictHostChecking", "timeout"
    })
    public SftpConfig(int port,
                      String host,
                      String user,
                      String password,
                      String privateKeyFilePath,
                      String preferredAuthentications,
                      String remoteBasePath,
                      String errorPath,
                      String knownHostsFile,
                      boolean disableStrictHostChecking,
                      Duration timeout) {

        this.port = (port == 0) ? DEFAULT_PORT : port;
        this.host = host;
        this.user = user;
        this.password = password;
        this.privateKeyFilePath = privateKeyFilePath;
        this.preferredAuthentications =
                isBlank(preferredAuthentications) ? DEFAULT_PREFERRED_AUTHENTICATIONS : preferredAuthentications;
        this.remoteBasePath = remoteBasePath;
        this.errorPath = errorPath;
        this.knownHostsFile = knownHostsFile;
        this.disableStrictHostChecking = disableStrictHostChecking;
        this.timeout = isNull(timeout) ? DEFAULT_TIMEOUT : timeout;
    }
}
