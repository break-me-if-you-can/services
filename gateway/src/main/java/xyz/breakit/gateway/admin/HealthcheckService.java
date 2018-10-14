package xyz.breakit.gateway.admin;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import xyz.breakit.admin.HealthCheckRequest;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceFutureStub;
import xyz.breakit.admin.ServiceHealthCheckStatus;
import xyz.breakit.gateway.flags.Flags;

import java.util.Objects;

import static xyz.breakit.gateway.admin.ServiceNames.GATEWAY_SERVICE;

/**
 * Service to provide healthcheck.
 */
public final class HealthcheckService {

    private final Flags flags;
    private final HealthCheckServiceFutureStub geeseHealthcheck;
    private final HealthCheckServiceFutureStub cloudsHealthcheck;

    public HealthcheckService(Flags flags,
                              HealthCheckServiceFutureStub geeseHealthcheck,
                              HealthCheckServiceFutureStub cloudsHealthcheck) {
        this.flags = flags;
        this.geeseHealthcheck = geeseHealthcheck;
        this.cloudsHealthcheck = cloudsHealthcheck;
    }

    public ListenableFuture<HealthCheckResponse> healthCheck() {

        HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

        ListenableFuture<HealthCheckResponse> geeseHealth = geeseHealthcheck.healthCheck(request);
        ListenableFuture<HealthCheckResponse> cloudsHealth = cloudsHealthcheck.healthCheck(request);

        return Futures.transform(Futures.successfulAsList(geeseHealth, cloudsHealth),
                remoteServicesHealth -> {
                    ServiceHealthCheckStatus.Builder gatewayHealth = ServiceHealthCheckStatus.newBuilder()
                            .setServiceName(GATEWAY_SERVICE)
                            .setPartialDegradationEnabled(flags.isPartialDegradationEnabled())
                            .setRetryEnabled(flags.isRetryEnabled());

                    HealthCheckResponse.Builder response =
                            HealthCheckResponse.newBuilder()
                                    .addServiceHealthStatus(gatewayHealth);

                    remoteServicesHealth.stream()
                            .filter(Objects::nonNull)
                            .map(HealthCheckResponse::getServiceHealthStatusList)
                            .forEach(response::addAllServiceHealthStatus);

                    return response.build();
                }
        );
    }

}
