package xyz.breakit.leaderboard;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import com.netflix.concurrency.limits.Limiter;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.SpanAdjuster;
import org.springframework.context.annotation.Bean;
import xyz.breakit.common.healthcheck.CommonHealthcheckService;
import xyz.breakit.common.instrumentation.census.GrpcCensusReporter;
import xyz.breakit.common.instrumentation.failure.AddLatencyServerInterceptor;
import xyz.breakit.common.instrumentation.failure.FailureInjectionAdminService;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;
import xyz.breakit.common.instrumentation.tracing.ForceNewTraceServerInterceptor;
import xyz.breakit.geese.GeeseServiceGrpc;
import xyz.breakit.leaderboard.grpc.LeaderboardGrpcService;
import xyz.breakit.leaderboard.service.LeaderboardService;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
public class Application {

	private static final int GRPC_SERVER_PORT = 8090;
	private static final int ZPAGES_PORT = 9080;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public SpanAdjuster customSpanAdjuster() {
		return span -> span.toBuilder().name("#leaderboard/" + span.name().replace("http:/", "")).build();
	}

	@Bean
	public static Server grpcServer(
			GrpcTracing grpcTracing,
			LeaderboardGrpcService leaderboardService,
			InjectedFailureProvider failureProvider
	) {

		return ServerBuilder.forPort(GRPC_SERVER_PORT)
				.addService(leaderboardService)
				.addService(ProtoReflectionService.newInstance())
				.intercept(grpcTracing.newServerInterceptor())
				.addService(new FailureInjectionAdminService(new FailureInjectionService(failureProvider, failureProvider)))
				.addService(new CommonHealthcheckService("gateway", failureProvider, failureProvider))
				.build();
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

}
