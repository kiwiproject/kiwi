package org.kiwiproject.net;

import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS;
import static org.kiwiproject.net.LocalPortChecker.MAX_PORT;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

@DisplayName("SocketChecker")
class SocketCheckerTest {

    private SocketChecker socketChecker;
    private LocalPortChecker portChecker;

    @BeforeEach
    void setUp() {
        socketChecker = new SocketChecker();
        portChecker = new LocalPortChecker();
    }

    @Test
    void testCanConnectViaSocket_WhenCanConnect_WithDefaultTimeout() throws InterruptedException {
        var port = portChecker.findFirstOpenPortAbove(20_000)
                .orElseThrow(() -> new IllegalStateException("Cannot find any open ports"));

        var server = new StupidSimpleServer();
        server.runAcceptOnlyOnceServer(port);

        assertThat(socketChecker.canConnectViaSocket("localhost", port)).isTrue();
    }

    @Test
    void testCanConnectViaSocket_WhenCanConnect_AndMightOrMightNotTimeOut() throws InterruptedException {
        var port = portChecker.findFirstOpenPortAbove(25_000)
                .orElseThrow(() -> new IllegalStateException("Cannot find any open ports"));

        var server = new StupidSimpleServer();
        server.runAcceptOnlyOnceServer(port);

        assertThatCode(() -> socketChecker.canConnectViaSocket(Pair.of("localhost", port), Duration.ofMillis(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void testCanConnectViaSocket_WhenCannotConnect() {
        var closedPort = findFirstClosedPort();
        assertThat(socketChecker.canConnectViaSocket(Pair.of("localhost", closedPort))).isFalse();
    }

    private int findFirstClosedPort() {
        return IntStream.rangeClosed(1024, MAX_PORT)
                .filter(portChecker::isPortAvailable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Did not find any closed ports"));
    }


    @Slf4j
    private static class StupidSimpleServer {

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        void runAcceptOnlyOnceServer(int port) throws InterruptedException {
            var server = new Thread(() -> {
                try (var serverSocket = new ServerSocket(port)) {
                    countDownLatch.countDown();
                    serverSocket.accept();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            server.setName("stupid-simple-server-thread");
            server.setUncaughtExceptionHandler((thread, throwable) -> {
                LOG.warn("Uncaught exception in thread {}", thread.getName(), throwable);
            });
            server.start();

            var countedDown = countDownLatch.await(5, SECONDS);
            LOG.info("CountDownLatch reached zero before timeout: {}", countedDown);

            checkState(countedDown, "Timed out before latch count reached zero");
        }
    }
}
