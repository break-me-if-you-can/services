package xyz.breakit.game.leaderboard.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.breakit.game.leaderboard.*;
import xyz.breakit.game.leaderboard.service.LeaderboardService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardGrpcService extends LeaderboardServiceGrpc.LeaderboardServiceImplBase {

    private final LeaderboardService leaderboardService;

    @Autowired
    public LeaderboardGrpcService(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @Override
    public void getTopScores(TopScoresRequest request, StreamObserver<TopScoresResponse> responseObserver) {
        responseObserver.onNext(TopScoresResponse.newBuilder()
                .addAllTopScores(topScores(request))
                .build());

        responseObserver.onCompleted();
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

    @Override
    public void updateScore(UpdateScoreRequest request, StreamObserver<UpdateScoreResponse> responseObserver) {
        leaderboardService.recordScore(request.getPlayerScore().getPlayerId(), request.getPlayerScore().getScore());
        responseObserver.onNext(UpdateScoreResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
