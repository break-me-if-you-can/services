package xyz.breakit.game.gateway;

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
import io.grpc.protobuf.services.ProtoReflectionService;
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
import xyz.breakit.common.healthcheck.CommonHealthcheckService;
import xyz.breakit.common.instrumentation.census.GrpcCensusReporter;
import xyz.breakit.common.instrumentation.failure.AddLatencyServerInterceptor;
import xyz.breakit.common.instrumentation.failure.FailureInjectionAdminService;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;
import xyz.breakit.common.instrumentation.tracing.ForceNewTraceServerInterceptor;
import xyz.breakit.game.gateway.admin.GatewayAdminService;
import xyz.breakit.game.gateway.admin.HealthcheckGrpcService;
import xyz.breakit.game.gateway.admin.HealthcheckService;
import xyz.breakit.game.gateway.flags.Flags;
import xyz.breakit.game.gateway.flags.SettableFlags;
import xyz.breakit.game.gateway.interceptors.FixtureMetricsReportingInterceptor;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceFutureStub;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceStub;
import xyz.breakit.game.leaderboard.StreamingLeaderboardServiceGrpc;
import xyz.breakit.game.leaderboard.StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceStub;
import xyz.breakit.game.playerid.PlayerIdServiceGrpc;
import xyz.breakit.game.playerid.PlayerIdServiceGrpc.PlayerIdServiceStub;
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
    public static Server grpcServer(
            GrpcTracing grpcTracing,
            FixtureService fixtureService,
            PlayerIdService playerIdService,
            LeaderboardService leaderboardService,
            StreamingLeaderboardService streamingLeaderboardService,
            GatewayAdminService adminService,
            HealthcheckGrpcService healthcheckService,
            Limiter<GrpcServerRequestContext> limiter,
            ForceNewTraceServerInterceptor forceNewTraceServerInterceptor,
            FailureInjectionService failureInjectionService,
            InjectedFailureProvider failureProvider
    ) {

        AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(failureProvider);

        return ServerBuilder.forPort(SERVER_PORT)
                .addService(fixtureService)
                .addService(ServerInterceptors.intercept(playerIdService, latencyInterceptor))
                .addService(leaderboardService)
                .addService(streamingLeaderboardService)
                .addService(adminService)
                .addService(healthcheckService)
                .addService(ProtoReflectionService.newInstance())
                .intercept(grpcTracing.newServerInterceptor())
                .intercept(forceNewTraceServerInterceptor)
                .addService(new FailureInjectionAdminService(new FailureInjectionService(failureProvider, failureProvider)))
                .addService(new CommonHealthcheckService("gateway", failureProvider, failureProvider))
                .build();
    }

    @Bean
    public InjectedFailureProvider gwFailureProvider() {
        return new InjectedFailureProvider();
    }

    @Bean
    public FailureInjectionService failureInjectionService(InjectedFailureProvider failureProvider) {
        return new FailureInjectionService(failureProvider, failureProvider);
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
    public PlayerIdService userIdService(PlayerIdServiceStub playerIdClient) {
        return new PlayerIdService(playerIdClient);
    }

    @Bean
    public GatewayAdminService adminService(
            SettableFlags flags,
            @Qualifier("GeeseAdmin") AdminServiceStub geeseAdmin,
            @Qualifier("CloudsAdmin") AdminServiceStub cloudsAdmin,
            @Qualifier("LeadeboardAdmin") AdminServiceStub leaderboardAdmin,
            @Qualifier("PlayerIdAdmin") AdminServiceStub playerIdAdmin,
            FailureInjectionService failureInjectionService) {
        return new GatewayAdminService(flags, geeseAdmin, cloudsAdmin, leaderboardAdmin, playerIdAdmin,
                failureInjectionService);
    }

    @Bean
    public HealthcheckService healthcheckService(
            Flags flags,
            @Qualifier("GeeseHealthcheck") HealthCheckServiceFutureStub geeseHealthcheck,
            @Qualifier("CloudsHealthcheck") HealthCheckServiceFutureStub cloudsHealthcheck,
            @Qualifier("LeaderboardHealthcheck") HealthCheckServiceFutureStub leaderboardHealthcheck,
            @Qualifier("PlayerIdHealthcheck") HealthCheckServiceFutureStub playerIdHealthcheck) {
        return new HealthcheckService(flags, geeseHealthcheck, cloudsHealthcheck, leaderboardHealthcheck,
                playerIdHealthcheck);
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

    @Bean("LeadeboardAdmin")
    public AdminServiceStub leaderboardAdmin(
            @Qualifier("LeaderboardChannel") Channel leaderboardChannel) {
        return AdminServiceGrpc.newStub(leaderboardChannel);
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

    @Bean("LeaderboardHealthcheck")
    public HealthCheckServiceFutureStub leaderboardHealthcheckClient(
            @Qualifier("LeaderboardChannel") Channel leaderboardChannel) {
        return HealthCheckServiceGrpc.newFutureStub(leaderboardChannel);
    }

    @Bean
    public LeaderboardServiceStub leaderboardClient(
            @Qualifier("LeaderboardChannel") Channel leaderboardChannel) {
        return LeaderboardServiceGrpc.newStub(leaderboardChannel);
    }

    @Bean
    public LeaderboardServiceFutureStub leaderboardFutureClient(
            @Qualifier("LeaderboardChannel") Channel leaderboardChannel) {
        return LeaderboardServiceGrpc.newFutureStub(leaderboardChannel);
    }

    @Bean
    public StreamingLeaderboardServiceStub streamingLeaderboardClient(
            @Qualifier("LeaderboardChannel") Channel leaderboardChannel) {
        return StreamingLeaderboardServiceGrpc.newStub(leaderboardChannel);
    }

    @Bean("LeaderboardChannel")
    public Channel leaderboardChannel(
            @Value("${grpc.leaderboard.host:leaderboard}") String lbHost,
            @Value("${grpc.leaderboard.port:8090}") int lbPort,
            GrpcTracing grpcTracing) {
        return ManagedChannelBuilder
                .forAddress(lbHost, lbPort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();
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

    @Bean("PlayerIdChannel")
    public Channel playerIdChannel(
            @Value("${grpc.playerid.host:playerid}") String playerIdHost,
            @Value("${grpc.playerid.port:8110}") int playerIdPort,
            GrpcTracing grpcTracing) {
        return ManagedChannelBuilder
                .forAddress(playerIdHost, playerIdPort)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();
    }

    @Bean
    public PlayerIdServiceStub playerIdClient(@Qualifier("PlayerIdChannel") Channel playerIdChannel) {
        return PlayerIdServiceGrpc.newStub(playerIdChannel);
    }

    @Bean("PlayerIdHealthcheck")
    public HealthCheckServiceFutureStub playerIdHealthcheck(@Qualifier("PlayerIdChannel") Channel playerIdChannel) {
        return HealthCheckServiceGrpc.newFutureStub(playerIdChannel);
    }

    @Bean("PlayerIdAdmin")
    public AdminServiceStub playerIdAdmin(@Qualifier("PlayerIdChannel") Channel playerIdChannel) {
        return AdminServiceGrpc.newStub(playerIdChannel);
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
