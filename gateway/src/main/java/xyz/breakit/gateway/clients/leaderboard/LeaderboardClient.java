package xyz.breakit.gateway.clients.leaderboard;

import io.netty.channel.ChannelOption;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

@Service
public class LeaderboardClient {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardClient.class);

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
    private static final ParameterizedTypeReference<List<LeaderboardEntry>> LEADERBOARD_LIST_TYPE = new ParameterizedTypeReference<List<LeaderboardEntry>>() {};

    private final static RetryPolicy RETRY_POLICY_WITH_NO_BACKOFF = new RetryPolicy()
            .retryOn(Throwable.class)
            .withMaxRetries(5);

    private final static RetryPolicy NO_RETRY_POLICY = new RetryPolicy()
            .retryOn(Throwable.class)
            .withMaxRetries(0);

    private final String leaderboardUrl;
    private final WebClient httpClient;
    private final BeanFactory beanFactory;

    private RetryPolicy currentRetryPolicy;


    @Autowired
    public LeaderboardClient(
            @Value("${rest.leaderboard.host}") String leaderboardHost,
            @Value("${rest.leaderboard.port}") int leaderboardPort,
            WebClient webClientTemplate,
            BeanFactory beanFactory
    ) {
        this.beanFactory = beanFactory;
        this.leaderboardUrl = "http://" + leaderboardHost + ":" + leaderboardPort;
        LOG.info("LB URL: {}", leaderboardUrl);

        HttpClient nettyHttpClient = HttpClient
                .create()
                .tcpConfiguration(client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000));

        httpClient = webClientTemplate.mutate()
                .clientConnector(new ReactorClientHttpConnector(nettyHttpClient))
                .baseUrl(leaderboardUrl).build();

        currentRetryPolicy = NO_RETRY_POLICY;
    }


    public void disableRetries() {
        currentRetryPolicy = NO_RETRY_POLICY;
    }

    public void enableRetriesWithNoBackoff() {
        currentRetryPolicy = RETRY_POLICY_WITH_NO_BACKOFF;
    }

    public CompletableFuture<List<LeaderboardEntry>> top5() {
        return Failsafe
                .with(currentRetryPolicy)
                .with(new TraceableScheduledExecutorService(beanFactory, EXECUTOR))
                .onFailedAttempt(t -> LOG.error("Error fetching top 5", t))
                .future(() -> top5Request().toFuture());
    }

    private Mono<List<LeaderboardEntry>> top5Request() {
        return httpClient
                .get()
                .uri("/top/5")
                .retrieve()
                .bodyToMono(LEADERBOARD_LIST_TYPE)
                .timeout(Duration.ofMillis(500));
    }

    public void updateScore(LeaderboardEntry newScore,
                            Runnable onMessage,
                            Consumer<? super Throwable> onError,
                            Runnable onComplete) {
        httpClient
                .post()
                .uri("/scores/")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newScore)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(500))
                .subscribe(resp -> onMessage.run(), onError, onComplete);
    }



}