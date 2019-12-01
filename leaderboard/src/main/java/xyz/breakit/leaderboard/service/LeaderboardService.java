package xyz.breakit.leaderboard.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.random;

@Service
public class LeaderboardService {

    private final static Logger LOG = LoggerFactory.getLogger(LeaderboardService.class);
    public static final int MAX_HISTORY = 300;

    private final ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();

    private final AtomicBoolean broken = new AtomicBoolean(false);
    private final EmitterProcessor<Boolean> leaderboardUpdatesFlux;

    public LeaderboardService() {
        leaderboardUpdatesFlux = EmitterProcessor.create(MAX_HISTORY);
    }

    public Flux<Boolean> getLeaderboardUpdatesFlux() {
        return Flux.merge(leaderboardUpdatesFlux);
    }

    public void clear() {
        scores.clear();
    }

    public void recordScore(String name, int newScore) {
        delayIfBroken();
        scores.put(name, newScore);
        leaderboardUpdatesFlux.onNext(true);
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
        broken.set(true);
    }

    public void unbreak() {
        broken.set(false);
    }

    private void delayIfBroken() {
        if (broken.get() && random() < 0.5) {
            throw new RuntimeException("An error has been simulated!");
        }
    }

}