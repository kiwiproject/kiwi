package org.kiwiproject.validation;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Validates that a string value is a valid IPV4 address.
 *
 * @implNote The logic here was adapted from the Apache Commons Validator's {@code InetAddressValidator}, re-written
 * in a (somewhat) functional style that (we think) is more understandable and more easily testable since individual
 * checks have been extracted into helper methods.
 */
public class Ipv4AddressValidator implements ConstraintValidator<Ipv4Address, String> {

    private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private static final int IPV4_MAX_OCTET_VALUE = 255;
    private static final int INVALID_IP_SEGMENT = -1;

    private Ipv4Address ipv4Address;

    @Override
    public void initialize(Ipv4Address constraintAnnotation) {
        this.ipv4Address = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return ipv4Address.allowNull();
        }

        var matcher = IPV4_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return false;
        }

        var segments = segmentsOf(matcher);
        return allSegmentsAreValid(segments);
    }

    private Stream<String> segmentsOf(Matcher matcher) {
        return IntStream.rangeClosed(1, matcher.groupCount())
                .mapToObj(matcher::group)
                .toList()
                .stream();
    }

    private boolean allSegmentsAreValid(Stream<String> segmentStream) {
        var invalidSegmentCount = segmentStream.mapToInt(Ipv4AddressValidator::ipSegmentAsInt)
                .filter(Ipv4AddressValidator::isInvalidIpSegment)
                .count();

        return invalidSegmentCount == 0;
    }

    @VisibleForTesting
    static int ipSegmentAsInt(String ipSegment) {
        if (isBlank(ipSegment)) {
            return INVALID_IP_SEGMENT;
        }

        if (hasMoreThanOneDigitStartingWithZero(ipSegment)) {
            return INVALID_IP_SEGMENT;
        }

        try {
            return Integer.parseInt(ipSegment);
        } catch (NumberFormatException e) {
            return INVALID_IP_SEGMENT;
        }
    }

    private static boolean hasMoreThanOneDigitStartingWithZero(String ipSegment) {
        return ipSegment.length() > 1 && ipSegment.startsWith("0");
    }

    @VisibleForTesting
    static boolean isInvalidIpSegment(int ipSegment) {
        return ipSegment < 0 || ipSegment > IPV4_MAX_OCTET_VALUE;
    }
}
