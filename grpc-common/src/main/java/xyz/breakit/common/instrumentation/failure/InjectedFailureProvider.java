package xyz.breakit.common.instrumentation.failure;

import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simple in-memory mutable implementation of {@link AddedLatencyProvider}
 * and {@link FixtureFailureProvider}.
 */
@ThreadSafe
public final class InjectedFailureProvider implements
        AddedLatencyProvider, AddedLatencyMutator, FixtureFailureProvider, FixtureFailureMutator {

    private final AtomicDouble latencyProbability = new AtomicDouble();
    private final AtomicReference<Duration> duration = new AtomicReference<>();
    private final AtomicBoolean fullFixtureEnabled = new AtomicBoolean();

    @Override
    public double latencyProbability() {
        return latencyProbability.doubleValue();
    }

    @Override
    public Duration duration() {
        return duration.get();
    }

    @Override
    public void setLatency(double probability, Duration duration) {
        // two separate updates, not an atomic one,
        // but it's ok for the purpose of this demo
        this.latencyProbability.set(probability);
        this.duration.set(duration);
    }

    @Override
    public boolean isFullFixtureEnabled() {
        return fullFixtureEnabled.get();
    }

    @Override
    public void setFullFixtureEnabled(boolean enabled) {
        fullFixtureEnabled.set(enabled);
    }
}
