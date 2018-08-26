package xyz.breakit.leaderboard.rest;

import com.google.common.io.Resources;
import org.junit.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.charset.Charset;


public class LeaderboardControllerTest {

    private WebTestClient webTestClient = WebTestClient.bindToController(new LeaderboardController()).build();;

    @Test
    public void testTop10() throws Exception {
        webTestClient.get().uri("/top10")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(top10());
    }

    private String top10() throws IOException {
        return Resources.toString(Resources.getResource("top10.json"), Charset.defaultCharset());
    }

}
