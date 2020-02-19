package xyz.breakit.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.breakit.leaderboard.service.LeaderboardService;

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
    public String clear() {
        leaderboardService.clear();
        return "OK";
    }

    @PostMapping(value = "/break")
    public String breakService() {
        leaderboardService.breakService();
        return "OK";
    }

    @PostMapping(value = "/unbreak")
    public String unbreak() {
        leaderboardService.unbreak();
        return "OK";
    }

    @PostMapping(value = "/rateLimit/{n}")
    public String rateLimit(@PathVariable("n") int n) {
        limiterFilter.enable(n);
        return "OK";
    }

    @PostMapping(value = "/disableRateLimit")
    public String disableRateLimit() {
        limiterFilter.disable();
        return "OK";
    }

}
