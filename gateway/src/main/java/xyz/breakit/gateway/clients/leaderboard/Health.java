package xyz.breakit.gateway.clients.leaderboard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
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