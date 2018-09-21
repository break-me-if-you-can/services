package xyz.breakit.gateway;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.sampler.Sampler;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.breakit.clouds.CloudsServiceGrpc;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceFutureStub;
import xyz.breakit.geese.GeeseServiceGrpc;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceFutureStub;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Gateway service entry point.
 */
@SpringBootApplication
public class Gateway {

    private static final int MAX_RETRIES = 5;

    @Autowired
    private Server server;

    public static void main(String... args) {
        SpringApplication.run(Gateway.class, args);
    }

    @PostConstruct
    public void startGrpcServer() throws IOException {
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }

    @Bean
    public WebClient webClient(){
        return WebClient.builder().build();
    }

    @Bean
    public Server grpcServer(
            GrpcTracing grpcTracing,
            GeeseServiceFutureStub geeseClient,
            CloudsServiceFutureStub cloudsClient,
            LeaderboardService leaderboardService) {
        return ServerBuilder.forPort(8080)
                .addService(new FixtureService(geeseClient, cloudsClient))
                .addService(leaderboardService)
                .intercept(grpcTracing.newServerInterceptor())
                .build();
    }

    @Bean
    public CloudsServiceFutureStub cloudsService(
            @Value("${grpc.clouds.host:clouds}") String cloudsHost,
            @Value("${grpc.clouds.port:8080}") int cloudsPort,
            GrpcTracing grpcTracing) {
        Channel cloudsChannel = ManagedChannelBuilder
                .forAddress(cloudsHost, cloudsPort)
                .intercept(grpcTracing.newClientInterceptor())
                .enableRetry()
                .maxRetryAttempts(MAX_RETRIES)
                .usePlaintext()
                .build();
        return CloudsServiceGrpc.newFutureStub(cloudsChannel);
    }

    @Bean
    public GeeseServiceFutureStub geeseService(
            @Value("${grpc.geese.host:geese}") String geeseHost,
            @Value("${grpc.geese.port:8080}") int geesePort,
            GrpcTracing grpcTracing) {
        Channel geeseChannel = ManagedChannelBuilder
                .forAddress(geeseHost, geesePort)
                .intercept(grpcTracing.newClientInterceptor())
                .enableRetry()
                .maxRetryAttempts(MAX_RETRIES)
                .usePlaintext()
                .build();
        GeeseServiceFutureStub geeseStub = GeeseServiceGrpc.newFutureStub(geeseChannel);
        return geeseStub;
    }

    @Bean
    public GrpcTracing grpcTracing(Sampler sampler) {

        String zipkinHost = System.getenv().getOrDefault("ZIPKIN_SERVICE_HOST", "zipkin");
        int zipkinPort = Integer.valueOf(System.getenv().getOrDefault("ZIPKIN_SERVICE_PORT", "9411"));

        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(String.format("http://%s:%s/api/v2/spans", zipkinHost, zipkinPort))
                .build();

        return GrpcTracing.create(Tracing.newBuilder()
                .sampler(sampler)
                .spanReporter(AsyncReporter.create(sender))
                .build());
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }

}
