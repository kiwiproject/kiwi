package org.kiwiproject.net;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

/**
 * Utilities for checking sockets.
 */
@Slf4j
public class SocketChecker {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Check whether a {@link Socket} can be opened on the specified host and port.
     *
     * @param host the host to check
     * @param port the port to check
     * @return {@code true} if the port is open; {@code false} otherwise
     */
    public boolean canConnectViaSocket(String host, int port) {
        return canConnectViaSocket(host, port, DEFAULT_TIMEOUT);
    }

    /**
     * Check whether a {@link Socket} can be opened on the specified host and port, with the specified timeout.
     *
     * @param host    the host to check
     * @param port    the port to check
     * @param timeout how long to wait for the socket connection before timing out
     * @return {@code true} if the socket connection succeeded; {@code false} otherwise
     */
    public boolean canConnectViaSocket(String host, int port, Duration timeout) {
        try (var socket = new Socket()) {
            LOG.trace("Checking socket at {}:{} with timeout {} ms", host, port, timeout.toMillis());
            socket.connect(new InetSocketAddress(host, port), Ints.checkedCast(timeout.toMillis()));
            LOG.trace("Socket at {}:{} is available", host, port);
            return true;
        } catch (IOException e) {
            LOG.trace("Socket at {}:{} not available", host, port, e);
            return false;
        }
    }

    /**
     * Check whether a {@link Socket} can be opened on the specified host and port. Uses the default timeout
     * of {@link #DEFAULT_TIMEOUT}.
     *
     * @param hostAndPort a Pair containing the host and port to check
     * @return {@code true} if the socket connection succeeded; {@code false} otherwise
     */
    public boolean canConnectViaSocket(Pair<String, Integer> hostAndPort) {
        return canConnectViaSocket(hostAndPort, DEFAULT_TIMEOUT);
    }

    /**
     * Check whether a {@link Socket} can be opened on the specified host and port, with the specified timeout.
     *
     * @param hostAndPort a Pair containing the host and port to check
     * @param timeout     how long to wait for the socket connection before timing out
     * @return {@code true} if the socket connection succeeded; {@code false} otherwise
     */
    public boolean canConnectViaSocket(Pair<String, Integer> hostAndPort, Duration timeout) {
        var host = hostAndPort.getLeft();
        var port = hostAndPort.getRight();
        return canConnectViaSocket(host, port, timeout);
    }
}
