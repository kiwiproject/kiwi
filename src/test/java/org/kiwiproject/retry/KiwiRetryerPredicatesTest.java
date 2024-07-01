package org.kiwiproject.retry;

import static com.google.common.base.Verify.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.base.KiwiStrings.f;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.SSLHandshakeException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @implNote The nested classes ending in "_Predicate" and extending {@link AbstractExceptionPredicateTest}
 * automatically execute all the tests defined in that base class for a given predicate. The steps for adding
 * a new predicate are:
 * <ol>
 *     <li>Create a {@link Nested} class named as "[PREDICATE_NAME]_Predicate"</li>
 *     <li>Make the class extend {@link AbstractExceptionPredicateTest}</li>
 *     <li>Implement the required getter methods (easiest to use Lombok to generate the getter methods)</li>
 * </ol>
 */
@DisplayName("KiwiRetryerPredicates")
class KiwiRetryerPredicatesTest {

    @Nested
    @Getter
    class UNKNOWN_HOST_Predicate extends AbstractExceptionPredicateTest {
        private final Exception exception = new UnknownHostException("This host is not known to me!");
        private final Predicate<Exception> predicate = KiwiRetryerPredicates.UNKNOWN_HOST;
    }

    @Nested
    @Getter
    class CONNECTION_ERROR_Predicate extends AbstractExceptionPredicateTest {
        private final Exception exception = new ConnectException("I'm having trouble connecting...");
        private final Predicate<Exception> predicate = KiwiRetryerPredicates.CONNECTION_ERROR;
    }

    @Nested
    @Getter
    class SOCKET_TIMEOUT_Predicate extends AbstractExceptionPredicateTest {
        private final Exception exception = new SocketTimeoutException("Did it actually finish? No idea...");
        private final Predicate<Exception> predicate = KiwiRetryerPredicates.SOCKET_TIMEOUT;
    }

    @Nested
    @Getter
    class SSL_HANDSHAKE_ERROR_Predicate extends AbstractExceptionPredicateTest {
        private final Exception exception = new SSLHandshakeException("Remote host closed connection during handshake");
        private final Predicate<Exception> predicate = KiwiRetryerPredicates.SSL_HANDSHAKE_ERROR;
    }

    @Nested
    @Getter
    class NO_ROUTE_TO_HOST_Predicate extends AbstractExceptionPredicateTest {
        private final Exception exception = new NoRouteToHostException("Where did it go?");
        private final Predicate<Exception> predicate = KiwiRetryerPredicates.NO_ROUTE_TO_HOST;
    }

    /**
     * Base class providing test generation for the Throwable predicates in KiwiRetryerPredicates.
     */
    @DisplayNameGeneration(PredicateDisplayNameGenerator.class)
    abstract static class AbstractExceptionPredicateTest {

        /**
         * The predicate to test.
         */
        abstract Predicate<Exception> getPredicate();

        /**
         * An exception instance of the same type that the above predicate tests.
         */
        abstract Exception getException();

        @Test
        void whenTheExpectedExceptionIsTheDirectException_shouldBeTrue() {
            assertThat(getPredicate().test(getException())).isTrue();
        }

        @Test
        void whenTheExpectedExceptionIsTheExceptionCause_shouldBeTrue() {
            assertThat(getPredicate().test(makeThrowableTheExceptionCause(getException()))).isTrue();
        }

        @Test
        void whenTheExpectedExceptionIsTheRootCause_shouldBeTrue() {
            assertThat(getPredicate().test(makeThrowableTheRootCause(getException()))).isTrue();
        }

        @ParameterizedTest
        @ValueSource(classes = {
                RuntimeException.class,
                IllegalStateException.class,
                IOException.class,
                FileNotFoundException.class
        })
        void whenIsAnExceptionOfType_shouldBeFalse(Class<?> clazz) throws Exception {
            var exception = (Exception) clazz.getDeclaredConstructor(String.class).newInstance("oopsy daisy");

            assertThat(getPredicate().test(exception)).isFalse();
        }

        private IOException makeThrowableTheExceptionCause(Throwable t) {
            return new IOException("well, something went wrong", t);
        }

        private UncheckedIOException makeThrowableTheRootCause(Throwable t) {
            return new UncheckedIOException(makeThrowableTheExceptionCause(t));
        }
    }

    /**
     * Name generator for test classes that extend {@link AbstractExceptionPredicateTest}.
     */
    private static class PredicateDisplayNameGenerator implements DisplayNameGenerator {

        @Override
        public String generateDisplayNameForClass(Class<?> testClass) {
            return testClass.getSimpleName();
        }

        @Override
        public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
            return f("The {} predicate returns", stripTrailingTestSuffix(nestedClass));
        }

        private String stripTrailingTestSuffix(Class<?> clazz) {
            return clazz.getSimpleName().replace("_Predicate", "");
        }

        // Generates display name like: "true when the expected exception is the root cause" from
        // method names that have the format: <camelCaseConditionDescription>_[shouldBeTrue|shouldBeFalse]
        @Override
        public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
            var methodName = testMethod.getName();
            var parts = methodName.split("_");

            verify(parts.length == 2, "Invalid test method name: %s", methodName);
            verify("shouldBeTrue".equals(parts[1]) || "shouldBeFalse".equals(parts[1]),
                    "Test method must end with shouldBeTrue or shouldBeFalse");

            var expectedTestResult = parts[1].equals("shouldBeTrue") ? "true" : "false";
            var conditionMessage = String.join(" ", StringUtils.splitByCharacterTypeCamelCase(parts[0]))
                    .toLowerCase(Locale.ENGLISH);

            return f("{} {}", expectedTestResult, conditionMessage);
        }
    }

    @Nested
    class IS_HTTP_400s {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.retry.KiwiRetryerPredicatesTest#httpNonErrorResponseProvider")
        @DisplayName("should return false when given a status code between 100-399")
        void IS_HTTP_400s_shouldReturnFalse_whenNonErrorResponses(Response response) {
            assertThat(KiwiRetryerPredicates.IS_HTTP_400s.test(response)).isFalse();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.retry.KiwiRetryerPredicatesTest#httpClientErrorResponseProvider")
        @DisplayName("should return true when given a status code between 400-499")
        void IS_HTTP_400s_shouldReturnTrue_whenClientErrorResponses(Response response) {
            assertThat(KiwiRetryerPredicates.IS_HTTP_400s.test(response)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.retry.KiwiRetryerPredicatesTest#httpServerErrorResponseProvider")
        @DisplayName("should return false when given a status code between 500-599")
        void IS_HTTP_400s_shouldReturnFalse_whenServerErrorResponses(Response response) {
            assertThat(KiwiRetryerPredicates.IS_HTTP_400s.test(response)).isFalse();
        }
    }

    @Nested
    class IS_HTTP_500s {

        @ParameterizedTest
        @MethodSource("org.kiwiproject.retry.KiwiRetryerPredicatesTest#httpNonErrorResponseProvider")
        @DisplayName("should return false when given a status code between 100-399")
        void shouldReturnFalse_whenNonErrorResponses(Response response) {
            assertThat(KiwiRetryerPredicates.IS_HTTP_500s.test(response)).isFalse();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.retry.KiwiRetryerPredicatesTest#httpClientErrorResponseProvider")
        @DisplayName("should return false when given a status code between 400-499")
        void shouldReturnFalse_whenClientErrorResponses(Response response) {
            assertThat(KiwiRetryerPredicates.IS_HTTP_500s.test(response)).isFalse();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.retry.KiwiRetryerPredicatesTest#httpServerErrorResponseProvider")
        @DisplayName("should return true when given a status code between 500-599")
        void shouldReturnTrue_whenServerErrorResponses(Response response) {
            assertThat(KiwiRetryerPredicates.IS_HTTP_500s.test(response)).isTrue();
        }
    }

    private static Stream<Arguments> httpNonErrorResponseProvider() {
        return intArgumentsStream(100, 399);
    }

    private static Stream<Arguments> httpClientErrorResponseProvider() {
        return intArgumentsStream(400, 499);
    }

    private static Stream<Arguments> httpServerErrorResponseProvider() {
        return intArgumentsStream(500, 599);
    }

    private static Stream<Arguments> intArgumentsStream(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive)
                .mapToObj(Response::status)
                .map(Response.ResponseBuilder::build)
                .map(Arguments::of);
    }
}
