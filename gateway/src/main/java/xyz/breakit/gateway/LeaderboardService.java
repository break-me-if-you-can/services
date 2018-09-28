package xyz.breakit.gateway;

import brave.Span;
import brave.Tracing;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.gateway.LeaderboardServiceGrpc.LeaderboardServiceImplBase;
import xyz.breakit.gateway.clients.leaderboard.ImmutableLeaderboardEntry;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardClient;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardEntry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of gateway leaderboard service.
 * Currently returns predefined responses, but should make
 * remote calls to leaderboard service instead.
 */
@Service
public class LeaderboardService extends LeaderboardServiceImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(LeaderboardService.class);

    private final LeaderboardClient leaderboardClient;
    private final Tracing tracing;

    @Autowired
    public LeaderboardService(LeaderboardClient leaderboardClient, Tracing tracing) {
        this.leaderboardClient = leaderboardClient;
        this.tracing = tracing;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {
        CompletableFuture<List<LeaderboardEntry>> top5 = leaderboardClient.top5();
        top5.whenCompleteAsync((l, e) -> {
            if (e != null) {
                responseObserver.onError(e);
            } else {
                List<PlayerScore> playerScores = l.stream()
                        .map(score -> PlayerScore.newBuilder().setPlayerId(score.name()).setScore(score.score()).build())
                        .collect(Collectors.toList());

                TopScoresResponse response = TopScoresResponse.newBuilder()
                        .addAllTopScores(playerScores)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        });
    }

    @Override
    public void updateScore(UpdateScoreRequest request,
                            StreamObserver<UpdateScoreResponse> responseObserver) {

        LeaderboardEntry entry = ImmutableLeaderboardEntry
                .builder()
                .name(request.getPlayerScore().getPlayerId())
                .score(request.getPlayerScore().getScore())
                .build();

        Span span = tracing.tracer().nextSpan();
        LOG.info("Outer TraceId = {}", span.context().traceId());

        leaderboardClient.updateScore(entry);
        responseObserver.onNext(UpdateScoreResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
