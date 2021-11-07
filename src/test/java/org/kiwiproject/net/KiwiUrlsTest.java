package org.kiwiproject.net;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.collect.KiwiMaps;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DisplayName("KiwiUrls")
@Slf4j
class KiwiUrlsTest {

    private static final String BAD_URL = "this is not a URL";

    @Test
    void testCreateUrlObject_WhenWellFormed() {
        var url = KiwiUrls.createUrlObject("https://acme.com/foo");
        assertThat(url.getHost()).isEqualTo("acme.com");
    }

    @Test
    void testCreateUrlObject_WhenMalformed() {
        assertThatThrownBy(() -> KiwiUrls.createUrlObject("acme.com/goo"))
                .isExactlyInstanceOf(UncheckedMalformedURLException.class)
                .hasCauseExactlyInstanceOf(MalformedURLException.class);
    }

    @Test
    void testCreateUrl_WhenMalformed() {
        var thrown = catchThrowable(() -> KiwiUrls.createUrl("badprotocol", "some-host", 8000));

        assertThat(thrown).isExactlyInstanceOf(UncheckedMalformedURLException.class)
                .hasMessage("Error constructing URL from protocol [badprotocol], hostname [some-host], port [8000], path []")
                .hasCauseInstanceOf(MalformedURLException.class);
    }

    @Test
    void testCreateUrl_WithNoPath() {
        var url = KiwiUrls.createUrl("http", "localhost", 9090);
        assertThat(url).isEqualTo("http://localhost:9090");
    }

    @Test
    void testCreateUrlObject_WithNoPath() {
        var url = KiwiUrls.createUrlObject("http", "localhost", 9090);
        assertThat(url).hasToString("http://localhost:9090");
    }

    @Test
    void testCreateHttpUrl_WithNoPath() {
        var url = KiwiUrls.createHttpUrl("localhost", 9090);
        assertThat(url).isEqualTo("http://localhost:9090");
    }

    @Test
    void testCreateHttpSecureUrl_WithNoPath() {
        var url = KiwiUrls.createHttpsUrl("localhost", 9090);
        assertThat(url).isEqualTo("https://localhost:9090");
    }

    @Test
    void testCreateUrl_WithPath() {
        var url = KiwiUrls.createUrl("http", "localhost", 9090, "admin/healthcheck");
        assertThat(url).isEqualTo("http://localhost:9090/admin/healthcheck");
    }

    @Test
    void testCreateHttpUrl_WithPath() {
        var url = KiwiUrls.createHttpUrl("localhost", 9090, "admin/healthcheck");
        assertThat(url).isEqualTo("http://localhost:9090/admin/healthcheck");
    }

    @Test
    void testCreateHttpsUrl_WithPath() {
        var url = KiwiUrls.createHttpsUrl("localhost", 9090, "admin/healthcheck");
        assertThat(url).isEqualTo("https://localhost:9090/admin/healthcheck");
    }

    @Nested
    class CreateUrlUsingBaseUrlAndPathComponents {
        private static final String BASE_URI = "https://acme.com:8073";

        @Test
        void testWithOnlySchemeHostPort() {
            assertThat(KiwiUrls.createUrl(BASE_URI)).isEqualTo(BASE_URI);
        }

        @Test
        void testWithOnePathComponent() {
            assertThat(KiwiUrls.createUrl(BASE_URI, "foo")).isEqualTo(BASE_URI + "/foo");
        }

        @Test
        void testWithMultiplePathComponents() {
            assertThat(KiwiUrls.createUrl(BASE_URI, "foo", "/bar/", "/baz"))
                    .isEqualTo(BASE_URI + "/foo/bar/baz");
        }

        @Test
        void testWithMultiplePathComponentsAndQueryString() {
            assertThat(KiwiUrls.createUrl(BASE_URI, "foo", "/bar?option1=A"))
                    .isEqualTo(BASE_URI + "/foo/bar?option1=A");
        }

        @Test
        void testWhenSchemeHostPort_NotAbsolute() {
            assertThatThrownBy(() -> KiwiUrls.createUrl("/foo"))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void testWhenSchemeHostPort_HasInvalidProtocol() {
            assertThatThrownBy(() -> KiwiUrls.createUrl("gndn://foo"))
                    .hasMessage("Error creating URL from: gndn://foo")
                    .isExactlyInstanceOf(UncheckedMalformedURLException.class)
                    .hasCauseExactlyInstanceOf(MalformedURLException.class);

        }
    }

    @Test
    void testExtractAllFrom_WithBadUrl_ReturnsEmptySections() {
        var components = KiwiUrls.extractAllFrom(BAD_URL);

        assertThat(components.getScheme()).isNullOrEmpty();
        assertThat(components.getSubDomainName()).isNullOrEmpty();
        assertThat(components.getDomainName()).isNullOrEmpty();
        assertThat(components.getCanonicalName()).isNullOrEmpty();
        assertThat(components.getPort()).isEmpty();
        assertThat(components.getPath()).isEmpty();
    }

    @Test
    void testExtractAll_TrailingSlashButNoPath() {
        var components = KiwiUrls.extractAllFrom("http://prod-server-8.xxx.prod/");

        assertThat(components.getScheme()).isEqualTo("http");
        assertThat(components.getSubDomainName()).isEqualTo("prod-server-8");
        assertThat(components.getDomainName()).isEqualTo("xxx.prod");
        assertThat(components.getPort()).contains(80);
        assertThat(components.getPath()).isEmpty();
    }

    @Test
    void testExtractAll_WithPort() {
        var components = KiwiUrls.extractAllFrom("http://prod-server-8.xxx.prod:8080/");

        assertThat(components.getScheme()).isEqualTo("http");
        assertThat(components.getSubDomainName()).isEqualTo("prod-server-8");
        assertThat(components.getDomainName()).isEqualTo("xxx.prod");
        assertThat(components.getPort()).contains(8080);
        assertThat(components.getPath()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://news.bbc.co.uk:8080/a-news-article/about-stuff, news.bbc.co.uk",
            "https://news.bbc.co.uk:8080/a-news-article, news.bbc.co.uk",
            "https://news.bbc.co.uk/a-news-article, news.bbc.co.uk",
            "http://news.bbc.co.uk/a-news-article, news.bbc.co.uk",
            "http://news.bbc.co.uk/, news.bbc.co.uk",
            "http://news.bbc.co.uk, news.bbc.co.uk",
            "sftp://news.bbc.co.uk/a-news-article, news.bbc.co.uk",
            "ftp://news.bbc.co.uk/a-news-article, news.bbc.co.uk",
            "http://localhost/some-data, localhost",
            "http://localhost.localdomain/some-data, localhost.localdomain"
    })
    void testExtractCanonicalNameFrom(String url, String expectedName) {
        assertThat(KiwiUrls.extractCanonicalNameFrom(url)).contains(expectedName);
    }

    @Test
    void testExtractCanonicalNameFrom_WithBadUrl() {
        assertThat(KiwiUrls.extractCanonicalNameFrom(BAD_URL)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://news.bbc.co.uk:8080/a-news-article/about-stuff, bbc.co.uk",
            "https://news.bbc.co.uk:8080/a-news-article, bbc.co.uk",
            "https://news.bbc.co.uk/a-news-article, bbc.co.uk",
            "http://news.bbc.co.uk/a-news-article, bbc.co.uk",
            "http://news.bbc.co.uk/, bbc.co.uk",
            "http://news.bbc.co.uk, bbc.co.uk",
            "sftp://news.bbc.co.uk/a-news-article, bbc.co.uk",
            "ftp://news.bbc.co.uk/a-news-article, bbc.co.uk",
            "http://localhost.localdomain/some-data, localdomain"
    })
    void testExtractDomainNameFrom(String url, String expectedName) {
        assertThat(KiwiUrls.extractDomainNameFrom(url)).contains(expectedName);
    }

    @Test
    void testExtractDomainNameFrom_ShouldBeEmpty() {
        assertThat(KiwiUrls.extractDomainNameFrom("http://localhost/some-data")).isEmpty();
        assertThat(KiwiUrls.extractDomainNameFrom(BAD_URL)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://news.bbc.co.uk:8080/a-news-article/about-stuff, /a-news-article/about-stuff",
            "https://news.bbc.co.uk:8080/a-news-article, /a-news-article",
            "https://news.bbc.co.uk/a-news-article, /a-news-article",
            "http://news.bbc.co.uk/a-news-article, /a-news-article",
            "sftp://news.bbc.co.uk/a-news-article, /a-news-article",
            "ftp://news.bbc.co.uk/a-news-article, /a-news-article",
            "http://localhost/some-data, /some-data",
            "http://localhost.localdomain/some-data, /some-data"
    })
    void testExtractPathFrom(String url, String expectedPath) {
        assertThat(KiwiUrls.extractPathFrom(url)).contains(expectedPath);
    }

    @Test
    void testExtractPathFrom_WhenNoPathExists() {
        assertThat(KiwiUrls.extractPathFrom("http://news.bbc.co.uk/")).isEmpty();
        assertThat(KiwiUrls.extractPathFrom("http://news.bbc.co.uk")).isEmpty();
        assertThat(KiwiUrls.extractPathFrom(BAD_URL)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://news.bbc.co.uk:8080/a-news-article/about-stuff, 8080",
            "https://news.bbc.co.uk:8080/a-news-article, 8080",
            "https://news.bbc.co.uk/a-news-article, 443",
            "http://news.bbc.co.uk/a-news-article, 80",
            "http://news.bbc.co.uk/, 80",
            "http://news.bbc.co.uk, 80",
            "sftp://news.bbc.co.uk/a-news-article, 22",
            "ftp://news.bbc.co.uk/a-news-article, 21",
            "http://localhost/some-data, 80",
            "http://localhost.localdomain/some-data, 80"
    })
    void testExtractPortFrom(String url, int port) {
        assertThat(KiwiUrls.extractPortFrom(url)).hasValue(port);
    }

    @Test
    void testExtractPortFrom_WithBadUrl_ShouldBeEmpty() {
        assertThat(KiwiUrls.extractPortFrom(BAD_URL)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "https://news.bbc.co.uk:8080/a-news-article/about-stuff, news",
            "https://news.bbc.co.uk:8080/a-news-article, news",
            "https://news.bbc.co.uk/a-news-article, news",
            "http://news.bbc.co.uk/a-news-article, news",
            "http://news.bbc.co.uk/, news",
            "http://news.bbc.co.uk, news",
            "sftp://news.bbc.co.uk/a-news-article, news",
            "ftp://news.bbc.co.uk/a-news-article, news",
            "http://localhost/some-data, localhost",
            "http://localhost.localdomain/some-data, localhost"
    })
    void testExtractSubDomainNameFrom(String url, String subdomain) {
        assertThat(KiwiUrls.extractSubDomainNameFrom(url)).contains(subdomain);
    }

    @Test
    void testExtractSubDomainNameFrom_WithBadUrl_ShouldBeEmpty() {
        assertThat(KiwiUrls.extractSubDomainNameFrom(BAD_URL)).isEmpty();
    }

    @Test
    void testPrependLeadingSlash_WhenLeadingSlashExists() {
        var original = "/foo/bar";
        assertThat(KiwiUrls.prependLeadingSlash(original)).isEqualTo(original);
    }

    @Test
    void testPrependLeadingSlash_WhenLeadingSlashDoesNotExist() {
        var original = "foo/bar";
        assertThat(KiwiUrls.prependLeadingSlash(original)).isEqualTo("/" + original);
    }

    @Test
    void testPrependLeadingSlash_WhenExtraPadding() {
        var notPadded = "foo/bar";
        var padded = format("    %s    ", notPadded);
        assertThat(KiwiUrls.prependLeadingSlash(padded)).isEqualTo("/" + notPadded);
    }

    @Test
    void testReplaceDomainIn_BiggerDomain() {
        assertThat(KiwiUrls.replaceDomainsIn(
                "http://disc-1-acme.org:8761/eureka,http://disc-2-acme.org:8761/eureka",
                "dev.co"))
                .isEqualTo("http://disc-1-acme.dev.co:8761/eureka,http://disc-2-acme.dev.co:8761/eureka");
    }

    @Test
    void testReplaceDomainIn_SameSizeDomain() {
        assertThat(KiwiUrls.replaceDomainsIn(
                "http://disc-1-acme.prod.co:8761/eureka,http://disc-2-acme.prod.co:8761/eureka",
                "dev.co"))
                .isEqualTo("http://disc-1-acme.dev.co:8761/eureka,http://disc-2-acme.dev.co:8761/eureka");
    }

    @Test
    void testReplaceDomainIn_SmallerDomain() {
        assertThat(KiwiUrls.replaceDomainsIn(
                "http://disc-1-acme.prod.co:8761/eureka,http://disc-2-acme.prod.co:8761/eureka",
                "com"))
                .isEqualTo("http://disc-1-acme.com:8761/eureka,http://disc-2-acme.com:8761/eureka");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost/an-endpoint,https://localhost/another-endpoint",
            "http://localhost/an-endpoint",
            "http://localhost/",
            "http://localhost",
    })
    void testReplaceDomainIn_WithoutDomain_ReturnTheOriginalUrl(String url) {
        assertThat(KiwiUrls.replaceDomainsIn(url, "new.domain.net")).isEqualTo(url);
    }

    @Test
    void testReplaceDomainIn_WhenHostnameContainsDomain() {
        assertThat(KiwiUrls.replaceDomainsIn(
                "http://disc-1-acme.prod.co:8761/eureka,http://disc-2-acme.prod.co:8761/eureka",
                "prod.com"))
                .isEqualTo("http://disc-1-acme.prod.com:8761/eureka,http://disc-2-acme.prod.com:8761/eureka");
    }

    @Test
    void testReplaceDomainIn_WhenThereIsAnExtraTrailingDotInTheHostname_ShouldIgnoreIt() {
        assertThat(KiwiUrls.replaceDomainsIn(
                "http://disc-1-acme.co.:8761/eureka,http://disc-2-acme.co.:8761/eureka",
                "com"))
                .isEqualTo("http://disc-1-acme.com:8761/eureka,http://disc-2-acme.com:8761/eureka");
    }

    @Test
    void testReplaceDomainIn_WhenThereIsSillinessLikeABunchOfCommas_ShouldIgnoreEmptyStrings() {
        assertThat(KiwiUrls.replaceDomainsIn(
                ",,,http://disc-1-acme.co:8761/eureka,,,,,http://disc-2-acme.co:8761/eureka,,,",
                "com"))
                .isEqualTo("http://disc-1-acme.com:8761/eureka,http://disc-2-acme.com:8761/eureka");
    }

    @Test
    void testReplaceDomainIn_WhenThereAreDifferentDomains_ShouldOnlyUseFirst() {
        assertThat(KiwiUrls.replaceDomainsIn(
                "http://disc-1-acme.co:8761/eureka,http://disc-2-acme.org:8761/eureka",
                "com"))
                .isEqualTo("http://disc-1-acme.com:8761/eureka,http://disc-2-acme.org:8761/eureka");
    }

    @Test
    void testStripTrailingSlashes() {
        var originalUrls = List.of(
                "http://localhost:7001",
                "http://localhost:7002/",
                "http://localhost:7003/status/"
        );

        var expectedUrls = List.of(
                "http://localhost:7001",
                "http://localhost:7002",
                "http://localhost:7003/status"
        );

        var newUrls = KiwiUrls.stripTrailingSlashes(originalUrls);

        assertThat(newUrls).isEqualTo(expectedUrls);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "http://localhost:7003/status/, http://localhost:7003/status",
            "http://localhost:7003/status, http://localhost:7003/status",
            "   http://localhost:7003/status   , http://localhost:7003/status"
    })
    void testStripTrailingSlash(String originalUrl, String strippedUrl) {
        assertThat(KiwiUrls.stripTrailingSlash(originalUrl)).isEqualTo(strippedUrl);
    }

    @Test
    void testQueryStringToMap_WhenNullString() {
        assertThat(KiwiUrls.queryStringToMap(null)).isEmpty();
    }

    @Test
    void testQueryStringToMap_WhenEmptyString() {
        assertThat(KiwiUrls.queryStringToMap("")).isEmpty();
    }

    @Test
    void testQueryStringToMap_WhenOneUrlParameter() {
        assertThat(KiwiUrls.queryStringToMap("noop=true")).containsExactly(entry("noop", "true"));
    }

    @Test
    void testQueryStringToMap_WhenMultipleUrlParameter() {
        assertThat(KiwiUrls.queryStringToMap("autoCreate=false&move=.processed&moveFailed=.error&readLock=changed"))
                .containsOnly(
                        entry("autoCreate", "false"),
                        entry("move", ".processed"),
                        entry("moveFailed", ".error"),
                        entry("readLock", "changed")
                );
    }

    @RepeatedTest(10)
    void testQueryStringToMap_OnlyReturnsOneValue_ForParametersWithMultipleValues() {
        var parameters = KiwiUrls.queryStringToMap("topping=pepperoni&topping=banana+pepper&topping=sausage");

        assertThat(parameters).containsExactly(entry("topping", "pepperoni"));
    }

    @Nested
    class ToQueryString {

        @Test
        void shouldReturnEmptyString_WhenNullParameters() {
            assertThat(KiwiUrls.toQueryString(null)).isEmpty();
        }

        @Test
        void shouldReturnEmptyString_WhenEmptyParameters() {
            assertThat(KiwiUrls.toQueryString(new HashMap<>())).isEmpty();
        }

        @Test
        void shouldConvertSingleEntryMap() {
            assertThat(KiwiUrls.toQueryString(Map.of("noop", "true"))).isEqualTo("noop=true");
        }

        @Test
        void shouldConvertMapWithMultipleEntries() {
            assertThat(KiwiUrls.toQueryString(Map.of("noop", "true", "readLock", "changed")).split("&"))
                    .hasSize(2)
                    .containsOnly("noop=true", "readLock=changed");
        }

        @Test
        void shouldConvertNullValuesToStringLiteralNull() {
            var queryString = KiwiUrls.toQueryString(KiwiMaps.newHashMap("key1", null, "key2", null, "key3", "value3"));
            assertThat(queryString.split("&"))
                    .containsOnlyOnce("key1=null", "key2=null", "key3=value3");
        }

        @Test
        void shouldConvertValuesToStrings() {
            var params = Map.of("key1", "value1", "key2", 42, "key3", 84.0, "key4", true, "key5", 'g');
            var queryString = KiwiUrls.toQueryString(params);
            assertThat(queryString.split("&"))
                    .containsOnlyOnce("key1=value1", "key2=42", "key3=84.0", "key4=true", "key5=g");
        }
    }

    @Nested
    class ToEncodedQueryString {

        private Map<String, Object> params;

        @BeforeEach
        void setUp() {
            params = sampleParameters();
        }

        @Test
        void shouldEncodeQueryParameters() {
            var queryString = KiwiUrls.toEncodedQueryString(params);
            assertEncodedQueryParameters(queryString, params);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyString_WhenGivenNullOrEmptyParameters(Map<String, String> parameters) {
            assertThat(KiwiUrls.toEncodedQueryString(parameters)).isEmpty();
        }

        @Test
        void shouldEncodeQueryParameters_UsingGivenCharsetName() {
            var queryString = KiwiUrls.toEncodedQueryString(params, UTF_8.name());
            assertEncodedQueryParameters(queryString, params);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotAllowBlankCharsetName(String charsetName) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiUrls.toEncodedQueryString(params, charsetName))
                    .withMessage("charsetName must not be blank");
        }

        @Test
        void shouldRequireLegalCharsetName() {
            assertThatThrownBy(() -> KiwiUrls.toEncodedQueryString(params, "ILLEGAL CHARSET NAME"))
                    .isExactlyInstanceOf(IllegalCharsetNameException.class);
        }

        @Test
        void shouldRequireSupportedCharsetName() {
            assertThatThrownBy(() -> KiwiUrls.toEncodedQueryString(params, "INVALID_CHARSET"))
                    .isExactlyInstanceOf(UnsupportedCharsetException.class);
        }

        @Test
        void shouldEncodeQueryParameters_UsingGivenCharset() {
            var queryString = KiwiUrls.toEncodedQueryString(params, UTF_8);
            assertEncodedQueryParameters(queryString, params);
        }

        @Test
        void shouldNotAllowNullCharset() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiUrls.toEncodedQueryString(params, (Charset) null))
                    .withMessage("charset must not be null");
        }

        private Map<String, Object> sampleParameters() {
            return Map.of(
                    "key1", "value 1",
                    "key 2", "value$2",
                    "key3", "value%3",
                    "key4", "value+4",
                    "key5", "value_5",
                    "key 6", "value!6",
                    "key7", 42,
                    "key8", 24.0,
                    "key9", false,
                    "key10", 'x'
            );
        }

        private void assertEncodedQueryParameters(String encodedQueryString, Map<String, Object> params) {
            var nameValuePairs = Set.copyOf(KiwiStrings.splitToList(encodedQueryString, '&'));

            var expectedNameValuePairs = params.entrySet()
                    .stream()
                    .map(entry -> urlEncodeParameter(entry.getKey(), entry.getValue()))
                    .peek(parameter -> LOG.debug("expect: {}", parameter))
                    .collect(toUnmodifiableSet());

            assertThat(nameValuePairs).isEqualTo(expectedNameValuePairs);
        }

        private String urlEncodeParameter(String name, Object value) {
            return URLEncoder.encode(name, UTF_8) + "=" + URLEncoder.encode(String.valueOf(value), UTF_8);
        }
    }

}
