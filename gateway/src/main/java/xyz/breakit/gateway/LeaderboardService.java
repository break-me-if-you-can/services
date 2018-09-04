package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import xyz.breakit.gateway.LeaderboardServiceGrpc.LeaderboardServiceImplBase;

/**
 * Implementation of gateway leaderboard service.
 * Currently returns predefined responses, but should make
 * remote calls to leaderboard service instead.
 */
public class LeaderboardService extends LeaderboardServiceImplBase {

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {

        // Mykyta: Make call to leaderboard service here and convert its response to TopScoresResponse
        TopScoresResponse response = TopScoresResponse.newBuilder()
                .addTopScores(PlayerScore.newBuilder().setPlayerId("Petro").setScore(100500).build())
                .addTopScores(PlayerScore.newBuilder().setPlayerId("Vasyl").setScore(321).build())
                .addTopScores(PlayerScore.newBuilder().setPlayerId("Hanna").setScore(42).build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateScore(UpdateScoreRequest request,
                            StreamObserver<UpdateScoreResponse> responseObserver) {
        // Mykyta: Make call to leaderboard service here
        responseObserver.onNext(UpdateScoreResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
