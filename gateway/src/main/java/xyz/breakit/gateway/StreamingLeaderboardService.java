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

    private final StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceStub leaderboardClient;

    @Autowired
    public StreamingLeaderboardService(StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceStub leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    public void getTopScores(TopScoresRequest request,
                             StreamObserver<TopScoresResponse> responseObserver) {

        leaderboardClient.getTopScores(
                request,
                new StreamObserver<TopScoresResponse>() {
                    @Override
                    public void onNext(TopScoresResponse value) {
                        responseObserver.onNext(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseObserver.onError(t);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                });
    }

}
