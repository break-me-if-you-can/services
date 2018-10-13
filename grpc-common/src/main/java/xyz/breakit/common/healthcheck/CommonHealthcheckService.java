package xyz.breakit.common.healthcheck;

import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.HealthCheckRequest;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceImplBase;
import xyz.breakit.admin.ServiceHealthCheckStatus;
import xyz.breakit.common.instrumentation.failure.AddedLatencyProvider;

import java.time.Duration;

/**
 * Common implementation of healthcheck service.
 * Reads status from {@link AddedLatencyProvider}.
 */
public class CommonHealthcheckService extends HealthCheckServiceImplBase {

    private final String serviceName;
    private final AddedLatencyProvider latencyProvider;

    public CommonHealthcheckService(String serviceName,
                                    AddedLatencyProvider latencyProvider) {
        this.serviceName = serviceName;
        this.latencyProvider = latencyProvider;
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
        ServiceHealthCheckStatus healthStatus = ServiceHealthCheckStatus.newBuilder()
                .setAddedLatency(latencySpec)
                .build();
        HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .addServiceHealthStatus(healthStatus)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
