package org.kiwiproject.jaxrs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@DisplayName("KiwiJerseyClients")
class KiwiJerseyClientsTest {

    private static final long TOO_BIG_TIMEOUT = 1L + Integer.MAX_VALUE;

    private Client client;

    @BeforeEach
    void setUp() {
        client = JerseyClientBuilder.createClient();
    }

    @Nested
    class ConnectTimeout {

        @Nested
        class DropwizardDuration {

            @Test
            void shouldNotAllowNullArgument() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.connectTimeout(client, (io.dropwizard.util.Duration) null));
            }

            @Test
            void shouldNotAllowTimeoutsExceedingMaxInteger() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.connectTimeout(client, io.dropwizard.util.Duration.milliseconds(TOO_BIG_TIMEOUT)));
            }

            @Test
            void shouldSetConnectTimeout() {
                var updatedClient = KiwiJerseyClients.connectTimeout(client, io.dropwizard.util.Duration.seconds(5));

                assertTimeout(client, updatedClient, ClientProperties.CONNECT_TIMEOUT, 5_000);
            }
        }

        @Nested
        class JavaDuration {

            @Test
            void shouldNotAllowNullArgument() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.connectTimeout(client, (Duration) null));
            }

            @Test
            void shouldNotAllowTimeoutsExceedingMaxInteger() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.connectTimeout(client, Duration.ofMillis(TOO_BIG_TIMEOUT)));
            }

            @Test
            void shouldSetConnectTimeout() {
                var updatedClient = KiwiJerseyClients.connectTimeout(client, Duration.ofSeconds(10));

                assertTimeout(client, updatedClient, ClientProperties.CONNECT_TIMEOUT, 10_000);
            }
        }

        @Nested
        class JavaLong {

            @Test
            void shouldNotAllowTimeoutsExceedingMaxInteger() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.connectTimeout(client, TOO_BIG_TIMEOUT));
            }

            @Test
            void shouldSetConnectTimeout() {
                var timeout = 3_500L;
                var updatedClient = KiwiJerseyClients.connectTimeout(client, timeout);

                assertTimeout(client, updatedClient, ClientProperties.CONNECT_TIMEOUT, (int) timeout);
            }
        }

        @Nested
        class JavaInt {

            @Test
            void shouldAcceptMaxIntegerAsTimeout() {
                assertThatCode(() -> KiwiJerseyClients.connectTimeout(client, Integer.MAX_VALUE))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldSetConnectTimeout() {
                var timeout = 4_200;
                var updatedClient = KiwiJerseyClients.connectTimeout(client, timeout);

                assertTimeout(client, updatedClient, ClientProperties.CONNECT_TIMEOUT, timeout);
            }
        }
    }

    @Nested
    class ReadTimeout {

        @Nested
        class DropwizardDuration {

            @Test
            void shouldNotAllowNullArgument() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.readTimeout(client, (io.dropwizard.util.Duration) null));
            }

            @Test
            void shouldNotAllowTimeoutsExceedingMaxInteger() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.readTimeout(client, io.dropwizard.util.Duration.milliseconds(TOO_BIG_TIMEOUT)));
            }

            @Test
            void shouldSetReadTimeout() {
                var updatedClient = KiwiJerseyClients.readTimeout(client, io.dropwizard.util.Duration.seconds(7));

                assertTimeout(client, updatedClient, ClientProperties.READ_TIMEOUT, 7_000);
            }
        }

        @Nested
        class JavaDuration {

            @Test
            void shouldNotAllowNullArgument() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.readTimeout(client, (Duration) null));
            }

            @Test
            void shouldNotAllowTimeoutsExceedingMaxInteger() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.readTimeout(client, Duration.ofMillis(TOO_BIG_TIMEOUT)));
            }

            @Test
            void shouldSetReadTimeout() {
                var updatedClient = KiwiJerseyClients.readTimeout(client, Duration.ofSeconds(3));

                assertTimeout(client, updatedClient, ClientProperties.READ_TIMEOUT, 3_000);
            }
        }

        @Nested
        class JavaLong {

            @Test
            void shouldNotAllowTimeoutsExceedingMaxInteger() {
                assertThatIllegalArgumentException().isThrownBy(() ->
                        KiwiJerseyClients.readTimeout(client, TOO_BIG_TIMEOUT));
            }

            @Test
            void shouldSetReadTimeout() {
                var timeout = 1_500L;
                var updatedClient = KiwiJerseyClients.readTimeout(client, timeout);

                assertTimeout(client, updatedClient, ClientProperties.READ_TIMEOUT, (int) timeout);
            }
        }

        @Nested
        class JavaInt {

            @Test
            void shouldAcceptMaxIntegerAsTimeout() {
                assertThatCode(() -> KiwiJerseyClients.readTimeout(client, Integer.MAX_VALUE))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldSetReadTimeout() {
                var timeout = 4_000;
                var updatedClient = KiwiJerseyClients.readTimeout(client, timeout);

                assertTimeout(client, updatedClient, ClientProperties.READ_TIMEOUT, timeout);
            }
        }
    }

    @Nested
    class CheckTimeout {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.jaxrs.client.KiwiJerseyClientsTest#invalidTimeouts")
        void shouldThrowIllegalArgumentExceptionForTimeoutsOverMaxInteger(long timeout) {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiJerseyClients.checkTimeout(timeout))
                    .withMessageStartingWith("timeout must be convertible to an int but %d is more than Integer.MAX_VALUE", timeout);
        }
    }

    @SuppressWarnings("unused")
    private static Stream<Long> invalidTimeouts() {
        return Stream.generate(() -> (long) Integer.MAX_VALUE + randomPositiveInt())
                .limit(25);
    }

    private static int randomPositiveInt() {
        return ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
    }

    private static void assertTimeout(Client originalClient, Client updatedClient, String timeoutProperty, int expectedTimeout) {
        assertThat(updatedClient)
                .describedAs("updatedClient should be same instance as originalClient")
                .isSameAs(originalClient);

        var actualTimeout = updatedClient.getConfiguration().getProperty(timeoutProperty);
        assertThat(actualTimeout)
                .describedAs("%s was not the expected value", timeoutProperty)
                .isEqualTo(expectedTimeout);
    }
}
