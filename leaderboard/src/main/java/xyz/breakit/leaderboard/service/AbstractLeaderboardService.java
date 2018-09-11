package xyz.breakit.leaderboard.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public abstract class AbstractLeaderboardService {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractLeaderboardService.class);

    private final Scores scores;

    protected AbstractLeaderboardService(Scores scores) {
        this.scores = scores;
    }

    public void clear() {
        scores.getScores().clear();
    }

    public void recordScore(String name, int newScore) {
        injectFailure();
        scores.getScores().put(name, newScore);
    }

    public List<LeaderboardEntry> getTopScores(int k) {
        injectFailure();

        Map<String, Integer> currentScores = ImmutableMap.copyOf(scores.getScores());

        return currentScores.entrySet()
                .stream()
                .sorted(this::topScoresFirst)
                .limit(k)
                .map(this::toLeaderboardEntry)
                .collect(Collectors.toList());
    }

    private ImmutableLeaderboardEntry toLeaderboardEntry(Map.Entry<String, Integer> e) {
        return ImmutableLeaderboardEntry.builder().name(e.getKey()).score(e.getValue()).build();
    }

    private int topScoresFirst(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
        return b.getValue() - a.getValue();
    }


    protected abstract void injectFailure();

}