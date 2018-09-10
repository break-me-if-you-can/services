package xyz.breakit.gateway;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import xyz.breakit.clouds.CloudsServiceGrpc;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceFutureStub;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardClient;
import xyz.breakit.geese.GeeseServiceGrpc;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceFutureStub;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.io.IOException;

import static brave.sampler.Sampler.ALWAYS_SAMPLE;

/**
 * Gateway service entry point.
 */
public class Gateway {

    public static void main(String... args) throws IOException, InterruptedException {

        String geeseHost = System.getenv("geese_host");
        int geesePort = Integer.valueOf(System.getenv("geese_port"));

        String cloudsHost = System.getenv("clouds_host");
        int cloudsPort = Integer.valueOf(System.getenv("clouds_port"));

        String lbHost = System.getenv("leaderboard_host");
        int lbPort = Integer.valueOf(System.getenv("leaderboard_port"));
        String lbUrl = "http://"+lbHost+":"+lbPort;

        GrpcTracing grpcTracing = grpcTracing();

        Channel geeseChannel = ManagedChannelBuilder
                .forAddress(geeseHost, geesePort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();
        GeeseServiceFutureStub geeseClient = GeeseServiceGrpc.newFutureStub(geeseChannel);

        Channel cloudsChannel = ManagedChannelBuilder
                .forAddress(cloudsHost, cloudsPort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();
        CloudsServiceFutureStub cloudsClient = CloudsServiceGrpc.newFutureStub(cloudsChannel);

        Server server = ServerBuilder.forPort(8080)
                .addService(new FixtureService(geeseClient, cloudsClient))
                .addService(new LeaderboardService(new LeaderboardClient(lbUrl)))
                .intercept(grpcTracing.newServerInterceptor())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

    private static GrpcTracing grpcTracing() {

        String zipkinHost = System.getenv().getOrDefault("ZIPKIN_SERVICE_HOST", "zipkin");
        int zipkinPort = Integer.valueOf(System.getenv().getOrDefault("ZIPKIN_SERVICE_PORT", "9411"));

        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(String.format("http://%s:%s/api/v2/spans", zipkinHost, zipkinPort))
                .build();

        return GrpcTracing.create(Tracing.newBuilder()
                .sampler(ALWAYS_SAMPLE)
                .spanReporter(AsyncReporter.create(sender))
                .build());
    }



}
