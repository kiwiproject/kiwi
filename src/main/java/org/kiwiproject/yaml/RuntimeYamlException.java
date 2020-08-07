package org.kiwiproject.yaml;

/**
 * A runtime exception that can be thrown by {@link YamlHelper}.
 */
public class RuntimeYamlException extends RuntimeException {

    public RuntimeYamlException(String message) {
        super(message);
    }

    public RuntimeYamlException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeYamlException(Throwable cause) {
        super(cause);
    }
}
