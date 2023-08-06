package org.kiwiproject.jaxrs.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.jaxrs.client.WebTargetClientHelper.withClient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;

@DisplayName("WebTargetClientHelper")
class WebTargetClientHelperTest {

    private static Client client;

    @BeforeAll
    static void beforeAll() {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    static void afterAll() {
        client.close();
    }

    @Nested
    class Target {

        @Test
        void shouldAcceptString() {
            var newWebTarget = withClient(client)
                    .target("/search")
                    .queryParamIfNotBlank("q", "pangram")
                    .queryParamIfNotBlank("sort", "")
                    .queryParamIfNotNull("page", null)
                    .queryParamIfNotNull("limit", null);

            assertThat(newWebTarget.getUri()).hasQuery("q=pangram");
        }

        @Test
        void shouldAcceptURI() {
            var newWebTarget = withClient(client)
                    .target(URI.create("/search"))
                    .queryParamIfNotBlank("q", "pangram")
                    .queryParamIfNotBlank("sort", "")
                    .queryParamIfNotNull("page", 0)
                    .queryParamIfNotNull("limit", 10);

            assertThat(newWebTarget.getUri()).hasQuery("q=pangram&page=0&limit=10");
        }

        @Test
        void shouldAcceptUriBuilder() {
            var uriBuilder = UriBuilder.fromPath("/search");
            var newWebTarget = withClient(client)
                    .target(uriBuilder)
                    .queryParamIfNotBlank("q", "pangram")
                    .queryParamIfNotBlank("sort", "relevance")
                    .queryParamIfNotNull("page", 0)
                    .queryParamIfNotNull("limit", 25);

            assertThat(newWebTarget.getUri()).hasQuery("q=pangram&sort=relevance&page=0&limit=25");
        }

        @Test
        void shouldAcceptLink() {
            var link = Link.fromPath("/search").build();
            var newWebTarget = withClient(client)
                    .target(link)
                    .queryParamIfNotBlank("q", "pangram")
                    .queryParamIfNotBlank("sort", "")
                    .queryParamIfNotNull("page", null)
                    .queryParamIfNotNull("limit", 10);

            assertThat(newWebTarget.getUri()).hasQuery("q=pangram&limit=10");
        }
    }
}
