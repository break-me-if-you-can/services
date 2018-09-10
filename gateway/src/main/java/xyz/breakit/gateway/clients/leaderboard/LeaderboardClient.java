package xyz.breakit.gateway.clients.leaderboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LeaderboardClient {

    // one per core
    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(4);

    private static final CircuitBreaker CIRCUIT_BREAKER = new CircuitBreaker()
            .withFailureThreshold(3, 5);


    private final static RetryPolicy RETRY_POLICY = new RetryPolicy()
            .withBackoff(1, 30, TimeUnit.SECONDS, 2.0)
            .withJitter(300, TimeUnit.MILLISECONDS)
            .retryOn(Throwable.class)
            .withMaxRetries(5);

    private final String leaderboardUrl;
    private final ObjectMapper objectMapper;
    private final WebClient httpClient;

    public LeaderboardClient(String leaderboardUrl) {
        objectMapper = new ObjectMapper();

        this.leaderboardUrl = leaderboardUrl;
        httpClient = WebClient.builder().build();
    }

    public CompletableFuture<List<LeaderboardEntry>> top5() throws IOException {
        return Failsafe
                .with(RETRY_POLICY)
                //.with(CIRCUIT_BREAKER)
                .with(EXECUTOR)
                .future(this::top5Request);
    }

    public void updateScore(LeaderboardEntry newScore) throws IOException {
        httpClient
                .post()
                .uri(leaderboardUrl + "/top/5")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(newScore)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .subscribeOn(Schedulers.elastic());
    }

    private CompletableFuture<List<LeaderboardEntry>> top5Request() throws IOException {
        Mono<ClientResponse> response = httpClient
                .get()
                .uri(leaderboardUrl + "/top/5")
                .exchange().timeout(Duration.ofMillis(500))
                .subscribeOn(Schedulers.elastic());

        return response
                .flatMap(cr -> {
                    if (cr.statusCode().value() != 200) {
                        throw new RuntimeException("HTTP Error: " + cr.statusCode().value());
                    } else {
                        return cr.bodyToMono(new ParameterizedTypeReference<List<LeaderboardEntry>>() {});
                    }
                })
                .toFuture();
    }
}