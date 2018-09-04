package xyz.breakit.gateway;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import xyz.breakit.clouds.CloudsServiceGrpc;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceFutureStub;
import xyz.breakit.geese.GeeseServiceGrpc;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceFutureStub;

import java.io.IOException;

/**
 * Gateway service entry point.
 */
public class Gateway {

    public static void main(String... args) throws IOException, InterruptedException {

        String geeseHost = System.getenv("geese_host");
        int geesePort = Integer.valueOf(System.getenv("geese_port"));

        String cloudsHost = System.getenv("clouds_host");
        int cloudsPort = Integer.valueOf(System.getenv("clouds_port"));

        Channel geeseChannel = ManagedChannelBuilder
                .forAddress(geeseHost, geesePort).usePlaintext().build();
        GeeseServiceFutureStub geeseClient = GeeseServiceGrpc.newFutureStub(geeseChannel);
        Channel cloudsChannel = ManagedChannelBuilder
                .forAddress(cloudsHost, cloudsPort).usePlaintext().build();
        CloudsServiceFutureStub cloudsClient = CloudsServiceGrpc.newFutureStub(cloudsChannel);

        Server server = ServerBuilder.forPort(8080)
                .addService(new FixtureService(geeseClient, cloudsClient))
                .addService(new LeaderboardService())
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
