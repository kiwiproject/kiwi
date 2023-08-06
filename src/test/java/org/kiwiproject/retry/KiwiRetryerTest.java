package org.kiwiproject.retry;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
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

        @Test
        void whenCallWithNullRetryerId() {
            Callable<Long> callable = () -> 42L;

            var retryer = KiwiRetryer.<Long>builder().build();

            var result = retryer.call(null, callable);

            assertThat(result).isEqualTo(42L);
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

            var kiwiRetryerException = catchThrowableOfType(() -> retryer.call(callable), KiwiRetryerException.class);

            assertThat(kiwiRetryerException)
                    .hasMessage("KiwiRetryer thisFails failed all 1 attempts. Error: Retrying failed to complete successfully after 1 attempts.");

            var unwrappedException = kiwiRetryerException.unwrapFully().orElseThrow();
            assertThat(unwrappedException)
                    .isExactlyInstanceOf(RuntimeException.class)
                    .hasMessage("this failed");
        }

        @Nested
        class WhenMaxAttemptsReached {

            @Test
            void whenAlternatesExceptionsAndResultsThatMatchResultPredicate() {
                var serviceUnavailableStatusCode = 503;

                Callable<Response> exceptionOr503Callable = () -> {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        throw new UnknownHostException("don't know what or who this is!");
                    }
                    return Response.status(serviceUnavailableStatusCode).build();
                };

                var maxAttempts = 10;
                var retryer = KiwiRetryer.<Response>builder()
                        .retryerId("exceptionOr503")
                        .maxAttempts(maxAttempts)
                        .initialSleepTimeAmount(10)  // millis
                        .retryIncrementTimeAmount(10)  // millis
                        .retryOnAllExceptions(true)
                        .resultPredicate(response ->
                                nonNull(response) && response.getStatus() == serviceUnavailableStatusCode)
                        .processingLogLevel(Level.TRACE)
                        .exceptionLogLevel(Level.DEBUG)
                        .build();

                var thrown = catchThrowable(() -> retryer.call(exceptionOr503Callable));

                assertThat(thrown)
                        .isExactlyInstanceOf(KiwiRetryerException.class)
                        .hasMessageStartingWith("KiwiRetryer exceptionOr503 failed all %d attempts. Error:",
                                maxAttempts);

                var unwrappedException = ((KiwiRetryerException) thrown).unwrap().orElseThrow();
                assertThat(unwrappedException)
                        .isExactlyInstanceOf(RetryException.class)
                        .hasMessage("Retrying failed to complete successfully after %d attempts.", maxAttempts);
            }

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

                var kiwiRetryerException = catchThrowableOfType(() -> retryer.call(callable), KiwiRetryerException.class);

                assertThat(kiwiRetryerException)
                        .hasMessage("KiwiRetryer retryOnAllRuntimeExceptionsShouldTakePrecedence failed all 1 attempts." +
                                " Error: Retrying failed to complete successfully after 1 attempts.");

                var unwrappedException = kiwiRetryerException.unwrapFully().orElseThrow();
                assertThat(unwrappedException)
                        .isExactlyInstanceOf(ConnectException.class)
                        .hasMessage("connect error");
            }
        }
    }

    @Nested
    class WhenCallingConcurrently {

        @Test
        void shouldHandleAllSuccessful() {
            final var unacceptableValue = 21;

            var callable1 = new Callable<Integer>() {
                private int attempt = 0;

                @Override
                public Integer call() {
                    ++attempt;
                    if (attempt == 1) {
                        return unacceptableValue;
                    }
                    return 42;
                }
            };

            var callable2 = new Callable<Integer>() {
                private int attempt = 0;

                @Override
                public Integer call() {
                    ++attempt;
                    if (attempt < 3) {
                        throw new RuntimeException("oops on attempt " + attempt);
                    }
                    return 84;
                }
            };

            Callable<Integer> callable3 = () -> 168;

            var retryer = KiwiRetryer.<Integer>builder()
                    .retryerId("test")
                    .retryOnAllExceptions(true)
                    .resultPredicate(value -> nonNull(value) && value == unacceptableValue)
                    .waitStrategy(WaitStrategies.fixedWait(10, TimeUnit.MILLISECONDS))
                    .build();

            var executor = Executors.newFixedThreadPool(3);

            var future1 = CompletableFuture.supplyAsync(() ->
                    retryer.call("test1", callable1), executor);
            var future2 = CompletableFuture.supplyAsync(() ->
                    retryer.call("test2", callable2), executor);
            var future3 = CompletableFuture.supplyAsync(() ->
                    retryer.call("test3", callable3), executor);

            assertThatCode(() -> CompletableFuture.allOf(future1, future2, future3).join())
                    .doesNotThrowAnyException();

            assertThat(future1).isCompletedWithValueMatching(value -> value == 42);
            assertThat(future2).isCompletedWithValueMatching(value -> value == 84);
            assertThat(future3).isCompletedWithValueMatching(value -> value == 168);
        }

        @Test
        void shouldHandleMixedSuccessAndFailure() {
            Callable<Response> successfulResponseCallable = () -> Response.ok().build();
            Callable<Response> errorResponseCallable = () -> Response.serverError().entity(2).build();
            Callable<Response> throwingCallable = () -> {
                throw new SocketTimeoutException("timeout!");
            };

            var retryer = KiwiRetryer.<Response>builder()
                    .retryerId("test")
                    .maxAttempts(7)
                    .initialSleepTimeAmount(5)  // millis
                    .retryIncrementTimeAmount(5)  // millis
                    .retryOnAllExceptions(true)
                    .resultPredicate(KiwiRetryerPredicates.IS_HTTP_500s)
                    .processingLogLevel(Level.TRACE)
                    .exceptionLogLevel(Level.DEBUG)
                    .build();

            var executor = Executors.newFixedThreadPool(3);

            var future1 = CompletableFuture.supplyAsync(() ->
                    retryer.call("test1", successfulResponseCallable), executor);
            var future2 = CompletableFuture.supplyAsync(() ->
                    retryer.call("test2", errorResponseCallable), executor);
            var future3 = CompletableFuture.supplyAsync(() ->
                    retryer.call("test3", throwingCallable), executor);
            var allFutures = CompletableFuture.allOf(future1, future2, future3);

            assertThatThrownBy(allFutures::join).isExactlyInstanceOf(CompletionException.class);

            // First one should have succeeded
            assertThat(future1).isCompletedWithValueMatching(response -> response.getStatus() == 200);

            // Second one should have failed because of bad response status 500
            var thrownByFuture2 = catchThrowable(future2::join);
            assertThat(thrownByFuture2)
                    .isExactlyInstanceOf(CompletionException.class)
                    .hasCauseExactlyInstanceOf(KiwiRetryerException.class);
            var kiwiRetryerException2 = (KiwiRetryerException) thrownByFuture2.getCause();
            var unwrappedException2 = kiwiRetryerException2.unwrap().orElseThrow();
            assertThat(unwrappedException2).isExactlyInstanceOf(RetryException.class);
            var retryException2 = (RetryException) unwrappedException2;
            var result2 = (Response) retryException2.getLastFailedAttempt().getResult();
            assertThat(result2.getStatus()).isEqualTo(500);
            assertThat(result2.getEntity()).isEqualTo(2);

            // Third one should have failed because of SocketTimeoutException
            var thrownByFuture3 = catchThrowable(future3::join);
            assertThat(thrownByFuture3)
                    .isExactlyInstanceOf(CompletionException.class)
                    .hasCauseExactlyInstanceOf(KiwiRetryerException.class);
            var kiwiRetryerException3 = (KiwiRetryerException) thrownByFuture3.getCause();
            var unwrappedException3 = kiwiRetryerException3.unwrap().orElseThrow();
            assertThat(unwrappedException3).isExactlyInstanceOf(RetryException.class);
            var retryException3 = (RetryException) unwrappedException3;
            var exception3 = retryException3.getLastFailedAttempt().getException();
            assertThat(exception3)
                    .isExactlyInstanceOf(SocketTimeoutException.class)
                    .hasMessage("timeout!");
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
