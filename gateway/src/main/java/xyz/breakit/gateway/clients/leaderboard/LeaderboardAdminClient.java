package xyz.breakit.gateway.clients.leaderboard;

import com.google.protobuf.util.Durations;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import xyz.breakit.admin.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

        HttpClient nettyHttpClient = HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        httpClient = webClientTemplate.mutate()
                .clientConnector(new ReactorClientHttpConnector(nettyHttpClient))
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

    public void breakService() {
        Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .post()
                                .uri("/admin/break")
                                .retrieve()
                                .toBodilessEntity()
                                .subscribeOn(Schedulers.boundedElastic())
                                .toFuture().get(1L, TimeUnit.SECONDS));
    }

    public void unbreakService() {
        Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .post()
                                .uri("/admin/unbreak")
                                .retrieve()
                                .toBodilessEntity()
                                .subscribeOn(Schedulers.boundedElastic())
                                .toFuture().get(1L, TimeUnit.SECONDS)
                                );
    }

    public void clear() {
        Failsafe
                .with(NO_RETRY_POLICY)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .get(() ->
                        httpClient
                                .post()
                                .uri("/admin/clear")
                                .retrieve()
                                .toBodilessEntity()
                                .subscribeOn(Schedulers.boundedElastic())
                                .toFuture().get(1L, TimeUnit.SECONDS));
    }

}