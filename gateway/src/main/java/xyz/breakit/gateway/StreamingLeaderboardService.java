package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.leaderboard.LeaderboardServiceGrpc;
import xyz.breakit.leaderboard.TopScoresRequest;
import xyz.breakit.leaderboard.TopScoresResponse;

/**
 * Implementation of streaming gateway leaderboard service.
  */
@Service
public class StreamingLeaderboardService extends StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceImplBase {

    private final LeaderboardServiceGrpc.LeaderboardServiceStub leaderboardClient;

    @Autowired
    public StreamingLeaderboardService(LeaderboardServiceGrpc.LeaderboardServiceStub leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {

        leaderboardClient.getTopScoresStream(
                request,
                responseObserver);
    }

}
