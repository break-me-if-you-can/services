package xyz.breakit.gateway;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.http.HttpTracing;
import brave.sampler.Sampler;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.grpc.client.ConcurrencyLimitClientInterceptor;
import com.netflix.concurrency.limits.grpc.client.GrpcClientLimiterBuilder;
import com.netflix.concurrency.limits.grpc.client.GrpcClientRequestContext;
import com.netflix.concurrency.limits.grpc.server.ConcurrencyLimitServerInterceptor;
import com.netflix.concurrency.limits.grpc.server.GrpcServerLimiterBuilder;
import com.netflix.concurrency.limits.grpc.server.GrpcServerRequestContext;
import com.netflix.concurrency.limits.limit.Gradient2Limit;
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
    public static final int SERVER_PORT = 8080;

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
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public Server grpcServer(
            GrpcTracing grpcTracing,
            FixtureService fixtureService,
            PlayerIdService playerIdService,
            LeaderboardService leaderboardService,
            Limiter<GrpcServerRequestContext> limiter) {

        return ServerBuilder.forPort(SERVER_PORT)
                .addService(fixtureService)
                .addService(playerIdService)
                .addService(leaderboardService)
                .intercept(grpcTracing.newServerInterceptor())
                .intercept(ConcurrencyLimitServerInterceptor.newBuilder(limiter).build())
                .build();
    }

    @Bean
    public FixtureService fixtureService(GeeseServiceFutureStub geeseClient,
                                         CloudsServiceFutureStub cloudsClient) {
        return new FixtureService(geeseClient, cloudsClient);
    }

    @Bean
    public PlayerIdService userIdService() {
        return new PlayerIdService();
    }

    @Bean
    public CloudsServiceFutureStub cloudsService(
            @Value("${grpc.clouds.host:clouds}") String cloudsHost,
            @Value("${grpc.clouds.port:8080}") int cloudsPort,
            GrpcTracing grpcTracing,
            Limiter<GrpcClientRequestContext> limiter) {

        Channel cloudsChannel = ManagedChannelBuilder
                .forAddress(cloudsHost, cloudsPort)
                .intercept(grpcTracing.newClientInterceptor())
                .intercept(new ConcurrencyLimitClientInterceptor(limiter))
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
            GrpcTracing grpcTracing,
            Limiter<GrpcClientRequestContext> limiter) {
        Channel geeseChannel = ManagedChannelBuilder
                .forAddress(geeseHost, geesePort)
                .intercept(grpcTracing.newClientInterceptor())
                .intercept(new ConcurrencyLimitClientInterceptor(limiter))
                .enableRetry()
                .maxRetryAttempts(MAX_RETRIES)
                .usePlaintext()
                .build();
        GeeseServiceFutureStub geeseStub = GeeseServiceGrpc.newFutureStub(geeseChannel);
        return geeseStub;
    }

    @Bean
    public GrpcTracing grpcTracing(Tracing tracing) {
        return GrpcTracing.create(tracing);
    }

    @Bean
    public HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }

    @Bean
    public Limiter<GrpcClientRequestContext> grpcClientLimiter() {
        return new GrpcClientLimiterBuilder()
                .limit(Gradient2Limit.newBuilder().initialLimit(1000).build())
                .blockOnLimit(false) // fail-fast
                .build();
    }

    @Bean
    public Limiter<GrpcServerRequestContext> grpcServerLimiter() {
        return new GrpcServerLimiterBuilder()
                .limit(Gradient2Limit.newBuilder().initialLimit(1000).build())
                .build();
    }

    @Bean
    public Tracing tracing(Sampler sampler) {
        String zipkinHost = System.getenv().getOrDefault("ZIPKIN_SERVICE_HOST", "zipkin");
        int zipkinPort = Integer.valueOf(System.getenv().getOrDefault("ZIPKIN_SERVICE_PORT", "9411"));

        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(String.format("http://%s:%s/api/v2/spans", zipkinHost, zipkinPort))
                .build();

        return Tracing.newBuilder()
                .sampler(sampler)
                .spanReporter(AsyncReporter.create(sender))
                .build();
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }

}
