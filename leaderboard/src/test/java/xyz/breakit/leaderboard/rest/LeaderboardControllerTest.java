package xyz.breakit.leaderboard.rest;

import com.google.common.io.Resources;
import org.junit.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import xyz.breakit.leaderboard.service.ImmutableLeaderboardEntry;

import java.io.IOException;
import java.nio.charset.Charset;

public class LeaderboardControllerTest {

    private WebTestClient webTestClient = WebTestClient.bindToController(new LeaderboardController()).build();;

    @Test
    public void testTop10() throws Exception {
        webTestClient.get().uri("/top/10")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(top10());
    }

    @Test
    public void testSubmit() throws Exception {
        webTestClient.post().uri("/scores/submit")
                .syncBody(ImmutableLeaderboardEntry.builder().name("name").score(100).build())
                .exchange()
                .expectStatus().isOk();
    }

    private String top10() throws IOException {
        return Resources.toString(Resources.getResource("top10.json"), Charset.defaultCharset());
    }

}
