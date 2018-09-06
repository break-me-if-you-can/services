package xyz.breakit.leaderboard.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final static Logger LOG = LoggerFactory.getLogger(LeaderboardService.class);

    private final ConcurrentMap<String, Integer> scores = new ConcurrentHashMap<>();
    private final AtomicBoolean broken = new AtomicBoolean(false);

    public void clear() {
        scores.clear();
    }

    public void recordScore(String name, int newScore) {
        delayIfBroken();
        scores.put(name, newScore);
    }

    public List<LeaderboardEntry> getTopScores(int k) {
        delayIfBroken();

        Map<String, Integer> currentScores = ImmutableMap.copyOf(scores);

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

    public void breakService() {
        broken.set(true);
    }

    public void unbreak() {
        broken.set(false);
    }

    private void delayIfBroken() {
        if (broken.get()) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}