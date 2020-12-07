package org.kiwiproject.jaxrs.client;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.kiwiproject.collect.KiwiArrays;
import org.kiwiproject.collect.KiwiLists;
import org.kiwiproject.collect.KiwiMaps;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Use with JAX-RS {@link WebTarget} instances to provide convenient functionality when adding query parameters.
 * <p>
 * Usage example (assuming {@link WebTargetClientHelper#withClient(Client) withClient} is statically imported):
 * <pre>
 * withClient(client).target("/search")
 *         .queryParamRequireNotBlank("q", query)
 *         .queryParamIfNotBlank("sort", sort)
 *         .queryParamIfNotBlank("page", page)
 *         .queryParamIfNotBlank("limit", limit)
 *         .queryParamFilterNotBlank("langs", langs);
 * </pre>
 * <h3>Limitations</h3>
 * This is a <strong>limited</strong> wrapper around {@link WebTarget} that provides enhanced functionality only for
 * adding query parameters. Only the methods defined in this class are chainable, i.e. once you call a method defined
 * in the regular {@link Client} interface, you leave the {@link WebTargetHelper} context.
 * <p>
 * For example you can <strong>NOT</strong> do this:
 * <pre>
 * withClient(client).target("/search")
 *         .queryParamRequireNotBlank("q", query)
 *         .queryParam("sort", sort)  // after this, only Client methods are accessible!!! WON'T COMPILE
 *         .queryParamIfNotBlank("page", page)
 *         .queryParamIfNotBlank("limit", limit)
 *         .queryParamFilterNotBlank("langs", langs);
 * </pre>
 * With the current basic implementation, this means certain usages will be awkward. For example, when using
 * both parameter templates and query parameters, the query parameters need to be added first, for the reason
 * given above about leaving the {@link WebTargetHelper} context. For example:
 * <pre>
 * var response = withClient(client).target("/users/{userId}/trades/{tradeId}")
 *         .queryParamIfNotBlank("displayCurrency", currency)
 *         .queryParamIfNotNull("showLimitPrice", showLimitPrice)
 *         .resolveTemplate("userId", userId)  // after this, only Client methods are accessible!!!
 *         .resolveTemplate("tradeId", tradeId)
 *         .request()
 *         .get();
 * </pre>
 * One way to get around this restriction is to use methods from {@link WebTarget} as normal, and then wrap it
 * with a {@link WebTargetHelper} to add query parameters. The above example would then look like:
 * <pre>
 * var pathResolvedTarget = client.target("/users/{userId}/trades/{tradeId}")
 *         .resolveTemplate("userId", userId)
 *         .resolveTemplate("tradeId", tradeId);
 *
 * var response = withWebTarget(pathResolvedTarget)
 *         .queryParamIfNotBlank("displayCurrency", currency)
 *         .queryParamIfNotNull("showLimitPrice", showLimitPrice)
 *         .request()
 *         .get();
 * </pre>
 * This usage allows for full functionality of {@link WebTarget} while still getting the enhanced query parameter
 * features of this class. It isn't perfect but it works and, in our opinion anyway, doesn't intrude too much on
 * building JAX-RS requests. In other words, we think it is a decent trade off.
 *
 * @implNote Internally this uses Lombok's {@link Delegate}, which is why this class doesn't implement {@link WebTarget}
 * directly. While this lets us easily delegate method calls to a {@link WebTarget}, it also restricts what we can do
 * here, and is the primary reason why there are usage restrictions. However, in our general usage this implementation
 * has been enough for our needs. Nevertheless this is currently marked with the Guava {@link Beta} annotation in case
 * we change our minds on the implementation.
 */
@Beta
public class WebTargetHelper {

    @Delegate
    private final WebTarget webTarget;

    /**
     * Package-private constructor. Used by {@link WebTargetClientHelper}.
     *
     * @param webTarget the WebTarget to wrap
     */
    WebTargetHelper(WebTarget webTarget) {
        this.webTarget = requireNotNull(webTarget);
    }

    /**
     * @return the wrapped WebTarget
     */
    @VisibleForTesting
    WebTarget wrapped() {
        return webTarget;
    }

    /**
     * Create a new instance with the given {@link WebTarget}.
     *
     * @param webTarget the WebTarget to use
     * @return a new instance
     */
    public static WebTargetHelper withWebTarget(WebTarget webTarget) {
        return new WebTargetHelper(webTarget);
    }

    /**
     * Add the required query parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this instance
     * @throws IllegalArgumentException if value is null
     */
    public WebTargetHelper queryParamRequireNotNull(String name, Object value) {
        checkArgumentNotNull(value, "value cannot be null for parameter %s", name);

        var newWebTarget = webTarget.queryParam(name, value);
        return new WebTargetHelper(newWebTarget);
    }

    /**
     * Add the given query parameter only if it is not null.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this instance
     */
    public WebTargetHelper queryParamIfNotNull(String name, Object value) {
        var newWebTarget = this.webTarget.queryParam(name, value);

        return isNull(value) ? this : new WebTargetHelper(newWebTarget);
    }

    /**
     * Adds any non-null values to the the given query parameter.
     *
     * @param name   the parameter name
     * @param values one or more parameter values
     * @return this instance
     */
    public WebTargetHelper queryParamFilterNotNull(String name, Object... values) {
        if (KiwiArrays.isNullOrEmpty(values)) {
            return this;
        }

        return queryParamFilterNotNull(name, Arrays.stream(values));
    }

    /**
     * Adds any non-null values to the the given query parameter.
     *
     * @param name   the parameter name
     * @param values one or more parameter values
     * @return this instance
     */
    public WebTargetHelper queryParamFilterNotNull(String name, List<Object> values) {
        if (KiwiLists.isNullOrEmpty(values)) {
            return this;
        }

        return queryParamFilterNotNull(name, values.stream());
    }

    /**
     * Adds any non-null values to the the given query parameter.
     *
     * @param name   the parameter name
     * @param stream containing one or more parameter values
     * @return this instance
     */
    public WebTargetHelper queryParamFilterNotNull(String name, Stream<Object> stream) {
        if (isNull(stream)) {
            return this;
        }

        var nonNullValues = stream
                .filter(Objects::nonNull)
                .toArray();

        var newWebTarget = webTarget.queryParam(name, nonNullValues);
        return new WebTargetHelper(newWebTarget);
    }

    /**
     * Add the required query parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this instance
     * @throws IllegalArgumentException if value is blank
     */
    public WebTargetHelper queryParamRequireNotBlank(String name, String value) {
        checkArgumentNotBlank(value, "value cannot be blank for parameter %s", name);

        var newWebTarget = webTarget.queryParam(name, value);
        return new WebTargetHelper(newWebTarget);
    }

    /**
     * Add the given query parameter only if it is not blank.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this instance
     */
    public WebTargetHelper queryParamIfNotBlank(String name, String value) {
        var newWebTarget = this.webTarget.queryParam(name, value);

        return isBlank(value) ? this : new WebTargetHelper(newWebTarget);
    }

    /**
     * Adds any non-blank values to the the given query parameter.
     *
     * @param name   the parameter name
     * @param values one or more parameter values
     * @return this instance
     */
    public WebTargetHelper queryParamFilterNotBlank(String name, String... values) {
        if (KiwiArrays.isNullOrEmpty(values)) {
            return this;
        }

        return queryParamFilterNotBlank(name, Arrays.stream(values));
    }

    /**
     * Adds any non-blank values to the the given query parameter.
     *
     * @param name   the parameter name
     * @param values one or more parameter values
     * @return this instance
     */
    public WebTargetHelper queryParamFilterNotBlank(String name, List<String> values) {
        if (KiwiLists.isNullOrEmpty(values)) {
            return this;
        }

        return queryParamFilterNotBlank(name, values.stream());
    }

    /**
     * Adds any non-blank values to the the given query parameter.
     *
     * @param name   the parameter name
     * @param stream containing one or more parameter values
     * @return this instance
     */
    public WebTargetHelper queryParamFilterNotBlank(String name, Stream<String> stream) {
        if (isNull(stream)) {
            return this;
        }

        var nonBlankValues = stream
                .filter(StringUtils::isNotBlank)
                .toArray();

        var newWebTarget = webTarget.queryParam(name, nonBlankValues);
        return new WebTargetHelper(newWebTarget);
    }

    /**
     * Adds non-null query parameters from the given map. All map keys <strong>must</strong> be non-null.
     *
     * @param parameters a map representing the query parameters
     * @return this instance
     */
    public WebTargetHelper queryParams(Map<String, Object> parameters) {
        if (KiwiMaps.isNullOrEmpty(parameters)) {
            return this;
        }

        var targetHelper = this;
        for (var entry : parameters.entrySet()) {
            targetHelper = targetHelper.queryParamIfNotNull(entry.getKey(), entry.getValue());
        }

        // NOTE: The above is effectively a foldLeft, which Java Streams does not have. The 3-arg reduce version is a lot
        // more difficult to understand than a simple loop with a mutable variable that we keep replacing. In addition,
        // the reduce version cannot be strictly correct,  since we cannot define a combiner function which is "an
        // associative, non-interfering, stateless function for combining" two WebTargetHelper instances. Instead, we
        // would require it is only used on a sequential (non-parallel) stream. Regardless, the implementation is less
        // clear than just a loop with a mutable variable, which is why this is not using the streams API. While the
        // lovely StreamEx library does have a foldLeft where the seed and accumulator have differing types, it is not
        // worth adding a hard dependency on that library for one function.
        // See https://stackoverflow.com/questions/24308146/why-is-a-combiner-needed-for-reduce-method-that-converts-type-in-java-8

        return targetHelper;
    }

    /**
     * Adds non-null query parameters from the given multivalued map. All map keys <strong>must</strong> be non-null.
     *
     * @param parameters a multivalued representing the query parameters
     * @return this instance
     */
    public WebTargetHelper queryParams(MultivaluedMap<String, Object> parameters) {
        if (isNull(parameters) || parameters.isEmpty()) {
            return this;
        }

        // NOTE: This is effectively a foldLeft, which Java Streams does not have. See explanation in method above.
        var targetHelper = this;
        for (var entry : parameters.entrySet()) {
            targetHelper = targetHelper.queryParamFilterNotNull(entry.getKey(), entry.getValue());
        }

        return targetHelper;
    }

}
