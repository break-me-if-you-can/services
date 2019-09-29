package xyz.breakit.leaderboard.service;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import xyz.breakit.leaderboard.service.LeaderboardService;

import static org.junit.Assert.*;

public class LeaderboardServiceTest {
    private LeaderboardService leaderboardService = new LeaderboardService();

    @Test(timeout = 1000L)
    public void testLeaderboardUpdatesFlux() {
        Flux<Boolean> leaderboardUpdatesFlux = leaderboardService.getLeaderboardUpdatesFlux();
        leaderboardService.recordScore("a", 300);
        leaderboardService.recordScore("b", 200);
        leaderboardService.recordScore("c", 400);

        StepVerifier.create(leaderboardUpdatesFlux)
                .expectNext(true)
                .expectNext(true)
                .expectNext(true)
                .thenCancel()
                .verify();

    }
}