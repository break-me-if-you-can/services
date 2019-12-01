package xyz.breakit.gateway;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.http.HttpTracing;
import brave.sampler.Sampler;
import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.grpc.client.GrpcClientLimiterBuilder;
import com.netflix.concurrency.limits.grpc.client.GrpcClientRequestContext;
import com.netflix.concurrency.limits.grpc.server.GrpcServerLimiterBuilder;
import com.netflix.concurrency.limits.grpc.server.GrpcServerRequestContext;
import com.netflix.concurrency.limits.limit.Gradient2Limit;
import io.grpc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.breakit.admin.AdminServiceGrpc;
import xyz.breakit.admin.AdminServiceGrpc.AdminServiceStub;
import xyz.breakit.admin.HealthCheckServiceGrpc;
import xyz.breakit.admin.HealthCheckServiceGrpc.HealthCheckServiceFutureStub;
import xyz.breakit.clouds.CloudsServiceGrpc;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceFutureStub;
import xyz.breakit.common.instrumentation.census.GrpcCensusReporter;
import xyz.breakit.common.instrumentation.tracing.ForceNewTraceServerInterceptor;
import xyz.breakit.gateway.admin.GatewayAdminService;
import xyz.breakit.gateway.admin.HealthcheckGrpcService;
import xyz.breakit.gateway.admin.HealthcheckService;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardAdminClient;
import xyz.breakit.gateway.flags.Flags;
import xyz.breakit.gateway.flags.SettableFlags;
import xyz.breakit.gateway.interceptors.FixtureMetricsReportingInterceptor;
import xyz.breakit.geese.GeeseServiceGrpc;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceFutureStub;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Gateway service entry point.
 */
@SpringBootApplication
public class Gateway {

    private static final int MAX_RETRIES = 5;
    private static final int SERVER_PORT = 8080;
    private static final int ZPAGES_PORT = 9080;

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

    @PostConstruct
    public void startCensusReporting() throws IOException {
        GrpcCensusReporter.registerAndExportViews(ZPAGES_PORT);
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
            GatewayAdminService adminService,
            HealthcheckGrpcService healthcheckService,
            Limiter<GrpcServerRequestContext> limiter,
            ForceNewTraceServerInterceptor forceNewTraceServerInterceptor) {

        return ServerBuilder.forPort(SERVER_PORT)
                .addService(fixtureService)
                .addService(playerIdService)
                .addService(leaderboardService)
                .addService(adminService)
                .addService(healthcheckService)
                .intercept(grpcTracing.newServerInterceptor())
                .intercept(forceNewTraceServerInterceptor)
                //.intercept(ConcurrencyLimitServerInterceptor.newBuilder(limiter).build())
                .build();
    }

    @Bean
    public ForceNewTraceServerInterceptor forceNewTraceServerInterceptor() {
        return new ForceNewTraceServerInterceptor();
    }

    @Bean
    public FixtureService fixtureService(Supplier<GeeseServiceFutureStub> geeseClient,
                                         Supplier<CloudsServiceFutureStub> cloudsClient,
                                         Flags flags) {
        FixtureService fixtureService = new FixtureService(geeseClient, cloudsClient, flags);
        ServerInterceptors.intercept(fixtureService, new FixtureMetricsReportingInterceptor());
        return fixtureService;
    }

    @Bean
    public PlayerIdService userIdService() {
        return new PlayerIdService();
    }

    @Bean
    public GatewayAdminService adminService(
            SettableFlags flags,
            @Qualifier("GeeseAdmin") AdminServiceStub geeseAdmin,
            @Qualifier("CloudsAdmin") AdminServiceStub cloudsAdmin) {
        return new GatewayAdminService(flags, geeseAdmin, cloudsAdmin);
    }

    @Bean
    public HealthcheckService healthcheckService(
            Flags flags,
            @Qualifier("GeeseHealthcheck") HealthCheckServiceFutureStub geeseHealthcheck,
            @Qualifier("CloudsHealthcheck") HealthCheckServiceFutureStub cloudsHealthcheck,
            LeaderboardAdminClient lbAdminClient) {
        return new HealthcheckService(flags, geeseHealthcheck, cloudsHealthcheck, lbAdminClient);
    }

    @Bean
    public HealthcheckGrpcService healthcheckGrpc(
            HealthcheckService healthcheckService) {
        return new HealthcheckGrpcService(healthcheckService);
    }

    @Bean("CloudsChannel")
    public Channel cloudsChannel(
            @Value("${grpc.clouds.host:clouds}") String cloudsHost,
            @Value("${grpc.clouds.port:8080}") int cloudsPort,
            GrpcTracing grpcTracing) {
        return ManagedChannelBuilder
                .forAddress(cloudsHost, cloudsPort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();
    }

    @Bean("CloudsChannelWithRetries")
    public Channel cloudsChannelWithRetries(
            @Value("${grpc.clouds.host:clouds}") String cloudsHost,
            @Value("${grpc.clouds.port:8080}") int cloudsPort,
            GrpcTracing grpcTracing) {
        return ManagedChannelBuilder
                .forAddress(cloudsHost, cloudsPort)
                .intercept(grpcTracing.newClientInterceptor())
                //.enableRetry()
                //.maxRetryAttempts(MAX_RETRIES)
                .usePlaintext()
                .build();
    }

    @Bean
    public Supplier<CloudsServiceFutureStub> cloudsServiceClient(
            @Qualifier("CloudsChannel") Channel cloudsChannel,
            @Qualifier("CloudsChannelWithRetries") Channel cloudsChannelWithRetries,
            Flags flags,
            Limiter<GrpcClientRequestContext> limiter) {

        //Channel channel = ClientInterceptors.intercept(cloudsChannel, new ConcurrencyLimitClientInterceptor(limiter));

        CloudsServiceFutureStub client = CloudsServiceGrpc.newFutureStub(cloudsChannel);
        CloudsServiceFutureStub clientWithRetries = CloudsServiceGrpc.newFutureStub(cloudsChannelWithRetries);

        return () -> flags.isRetryEnabled() ? clientWithRetries : client;
    }

    @Bean("CloudsAdmin")
    public AdminServiceStub cloudsAdmin(
            @Qualifier("CloudsChannel") Channel cloudsChannel) {
        return AdminServiceGrpc.newStub(cloudsChannel);
    }

    @Bean("CloudsHealthcheck")
    public HealthCheckServiceFutureStub cloudsHealthcheckClient(
            @Qualifier("CloudsChannel") Channel cloudsChannel) {
        return HealthCheckServiceGrpc.newFutureStub(cloudsChannel);
    }

    @Bean("GeeseChannel")
    public Channel geeseChannel(
            @Value("${grpc.geese.host:geese}") String geeseHost,
            @Value("${grpc.geese.port:8080}") int geesePort,
            GrpcTracing grpcTracing) {
        return ManagedChannelBuilder
                .forAddress(geeseHost, geesePort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();
    }

    @Bean("GeeseChannelWithRetries")
    public Channel geeseChannelWithRetries(
            @Value("${grpc.geese.host:geese}") String geeseHost,
            @Value("${grpc.geese.port:8080}") int geesePort,
            GrpcTracing grpcTracing) {
        return ManagedChannelBuilder
                .forAddress(geeseHost, geesePort)
                .intercept(grpcTracing.newClientInterceptor())
                //.enableRetry()
                //.maxRetryAttempts(MAX_RETRIES)
                .usePlaintext()
                .build();
    }

    @Bean
    public Supplier<GeeseServiceFutureStub> geeseServiceClient(
            @Qualifier("GeeseChannel") Channel geeseChannel,
            @Qualifier("GeeseChannelWithRetries") Channel geeseChannelWithRetries,
            Flags flags,
            Limiter<GrpcClientRequestContext> limiter) {

        //Channel channel = ClientInterceptors.intercept(geeseChannel, new ConcurrencyLimitClientInterceptor(limiter));

        GeeseServiceFutureStub client = GeeseServiceGrpc.newFutureStub(geeseChannel);
        GeeseServiceFutureStub clientWithRetries = GeeseServiceGrpc.newFutureStub(geeseChannelWithRetries);

        return () -> flags.isRetryEnabled() ? clientWithRetries : client;
    }

    @Bean("GeeseAdmin")
    public AdminServiceStub geeseAdmin(
            @Qualifier("GeeseChannel") Channel geeseChannel) {
        return AdminServiceGrpc.newStub(geeseChannel);
    }

    @Bean("GeeseHealthcheck")
    public HealthCheckServiceFutureStub geeseHealthcheckClient(
            @Qualifier("GeeseChannel") Channel geeseChannel) {
        return HealthCheckServiceGrpc.newFutureStub(geeseChannel);
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

    @Bean
    public Flags flags() {
        return new SettableFlags();
    }

}
