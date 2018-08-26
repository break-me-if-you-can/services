package xyz.breakit.leaderboard.service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
public interface AbstractLeaderboardEntry {
    String name();
    int score();
}
