package org.kiwiproject.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;
import static org.kiwiproject.config.EndpointUriBuilder.stripLeadingAndTrailingSlashes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kiwiproject.base.KiwiStrings;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Configuration for a specific REST endpoint. Can be used standalone or in conjunction with
 * {@link SecureEndpointsConfiguration}.
 * <p>
 * As this is a configuration class generally intended to be populated from external configuration, it is mutable.
 */
@Getter
@Setter
public class EndpointConfiguration {

    /**
     * Use this to uniquely identify an endpoint within a {@link SecureEndpointsConfiguration}, or to provide
     * a way to find an {@link EndpointConfiguration} in any collection of them, e.g. using a
     * {@link java.util.stream.Stream#filter(Predicate)} on a stream of endpoint configurations.
     *
     * @see SecureEndpointsConfiguration#getEndpointByTag(String)
     * @see SecureEndpointsConfiguration#getEndpointByTagOrEmpty(String)
     */
    private String tag;

    /**
     * The connection scheme, e.g. https or https.
     */
    private String scheme;

    /**
     * A single domain, or a comma-separated list of domains.
     *
     * @see #setDomain(String)
     */
    @Setter(value = AccessLevel.NONE)
    private String domain;

    /**
     * Parsed from domain. No public getter or setter.
     */
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private List<String> domainList;

    /**
     * The port the endpoint listens on.
     */
    private String port;

    /**
     * The path component of the URI.
     */
    private String path;

    /**
     * Configures any URI rewriting that should be performed when building URIs.
     */
    private UrlRewriteConfiguration urlRewriteConfiguration = UrlRewriteConfiguration.none();

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private AtomicInteger roundRobinIndex = new AtomicInteger(0);

    /**
     * To use this {@link Builder} standalone, use the {@link #builder()} method. When using this, do not call
     * {@link #buildEndpoint()} or else an {@link IllegalStateException} will be thrown since this method assumes
     * the endpoint is being built in the context of a {@link SecureEndpointsConfiguration}.
     * <p>
     * To use this {@link Builder} as part of building a {@link SecureEndpointsConfiguration}, use the
     * {@link #builder(SecureEndpointsConfiguration.Builder)} method and supply the {@link SecureEndpointsConfiguration}
     * instance that becomes the "parent" of this endpoint.
     *
     * @implNote This was implemented well before we started using Lombok, thus the manual builder code. Since there
     * are some differences here, e.g. the constructor accepting the "parent" and the {@link #buildEndpoint()} method,
     * not sure how feasible it is to refactor to use Lombok, or if it's worth bothering. In addition, we have left
     * the original setXxx() methods in here and added Lombok-style xxx() methods. While permissible, you
     * should be consistent in using all xxx() or all setXxx().
     */
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Builder {

        private final EndpointConfiguration endpoint = new EndpointConfiguration();

        private SecureEndpointsConfiguration.Builder parent;

        protected Builder(SecureEndpointsConfiguration.Builder parent) {
            this.parent = parent;
        }

        public Builder tag(String tag) {
            return setTag(tag);
        }

        public Builder setTag(String tag) {
            endpoint.setTag(tag);
            return this;
        }

        public Builder scheme(String scheme) {
            return setScheme(scheme);
        }

        public Builder setScheme(String scheme) {
            endpoint.setScheme(scheme);
            return this;
        }

        public Builder domain(String domain) {
            return setDomain(domain);
        }

        public Builder setDomain(String domain) {
            endpoint.setDomain(stripLeadingAndTrailingSlashes(domain));
            return this;
        }

        public Builder port(String port) {
            return setPort(port);
        }

        public Builder setPort(String port) {
            checkArgument(isDigits(port), "port should contain only digits: %s", port);
            endpoint.setPort(port);
            return this;
        }

        public Builder path(String path) {
            return setPath(path);
        }

        public Builder setPath(String path) {
            endpoint.setPath(path);
            return this;
        }

        public Builder urlRewriteConfiguration(UrlRewriteConfiguration urlRewriteConfig) {
            return setUrlRewriteConfiguration(urlRewriteConfig);
        }

        public Builder setUrlRewriteConfiguration(UrlRewriteConfiguration urlRewriteConfig) {
            endpoint.setUrlRewriteConfiguration(urlRewriteConfig);
            return this;
        }

        public EndpointConfiguration build() {
            return endpoint;
        }

        /**
         * Call this to add this {@link EndpointConfiguration} to the parent {@link SecureEndpointsConfiguration}
         * and return to building the {@link SecureEndpointsConfiguration}, which can include more endpoints.
         *
         * @return The parent {@link SecureEndpointsConfiguration}'s builder, after adding this endpoint to the parent
         * @see SecureEndpointsConfiguration.Builder#addEndpoint()
         */
        public SecureEndpointsConfiguration.Builder buildEndpoint() {
            checkState(nonNull(parent),
                    "The parent SecureEndpointsConfiguration is null. This method should" +
                            " only be used when building via SecureEndpointsConfiguration.");

            parent.build().getEndpoints().add(endpoint);
            return parent;
        }
    }

    /**
     * Return a new builder instance to create a standalone {@link EndpointConfiguration}.
     *
     * @return the builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Return a new builder instance to create a {@link EndpointConfiguration} within a
     * {@link SecureEndpointsConfiguration}. Supply the {@link SecureEndpointsConfiguration.Builder} which
     * is being used to build the {@link SecureEndpointsConfiguration}.
     *
     * @param parent the parent builder
     * @return the builder instance
     */
    public static Builder builder(SecureEndpointsConfiguration.Builder parent) {
        return new Builder(parent);
    }

    /**
     * Returns true if the scheme is "wss" or "https".
     *
     * @return true if using a secure scheme
     */
    public boolean isSecure() {
        return "wss".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    /**
     * Set the domain(s), which can be a single domain (e.g. example.org) or multiple domains separated by commas. If
     * a comma-separated list of domains is specified, whitespace is allowed and trimmed. For example this is
     * valid: " domain-1.test , domain-2.test , domain-3.test " and results in the three domains "domain-1.test",
     * "domain-2.test", and "domain-3.test".
     *
     * @param singleDomainOrCsv a single domain like example.org or a comma-delimited list of domains
     */
    public void setDomain(String singleDomainOrCsv) {
        this.domain = singleDomainOrCsv;
        this.domainList = KiwiStrings.splitOnCommas(singleDomainOrCsv);
    }

    /**
     * Return the domains as a list.
     *
     * @return the list of domains
     */
    public List<String> getDomains() {
        return domainList;
    }

    /**
     * Uses this endpoint's {@code path} to build a URI as a String. The host in the returned URI will be the result of
     * round-robin among the domains returned by {@link #getDomains()}.
     *
     * @return the URI as a {@link String}
     * @implNote This currently builds URIs using simple string substitution, any any leading or trailing slashes
     * on the domain are stripped.
     */
    public String getURI() {
        return getUriWithNextHost(path);
    }

    /**
     * Uses this endpoint's {@code path} to build a {@link URI}.
     *
     * @return the URI as a {@link URI}
     * @see #getURI()
     */
    public URI getUriObject() {
        return URI.create(getURI());
    }

    private String getUriWithNextHost(String thePath) {
        var nextHost = getNextDomain();

        return EndpointUriBuilder.builder()
                .scheme(scheme)
                .host(nextHost)
                .port(port)
                .path(thePath)
                .urlRewriteConfig(urlRewriteConfiguration)
                .build()
                .getURI();
    }

    /**
     * @implNote The simplest way to ensure safe concurrent access is to make this synchronized. If there is
     * ever a (good) reason to change this to use an internal lock or similar, it will be relatively easy since
     * this method is private, thus not a part of the public API. This change was made because of an error we
     * encountered wherein we saw some {@link ArrayIndexOutOfBoundsException} occurring on the
     * {@code domainList.get(index)} call. This cause was due to multiple threads accessing concurrently.
     */
    private synchronized String getNextDomain() {
        checkState(nonNull(domainList), "No domains have been set on this endpoint!");

        if (domainList.size() == 1) {
            return domain;
        }

        var nextDomain = domainList.get(roundRobinIndex.getAndIncrement());

        if (roundRobinIndex.get() >= domainList.size()) {
            roundRobinIndex.set(0);
        }

        return nextDomain;
    }

    /**
     * Converts the URI returned by {@link #getURI()} into a JAX-RS {@link UriBuilder}.
     * <p>
     * Note that JAX-RS must be in the classpath when calling this method.
     *
     * @return a JAX-RS {@link UriBuilder} instance
     */
    public UriBuilder toUriBuilder() {
        return UriBuilder.fromUri(getURI());
    }
}
