package xyz.breakit.leaderboard.rest;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.leaderboard.service.*;

@Service
public class LeaderboardFactory {
    private final BrokenLeaderboardService brokenLeaderboardService;
    private final FlakyLeaderboardService flakyLeaderboardService;
    private final NormalLeaderboardService normalLeaderboardService;
    private final RandomlySlowLeaderboardService randomlySlowLeaderboardService;
    private final SlowLeaderboardService slowLeaderboardService;

    @Autowired
    public LeaderboardFactory(
            BrokenLeaderboardService brokenLeaderboardService,
            FlakyLeaderboardService flakyLeaderboardService,
            NormalLeaderboardService normalLeaderboardService,
            RandomlySlowLeaderboardService randomlySlowLeaderboardService,
            SlowLeaderboardService slowLeaderboardService) {
        this.brokenLeaderboardService = brokenLeaderboardService;
        this.flakyLeaderboardService = flakyLeaderboardService;
        this.normalLeaderboardService = normalLeaderboardService;
        this.randomlySlowLeaderboardService = randomlySlowLeaderboardService;
        this.slowLeaderboardService = slowLeaderboardService;
    }

    AbstractLeaderboardService create(ErrorType errorType) {
        switch (ObjectUtils.defaultIfNull(errorType, ErrorType.NORMAL)) {
            case FLAKY:
                return flakyLeaderboardService;
            case SLOW:
                return slowLeaderboardService;
            case BROKEN:
                return brokenLeaderboardService;
            case RANDOMLY_SLOW:
                return randomlySlowLeaderboardService;
            case NORMAL:
            default:
                return normalLeaderboardService;
        }
    }
}
