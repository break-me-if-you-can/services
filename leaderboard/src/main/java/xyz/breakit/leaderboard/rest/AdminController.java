package xyz.breakit.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.leaderboard.service.AbstractLeaderboardService;
import xyz.breakit.leaderboard.service.Scores;

@RestController
@RequestMapping("/magic")
public class AdminController {

    private final Scores scores;
    private final LimiterFilter limiterFilter;

    @Autowired
    public AdminController(Scores scores, LimiterFilter limiterFilter) {
        this.scores = scores;
        this.limiterFilter = limiterFilter;
    }

    @PostMapping(value = "/clear", headers = "secret=42")
    public void clear() {
        scores.getScores().clear();
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
