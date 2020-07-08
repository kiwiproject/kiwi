package org.kiwiproject.retry;

import static com.google.common.base.Verify.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@DisplayName("KiwiRetryer")
class KiwiRetryerTest {

    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    @Nested
    class ShouldReturnResult {

        @Test
        void whenResultReturnedOnFirstAttempt() {
            Callable<Response> callable = () -> Response.ok().build();

            var retryer = KiwiRetryer.<Response>newRetryerWithDefaults();

            var response = retryer.call(callable);

            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Test
        void whenResultReturnedBeforeMaxAttemptsReached() {
            final var successfulAttemptNumber = 4;

            var callable = new Callable<Response>() {
                private int attempt = 0;

                @Override
                public Response call() throws ConnectException {
                    ++attempt;
                    if (attempt < successfulAttemptNumber) {
                        throw new ConnectException("nobody home");
                    }

                    return Response.ok().build();
                }
            };

            var retryer = KiwiRetryer.<Response>builder()
                    .exceptionPredicate(KiwiRetryerPredicates.CONNECTION_ERROR)
                    .waitStrategy(WaitStrategies.fixedWait(10L, TimeUnit.MILLISECONDS))
                    .stopStrategy(StopStrategies.stopAfterAttempt(successfulAttemptNumber))
                    .build();

            var response = retryer.call(callable);

            assertThat(response.getStatus()).isEqualTo(200);
        }

        @Nested
        class WhenReturnsResultBeforeMaxAttempts {

            @Test
            void whenRetryingAllExceptions() {
                final var successfulAttemptNumber = 2;

                var callable = new Callable<Response>() {
                    private int attempt = 0;

                    @Override
                    public Response call() throws ConnectException {
                        ++attempt;
                        if (attempt < successfulAttemptNumber) {
                            throw new ConnectException("nobody home");
                        }

                        return Response.ok().entity(attempt).build();
                    }
                };

                var retryer = KiwiRetryer.<Response>newRetryerRetryingAllExceptions("retryAllExceptions");

                var response = retryer.call(callable);

                assertThat(response.getStatus()).isEqualTo(200);
                assertThat(response.getEntity()).isEqualTo(successfulAttemptNumber);
            }

            @Test
            void retryAllExceptionsShouldTakePrecedence() {
                var callable = exceptionThrowingResponseCallable();

                var retryer = KiwiRetryer.<Response>builder()
                        .retryerId("retryOnAllExceptionsShouldTakePrecedence")
                        .stopStrategy(StopStrategies.neverStop())
                        .waitStrategy(WaitStrategies.fixedWait(10, TimeUnit.MILLISECONDS))
                        .retryOnAllExceptions(true)
                        .retryOnAllRuntimeExceptions(true)
                        .exceptionPredicate(KiwiRetryerPredicates.CONNECTION_ERROR)
                        .exceptionPredicate(KiwiRetryerPredicates.NO_ROUTE_TO_HOST)
                        .exceptionPredicate(KiwiRetryerPredicates.SOCKET_TIMEOUT)
                        .exceptionPredicate(KiwiRetryerPredicates.UNKNOWN_HOST)
                        .build();

                var response = retryer.call(callable);

                assertThat(response.getEntity()).isEqualTo(ExceptionThrowingResponseCallable.SUCCESS_ATTEMPT_NUMBER);
            }

            @Test
            void whenUsing4xxPredicate() {
                final var successfulAttemptNumber = 2;

                var callable = new Callable<Response>() {
                    private int attempt = 0;

                    @Override
                    public Response call() {
                        ++attempt;
                        if (attempt < successfulAttemptNumber) {
                            return Response.status(418).entity(attempt).build();
                        }
                        return Response.ok().entity(attempt).build();
                    }
                };

                var retryer = KiwiRetryer.<Response>builder()
                        .resultPredicate(KiwiRetryerPredicates.IS_HTTP_400s)
                        .build();

                var response = retryer.call(callable);

                assertThat(response.getEntity()).isEqualTo(successfulAttemptNumber);
            }

            @Test
            void whenUsing5xxPredicate() {
                var successfulAttemptNumber = 3;

                var callable = new Callable<Response>() {
                    private int attempt = 0;

                    @Override
                    public Response call() {
                        ++attempt;
                        if (attempt < successfulAttemptNumber) {
                            return Response.status(503).entity(attempt).build();
                        }
                        return Response.ok().entity(attempt).build();
                    }
                };

                var retryer = KiwiRetryer.<Response>builder()
                        .resultPredicate(KiwiRetryerPredicates.IS_HTTP_500s)
                        .build();

                var response = retryer.call(callable);

                assertThat(response.getEntity()).isEqualTo(successfulAttemptNumber);
            }

        }

    }

    @Nested
    class ShouldThrowKiwiRetryerException {

        @Test
        void whenDoesNotSucceedBeforeMaxAttempts() {
            Callable<Response> callable = () -> {
                throw new UnknownHostException("don't know what or who this is!");
            };

            var maxAttempts = 3;
            var retryer = KiwiRetryer.<Response>builder()
                    .retryerId("retriesExceedsMaxAttempts")
                    .maxAttempts(maxAttempts)
                    .exceptionPredicate(KiwiRetryerPredicates.UNKNOWN_HOST)
                    .build();

            var thrown = catchThrowable(() -> retryer.call(callable));

            assertThat(thrown)
                    .isExactlyInstanceOf(KiwiRetryerException.class)
                    .hasMessageStartingWith("KiwiRetryer retriesExceedsMaxAttempts failed all %d attempts. Error:", maxAttempts);

            var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
            assertThat(unwrappedException)
                    .isExactlyInstanceOf(RetryException.class)
                    .hasMessage("Retrying failed to complete successfully after %d attempts.", maxAttempts);
        }

        @Test
        void whenUsingDefaultExceptions_AndThrowsOtherException() {
            Callable<Response> callable = () -> {
                throw new RuntimeException("this failed");
            };

            var retryer = KiwiRetryer.<Response>newRetryerWithDefaultExceptions("thisFails");

            var thrown = catchThrowable(() -> retryer.call(callable));

            assertThat(thrown)
                    .isExactlyInstanceOf(KiwiRetryerException.class)
                    .hasMessageStartingWith("KiwiRetryer thisFails failed making call. Wrapped exception:");

            var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
            assertThat(unwrappedException)
                    .isExactlyInstanceOf(RuntimeException.class)
                    .hasMessage("this failed");
        }

        @Nested
        class WhenMaxAttemptsReached {

            @Test
            void whenDefaultExceptionsThrown() {
                Callable<Response> callable = () -> {
                    int seed = ThreadLocalRandom.current().nextInt(4);

                    switch (seed) {
                        case 0:
                            throw new ConnectException("connect error");

                        case 1:
                            throw new UnknownHostException("unknown host");

                        case 2:
                            throw new SocketTimeoutException("socket timeout");

                        case 3:
                            throw new NoRouteToHostException("no route to host");

                        default:
                            throw new Exception("should NEVER get here!");
                    }
                };

                var retryer = KiwiRetryer.<Response>newRetryerWithDefaultExceptions("retryerWithDefaultExceptions");

                var thrown = catchThrowable(() -> retryer.call(callable));

                assertThat(thrown)
                        .isExactlyInstanceOf(KiwiRetryerException.class)
                        .hasMessageStartingWith("KiwiRetryer retryerWithDefaultExceptions failed all %d attempts. Error:",
                                DEFAULT_MAX_ATTEMPTS);

                var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
                assertThat(unwrappedException)
                        .isExactlyInstanceOf(RetryException.class)
                        .hasMessage("Retrying failed to complete successfully after %d attempts.",
                                DEFAULT_MAX_ATTEMPTS);
            }

            @Test
            void whenRetryingAllExceptions() {
                var callable = exceptionThrowingResponseCallable();

                var retryer = KiwiRetryer.<Response>newRetryerRetryingAllExceptions("retryAllExceptions");

                var thrown = catchThrowable(() -> retryer.call(callable));

                assertThat(thrown)
                        .isExactlyInstanceOf(KiwiRetryerException.class)
                        .hasMessageStartingWith("KiwiRetryer retryAllExceptions failed all %d attempts. Error:",
                                DEFAULT_MAX_ATTEMPTS);

                var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
                assertThat(unwrappedException)
                        .isExactlyInstanceOf(RetryException.class)
                        .hasMessage("Retrying failed to complete successfully after %d attempts.",
                                DEFAULT_MAX_ATTEMPTS);

                var fullyUnwrappedException = ((KiwiRetryerException) thrown).unwrapFully().orElseThrow();
                assertThat(fullyUnwrappedException)
                        .isNotInstanceOf(RetryException.class)
                        .isInstanceOf(RuntimeException.class);
            }

            @Test
            void whenRetryingAllRuntimeExceptions() {
                Callable<Response> callable = () -> {
                    throw new RuntimeException("Catch me if you can!");
                };

                var retryer = KiwiRetryer.<Response>newRetryerRetryingAllRuntimeExceptions("retryAllRuntimeExceptions");

                var thrown = catchThrowable(() -> retryer.call(callable));

                assertThat(thrown)
                        .isExactlyInstanceOf(KiwiRetryerException.class)
                        .hasMessageStartingWith("KiwiRetryer retryAllRuntimeExceptions failed all %d attempts. Error:",
                                DEFAULT_MAX_ATTEMPTS);

                var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
                assertThat(unwrappedException)
                        .isExactlyInstanceOf(RetryException.class)
                        .hasMessage("Retrying failed to complete successfully after %d attempts.",
                                DEFAULT_MAX_ATTEMPTS);

                var fullyUnwrappedException = ((KiwiRetryerException) thrown).unwrapFully().orElseThrow();
                assertThat(fullyUnwrappedException)
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("Catch me if you can!");
            }

            @Test
            void whenRetryRuntimeExceptions_ShouldSupersedeNamedPredicates() {
                var callable = exceptionThrowingResponseCallable();

                var retryer = KiwiRetryer.<Response>builder()
                        .retryerId("retryOnAllRuntimeExceptionsShouldTakePrecedence")
                        .stopStrategy(StopStrategies.neverStop())
                        .waitStrategy(WaitStrategies.fixedWait(10, TimeUnit.MILLISECONDS))
                        .retryOnAllRuntimeExceptions(true)
                        .exceptionPredicate(KiwiRetryerPredicates.CONNECTION_ERROR)
                        .exceptionPredicate(KiwiRetryerPredicates.NO_ROUTE_TO_HOST)
                        .exceptionPredicate(KiwiRetryerPredicates.SOCKET_TIMEOUT)
                        .exceptionPredicate(KiwiRetryerPredicates.UNKNOWN_HOST)
                        .build();

                var thrown = catchThrowable(() -> retryer.call(callable));

                assertThat(thrown)
                        .isExactlyInstanceOf(KiwiRetryerException.class)
                        .hasMessageStartingWith(
                                "KiwiRetryer retryOnAllRuntimeExceptionsShouldTakePrecedence failed making call. Wrapped exception:");

                var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
                assertThat(unwrappedException)
                        .isExactlyInstanceOf(ConnectException.class)
                        .hasMessage("connect error");
            }
        }

    }

    private Callable<Response> exceptionThrowingResponseCallable() {
        return new ExceptionThrowingResponseCallable();
    }

    private static class ExceptionThrowingResponseCallable implements Callable<Response> {

        static final int SUCCESS_ATTEMPT_NUMBER = 7;

        private int attempt = 0;

        @Override
        public Response call() throws Exception {
            ++attempt;

            switch (attempt) {
                case 1:
                    throw new ConnectException("connect error");

                case 2:
                    throw new UnknownHostException("unknown host");

                case 3:
                    throw new SocketTimeoutException("socket timeout");

                case 4:
                    throw new NoRouteToHostException("no route to host");

                case 5:
                    throw new RuntimeException("plain runtime exception");

                case 6:
                    throw new Exception("plain exception");

                default:
                    verify(attempt == SUCCESS_ATTEMPT_NUMBER,
                            "success expected at attempt %", SUCCESS_ATTEMPT_NUMBER);
                    return Response.ok().entity(attempt).build();
            }
        }
    }
}
