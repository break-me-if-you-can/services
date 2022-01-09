package xyz.breakit.gateway.clients.leaderboard;

import com.google.protobuf.util.Durations;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.breakit.admin.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Service
public class LeaderboardAdminClient {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardAdminClient.class);

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    private final static RetryPolicy<Object> NO_RETRY_POLICY = RetryPolicy.builder()
            .withMaxRetries(0)
            .build();

    private final WebClient httpClient;
    private final BeanFactory beanFactory;


    @Autowired
    public LeaderboardAdminClient(
            @Value("${rest.leaderboard.host}") String leaderboardHost,
            @Value("${rest.leaderboard.port}") int leaderboardPort,
            WebClient webClientTemplate,
            BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        String leaderboardUrl = "http://" + leaderboardHost + ":" + leaderboardPort;
        LOG.info("LB URL: {}", leaderboardUrl);

        httpClient = webClientTemplate.mutate()
                .baseUrl(leaderboardUrl).build();
    }

    public CompletableFuture<HealthCheckResponse> health() {
        return Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .get()
                                .uri("/admin/health")
                                .retrieve()
                                .bodyToMono(Health.class)
                                .timeout(Duration.ofMillis(1000))
                                .map(this::toServiceHealthCheckStatus)
                                .map(status ->
                                        HealthCheckResponse
                                                .newBuilder()
                                                .addServiceHealthStatus(status
                                                ).build())
                                .toFuture());
    }

    private ServiceHealthCheckStatus toServiceHealthCheckStatus(Health health) {
        AddedLatencySpec addedLatencySpec = health.latencyEnabled() ?
                AddedLatencySpec.newBuilder()
                        .setDuration(Durations.fromMillis(health.latencyMs()))
                        .setProbability(health.latencyProbability())
                        .build()
                : AddedLatencySpec.getDefaultInstance();

        FailureCodeSpec failureCodeSpec = health.failureEnabled() ?
                FailureCodeSpec.newBuilder()
                        .setFailureProbability(health.failureProbability())
                        .setHttpStatusCode(health.httpErrorCode())
                        .build()
                : FailureCodeSpec.getDefaultInstance();

        return ServiceHealthCheckStatus.newBuilder()
                .setServiceName("leaderboard")
                .setCodeFailure(failureCodeSpec)
                .setAddedLatency(addedLatencySpec)
                .build();

    }

    public CompletableFuture<String> breakService() {
        return Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .post()
                                .uri("/admin/break")
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(String.class)
                                .timeout(Duration.ofMillis(1000))
                                .toFuture());
    }

    public CompletableFuture<String> unbreakService() {
        return Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .post()
                                .uri("/admin/unbreak")
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(String.class)
                                .timeout(Duration.ofMillis(1000))
                                .toFuture());
    }

    public CompletableFuture<String> clear() {
        return Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .post()
                                .uri("/admin/clear")
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(String.class)
                                .timeout(Duration.ofMillis(1000))
                                .toFuture());
    }

}