package xyz.breakit.game.gateway.admin;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import xyz.breakit.admin.HealthCheckRequest;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceFutureStub;
import xyz.breakit.admin.ServiceHealthCheckStatus;
import xyz.breakit.game.gateway.flags.Flags;

import java.util.Objects;

/**
 * Service to provide healthcheck.
 */
public final class HealthcheckService {

    private final Flags flags;
    private final HealthCheckServiceFutureStub geeseHealthcheck;
    private final HealthCheckServiceFutureStub cloudsHealthcheck;
    private final HealthCheckServiceFutureStub leaderboardHealthcheck;
    private final HealthCheckServiceFutureStub playeridHealthcheck;

    public HealthcheckService(Flags flags,
                              HealthCheckServiceFutureStub geeseHealthcheck,
                              HealthCheckServiceFutureStub cloudsHealthcheck,
                              HealthCheckServiceFutureStub leaderboardHealthcheck,
                              HealthCheckServiceFutureStub playeridHealthcheck) {
        this.flags = flags;
        this.geeseHealthcheck = geeseHealthcheck;
        this.cloudsHealthcheck = cloudsHealthcheck;
        this.leaderboardHealthcheck = leaderboardHealthcheck;
        this.playeridHealthcheck = playeridHealthcheck;
    }

    public ListenableFuture<HealthCheckResponse> healthCheck() {

        HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();

        ListenableFuture<HealthCheckResponse> geeseHealth = geeseHealthcheck.healthCheck(request);
        ListenableFuture<HealthCheckResponse> cloudsHealth = cloudsHealthcheck.healthCheck(request);
        ListenableFuture<HealthCheckResponse> leaderboardHealth = leaderboardHealthcheck.healthCheck(request);
        ListenableFuture<HealthCheckResponse> playeridHealth = playeridHealthcheck.healthCheck(request);

        return Futures.transform(Futures.successfulAsList(geeseHealth, cloudsHealth, leaderboardHealth, playeridHealth),
                remoteServicesHealth -> {
                    ServiceHealthCheckStatus.Builder gatewayHealth = ServiceHealthCheckStatus.newBuilder()
                            .setServiceName(ServiceNames.GATEWAY_SERVICE)
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
                },
                MoreExecutors.directExecutor()
        );
    }

}
