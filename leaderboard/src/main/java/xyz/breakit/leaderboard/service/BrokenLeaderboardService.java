package xyz.breakit.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BrokenLeaderboardService extends AbstractLeaderboardService {

    @Autowired
    public BrokenLeaderboardService(Scores scores) {
        super(scores);
    }

    @Override
    protected void injectFailure() {
        throw new RuntimeException("Leaderboard is broken! Alarm! Alarm!");
    }
}
