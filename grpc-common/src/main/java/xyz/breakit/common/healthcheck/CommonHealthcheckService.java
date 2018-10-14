package xyz.breakit.common.healthcheck;

import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.*;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceImplBase;
import xyz.breakit.common.instrumentation.failure.AddedLatencyProvider;
import xyz.breakit.common.instrumentation.failure.FixtureFailureProvider;

import java.time.Duration;

/**
 * Common implementation of healthcheck service.
 * Reads status from {@link AddedLatencyProvider}.
 */
public class CommonHealthcheckService extends HealthCheckServiceImplBase {

    private final String serviceName;
    private final AddedLatencyProvider latencyProvider;
    private final FixtureFailureProvider fixtureFailureProvider;

    public CommonHealthcheckService(String serviceName,
                                    AddedLatencyProvider latencyProvider,
                                    FixtureFailureProvider fixtureFailureProvider) {
        this.serviceName = serviceName;
        this.latencyProvider = latencyProvider;
        this.fixtureFailureProvider = fixtureFailureProvider;
    }

    @Override
    public void healthCheck(HealthCheckRequest request,
                            StreamObserver<HealthCheckResponse> responseObserver) {

        double latencyProbability = latencyProvider.latencyProbability();
        Duration duration = latencyProvider.duration();

        AddedLatencySpec.Builder latencySpec =
                AddedLatencySpec.newBuilder().setProbability(latencyProbability);
        if (duration != null && !duration.isZero()) {
            latencySpec.setDuration(Durations.fromNanos(duration.toNanos()));
        }
        FixtureFailureSpec.Builder fixtureFailure =
                FixtureFailureSpec.newBuilder()
                        .setFullFixtureEnabled(fixtureFailureProvider.isFullFixtureEnabled());
        ServiceHealthCheckStatus healthStatus = ServiceHealthCheckStatus.newBuilder()
                .setServiceName(serviceName)
                .setAddedLatency(latencySpec)
                .setFixtureFailure(fixtureFailure)
                .build();
        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .addServiceHealthStatus(healthStatus)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
