package org.kiwiproject.net;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.f;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiMaps.isNullOrEmpty;

import com.google.common.base.Splitter;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * Static utilities for creating URLs
 */
@UtilityClass
public class KiwiUrls {

    public static final int UNKNOWN_PORT = -1;

    public static final String FTP_PROTOCOL = "ftp";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String SFTP_PROTOCOL = "sftp";

    /**
     * FYI, this "?<word>" section in the regular expression below allows us to create "named groups" for pattern
     * matching. This means instead of having to determine what the index of a given group is (can you identify what
     * the index of port is quickly?), we can just reference the name.
     * <p>
     * Look at the (lengthy) preamble in {@link java.util.regex.Pattern} for more information.
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "((?<scheme>[a-z]+)://)?(?<canonical>(?<subdomain>[a-z0-9-]+)(\\.(?<domain>[a-z0-9.-]+))?)(:(?<port>[0-9]+))?(?<path>/.*)?",
            Pattern.CASE_INSENSITIVE
    );

    public static final String SCHEME_GROUP = "scheme";
    public static final String CANONICAL_GROUP = "canonical";
    public static final String SUB_DOMAIN_GROUP = "subdomain";
    public static final String DOMAIN_GROUP = "domain";
    public static final String PORT_GROUP = "port";
    public static final String PATH_GROUP = "path";

    public static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    /**
     * Create a well-formed URL (String) from the given protocol, hostname, and port.
     *
     * @param protocol the protocol
     * @param hostname the host name
     * @param port     the port
     * @return the URL as a {@link String} object
     */
    public static String createUrl(String protocol, String hostname, int port) {
        return createUrlObject(protocol, hostname, port).toString();
    }

    /**
     * Create a well-formed URL from the given protocol, hostname, and port.
     *
     * @param protocol the protocol
     * @param hostname the host name
     * @param port     the port
     * @return the URL as a {@link URL} object
     */
    public static URL createUrlObject(String protocol, String hostname, int port) {
        return createUrlObject(protocol, hostname, port, "");
    }

    /**
     * Create a well-formed URL (String) from the given protocol, hostname, port and path.
     *
     * @param protocol the protocol
     * @param hostname the host name
     * @param port     the port
     * @param path     the path
     * @return the URL as a {@link String} object
     */
    public static String createUrl(String protocol, String hostname, int port, String path) {
        return createUrlObject(protocol, hostname, port, path).toString();
    }

    /**
     * Create a well-formed URL from the given protocol, hostname, port, and path.
     *
     * @param protocol the protocol
     * @param hostname the host name
     * @param port     the port
     * @param path     the path
     * @return the URL as a {@link URL} object
     */
    public static URL createUrlObject(String protocol, String hostname, int port, String path) {
        try {
            var pathWithLeadingSlash = StringUtils.isBlank(path) ? "" : prependLeadingSlash(path);

            return new URL(protocol, hostname, port, pathWithLeadingSlash);
        } catch (MalformedURLException e) {
            var message = f("Error constructing URL from protocol [%s], hostname [%s], port [%s], path [%s]",
                    protocol, hostname, port, path);
            throw new UncheckedMalformedURLException(message, e);
        }
    }

    /**
     * Wrapper around URL's constructor which throws a checked {@link MalformedURLException}. This instead assumes
     * the given {@code urlSpec} is valid and throws {@link UncheckedMalformedURLException} in case it is actually
     * not valid.
     *
     * @param urlSpec the String to parse as a URL
     * @return a new {@link URL} instance
     * @throws UncheckedMalformedURLException that wraps a {@link MalformedURLException} if any error occurs
     */
    public static URL createUrlObject(String urlSpec) {
        try {
            return new URL(urlSpec);
        } catch (MalformedURLException e) {
            throw new UncheckedMalformedURLException(e);
        }
    }

    /**
     * Create a well-formed URL string from the given {@code schemeHostPort} and zero or more path components.
     *
     * @param schemeHostPort a string containing the scheme, host, and port parts, e.g. http://acme.com:8080
     * @param pathComponents zero or more path components to append
     * @return the constructed URL as a {@link String}
     */
    public static String createUrl(String schemeHostPort, String... pathComponents) {
        return createUrlObject(schemeHostPort, pathComponents).toString();
    }

    /**
     * Create a well-formed URL from the given {@code schemeHostPort} and zero or more path components.
     *
     * @param schemeHostPort a string containing the scheme, host, and port parts, e.g. http://acme.com:8080
     * @param pathComponents zero or more path components to append
     * @return the constructed URL as a {@link URL}
     */
    public static URL createUrlObject(String schemeHostPort, String... pathComponents) {
        var rawBaseUri = URI.create(schemeHostPort);
        if (pathComponents.length == 0) {
            return toURL(rawBaseUri);
        }

        var baseUri = stripTrailingSlash(rawBaseUri.toString());
        var path = Paths.get("/", pathComponents).toString();
        var fullUrlString = baseUri + path;

        return toURL(URI.create(fullUrlString));
    }

    /**
     * Tries to convert the given {@code uri} into a {@link URL}, throwing an unchecked exception if the conversion
     * fails. The thrown unchecked exception wraps the original checked {@link MalformedURLException}.
     *
     * @param uri the URI to convert
     * @return a {@link URL} instance
     * @throws UncheckedMalformedURLException if conversion from {@link URI} to {@link URL} fails
     * @see URI#toURL()
     */
    public static URL toURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            var message = "Error creating URL from: " + uri;
            throw new UncheckedMalformedURLException(message, e);
        }
    }

    /**
     * Trims {@code path} and, if a leading slash is not present, adds it.
     *
     * @param path a path
     * @return a new String with a leading slash
     */
    public static String prependLeadingSlash(String path) {
        var trimmedPath = path.trim();
        if (trimmedPath.charAt(0) == '/') {
            return trimmedPath;
        }

        return '/' + trimmedPath;
    }

    /**
     * Create a well-formed HTTP URL (String) from the given hostname and port.
     *
     * @param hostname the host name
     * @param port     the port
     * @return a URL as a {@link String}
     */
    public static String createHttpUrl(String hostname, int port) {
        return createHttpUrlObject(hostname, port).toString();
    }

    /**
     * Create a well-formed HTTP URL from the given hostname and port.
     *
     * @param hostname the host name
     * @param port     the port
     * @return a URL as a {@link URL}
     */
    public static URL createHttpUrlObject(String hostname, int port) {
        return createUrlObject(HTTP_PROTOCOL, hostname, port);
    }

    /**
     * Create a well-formed HTTP URL (String) from the given hostname, port and path.
     *
     * @param hostname the host name
     * @param port     the port
     * @param path     the path
     * @return a URL as a {@link String}
     */
    public static String createHttpUrl(String hostname, int port, String path) {
        return createHttpUrlObject(hostname, port, path).toString();
    }

    /**
     * Create a well-formed HTTP URL from the given hostname, port and path.
     *
     * @param hostname the host name
     * @param port     the port
     * @param path     the path
     * @return a URL as a {@link URL}
     */
    public static URL createHttpUrlObject(String hostname, int port, String path) {
        return createUrlObject(HTTP_PROTOCOL, hostname, port, path);
    }

    /**
     * Create a well-formed HTTPS URL (String) from the given hostname and port.
     *
     * @param hostname the host name
     * @param port     the port
     * @return a URL as a {@link String}
     */
    public static String createHttpsUrl(String hostname, int port) {
        return createHttpsUrlObject(hostname, port).toString();
    }

    /**
     * Create a well-formed HTTPS URL from the given hostname and port.
     *
     * @param hostname the host name
     * @param port     the port
     * @return a URL as a {@link URL}
     */
    public static URL createHttpsUrlObject(String hostname, int port) {
        return createUrlObject(HTTPS_PROTOCOL, hostname, port);
    }

    /**
     * Create a well-formed HTTPS URL (String) from the given hostname, port and path.
     *
     * @param hostname the host name
     * @param port     the port
     * @param path     the path
     * @return a URL as a {@link String}
     */
    public static String createHttpsUrl(String hostname, int port, String path) {
        return createHttpsUrlObject(hostname, port, path).toString();
    }

    /**
     * Create a well-formed HTTPS URL from the given hostname, port and path.
     *
     * @param hostname the host name
     * @param port     the port
     * @param path     the path
     * @return a URL as a {@link URL}
     */
    public static URL createHttpsUrlObject(String hostname, int port, String path) {
        return createUrlObject(HTTPS_PROTOCOL, hostname, port, path);
    }

    /**
     * Extract all of the relevant sections from the given {@code uri}.
     * <p>
     * As an example, if given https://news.bbc.co.uk:8080/a-news-article" this would return the following:
     * <ul>
     * <li>scheme = "https"</li>
     * <li>subDomainName = "news"</li>
     * <li>domainName = "bbc.co.uk"</li>
     * <li>canonicalName = "news.bbc.co.uk"</li>
     * <li>port = 8080</li>
     * <li>path = "a-news-article"</li>
     * </ul>
     *
     * @param url the URL to analyze
     * @return the {@link Components} found or an empty {@link Components} object if the URL was invalid
     * @implNote This method does not check if the URL is valid or not.
     */
    public static Components extractAllFrom(String url) {
        var matcher = URL_PATTERN.matcher(url);

        if (matcher.matches()) {
            var portString = matcher.group(PORT_GROUP);
            var scheme = matcher.group(SCHEME_GROUP);

            int port;

            if (isBlank(portString)) {
                port = defaultPortForScheme(scheme);
            } else {
                port = Integer.parseInt(portString);
            }

            var components = Components.builder()
                    .scheme(scheme)
                    .subDomainName(matcher.group(SUB_DOMAIN_GROUP))
                    .domainName(matcher.group(DOMAIN_GROUP))
                    .canonicalName(matcher.group(CANONICAL_GROUP))
                    .path(matcher.group(PATH_GROUP))
                    .build();

            if (port > 0) {
                components = components.toBuilder()
                        .port(port)
                        .build();
            }

            return components;
        }

        return Components.builder().build();
    }

    /**
     * Trims {@code url} and, if present, strips the trailing slash
     *
     * @param url the URL
     * @return the URL minus any trailing slash
     */
    public static String stripTrailingSlash(String url) {
        var trimmedUrl = url.trim();

        if (trimmedUrl.endsWith("/")) {
            return trimmedUrl.substring(0, trimmedUrl.length() - 1);
        }

        return trimmedUrl;
    }

    /**
     * Trims each URL in {@code urls} and strips any trailing slashes
     *
     * @param urls a list of URLs
     * @return a list of URLs matching the input URLs minus any trailing slash
     */
    public static List<String> stripTrailingSlashes(List<String> urls) {
        return urls.stream().map(KiwiUrls::stripTrailingSlash).collect(toList());
    }

    /**
     * Extracts the canonical server name from a given URL.
     * <p>
     * As an example, if given "https://news.bbc.co.uk:8080/a-news-article" this method would return
     * "news.bbc.co.uk"
     * </p>
     *
     * @param url the URL to evaluate
     * @return an {@link Optional} containing the canonical server name or {@link Optional#empty()} if it could not
     * be found.
     * @implNote This method does not check if the URL is valid or not. Also, if you will need to extract more than
     * one section of the URL, you should instead use {@link KiwiUrls#extractAllFrom(String)}.
     */
    public static Optional<String> extractCanonicalNameFrom(String url) {
        return findGroupInUrl(url, CANONICAL_GROUP);
    }

    /**
     * Extracts the server's domain name from a given URL.
     * <p>
     * As an example, if given "https://news.bbc.co.uk:8080/a-news-article" this method would return
     * "bbc.co.uk"
     * </p>
     *
     * @param url the URL to evaluate
     * @return an {@link Optional} containing the server's domain name or {@link Optional#empty()} if it could not
     * be found.
     * @implNote This method does not check if the URL is valid or not. Also, if you will need to extract more than
     * one section of the URL, you should instead use {@link KiwiUrls#extractAllFrom(String)}.
     */
    public static Optional<String> extractDomainNameFrom(String url) {
        return findGroupInUrl(url, DOMAIN_GROUP);
    }

    /**
     * Extracts the path from a given URL.
     * <p>
     * As an example, if given "https://news.bbc.co.uk:8080/a-news-article" this method would return
     * "a-news-article"
     * </p>
     *
     * @param url the URL to evaluate
     * @return an {@link Optional} containing the path or {@link Optional#empty()} if it could not
     * be found.
     * @implNote This method does not check if the URL is valid or not. Also, if you will need to extract more than
     * one section of the URL, you should instead use {@link KiwiUrls#extractAllFrom(String)}.
     */
    public static Optional<String> extractPathFrom(String url) {
        var result = findGroupInUrl(url, PATH_GROUP);

        if (result.isPresent()) {
            var value = result.get();

            if ("/".equals(value)) {
                return Optional.empty();
            }

            return result;
        }

        return Optional.empty();
    }

    /**
     * Extracts the port from a given URL.
     * <p>
     * As an example, if given "https://news.bbc.co.uk:8080/a-news-article" this method would return
     * "8080" (represented by an int).
     * </p>
     *
     * @param url the URL to evaluate
     * @return an {@link Optional} containing the port or {@link Optional#empty()} if it could not
     * be found.
     * @implNote This method does not check if the URL is valid or not. Also, if you will need to extract more than
     * one section of the URL, you should instead use {@link KiwiUrls#extractAllFrom(String)}.
     */
    public static OptionalInt extractPortFrom(String url) {
        var optionalPort = findGroupInUrl(url, PORT_GROUP);

        var port = optionalPort.map(Integer::parseInt)
                .orElseGet(() -> extractSchemeFrom(url).map(KiwiUrls::defaultPortForScheme).orElse(UNKNOWN_PORT));

        if (port > 0) {
            return OptionalInt.of(port);
        }

        return OptionalInt.empty();
    }

    /**
     * Extracts the scheme from a given URL.
     * <p>
     * As an example, if given "https://news.bbc.co.uk:8080/a-news-article" this method would return
     * "https"
     * </p>
     *
     * @param url the URL to evaluate
     * @return an {@link Optional} containing the canonical server name or {@link Optional#empty()} if it could not
     * be found.
     * @implNote This method does not check if the URL is valid or not. Also, if you will need to extract more than
     * one section of the URL, you should instead use {@link KiwiUrls#extractAllFrom(String)}.
     */
    public static Optional<String> extractSchemeFrom(String url) {
        return findGroupInUrl(url, SCHEME_GROUP);
    }

    /**
     * Extracts the simple server name from a given URL.
     * <p>
     * As an example, if given "https://news.bbc.co.uk:8080/a-news-article" this method would return
     * "news"
     * </p>
     *
     * @param url the URL to evaluate
     * @return an {@link Optional} containing the simple server name or {@link Optional#empty()} if it could not
     * be found.
     * @implNote This method does not check if the URL is valid or not. Also, if you will need to extract more than
     * one section of the URL, you should instead use {@link KiwiUrls#extractAllFrom(String)}.
     */
    public static Optional<String> extractSubDomainNameFrom(String url) {
        return findGroupInUrl(url, SUB_DOMAIN_GROUP);
    }

    /**
     * Searches the {@code commaDelimitedUrls} for its domains, and if found, replaces all entries with
     * {@code replacmentDomain}. The {@code commaDelimitedUrls} can be a standalone URL.
     *
     * @param commaDelimitedUrls the comma delimited URLs to search
     * @param replacementDomain  the domain to replace if found
     * @return the updated comma-delimited URLs if a domain is found, otherwise {@code commaDelimitedUrls} unchanged
     * @implNote This method assumes that the domains are the same for all URLs in the {@code commaDelimitedUrls}; it only
     * checks the first URL to obtain the domain.
     */
    public static String replaceDomainsIn(String commaDelimitedUrls, String replacementDomain) {
        var urls = COMMA_SPLITTER.splitToList(commaDelimitedUrls);
        var originalDomain = domainOrNull(first(urls));

        if (isNull(originalDomain)) {
            return commaDelimitedUrls;
        }

        return urls.stream()
                .map(KiwiUrls::createUrlObject)
                .map(url -> replaceDomain(url, originalDomain, replacementDomain))
                .collect(joining(","));
    }

    private static String domainOrNull(String url) {
        var matcher = URL_PATTERN.matcher(url);

        if (matcher.matches()) {
            var oldDomain = matcher.group(DOMAIN_GROUP);

            if (nonNull(oldDomain)) {
                return oldDomain;
            }
        }

        return null;
    }

    /**
     * Replace {@code expectedDomain} in {@code url} with {@code replacementDomain} if and only if {@code url}'s domain
     * is equal to {@code expectedDomain}. If the given {@code url} has no domain, or is not the expected domain, b
     * lenient and just return the original URL.
     */
    private static String replaceDomain(URL url, String expectedDomain, String replacementDomain) {
        var host = url.getHost();
        var index = host.indexOf('.');
        if (index < 0) {
            return url.toString();
        }

        var actualDomain = host.substring(index + 1);
        if (!actualDomain.equals(expectedDomain)) {
            return url.toString();
        }

        var hostMinusDomain = host.substring(0, index);
        var newHost = hostMinusDomain + '.' + replacementDomain;

        var port = url.getPort() == -1 ? "" : (":" + url.getPort());
        var path = isBlank(url.getPath()) ? "" : url.getPath();

        return url.getProtocol() + "://" + newHost + port + path;
    }

    private static int defaultPortForScheme(String scheme) {
        switch (scheme) {
            case HTTPS_PROTOCOL:
                return 443;
            case HTTP_PROTOCOL:
                return 80;
            case SFTP_PROTOCOL:
                return 22;
            case FTP_PROTOCOL:
                return 21;
            default:
                return UNKNOWN_PORT;
        }
    }

    /**
     * Converts a query string (comprised of key/value pairs separated by '&amp;' characters) into a Map of String of
     * Strings
     *
     * @param queryString the query string to create the map from
     * @return a map of the query params
     * @see #toQueryString(Map) for inverse
     */
    public static Map<String, String> queryStringToMap(String queryString) {
        if (isBlank(queryString)) {
            return new HashMap<>();
        }

        return Arrays.stream(queryString.split("&"))
                .map(keyValue -> keyValue.split("="))
                .collect(toMap(splat -> splat[0], splat -> splat[1]));
    }

    /**
     * Converts a Map of String of Strings into one (potentially long) string of key/value parameters (each key/value
     * parameter is separated by an '=' character), with each parameter pair separated by an '&amp;' character.
     *
     * @param parameters the map of the parameters to create the query string from
     * @return a concatenated query string
     * @see #queryStringToMap(String) for inverse
     */
    public static String toQueryString(Map<String, String> parameters) {
        if (isNullOrEmpty(parameters)) {
            return "";
        }

        return parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining("&"));
    }

    private static Optional<String> findGroupInUrl(String url, String matchGroup) {
        var matcher = URL_PATTERN.matcher(url);

        if (matcher.matches()) {
            return Optional.ofNullable(matcher.group(matchGroup));
        }

        return Optional.empty();
    }

    /**
     * A simple value class to hold the various parts of the URL.
     */
    @Getter
    @Builder(toBuilder = true)
    public static final class Components {

        private final String scheme;
        private final String subDomainName;
        private final String domainName;
        private final String canonicalName;
        private final Integer port;
        private final String path;

        public Optional<Integer> getPort() {
            return Optional.ofNullable(port);
        }

        public Optional<String> getPath() {
            if ("/".equals(path)) {
                return Optional.empty();
            }
            return Optional.ofNullable(path);
        }
    }
}
