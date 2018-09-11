package xyz.breakit.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NormalLeaderboardService extends AbstractLeaderboardService {
    @Autowired
    public NormalLeaderboardService(Scores scores) {
        super(scores);
    }

    @Override
    protected void injectFailure() {
        // no failure
    }
}
