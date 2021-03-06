package xyz.breakit.gateway;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import brave.sampler.Sampler;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import xyz.breakit.gateway.FixtureServiceGrpc.FixtureServiceBlockingStub;
import xyz.breakit.gateway.LeaderboardServiceGrpc.LeaderboardServiceBlockingStub;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/**
 * Client to call gateway service.
 */
public class GatewayClient {

    public static void main(String[] args) {
        String host = "35.233.196.238";
        Channel channel = ManagedChannelBuilder
                .forAddress(host, 80)
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
                .setPlayerScore(PlayerScore.newBuilder().setPlayerId("Trololo_100500").setScore(111111).build()).build());

        TopScoresRequest topScoresRequest = TopScoresRequest.newBuilder().setSize(3).build();
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
