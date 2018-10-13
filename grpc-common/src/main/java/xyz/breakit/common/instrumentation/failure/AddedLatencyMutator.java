package xyz.breakit.common.instrumentation.failure;

import java.time.Duration;

/**
 * Allows injecting additional latency.
 *
 * @see AddedLatencyProvider
 */
public interface AddedLatencyMutator {

    void setLatency(double probability, Duration duration);

}
