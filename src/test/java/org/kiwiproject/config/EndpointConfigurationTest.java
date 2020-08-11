package org.kiwiproject.config;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@DisplayName("EndpointConfiguration")
class EndpointConfigurationTest {

    @Nested
    class Builder {

        @Test
        void shouldThrowIllegalStateException_IfBuildEndpointCalled_WhenNoParent() {
            assertThatThrownBy(() ->
                    EndpointConfiguration.builder()
                            .buildEndpoint()
                            .build())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessageStartingWith("The parent SecureEndpointsConfiguration is null");
        }
    }

    @Nested
    class UrlRewriteConfigurationProperty {

        @Test
        void shouldDefaultToNone() {
            var config = new EndpointConfiguration();

            assertThat(config.getUrlRewriteConfiguration().getPathPrefix()).isNull();
            assertThat(config.getUrlRewriteConfiguration().shouldRewrite()).isFalse();
        }
    }

    @Nested
    class GetUris {

        @Nested
        class ShouldReturnExpectedValues {

            @Test
            void whenNoUrlRewritingIsConfigured() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain0.test")
                        .port("443")
                        .path("path")
                        .build();

                var expectedUri = "https://domain0.test:443/path";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenUrlRewritingIsConfigured() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain1.test")
                        .port("443")
                        .path("path")
                        .urlRewriteConfiguration(UrlRewriteConfiguration.builder()
                                .pathPrefix("/some-proxy/")
                                .build())
                        .build();

                var expectedUri = "https://domain1.test:443/some-proxy/path";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenLeadingAndTrailingSlashesInPath() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain2.test")
                        .port("8443")
                        .path("/path/")
                        .build();

                var expectedUri = "https://domain2.test:8443/path";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenTrailingSlashesInDomainAndPath() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain3.test/")
                        .port("8443")
                        .path("path/")
                        .build();

                var expectedUri = "https://domain3.test:8443/path";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenNoDomain() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .path("path")
                        .build();

                assertThatThrownBy(config::getURI)
                        .isExactlyInstanceOf(IllegalStateException.class)
                        .hasMessage("No domains have been set on this endpoint!");
            }

            @Test
            void whenNoPath() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain4.test")
                        .port("443")
                        .build();

                var expectedUri = "https://domain4.test:443";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenNoPort() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain5.test")
                        .path("/path")
                        .build();

                var expectedUri = "https://domain5.test/path";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenNoPathOrPort() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain6.test")
                        .build();

                var expectedUri = "https://domain6.test";
                assertExpectedUris(config, expectedUri);
            }

            @Test
            void whenNonNumericPort() {
                assertThatThrownBy(() ->
                        EndpointConfiguration.builder()
                                .scheme("https")
                                .domain("domain7.test")
                                .port("foobar")
                                .path("path")
                                .build())
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("port should contain only digits: foobar");
            }

            private void assertExpectedUris(EndpointConfiguration config, String expectedUri) {
                assertThat(config.getURI()).isEqualTo(expectedUri);

                var expectedUriObject = URI.create(expectedUri);
                assertThat(config.getUriObject()).isEqualTo(expectedUriObject);
                assertThat(config.toUriBuilder().build()).isEqualTo(expectedUriObject);
            }
        }

        @Nested
        class WithMultipleDomains {

            @Test
            void shouldRoundRobinTwoDomains() {
                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain("domain-1.test,domain-2.test")
                        .port("443")
                        .path("path")
                        .build();

                assertThat(config.getURI()).isEqualTo("https://domain-1.test:443/path");
                assertThat(config.getUriObject()).hasToString("https://domain-2.test:443/path");

                assertThat(config.getURI())
                        .describedAs("should have reset back to first domain")
                        .isEqualTo("https://domain-1.test:443/path");
                assertThat(config.toUriBuilder().build()).hasToString("https://domain-2.test:443/path");

                assertThat(config.getUriObject())
                        .describedAs("should have reset back to first domain again")
                        .hasToString("https://domain-1.test:443/path");
            }

            @Test
            void shouldRoundRobinManyDomains() {
                final var numberOfDomains = 5;

                var domainCsv = IntStream.rangeClosed(1, numberOfDomains)
                        .mapToObj(number -> "domain-" + number + ".test")
                        .collect(joining(","));
                LOG.trace("domains: {}", domainCsv);

                var config = EndpointConfiguration.builder()
                        .scheme("https")
                        .domain(domainCsv)
                        .port("443")
                        .path("path")
                        .build();

                var domainNumbers = Stream.iterate(1, value -> value + 1)
                        .limit(numberOfDomains)
                        .collect(toList());
                var cyclingIterable = Iterables.cycle(domainNumbers);

                StreamSupport.stream(cyclingIterable.spliterator(), /* parallel */ false)
                        .limit(numberOfDomains * 10)
                        .forEach(value -> {
                            var expectedUri = f("https://domain-{}.test:443/path", value);
                            LOG.trace("Expecting next config.getURI() to return: {}", expectedUri);
                            assertThat(config.getURI()).isEqualTo(expectedUri);
                        });
            }
        }
    }

    @Nested
    class SetDomain {

        private EndpointConfiguration config;

        @BeforeEach
        void setUp() {
            config = new EndpointConfiguration();
        }

        @Test
        void withSingleDomain() {
            config.setDomain("domain-1.test");

            assertThat(config.getDomain()).isEqualTo("domain-1.test");
            assertThat(config.getDomains()).containsExactly("domain-1.test");
        }

        @Test
        void withNoSpacesBetweenDomains() {
            config.setDomain("domain-1.test,domain-2.test,domain-3.test");

            assertThat(config.getDomain()).isEqualTo("domain-1.test,domain-2.test,domain-3.test");
            assertThat(config.getDomains()).containsExactly("domain-1.test", "domain-2.test", "domain-3.test");
        }

        @Test
        void withWhitespaceInDomains() {
            var domainCsv = " domain-1.test , domain-2.test , domain-3.test ";
            config.setDomain(domainCsv);

            assertThat(config.getDomain()).isEqualTo(domainCsv);
            assertThat(config.getDomains()).containsExactly("domain-1.test", "domain-2.test", "domain-3.test");
        }

        @Test
        void withExtraWhitespaceAndCommasInDomains() {
            var domainCsv = " domain-1.test ,, ,,, domain-2.test ,, domain-3.test ,,,";
            config.setDomain(domainCsv);

            assertThat(config.getDomain()).isEqualTo(domainCsv);
            assertThat(config.getDomains()).containsExactly("domain-1.test", "domain-2.test", "domain-3.test");
        }
    }

    @Nested
    class IsSecure {

        @ParameterizedTest
        @ValueSource(strings = {"https", "wss"})
        void shouldBeTrue_ForSecureSchemes(String scheme) {
            var config = EndpointConfiguration.builder().scheme(scheme).build();

            assertThat(config.isSecure()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"http", "ws"})
        void shouldBeFalse_ForNonSecureSchemes(String scheme) {
            var config = EndpointConfiguration.builder().scheme(scheme).build();

            assertThat(config.isSecure()).isFalse();
        }
    }

    /**
     * @implNote These were added after we saw ArrayIndexOutOfBoundsException occurring due to concurrent access.
     * They do not test the round-robin (that is done in other tests) but instead ensures that we don't see any
     * errors when there are multiple threads requesting the URIs concurrently.
     */
    @Nested
    class MultipleDomainConcurrencyTests {

        private static final int NUM_TIMES = 1_000;

        private EndpointConfiguration config;
        private AtomicInteger errorCount;

        private ExecutorService executorService;
        private CompletionService<String> completionService;

        @BeforeEach
        void setUp() {
            errorCount = new AtomicInteger();
            executorService = Executors.newFixedThreadPool(5);
            completionService = new ExecutorCompletionService<>(executorService);
        }

        @AfterEach
        void tearDown() {
            executorService.shutdownNow();
        }

        @Nested
        class ShouldGetUriFromMultipleThreads {

            @RepeatedTest(100)
            void whenSingleDomain() {
                config = newEndpointConfigurationWithDomain("domain-1.test");

                submitConcurrentCallsAndAssertZeroFailures(List.of("https://domain-1.test/path"));
            }

            @RepeatedTest(100)
            void whenMultipleDomains() {
                config = newEndpointConfigurationWithDomain("domain-1.test,domain-2.test");

                submitConcurrentCallsAndAssertZeroFailures(
                        List.of("https://domain-1.test/path", "https://domain-2.test/path"));
            }
        }

        private EndpointConfiguration newEndpointConfigurationWithDomain(String domain) {
            return EndpointConfiguration.builder()
                    .scheme("https")
                    .domain(domain)
                    .path("path")
                    .build();
        }

        private void submitConcurrentCallsAndAssertZeroFailures(List<String> validResults) {
            for (int i = 0; i < NUM_TIMES; i++) {
                completionService.submit(() -> config.getURI());
            }

            for (int i = 0; i < NUM_TIMES; i++) {
                try {
                    var uri = completionService.take().get();
                    assertThat(validResults).contains(uri);
                } catch (InterruptedException e) {
                    fail("InterruptedException taking or getting next URI", e);
                } catch (ExecutionException e) {
                    LOG.error("ExecutionException taking or getting next URI", e);
                    errorCount.incrementAndGet();
                }
            }

            assertThat(errorCount)
                    .describedAs("should have zero errors")
                    .hasValue(0);
        }
    }

}