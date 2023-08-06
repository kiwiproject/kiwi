package org.kiwiproject.jaxrs.client;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import com.google.common.annotations.Beta;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;

/**
 * Use with JAX-RS {@link Client} instances to provide additional functionality via {@link WebTargetHelper}. Each
 * of the {@code target} methods returns a {@link WebTargetHelper} to allow method chaining. Please see the
 * documentation in {@link WebTargetHelper} for more explanation.
 *
 * @see WebTargetHelper
 */
@Beta
public class WebTargetClientHelper {

    private final Client client;

    private WebTargetClientHelper(Client client) {
        this.client = requireNotNull(client);
    }

    /**
     * Create a new instance with the given {@link Client}.
     *
     * @param client the Client to use when building requests
     * @return a new instance
     */
    public static WebTargetClientHelper withClient(Client client) {
        return new WebTargetClientHelper(client);
    }

    /**
     * Build a new web resource target.
     *
     * @param uri web resource URI. May contain template parameters. Must not be null
     * @return a {@link WebTargetHelper} with the target bound to the provided URI
     * @see Client#target(String)
     */
    public WebTargetHelper target(String uri) {
        return new WebTargetHelper(client.target(uri));
    }

    /**
     * Build a new web resource target.
     *
     * @param uri web resource URI. Must not be null.
     * @return a {@link WebTargetHelper} with the target bound to the provided URI.
     * @see Client#target(URI)
     */
    public WebTargetHelper target(URI uri) {
        return new WebTargetHelper(client.target(uri));
    }

    /**
     * Build a new web resource target.
     *
     * @param uriBuilder web resource URI represented as URI builder. Must not be null.
     * @return a {@link WebTargetHelper} with the target bound to the provided URI.
     */
    public WebTargetHelper target(UriBuilder uriBuilder) {
        return new WebTargetHelper(client.target(uriBuilder));
    }

    /**
     * Build a new web resource target.
     *
     * @param link link to a web resource. Must not be null.
     * @return a {@link WebTargetHelper} with the target bound to the linked web resource
     */
    public WebTargetHelper target(Link link) {
        return new WebTargetHelper(client.target(link));
    }
}
