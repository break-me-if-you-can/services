package xyz.breakit.game.leaderboard;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.sampler.Sampler;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import xyz.breakit.common.healthcheck.CommonHealthcheckService;
import xyz.breakit.common.instrumentation.census.GrpcCensusReporter;
import xyz.breakit.common.instrumentation.failure.AddLatencyServerInterceptor;
import xyz.breakit.common.instrumentation.failure.FailureInjectionAdminService;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;
import xyz.breakit.game.leaderboard.grpc.LeaderboardGrpcService;
import xyz.breakit.game.leaderboard.grpc.StreamingLeaderboardGrpcService;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
public class Application {

	private static final int GRPC_SERVER_PORT = 8090;
	private static final int ZPAGES_PORT = 9080;

	@Autowired
	private Server server;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public static Server grpcServer(
			Tracing tracing,
			GrpcTracing grpcTracing,
			LeaderboardGrpcService leaderboardService,
			StreamingLeaderboardGrpcService streamingLeaderboardService,
			InjectedFailureProvider failureProvider
	) {

		AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(failureProvider);

		return ServerBuilder.forPort(GRPC_SERVER_PORT)
				.addService(ServerInterceptors.intercept(leaderboardService, latencyInterceptor))
				.addService(streamingLeaderboardService)
				.addService(ProtoReflectionService.newInstance())
				.intercept(grpcTracing.newServerInterceptor())
				.addService(new CommonHealthcheckService("leaderboard", failureProvider, failureProvider))
				.addService(new FailureInjectionAdminService(new FailureInjectionService(failureProvider, failureProvider)))
				.build();
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
	public InjectedFailureProvider failureProvider() {
		return new InjectedFailureProvider();
	}

	@Bean
	public GrpcTracing grpcTracing(Tracing tracing) {
		return GrpcTracing.create(tracing);
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
