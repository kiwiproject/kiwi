package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Validator for the {@link ValidURL} annotation.
 * <p>
 * The validator can be configured to allow or disallow null values, to allow all URL schemes or only
 * specific schemes, and to specify which schemes are allowed.
 *
 * @implNote This validator requires the commons-validator dependency
 * when using it, since it uses Apache Commons Validator's
 * {@link org.apache.commons.validator.routines.UrlValidator} internally.
 */
@Slf4j
public class ValidURLValidator implements ConstraintValidator<ValidURL, CharSequence> {

    /**
     * The URL annotation instance that this validator is validating against.
     */
    private ValidURL validURL;

    /**
     * The Apache Commons Validator's UrlValidator instance used for URL validation.
     */
    private UrlValidator validator;

    /**
     * Initializes the validator with the annotation's properties.
     * <p>
     * This method is called by the Bean Validation API before validation begins.
     * It configures the internal {@link UrlValidator} based on the annotation's properties.
     *
     * @param constraintAnnotation the annotation instance
     */
    @Override
    public void initialize(ValidURL constraintAnnotation) {
        this.validURL = constraintAnnotation;

        var options = buildOptionsForCommonsUrlValidator(this.validURL);

        if (validURL.allowAllSchemes()) {
            this.validator = new UrlValidator(options);
        } else {
            this.validator = new UrlValidator(validURL.allowSchemes(), options);
        }
    }

    private static long buildOptionsForCommonsUrlValidator(ValidURL url) {
        var options = url.allowAllSchemes() ? UrlValidator.ALLOW_ALL_SCHEMES : 0L;

        if (url.allowLocalUrls()) {
            options += UrlValidator.ALLOW_LOCAL_URLS;
        }

        if (url.allowTwoSlashes()) {
            options += UrlValidator.ALLOW_2_SLASHES;
        }

        if (!url.allowFragments()) {
            options += UrlValidator.NO_FRAGMENTS;
        }

        return options;
    }

    /**
     * Validates that the given value is a valid URL.
     * <p>
     * The validation process consists of the following steps:
     * <ol>
     *   <li>If the value is null, it is valid only if {@link ValidURL#allowNull()} is true.</li>
     *   <li>The value is validated using Apache Commons Validator's {@link UrlValidator}.</li>
     *   <li>If the value passes the initial validation, additional edge cases are checked.</li>
     * </ol>
     *
     * @param value   the value to validate
     * @param context the constraint validator context
     * @return true if the value is a valid URL, false otherwise
     */
    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return validURL.allowNull();
        }

        var string = value.toString();
        var valid = validator.isValid(string);

        if (!valid) {
            LOG.trace("'{}' is NOT valid according to UrlValidator. Skip edge-case checks.", string);
            return false;
        }

        LOG.trace("'{}' is valid according to UrlValidator. Check edge-cases.", string);

        return checkEdgeCases(string, validURL.allowLocalUrls());
    }

    /**
     * Checks for edge cases that the {@link UrlValidator} might miss.
     * <p>
     * This method performs additional validation checks that are not covered by the
     * Apache Commons Validator's {@link UrlValidator}. Currently, it checks:
     * <ul>
     *   <li>If the URL can be parsed as a {@link URI}</li>
     *   <li>If the authority component ends with a colon but no port</li>
     *   <li>If the URL hostname is {@code 127.0.0.1} and allowLocalUrls is false</li>
     *   <li>If the URL hostname {@code ::1} and allowLocalUrls is false</li>
     * </ul>
     *
     * @param string the URL string to check
     * @return true if the URL passes all edge case checks, false otherwise
     */
    @VisibleForTesting
    static boolean checkEdgeCases(String string, boolean allowLocalUrls) {
        var result = parseUri(string);
        var error = result.error();
        if (nonNull(error)) {
            LOG.trace("UrlValidator reported valid, but URI#create threw exception for '{}'", string, error);
            return false;
        }

        var uri = result.uri();

        // Edge case 1 - Don't let authority end with a colon but no port
        var authority = uri.getAuthority();
        if (nonNull(authority) && authority.endsWith(":")) {
            LOG.trace("Authority should not end with a colon but no port: '{}'", authority);
            return false;
        }

        // Edge case 2 - If not allowing local URLS, check for loopback IPs.
        //  UrlValidator doesn't consider these as local addresses.
        var host = uri.getHost();
        if (!allowLocalUrls && nonNull(host)) {

            // don't allow IPv4 or IPv6 loopback
            var loopbackResult = checkLoopbackIpAddress(host);
            if (loopbackResult.isLoopbackOrError()) {
                LOG.trace("Loopback check for host '{}' failed. Is loopback? {}, Error? {}",
                        host,
                        loopbackResult.isLoopback(),
                        loopbackResult.isError(),
                        loopbackResult.error());
                return false;
            }
        }

        return true;
    }

    private record UriParseResult(URI uri, Exception error) {
    }

    private static UriParseResult parseUri(String string) {
        try {
            return new UriParseResult(URI.create(string), null);
        } catch (Exception e) {
            return new UriParseResult(null, e);
        }
    }

    @VisibleForTesting
    record LoopbackCheckResult(boolean isLoopback, Exception error) {
        boolean isLoopbackOrError() {
            return isLoopback || isError();
        }
        
        boolean isError() {
            return nonNull(error);
        }
    }

    @VisibleForTesting
    static LoopbackCheckResult checkLoopbackIpAddress(String host) {
        try {
            var isLoopback = InetAddress.getByName(host).isLoopbackAddress();
            return new LoopbackCheckResult(isLoopback, null);
        } catch (UnknownHostException e) {
            LOG.trace("Error getting InetAddress by name for host: {}", host, e);
            return new LoopbackCheckResult(false, e);
        }
    }
}
