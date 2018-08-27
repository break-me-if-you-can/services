package xyz.breakit.gateway;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import xyz.breakit.gateway.FixtureServiceGrpc.FixtureServiceBlockingStub;

/**
 * Client to call gateway service.
 */
public class GatewayClient {

    public static void main(String[] args) {
        String host = "35.230.13.179";
        ManagedChannel channel = NettyChannelBuilder.forAddress(host, 8080)
                .usePlaintext().build();
        FixtureServiceBlockingStub client = FixtureServiceGrpc.newBlockingStub(channel);

        FixtureLineResponse response = client.getFixtureLine(GetFixtureLineRequest.getDefaultInstance());
        System.out.println("Response: " + response);
    }

}
