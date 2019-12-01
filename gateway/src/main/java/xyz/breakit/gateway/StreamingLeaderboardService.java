package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.leaderboard.*;
import xyz.breakit.leaderboard.StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceStub;

/**
 * Implementation of streaming leaderboard service.
 */
@Service
public class StreamingLeaderboardService extends StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceImplBase {

    private static final int DEFAULT_SIZE = 5;
    private final StreamingLeaderboardServiceStub streamingLeaderboardClient;

    @Autowired
    public StreamingLeaderboardService(StreamingLeaderboardServiceStub streamingLeaderboardClient) {
        this.streamingLeaderboardClient = streamingLeaderboardClient;
    }

    @Override
    public void streamTopScores(TopScoresRequest request, StreamObserver<TopScoresResponse> responseObserver) {
        request = TopScoresRequest.newBuilder().setSize(DEFAULT_SIZE).build();
        if (request.getSize() == 0) {
            request = TopScoresRequest.newBuilder().setSize(DEFAULT_SIZE).build();
        }
        streamingLeaderboardClient.streamTopScores(request, responseObserver);
    }
}


