package xyz.breakit.gateway.admin;

import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.HealthCheckRequest;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceImplBase;

/**
 * Gateway healthcheck service implementation.
 */
public final class HealthcheckGrpcService extends HealthCheckServiceImplBase {

    private final HealthcheckService healthcheckService;

    public HealthcheckGrpcService(HealthcheckService healthcheckService) {
        this.healthcheckService = healthcheckService;
    }

    @Override
    public void healthCheck(HealthCheckRequest request,
                            StreamObserver<HealthCheckResponse> responseObserver) {
        responseObserver.onNext(healthcheckService.healthCheck());
        responseObserver.onCompleted();
    }
}
