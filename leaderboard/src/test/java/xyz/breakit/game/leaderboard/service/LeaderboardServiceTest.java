package xyz.breakit.game.leaderboard.service;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class LeaderboardServiceTest {
    private LeaderboardService leaderboardService = new LeaderboardService(0);

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