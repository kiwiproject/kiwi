package org.kiwiproject.dropwizard.metrics.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.util.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Health Check wrapper that will timeout after a set period of time.
 * <p>
 * This is mainly to protect the health checks from blocking forever.
 */
public class TimedHealthCheck extends HealthCheck {

    public static final ExecutorService HEALTH_CHECK_EXECUTOR = Executors.newFixedThreadPool(3);

    /**
     * The actual healthcheck to run
     */
    private final HealthCheck delegate;

    /**
     * The duration to wait for the healthcheck to return. Defaults to 5 seconds.
     */
    private final Duration timeout;

    /**
     * Creates a new TimedHealthCheck with the given {@code delegate} {@link HealthCheck} and the
     * default timeout (5 seconds).
     *
     * @param delegate the target health check
     */
    public TimedHealthCheck(HealthCheck delegate) {
        this(delegate, Duration.seconds(5));
    }

    /**
     * Creates a new TimedHealthCheck with the given ({@code delegate} {@link HealthCheck} and the given {@code timeout}.
     *
     * @param delegate the target health check
     * @param timeout  the timeout to use
     */
    public TimedHealthCheck(HealthCheck delegate, Duration timeout) {
        this.delegate = delegate;
        this.timeout = timeout;
    }

    @Override
    protected Result check() {
        var healthCheckFuture = getFuture();

        try {
            return healthCheckFuture.get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return createInterruptedResult(e);
        } catch (ExecutionException e) {
            return createExecutionErrorResult(e);
        } catch (TimeoutException e) {
            return createTimeoutResult(e);
        }
    }

    @VisibleForTesting
    CompletableFuture<HealthCheck.Result> getFuture() {
        return CompletableFuture.supplyAsync(delegate::execute, HEALTH_CHECK_EXECUTOR);
    }

    private Result createTimeoutResult(Exception e) {
        return Result.builder()
                .unhealthy(e)
                .withMessage("Unable to obtain result in %d seconds", timeout.toSeconds())
                .build();
    }

    private Result createExecutionErrorResult(Exception e) {
        return Result.builder()
                .unhealthy(e)
                .build();
    }

    private Result createInterruptedResult(Exception e) {
        return Result.builder()
                .unhealthy(e)
                .withMessage("Unable to obtain result due to process being interrupted")
                .build();
    }

}
