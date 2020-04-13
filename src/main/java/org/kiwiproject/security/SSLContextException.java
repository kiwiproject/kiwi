package org.kiwiproject.security;

/**
 * Exception thrown by Kiwi when error occur creating various security-related objects
 * such as {@link javax.net.ssl.SSLContext}, {@link java.security.KeyStore},
 * {@link javax.net.ssl.KeyManager}, and {@link javax.net.ssl.TrustManager}.
 */
@SuppressWarnings("unused")
public class SSLContextException extends RuntimeException {

    public SSLContextException() {
    }

    public SSLContextException(String message) {
        super(message);
    }

    public SSLContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSLContextException(Throwable cause) {
        super(cause);
    }
}
