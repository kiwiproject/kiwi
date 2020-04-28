package org.kiwiproject.dropwizard.metrics.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DisplayName("TimedHealthCheck")
class TimedHealthCheckTest {

    @Nested
    class CompletesInTime {

        @Test
        void shouldReturnHealth_IfDelegateIsHealthy() {
            var delegate = mock(HealthCheck.class);
            when(delegate.execute()).thenReturn(HealthCheck.Result.healthy("All good!"));

            var healthCheck = new TimedHealthCheck(delegate);
            var result = healthCheck.execute();

            assertThat(result.isHealthy()).isTrue();
            assertThat(result.getMessage()).isEqualTo("All good!");
        }

        @Test
        void shouldReturnUnhealthy_IfDelegateIsUnhealthy() {
            var delegate = mock(HealthCheck.class);
            when(delegate.execute()).thenReturn(HealthCheck.Result.unhealthy("Houston we have a problem"));

            var healthCheck = new TimedHealthCheck(delegate);
            var result = healthCheck.execute();

            assertThat(result.isHealthy()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Houston we have a problem");
        }

    }

    @Nested
    class WhenDelegateTimesOut {

        @SuppressWarnings("java:S2925")
        @Test
        void shouldReturnUnhealthy() {
            var delegate = mock(HealthCheck.class);
            when(delegate.execute()).thenAnswer((Answer<HealthCheck.Result>) invocation -> {
                Thread.sleep(500);
                return HealthCheck.Result.healthy();
            });

            var healthCheck = new TimedHealthCheck(delegate, Duration.milliseconds(100));
            var result = healthCheck.execute();

            assertThat(result.isHealthy()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Unable to obtain result in 0 seconds");
        }

    }

    @Nested
    class WhenDelegateThrowsExecutionException {

        @Test
        void shouldReturnUnhealthy() {
            var delegate = mock(HealthCheck.class);
            doThrow(new RuntimeException("I failed in a bad way")).when(delegate).execute();

            var healthCheck = new TimedHealthCheck(delegate);

            var result = healthCheck.execute();

            assertThat(result.isHealthy()).isFalse();
            assertThat(result.getMessage()).contains("I failed in a bad way");
            assertThat(result.getError())
                    .isExactlyInstanceOf(ExecutionException.class)
                    .hasCauseExactlyInstanceOf(RuntimeException.class)
                    .hasMessageContaining("I failed in a bad way");
        }

    }

    @Nested
    class WhenDelegateIsInterrupted {
        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnUnhealthy() throws ExecutionException, InterruptedException, TimeoutException {
            var delegate = mock(HealthCheck.class);
            var healthCheck = new TimedHealthCheck(delegate);

            var spy = spy(healthCheck);
            var future = mock(CompletableFuture.class);
            doThrow(new InterruptedException()).when(future).get(anyLong(), any(TimeUnit.class));

            when(spy.getFuture()).thenReturn(future);

            var result = spy.execute();

            assertThat(result.isHealthy()).isFalse();
            assertThat(result.getMessage()).contains("Unable to obtain result due to process being interrupted");
            assertThat(result.getError()).isExactlyInstanceOf(InterruptedException.class);
        }
    }
}
