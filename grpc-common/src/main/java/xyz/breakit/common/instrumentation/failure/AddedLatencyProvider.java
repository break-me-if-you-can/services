package xyz.breakit.common.instrumentation.failure;

import java.time.Duration;

/**
 * Provides probability and duration of added latency.
 * Supposed to be used together with {@link AddLatencyServerInterceptor} to add delays to server calls.
 * <p>
 * Values returned by {@link #latencyProbability()} and {@link #duration()} can
 * be dynamic, driven by flags, experiments, etc.
 * This enables changing latency and probability dynamically without
 * reinstantiating interceptors, reconfiguring servers, etc.
 */
public interface AddedLatencyProvider {

    /**
     * @return probability of adding latency, a double between
     * {@code 0.0} and {@code 1.0}. for adding latency to all the calls,
     * {@code addedLatencyProbability} should return {@code 1.0}.
     * For adding latency to half of the calls (50%), it should return
     * {@code 0.5}. {@code 0.0} means no added latency.
     */
    double latencyProbability();

    /**
     * @return duration to be added to server calls.
     */
    Duration duration();

}
