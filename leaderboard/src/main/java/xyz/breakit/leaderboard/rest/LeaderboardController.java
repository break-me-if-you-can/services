package xyz.breakit.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.breakit.leaderboard.service.LeaderboardEntry;

import java.util.List;

@RestController
public class LeaderboardController {

    private final LeaderboardFactory leaderboardFactory;

    @Autowired
    public LeaderboardController(LeaderboardFactory leaderboardFactory) {
        this.leaderboardFactory = leaderboardFactory;
    }

    @GetMapping("/top/{k}")
    public List<LeaderboardEntry> top10(
            @PathVariable int k,
            @RequestHeader(value = "error_type", required = false) ErrorType errorType) {
        return leaderboardFactory.create(errorType).getTopScores(k);
    }

    @PostMapping(value = "/scores/*", consumes = "application/json")
    public void submit(
            @RequestBody LeaderboardEntry score,
            @RequestHeader(value = "error_type", required = false) ErrorType errorType) {
        leaderboardFactory.create(errorType).recordScore(score.name(), score.score());
    }

}
