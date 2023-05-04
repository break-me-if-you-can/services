package xyz.breakit.leaderboard.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.Math.random;

@Service
public class LeaderboardService {

    private final static Logger LOG = LoggerFactory.getLogger(LeaderboardService.class);

    private final ConcurrentMap<String, Integer> scores = new ConcurrentHashMap<>();
    private final AtomicBoolean broken = new AtomicBoolean(false);

    public void clear() {
        scores.clear();
    }

    public void recordScore(String name, int newScore) {
        int existingScore = scores.getOrDefault(name, 0);
        scores.put(name, Math.max(newScore, existingScore));
    }

    public boolean isBroken() {
        return broken.get();
    }

    public List<LeaderboardEntry> getTopScores(int k) {
        delayIfBroken();

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

    public void breakService() {
        LOG.info("leaderboard is broken");
        broken.set(true);
    }

    public void unbreak() {
        LOG.info("leaderboard is unbroken");
        broken.set(false);
    }

    private void delayIfBroken() {
        if (broken.get() && random() < 0.5) {
            LOG.info("An error has been simulated!");
            throw new RuntimeException("An error has been simulated!");
        }
    }

}