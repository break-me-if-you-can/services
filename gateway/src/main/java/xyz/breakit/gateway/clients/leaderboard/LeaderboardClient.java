package xyz.breakit.gateway.clients.leaderboard;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class LeaderboardClient {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardClient.class);

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    private static final CircuitBreaker CIRCUIT_BREAKER = new CircuitBreaker()
            .withFailureThreshold(3, 5);

    private final static RetryPolicy RETRY_POLICY = new RetryPolicy()
            .withBackoff(1, 30, TimeUnit.SECONDS, 2.0)
            .withJitter(300, TimeUnit.MILLISECONDS)
            .retryOn(Throwable.class)
            .withMaxRetries(5);

    private final String leaderboardUrl;
    private final WebClient httpClient;


    @Autowired
    public LeaderboardClient(
            @Value("${rest.leaderboard.host}") String leaderboardHost,
            @Value("${rest.leaderboard.port}") int leaderboardPort,
            @Qualifier("tracingWebClient") WebClient webClientTemplate
    ) {
        this.leaderboardUrl = "http://" + leaderboardHost + ":" + leaderboardPort;
        LOG.info("LB URL: {}", leaderboardUrl);

        httpClient = webClientTemplate.mutate().baseUrl(leaderboardUrl).build();
    }


    public CompletableFuture<List<LeaderboardEntry>> top5() {
        CompletableFuture<List<LeaderboardEntry>> future = top5Request()
                .toFuture();

        return Failsafe
                .with(RETRY_POLICY)
                //.with(CIRCUIT_BREAKER)
                //.with(EXECUTOR)
                .get(() -> future);
    }


    public void updateScore(LeaderboardEntry newScore,
                            Runnable onMessage,
                            Consumer<? super Throwable> onError,
                            Runnable onComplete) {
        httpClient
                .post()
                .uri("/scores/")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(newScore)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .subscribe(resp -> onMessage.run(), onError, onComplete);
    }

    private Mono<List<LeaderboardEntry>> top5Request() {
        return httpClient
                .get()
                .uri("/top/5")
                .exchange()
                .timeout(Duration.ofMillis(500))
                .flatMap(cr -> cr.bodyToMono(new ParameterizedTypeReference<List<LeaderboardEntry>>() {
                }));
    }

}