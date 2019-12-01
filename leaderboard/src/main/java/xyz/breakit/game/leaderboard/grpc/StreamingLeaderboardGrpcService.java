package xyz.breakit.game.leaderboard.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.game.leaderboard.PlayerScore;
import xyz.breakit.game.leaderboard.StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceImplBase;
import xyz.breakit.game.leaderboard.TopScoresRequest;
import xyz.breakit.game.leaderboard.TopScoresResponse;
import xyz.breakit.game.leaderboard.service.LeaderboardService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StreamingLeaderboardGrpcService extends StreamingLeaderboardServiceImplBase {

    private final LeaderboardService leaderboardService;

    @Autowired
    public StreamingLeaderboardGrpcService(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void streamTopScores(TopScoresRequest request, StreamObserver<TopScoresResponse> responseObserver) {
        responseObserver.onNext(TopScoresResponse.newBuilder()
                .addAllTopScores(topScores(request))
                .build());

        leaderboardService.getLeaderboardUpdatesFlux()
                .map(updated -> topScores(request))
                .doFinally(signalType -> responseObserver.onCompleted())
                .subscribe(
                        scores -> responseObserver.onNext(
                                TopScoresResponse.newBuilder()
                                        .addAllTopScores(scores)
                                        .build())
                );
    }

    private List<PlayerScore> topScores(TopScoresRequest request) {
        return leaderboardService.getTopScores(request.getSize())
                .stream()
                .map(score -> PlayerScore.newBuilder()
                        .setPlayerId(score.name())
                        .setScore(score.score())
                        .build())
                .collect(Collectors.toList());
    }

}
