package xyz.breakit.game.gateway.webclient;

import brave.Span;
import brave.Tracer;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TracingWebClientConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(TracingWebClientConfiguration.class);

    private static final Propagation.Setter<ClientRequest.Builder, String> SETTER =
            new Propagation.Setter<ClientRequest.Builder, String>() {
                @Override
                public void put(ClientRequest.Builder carrier, String key, String value) {
                    carrier.header(key, value);
                }

                @Override
                public String toString() {
                    return "ClientRequest.Builder::setHeader";
                }
            };


    @Bean("tracingWebClient")
    public WebClient tracingWebClient(
            WebClient webClientTemplate,
            HttpTracing httpTracing

    ) {
        HttpClientHandler<ClientRequest.Builder, ClientResponse> handler;
        TraceContext.Injector<ClientRequest.Builder> injector;

        handler = HttpClientHandler.create(httpTracing, new WebClientAdapter());
        injector = httpTracing.tracing().propagation().injector(SETTER);


        return webClientTemplate.mutate()
                .filter((request, next) -> {
                    Span span = extractSpan(httpTracing.tracing().tracer());
                    ClientRequest.Builder newRequestBuilder = ClientRequest.from(request);

                    Span newSpan = handler.handleSend(injector, newRequestBuilder, span);

                    LOG.trace("subscribing traceID: {}", span.context().traceIdString());

                    return next.exchange(newRequestBuilder.build())
                            .doOnSuccessOrError((r, e) -> {
                                handler.handleReceive(r, e, newSpan);
                                span.finish();
                                LOG.trace("finishing subscription TraceID: {}", span.context().traceIdString());
                            });
                })
                .build();
    }

    private Span extractSpan(Tracer tracer) {
        Span span = tracer.nextSpan();
        tracer.withSpanInScope(span);
        return span;
    }

}

