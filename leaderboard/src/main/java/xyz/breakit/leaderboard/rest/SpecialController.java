package xyz.breakit.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.leaderboard.service.LeaderboardService;

@RestController
@RequestMapping("/magic")
public class SpecialController {

    private final LeaderboardService leaderboardService;
    private final LimiterFilter limiterFilter;

    @Autowired
    public SpecialController(LeaderboardService leaderboardService, LimiterFilter limiterFilter) {
        this.leaderboardService = leaderboardService;
        this.limiterFilter = limiterFilter;
    }

    @PostMapping(value = "/clear", headers = "secret=42")
    public void clear() {
        leaderboardService.clear();
    }

    @PostMapping(value = "/break", headers = "secret=42")
    public void breakService() {
        leaderboardService.breakService();
    }

    @PostMapping(value = "/unbreak", headers = "secret=42")
    public void unbreak() {
        leaderboardService.unbreak();
    }

    @PostMapping(value = "/rateLimit/{n}", headers = "secret=42")
    public void rateLimit(@PathVariable("n") int n) {
        limiterFilter.enable(n);
    }

    @PostMapping(value = "/clearRateLimit", headers = "secret=42")
    public void rateLimit() {
        limiterFilter.disable();
    }


}
