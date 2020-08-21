package org.kiwiproject.validation;

import static java.util.Objects.isNull;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validates that a string matches a regular expression representing an IPv4 address and port separated by a colon.
 * <p>
 * <em>Does not perform any range checks on the IP address segments or port.</em>
 */
public class Ipv4AndPortValidator implements ConstraintValidator<Ipv4AndPort, String> {

    private static final String IPV4_AND_PORT_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3}):\\d+$";
    private static final Pattern IPV4_AND_PORT_PATTERN = Pattern.compile(IPV4_AND_PORT_REGEX);

    private Ipv4AndPort ipv4AndPort;

    @Override
    public void initialize(Ipv4AndPort constraintAnnotation) {
        this.ipv4AndPort = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return ipv4AndPort.allowNull();
        }

        return IPV4_AND_PORT_PATTERN.matcher(value).matches();
    }
}
