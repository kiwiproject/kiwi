package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;
import org.slf4j.event.Level;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@DisplayName("SimpleRetries")
class SimpleRetriesTest {

    private static final int MAX_ATTEMPTS = 10;
    private static final int RETRY_DELAY = 1;
    private static final TimeUnit RETRY_UNIT = TimeUnit.SECONDS;

    private KiwiEnvironment environment;

    @BeforeEach
    void setUp() {
        environment = mock(KiwiEnvironment.class);
    }

    @Nested
    class TryGetObject {

        @Nested
        class ShouldHaveObject {

            @Test
            void whenSucceedsOnFirstAttempt() {
                var supplier = new NullReturningSupplier<>("orange").withTimesToReturnNull(0);
                Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, RETRY_DELAY, RETRY_UNIT, environment, String.class, supplier);

                assertThat(result).contains("orange");
                assertThat(supplier.getCount()).isOne();

                verifyNoInteractions(environment);
            }

            @Nested
            class WhenSucceedsOnIntermediateAttempt {

                @Test
                void whenNoTypeGiven() {
                    var numTimesToFail = 2;
                    var supplier = new NullReturningSupplier<>("blue").withTimesToReturnNull(numTimesToFail);

                    Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, 10, TimeUnit.MILLISECONDS, supplier);

                    assertThat(result).contains("blue");
                    assertThat(supplier.getCount()).isEqualTo(numTimesToFail + 1);
                }

                @Test
                void whenGivenObjectType() {
                    var numTimesToFail = 4;
                    var supplier = new NullReturningSupplier<>("orange").withTimesToReturnNull(numTimesToFail);

                    Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, 10, TimeUnit.MILLISECONDS, String.class, supplier);

                    assertThat(result).contains("orange");
                    assertThat(supplier.getCount()).isEqualTo(numTimesToFail + 1);
                }

                @Test
                void whenGivenExplicitRetryLogLevel() {
                    var numTimesToFail = 2;
                    var supplier = new ExceptionThrowingSupplier<>("red").withTimesToThrowException(numTimesToFail);

                    Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, 10, TimeUnit.MILLISECONDS, environment, "String", Level.WARN, supplier);

                    assertThat(result).contains("red");
                    assertThat(supplier.getCount()).isEqualTo(numTimesToFail + 1);
                }

                @Test
                void whenSupplierReturnsValueOnSecondAttempt() {
                    var numTimesToFail = 1;
                    var supplier = new NullReturningSupplier<>("orange").withTimesToReturnNull(numTimesToFail);

                    Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, RETRY_DELAY, RETRY_UNIT, environment, String.class, supplier);

                    assertThat(result).contains("orange");
                    assertThat(supplier.getCount()).isEqualTo(2);

                    verify(environment, times(numTimesToFail)).sleepQuietly(RETRY_DELAY, RETRY_UNIT);
                }

                @Test
                void whenSupplierReturnsValueOnLastAttempt() {
                    var supplier = new NullReturningSupplier<>("blue").withTimesToReturnNull(MAX_ATTEMPTS - 1);

                    Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, RETRY_DELAY, RETRY_UNIT, environment, supplier);

                    assertThat(result).contains("blue");
                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verify(environment, times(MAX_ATTEMPTS - 1)).sleepQuietly(RETRY_DELAY, RETRY_UNIT);
                }

                @Test
                void whenSupplierThrowsExceptions() {
                    var numTimesToFail = 5;
                    var supplier = new ExceptionThrowingSupplier<>("yellow").withTimesToThrowException(numTimesToFail);

                    Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, RETRY_DELAY, RETRY_UNIT, environment, supplier);

                    assertThat(result).contains("yellow");
                    assertThat(supplier.getCount()).isEqualTo(numTimesToFail + 1);

                    verify(environment, times(numTimesToFail)).sleepQuietly(RETRY_DELAY, RETRY_UNIT);
                }
            }
        }

        @Nested
        class ShouldNotHaveObject {

            @Test
            void whenSupplierAlwaysReturnsNull() {
                var supplier = new NullReturningSupplier<>("purple").withTimesToReturnNull(MAX_ATTEMPTS);

                Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, RETRY_DELAY, RETRY_UNIT, environment, supplier);

                assertThat(result).isEmpty();
                assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                verify(environment, times(MAX_ATTEMPTS - 1)).sleepQuietly(RETRY_DELAY, RETRY_UNIT);
            }

            @Test
            void whenSupplierAlwaysThrowsExceptions() {
                var supplier = new ExceptionThrowingSupplier<>("green").withTimesToThrowException(MAX_ATTEMPTS);

                Optional<String> result = SimpleRetries.tryGetObject(MAX_ATTEMPTS, RETRY_DELAY, RETRY_UNIT, environment, supplier);

                assertThat(result).isEmpty();
                assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                verify(environment, times(MAX_ATTEMPTS - 1)).sleepQuietly(RETRY_DELAY, RETRY_UNIT);
            }
        }
    }

    @Nested
    class TryGetObjectCollectingErrors {

        @Nested
        class ShouldHaveResultAndNoErrors {

            @Test
            void whenSucceedsOnFirstAttempt() {
                Supplier<Integer> supplier = () -> 42;

                var retryResult = SimpleRetries.tryGetObjectCollectingErrors(5, 1, TimeUnit.SECONDS, Integer.class, supplier);

                assertThat(retryResult.succeeded()).isTrue();
                assertThat(retryResult.getObjectIfPresent()).hasValue(42);
                assertThat(retryResult.getErrors()).isEmpty();
            }

            @Test
            void whenSucceedsOnLastAttempt_ButNoExceptionsWereThrown() {
                int maxAttempts = 5;
                var supplier = new NullReturningSupplier<>(42).withTimesToReturnNull(maxAttempts - 1);

                var retryResult = SimpleRetries.tryGetObjectCollectingErrors(maxAttempts, 10, TimeUnit.MILLISECONDS, supplier);

                assertThat(supplier.getCount()).isEqualTo(maxAttempts);
                assertThat(retryResult.succeeded()).isTrue();
                assertThat(retryResult.getObjectIfPresent()).hasValue(42);
                assertThat(retryResult.getErrors()).isEmpty();
            }
        }

        @Nested
        class ShouldHaveResultAndErrors {

            @Test
            void whenSucceedsOnIntermediateAttempt_AndExceptionsWereThrown() {
                int attemptsToFail = 2;
                int maxAttempts = 5;
                var supplier = new ExceptionThrowingSupplier<>(42).withTimesToThrowException(attemptsToFail);

                var retryResult = SimpleRetries.tryGetObjectCollectingErrors(maxAttempts, 25, TimeUnit.MILLISECONDS, Integer.class, supplier);

                assertThat(supplier.getCount()).isEqualTo(attemptsToFail + 1);
                assertThat(retryResult.succeeded()).isTrue();
                assertThat(retryResult.getObjectIfPresent()).hasValue(42);
                assertThat(retryResult.getErrors()).hasSize(attemptsToFail);
            }

            @Test
            void whenSucceedsOnLastAttempt_AndExceptionsWereThrown() {
                int maxAttempts = 5;
                var supplier = new ExceptionThrowingSupplier<>(42).withTimesToThrowException(maxAttempts - 1);

                var retryResult = SimpleRetries.tryGetObjectCollectingErrors(maxAttempts, 10, TimeUnit.MILLISECONDS, supplier);

                assertThat(supplier.getCount()).isEqualTo(maxAttempts);
                assertThat(retryResult.succeeded()).isTrue();
                assertThat(retryResult.getObjectIfPresent()).hasValue(42);
                assertThat(retryResult.getErrors()).hasSize(maxAttempts - 1);
            }
        }

        @Nested
        class ShouldHaveNoResult {

            @Test
            void whenSupplierAlwaysThrowsException() {
                int maxAttempts = 3;
                var supplier = new ExceptionThrowingSupplier<>("gray").withTimesToThrowException(maxAttempts);

                var retryResult = SimpleRetries.tryGetObjectCollectingErrors(maxAttempts, 10, TimeUnit.MILLISECONDS, supplier);

                assertThat(retryResult.failed()).isTrue();
                assertThat(retryResult.getObjectIfPresent()).isEmpty();
                assertThat(retryResult.getErrors()).hasSize(maxAttempts);
            }

            @Test
            void whenSupplierAlwaysReturnsNull_ButNoExceptionsThrown() {
                int maxAttempts = 5;
                var supplier = new NullReturningSupplier<>("black").withTimesToReturnNull(maxAttempts);

                var retryResult = SimpleRetries.tryGetObjectCollectingErrors(maxAttempts, 10, TimeUnit.MILLISECONDS, supplier);

                assertThat(retryResult.failed()).isTrue();
                assertThat(retryResult.getObjectIfPresent()).isEmpty();
                assertThat(retryResult.getErrors()).isEmpty();
            }
        }
    }
}