package xyz.breakit.gateway.admin;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.HealthCheckRequest;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceFutureStub;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceImplBase;

import java.util.Objects;

/**
 * Gateway healthcheck service implementation.
 */
public final class HealthcheckGrpcService extends HealthCheckServiceImplBase {

    private final HealthcheckService healthcheckService;
    private final HealthCheckServiceFutureStub geeseHealthcheck;
    private final HealthCheckServiceFutureStub cloudsHealthcheck;

    public HealthcheckGrpcService(HealthcheckService healthcheckService,
                                  HealthCheckServiceFutureStub geeseHealthcheck,
                                  HealthCheckServiceFutureStub cloudsHealthcheck) {
        this.healthcheckService = healthcheckService;
        this.geeseHealthcheck = geeseHealthcheck;
        this.cloudsHealthcheck = cloudsHealthcheck;
    }

    @Override
    public void healthCheck(HealthCheckRequest request,
                            StreamObserver<HealthCheckResponse> responseObserver) {

        ListenableFuture<HealthCheckResponse> geeseHealth = geeseHealthcheck.healthCheck(request);
        ListenableFuture<HealthCheckResponse> cloudsHealth = cloudsHealthcheck.healthCheck(request);

        Futures.transform(Futures.successfulAsList(geeseHealth, cloudsHealth),
                remoteServicesHealth -> {
                    HealthCheckResponse.Builder response =
                            HealthCheckResponse.newBuilder()
                                    .addServiceHealthStatus(healthcheckService.healthCheck());

                    remoteServicesHealth.stream()
                            .filter(Objects::nonNull)
                            .map(HealthCheckResponse::getServiceHealthStatusList)
                            .forEach(response::addAllServiceHealthStatus);

                    responseObserver.onNext(response.build());
                    responseObserver.onCompleted();

                    return null;
                }
        );
    }
}
