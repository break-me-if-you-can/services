package xyz.breakit.common.instrumentation.failure;

import com.google.protobuf.util.Durations;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.InjectFailureRequest;

import java.time.Duration;

/**
 * Inject failures to {@link AddedLatencyMutator}.
 */
public final class FailureInjectionService {

    private final AddedLatencyMutator addedLatencyMutator;
    private final FixtureFailureMutator fixtureFailureMutator;

    public FailureInjectionService(AddedLatencyMutator addedLatencyMutator,
                                   FixtureFailureMutator fixtureFailureMutator) {
        this.addedLatencyMutator = addedLatencyMutator;
        this.fixtureFailureMutator = fixtureFailureMutator;
    }

    public void injectFailure(InjectFailureRequest request) {

        switch (request.getFailureCase()) {
            case ADDED_LATENCY:
                AddedLatencySpec addedLatency = request.getAddedLatency();
                Duration duration = Duration.ofNanos(Durations.toNanos(addedLatency.getDuration()));
                addedLatencyMutator.setLatency(
                        addedLatency.getProbability(), duration);

                break;
            case FAILURE_NOT_SET:
                addedLatencyMutator.setLatency(0.0, Duration.ZERO);
                break;
            default:
                StatusRuntimeException invalidArgumentException =
                        Status.INVALID_ARGUMENT
                                .withDescription(
                                        "Unsupported failure type: " + request.getFailureCase())
                                .asRuntimeException();
                throw invalidArgumentException;
        }

        boolean fullFixtureEnabled = request.getFixtureFailure().getFullFixtureEnabled();
        fixtureFailureMutator.setFullFixtureEnabled(fullFixtureEnabled);
    }

}
