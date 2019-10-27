package xyz.breakit.game.leaderboard.grpc;

import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import xyz.breakit.game.leaderboard.*;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceBlockingStub;
import xyz.breakit.game.leaderboard.StreamingLeaderboardServiceGrpc.StreamingLeaderboardServiceBlockingStub;
import xyz.breakit.game.leaderboard.service.LeaderboardService;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamingLeaderboardGrpcServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private LeaderboardService leaderboardService = new LeaderboardService(10);
    private LeaderboardGrpcService leaderboardGrpcService = new LeaderboardGrpcService(leaderboardService);
    private StreamingLeaderboardGrpcService streamingLeaderboardGrpcService = new StreamingLeaderboardGrpcService(leaderboardService);

    private LeaderboardServiceBlockingStub blockingGrpcClient;
    private StreamingLeaderboardServiceBlockingStub streamingBlockingGrpcClient;
    private Server grpcServer;

    @Before
    public void setupGrpcServer() throws IOException {

        String serverName = InProcessServerBuilder.generateName();
        grpcServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(leaderboardGrpcService)
                .addService(streamingLeaderboardGrpcService)
                .build()
                .start();

        grpcCleanup.register(grpcServer);

        blockingGrpcClient = LeaderboardServiceGrpc.newBlockingStub(
                grpcCleanup.register(
                        InProcessChannelBuilder.forName(serverName)
                                .directExecutor()
                                .build()));
        streamingBlockingGrpcClient = StreamingLeaderboardServiceGrpc.newBlockingStub(
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
        Iterator<TopScoresResponse> topScores = streamingBlockingGrpcClient.streamTopScores(TopScoresRequest.newBuilder().setSize(3).build());
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