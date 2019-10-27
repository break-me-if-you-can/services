package xyz.breakit.game.gateway.clients.leaderboard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLeaderboardEntry.class)
@JsonDeserialize(as = ImmutableLeaderboardEntry.class)
public interface LeaderboardEntry {
    String name();
    int score();
}
