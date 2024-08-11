package org.kiwiproject.config;

import static org.kiwiproject.base.KiwiStrings.f;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Configuration for secure REST endpoints, including the configuration from an {@link SSLContextConfiguration}
 * as well as adding a collection of {@link EndpointConfiguration} instances. Supports programmatic creation
 * using a builder. Also supports construction from external configuration, e.g., from a YAML configuration file, using
 * the no-args constructor and setter methods.
 * <p>
 * As this is a configuration class that supports population from external configuration, it is mutable.
 */
public class SecureEndpointsConfiguration extends SSLContextConfiguration {

    /**
     * The endpoints in this configuration.
     */
    @Getter
    @Setter
    private List<EndpointConfiguration> endpoints;

    /**
     * Create instance with an empty collection of endpoints.
     */
    public SecureEndpointsConfiguration() {
        this.endpoints = new ArrayList<>();
    }

    /**
     * Builder class for {@link SecureEndpointsConfiguration}.
     *
     * @implNote This was implemented well before we started using Lombok, thus the manual builder code.
     * We have retained the original setXxx() methods but have added Lombok-style xxx() methods.
     */
    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Builder extends SSLContextConfiguration.Builder {

        private final SecureEndpointsConfiguration configuration = new SecureEndpointsConfiguration();

        /**
         * Returns an {@link EndpointConfiguration.Builder} from which an endpoint can be configured.
         * <p>
         * Call {@link EndpointConfiguration.Builder#buildEndpoint()} to add the endpoint to this
         * configuration.
         * <p>
         * Multiple endpoints can be added.
         *
         * @return the builder instance
         */
        public EndpointConfiguration.Builder addEndpoint() {
            return EndpointConfiguration.builder(this);
        }

        @Override
        public Builder keyStorePath(String keyStorePath) {
            return setKeyStorePath(keyStorePath);
        }

        @Override
        public Builder setKeyStorePath(String keyStorePath) {
            configuration.setKeyStorePath(keyStorePath);
            return this;
        }

        @Override
        public Builder keyStorePassword(String keyStorePassword) {
            return setKeyStorePassword(keyStorePassword);
        }

        @Override
        public Builder setKeyStorePassword(String keyStorePassword) {
            configuration.setKeyStorePassword(keyStorePassword);
            return this;
        }

        @Override
        public Builder trustStorePath(String trustStorePath) {
            return setTrustStorePath(trustStorePath);
        }

        @Override
        public Builder setTrustStorePath(String trustStorePath) {
            configuration.setTrustStorePath(trustStorePath);
            return this;
        }

        @Override
        public Builder trustStorePassword(String trustStorePassword) {
            return setTrustStorePassword(trustStorePassword);
        }

        @Override
        public Builder setTrustStorePassword(String trustStorePassword) {
            configuration.setTrustStorePassword(trustStorePassword);
            return this;
        }

        @Override
        public Builder protocol(String protocol) {
            return setProtocol(protocol);
        }

        @Override
        public Builder setProtocol(String protocol) {
            configuration.setProtocol(protocol);
            return this;
        }

        @Override
        public Builder keyStoreType(String keyStoreType) {
            return setKeyStoreType(keyStoreType);
        }

        @Override
        public Builder setKeyStoreType(String keyStoreType) {
            configuration.setKeyStoreType(keyStoreType);
            return this;
        }

        @Override
        public Builder trustStoreType(String trustStoreType) {
            return setTrustStoreType(trustStoreType);
        }

        @Override
        public Builder setTrustStoreType(String trustStoreType) {
            configuration.setTrustStoreType(trustStoreType);
            return this;
        }

        @Override
        public Builder verifyHostname(boolean verifyHostname) {
            return setVerifyHostname(verifyHostname);
        }

        @Override
        public Builder setVerifyHostname(boolean verifyHostname) {
            configuration.setVerifyHostname(verifyHostname);
            return this;
        }

        @Override
        public SecureEndpointsConfiguration build() {
            return configuration;
        }
    }

    /**
     * Return a new builder instance.
     *
     * @return the builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns true if <strong>any</strong> of the endpoints in this configuration is secure.
     * <p>
     * Alias for {@link #anyEndpointSecure()}.
     *
     * @return true if any endpoint is secure
     */
    public boolean isSecure() {
        return endpoints.stream().anyMatch(EndpointConfiguration::isSecure);
    }

    /**
     * Returns true if <strong>any</strong> of the endpoints in this configuration is secure.
     *
     * @return {@code true} if any of endpoint is secure: {@code false} otherwise
     */
    public boolean anyEndpointSecure() {
        return isSecure();
    }

    /**
     * Returns true if <strong>all</strong> of the endpoints in this configuration are secure.
     *
     * @return {@code true} if all endpoints are secure: {@code false} otherwise
     */
    public boolean allEndpointsSecure() {
        return endpoints.stream().allMatch(EndpointConfiguration::isSecure);
    }

    /**
     * Finds the {@link EndpointConfiguration} with the given tag, throwing an exception if not found.
     *
     * @param tag the tag to search for
     * @return the EndpointConfiguration with the given tag
     * @throws IllegalStateException if there is no such endpoint
     */
    public EndpointConfiguration getEndpointByTag(String tag) {
        return getEndpointByTagOrEmpty(tag)
                .orElseThrow(endpointNotExistsException("tag", tag));
    }

    /**
     * Finds the {@link EndpointConfiguration} with the given tag, returning an empty Optional if not found.
     *
     * @param tag the tag to search for
     * @return an {@link Optional} that may or may not contain an endpoint
     */
    public Optional<EndpointConfiguration> getEndpointByTagOrEmpty(String tag) {
        return endpoints.stream()
                .filter(endpoint -> endpoint.getTag().equalsIgnoreCase(tag))
                .findFirst();
    }

    /**
     * Finds the {@link EndpointConfiguration} with a path ending with the given {@code pathEnding}, throwing an
     * exception if not found.
     *
     * @param pathEnding the end of the path to look for in each endpoint
     * @return the EndpointConfiguration with the given path ending
     * @throws IllegalStateException if there is no such endpoint
     */
    public EndpointConfiguration getEndpointByPathEnding(String pathEnding) {
        return getEndpointByPathEndingOrEmpty(pathEnding)
                .orElseThrow(endpointNotExistsException("pathEnding", pathEnding));
    }

    /**
     * Finds the {@link EndpointConfiguration} with a path ending with the given {@code pathEnding}, returning an
     * empty Optional if not found.
     *
     * @param pathEnding the end of the path to look for in each endpoint
     * @return an {@link Optional} that may or may not contain an endpoint
     */
    public Optional<EndpointConfiguration> getEndpointByPathEndingOrEmpty(String pathEnding) {
        return endpoints.stream()
                .filter(endpoint -> endpoint.getPath().endsWith(pathEnding))
                .findFirst();
    }

    private static Supplier<IllegalStateException> endpointNotExistsException(String classifier, String value) {
        return () -> new IllegalStateException(f("No endpoint by {} with value [{}] was found", classifier, value));
    }
}
