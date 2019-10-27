package xyz.breakit.game.gateway.clients.leaderboard;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.util.Durations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.breakit.admin.AddedLatencySpec;
import xyz.breakit.admin.FailureCodeSpec;
import xyz.breakit.admin.HealthCheckResponse;
import xyz.breakit.admin.ServiceHealthCheckStatus;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceFutureStub;
import xyz.breakit.game.leaderboard.PlayerScore;
import xyz.breakit.game.leaderboard.UpdateScoreRequest;
import xyz.breakit.game.leaderboard.UpdateScoreResponse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class LeaderboardAdminClient {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardAdminClient.class);

    private final String leaderboardUrl;
    private final WebClient httpClient;
    private final LeaderboardServiceFutureStub leaderboardServiceFutureStub;

    @Autowired
    public LeaderboardAdminClient(
            @Value("${rest.leaderboard.host:leaderboard}") String leaderboardHost,
            @Value("${rest.leaderboard.port:8080}") int leaderboardPort,
            @Qualifier("tracingWebClient") WebClient webClientTemplate,
            LeaderboardServiceFutureStub leaderboardServiceFutureStub) {
        this.leaderboardUrl = "http://" + leaderboardHost + ":" + leaderboardPort;
        LOG.info("LB URL: {}", leaderboardUrl);

        this.httpClient = webClientTemplate.mutate().baseUrl(leaderboardUrl).build();
        this.leaderboardServiceFutureStub = leaderboardServiceFutureStub;
    }

    public CompletableFuture<HealthCheckResponse> health() {
        return httpClient
                .get()
                .uri("/admin/health")
                .exchange()
                .timeout(Duration.ofMillis(500))
                .doOnNext(this::checkStatusCode)
                .flatMap(cr -> cr.bodyToMono(Health.class))
                .map(this::toServiceHealthCheckStatus)
                .map(status ->
                        HealthCheckResponse.newBuilder().addServiceHealthStatus(status).build())
                .toFuture();
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

    public CompletableFuture<Void> enableLimit(int limit) {
        return makeRestCall("/admin/rateLimit/" + limit);
    }

    public CompletableFuture<Void> disableLimit() {
        return makeRestCall("/admin/disableRateLimit");
    }


    public CompletableFuture<Void> breakService() {
        return makeRestCall("/admin/break");
    }

    public CompletableFuture<Void> unbreakService() {
        return makeRestCall("/admin/unbreak");
    }

    public Future<Void> clear() {
        return makeRestCall("/admin/clear");
    }

    public Future<Void> initStrangeLeaderboard() {
        CompletableFuture<Void> clear = makeRestCall("/admin/clear");
        return clear.thenRun(this::setStrangeLeaderboard);
    }

    private CompletableFuture<Void> makeRestCall(String s) {
        return httpClient
                .post()
                .uri(s)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .doOnNext(this::checkStatusCode)
                .flatMap(cr -> cr.bodyToMono(String.class))
                .then().toFuture();
    }

    private Future<?> setStrangeLeaderboard() {

        ListenableFuture<UpdateScoreResponse> madmax =
                leaderboardServiceFutureStub.updateScore(UpdateScoreRequest.newBuilder()
                        .setPlayerScore(PlayerScore.newBuilder().setPlayerId("MADMAX")
                                .setScore(751300)).build());
        ListenableFuture<UpdateScoreResponse> dustin =
                leaderboardServiceFutureStub.updateScore(UpdateScoreRequest.newBuilder()
                        .setPlayerScore(PlayerScore.newBuilder().setPlayerId("DUSTIN")
                                .setScore(650990)).build());

        return Futures.allAsList(madmax, dustin);
    }

    private void checkStatusCode(ClientResponse cr) {
        if (cr.statusCode().value() != 200) {
            throw new RuntimeException("Got HTTP code of " + cr.statusCode().value());
        }
    }

}