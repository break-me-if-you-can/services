package xyz.breakit.game.gateway;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import xyz.breakit.admin.HealthCheckRequest;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.HealthCheckServiceGrpc;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceBlockingStub;

/**
 * Client to call gateway service healthcheck API.
 */
public class GatewayHealthcheckClient {

    public static void main(String... args) {
        String host = "35.233.196.238";
        Channel channel = ManagedChannelBuilder
                .forAddress(host, 80)
                .usePlaintext()
                .build();

        HealthCheckServiceBlockingStub healthCheckClient =
                HealthCheckServiceGrpc.newBlockingStub(channel);

        HealthCheckResponse healthCheckResponse =
                healthCheckClient.healthCheck(HealthCheckRequest.getDefaultInstance());
        System.out.println("HealthCheckResponse: " + healthCheckResponse);
    }

}
