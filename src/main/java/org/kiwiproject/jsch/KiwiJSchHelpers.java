package org.kiwiproject.jsch;

import static com.google.common.base.Preconditions.checkState;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.net.InetAddresses;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.Session;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Optional;

/**
 * Static utilities for working with JSch.
 */
@UtilityClass
public class KiwiJSchHelpers {

    /**
     * Detect the key exchange for a given host (or IP address) and {@link HostKeyRepository}, which is
     * JSch's encapsulation of the {@code known_hosts} file.
     * <p>
     * This method supports only a subset of the possible formats in {@code known_hosts}. The supported formats are:
     * <ul>
     *     <li>{@code hostname}, e.g. {@code dev-svc-1.acme.com}</li>
     *     <li>{@code ip_address}, e.g. {@code 192.168.1.150}</li>
     *     <li>{@code hostname,ip_address}, e,g, {@code dev-svc-1.acme.com,192.168.1.150}</li>
     * </ul>
     * {@code known_hosts} can have various other formats, but we do not support them at the present time.
     * See <a href="https://github.com/kiwiproject/kiwi/discussions/577">this discussion on known_hosts formats</a>
     * for additional information.
     *
     * @param host       the host name or IP to match
     * @param knownHosts the known hosts
     * @return an Optional containing the detected key exchange type, or an empty Optional
     * @implNote Finds the <em>first</em> matching known host entry
     */
    public static Optional<String> detectKeyExchangeTypeForHost(String host, HostKeyRepository knownHosts) {
        checkArgumentNotBlank(host);
        checkArgumentNotNull(knownHosts);

        return Arrays.stream(knownHosts.getHostKey())
                .filter(knownHost -> hostMatchesKnownHost(host, knownHost))
                .map(HostKey::getType)
                .findFirst();
    }

    /**
     * Sets the given key exchange type, e.g. {@code ssh-rsa} or {@code ecdsa-sha2-nistp256} on the specified session.
     *
     * @param session         the JSch session
     * @param keyExchangeType key exchange type
     * @implNote JSch does not appear to have any constants to use, so it's just raw strings
     */
    public static void setSessionKeyExchangeType(Session session, String keyExchangeType) {
        checkArgumentNotNull(session);
        checkArgumentNotBlank(keyExchangeType);
        session.setConfig("server_host_key", keyExchangeType);
    }

    private static boolean hostMatchesKnownHost(String hostOrIpToFind, HostKey hostKey) {
        var knownHost = new KnownHost(hostKey);
        if (InetAddresses.isInetAddress(hostOrIpToFind)) {
            return hostOrIpToFind.equals(knownHost.ipAddress);
        }
        return hostOrIpToFind.equals(knownHost.hostName);
    }

    private static class KnownHost {
        final String hostName;
        final String ipAddress;

        KnownHost(HostKey knownHost) {
            String host = knownHost.getHost();
            if (containsCommaSeparatedHostAndIpAddress(host)) {
                String[] splat = host.split(",");
                checkState(splat.length > 1, "Expecting host key to be in format: hostName,IP");
                hostName = splat[0];
                ipAddress = splat[1];
            } else {
                if (InetAddresses.isInetAddress(host)) {
                    hostName = null;
                    ipAddress = host;
                } else {
                    hostName = host;
                    ipAddress = null;
                }
            }
        }

        private static boolean containsCommaSeparatedHostAndIpAddress(String value) {
            return value.contains(",");
        }
    }
}
