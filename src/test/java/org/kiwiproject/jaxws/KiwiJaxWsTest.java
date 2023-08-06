package org.kiwiproject.jaxws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

import jakarta.xml.ws.Binding;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.EndpointReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@DisplayName("KiwiJaxWs")
class KiwiJaxWsTest {

    private static final String ENDPOINT_URI = "https://acme.com/service-foo/FooService?wsdl";

    private BindingProvider provider;
    private SSLSocketFactory socketFactory;

    @BeforeEach
    void setUp() {
        provider = new MockBindingProvider();
        socketFactory = mock(SSLSocketFactory.class);
    }

    @Test
    void shouldConfigureJdkWebServiceWithDefaultTimeouts() {
        var testProvider = KiwiJaxWs.configureJdkWebServiceWithDefaultTimeouts(
                MockBindingProvider.class,
                provider,
                ENDPOINT_URI,
                socketFactory);

        assertThat(testProvider).isSameAs(provider);
        assertThat(testProvider.getRequestContext()).containsOnly(
                entry("com.sun.xml.ws.transport.https.client.SSLSocketFactory", socketFactory),
                entry("com.sun.xml.ws.connect.timeout", 5_000),
                entry("com.sun.xml.ws.request.timeout", 5_000),
                entry(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URI)
        );
    }

    @Test
    void shouldConfigureJdkWebService() {
        var testProvider = KiwiJaxWs.configureJdkWebService(
                MockBindingProvider.class,
                provider,
                ENDPOINT_URI,
                socketFactory,
                10_000,
                15_000);

        assertThat(testProvider).isSameAs(provider);
        assertThat(testProvider.getRequestContext()).containsOnly(
                entry("com.sun.xml.ws.transport.https.client.SSLSocketFactory", socketFactory),
                entry("com.sun.xml.ws.connect.timeout", 10_000),
                entry("com.sun.xml.ws.request.timeout", 15_000),
                entry(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URI)
        );
    }

    @Test
    void shouldConfigureJdkWebServiceDefaultConnectTimeout() {
        KiwiJaxWs.configureJdkWebServiceDefaultConnectTimeout(provider);

        assertThat(provider.getRequestContext()).containsOnly(
                entry("com.sun.xml.ws.connect.timeout", 5_000)
        );
    }

    @Test
    void shouldConfigureJdkWebServiceConnectTimeout() {
        KiwiJaxWs.configureJdkWebServiceConnectTimeout(provider, 7_500);

        assertThat(provider.getRequestContext()).containsOnly(
                entry("com.sun.xml.ws.connect.timeout", 7_500)
        );
    }

    @Test
    void shouldConfigureJdkWebServiceDefaultReadTimeout() {
        KiwiJaxWs.configureJdkWebServiceDefaultReadTimeout(provider);

        assertThat(provider.getRequestContext()).containsOnly(
                entry("com.sun.xml.ws.request.timeout", 5_000)
        );
    }

    @Test
    void shouldConfigureJdkWebServiceReadTimeout() {
        KiwiJaxWs.configureJdkWebServiceReadTimeout(provider, 6_250);

        assertThat(provider.getRequestContext()).containsOnly(
                entry("com.sun.xml.ws.request.timeout", 6_250)
        );
    }

    @Test
    void shouldConfigureJdkWebServiceEndpointAddressFromString() {
        KiwiJaxWs.configureJdkWebServiceEndpointAddress(provider, ENDPOINT_URI);

        assertThat(provider.getRequestContext()).containsOnly(
                entry(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URI)
        );
    }

    @Test
    void shouldConfigureJdkWebServiceEndpointAddressFromURI() {
        KiwiJaxWs.configureJdkWebServiceEndpointAddress(provider, URI.create(ENDPOINT_URI));

        assertThat(provider.getRequestContext()).containsOnly(
                entry(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URI)
        );
    }


    static class MockBindingProvider implements BindingProvider {

        Map<String, Object> requestContext = new HashMap<>();

        Map<String, Object> responseContext = new HashMap<>();

        @Override
        public Map<String, Object> getRequestContext() {
            return requestContext;
        }

        @Override
        public Map<String, Object> getResponseContext() {
            return responseContext;
        }

        @Override
        public Binding getBinding() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EndpointReference getEndpointReference() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
            throw new UnsupportedOperationException();
        }
    }

}
