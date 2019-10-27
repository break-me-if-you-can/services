package xyz.breakit.game.leaderboard.rest;

import org.springframework.web.bind.annotation.*;
import xyz.breakit.game.leaderboard.service.LeaderboardEntry;
import xyz.breakit.game.leaderboard.service.LeaderboardService;

import java.util.List;

@RestController
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }


    @GetMapping("/top/{k}")
    public List<LeaderboardEntry> top10(@PathVariable int k) throws InterruptedException {
        return leaderboardService.getTopScores(k);
    }

    @PostMapping(value = "/scores/*", consumes = "application/json")
    public void submit(@RequestBody LeaderboardEntry score) {
        leaderboardService.recordScore(score.name(), score.score());
    }

}
