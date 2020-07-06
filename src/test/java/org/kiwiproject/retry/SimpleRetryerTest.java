package org.kiwiproject.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.slf4j.event.Level;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@DisplayName("SimpleRetryer")
@ExtendWith(SoftAssertionsExtension.class)
class SimpleRetryerTest {

    private static final int MAX_ATTEMPTS = 10;

    @Nested
    class Builder {

        @Test
        void shouldHaveDefaultValues(SoftAssertions softly) {
            var retryer = SimpleRetryer.builder().build();
            softly.assertThat(retryer.environment).isNotNull();
            assertDefaults(softly, retryer);
        }

        @Test
        void shouldAcceptEnvironment(SoftAssertions softly) {
            var env = new DefaultEnvironment();
            var retryer = SimpleRetryer.builder().environment(env).build();
            softly.assertThat(retryer.environment).isSameAs(env);
            assertDefaults(softly, retryer);
        }

        private void assertDefaults(SoftAssertions softly, SimpleRetryer retryer) {
            softly.assertThat(retryer.maxAttempts).isEqualTo(3);
            softly.assertThat(retryer.retryDelayTime).isEqualTo(50);
            softly.assertThat(retryer.retryDelayUnit).isEqualTo(TimeUnit.MILLISECONDS);
            softly.assertThat(retryer.commonType).isEqualTo("object");
            softly.assertThat(retryer.logLevelForSubsequentAttempts).isEqualTo(Level.TRACE);
        }

        @Test
        void shouldAcceptAllValues(SoftAssertions softly) {
            var env = new DefaultEnvironment();
            var retryer = SimpleRetryer.builder()
                    .environment(env)
                    .maxAttempts(5)
                    .retryDelayTime(1_000)
                    .retryDelayUnit(TimeUnit.MICROSECONDS)
                    .commonType("something")
                    .logLevelForSubsequentAttempts(Level.WARN)
                    .build();

            softly.assertThat(retryer.environment).isSameAs(env);
            softly.assertThat(retryer.maxAttempts).isEqualTo(5);
            softly.assertThat(retryer.retryDelayTime).isEqualTo(1_000);
            softly.assertThat(retryer.retryDelayUnit).isEqualTo(TimeUnit.MICROSECONDS);
            softly.assertThat(retryer.commonType).isEqualTo("something");
            softly.assertThat(retryer.logLevelForSubsequentAttempts).isEqualTo(Level.WARN);
        }
    }

    @Nested
    class TryGetObjectMethods {

        private KiwiEnvironment environment;
        private SimpleRetryer retryer;
        private InvocationCountingSupplier<String> supplier;

        @BeforeEach
        void setUp() {
            environment = mock(KiwiEnvironment.class);
            retryer = SimpleRetryer.builder()
                    .environment(environment)
                    .maxAttempts(MAX_ATTEMPTS)
                    .build();
        }

        @Nested
        class TryGetObject {

            @Nested
            class ShouldHaveObject {

                @Test
                void whenSucceedsOnFirstAttempt() {
                    supplier = new NullReturningSupplier<>("blue").withTimesToReturnNull(0);

                    Optional<String> result = retryer.tryGetObject(supplier);

                    assertThat(result).hasValue("blue");
                    assertThat(supplier.getCount()).isOne();

                    verifyNoInteractions(environment);
                }

                @Test
                void whenSucceedsOnIntermediateAttempt() {
                    var numTimesToFail = 3;
                    supplier = new NullReturningSupplier<>("purple").withTimesToReturnNull(numTimesToFail);

                    Optional<String> result = retryer.tryGetObject(String.class, supplier);

                    assertThat(result).hasValue("purple");
                    assertThat(supplier.getCount()).isEqualTo(numTimesToFail + 1);

                    verifyNumberOfSleeps(environment, numTimesToFail);
                }

                @Test
                void whenSucceedsOnLastAttempt() {
                    supplier = new ExceptionThrowingSupplier<>("red")
                            .withTimesToThrowException(MAX_ATTEMPTS - 1);

                    Optional<String> result = retryer.tryGetObject(supplier);

                    assertThat(result).hasValue("red");
                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verifyNumberOfSleeps(environment, MAX_ATTEMPTS - 1);
                }
            }

            @Nested
            class ShouldNotHaveObject {

                @Test
                void whenAllAttemptsReturnNull() {
                    supplier = new NullReturningSupplier<>("orange").withTimesToReturnNull(MAX_ATTEMPTS);

                    Optional<String> result = retryer.tryGetObject(supplier);

                    assertThat(result).isEmpty();
                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verifyNumberOfSleeps(environment, MAX_ATTEMPTS - 1);
                }

                @Test
                void whenAllAttemptsThrowException() {
                    supplier = new ExceptionThrowingSupplier<>("red")
                            .withTimesToThrowException(MAX_ATTEMPTS);

                    Optional<String> result = retryer.tryGetObject(supplier);

                    assertThat(result).isEmpty();
                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verifyNumberOfSleeps(environment, MAX_ATTEMPTS - 1);
                }
            }
        }

        @Nested
        class TryGetObjectCollectingErrors {

            @Nested
            class ShouldHaveObject {

                @Test
                void whenSucceedsOnFirstAttempt() {
                    supplier = new ExceptionThrowingSupplier<>("yellow").withTimesToThrowException(0);

                    var retryResult = retryer.tryGetObjectCollectingErrors(supplier);

                    assertThat(retryResult.getObjectIfPresent()).hasValue("yellow");
                    assertThat(retryResult.getErrors()).isEmpty();

                    assertThat(supplier.getCount()).isEqualTo(1);

                    verifyNoInteractions(environment);
                }

                @Test
                void whenSucceedsOnIntermediateAttempt() {
                    var numTimesToFail = 2;
                    supplier = new NullReturningSupplier<>("yellow").withTimesToReturnNull(numTimesToFail);

                    var retryResult = retryer.tryGetObjectCollectingErrors(supplier);

                    assertThat(retryResult.getObjectIfPresent()).hasValue("yellow");
                    assertThat(retryResult.getErrors()).isEmpty();

                    assertThat(supplier.getCount()).isEqualTo(numTimesToFail + 1);

                    verifyNumberOfSleeps(environment, numTimesToFail);
                }

                @Test
                void whenSucceedsOnLastAttempt() {
                    supplier = new NullReturningSupplier<>("yellow").withTimesToReturnNull(MAX_ATTEMPTS - 1);

                    var retryResult = retryer.tryGetObjectCollectingErrors(String.class, supplier);

                    assertThat(retryResult.getObjectIfPresent()).hasValue("yellow");
                    assertThat(retryResult.getErrors()).isEmpty();

                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verifyNumberOfSleeps(environment, MAX_ATTEMPTS - 1);
                }
            }

            @Nested
            class ShouldNotHaveObject {

                @Test
                void whenAllAttemptsReturnNull() {
                    supplier = new NullReturningSupplier<>("orange").withTimesToReturnNull(MAX_ATTEMPTS);

                    RetryResult<String> retryResult = retryer.tryGetObjectCollectingErrors(supplier);

                    assertThat(retryResult.getObjectIfPresent()).isEmpty();
                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verifyNumberOfSleeps(environment, MAX_ATTEMPTS - 1);
                }

                @Test
                void whenAllAttemptsThrowException() {
                    supplier = new ExceptionThrowingSupplier<>("red").withTimesToThrowException(MAX_ATTEMPTS);

                    RetryResult<String> retryResult = retryer.tryGetObjectCollectingErrors(supplier);

                    assertThat(retryResult.getObjectIfPresent()).isEmpty();
                    assertThat(supplier.getCount()).isEqualTo(MAX_ATTEMPTS);

                    verifyNumberOfSleeps(environment, MAX_ATTEMPTS - 1);
                }
            }
        }

        private void verifyNumberOfSleeps(KiwiEnvironment environment, int numSleepsWanted) {
            verify(environment, times(numSleepsWanted))
                    .sleepQuietly(SimpleRetryer.DEFAULT_RETRY_DELAY_TIME, SimpleRetryer.DEFAULT_RETRY_DELAY_UNIT);
        }
    }
}