package xyz.breakit.game.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.breakit.game.leaderboard.service.LeaderboardService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final LeaderboardService leaderboardService;
    private final LimiterFilter limiterFilter;

    @Autowired
    public AdminController(LeaderboardService leaderboardService, LimiterFilter limiterFilter) {
        this.leaderboardService = leaderboardService;
        this.limiterFilter = limiterFilter;
    }

    @GetMapping(value = "/health")
    public Health health() {
        return ImmutableHealth.builder()
                .failureEnabled(leaderboardService.isBroken())
                .failureProbability(0.5)
                .httpErrorCode(500)
                .latencyEnabled(false)
                .latencyMs(0)
                .latencyProbability(0.0)
                .build();
    }

    @PostMapping(value = "/clear")
    public void clear() {
        leaderboardService.clear();
    }

    @PostMapping(value = "/break")
    public void breakService() {
        leaderboardService.breakService();
    }

    @PostMapping(value = "/unbreak")
    public void unbreak() {
        leaderboardService.unbreak();
    }

    @PostMapping(value = "/rateLimit/{n}")
    public void rateLimit(@PathVariable("n") int n) {
        limiterFilter.enable(n);
    }

    @PostMapping(value = "/disableRateLimit")
    public void disableRateLimit() {
        limiterFilter.disable();
    }


}
