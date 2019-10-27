package xyz.breakit.game.gateway;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.sampler.Sampler;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import xyz.breakit.game.gateway.FixtureServiceGrpc.FixtureServiceBlockingStub;
import xyz.breakit.game.leaderboard.*;
import xyz.breakit.game.leaderboard.LeaderboardServiceGrpc.LeaderboardServiceBlockingStub;
import xyz.breakit.game.playerid.GeneratePlayerIdRequest;
import xyz.breakit.game.playerid.GeneratePlayerIdResponse;
import xyz.breakit.game.playerid.PlayerIdServiceGrpc;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Client to call gateway service.
 */
public class GatewayClient {

    public static void main(String[] args) {
        String host = "35.233.196.238";
        Channel channel = ManagedChannelBuilder
                .forAddress(host, 8080)
                .usePlaintext()
                .intercept(grpcTracing(Sampler.ALWAYS_SAMPLE).newClientInterceptor())
                .build();

        callUserIdService(channel);
        callFixtureService(channel);
        callLeaderboardService(channel);
    }

    private static void callUserIdService(Channel channel) {
        PlayerIdServiceGrpc.PlayerIdServiceBlockingStub userIdService =
                PlayerIdServiceGrpc.newBlockingStub(channel);

        GeneratePlayerIdResponse generateUserResponse =
                userIdService.generatePlayerId(GeneratePlayerIdRequest.getDefaultInstance());
        System.out.println("GenerateUserResponse: " + generateUserResponse);
    }

    private static void callFixtureService(Channel channel) {
        FixtureServiceBlockingStub client = FixtureServiceGrpc.newBlockingStub(channel);

        GetFixtureRequest fixtureRequest = GetFixtureRequest.newBuilder()
                .setLinesCount(100)
                .setLineWidth(100)
                .setCloudWidth(10)
                .setGooseWidth(10)
                .build();

        FixtureResponse fixture = client.getFixture(fixtureRequest);
        System.out.println("FixtureResponse: " + fixture);
    }

    private static void callLeaderboardService(Channel channel) {
        LeaderboardServiceBlockingStub client = LeaderboardServiceGrpc.newBlockingStub(channel);

        client.updateScore(UpdateScoreRequest.newBuilder()
                .setPlayerScore(PlayerScore.newBuilder().setPlayerId("MADMAX")
                        .setScore(751300).build()).build());
        client.updateScore(UpdateScoreRequest.newBuilder()
                .setPlayerScore(PlayerScore.newBuilder().setPlayerId("DUSTIN")
                        .setScore(650990).build()).build());

        TopScoresRequest topScoresRequest = TopScoresRequest.getDefaultInstance();
        TopScoresResponse topScores = client.getTopScores(topScoresRequest);
        System.out.println("TopScoresResponse: " + topScores);
    }

    private static GrpcTracing grpcTracing(Sampler sampler) {

        String zipkinHost = System.getenv().getOrDefault("ZIPKIN_SERVICE_HOST", "zipkin");
        int zipkinPort = Integer.valueOf(System.getenv().getOrDefault("ZIPKIN_SERVICE_PORT", "9411"));

        URLConnectionSender sender = URLConnectionSender.newBuilder()
                .endpoint(String.format("http://%s:%s/api/v2/spans", zipkinHost, zipkinPort))
                .build();

        return GrpcTracing.create(Tracing.newBuilder()
                .sampler(sampler)
                .spanReporter(AsyncReporter.create(sender))
                .build());
    }


}
