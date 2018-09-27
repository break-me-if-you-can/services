package xyz.breakit.leaderboard.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final static Logger LOG = LoggerFactory.getLogger(LeaderboardService.class);

    private final ConcurrentMap<String, Integer> scores = new ConcurrentHashMap<>();

    public void clear() {
        scores.clear();
    }

    public void recordScore(String name, int newScore) {
        scores.put(name, newScore);
    }

    public List<LeaderboardEntry> getTopScores(int k) {
        Map<String, Integer> currentScores = ImmutableMap.copyOf(scores);

        return currentScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(k)
                .map(this::toLeaderboardEntry)
                .collect(Collectors.toList());
    }

    private ImmutableLeaderboardEntry toLeaderboardEntry(Map.Entry<String, Integer> e) {
        return ImmutableLeaderboardEntry.builder().name(e.getKey()).score(e.getValue()).build();
    }

}