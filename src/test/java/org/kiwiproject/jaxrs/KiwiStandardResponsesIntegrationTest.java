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
import lombok.Value;
import lombok.With;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
            var entity = Map.of("answer", 42);
            return KiwiStandardResponses.standardAcceptedResponse(entity);
        }

        @PUT
        public Response put(User user) {
            return KiwiStandardResponses.standardPutResponse(user);
        }

        @DELETE
        @Path("/{id}")
        public Response delete() {
            return KiwiStandardResponses.standardDeleteResponse();
        }

        @DELETE
        @Path("/{id}/entity")
        public Response deleteReturningEntity() {
            var deletedEntity = new User(12345L, "alice", "alice@example.org");
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
        var response = RESOURCES.client().target("/ask")
                .request()
                .post(Entity.json("What is the answer to the ultimate question?"));

        assertAcceptedResponse(response);
        assertJsonResponseType(response);

        var entity = response.readEntity(KiwiGenericTypes.MAP_OF_STRING_TO_OBJECT_GENERIC_TYPE);
        assertThat(entity).containsOnly(
                entry("answer", 42)
        );
    }

    private static void assertResponseEntityMapHasErrorsKey(Response response) {
        var entity = response.readEntity(Map.class);

        //noinspection unchecked
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
