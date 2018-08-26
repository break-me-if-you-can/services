package xyz.breakit.leaderboard.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import xyz.breakit.leaderboard.service.ImmutableLeaderboardEntry;
import xyz.breakit.leaderboard.service.*;
import java.util.Arrays;
import java.util.List;

@RestController
public class LeaderboardController {

    @GetMapping("/top10")
    public Mono<List<AbstractLeaderboardEntry>> top10() {
        Mono<List<AbstractLeaderboardEntry>> result = Mono.just(Arrays.asList(
                ImmutableLeaderboardEntry.builder().name("a").score(100).build(),
                ImmutableLeaderboardEntry.builder().name("b").score(99).build(),
                ImmutableLeaderboardEntry.builder().name("c").score(98).build(),
                ImmutableLeaderboardEntry.builder().name("d").score(97).build(),
                ImmutableLeaderboardEntry.builder().name("e").score(96).build(),
                ImmutableLeaderboardEntry.builder().name("f").score(95).build(),
                ImmutableLeaderboardEntry.builder().name("g").score(94).build(),
                ImmutableLeaderboardEntry.builder().name("h").score(93).build(),
                ImmutableLeaderboardEntry.builder().name("j").score(92).build(),
                ImmutableLeaderboardEntry.builder().name("k").score(91).build()
        ));
        return result;
    }

}
