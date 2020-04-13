package org.kiwiproject.security;

import javax.net.ssl.SSLContext;

/**
 * Defines a configuration interface for properties needed to create key and trust stores, and a contract to
 * be able to create an {@link SSLContext} and {@link javax.net.ssl.SSLSocketFactory} from this configuration.
 */
public interface KeyAndTrustStoreConfigProvider extends TrustStoreConfigProvider {

    /**
     * The path to the key store.
     */
    String getKeyStorePath();

    /**
     * The key store password (plain text).
     */
    String getKeyStorePassword();

    /**
     * Key store type. Default is JKS.
     *
     * @see KeyStoreType#JKS
     */
    default String getKeyStoreType() {
        return KeyStoreType.JKS.value;
    }

    /**
     * Convert this configuration into a {@link SSLContext}.
     *
     * @see KiwiSecurity#createSslContext(String, String, String, String, String, String, String)
     */
    @Override
    default SSLContext toSSLContext() {
        return KiwiSecurity.createSslContext(
                getKeyStorePath(), getKeyStorePassword(), getKeyStoreType(),
                getTrustStorePath(), getTrustStorePassword(), getTrustStoreType(),
                getProtocol()
        );
    }
}
