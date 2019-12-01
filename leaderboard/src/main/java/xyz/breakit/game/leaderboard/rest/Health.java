package xyz.breakit.game.leaderboard.rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableHealth.class)
@JsonDeserialize(as = ImmutableHealth.class)
public interface Health {
    boolean failureEnabled();
    double failureProbability();
    int httpErrorCode();
    boolean latencyEnabled();
    double latencyProbability();
    long latencyMs();
}
