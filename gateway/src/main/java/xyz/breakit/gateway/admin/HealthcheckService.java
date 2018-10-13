package xyz.breakit.gateway.admin;

import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.ServiceHealthCheckStatus;
import xyz.breakit.gateway.flags.Flags;

/**
 * Service to provide healthcheck.
 */
public final class HealthcheckService {

    private final Flags flags;

    public HealthcheckService(Flags flags) {
        this.flags = flags;
    }

    public HealthCheckResponse healthCheck() {
        ServiceHealthCheckStatus gatewayStatus =
                ServiceHealthCheckStatus.newBuilder()
                        .setServiceName("gateway")
                        .setPartialDegradationEnabled(flags.isPartialDegradationEnabled())
                        .build();

        return HealthCheckResponse.newBuilder()
                .addServiceHealthStatus(gatewayStatus)
                .build();
    }

}
