package org.kiwiproject.jaxrs;

import static com.google.common.base.Verify.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertCreatedResponseWithLocation;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertCustomHeaderFirstValue;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseEntity;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseType;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertStatusAndResponseEntity;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.jaxrs.exception.ErrorMessage;
import org.kiwiproject.jaxrs.exception.JaxrsBadRequestException;
import org.kiwiproject.jaxrs.exception.JaxrsNotFoundException;
import org.kiwiproject.json.JsonHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@DisplayName("KiwiResources")
@ExtendWith(DropwizardExtensionsSupport.class)
class KiwiResourcesTest {

    private static final int ID = 42;
    private static final MyEntity ENTITY = new MyEntity(ID);
    private static final MyEntity NULL_ENTITY = null;
    private static final Optional<MyEntity> ENTITY_OPTIONAL = Optional.of(ENTITY);
    private static final Optional<MyEntity> EMPTY_ENTITY_OPTIONAL = Optional.empty();
    private static final String ENTITY_NOT_FOUND_MESSAGE = "MyEntity not found";
    private static final String ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_1 = "MyEntity {} not found";
    private static final String ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_2 = "MyEntity %s not found";

    private static final JsonHelper JSON_HELPER = JsonHelper.newDropwizardJsonHelper();

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(new FromResponseTestResource())
            .build();

    @Nested
    class VerifyExistence {

        @Nested
        class EntityArgument {

            @Test
            void shouldNotThrow_WhenEntityNotNull() {
                assertThatCode(() -> KiwiResources.verifyExistence(ENTITY))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrow_WhenEntityIsNull() {
                assertThatThrownBy(() -> KiwiResources.verifyExistence(NULL_ENTITY))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(defaultNotFoundMessage());
            }

            @Test
            void shouldNotThrow_WhenOptionalContainsValue() {
                var verifiedEntity = KiwiResources.verifyExistence(ENTITY_OPTIONAL);
                assertThat(verifiedEntity).isSameAs(ENTITY);
            }

            @Test
            void shouldThrow_WhenOptionalIsEmpty() {
                assertThatThrownBy(() -> KiwiResources.verifyExistence(EMPTY_ENTITY_OPTIONAL))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(defaultNotFoundMessage());
            }
        }

        @Nested
        class EntityTypeAndIdentifier {

            @Test
            void shouldNotThrow_WhenEntityNotNull() {
                assertThatCode(() -> KiwiResources.verifyExistence(ENTITY, MyEntity.class, ID))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrow_WhenEntityIsNull() {
                var message = JaxrsNotFoundException.buildMessage("MyEntity", ID);

                assertThatThrownBy(() -> KiwiResources.verifyExistence((MyEntity) null, MyEntity.class, ID))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(message);
            }

            @Test
            void shouldNotThrow_WhenOptionalContainsValue() {
                var verifiedEntity = KiwiResources.verifyExistence(ENTITY_OPTIONAL, MyEntity.class, ID);
                assertThat(verifiedEntity).isSameAs(ENTITY);
            }

            @Test
            void shouldThrow_WhenOptionalIsEmpty() {
                var message = JaxrsNotFoundException.buildMessage("MyEntity", ID);

                assertThatThrownBy(() -> KiwiResources.verifyExistence(EMPTY_ENTITY_OPTIONAL, MyEntity.class, ID))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(message);
            }
        }

        @Nested
        class EntityAndNotFoundMessage {

            @Test
            void shouldNotThrow_WhenEntityNotNull() {
                assertThatCode(() -> KiwiResources.verifyExistence(ENTITY, ENTITY_NOT_FOUND_MESSAGE))
                        .doesNotThrowAnyException();
            }

            @Test
            void shouldThrow_WhenEntityIsNull() {
                assertThatThrownBy(() -> KiwiResources.verifyExistence(NULL_ENTITY, ENTITY_NOT_FOUND_MESSAGE))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(ENTITY_NOT_FOUND_MESSAGE);
            }

            @Test
            void shouldNotThrow_WhenOptionalContainsValue() {
                var verifiedEntity = KiwiResources.verifyExistence(ENTITY_OPTIONAL, ENTITY_NOT_FOUND_MESSAGE);
                assertThat(verifiedEntity).isSameAs(ENTITY);
            }

            @Test
            void shouldThrow_WhenOptionalIsEmpty() {
                assertThatThrownBy(() -> KiwiResources.verifyExistence(EMPTY_ENTITY_OPTIONAL, ENTITY_NOT_FOUND_MESSAGE))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(ENTITY_NOT_FOUND_MESSAGE);
            }
        }

        @Nested
        class EntityAndNotFoundMessageTemplateWithArgs {

            @Test
            void shouldNotThrow_WhenEntityNotNull() {
                assertThatCode(() -> KiwiResources.verifyExistence(ENTITY, ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_1, 42))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_1,
                    ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_2
            })
            void shouldThrow_WhenEntityIsNull(String template) {
                var arg = 84;
                assertThatThrownBy(() -> KiwiResources.verifyExistence(NULL_ENTITY, template, arg))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(f(template, arg));
            }

            @Test
            void shouldNotThrow_WhenOptionalContainsValue() {
                var verifiedEntity = KiwiResources.verifyExistence(ENTITY_OPTIONAL, ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_1, 24);
                assertThat(verifiedEntity).isSameAs(ENTITY);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_1,
                    ENTITY_NOT_FOUND_MESSAGE_TEMPLATE_2
            })
            void shouldThrow_WhenOptionalIsEmpty(String template) {
                var arg = 126;
                assertThatThrownBy(() -> KiwiResources.verifyExistence(EMPTY_ENTITY_OPTIONAL, template, arg))
                        .isExactlyInstanceOf(JaxrsNotFoundException.class)
                        .hasMessage(f(template, arg));
            }
        }
    }

    @Nested
    class NewResponse {

        @Test
        void shouldCreateWithStatusAndEntity() {
            var status = Response.Status.CREATED;

            var response = KiwiResources.newResponse(status, ENTITY);
            assertStatusAndResponseEntity(response, status, ENTITY);
            assertThat(response.getHeaders()).isEmpty();
        }

        @Test
        void shouldCreateWithStatusAndEntityAndContentType() {
            var status = Response.Status.CREATED;
            var contentType = MediaType.APPLICATION_JSON;

            var response = KiwiResources.newResponse(status, ENTITY, contentType);

            assertStatusAndResponseEntity(response, status, ENTITY);
            assertResponseType(response, contentType);
            assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(contentType);
        }

        @Test
        void shouldCreateWithSingleValuedHeadersMap() {
            var status = Response.Status.ACCEPTED;

            var value1 = "value1";
            var value2 = 42;
            var headers = Map.<String, Object>of("Header1", value1, "Header2", value2);

            var response = KiwiResources.newResponse(status, ENTITY, headers);

            assertStatusAndResponseEntity(response, status, ENTITY);

            var responseHeaders = response.getHeaders();
            assertThat(responseHeaders).hasSize(2);
            assertThat(responseHeaders.get("Header1")).containsExactly(value1);
            assertThat(responseHeaders.get("Header2")).containsExactly(value2);
        }

        @Test
        void shouldCreateWithMultivaluedHeadersMap() {
            var status = Response.Status.OK;

            var value1a = "value1-a";
            var value1b = "value1-b";
            var value2 = "42";

            var headers = new MultivaluedHashMap<String, Object>();
            headers.addAll("Header1", value1a, value1b);
            headers.putSingle("Header2", value2);

            var response = KiwiResources.newResponse(status, ENTITY, headers);

            assertStatusAndResponseEntity(response, status, ENTITY);

            var responseHeaders = response.getHeaders();
            assertThat(responseHeaders).hasSize(2);
            assertThat(responseHeaders.get("Header1")).containsExactly(value1a, value1b);
            assertThat(responseHeaders.get("Header2")).containsExactly(value2);
        }
    }

    @Nested
    class NewResponseBuilder {

        @Test
        void shouldBuildWithStatusAndEntity() {
            var status = Response.Status.OK;

            var response = KiwiResources.newResponseBuilder(status, ENTITY)
                    .type(MediaType.APPLICATION_JSON)
                    .header("X-Foo", "Bar")
                    .build();

            assertStatusAndResponseEntity(response, status, ENTITY);
            assertResponseType(response, MediaType.APPLICATION_JSON);
            assertCustomHeaderFirstValue(response, "X-Foo", "Bar");
        }
    }

    @Nested
    class CreatedResponse {

        @Test
        void shouldCreateCreatedResponse() {
            var location = URI.create("http://localhost/myentity/42");

            var response = KiwiResources.createdResponse(location, ENTITY);

            assertCreatedResponseWithLocation(response, location.toString());
            assertResponseEntity(response, ENTITY);
        }

        @Test
        void shouldBuildCreatedResponseBuilder() {
            var location = URI.create("http://localhost/myentity/42");

            var response = KiwiResources.createdResponseBuilder(location, ENTITY)
                    .type(MediaType.APPLICATION_JSON)
                    .header("X-Custom", "Bar")
                    .build();

            assertCreatedResponseWithLocation(response, location.toString());
            assertResponseEntity(response, ENTITY);
            assertResponseType(response, MediaType.APPLICATION_JSON_TYPE);
            assertCustomHeaderFirstValue(response, "X-Custom", "Bar");
        }
    }

    @Nested
    class OkResponse {

        @Test
        void shouldCreateOkResponse() {
            var response = KiwiResources.okResponse(ENTITY);

            assertOkResponse(response);
            assertResponseEntity(response, ENTITY);
        }

        @Test
        void shouldBuildOkResponseBuilder() {
            var response = KiwiResources.okResponseBuilder(ENTITY)
                    .type(MediaType.APPLICATION_XML)
                    .build();

            assertOkResponse(response);
            assertResponseType(response, MediaType.APPLICATION_XML_TYPE);
            assertResponseEntity(response, ENTITY);
        }
    }

    /**
     * A test resource class for {@link NewResponseBufferingEntityFrom}
     */
    @Path("/from-response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Slf4j
    public static class FromResponseTestResource {

        static Map<String, Object> ENTITY = Map.of("name", "Alice", "age", 42);

        @GET
        @Path("/with-entity")
        public Response withEntity() {
            LOG.info("Returning 200 with an entity");

            return Response.ok(ENTITY)
                    .header("Header-1", "Value 1")
                    .header("Header-2", "Value 2")
                    .build();
        }

        @GET
        @Path("/no-entity")
        public Response noEntity() {
            LOG.info("Returning 200 with no entity");

            return Response.ok()
                    .header("Header-A", "Value A")
                    .header("Header-B", "Value B")
                    .build();
        }
    }

    @Nested
    class NewResponseBufferingEntityFrom {

        private Client client;

        @BeforeEach
        void setUp() {
            client = ClientBuilder.newClient();
        }

        @AfterEach
        void tearDown() {
            client.close();
        }

        @Test
        void shouldCopyHeaders() {
            var originalResponse = RESOURCES.client().target("/from-response/with-entity").request().get();

            var response = KiwiResources.newResponseBufferingEntityFrom(originalResponse);
            assertOkResponse(response);

            assertCustomHeaderFirstValue(response, "Header-1", "Value 1");
            assertCustomHeaderFirstValue(response, "Header-2", "Value 2");
        }

        @Test
        void shouldBufferEntity() throws IOException {
            var originalResponse = RESOURCES.client().target("/from-response/with-entity").request().get();

            var response = KiwiResources.newResponseBufferingEntityFrom(originalResponse);
            assertOkResponse(response);

            assertEntity(response);
        }

        @Test
        void shouldBufferEntity_IfEntityAlreadyBuffered() throws IOException {
            var originalResponse = RESOURCES.client().target("/from-response/with-entity").request().get();

            var wasBuffered = originalResponse.bufferEntity();
            verify(wasBuffered);

            var response = KiwiResources.newResponseBufferingEntityFrom(originalResponse);
            assertOkResponse(response);

            assertEntity(response);
        }

        private void assertEntity(Response response) throws IOException {
            assertThat(response.hasEntity()).isTrue();

            // NOTE:
            // We have to use getEntity instead of readEntity because after buffering, the response is an
            // OutboundJaxrsResponse instead of an InboundJaxrsResponse, and OutboundJaxrsResponse throws an
            // IllegalStateException on calls to readEntity.

            var inputStream = assertResponseEntityIsInputStream(response);
            var json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            var mapEntity = JSON_HELPER.toMap(json);
            assertThat(mapEntity).isEqualTo(FromResponseTestResource.ENTITY);
        }

        @Test
        void shouldThrowIllegalStateException_WhenEntityAlreadyConsumed() {
            var originalResponse = RESOURCES.client().target("/from-response/with-entity").request().get();

            var entity = originalResponse.readEntity(KiwiGenericTypes.MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE);
            assertThat(entity).isEqualTo(FromResponseTestResource.ENTITY);

            assertThatIllegalStateException()
                    .describedAs("Should not be able to buffer when entity was already consumed")
                    .isThrownBy(() -> KiwiResources.newResponseBufferingEntityFrom(originalResponse));
        }

        @Test
        void shouldWorkWhen_OriginalResponse_HasNoEntity() {
            var originalResponse = RESOURCES.client().target("/from-response/no-entity").request().get();

            var response = KiwiResources.newResponseBufferingEntityFrom(originalResponse);
            assertOkResponse(response);

            assertCustomHeaderFirstValue(response, "Header-A", "Value A");
            assertCustomHeaderFirstValue(response, "Header-B", "Value B");

            assertThat(response.hasEntity())
                    .describedAs("Should be true though the buffered InputStream will be empty")
                    .isTrue();

            var inputStream = assertResponseEntityIsInputStream(response);
            assertThat(inputStream).isEmpty();
        }

        private InputStream assertResponseEntityIsInputStream(Response response) {
            var entity = response.getEntity();
            assertThat(entity)
                    .describedAs("Expected entity to be buffered as an InputStream")
                    .isInstanceOf(InputStream.class);

            return (InputStream) entity;
        }

        @Test
        void shouldIgnore_WhenBufferingOutboundResponse_ContainingNoEntity() {
            var originalOutboundResponse = Response.ok().build();
            var bufferedOutboundResponse = KiwiResources.newResponseBufferingEntityFrom(originalOutboundResponse);

            assertThat(bufferedOutboundResponse.hasEntity()).isFalse();
        }
    }

    @Nested
    class ValidateIntParameter {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.jaxrs.KiwiResourcesTest#validIntParameterMaps")
        void shouldReturnTheValue_WhenIsOrCanBecomeAnInt(Map<String, Object> parameters) {
            assertThat(KiwiResources.validateIntParameter(parameters, "id"))
                    .isEqualTo(42);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.jaxrs.KiwiResourcesTest#invalidIntParameterMaps")
        void shouldReturnTheValue_WhenCannotBecomeAnInt(Map<String, Object> parameters) {
            assertThatThrownBy(() -> KiwiResources.validateIntParameter(parameters, "id"))
                    .isExactlyInstanceOf(JaxrsBadRequestException.class);
        }
    }

    private static Stream<Map<String, Object>> validIntParameterMaps() {
        return Stream.of(
                Map.of("id", 42),
                Map.of("id", 42L),
                Map.of("id", "42")
        );
    }

    private static Stream<Map<String, Object>> invalidIntParameterMaps() {
        return Stream.of(
                null,
                Map.of(),
                Map.of("id", "asdf"),
                Map.of("id", "forty two"),
                Map.of("id", 42.0F),
                Map.of("id", 42.2F),
                Map.of("id", 42.0),
                Map.of("id", 42.2),
                Map.of("id", Integer.MAX_VALUE + 1L)
        );
    }

    @Nested
    class ValidateLongParameter {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.jaxrs.KiwiResourcesTest#validLongParameterMaps")
        void shouldReturnTheValue_WhenIsOrCanBecomeAnInt(Map<String, Object> parameters) {
            assertThat(KiwiResources.validateLongParameter(parameters, "id"))
                    .isEqualTo(42);
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.jaxrs.KiwiResourcesTest#invalidLongParameterMaps")
        void shouldReturnTheValue_WhenCannotBecomeAnInt(Map<String, Object> parameters) {
            assertThatThrownBy(() -> KiwiResources.validateLongParameter(parameters, "id"))
                    .isExactlyInstanceOf(JaxrsBadRequestException.class);
        }
    }

    private static Stream<Map<String, Object>> validLongParameterMaps() {
        return Stream.of(
                Map.of("id", 42),
                Map.of("id", 42L),
                Map.of("id", "42")
        );
    }

    private static Stream<Map<String, Object>> invalidLongParameterMaps() {
        return Stream.of(
                null,
                Map.of(),
                Map.of("id", "asdf"),
                Map.of("id", "forty two"),
                Map.of("id", 42.0F),
                Map.of("id", 42.2F),
                Map.of("id", 42.0),
                Map.of("id", 42.2)
        );
    }

    private static String defaultNotFoundMessage() {
        return new ErrorMessage(null).getMessage();
    }

    @Value
    private static class MyEntity {
        Integer id;
    }
}
