package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
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

    private final LeaderboardClient leaderboardClient;

    @Autowired
    public LeaderboardService(LeaderboardClient leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {
        CompletableFuture<List<LeaderboardEntry>> top5 = leaderboardClient.top5();
        top5.whenComplete((l, e) -> {
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

        leaderboardClient.updateScore(entry,
                () -> responseObserver.onNext(UpdateScoreResponse.newBuilder().setMessage("OK").build()),
                responseObserver::onError,
                responseObserver::onCompleted);
    }
}
