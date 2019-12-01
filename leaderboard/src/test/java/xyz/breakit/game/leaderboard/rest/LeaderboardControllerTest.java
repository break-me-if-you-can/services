package xyz.breakit.game.leaderboard.rest;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import xyz.breakit.game.leaderboard.service.ImmutableLeaderboardEntry;
import xyz.breakit.game.leaderboard.service.LeaderboardService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LeaderboardControllerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private LeaderboardService leaderboardService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeaderboardController(leaderboardService)).build();
    }

    @Test
    public void testTop2() throws Exception {
        // given
        twoEntriesInLeaderboard();

        // when
        mockMvc.perform(get("/top/2"))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(top2()));
    }

    private void twoEntriesInLeaderboard() {
        when(leaderboardService.getTopScores(2)).thenReturn(
                Arrays.asList(
                        ImmutableLeaderboardEntry.builder().name("a").score(100).build(),
                        ImmutableLeaderboardEntry.builder().name("b").score(99).build()
                )
        );
    }

    @Test
    public void testSubmit() throws Exception {
        mockMvc.perform(
                post("/scores/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"a\", \"score\": 100}")
        ).andExpect(status().isOk());
    }

    private String top2() throws IOException {
        return Resources.toString(Resources.getResource("top2.json"), Charset.defaultCharset());
    }

}
