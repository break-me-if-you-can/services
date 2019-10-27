package xyz.breakit.game.leaderboard.service;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.Math.random;

@Service
public class LeaderboardService {

    private final static Logger LOG = LoggerFactory.getLogger(LeaderboardService.class);
    private static final int MAX_HISTORY = 300;

    private final ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();

    private final AtomicBoolean broken = new AtomicBoolean(false);
    private final EmitterProcessor<Boolean> leaderboardUpdatesProcessor;
    private final Flux<Boolean> leaderboardUpdatesFlux;

    public LeaderboardService(@Value("${leadeboard.getTopScores.sampleWindowInMs:1000}") int sampleWindowInMs) {
        leaderboardUpdatesProcessor = EmitterProcessor.<Boolean>create(MAX_HISTORY);
        leaderboardUpdatesFlux = sampleWindowInMs == 0 ?
                leaderboardUpdatesProcessor
                : leaderboardUpdatesProcessor.sample(Duration.ofMillis(sampleWindowInMs));
    }

    public Flux<Boolean> getLeaderboardUpdatesFlux() {
        return Flux.create(sink -> leaderboardUpdatesFlux.doOnNext(sink::next).subscribe());
    }

    public void clear() {
        scores.clear();
    }

    public void recordScore(String name, int newScore) {
        delayIfBroken();
        scores.put(name, newScore);
        leaderboardUpdatesProcessor.onNext(true);
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