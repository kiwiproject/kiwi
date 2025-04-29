package org.kiwiproject.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.validation.ValidationTestHelper.assertNoViolations;
import static org.kiwiproject.validation.ValidationTestHelper.assertOnePropertyViolation;
import static org.kiwiproject.validation.ValidationTestHelper.assertPropertyViolations;

import jakarta.validation.Validator;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.UnknownHostException;

@DisplayName("URLValidator")
@SuppressWarnings("HttpUrlsUsage")
class ValidURLValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = KiwiValidations.getValidator();
    }

    @Nested
    class ShouldBeValid {

        @ParameterizedTest
        @ValueSource(strings = {
                "http://example.com",
                "https://example.com",
                "http://example.com/path",
                "http://example.com/path?query=value",
                "http://example.com/path?query=value#fragment",
                "http://example.com:8080",
                "http://localhost",
                "http://127.0.0.1",
                "http://127.0.0.2",
                "http://127.255.255.255",
                "http://192.168.1.1",
                "https://192.168.1.1",
                "http://10.0.0.1",
                "http://172.16.0.1",
                "http://192.168.1.1:8080",
                "http://192.168.1.1/path",
                "http://192.168.1.1/path?query=value",
                "http://192.168.1.1/path?query=value#fragment",
                "http://user:password@192.168.1.1",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]",
                "https://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]",
                "http://[2001:db8::1]",
                "http://[::1]",
                "http://[2001:db8::1]:8080",
                "http://[2001:db8::1]/path",
                "http://[2001:db8::1]/path?query=value",
                "http://[2001:db8::1]/path?query=value#fragment",
                "http://user:password@example.com",
                "http://example.com/path/to/resource.html",
                "https://example.co.uk",
                "https://subdomain.example.com"
        })
        void whenUrlIsValid(String url) {
            var object = new UrlObject(url);
            assertNoViolations(validator, object);
        }

        @Test
        void whenAllowingNull_AndValueIsNull() {
            var object = new AllowNullUrlObject(null);
            assertNoViolations(validator, object);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ftp://example.com",
                "sftp://example.com",
                "file:///path/to/file",
                "ldap://ldap.example.org"
        })
        void whenAllowingAllSchemes_AndSchemeIsNotHttpOrHttps(String url) {
            var object = new AllowAllSchemesUrlObject(url);
            assertNoViolations(validator, object);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ftp://example.com",
                "sftp://example.com"
        })
        void whenSpecificSchemesAllowed_AndSchemeIsInAllowedList(String url) {
            var object = new SpecificSchemesUrlObject(url);
            assertNoViolations(validator, object);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://example.com/api//users",
                "https://example.com/api//users",
                "http://example.com:9050/api//users",
                "https://example.com:951/api//users",
                "http://10.116.78.128:10050/api//users",
                "https://10.116.78.128:10051/api//users"
        })
        void whenAllowingTwoSlashesInPathComponent(String url) {
            var object = new AllowTwoSlashesUrlObject(url);
            assertNoViolations(validator, object);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://example.com",
                "https://example.com",
                "http://example.com:8000",
                "https://example.com:8001",
                "http://10.116.78.128:9050",
                "https://10.116.78.128:9051",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]",
                "https://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]"
        })
        void whenNotAllowingLocalUrls_AndUrlIsNotLocal(String url) {
            var object = new NotAllowingLocalUrlsObject(url);
            assertNoViolations(validator, object);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "file:///etc/hosts",
                "file:///path/to/file.txt",
        })
        void whenNotAllowingLocalUrls_AndSchemeDoesNotRequireHost(String url) {
            var object = new AllSchemesButNotAllowingLocalUrlsObject(url);
            assertNoViolations(validator, object);
        }
    }

    @Nested
    class ShouldNotBeValid {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                // General invalid URLs
                " ",
                "not-a-url",
                "http",
                "http://",
                "http:/example.com",
                "http:example.com",
                "example.com",
                "://example.com",

                // URLs with schemes other than HTTP or HTTPS (when not allowing all schemes)
                "ftp://example.com",
                "sftp://example.com",
                "file:///path/to/file",
                "mailto:user@example.com",

                // URLs with authority ending with colon but no port
                "http://example.com:",
                "https://example.com:",

                // Invalid IP addresses (IPv4)
                "http://256.0.0.1",
                "http://192.168.1.256",
                "http://192.168.1",
                "http://192.168.1.1.1",
                "http://192.168.1.a",
                "http://192.168.1.-1",
                "http://192.168.1",
                "http://192.168..1",

                // Invalid IP addresses (IPv6)
                "http://2001:0db8:85a3:0000:0000:8a2e:0370:7334",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:73341]",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:733g]",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370]",
                "http://[2001::85a3::7334]",

                // Invalid IP addresses with port
                "http://192.168.1.1:65536",
                "http://192.168.1.1:-1",
                "http://192.168.1.1:port",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:-1",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]:port",

                // URLs with two slashes in the path component
                "http://10.116.56.42/api//users",
                "https://10.116.56.42/api//users",
                "http://example.com/api//users",
                "https://example.com/api//users"
        })
        void whenUrlIsInvalid_ForVariousReasons(String url) {
            var object = new UrlObject(url);
            assertOnePropertyViolation(validator, object, "url");
        }

        @Test
        void whenNotAllowingNull_AndValueIsNull() {
            var object = new UrlObject(null);
            assertPropertyViolations(validator, object, "url", "is not a valid URL");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "ldap://ldap.example.org",
                "news:comp.lang.java"
        })
        void whenSpecificSchemesAllowed_AndSchemeIsNotInAllowedList(String url) {
            var object = new SpecificSchemesUrlObject(url);
            assertOnePropertyViolation(validator, object, "url");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://127.0.0.42",

                "http://localhost",
                "https://localhost",
                "http://localhost:9090",
                "https://localhost:9091",
                "http://localdomain",
                "https://localdomain",
                "http://localdomain:9090",
                "https://localdomain:9091",
                "http://foo",
                "https://foo",
                "http://foo:7070",
                "https://foo:7071",
                "http://foo:7070/bar",
                "https://foo:7071/bar",
                "http://127.0.0.1",
                "https://127.0.0.1",
                "http://127.0.0.1:3000",
                "https://127.0.0.1:3001",
                "http://127.0.0.1:3000/app",
                "https://127.0.0.1:3001/app",
                "http://127.0.0.2",
                "http://127.255.255.255",
                "http://[::1]",
                "https://[::1]",
                "http://[::1]:4000",
                "https://[::1]:4001",
                "http://[::1]:4000/path",
                "https://[::1]:4001/path",
        })
        void whenNotAllowingLocalUrls_AndUrlIsLocal(String url) {
            var object = new NotAllowingLocalUrlsObject(url);
            assertOnePropertyViolation(validator, object, "url");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://example.com#fragment",
                "https://example.com#fragment",
                "http://example.com/path#fragment",
                "https://example.com/path#fragment",
                "http://example.com:9000/path#fragment",
                "https://example.com:9001/path#fragment",
        })
        void whenNotAllowingFragments_AndUrlContainsFragment(String url) {
            var object = new NotAllowingFragmentsUrlObject(url);
            assertOnePropertyViolation(validator, object, "url");

        }
    }

    @Nested
    class DirectMethodTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "http://example.com:",
                "https://example.com:"
        })
        void checkEdgeCases_ShouldReturnFalse_WhenAuthorityEndsWithColonButNoPort(String url) {
            assertThat(ValidURLValidator.checkEdgeCases(url, true)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://example.com",
                "https://example.com:8080",
                "http://127.0.0.1",
                "https://192.168.1.1",
                "http://10.0.0.1:8080",
                "http://172.16.0.1/path",
                "http://[2001:db8::1]",
                "https://[::1]:8080",
                "http://[2001:0db8:85a3:0000:0000:8a2e:0370:7334]/path"
        })
        void checkEdgeCases_ShouldReturnTrue_WhenUrlIsValid(String url) {
            assertThat(ValidURLValidator.checkEdgeCases(url, true)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "http://[invalid",
                "http://example.com/path[invalid"
        })
        void checkEdgeCases_ShouldReturnFalse_WhenUriCannotBeParsed(String url) {
            assertThat(ValidURLValidator.checkEdgeCases(url, true)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "127.0.0.1",
                "127.0.0.2",
                "127.255.255.255",
                "::1",
                "[::1]",
                "localhost"
        })
        void checkLoopbackIpAddress_ShouldBeLoopback_WhenHostIsLoopback(String host) {
            var result = ValidURLValidator.checkLoopbackIpAddress(host);
            assertAll(
                    () -> assertThat(result.isLoopback()).isTrue(),
                    () -> assertThat(result.isLoopbackOrError()).isTrue(),
                    () -> assertThat(result.isError()).isFalse(),
                    () -> assertThat(result.error()).isNull()
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "foo",
                "bar"
        })
        void checkLoopbackIpAddress_ShouldBeError_WhenThrowsException(String host) {
            var result = ValidURLValidator.checkLoopbackIpAddress(host);
            assertAll(
                    () -> assertThat(result.isLoopback()).isFalse(),
                    () -> assertThat(result.isLoopbackOrError()).isTrue(),
                    () -> assertThat(result.isError()).isTrue(),
                    () -> assertThat(result.error()).isExactlyInstanceOf(UnknownHostException.class)
            );
        }
    }

    @Value
    private static class UrlObject {
        @ValidURL
        String url;
    }

    @Value
    private static class AllowNullUrlObject {
        @ValidURL(allowNull = true)
        String url;
    }

    @Value
    private static class AllowAllSchemesUrlObject {
        @ValidURL(allowAllSchemes = true)
        String url;
    }

    @Value
    private static class SpecificSchemesUrlObject {
        @ValidURL(allowSchemes = { "http", "https", "ftp", "sftp" })
        String url;
    }

    @Value
    private static class NotAllowingLocalUrlsObject {
        @ValidURL(allowLocalUrls = false)
        String url;
    }

    @Value
    private static class AllSchemesButNotAllowingLocalUrlsObject {
        @ValidURL(allowAllSchemes = true, allowLocalUrls = false)
        String url;
    }

    @Value
    private static class AllowTwoSlashesUrlObject {
        @ValidURL(allowTwoSlashes = true)
        String url;
    }

    @Value
    private static class NotAllowingFragmentsUrlObject {
        @ValidURL(allowFragments = false)
        String url;
    }
}
