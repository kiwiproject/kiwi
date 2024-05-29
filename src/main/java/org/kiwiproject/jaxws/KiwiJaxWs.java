package org.kiwiproject.jaxws;

import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.xml.ws.BindingProvider;
import lombok.experimental.UtilityClass;

import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

/**
 * Some utilities and constants for Jakarta XML Web Services. These make use of specific features in the
 * JDK/Apache CXF implementation in order to provide connect and read timeouts, as well as to provide an
 * {@link SSLSocketFactory}. See the implementation note for more details if you are not using the JDK/Apache CXF
 * implementations.
 *
 * @implNote Note specifically that these use Jakarta XML Web Services vendor extension features that are
 * available only when using the the JDK/Apache CXF implementations. Specifically, we are using constants in
 * {@link JAXWSProperties}. If you are not using the JDK implementation or Apache CXF, this will almost certainly
 * not work. For one thing you won't have JAXWSProperties in your classpath, so will most likely get
 * {@link NoClassDefFoundError} or {@link ClassNotFoundException} at runtime.
 * @see JAXWSProperties#CONNECT_TIMEOUT
 * @see JAXWSProperties#REQUEST_TIMEOUT
 * @see JAXWSProperties#SSL_SOCKET_FACTORY
 * @see BindingProvider
 * @see BindingProvider#ENDPOINT_ADDRESS_PROPERTY
 * @see java.net.HttpURLConnection#setConnectTimeout(int)
 * @see java.net.HttpURLConnection#setReadTimeout(int)
 */
@UtilityClass
public class KiwiJaxWs {

    /**
     * Default value of the connect timeout in milliseconds.
     *
     * @see JAXWSProperties#CONNECT_TIMEOUT
     * @see java.net.HttpURLConnection#setConnectTimeout(int)
     */
    public static final int JAX_WS_DEFAULT_CONNECT_TIMEOUT_MILLIS = 5_000;

    /**
     * Default value of the read timeout in milliseconds.
     *
     * @see JAXWSProperties#REQUEST_TIMEOUT
     * @see java.net.HttpURLConnection#setReadTimeout(int)
     */
    public static final int JAX_WS_DEFAULT_READ_TIMEOUT_MILLIS = 5_000;

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service class with the given service endpoint address, SSL socket
     * factory, and default timeout values.
     *
     * @param clazz              the class type of the specified {@link BindingProvider}
     * @param bindingProvider    the {@link BindingProvider} to configure
     * @param serviceEndpointUri the target service endpoint address
     * @param sslSocketFactory   the SSL socket factory to configure the web service to utilize when making secure calls
     * @param <T>                the web service type
     * @return the configured web service
     * @see #JAX_WS_DEFAULT_CONNECT_TIMEOUT_MILLIS
     * @see #JAX_WS_DEFAULT_READ_TIMEOUT_MILLIS
     */
    public static <T> T configureJdkWebServiceWithDefaultTimeouts(Class<T> clazz,
                                                                  BindingProvider bindingProvider,
                                                                  String serviceEndpointUri,
                                                                  SSLSocketFactory sslSocketFactory) {
        return configureJdkWebService(
                clazz, bindingProvider, serviceEndpointUri, sslSocketFactory,
                JAX_WS_DEFAULT_CONNECT_TIMEOUT_MILLIS, JAX_WS_DEFAULT_READ_TIMEOUT_MILLIS);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service class with the given service endpoint address, SSL socket
     * factory, and specified connect and read timeouts (in milliseconds).
     *
     * @param clazz                the class type of the specified {@link BindingProvider}
     * @param bindingProvider      the {@link BindingProvider} to configure
     * @param serviceEndpointUri   the target service endpoint address
     * @param sslSocketFactory     the SSL socket factory to configure the web service to utilize when making secure calls
     * @param connectTimeoutMillis the connect timeout in milliseconds
     * @param readTimeoutMillis    the read timeout in milliseconds
     * @param <T>                  the web service type
     * @return the configured web service
     */
    public static <T> T configureJdkWebService(Class<T> clazz,
                                               BindingProvider bindingProvider,
                                               String serviceEndpointUri,
                                               SSLSocketFactory sslSocketFactory,
                                               int connectTimeoutMillis,
                                               int readTimeoutMillis) {

        // Non-standard (JDK/Apache CXF) properties
        configureJdkWebServiceSslSocketFactory(bindingProvider, sslSocketFactory);
        configureJdkWebServiceConnectTimeout(bindingProvider, connectTimeoutMillis);
        configureJdkWebServiceReadTimeout(bindingProvider, readTimeoutMillis);

        // Standard properties
        configureJdkWebServiceEndpointAddress(bindingProvider, serviceEndpointUri);

        return clazz.cast(bindingProvider);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the given SSL
     * socket factory. <em>This is a non-standard property specific to the reference implementation.</em>
     *
     * @param bindingProvider  the {@link BindingProvider} to configure
     * @param sslSocketFactory the SSL socket factory to configure the web service to utilize when making secure calls
     * @see JAXWSProperties#SSL_SOCKET_FACTORY
     */
    public static void configureJdkWebServiceSslSocketFactory(BindingProvider bindingProvider,
                                                              SSLSocketFactory sslSocketFactory) {

        bindingProvider.getRequestContext()
                .put(JAXWSProperties.SSL_SOCKET_FACTORY, sslSocketFactory);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the default
     * connect timeout. <em>This is a non-standard property specific to the reference implementation.</em>
     *
     * @param bindingProvider the {@link BindingProvider} to configure
     * @see #JAX_WS_DEFAULT_CONNECT_TIMEOUT_MILLIS
     * @see JAXWSProperties#CONNECT_TIMEOUT
     */
    public static void configureJdkWebServiceDefaultConnectTimeout(BindingProvider bindingProvider) {
        configureJdkWebServiceConnectTimeout(bindingProvider, JAX_WS_DEFAULT_CONNECT_TIMEOUT_MILLIS);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the given
     * connect timeout in milliseconds.
     *
     * @param bindingProvider      the {@link BindingProvider} to configure
     * @param connectTimeoutMillis the connect timeout in milliseconds
     * @see JAXWSProperties#CONNECT_TIMEOUT
     */
    public static void configureJdkWebServiceConnectTimeout(BindingProvider bindingProvider,
                                                            int connectTimeoutMillis) {
        bindingProvider.getRequestContext()
                .put(JAXWSProperties.CONNECT_TIMEOUT, connectTimeoutMillis);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the default
     * read timeout. <em>This is a non-standard property specific to the reference implementation.</em>
     *
     * @param bindingProvider the {@link BindingProvider} to configure
     * @see #JAX_WS_DEFAULT_READ_TIMEOUT_MILLIS
     * @see JAXWSProperties#REQUEST_TIMEOUT
     */
    public static void configureJdkWebServiceDefaultReadTimeout(BindingProvider bindingProvider) {
        configureJdkWebServiceReadTimeout(bindingProvider, JAX_WS_DEFAULT_READ_TIMEOUT_MILLIS);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the given
     * read timeout in milliseconds.
     *
     * @param bindingProvider   the {@link BindingProvider} to configure
     * @param readTimeoutMillis the read timeout in milliseconds
     * @see JAXWSProperties#REQUEST_TIMEOUT
     */
    public static void configureJdkWebServiceReadTimeout(BindingProvider bindingProvider,
                                                         int readTimeoutMillis) {
        bindingProvider.getRequestContext()
                .put(JAXWSProperties.REQUEST_TIMEOUT, readTimeoutMillis);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the given
     * service endpoint URI.
     *
     * @param bindingProvider    the {@link BindingProvider} to configure
     * @param serviceEndpointUri the target service endpoint address
     * @see BindingProvider#ENDPOINT_ADDRESS_PROPERTY
     */
    public static void configureJdkWebServiceEndpointAddress(BindingProvider bindingProvider,
                                                             String serviceEndpointUri) {
        bindingProvider.getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceEndpointUri);
    }

    /**
     * Configure a JDK (e.g. Apache CXF-based) web service {@link BindingProvider} request context with the given
     * service endpoint URI.
     *
     * @param bindingProvider    the {@link BindingProvider} to configure
     * @param serviceEndpointUri the target service endpoint address
     * @see BindingProvider#ENDPOINT_ADDRESS_PROPERTY
     */
    public static void configureJdkWebServiceEndpointAddress(BindingProvider bindingProvider,
                                                             URI serviceEndpointUri) {

        configureJdkWebServiceEndpointAddress(bindingProvider, serviceEndpointUri.toString());
    }
}
