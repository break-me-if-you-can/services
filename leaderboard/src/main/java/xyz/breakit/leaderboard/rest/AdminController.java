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
        Health health = new Health();
        health.setBroken(leaderboardService.isBroken());
        return health;
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
