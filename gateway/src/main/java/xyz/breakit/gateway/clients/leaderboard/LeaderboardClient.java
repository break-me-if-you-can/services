package xyz.breakit.gateway.clients.leaderboard;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.tuple.Tuple;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class LeaderboardClient {

    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardClient.class);
    private static final String CLIENT_SPAN_KEY = "sleuth.webclient.clientSpan";

    static final Propagation.Setter<ClientRequest.Builder, String> SETTER =
            new Propagation.Setter<ClientRequest.Builder, String>() {
                @Override
                public void put(ClientRequest.Builder carrier, String key, String value) {
                    carrier.header(key, value);
                }

                @Override
                public String toString() {
                    return "ClientRequest::setHeader";
                }
            };

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
    private final HttpTracing httpTracing;
    private final Tracing tracing;

    final Tracer tracer;
    final HttpClientHandler<ClientRequest.Builder, ClientResponse> handler;
    final TraceContext.Injector<ClientRequest.Builder> injector;

    @Autowired
    public LeaderboardClient(
            @Value("${rest.leaderboard.host}") String leaderboardHost,
            @Value("${rest.leaderboard.port}") int leaderboardPort,
            WebClient webClientTemplate,
            HttpTracing httpTracing,
            Tracing tracing,
            Tracer tracer
            ) {
        this.httpTracing = httpTracing;
        this.tracing = tracing;
        this.leaderboardUrl = "http://" + leaderboardHost + ":" + leaderboardPort;
        LOG.info("LB URL: {}", leaderboardUrl);

        this.tracer = tracer; //httpTracing.tracing().tracer();
        handler = HttpClientHandler.create(httpTracing, new WebClientAdapter());
        injector = httpTracing.tracing().propagation().injector(SETTER);

        httpClient = webClientTemplate.mutate().baseUrl(leaderboardUrl)
                .filter((request, next) -> {
                            Span span = (Span) request.attribute(CLIENT_SPAN_KEY).get();
                            LOG.info("subscribing traceID: {}", span.context().traceIdString());
                            ClientRequest.Builder newRequestBuilder = ClientRequest.from(request);
                            newRequestBuilder.attribute("zipkin.span", span);
                            return next.exchange(newRequestBuilder.build())
                                    .doOnSuccessOrError((r, e) -> {
                                        LOG.info("finishing subscription TraceID: {}", span.context().traceIdString());
                                    });
                        }
                )
                .build();
    }

    private Span extractFromContext(Context c) {
        try {
            return (Span) c.get(CLIENT_SPAN_KEY);
        } catch (Exception e) {
            return tracing.tracer().nextSpan();
        }
    }

    private Span extractSpan(Tracing tracing, Tuple2<Span, Boolean> t) {
        Span span = t.getT1();
        if (span == null) {
            span = tracing.tracer().nextSpan();

        }
        return span;
    }


    public CompletableFuture<List<LeaderboardEntry>> top5() {
        Span span = tracer.currentSpan();
        return Failsafe
                .with(RETRY_POLICY)
                //.with(CIRCUIT_BREAKER)
                .with(EXECUTOR)
                .future(() -> top5Request(span));
    }

    public void updateScore(LeaderboardEntry newScore) {
        httpClient
                .post()
                .uri("/scores/")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(newScore)
                .exchange()
                .timeout(Duration.ofMillis(500))
                //.subscriberContext(context -> context.put(CLIENT_SPAN_KEY, span))
                .then();
    }

    private CompletableFuture<List<LeaderboardEntry>> top5Request(Span span) {
        return httpClient
                .get()
                .uri("/top/5")
                .attribute(CLIENT_SPAN_KEY, span)
                .exchange()
                .timeout(Duration.ofMillis(500))
                .flatMap(cr -> cr.bodyToMono(new ParameterizedTypeReference<List<LeaderboardEntry>>() {}))
                //.subscriberContext(context -> context.put(CLIENT_SPAN_KEY, span))

                .toFuture();
    }

}