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
     * Check port availability. Note specifically that zero is not a valid port for the reasons described
     * <a href="https://www.grc.com/port_0.htm">here</a> and <a href="https://www.lifewire.com/port-0-in-tcp-and-udp-818145">here</a>.
     *
     * @param port the port to check on the local machine
     * @return {@code true} if the port is available; {@code false} otherwise
     * @throws IllegalArgumentException if port is not valid
     */
    public boolean isPortAvailable(int port) {
        checkArgument(port > 0 && port <= MAX_PORT, "Invalid port: %s", port);

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
     * Find the first open port starting at the given port inclusive, i.e. if {@code port} is 1024 then it is the first
     * port checked. If no available port is found, return an empty {@link OptionalInt}.
     *
     * @param port the port to start from
     * @return an optional containing the first open port, or an empty optional
     * @throws IllegalArgumentException if port is not between 1 and {@link #MAX_PORT}
     * @implNote Since this method looks for availability of a specific port, zero is invalid because it is a reserved
     * port used to indicate a random port and is never bound directly to.
     */
    public OptionalInt findFirstOpenPortFrom(int port) {
        checkArgument(port > 0 && port <= MAX_PORT, "Invalid start port: %s", port);

        var startPort = port - 1;
        return findFirstOpenPortAbove(startPort);
    }

    /**
     * Find the first open port above the given port (i.e. if port is 1024 then the first port to be
     * checked will be 1025). If no available port is found, return an empty {@link OptionalInt}.
     *
     * @param port the port to check above
     * @return an optional containing the first open port, or an empty optional
     * @throws IllegalArgumentException if port is not between 0 and ({@link #MAX_PORT} - 1)
     */
    public OptionalInt findFirstOpenPortAbove(int port) {
        checkArgument(port >= 0 && port < MAX_PORT, "Invalid start port: %s", port);

        return IntStream.rangeClosed(port + 1, MAX_PORT)
                .filter(this::isPortAvailable)
                .findFirst();
    }
}
