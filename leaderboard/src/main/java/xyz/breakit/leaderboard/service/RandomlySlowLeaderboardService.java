package xyz.breakit.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomlySlowLeaderboardService extends AbstractLeaderboardService {

    private final double errorProbability;
    private final Random rnd = new Random();
    private final long delayMs;

    @Autowired
    public RandomlySlowLeaderboardService(
            @Value("${leaderboard.error.probability:0.1}") double errorProbability,
            @Value("${leaderboard.delay.ms:700}") long delayMs,
            Scores scores) {
        super(scores);
        this.errorProbability = errorProbability;
        this.delayMs = delayMs;
    }

    @Override
    protected void injectFailure() {
        double chance = rnd.nextInt(101) / 100.0;
        if (chance < errorProbability) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
