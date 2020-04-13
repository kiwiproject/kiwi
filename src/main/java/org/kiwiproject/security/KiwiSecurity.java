package org.kiwiproject.security;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

/**
 * Static utilities to create various security objects, such as {@link SSLContext}, {@link KeyStore},
 * {@link KeyManager}, and {@link TrustManager}.
 * <p>
 * WARNING: This is a low-level utility class. As such, many of its methods have a large number of arguments. This
 * is simply a byproduct of the many different objects that are required by the JDK classes to create objects such
 * as {@link SSLContext}. It is intended that this class will be used by other, higher-level, utilities
 * at higher levels of abstraction.
 */
@UtilityClass
@Slf4j
public class KiwiSecurity {

    private static final String ERROR_CREATING_SSL_CONTEXT = "Error creating SSLContext";
    private static final String ALGORITHM_NAME_CANNOT_BE_BLANK = "algorithm cannot be blank";

    /**
     * Create a new {@link SSLContext} instance for the given paths, passwords, and protocol, assuming that the
     * key and trust store types are {@link KeyStoreType#JKS}. Uses the default key and trust manager algorithms
     * as defined by {@link KeyManagerFactory} and {@link TrustManagerFactory}.
     * <p>
     * If only the trust store is needed, supply {@code null} values for the {@code keyStorePath} and
     * {@code keyStorePassword}.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param protocol           the protocol to use
     * @return a new {@link SSLContext} instance
     * @throws SSLContextException if unable to create the {@link SSLContext}
     * @see KeyStore
     * @see KeyManager
     * @see TrustManager
     * @see SSLContextProtocol
     * @see KeyManagerFactory#getDefaultAlgorithm()
     * @see TrustManagerFactory#getDefaultAlgorithm()
     */
    public static SSLContext createSslContext(@Nullable String keyStorePath,
                                              @Nullable String keyStorePassword,
                                              String trustStorePath,
                                              String trustStorePassword,
                                              SSLContextProtocol protocol) {
        checkArgumentNotNull(protocol, "protocol cannot be null");
        return createSslContext(keyStorePath, keyStorePassword, trustStorePath, trustStorePassword, protocol.value);
    }

    /**
     * Create a new {@link SSLContext} instance for the given paths, passwords, and protocol, assuming that the
     * key and trust store types are {@link KeyStoreType#JKS}. Uses the default key and trust manager algorithms
     * as defined by {@link KeyManagerFactory} and {@link TrustManagerFactory}.
     * <p>
     * If only the trust store is needed, supply {@code null} values for the {@code keyStorePath} and
     * {@code keyStorePassword}.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param protocol           the protocol to use
     * @return a new {@link SSLContext} instance
     * @throws SSLContextException if unable to create the {@link SSLContext}
     * @see KeyStore
     * @see KeyManager
     * @see TrustManager
     * @see SSLContextProtocol
     * @see KeyManagerFactory#getDefaultAlgorithm()
     * @see TrustManagerFactory#getDefaultAlgorithm()
     */
    public static SSLContext createSslContext(@Nullable String keyStorePath,
                                              @Nullable String keyStorePassword,
                                              String trustStorePath,
                                              String trustStorePassword,
                                              String protocol) {
        return createSslContext(keyStorePath,
                keyStorePassword,
                KeyStoreType.JKS.value,
                trustStorePath,
                trustStorePassword,
                KeyStoreType.JKS.value,
                protocol);
    }

    /**
     * Create a new {@link SSLContext} instance for the given paths, passwords, and protocol, assuming that the
     * key and trust store types are {@link KeyStoreType#JKS}. Uses the default key and trust manager algorithms
     * as defined by {@link KeyManagerFactory} and {@link TrustManagerFactory}. The key and trust store types should
     * be one of the algorithms defined in {@link KeyStoreType}.
     * <p>
     * If only the trust store is needed, supply {@code null} values for the {@code keyStorePath} and
     * {@code keyStorePassword}.
     * <p>
     * WARNING: While public, this is very low-level and not generally intended for client code to call directly.
     * We recommend using {@link #createSslContext(String, String, String, String, SSLContextProtocol)}
     * or {@link #createSslContext(String, String, String, String, String)}. Kiwi also provides higher-level
     * constructs in the {@link org.kiwiproject.security} package.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param keyStoreType       the key store type
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param trustStoreType     the trust store type
     * @param protocol           the protocol to use
     * @return a new {@link SSLContext} instance
     * @throws SSLContextException if unable to create the {@link SSLContext}
     * @see KeyStore
     * @see KeyManager
     * @see KeyManagerFactory#getDefaultAlgorithm()
     * @see TrustManager
     * @see TrustManagerFactory#getDefaultAlgorithm()
     * @see SSLContextProtocol
     * @see KeyStoreType
     */
    public static SSLContext createSslContext(@Nullable String keyStorePath,
                                              @Nullable String keyStorePassword,
                                              String keyStoreType,
                                              String trustStorePath,
                                              String trustStorePassword,
                                              String trustStoreType,
                                              SSLContextProtocol protocol) {
        checkArgumentNotNull(protocol, "protocol cannot be null");
        return createSslContext(keyStorePath,
                keyStorePassword,
                keyStoreType,
                trustStorePath,
                trustStorePassword,
                trustStoreType,
                protocol.value);
    }

    /**
     * Create a new {@link SSLContext} instance for the given paths, passwords, key and trust store types, and protocol.
     * Uses the default key and trust manager algorithms as defined by {@link KeyManagerFactory} and
     * {@link TrustManagerFactory}. The key and trust store types should be one of the algorithms defined
     * in {@link KeyStoreType}.
     * <p>
     * If only the trust store is needed, supply {@code null} values for the {@code keyStorePath} and
     * {@code keyStorePassword}.
     * <p>
     * WARNING: While public, this is very low-level and not generally intended for client code to call directly.
     * We recommend using {@link #createSslContext(String, String, String, String, SSLContextProtocol)}
     * or {@link #createSslContext(String, String, String, String, String)}. Kiwi also provides higher-level
     * constructs in the {@link org.kiwiproject.security} package.
     *
     * @param keyStorePath       path to the key store
     * @param keyStorePassword   password of the key store
     * @param keyStoreType       the key store type
     * @param trustStorePath     path to the trust store
     * @param trustStorePassword password of the trust store
     * @param trustStoreType     the trust store type
     * @param protocol           the protocol to use
     * @return a new {@link SSLContext} instance
     * @throws SSLContextException if unable to create the {@link SSLContext}
     * @see KeyStore
     * @see KeyManager
     * @see KeyManagerFactory#getDefaultAlgorithm()
     * @see TrustManager
     * @see TrustManagerFactory#getDefaultAlgorithm()
     * @see SSLContextProtocol
     * @see KeyStoreType
     */
    public static SSLContext createSslContext(@Nullable String keyStorePath,
                                              @Nullable String keyStorePassword,
                                              String keyStoreType,
                                              String trustStorePath,
                                              String trustStorePassword,
                                              String trustStoreType,
                                              String protocol) {
        return createSslContext(keyStorePath,
                keyStorePassword,
                keyStoreType,
                KeyManagerFactory.getDefaultAlgorithm(),
                trustStorePath,
                trustStorePassword,
                trustStoreType,
                TrustManagerFactory.getDefaultAlgorithm(),
                protocol);
    }

    /**
     * Create a new {@link SSLContext} instance for the given paths, passwords, key and trust store types, key and
     * trust manager algorithms, and protocol. The key and trust store types should be one of the algorithms
     * defined in {@link KeyStoreType}.
     * <p>
     * If only the trust store is needed, supply {@code null} values for the {@code keyStorePath} and
     * {@code keyStorePassword}.
     * <p>
     * WARNING: While public, this is very low-level and not generally intended for client code to call directly.
     * We recommend using {@link #createSslContext(String, String, String, String, SSLContextProtocol)}
     * or {@link #createSslContext(String, String, String, String, String)}. Kiwi also provides higher-level
     * constructs in the {@link org.kiwiproject.security} package.
     *
     * @param keyStorePath          path to the key store
     * @param keyStorePassword      password of the key store
     * @param keyStoreType          the key store type
     * @param keyManagerAlgorithm   the key manager algorithm
     * @param trustStorePath        path to the trust store
     * @param trustStorePassword    password of the trust store
     * @param trustStoreType        the trust store type
     * @param trustManagerAlgorithm the trust manager algorithm
     * @param protocol              the protocol to use
     * @return a new {@link SSLContext} instance
     * @throws SSLContextException if unable to create the {@link SSLContext}
     * @see KeyStore
     * @see KeyManager
     * @see KeyManagerFactory#getInstance(String)
     * @see TrustManager
     * @see TrustManagerFactory#getInstance(String)
     * @see SSLContextProtocol
     * @see KeyStoreType
     */
    // Suppress Sonar's "Methods should not have too many parameters" as this is intentionally a low-level class
    // and unfortunately creating an SSLContext from scratch requires a large number of objects, parameters, etc.
    @SuppressWarnings("java:S107")
    public static SSLContext createSslContext(@Nullable String keyStorePath,
                                              @Nullable String keyStorePassword,
                                              @Nullable String keyStoreType,
                                              @Nullable String keyManagerAlgorithm,
                                              String trustStorePath,
                                              String trustStorePassword,
                                              String trustStoreType,
                                              String trustManagerAlgorithm,
                                              String protocol) {

        if (isNotBlank(keyStorePath)) {
            checkArgumentNotNull(keyStorePassword, "keyStorePassword cannot be null");
            checkArgumentNotBlank(keyStoreType, "keyStoreType cannot be blank");
            checkArgumentNotBlank(keyManagerAlgorithm, "keyManagerAlgorithm cannot be blank");
        }

        checkArgumentNotBlank(trustStorePath, "trustStorePath cannot be blank");
        checkArgumentNotNull(trustStorePassword, "trustStorePassword cannot be null");
        checkArgumentNotBlank(trustStoreType, "trustStoreType cannot be blank");
        checkArgumentNotBlank(trustManagerAlgorithm, "trustManagerAlgorithm cannot be blank");
        checkArgumentNotBlank(protocol, "protocol cannot be blank");

        try {
            var optionalKeyStore = getKeyStore(keyStoreType, keyStorePath, keyStorePassword);
            var keyManagers = optionalKeyStore
                    .map(store -> getKeyManagers(store, keyStorePassword, keyManagerAlgorithm))
                    .orElse(null);

            var trustStore = getKeyStore(trustStoreType, trustStorePath, trustStorePassword)
                    .orElseThrow(IllegalArgumentException::new);
            var trustManagers = getTrustManagers(trustStore, trustManagerAlgorithm);

            var sslContext = SSLContext.getInstance(protocol);

            // Setting secureRandom parameter as well; the SSLContext will initialize one for us
            sslContext.init(keyManagers, trustManagers, null);

            return sslContext;
        } catch (Exception e) {
            LOG.error(ERROR_CREATING_SSL_CONTEXT, e);
            var cause = unwrapNestedSslContextExceptionOrTake(e);
            throw new SSLContextException(ERROR_CREATING_SSL_CONTEXT, cause);
        }
    }

    private static Throwable unwrapNestedSslContextExceptionOrTake(Exception ex) {
        if (ex instanceof SSLContextException) {
            LOG.trace("Unwrapping nested SSLContextException");
            return ex.getCause();
        }

        return ex;
    }

    /**
     * Return an {@link Optional} containing a {@link KeyStore} for the given {@link KeyStoreType}, path, and
     * password, or an empty {@link Optional} if the arguments are (both) null.
     *
     * @param keyStoreType the type of key store
     * @param path         the path to the key store
     * @param password     the key store password
     * @return an optional with a {@link KeyStore} or an empty optional
     * @throws IllegalArgumentException if keyStoreType is blank
     * @throws SSLContextException      if unable to create a {@link KeyStore}
     * @see KeyStore#getInstance(String)
     */
    public static Optional<KeyStore> getKeyStore(KeyStoreType keyStoreType, String path, String password) {
        checkArgumentNotNull(keyStoreType, "keyStoreType cannot be null");
        return getKeyStore(keyStoreType.value, path, password);
    }

    /**
     * Return an {@link Optional} containing a {@link KeyStore} for the given {@link KeyStoreType}, path, and
     * password, or an empty {@link Optional} if the arguments are (both) null.
     *
     * @param keyStoreType the type of key store
     * @param path         the path to the key store
     * @param password     the key store password
     * @return an optional with a {@link KeyStore} or an empty optional
     * @throws IllegalArgumentException if keyStoreType is blank
     * @throws SSLContextException      if unable to create a {@link KeyStore}
     * @see KeyStore#getInstance(String)
     */
    public static Optional<KeyStore> getKeyStore(String keyStoreType, String path, String password) {
        LOG.trace("Creating {} KeyStore/TrustStore for {}", keyStoreType, path);
        if (isNull(path) && isNull(password)) {
            LOG.debug("No keystore specified");
            return Optional.empty();
        }

        checkArgumentNotBlank(keyStoreType, "keyStoreType cannot be blank");

        try {
            var keyStore = KeyStore.getInstance(keyStoreType);
            var keyStoreUrl = Paths.get(path).toUri().toURL();
            try (var inputStream = keyStoreUrl.openStream()) {
                keyStore.load(inputStream, password.toCharArray());
            }
            return Optional.of(keyStore);
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new SSLContextException("Error getting key store", e);
        }
    }

    /**
     * Get the key managers for the given {@link KeyStore} and key store password using the default algorithm.
     *
     * @param keyStore         the key store
     * @param keyStorePassword the key store's password
     * @return an array of {@link KeyManager}
     * @throws SSLContextException if unable to get the {@link KeyManager} array
     * @see KeyManagerFactory#getDefaultAlgorithm()
     */
    public static KeyManager[] getKeyManagers(KeyStore keyStore, String keyStorePassword) {
        return getKeyManagers(keyStore, keyStorePassword, KeyManagerFactory.getDefaultAlgorithm());
    }

    /**
     * Get the key managers for the given {@link KeyStore}, key store password, and algorithm.
     *
     * @param keyStore         the key store
     * @param keyStorePassword the key store's password
     * @param algorithm        the key manager algorithm
     * @return an array of {@link KeyManager}
     * @throws SSLContextException if unable to get the {@link KeyManager} array
     * @see KeyManagerFactory#getInstance(String)
     */
    public static KeyManager[] getKeyManagers(KeyStore keyStore, String keyStorePassword, String algorithm) {
        checkArgumentNotNull(keyStore, "keyStore cannot be null");
        checkArgumentNotNull(keyStorePassword, "keyStorePassword cannot be null (but can be blank)");
        checkArgumentNotBlank(algorithm, ALGORITHM_NAME_CANNOT_BE_BLANK);

        try {
            var keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            throw new SSLContextException("Error getting key managers", e);
        }
    }

    /**
     * Get the trust managers for the given trust store using the default algorithm.
     *
     * @param trustStore the trust store
     * @return an array of {@link TrustManager}
     * @throws SSLContextException if unable to get the {@link TrustManager} array
     * @see TrustManagerFactory#getDefaultAlgorithm()
     */
    public static TrustManager[] getTrustManagers(KeyStore trustStore) {
        return getTrustManagers(trustStore, TrustManagerFactory.getDefaultAlgorithm());
    }

    /**
     * Get the trust managers for the given trust store and algorithm.
     *
     * @param trustStore the trust store
     * @param algorithm  the trust manager algorithm
     * @return an array of {@link TrustManager}
     * @throws SSLContextException if unable to get the {@link TrustManager} array
     * @see TrustManagerFactory#getInstance(String)
     */
    public static TrustManager[] getTrustManagers(KeyStore trustStore, String algorithm) {
        checkArgumentNotNull(trustStore, "trustStore cannot be null");
        checkArgumentNotBlank(algorithm, ALGORITHM_NAME_CANNOT_BE_BLANK);

        try {
            var trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
            trustManagerFactory.init(trustStore);
            return trustManagerFactory.getTrustManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new SSLContextException("Error getting trust managers", e);
        }
    }
}