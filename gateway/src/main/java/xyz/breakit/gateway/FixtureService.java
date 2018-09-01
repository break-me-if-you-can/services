package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import xyz.breakit.clouds.CloudsLine;
import xyz.breakit.gateway.FixtureServiceGrpc.FixtureServiceImplBase;
import xyz.breakit.geese.GeeseLine;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

/**
 * Implementation of fixture service.
 * Generates lines of geese and clouds.
 * To be replaced with a one that will call Geese and Clouds services.
 */
final class FixtureService extends FixtureServiceImplBase {

    private final Random random;

    FixtureService(Random random) {
        this.random = random;
    }

    @Override
    public void getFixture(GetFixtureRequest request,
                           StreamObserver<FixtureResponse> responseObserver) {

        FixtureResponse.Builder response = FixtureResponse.newBuilder();
        IntStream.range(0, request.getLinesCount())
                .mapToObj(i -> generateLine(request.getLineWidth()))
                .forEach(response::addLines);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private FixtureLine generateLine(int lineWidth) {
        CloudsLine clouds = generateCloudsLine(lineWidth);
        GeeseLine geese = generateGeeseLine(lineWidth);
        return merge(clouds, geese);
    }

    private CloudsLine generateCloudsLine(int lineWidth) {
        return CloudsLine.newBuilder()
                .addAllCloudPositions(generatePositions(lineWidth))
                .build();
    }

    private GeeseLine generateGeeseLine(int lineWidth) {
        return GeeseLine.newBuilder()
                .addAllGeesePositions(generatePositions(lineWidth))
                .build();
    }

    private FixtureLine merge(CloudsLine clouds, GeeseLine geese) {
        return FixtureLine.newBuilder()
                .addAllCloudPositions(clouds.getCloudPositionsList())
                .addAllGoosePositions(geese.getGeesePositionsList())
                .build();
    }

    private Collection<Integer> generatePositions(int lineWidth) {
        int objectsCount = random.nextInt(3);
        return random.ints(objectsCount, 0, lineWidth)
                .boxed()
                .collect(toSet());
    }

}
