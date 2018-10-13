package xyz.breakit.gateway.admin;

import xyz.breakit.admin.ServiceHealthCheckStatus;
import xyz.breakit.gateway.flags.Flags;

import static xyz.breakit.gateway.admin.ServiceNames.GATEWAY_SERVICE;

/**
 * Service to provide healthcheck.
 */
public final class HealthcheckService {

    private final Flags flags;

    public HealthcheckService(Flags flags) {
        this.flags = flags;
    }

    public ServiceHealthCheckStatus healthCheck() {
        return ServiceHealthCheckStatus.newBuilder()
                .setServiceName(GATEWAY_SERVICE)
                .setPartialDegradationEnabled(flags.isPartialDegradationEnabled())
                .build();
    }

}
