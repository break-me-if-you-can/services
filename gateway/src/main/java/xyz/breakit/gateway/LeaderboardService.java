package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.gateway.LeaderboardServiceGrpc.LeaderboardServiceImplBase;
import xyz.breakit.gateway.clients.leaderboard.ImmutableLeaderboardEntry;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardClient;
import xyz.breakit.gateway.clients.leaderboard.LeaderboardEntry;
import xyz.breakit.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceStub;
import xyz.breakit.leaderboard.PlayerScore;
import xyz.breakit.leaderboard.TopScoresRequest;
import xyz.breakit.leaderboard.TopScoresResponse;
import xyz.breakit.leaderboard.UpdateScoreRequest;
import xyz.breakit.leaderboard.UpdateScoreResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of gateway leaderboard service.
 */
@Service
public class LeaderboardService extends LeaderboardServiceImplBase {

    private final LeaderboardServiceStub leaderboardClient;

    @Autowired
    public LeaderboardService(LeaderboardServiceStub leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {
        leaderboardClient.getTopScoresOnce( request, responseObserver);
        responseObserver.onCompleted();
    }

    @Override
    public void updateScore(UpdateScoreRequest request,
                            StreamObserver<UpdateScoreResponse> responseObserver) {
        leaderboardClient.updateScore(request, responseObserver);
        responseObserver.onCompleted();
    }
}
