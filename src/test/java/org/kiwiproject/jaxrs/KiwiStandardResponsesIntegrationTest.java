package org.kiwiproject.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertAcceptedResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertBadRequestResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertCreatedResponseWithLocation;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertJsonResponseType;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertNoContentResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertNotFoundResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertOkResponse;
import static org.kiwiproject.jaxrs.JaxrsTestHelper.assertUnauthorizedFoundResponse;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Value;
import lombok.With;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.util.Map;

@DisplayName("KiwiStandardResponses")
@ExtendWith(DropwizardExtensionsSupport.class)
class KiwiStandardResponsesIntegrationTest {

    private static final User ALICE = new User(12345L, "alice", "alice@example.org");

    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class StandardResponsesTestResource {

        @GET
        @Path("found")
        public Response getFound() {
            return KiwiStandardResponses.standardGetResponse("id", ALICE.getId(), ALICE);
        }

        @GET
        @Path("notFound")
        public Response getNotFound() {
            return KiwiStandardResponses.standardGetResponse("id", 42L, (User) null);
        }

        @POST
        public Response post(User user) {
            var savedUser = user.withId(12345L);
            var location = URI.create("https://acme.com/users/12345");
            return KiwiStandardResponses.standardPostResponse(location, savedUser);
        }

        @POST
        @Path("/ask")
        public Response postWithAccept(String question) {
            var entity = Map.of("answer", 42, "question", question);
            return KiwiStandardResponses.standardAcceptedResponse(entity);
        }

        @PUT
        public Response put(User user) {
            return KiwiStandardResponses.standardPutResponse(user);
        }

        @DELETE
        @Path("/{id}")
        public Response delete(@SuppressWarnings("unused") @PathParam("id") Long id) {
            return KiwiStandardResponses.standardDeleteResponse();
        }

        @DELETE
        @Path("/{id}/entity")
        public Response deleteReturningEntity(@PathParam("id") Long id) {
            var deletedEntity = new User(id, "alice", "alice@example.org");
            return KiwiStandardResponses.standardDeleteResponse(deletedEntity);
        }

        @GET
        @Path("/badRequest")
        public Response badRequest() {
            return KiwiStandardResponses.standardBadRequestResponse("You sent a bad request.");
        }

        @GET
        @Path("/unauthorized")
        public Response unauthorized() {
            return KiwiStandardResponses.standardUnauthorizedResponse("You cannot pass!");
        }
    }

    private static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .bootstrapLogging(false)
            .addResource(new StandardResponsesTestResource())
            .build();

    @Test
    void shouldGetWhenEntityExists() {
        var response = RESOURCES.client().target("/found").request().get();

        assertOkResponse(response);
        assertJsonResponseType(response);
        var user = response.readEntity(User.class);
        assertThat(user).isEqualTo(ALICE);
    }

    @Test
    void shouldGetNotFound() {
        var response = RESOURCES.client().target("/notFound").request().get();

        assertNotFoundResponse(response);
        assertJsonResponseType(response);

        assertResponseEntityMapHasErrorsKey(response);
    }

    @Test
    void shouldPost() {
        var newUser = new User(null, "bob", "bob@example.org");
        var response = RESOURCES.client().target("/").request().post(Entity.json(newUser));

        assertCreatedResponseWithLocation(response, "https://acme.com/users/12345");
        assertJsonResponseType(response);

        var savedUser = response.readEntity(User.class);
        assertThat(savedUser).isEqualTo(newUser.withId(12345L));
    }

    @Test
    void shouldPut() {
        var user = new User(12345L, "alice", "alice_johnson@example.org");
        var response = RESOURCES.client().target("/").request().put(Entity.json(user));

        assertOkResponse(response);
        assertJsonResponseType(response);

        var updatedUser = response.readEntity(User.class);
        assertThat(updatedUser).isEqualTo(user);
    }

    @Test
    void shouldDelete() {
        var response = RESOURCES.client().target("/12345").request().delete();

        assertNoContentResponse(response);
        assertJsonResponseType(response);
        assertThat(response.hasEntity()).isFalse();
    }

    @Test
    void shouldDeleteWithReturnedEntity() {
        var response = RESOURCES.client().target("/12345/entity").request().delete();

        assertOkResponse(response);
        assertJsonResponseType(response);

        var deletedUser = response.readEntity(User.class);
        assertThat(deletedUser).isEqualTo(ALICE);
    }

    @Test
    void shouldSendBadRequest() {
        var response = RESOURCES.client().target("/badRequest").request().get();

        assertBadRequestResponse(response);
        assertJsonResponseType(response);
        assertResponseEntityMapHasErrorsKey(response);
    }

    @Test
    void shouldSendUnauthorized() {
        var response = RESOURCES.client().target("/unauthorized").request().get();

        assertUnauthorizedFoundResponse(response);
        assertJsonResponseType(response);
        assertResponseEntityMapHasErrorsKey(response);
    }

    @Test
    void shouldAccept() {
        var question = "What is the answer to the ultimate question?";
        var response = RESOURCES.client().target("/ask")
                .request()
                .post(Entity.json(question));

        assertAcceptedResponse(response);
        assertJsonResponseType(response);

        var entity = response.readEntity(KiwiGenericTypes.MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE);
        assertThat(entity).containsOnly(
                entry("answer", 42),
                entry("question", question)
        );
    }

    private static void assertResponseEntityMapHasErrorsKey(Response response) {
        var entity = response.readEntity(new GenericType<Map<String, Object>>() {});

        assertThat(entity).containsKey("errors");
    }

    @Value
    public static class User {
        @With
        Long id;
        String username;
        String email;
    }
}
