package xyz.breakit.game.gateway;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceImplBase;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceStub;
import xyz.breakit.game.leaderboard.TopScoresRequest;
import xyz.breakit.game.leaderboard.TopScoresResponse;
import xyz.breakit.game.leaderboard.UpdateScoreRequest;
import xyz.breakit.game.leaderboard.UpdateScoreResponse;

/**
 * Implementation of gateway leaderboard service.
 */
@Service
public class LeaderboardService extends LeaderboardServiceImplBase {

    private static final int DEFAULT_SIZE = 5;
    private final LeaderboardServiceStub leaderboardClient;

    @Autowired
    public LeaderboardService(LeaderboardServiceStub leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {
        if (request.getSize() == 0) {
            request = TopScoresRequest.newBuilder().setSize(DEFAULT_SIZE).build();
        }
        leaderboardClient.getTopScores(request, responseObserver);
    }

    @Override
    public void updateScore(UpdateScoreRequest request,
                            StreamObserver<UpdateScoreResponse> responseObserver) {
        leaderboardClient.updateScore(request, responseObserver);
    }
}
