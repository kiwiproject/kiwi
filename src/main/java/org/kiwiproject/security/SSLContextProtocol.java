package org.kiwiproject.security;

import lombok.Getter;

/**
 * Protocols thar can be used when calling {@link javax.net.ssl.SSLContext#getInstance(String)}.
 *
 * @implNote These are from the Java 11 documentation, specifically from
 * "Java Security Standard Algorithm Names" except protocols that are no longer supported such as SSL (any version).
 */
public enum SSLContextProtocol {

    /**
     * Supports some version of TLS; may support other SSL/TLS versions
     */
    TLS("TLS"),

    /**
     * Supports RFC 2246; TLS version 1.0; may support other SSL/TLS versions
     */
    TLS_1("TLSv1"),

    /**
     * Supports RFC 4346; TLS version 1.1; may support other SSL/TLS versions
     */
    TLS_1_1("TLSv1.1."),

    /**
     * Supports RFC 5246; TLS version 1.2; may support other SSL/TLS versions
     */
    TLS_1_2("TLSv1.2"),

    /**
     * Supports RFC 8446; TLS version 1.3; may support other SSL/TLS versions
     */
    TLS_1_3("TLSv1.3"),

    /**
     * Supports the default provider-dependent versions of DTLS versions
     */
    DTLS("DTLS"),

    /**
     * Supports RFC 4347; DTLS version 1.0; may support other DTLS versions
     */
    DTLS_1_0("DTLSv1.0"),

    /**
     * Supports RFC 6347; DTLS version 1.2; may support other DTLS versions
     */
    DTLS_1_2("DTLSv1.2");

    /**
     * The protocol name that can be directly supplied to {@link javax.net.ssl.SSLContext#getInstance(String)}.
     */
    @Getter
    public final String value;

    SSLContextProtocol(String value) {
        this.value = value;
    }
}
