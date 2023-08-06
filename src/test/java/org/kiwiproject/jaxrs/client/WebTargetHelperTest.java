package org.kiwiproject.jaxrs.client;

import static com.google.common.collect.Lists.newArrayList;
import static jakarta.ws.rs.Priorities.AUTHORIZATION;
import static jakarta.ws.rs.Priorities.ENTITY_CODER;
import static jakarta.ws.rs.Priorities.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.jaxrs.client.WebTargetHelper.withWebTarget;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.client.filter.CsrfProtectionFilter;
import org.glassfish.jersey.client.filter.EncodingFeature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.collect.KiwiMaps;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@DisplayName("WebTargetHelper")
class WebTargetHelperTest {

    private static Client client;

    private WebTarget originalWebTarget;

    @BeforeAll
    static void beforeAll() {
        client = ClientBuilder.newClient();
    }

    @BeforeEach
    void setUp() {
        originalWebTarget = client.target("/path");
    }

    @AfterAll
    static void afterAll() {
        client.close();
    }

    @Nested
    class WithWebTarget {

        @Test
        void shouldThrow_WhenGivenNullWebTarget() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> withWebTarget(null))
                    .withMessage("webTarget must not be null");
        }
    }

    @Nested
    class ToWebTarget {

        @Test
        void shouldReturnNewWebTargetInstance() {
            var webTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = webTargetHelper.toWebTarget();

            assertThat(newWebTarget).isNotSameAs(webTargetHelper.wrapped());
        }

        @Test
        void shouldReturnEqualWebTargetInstance() {
            var webTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = webTargetHelper.toWebTarget();

            assertThat(newWebTarget.getUri()).isEqualTo(webTargetHelper.wrapped().getUri());
        }

        @Test
        void shouldIncludeCurrentStateOfHelper() {
            var webTargetHelper = withWebTarget(originalWebTarget)
                    .queryParamIfNotBlank("name", "Robert")
                    .queryParamIfNotBlank("ssn", "")
                    .queryParamFilterNotBlank("nicknames", "Bob", "Bobby", "Roberto", null, "", " ");

            var newWebTarget = webTargetHelper.toWebTarget();

            var uri = newWebTarget.getUri();
            assertThat(uri)
                    .isEqualTo(webTargetHelper.wrapped().getUri())
                    .hasPath("/path")
                    .hasNoParameter("ssn")
                    .hasParameter("name", "Robert")
                    .hasParameter("nicknames", "Bob")
                    .hasParameter("nicknames", "Bobby")
                    .hasParameter("nicknames", "Roberto");
        }
    }

    @Nested
    class QueryParamRequireNotNull {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrow_WhenGivenNullOrEmptyName(String name) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> withWebTarget(originalWebTarget).queryParamRequireNotNull(name, "a value"))
                    .withMessage("name cannot be blank");
        }

        @Test
        void shouldThrow_WhenGivenNullValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            withWebTarget(originalWebTarget)
                                    .queryParamRequireNotNull("foo", 42)
                                    .queryParamRequireNotNull("bar", null)
                                    .queryParamRequireNotNull("baz", null))
                    .withMessage("value cannot be null for parameter bar");

            // NOTE: Only the first null value encountered will be reported, since there is
            // no easy and clean way to accumulate errors that I can see. Thus, only the 'bar'
            // parameter is reported in the exception.
        }

        @Test
        void shouldAddNonNullValues() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamRequireNotNull("foo", 42)
                    .queryParamRequireNotNull("bar", 84)
                    .queryParamRequireNotNull("baz", "glomp");

            assertThat(newWebTarget.getUri()).hasQuery("foo=42&bar=84&baz=glomp");
        }
    }

    @Nested
    class QueryParamIfNotNull {

        @Test
        void shouldReturnSameInstanceWithNoQuery_WhenGivenNullOrEmptyName() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamIfNotNull("", "value 1")
                    .queryParamIfNotNull(" ", "value 2")
                    .queryParamIfNotNull(null, "value 3");

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldReturnSameInstanceWithNoQuery_WhenGivenOnlyNullValues() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamIfNotNull("foo", null)
                    .queryParamIfNotNull("bar", null)
                    .queryParamIfNotNull("baz", null);

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldAddNonNullValues() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamIfNotNull("q", "pangram")
                    .queryParamIfNotNull("page", 42)
                    .queryParamIfNotNull("limit", 25)
                    .queryParamIfNotNull("sort", null)
                    .queryParamIfNotNull("sortDir", null)
                    .queryParamIfNotNull("", "en/US");

            assertThat(newWebTarget.getUri()).hasQuery("q=pangram&page=42&limit=25");
        }
    }

    @Nested
    class QueryParamFilterNotNull {

        @Nested
        class WhenArray {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNameIsBlank(String name) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull(name, 1, 2, 3);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNullOrEmpty(Object[] values) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("foo", values);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @Test
            void shouldIncludeOnlyNonNullValues() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("lottoNumbers", 42, 84, null, null, 252);

                assertThat(newWebTarget.getUri()).hasQuery("lottoNumbers=42&lottoNumbers=84&lottoNumbers=252");
            }
        }

        @Nested
        class WhenList {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNameIsBlank(String name) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull(name, List.of(1, 2, 3));

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNullOrEmpty(List<Object> values) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("foo", values);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @Test
            void shouldIncludeOnlyNonNullValues() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("lottoNumbers", newArrayList(42, 84, null, null, 252));

                assertThat(newWebTarget.getUri()).hasQuery("lottoNumbers=42&lottoNumbers=84&lottoNumbers=252");
            }
        }

        @Nested
        class WhenStream {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNameIsBlank(String name) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull(name, Stream.of(1, 2, 3));

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @ParameterizedTest
            @NullSource
            void shouldReturnSameInstanceWithNoQuery_WhenNull(Stream<Object> values) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("foo", values);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @Test
            void shouldReturnNewInstance_WhenEmpty() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("foo", Stream.of());

                assertNotOriginalWebTargetAndNoQuery(newWebTarget);
            }

            @Test
            void shouldIncludeOnlyNonNullValues() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotNull("lottoNumbers", Stream.of(42, 84, null, null, 252));

                assertThat(newWebTarget.getUri()).hasQuery("lottoNumbers=42&lottoNumbers=84&lottoNumbers=252");
            }
        }
    }

    @Nested
    class QueryParamRequireNotBlank {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrow_WhenGivenNullOrEmptyName(String name) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> withWebTarget(originalWebTarget).queryParamRequireNotBlank(name, "a value"))
                    .withMessage("name cannot be blank");
        }

        @Test
        void shouldThrow_WhenGivenNullValue() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            withWebTarget(originalWebTarget)
                                    .queryParamRequireNotBlank("foo", "42")
                                    .queryParamRequireNotBlank("bar", "")
                                    .queryParamRequireNotBlank("baz", ""))
                    .withMessage("value cannot be blank for parameter bar");

            // NOTE: Only the first null value encountered will be reported, since there is
            // no easy and clean way to accumulate errors that I can see. Thus, only the 'bar'
            // parameter is reported in the exception.
        }

        @Test
        void shouldAddNonNullValues() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamRequireNotBlank("foo", "42")
                    .queryParamRequireNotBlank("bar", "84")
                    .queryParamRequireNotBlank("baz", "glomp");

            assertThat(newWebTarget.getUri()).hasQuery("foo=42&bar=84&baz=glomp");
        }
    }

    @Nested
    class QueryParamIfNotBlank {

        @Test
        void shouldReturnSameInstanceWithNoQuery_WhenGivenNullOrEmptyName() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamIfNotBlank("", "value 1")
                    .queryParamIfNotBlank(" ", "value 2")
                    .queryParamIfNotBlank(null, "value 3");

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldReturnSameInstance_WhenGivenBlank() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamIfNotBlank("foo", "")
                    .queryParamIfNotBlank("bar", " ")
                    .queryParamIfNotBlank("misc", null)
                    .queryParamIfNotBlank("", "a value")
                    .queryParamIfNotBlank(null, "another value")
                    .queryParamIfNotBlank("baz", "\t  \n");

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldAddNonBlankValues() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamIfNotBlank("q", "what is a pangram")
                    .queryParamIfNotBlank("client", "")
                    .queryParamIfNotBlank("lang", "")
                    .queryParamIfNotBlank("misc", null)
                    .queryParamIfNotBlank("sort", "relevance")
                    .queryParamIfNotBlank("sortDir", "asc");

            assertThat(newWebTarget.getUri()).hasQuery("q=what+is+a+pangram&sort=relevance&sortDir=asc");
        }
    }

    @Nested
    class QueryParamFilterNotBlank {

        @Nested
        class WhenArray {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNameIsBlank(String name) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank(name, "a", "b", "c");

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstance_WhenNullOrEmpty(String[] values) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("foo", values);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @Test
            void shouldIncludeOnlyNonBlankValues() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("lottoNumbers",
                                "42", "", "84", null, "  ", null, "252", "\t  \n");

                assertThat(newWebTarget.getUri()).hasQuery("lottoNumbers=42&lottoNumbers=84&lottoNumbers=252");
            }
        }

        @Nested
        class WhenList {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNameIsBlank(String name) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank(name, List.of("a", "b", "c"));

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstance_WhenNullOrEmpty(List<String> values) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("foo", values);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @Test
            void shouldIncludeOnlyNonBlankValues() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("lottoNumbers",
                                newArrayList("42", "", "84", null, "  ", null, "252", "\t  \n"));

                assertThat(newWebTarget.getUri()).hasQuery("lottoNumbers=42&lottoNumbers=84&lottoNumbers=252");
            }
        }

        @Nested
        class WhenStream {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSameInstanceWithNoQuery_WhenNameIsBlank(String name) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank(name, Stream.of("a", "b", "c"));

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @ParameterizedTest
            @NullSource
            void shouldReturnSameInstance_WhenNull(Stream<String> values) {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("foo", values);

                assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
            }

            @Test
            void shouldReturnNewInstance_WhenEmpty() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("foo", Stream.of());

                assertNotOriginalWebTargetAndNoQuery(newWebTarget);
            }

            @Test
            void shouldIncludeOnlyNonBlankValues() {
                var newWebTarget = withWebTarget(originalWebTarget)
                        .queryParamFilterNotBlank("lottoNumbers",
                                Stream.of("42", "", "84", null, "  ", null, "252", "\t  \n"));

                assertThat(newWebTarget.getUri()).hasQuery("lottoNumbers=42&lottoNumbers=84&lottoNumbers=252");
            }
        }
    }

    @Nested
    class QueryParamsFromMap {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnSameInstance_WhenNullOrEmpty(Map<String, Object> parameters) {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamsFromMap(parameters);

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldReturnSameInstance_WhenAllValuesAreNull() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamsFromMap(KiwiMaps.newHashMap(
                            "foo", null,
                            "bar", null,
                            "baz", null));

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldIncludeOnlyNonNullValues() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamsFromMap(KiwiMaps.newHashMap(
                            "foo", null,
                            "q", "pangram",
                            "bar", null,
                            "baz", null,
                            "limit", 50,
                            "page", 1));

            // Cannot assume anything about map iteration order
            assertThat(splitQueryParams(newWebTarget))
                    .contains("q=pangram")
                    .contains("limit=50")
                    .contains("page=1")
                    .doesNotContain("foo=", "bar=", "baz=");
        }

        /**
         * Why is this test here? We do not want to make any assumptions about whether a caller
         * intends to exclude parameters whose values are blank strings, since query parameters
         * can be blank. For example, in the query string "{@code q=pangram&sort=&dir=&limit=10}" the
         * "sort" and "dir" parameters have no value, which might be perfectly valid. If a client
         * wants to exclude blank values, it should filter them before calling queryParamsFromMap.
         */
        @Test
        void shouldIncludeBlankStrings() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamsFromMap(KiwiMaps.newHashMap(
                            "foo", "",
                            "q", "pangram",
                            "bar", "",
                            "baz", " ",
                            "limit", 50,
                            "page", 1));

            assertThat(splitQueryParams(newWebTarget))
                    .contains("foo=")
                    .contains("q=pangram")
                    .contains("bar=")
                    .contains("baz=+")
                    .contains("limit=50")
                    .contains("page=1");
        }

        @Nested
        class ShouldWorkWithTypedMaps {

            @Test
            void whenStringValues() {
                Map<String, String> params = Map.of(
                        "q", "pangram",
                        "limit", "50",
                        "page", "1");

                var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMap(params);

                assertThat(splitQueryParams(newWebTarget))
                        .contains("q=pangram")
                        .contains("limit=50")
                        .contains("page=1");
            }

            @Test
            void whenIntegerValues() {
                Map<String, Integer> params = Map.of(
                        "a", 42,
                        "b", 16,
                        "c", 8
                );

                var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMap(params);

                assertThat(splitQueryParams(newWebTarget))
                        .contains("a=42")
                        .contains("b=16")
                        .contains("c=8");
            }
        }
    }

    @Nested
    class QueryParamsFromMultivaluedMap {

        @ParameterizedTest
        @NullSource
        void shouldReturnSameInstance_WhenNull(MultivaluedMap<String, Object> parameters) {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamsFromMultivaluedMap(parameters);

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldReturnSameInstance_WhenEmpty() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParamsFromMultivaluedMap(new MultivaluedHashMap<>());

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldReturnSameInstance_WhenAllValuesAreNull() {
            var multivaluedMap = new MultivaluedHashMap<String, Object>();
            multivaluedMap.put("ham", null);
            multivaluedMap.put("eggs", null);
            multivaluedMap.put("spam", null);

            var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMultivaluedMap(multivaluedMap);

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldReturnSameInstance_WhenAllValuesAreEmptyLists() {
            var multivaluedMap = new MultivaluedHashMap<String, Object>();
            multivaluedMap.put("ham", List.of());
            multivaluedMap.put("eggs", List.of());
            multivaluedMap.put("spam", List.of());

            var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMultivaluedMap(multivaluedMap);

            assertIsOriginalWebTargetAndHasNoQuery(newWebTarget);
        }

        @Test
        void shouldIncludeOnlyNonNullValues() {
            var multivaluedMap = new MultivaluedHashMap<String, Object>();
            multivaluedMap.put("ln", List.of(42, 84));
            multivaluedMap.put("fruits", List.of("apple", "orange", "banana"));
            multivaluedMap.put("foo", List.of());
            multivaluedMap.put("bar", null);
            multivaluedMap.put("baz", newArrayList(null, null));
            multivaluedMap.put("l", List.of("EN"));
            multivaluedMap.put("cc", List.of("US", "UK"));

            var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMultivaluedMap(multivaluedMap);

            // Cannot assume anything about map iteration order
            assertThat(splitQueryParams(newWebTarget))
                    .contains("ln=42")
                    .contains("ln=84")
                    .contains("fruits=apple")
                    .contains("fruits=orange")
                    .contains("fruits=banana")
                    .contains("l=EN")
                    .contains("cc=US")
                    .contains("cc=UK")
                    .doesNotContain("foo=", "bar=", "baz=");
        }

        /**
         * Why is this test here? See explanation above in {@link QueryParamsFromMap}
         */
        @Test
        void shouldIncludeBlankStrings() {
            var multivaluedMap = new MultivaluedHashMap<String, Object>();
            multivaluedMap.put("ln", List.of(42, 84));
            multivaluedMap.put("misc", List.of("", " ", "  "));

            var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMultivaluedMap(multivaluedMap);

            assertThat(splitQueryParams(newWebTarget))
                    .contains("ln=42")
                    .contains("ln=84")
                    .contains("misc=")
                    .contains("misc=+")
                    .contains("misc=++");
        }

        @Nested
        class ShouldWorkWithTypedMaps {

            @Test
            void whenStringValues() {
                var multivaluedMap = new MultivaluedHashMap<String, String>();
                multivaluedMap.put("l", List.of("a", "b", "c"));
                multivaluedMap.putSingle("m", "foo");
                multivaluedMap.put("n", List.of("32", "42", "52"));

                var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMultivaluedMap(multivaluedMap);

                assertThat(splitQueryParams(newWebTarget))
                        .contains("l=a")
                        .contains("l=b")
                        .contains("l=c")
                        .contains("m=foo")
                        .contains("n=32")
                        .contains("n=42")
                        .contains("n=52");
            }

            @Test
            void whenLongValues() {
                var multivaluedMap = new MultivaluedHashMap<String, Long>();
                multivaluedMap.put("x", List.of(12L, 34L));
                multivaluedMap.put("y", List.of(700L, 800L, 900L));
                multivaluedMap.putSingle("z", 5000L);

                var newWebTarget = withWebTarget(originalWebTarget).queryParamsFromMultivaluedMap(multivaluedMap);

                assertThat(splitQueryParams(newWebTarget))
                        .contains("x=12")
                        .contains("x=34")
                        .contains("y=700")
                        .contains("y=800")
                        .contains("y=900")
                        .contains("z=5000");
            }
        }
    }

    @Nested
    class ShouldDelegateTo {

        @Test
        void getUri() {
            var uri = withWebTarget(originalWebTarget).getUri();

            assertThat(uri).isEqualTo(originalWebTarget.getUri());
        }

        @Test
        void getUriBuilder() {
            var uriBuilder = withWebTarget(originalWebTarget).getUriBuilder();

            assertThat(uriBuilder.build()).isEqualTo(originalWebTarget.getUriBuilder().build());
        }

        @Test
        void path() {
            var newWebTarget = withWebTarget(originalWebTarget).path("/more");

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);
            assertThat(newWebTarget.getUri()).isEqualTo(originalWebTarget.path("/more").getUri());
        }

        @Test
        void resolveTemplate_Name_Value() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .path("more/{someParam}")
                    .resolveTemplate("someParam", "theValue");

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .path("more/{someParam}")
                    .resolveTemplate("someParam", "theValue")
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void resolveTemplate_Name_Value_EncodeSlashInPath(boolean encodeSlashInPath) {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .path("more/{someParam}")
                    .resolveTemplate("someParam", "theValue/theSubValue", encodeSlashInPath);

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .path("more/{someParam}")
                    .resolveTemplate("someParam", "theValue/theSubValue", encodeSlashInPath)
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @Test
        void resolveTemplateFromEncoded_Name_Value() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .path("more/{moreValue}")
                    .resolveTemplateFromEncoded("moreValue", "the%Value%foo");

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .path("more/{moreValue}")
                    .resolveTemplateFromEncoded("moreValue", "the%Value%foo")
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @Test
        void resolveTemplates_TemplateValues() {
            Map<String, Object> templateValues = Map.of(
                    "moreValue", "1",
                    "evenMoreValue", "2",
                    "andSomeMoreValue", "3"
            );

            var newWebTarget = withWebTarget(originalWebTarget)
                    .path("more/{moreValue}")
                    .path("evenMore/{evenMoreValue}")
                    .path("andSomeMore/{andSomeMoreValue}")
                    .resolveTemplates(templateValues);

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .path("more/{moreValue}")
                    .path("evenMore/{evenMoreValue}")
                    .path("andSomeMore/{andSomeMoreValue}")
                    .resolveTemplates(templateValues)
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }


        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void resolveTemplates_TemplateValues_EncodeSlashInPath(boolean encodeSlashInPath) {
            Map<String, Object> templateValues = Map.of(
                    "moreValue", "val/1",
                    "evenMoreValue", "val/2",
                    "andSomeMoreValue", "val/3"
            );

            var newWebTarget = withWebTarget(originalWebTarget)
                    .path("more/{moreValue}")
                    .path("evenMore/{evenMoreValue}")
                    .path("andSomeMore/{andSomeMoreValue}")
                    .resolveTemplates(templateValues, encodeSlashInPath);

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .path("more/{moreValue}")
                    .path("evenMore/{evenMoreValue}")
                    .path("andSomeMore/{andSomeMoreValue}")
                    .resolveTemplates(templateValues, encodeSlashInPath)
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @Test
        void resolveTemplatesFromEncoded_TemplateValues() {
            Map<String, Object> templateValues = Map.of(
                    "moreValue", "val%1",
                    "evenMoreValue", "val%2",
                    "andSomeMoreValue", "val%3"
            );

            var newWebTarget = withWebTarget(originalWebTarget)
                    .path("more/{moreValue}")
                    .path("evenMore/{evenMoreValue}")
                    .path("andSomeMore/{andSomeMoreValue}")
                    .resolveTemplatesFromEncoded(templateValues);

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .path("more/{moreValue}")
                    .path("evenMore/{evenMoreValue}")
                    .path("andSomeMore/{andSomeMoreValue}")
                    .resolveTemplatesFromEncoded(templateValues)
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @Test
        void matrixParam() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .matrixParam("p1", "a", "b", "c")
                    .matrixParam("p2", 1, 3, 5);

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .matrixParam("p1", "a", "b", "c")
                    .matrixParam("p2", 1, 3, 5)
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @Test
        void queryParam() {
            var newWebTarget = withWebTarget(originalWebTarget)
                    .queryParam("p1", "a", "b", "c")
                    .queryParam("p2", 1, 3, 5)
                    .queryParam("p3", 42);

            assertThat(newWebTarget).isNotSameAs(originalWebTarget);

            var expectedUri = originalWebTarget
                    .queryParam("p1", "a", "b", "c")
                    .queryParam("p2", 1, 3, 5)
                    .queryParam("p3", 42)
                    .getUri();
            assertThat(newWebTarget.getUri()).isEqualTo(expectedUri);
        }

        @Test
        void request() {
            var invocationBuilder = withWebTarget(originalWebTarget).request();
            assertThat(invocationBuilder).isNotNull();
        }

        @Test
        void request_StringAcceptedResponseTypes() {
            var invocationBuilder = withWebTarget(originalWebTarget)
                    .request("application/json", "text/xml", "application/xml");
            assertThat(invocationBuilder).isNotNull();
        }

        @Test
        void request_MediaTypeAcceptedResponseTypes() {
            var invocationBuilder = withWebTarget(originalWebTarget)
                    .request(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_XML_TYPE, MediaType.APPLICATION_XML_TYPE);
            assertThat(invocationBuilder).isNotNull();
        }

        @Test
        void getConfiguration() {
            var config = withWebTarget(originalWebTarget).getConfiguration();

            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
        }

        @Test
        void property() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .property("p1", "v1")
                    .property("p2", "v2")
                    .property("p3", "v3");

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.getProperty("p1")).isEqualTo("v1");
            assertThat(config.getProperty("p2")).isEqualTo("v2");
            assertThat(config.getProperty("p3")).isEqualTo("v3");
        }

        @Test
        void register_ClassComponent() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(CsrfProtectionFilter.class)
                    .register(EncodingFeature.class);

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(CsrfProtectionFilter.class)).isTrue();
            assertThat(config.isRegistered(EncodingFeature.class)).isTrue();
        }

        @Test
        void register_ClassComponent_IntPriority() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(CsrfProtectionFilter.class, AUTHORIZATION)
                    .register(EncodingFeature.class, ENTITY_CODER);

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(CsrfProtectionFilter.class)).isTrue();
            assertThat(config.isRegistered(EncodingFeature.class)).isTrue();
        }

        @Test
        void register_ClassComponent_ClassVarArgContracts() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(NoOpClientLoggingFilter.class, ClientResponseFilter.class);

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(NoOpClientLoggingFilter.class)).isTrue();
        }

        @Test
        void register_ClassComponent_MapContracts() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(NoOpClientLoggingFilter.class, Map.of(ClientResponseFilter.class, USER));

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(NoOpClientLoggingFilter.class)).isTrue();
        }

        @Test
        void register_ObjectComponent() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(new CsrfProtectionFilter())
                    .register(new EncodingFeature());

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(CsrfProtectionFilter.class)).isTrue();
            assertThat(config.isRegistered(EncodingFeature.class)).isTrue();
        }

        @Test
        void register_ObjectComponent_IntPriority() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(new CsrfProtectionFilter(), AUTHORIZATION)
                    .register(new EncodingFeature(), ENTITY_CODER);

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(CsrfProtectionFilter.class)).isTrue();
            assertThat(config.isRegistered(EncodingFeature.class)).isTrue();
        }

        @Test
        void register_ObjectComponent_ClassVarArgContracts() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(new NoOpClientLoggingFilter(), ClientResponseFilter.class);

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(NoOpClientLoggingFilter.class)).isTrue();
        }

        @Test
        void register_ObjectComponent_MapContracts() {
            var originalWebTargetHelper = withWebTarget(originalWebTarget);
            var newWebTarget = originalWebTargetHelper
                    .register(new NoOpClientLoggingFilter(), Map.of(ClientResponseFilter.class, USER));

            assertThat(newWebTarget).isSameAs(originalWebTargetHelper);

            var config = newWebTarget.getConfiguration();
            assertThat(config).isSameAs(originalWebTarget.getConfiguration());
            assertThat(config.isRegistered(NoOpClientLoggingFilter.class)).isTrue();
        }
    }

    public static class NoOpClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext) {
            // no-op
        }

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
            // no-op
        }
    }

    private void assertIsOriginalWebTargetAndHasNoQuery(WebTargetHelper newWebTarget) {
        assertThat(newWebTarget.wrapped()).isSameAs(originalWebTarget);
        assertThat(newWebTarget.getUri()).hasNoQuery();
    }

    private void assertNotOriginalWebTargetAndNoQuery(WebTargetHelper newWebTarget) {
        assertThat(newWebTarget.wrapped()).isNotSameAs(originalWebTarget);
        assertThat(newWebTarget.getUri()).hasNoQuery();
    }

    private static String[] splitQueryParams(WebTargetHelper newWebTarget) {
        return newWebTarget.getUri().getQuery().split("&");
    }
}
