package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.leaderboard.TopScoresRequest;
import xyz.breakit.leaderboard.TopScoresResponse;

/**
 * Implementation of streaming gateway leaderboard service.
  */
@Service
public class StreamingLeaderboardService extends StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceImplBase {

    private final StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceStub streamingLeaderboardClient;

    @Autowired
    public StreamingLeaderboardService(StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceStub streamingLeaderboardClient) {
        this.streamingLeaderboardClient = streamingLeaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {

        streamingLeaderboardClient.getTopScores(
                request,
                responseObserver);
    }

}
