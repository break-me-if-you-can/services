package xyz.breakit.gateway.clients.leaderboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class LeaderboardAdminClient {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardAdminClient.class);
    public static final String EMPTY_BODY = "";

    private final String leaderboardUrl;
    private final WebClient httpClient;


    @Autowired
    public LeaderboardAdminClient(
            @Value("${rest.leaderboard.host}") String leaderboardHost,
            @Value("${rest.leaderboard.port}") int leaderboardPort,
            @Qualifier("tracingWebClient") WebClient webClientTemplate
    ) {
        this.leaderboardUrl = "http://" + leaderboardHost + ":" + leaderboardPort;
        LOG.info("LB URL: {}", leaderboardUrl);

        httpClient = webClientTemplate.mutate().baseUrl(leaderboardUrl).build();
    }


    public CompletableFuture<Void> enableLimit(int limit) {
        return httpClient
                .post()
                .uri("/admin/rateLimit/" + limit)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .doOnNext(this::checkStatusCode)
                .then()
                .toFuture();
    }

    public CompletableFuture<Void> disableLimit() {
        return httpClient
                .post()
                .uri("/admin/disableRateLimit")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .doOnNext(this::checkStatusCode)
                .then()
                .toFuture();
    }


    public CompletableFuture<Void> breakService() {
        return httpClient
                .post()
                .uri("/admin/break")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .doOnNext(this::checkStatusCode)
                .then()
                .toFuture();
    }

    public CompletableFuture<Void> unbreakService() {
        return httpClient
                .post()
                .uri("/admin/unbreak")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .doOnNext(this::checkStatusCode)
                .then().toFuture();
    }

    private void checkStatusCode(ClientResponse cr) {
        if (cr.statusCode().value() != 200) {
            throw new RuntimeException("Got HTTP code of " + cr.statusCode().value());
        }
    }

}