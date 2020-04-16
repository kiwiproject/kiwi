package org.kiwiproject.config;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Package-private helper class used by {@link EndpointConfiguration} to generate URIs.
 */
@Builder
class EndpointUriBuilder {

    private static final String SLASH = "/";

    private String scheme;
    private String host;
    private String port;
    private String path;
    private UrlRewriteConfiguration urlRewriteConfig;

    String getURI() {
        var schemeAndHost = getSchemeAndHostString(scheme, host);
        var hostAndPort = getHostAndPort(schemeAndHost, port);
        return buildUri(hostAndPort, getPathPrefix(urlRewriteConfig), stripLeadingAndTrailingSlashes(path));
    }

    private static String getSchemeAndHostString(String scheme, String host) {
        checkArgumentNotBlank(scheme, "scheme cannot be blank");
        checkArgumentNotBlank(host, "host cannot be blank");

        return f("{}://{}", scheme, host);
    }

    private static String getHostAndPort(String schemeAndHost, String port) {
        return isNotBlank(port) ? f("{}:{}", schemeAndHost, port) : schemeAndHost;
    }

    static String stripLeadingAndTrailingSlashes(String value) {
        return StringUtils.strip(value, SLASH);
    }

    private static String getPathPrefix(UrlRewriteConfiguration config) {
        return config.shouldRewrite() ? stripLeadingAndTrailingSlashes(config.getPathPrefix()) : EMPTY;
    }

    private static String buildUri(String... uriParts) {
        return Stream.of(uriParts)
                .filter(StringUtils::isNotBlank)
                .collect(joining(SLASH));
    }
}
