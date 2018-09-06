package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import xyz.breakit.gateway.LeaderboardServiceGrpc.LeaderboardServiceImplBase;
import xyz.breakit.gateway.clients.leaderboard.ImmutableLeaderboardEntry;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardClient;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardEntry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of gateway leaderboard service.
 * Currently returns predefined responses, but should make
 * remote calls to leaderboard service instead.
 */
public class LeaderboardService extends LeaderboardServiceImplBase {

    private final LeaderboardClient leaderboardClient;

    public LeaderboardService(LeaderboardClient leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {

        try {
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
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateScore(UpdateScoreRequest request,
                            StreamObserver<UpdateScoreResponse> responseObserver) {

        LeaderboardEntry entry = ImmutableLeaderboardEntry
                .builder()
                .name(request.getPlayerScore().getPlayerId())
                .score(request.getPlayerScore().getScore())
                .build();

        try {
            leaderboardClient.updateScore(entry);
            responseObserver.onNext(UpdateScoreResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }
}
