package xyz.breakit.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class FlakyLeaderboardService extends AbstractLeaderboardService {

    private final double errorProbability;
    private final Random rnd = new Random();

    @Autowired
    public FlakyLeaderboardService(
            @Value("${leaderboard.error.probability:0.1}") double errorProbability,
            Scores scores) {
        super(scores);
        this.errorProbability = errorProbability;
    }

    @Override
    protected void injectFailure() {
        double chance = rnd.nextInt(101) / 100.0;
        if (chance < errorProbability) {
            throw new RuntimeException("Leaderboard is broken! Alarm! Alarm!");
        }
    }
}
