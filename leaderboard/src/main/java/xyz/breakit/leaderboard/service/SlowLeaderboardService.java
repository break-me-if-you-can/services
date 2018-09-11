package xyz.breakit.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlowLeaderboardService extends AbstractLeaderboardService {

    private final long delayMs;

    @Autowired
    public SlowLeaderboardService(
            @Value("${leaderboard.delay.ms:700}") long delayMs,
            Scores scores) {
        super(scores);
        this.delayMs = delayMs;
    }

    @Override
    protected void injectFailure() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
