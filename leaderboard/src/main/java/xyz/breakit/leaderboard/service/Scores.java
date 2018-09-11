package xyz.breakit.leaderboard.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class Scores {
    private final ConcurrentMap<String, Integer> scores = new ConcurrentHashMap<>();

    public ConcurrentMap<String, Integer> getScores() {
        return scores;
    }
}
