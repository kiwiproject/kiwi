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
     * @return protocol
     * @see SSLContextProtocol
     */
    String getProtocol();

    /**
     * The path to the trust store.
     *
     * @return path to trust store
     */
    String getTrustStorePath();

    /**
     * The trust store password (plain text).
     *
     * @return trust store password
     */
    String getTrustStorePassword();

    /**
     * Trust store type. Default is JKS.
     *
     * @return trust store type
     * @see KeyStoreType#JKS
     */
    default String getTrustStoreType() {
        return KeyStoreType.JKS.value;
    }

    /**
     * Defaults to {@code true}.
     *
     * @return true if hostname verification should be performed
     */
    default boolean isVerifyHostname() {
        return true;
    }

    /**
     * Convert this configuration into a {@link SSLContext}.
     *
     * @return a new SSLContext instance
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
     * @return a new SSLSocketFactory instance
     * @see #toSSLContext()
     */
    default SSLSocketFactory toSslSocketFactory() {
        return toSSLContext().getSocketFactory();
    }
}
