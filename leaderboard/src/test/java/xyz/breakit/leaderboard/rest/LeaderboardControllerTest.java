package xyz.breakit.leaderboard.rest;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.web.reactive.server.WebTestClient;
import xyz.breakit.leaderboard.service.ImmutableLeaderboardEntry;
import xyz.breakit.leaderboard.service.LeaderboardEntry;
import xyz.breakit.leaderboard.service.LeaderboardService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class LeaderboardControllerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private LeaderboardService leaderboardService;

    private WebTestClient webTestClient;

    @Before
    public void setup() {
        webTestClient = WebTestClient.bindToController(new LeaderboardController(leaderboardService)).build();
    }

    @Test
    public void testTop2() throws Exception {
        // given
        twoEntriesInLeaderboard();

        // when
        webTestClient.get().uri("/top/2")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectBody().json(top2());
    }

    private OngoingStubbing<List<LeaderboardEntry>> twoEntriesInLeaderboard() {
        return when(leaderboardService.getTopScores(2)).thenReturn(
                Arrays.asList(
                        ImmutableLeaderboardEntry.builder().name("a").score(100).build(),
                        ImmutableLeaderboardEntry.builder().name("b").score(99).build()
                )
        );
    }

    @Test
    public void testSubmit() throws Exception {
        webTestClient.post().uri("/scores/submit")
                .syncBody(ImmutableLeaderboardEntry.builder().name("name").score(100).build())
                .exchange()
                .expectStatus().isOk();
    }

    private String top2() throws IOException {
        return Resources.toString(Resources.getResource("top2.json"), Charset.defaultCharset());
    }

}
