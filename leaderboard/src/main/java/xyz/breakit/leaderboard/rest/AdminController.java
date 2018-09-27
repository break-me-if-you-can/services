package xyz.breakit.leaderboard.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.breakit.leaderboard.service.LeaderboardService;

@RestController
@RequestMapping("/magic")
public class AdminController {

    private final LeaderboardService leaderboardService;

    @Autowired
    public AdminController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @PostMapping(value = "/clear", headers = "secret=42")
    public void clear() {
        leaderboardService.clear();
    }

}
