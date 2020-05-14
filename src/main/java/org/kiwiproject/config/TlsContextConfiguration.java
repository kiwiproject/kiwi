package org.kiwiproject.config;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import io.dropwizard.client.ssl.TlsConfiguration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kiwiproject.security.KeyAndTrustStoreConfigProvider;
import org.kiwiproject.security.KeyStoreType;
import org.kiwiproject.security.SSLContextProtocol;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // for Builder (b/c also need no-args constructor)
@ToString(exclude = {"keyStorePassword", "trustStorePassword"})
public class TlsContextConfiguration implements KeyAndTrustStoreConfigProvider {

    /**
     * The TLS/SSL protocol to use. Default is {@link SSLContextProtocol#TLS_1_2}.
     *
     * @see SSLContextProtocol
     */
    @NotBlank
    @Builder.Default
    private String protocol = SSLContextProtocol.TLS_1_2.value;

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
    @Builder.Default
    private String keyStoreType = KeyStoreType.JKS.value;

    /**
     * Absolute path tot the trust store.
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
    @Builder.Default
    private String trustStoreType = KeyStoreType.JKS.value;

    /**
     * Should host names be verified when establishing secure connections? Default is {@code true}.
     */
    @Builder.Default
    private boolean verifyHostname = true;

    /**
     * List of supported protocols. It can be {@code null}. See the implementation note for why.
     *
     * @implNote Yes, this is null by default. This is due to the Dropwizard {@link TlsConfiguration} which has this
     * same property null by default; I suspect this is ultimately due to the (unfortunate) way in which Apache
     * HttpClient's {@link org.apache.http.conn.ssl.SSLConnectionSocketFactory} accepts {@code supportedProtocols}
     * in its constructors as arrays that are supposed to be null if you aren't specifying a specific list of them.
     * The HttpClient code does an explicit null check on the {@code supportedProtocols} in
     * {@link org.apache.http.conn.ssl.SSLConnectionSocketFactory#createLayeredSocket(Socket, String, int, org.apache.http.protocol.HttpContext)}.
     * You will need to look at the source code, as the JavaDoc doesn't mention this tidbit, nor do the constructors
     * since they don't have any documentation regarding their arguments. If you don't like reading source code of the
     * open source tools you rely on, then please close this file, log out, and change careers.
     */
    private List<String> supportedProtocols;

    /**
     * Given a Dropwizard {@link TlsConfiguration}, create a new {@link TlsContextConfiguration}.
     * <p>
     * Even though {@link TlsContextConfiguration} does not permit null trust store properties (per the validation
     * annotations), the {@link TlsConfiguration} does. If we encounter this sitation, we will be lenient; even though
     * this could possibly cause downstream problems, we will jsut assume the caller knows what it is doing.
     *
     * @param tlsConfig the Dropwizard TlsConfiguration from which to pull information
     * @return a new TlsContextConfiguration instance
     * @implNote Currently we do not support {@code supportedCiphers} or {@code certAlias}, which Dropwizard does.
     */
    public static TlsContextConfiguration fromDropwizardTlsConfiguration(TlsConfiguration tlsConfig) {
        checkArgumentNotNull(tlsConfig, "TlsConfiguration cannot be null");

        return TlsContextConfiguration.builder()
                .protocol(tlsConfig.getProtocol())
                .keyStorePath(absolutePathOrNull(tlsConfig.getKeyStorePath()))
                .keyStorePassword(tlsConfig.getKeyStorePassword())
                .keyStoreType(tlsConfig.getKeyStoreType())
                .trustStorePath(absolutePathOrNull(tlsConfig.getTrustStorePath()))
                .trustStorePassword(tlsConfig.getTrustStorePassword())
                .trustStoreType(tlsConfig.getTrustStoreType())
                .verifyHostname(tlsConfig.isVerifyHostname())
                .supportedProtocols(tlsConfig.getSupportedProtocols())
                .build();
    }

    private static String absolutePathOrNull(@Nullable File nullableFile) {
        return Optional.ofNullable(nullableFile).map(File::getAbsolutePath).orElse(null);
    }

    /**
     * Convert this {@link TlsContextConfiguration} into a Dropwizard {@link TlsConfiguration} object. Assumes that
     * this object is valid.
     *
     * @return a new Dropwizard TlsConfiguration instance
     * @implNote Requires dropwizard-client as a dependency
     */
    public TlsConfiguration toDropwizardTlsConfiguration() {
        var tlsConfig = new TlsConfiguration();

        tlsConfig.setProtocol(protocol);

        var keyStoreFile = Optional.ofNullable(keyStorePath).map(File::new).orElse(null);
        tlsConfig.setKeyStorePath(keyStoreFile);
        tlsConfig.setKeyStorePassword(keyStorePassword);
        tlsConfig.setKeyStoreType(keyStoreType);

        tlsConfig.setTrustStorePath(new File(trustStorePath));
        tlsConfig.setTrustStorePassword(trustStorePassword);
        tlsConfig.setTrustStoreType(trustStoreType);

        tlsConfig.setVerifyHostname(verifyHostname);
        tlsConfig.setSupportedProtocols(supportedProtocols);

        return tlsConfig;
    }

    /**
     * Convert this configuration into a {@link SSLContextConfiguration}.
     * <p>
     * Note that the conversion is "lossy" since {@link SSLContextConfiguration} does not currently have a separate
     * {@code trustStoreType} property. As a result the key store type in this instance is used to set the
     * {@code keyStoreType} on the returned instance, which is used as both the key and trust store type. Usually this
     * won't be an issue, as (at least in our experience) people normally use the same type of key and trust stores.
     * <p>
     * The {@link SSLContextConfiguration} also does not have {@code supportedProtocols}, so that information is lost
     * in the conversion.
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
                .protocol(protocol)
                .verifyHostname(verifyHostname)
                .build();
    }
}
