package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertCreatedResponseWithLocation;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertJsonResponseType;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseEntity;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertResponseEntityHasOneErrorMessage;

import jakarta.ws.rs.core.Response;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.Optional;

@DisplayName("KiwiStandardResponses")
class KiwiStandardResponsesTest {

    @Value
    private static class MyEntity {
        Integer id;
    }

    @Nested
    class StandardGetResponse {

        @Nested
        class WithEntityType {

            @Test
            void shouldReturnEntity_GivenNonNullEntity() {
                var entity = new MyEntity(42);
                var response = KiwiStandardResponses.standardGetResponse("id", 42, entity, MyEntity.class);

                assertOkResponse(response);
                assertJsonResponseType(response);
                assertResponseEntity(response, entity);
            }

            @Test
            void shouldReturnNotFound_GivenNullEntity() {
                var response = KiwiStandardResponses.standardGetResponse("id", 42, (MyEntity) null, MyEntity.class);

                assertNotFoundResponse(response);
                assertJsonResponseType(response);
                assertResponseEntityHasOneErrorMessage(response, 404, "MyEntity with id 42 not found");
            }

            @Test
            void shouldReturnEntity_GivenOptionalContainingEntity() {
                var entity = new MyEntity(42);
                var entityOpt = Optional.of(entity);
                var response = KiwiStandardResponses.standardGetResponse("id", 42, entityOpt, MyEntity.class);

                assertOkResponse(response);
                assertJsonResponseType(response);
                assertResponseEntity(response, entity);
            }

            @Test
            void shouldReturnNotFound_GivenEmptyOptional() {
                var entityOpt = Optional.<MyEntity>empty();
                var response = KiwiStandardResponses.standardGetResponse("id", 42, entityOpt, MyEntity.class);

                assertNotFoundResponse(response);
                assertJsonResponseType(response);
                assertResponseEntityHasOneErrorMessage(response, 404, "MyEntity with id 42 not found");
            }
        }

        @Nested
        class WithoutEntityType {

            @Test
            void shouldReturnEntity_GivenNonNullEntity() {
                var entity = new MyEntity(42);
                var response = KiwiStandardResponses.standardGetResponse("id", 42, entity);

                assertOkResponse(response);
                assertJsonResponseType(response);
                assertResponseEntity(response, entity);
            }

            @Test
            void shouldReturnNotFound_GivenNullEntity() {
                var response = KiwiStandardResponses.standardGetResponse("id", 42, (MyEntity) null);

                assertNotFoundResponse(response);
                assertJsonResponseType(response);
                assertResponseEntityHasOneErrorMessage(response, 404, "Object with id 42 not found");
            }

            @Test
            void shouldReturnEntity_GivenOptionalContainingEntity() {
                var entity = new MyEntity(42);
                var entityOpt = Optional.of(entity);
                var response = KiwiStandardResponses.standardGetResponse("id", 42, entityOpt);

                assertOkResponse(response);
                assertJsonResponseType(response);
                assertResponseEntity(response, entity);
            }

            @Test
            void shouldReturnNotFound_GivenEmptyOptional() {
                var entityOpt = Optional.<MyEntity>empty();
                var response = KiwiStandardResponses.standardGetResponse("id", 42, entityOpt);

                assertNotFoundResponse(response);
                assertJsonResponseType(response);
                assertResponseEntityHasOneErrorMessage(response, 404, "Object with id 42 not found");
            }
        }

        @Nested
        class WithExplicitMessage {

            @Test
            void shouldReturnEntity_GivenNonNullEntity() {
                var entity = new MyEntity(42);
                var response = KiwiStandardResponses.standardGetResponse(entity, "It was not found");

                assertOkResponse(response);
                assertJsonResponseType(response);
                assertResponseEntity(response, entity);
            }

            @Test
            void shouldReturnNotFound_GivenNullEntity() {
                var response = KiwiStandardResponses.standardGetResponse((MyEntity) null, "It was not found");

                assertNotFoundResponse(response);
                assertJsonResponseType(response);
                assertResponseEntityHasOneErrorMessage(response, 404, "It was not found");
            }

            @Test
            void shouldReturnEntity_GivenOptionalContainingEntity() {
                var entity = new MyEntity(42);
                var entityOpt = Optional.of(entity);
                var response = KiwiStandardResponses.standardGetResponse(entityOpt, "It was not found");

                assertOkResponse(response);
                assertJsonResponseType(response);
                assertResponseEntity(response, entity);
            }

            @Test
            void shouldReturnNotFound_GivenEmptyOptional() {
                var entityOpt = Optional.<MyEntity>empty();
                var response = KiwiStandardResponses.standardGetResponse(entityOpt, "It was not found");

                assertNotFoundResponse(response);
                assertJsonResponseType(response);
                assertResponseEntityHasOneErrorMessage(response, 404, "It was not found");
            }
        }
    }

    @Nested
    class StandardPostResponse {

        @Test
        void shouldReturnCreatedResponseWithEntity() {
            var location = URI.create("https://localhost/myentity/12345");
            var entity = new MyEntity(1);

            var response = KiwiStandardResponses.standardPostResponse(location, entity);

            assertCreatedResponseWithLocation(response, location.toString());
            assertJsonResponseType(response);
            assertResponseEntity(response, entity);
        }
    }

    @Nested
    class StandardPutResponse {

        @Test
        void shouldReturnOkResponseWithEntity() {
            var entity = new MyEntity(1);

            var response = KiwiStandardResponses.standardPutResponse(entity);

            assertOkResponse(response);
            assertJsonResponseType(response);
            assertResponseEntity(response, entity);
        }
    }

    @Nested
    class StandardPatchResponse {

        @Test
        void shouldReturnOkResponseWithEntity() {
            var entity = new MyEntity(1);

            var response = KiwiStandardResponses.standardPatchResponse(entity);

            assertOkResponse(response);
            assertJsonResponseType(response);
            assertResponseEntity(response, entity);
        }
    }

    @Nested
    class StandardDeleteResponse {

        @Test
        void shouldReturnNoContentResponse_WhenNoEntity() {
            var response = KiwiStandardResponses.standardDeleteResponse();

            assertNoContentResponse(response);
            assertJsonResponseType(response);
            assertThat(response.hasEntity()).isFalse();
        }

        @Test
        void shouldReturnOkResponse_WhenGivenDeletedEntity() {
            var deletedEntity = new MyEntity(12345);
            var response = KiwiStandardResponses.standardDeleteResponse(deletedEntity);

            assertOkResponse(response);
            assertJsonResponseType(response);
            assertResponseEntity(response, deletedEntity);
        }
    }

    @Nested
    class StandardBadRequestResponse {

        @Test
        void shouldReturnBadRequestResponse_WithErrorMessageEntity() {
            var response = KiwiStandardResponses.standardBadRequestResponse("This was a bad request. Please try again.");

            assertResponseEntityHasOneErrorMessage(response, 400, "This was a bad request. Please try again.");
            assertJsonResponseType(response);
        }
    }

    @Nested
    class StandardUnauthorizedResponse {

        @Test
        void shouldReturnUnauthorizedResponse_WithErrorMessageEntity() {
            var response = KiwiStandardResponses.standardUnauthorizedResponse("You shall not pass!");

            assertResponseEntityHasOneErrorMessage(response, 401, "You shall not pass!");
            assertJsonResponseType(response);
        }
    }

    @Nested
    class StandardNotFoundResponse {

        @Test
        void shouldReturnNotFoundResponse_WithErrorMessageEntity() {
            var response = KiwiStandardResponses.standardNotFoundResponse("Nothing here. Move along, move along...");

            assertResponseEntityHasOneErrorMessage(response, 404, "Nothing here. Move along, move along...");
            assertJsonResponseType(response);
        }
    }

    @Nested
    class StandardErrorResponse {

        @ParameterizedTest
        @ValueSource(ints = {411, 500, 501, 502, 503})
        void shouldReturnResponse_WithGivenStatusAndErrorMessageEntity(int statusCode) {
            var status = Response.Status.fromStatusCode(statusCode);
            var response = KiwiStandardResponses.standardErrorResponse(status, "This is the error message. It is very helpful.");

            assertResponseEntityHasOneErrorMessage(response, statusCode, "This is the error message. It is very helpful.");
            assertJsonResponseType(response);
        }

        @ParameterizedTest
        @ValueSource(ints = {200, 202, 302, 304})
        void shouldThrowIllegalArgument_WhenStatusCodeIsNotClientOrServerError(int statusCode) {
            var status = Response.Status.fromStatusCode(statusCode);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStandardResponses.standardErrorResponse(status, "This is not actually an error status!"))
                    .withMessage("status %d is not a client error (4xx) or server error (5xx)", statusCode);
        }
    }

    @Nested
    class StandardAcceptedResponse {

        @Test
        void shouldReturnAcceptedResponseWithEntity() {
            var entity = new MyEntity(1);

            var response = KiwiStandardResponses.standardAcceptedResponse(entity);

            assertAcceptedResponse(response);
            assertJsonResponseType(response);
            assertResponseEntity(response, entity);
        }
    }
}
