package org.kiwiproject.config;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import io.dropwizard.client.ssl.TlsConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.kiwiproject.base.KiwiBooleans;
import org.kiwiproject.security.KeyAndTrustStoreConfigProvider;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;

import java.beans.ConstructorProperties;
import java.io.File;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for standard/common properties required for secure TLS connections.
 * <p>
 * As this is a configuration class that supports population from external configuration, it is mutable
 * <p>
 * NOTE: This requires dropwizard-client as a dependency.
 */
@Getter
@Setter
@ToString(exclude = { "keyStorePassword", "trustStorePassword" })
public class TlsContextConfiguration implements KeyAndTrustStoreConfigProvider {

    /**
     * The TLS/SSL protocol to use. Default is {@link SSLContextProtocol#TLS_1_2}.
     *
     * @see SSLContextProtocol
     */
    @NotBlank
    private String protocol;

    /**
     * The name of the JCE (Java Cryptography Extension) provider to use on the client side for cryptographic
     * support (for example, SunJCE, Conscrypt, BC, etc.).
     * <p>
     * For more details, see the "Java Cryptography Architecture (JCA) Reference Guide" section of the Java
     * <a href="https://docs.oracle.com/en/java/javase/20/security/java-security-overview1.html">Security Developer’s Guide</a>.
     */
    private String provider;

    /**
     * Absolute path to the key store.
     */
    private String keyStorePath;

    /**
     * Key store password.
     */
    private String keyStorePassword;

    /**
     * Key store type. Defaults to {@link KeyStoreType#JKS}.
     *
     * @see KeyStoreType
     */
    @NotBlank
    private String keyStoreType;

    /**
     * The name of the provider for the key store, i.e., the value of {@code provider} to use when getting the
     * {@link java.security.KeyStore} instance for the key store.
     * <p>
     * For more details, see the "Java Cryptography Architecture (JCA) Reference Guide" section of the Java
     * <a href="https://docs.oracle.com/en/java/javase/20/security/java-security-overview1.html">Security Developer’s Guide</a>.
     *
     * @see java.security.KeyStore#getInstance(String, String)
     */
    private String keyStoreProvider;

    /**
     * Absolute path to the trust store.
     */
    @NotBlank
    private String trustStorePath;

    /**
     * Trust store password.
     */
    @NotNull
    private String trustStorePassword;

    /**
     * Trust store type. Defaults to {@link KeyStoreType#JKS}.
     *
     * @see KeyStoreType
     */
    @NotBlank
    private String trustStoreType;

    /**
     * The name of the provider for the trust store, i.e., the value of {@code provider} to use when getting the
     * {@link java.security.KeyStore} instance for the trust store.
     * <p>
     * For more details, see the "Java Cryptography Architecture (JCA) Reference Guide" section of the Java
     * <a href="https://docs.oracle.com/en/java/javase/20/security/java-security-overview1.html">Security Developer’s Guide</a>.
     *
     * @see java.security.KeyStore#getInstance(String, String)
     */
    private String trustStoreProvider;

    /**
     * Whether self-signed certificates should be trusted. Default is {@code false}.
     */
    private boolean trustSelfSignedCertificates;

    /**
     * Should host names be verified when establishing secure connections? Default is {@code true}.
     */
    private boolean verifyHostname;

    /**
     * Whether the SNI (Server Name Indication) host check is disabled. Default is {@code false}
     *
     * @see <a href="https://www.cloudflare.com/learning/ssl/what-is-sni/">What is SNI? How TLS server name indication works</a>
     */
    private boolean disableSniHostCheck;

    /**
     * List of supported protocols. It can be {@code null}. See the implementation note for why.
     *
     * @implNote Yes, this is null by default. This is due to the Dropwizard {@link TlsConfiguration} which has this
     * same property null by default; I suspect this is ultimately due to the (unfortunate) way in which Apache
     * HttpClient's {@link org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory} accepts {@code supportedProtocols}
     * in its constructors as arrays that are supposed to be null if you aren't specifying a specific list of them.
     * The HttpClient code does an explicit null check on the {@code supportedProtocols} in
     * {@link org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory#createLayeredSocket(Socket, String, int, org.apache.hc.core5.http.protocol.HttpContext)}.
     * You will need to look at the source code, as the Javadoc doesn't mention this tidbit, nor do the constructors
     * since they don't have any documentation regarding their arguments. If you don't like reading source code of the
     * open-source tools you rely on, then please close this file, log out, and change careers.
     */
    private List<String> supportedProtocols;

    /**
     * A list of cipher suites (e.g., TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256) which are supported.
     * All other cipher suites will be refused.
     * <p>
     * Note that this can be {@code null} for similar reason as {@code supportedProtocols}. See the implementation
     * note on {@code supportedProtocols}.
     */
    private List<String> supportedCiphers;

    /**
     * The alias of a specific client certificate to present when authenticating. Use this when the specified
     * keystore has multiple certificates to force use of a non-default certificate.
     */
    private String certAlias;

    /**
     * Create a new instance that can be configured via the setter methods.
     * <p>
     * When creating programmatically, consider using the {@link #builder()} instead.
     */
    public TlsContextConfiguration() {
        // Call the all-args constructor so that the default values
        // are respected. This avoids having to put default values
        // on the fields too, which would duplicate the defaults
        // that are set in the all-args constructor. Yes, the code
        // is not as pretty, but there is less chance of changing
        // one default value, and forgetting to change the other.
        this(null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    /**
     * This all-arguments constructor is annotated with Lombok {@link Builder} to
     * provide a Lombok-generated builder and to handle deserialization that uses
     * this constructor despite it being private (Hi Jackson!). This allows us to ensure
     * that the proper default values are set when we don't get a value, e.g., when
     * the JSON or YAML does not specify a value. This is also why the arguments for
     * boolean fields are Boolean, so that we can detect when we got a value.
     * <p>
     * It also uses the {@link ConstructorProperties} annotation to help deserializers
     * like Jackson, which use the all-args constructor.
     * <p>
     * Note we suppress Sonar's rule (java:S107) about too many arguments as it cannot be
     * avoided, and also IntelliJ's "unused" warning, since it is called reflectively
     * during deserialization.
     */
    @SuppressWarnings({ "java:S107", "unused" })
    @ConstructorProperties({
            "protocol", "provider", "keyStorePath", "keyStorePassword", "keyStoreType", "keyStoreProvider",
            "trustStorePath", "trustStorePassword", "trustStoreType", "trustStoreProvider",
            "trustSelfSignedCertificates", "verifyHostname", "disableSniHostCheck", "supportedProtocols",
            "supportedCiphers", "certAlias" })
    @Builder
    private TlsContextConfiguration(String protocol,
                                    String provider,
                                    String keyStorePath,
                                    String keyStorePassword,
                                    String keyStoreType,
                                    String keyStoreProvider,
                                    String trustStorePath,
                                    String trustStorePassword,
                                    String trustStoreType,
                                    String trustStoreProvider,
                                    Boolean trustSelfSignedCertificates,
                                    Boolean verifyHostname,
                                    Boolean disableSniHostCheck,
                                    List<String> supportedProtocols,
                                    List<String> supportedCiphers,
                                    String certAlias) {
        this.protocol = isNull(protocol) ? SSLContextProtocol.TLS_1_2.value : protocol;
        this.provider = provider;
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreType = isNull(keyStoreType) ? KeyStoreType.JKS.value : keyStoreType;
        this.keyStoreProvider = keyStoreProvider;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.trustStoreType = isNull(trustStoreType) ? KeyStoreType.JKS.value : trustStoreType;
        this.trustStoreProvider = trustStoreProvider;
        this.trustSelfSignedCertificates = KiwiBooleans.toBooleanOrFalse(trustSelfSignedCertificates);
        this.verifyHostname = KiwiBooleans.toBooleanOrTrue(verifyHostname);
        this.disableSniHostCheck = KiwiBooleans.toBooleanOrFalse(disableSniHostCheck);
        this.supportedProtocols = supportedProtocols;
        this.supportedCiphers = supportedCiphers;
        this.certAlias = certAlias;
    }

    /**
     * Given a Dropwizard {@link TlsConfiguration}, create a new {@link TlsContextConfiguration}.
     * <p>
     * Even though {@link TlsContextConfiguration} does not permit null trust store properties (per the validation
     * annotations), the {@link TlsConfiguration} does. If we encounter this situation, we will be lenient; even though
     * this could possibly cause downstream problems, we will just assume the caller knows what it is doing.
     * <p>
     * The Dropwizard {@link TlsConfiguration} class does not contain a {@code disableSniHostCheck} property, so
     * it cannot transfer and is therefore ignored during conversions. Also note that it is set to {@code false}
     * in the returned {@link TlsContextConfiguration} since that is the more secure option.
     *
     * @param tlsConfig the Dropwizard TlsConfiguration from which to pull information
     * @return a new TlsContextConfiguration instance
     * @implNote Currently we do not support {@code supportedCiphers} or {@code certAlias}, which Dropwizard does.
     */
    public static TlsContextConfiguration fromDropwizardTlsConfiguration(TlsConfiguration tlsConfig) {
        checkArgumentNotNull(tlsConfig, "TlsConfiguration cannot be null");

        return TlsContextConfiguration.builder()
                .protocol(tlsConfig.getProtocol())
                .provider(tlsConfig.getProvider())
                .keyStorePath(absolutePathOrNull(tlsConfig.getKeyStorePath()))
                .keyStorePassword(tlsConfig.getKeyStorePassword())
                .keyStoreType(tlsConfig.getKeyStoreType())
                .keyStoreProvider(tlsConfig.getKeyStoreProvider())
                .trustStorePath(absolutePathOrNull(tlsConfig.getTrustStorePath()))
                .trustStorePassword(tlsConfig.getTrustStorePassword())
                .trustStoreType(tlsConfig.getTrustStoreType())
                .trustStoreProvider(tlsConfig.getTrustStoreProvider())
                .trustSelfSignedCertificates(tlsConfig.isTrustSelfSignedCertificates())
                .verifyHostname(tlsConfig.isVerifyHostname())
                .disableSniHostCheck(false)
                .supportedProtocols(tlsConfig.getSupportedProtocols())
                .supportedCiphers(tlsConfig.getSupportedCiphers())
                .certAlias(tlsConfig.getCertAlias())
                .build();
    }

    private static String absolutePathOrNull(@Nullable File nullableFile) {
        return Optional.ofNullable(nullableFile).map(File::getAbsolutePath).orElse(null);
    }

    /**
     * Convert this {@link TlsContextConfiguration} into a Dropwizard {@link TlsConfiguration} object. Assumes that
     * this object is valid.
     * <p>
     * The Dropwizard {@link TlsConfiguration} class does not contain a {@code disableSniHostCheck} property, so
     * it cannot transfer and is therefore ignored during conversions.
     *
     * @return a new Dropwizard TlsConfiguration instance
     * @implNote Requires dropwizard-client as a dependency
     */
    public TlsConfiguration toDropwizardTlsConfiguration() {
        var tlsConfig = new TlsConfiguration();

        tlsConfig.setProtocol(protocol);
        tlsConfig.setProvider(provider);

        var keyStoreFile = Optional.ofNullable(keyStorePath).map(File::new).orElse(null);
        tlsConfig.setKeyStorePath(keyStoreFile);
        tlsConfig.setKeyStorePassword(keyStorePassword);
        tlsConfig.setKeyStoreType(keyStoreType);
        tlsConfig.setKeyStoreProvider(keyStoreProvider);

        tlsConfig.setTrustStorePath(new File(trustStorePath));
        tlsConfig.setTrustStorePassword(trustStorePassword);
        tlsConfig.setTrustStoreType(trustStoreType);
        tlsConfig.setTrustStoreProvider(trustStoreProvider);

        tlsConfig.setTrustSelfSignedCertificates(trustSelfSignedCertificates);
        tlsConfig.setVerifyHostname(verifyHostname);
        tlsConfig.setSupportedProtocols(supportedProtocols);
        tlsConfig.setSupportedCiphers(supportedCiphers);
        tlsConfig.setCertAlias(certAlias);

        return tlsConfig;
    }

    /**
     * Convert this configuration into a {@link SSLContextConfiguration}.
     * <p>
     * Note that {@link SSLContextConfiguration} does not have {@code provider}, {@code keyStoreProvider},
     * {@code trustStoreProvider}, {@code trustSelfSignedCertificates}, {@code supportedProtocols},
     * {@code supportedCiphers}, or {@code certAlias}. As a result, this is a "lossy" conversion since it loses
     * these values.
     *
     * @return the new SSLContextConfiguration instance
     */
    public SSLContextConfiguration toSslContextConfiguration() {
        return SSLContextConfiguration.builder()
                .keyStorePath(keyStorePath)
                .keyStorePassword(keyStorePassword)
                .keyStoreType(keyStoreType)
                .trustStorePath(trustStorePath)
                .trustStorePassword(trustStorePassword)
                .trustStoreType(trustStoreType)
                .protocol(protocol)
                .verifyHostname(verifyHostname)
                .disableSniHostCheck(disableSniHostCheck)
                .build();
    }
}
