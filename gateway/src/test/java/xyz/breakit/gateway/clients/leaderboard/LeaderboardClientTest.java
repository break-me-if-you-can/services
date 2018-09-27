package xyz.breakit.gateway.clients.leaderboard;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracer;
import brave.Tracing;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestPropertySource("classpath:test.properties")
@RunWith(SpringRunner.class)
public class LeaderboardClientTest {

    @Autowired
    private LeaderboardClient client;

    @Autowired
    private Tracer tracer;

    @Test
    public void testTop5() throws Exception {
        Span span;
        span = tracer.newTrace();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {

            client.top5().get(15000, TimeUnit.MILLISECONDS);
        } finally {
            span.finish();
        }

        span = tracer.newTrace();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            client.top5().get(15000, TimeUnit.MILLISECONDS);
        } finally {
            span.finish();
        }
    }
}