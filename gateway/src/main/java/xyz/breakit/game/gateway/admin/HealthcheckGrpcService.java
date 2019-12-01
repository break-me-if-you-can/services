package xyz.breakit.game.gateway.admin;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
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

        Futures.transform(healthcheckService.healthCheck(), response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    return null;
                },
        MoreExecutors.directExecutor());
    }
}
