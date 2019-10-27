package xyz.breakit.game.gateway;

import com.google.protobuf.util.Durations;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.AdminServiceGrpc;
import xyz.breakit.admin.InjectFailureRequest;

/**
 * Client for admin Gateway API. Allows injecting latency.
 */
public class GatewayAdminClient {

    public static void main(String... args) {
        String host = "35.233.196.238";
        Channel channel = ManagedChannelBuilder
                .forAddress(host, 80)
                .usePlaintext()
                .build();

        AdminServiceGrpc.AdminServiceBlockingStub adminServiceClient =
                AdminServiceGrpc.newBlockingStub(channel);

        InjectFailureRequest injectFailure = InjectFailureRequest.newBuilder()
                .setServiceName("clouds")
                .setAddedLatency(AddedLatencySpec.newBuilder()
                        .setProbability(0.01).setDuration(Durations.fromSeconds(2)))
                .build();
        adminServiceClient.injectFailure(injectFailure);
    }

}
