package org.kiwiproject.net;

import static com.google.common.base.Preconditions.checkArgument;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Utility to check port availability.
 */
@Slf4j
public class LocalPortChecker {

    public static final int MAX_PORT = 65_535;

    /**
     * Check port availability.
     *
     * @param port the port to check on the local machine
     * @return {@code true} if the port is available; {@code false} otherwise
     */
    public boolean isPortAvailable(int port) {
        try (var serverSocket = new ServerSocket(port); var dataSocket = new DatagramSocket(port)) {
            serverSocket.setReuseAddress(true);
            dataSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            LOG.trace("Error occurred checking port availability for port {}", port, e);
            return false;
        }
    }

    /**
     * Find the first open port above the given port (i.e. if port is 1024 then the first port to be
     * checked will be 1025). If none are found return an empty {@link OptionalInt}
     *
     * @param port the port to check above
     * @return an optional containing the first open port, or an empty optional
     */
    public OptionalInt findFirstOpenPortAbove(int port) {
        checkArgument(port >= 0 && port < MAX_PORT, "Invalid start port: %s", port);

        return IntStream.rangeClosed(port + 1, MAX_PORT)
                .filter(this::isPortAvailable)
                .findFirst();
    }
}
