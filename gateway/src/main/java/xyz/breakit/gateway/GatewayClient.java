package xyz.breakit.gateway;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import xyz.breakit.gateway.FixtureServiceGrpc.FixtureServiceBlockingStub;
import xyz.breakit.gateway.LeaderboardServiceGrpc.LeaderboardServiceBlockingStub;

/**
 * Client to call gateway service.
 */
public class GatewayClient {

    public static void main(String[] args) {
        String host = "35.230.13.179";
        Channel channel = ManagedChannelBuilder
                .forAddress(host, 8080)
                .usePlaintext()
                .build();

        callFixtureService(channel);
        callLeaderboardService(channel);
    }

    private static void callFixtureService(Channel channel) {
        FixtureServiceBlockingStub client = FixtureServiceGrpc.newBlockingStub(channel);

        GetFixtureRequest fixtureRequest = GetFixtureRequest.newBuilder()
                .setLinesCount(10)
                .setLineWidth(10)
                .build();

        FixtureResponse fixture = client.getFixture(fixtureRequest);
        System.out.println("FixtureResponse: " + fixture);
    }

    private static void callLeaderboardService(Channel channel) {
        LeaderboardServiceBlockingStub client = LeaderboardServiceGrpc.newBlockingStub(channel);

        TopScoresRequest topScoresRequest = TopScoresRequest.newBuilder().setSize(3).build();
        TopScoresResponse topScores = client.getTopScores(topScoresRequest);
        System.out.println("TopScoresResponse: " + topScores);
    }


}
