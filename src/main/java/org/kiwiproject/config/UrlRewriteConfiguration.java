package org.kiwiproject.config;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Specifies URL rewriting configuration used by {@link EndpointConfiguration}. Currently, supports only a URL
 * path prefix.
 * <p>
 * As this is a configuration class generally intended to be populated from external configuration, it is mutable and
 * supports construction in a variety of ways - no argument constructor and setter methods, all-arguments
 * constructor, or a builder. Once constructed, it is intended to be read-only, though this cannot be enforced
 * and still have setter methods.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlRewriteConfiguration {

    /**
     * If specified, then URIs/URLs generated by {@link EndpointConfiguration} will include this prefix, e.g.
     * given a URL {@code https://acme.com/order/12345} and a prefix of {@code /my-proxy}, the resulting URL is
     * {@code https://acme.com/my-proxy/order/12345}.
     */
    private String pathPrefix;

    /**
     * Returns and instance that will not perform any URL rewriting. Useful as a default value.
     *
     * @return a new "no-rewriting" UrlRewriteConfiguration
     */
    public static UrlRewriteConfiguration none() {
        return UrlRewriteConfiguration.builder().build();
    }

    /**
     * Whether URL rewrites will be performed.
     *
     * @return true if there is a path prefix, false otherwise
     */
    public boolean shouldRewrite() {
        return isNotBlank(pathPrefix);
    }
}
