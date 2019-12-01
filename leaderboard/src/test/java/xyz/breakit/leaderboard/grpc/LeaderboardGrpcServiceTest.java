package xyz.breakit.leaderboard.grpc;

import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import xyz.breakit.leaderboard.*;
import xyz.breakit.leaderboard.service.LeaderboardService;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class LeaderboardGrpcServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private LeaderboardService leaderboardService = new LeaderboardService();
    private LeaderboardGrpcService leaderboardGrpcService = new LeaderboardGrpcService(leaderboardService);

    private LeaderboardServiceGrpc.LeaderboardServiceBlockingStub blockingGrpcClient;
    private Server grpcServer;

    @Before
    public void setupGrpcServer() throws IOException {

        String serverName = InProcessServerBuilder.generateName();
        grpcServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(leaderboardGrpcService)
                .build()
                .start();

        grpcCleanup.register(grpcServer);

        blockingGrpcClient = LeaderboardServiceGrpc.newBlockingStub(
                grpcCleanup.register(
                        InProcessChannelBuilder.forName(serverName)
                                .directExecutor()
                                .build()));
    }

    @After
    public void cleanup() {
        grpcServer.shutdownNow();
    }

    @Test
    public void testTopScoresStream() {
        Iterator<TopScoresResponse> topScores = blockingGrpcClient.getTopScores(TopScoresRequest.newBuilder().setSize(3).build());
        assertThat(topScores.next()).isEqualTo(
                TopScoresResponse.getDefaultInstance()
        );

        blockingGrpcClient.updateScore(scoreRequest("a", 100));
        assertThat(topScores.next()).isEqualTo(
                TopScoresResponse.newBuilder()
                        .addTopScores(score("a", 100))
                        .build()
        );
        blockingGrpcClient.updateScore(scoreRequest("b", 200));
        assertThat(topScores.next()).isEqualTo(
                TopScoresResponse.newBuilder()
                        .addTopScores(score("b", 200))
                        .addTopScores(score("a", 100))
                        .build()
        );

        blockingGrpcClient.updateScore(scoreRequest("c", 400));
        assertThat(topScores.next()).isEqualTo(
                TopScoresResponse.newBuilder()
                        .addTopScores(score("c", 400))
                        .addTopScores(score("b", 200))
                        .addTopScores(score("a", 100))
                        .build()
        );
    }

    private UpdateScoreRequest scoreRequest(String a, int i) {
        return UpdateScoreRequest.newBuilder().setPlayerScore(score(a, i)).build();
    }

    private PlayerScore score(String a, int i) {
        return PlayerScore.newBuilder().setPlayerId(a).setScore(i).build();
    }
}