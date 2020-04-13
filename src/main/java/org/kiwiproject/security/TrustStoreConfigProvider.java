package org.kiwiproject.security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Defines a configuration interface for properties needed to create trust stores, and a contract to be able to create
 * a {@link javax.net.ssl.SSLContext} and {@link javax.net.ssl.SSLSocketFactory} from this configuration.
 */
public interface TrustStoreConfigProvider {

    /**
     * The protocol to use. Consider using {@link SSLContextProtocol} to ensure valid protocols.
     *
     * @see SSLContextProtocol
     */
    String getProtocol();

    /**
     * The path to the trust store.
     */
    String getTrustStorePath();

    /**
     * The trust store password (plain text).
     */
    String getTrustStorePassword();

    /**
     * Trust store type. Default is JKS.
     *
     * @see KeyStoreType#JKS
     */
    default String getTrustStoreType() {
        return KeyStoreType.JKS.value;
    }

    /**
     * Defaults to {@code true}/.
     */
    default boolean isVerifyHostname() {
        return true;
    }

    /**
     * Convert this configuration into a {@link SSLContext}.
     *
     * @see KiwiSecurity#createSslContext(String, String, String, String, String, String, String)
     */
    default SSLContext toSSLContext() {
        return KiwiSecurity.createSslContext(
                null, null, null,
                getTrustStorePath(), getTrustStorePassword(),
                getTrustStoreType(),
                getProtocol()
        );
    }

    /**
     * Convert this configuration into a {@link SSLSocketFactory}.
     *
     * @see #toSSLContext()
     */
    default SSLSocketFactory toSslSocketFactory() {
        return toSSLContext().getSocketFactory();
    }
}
