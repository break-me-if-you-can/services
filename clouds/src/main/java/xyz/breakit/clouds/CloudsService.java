package xyz.breakit.clouds;

import io.grpc.stub.StreamObserver;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceImplBase;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

/**
 * Implementation of clouds service.
 * Generates lines with clouds.
 */
final class CloudsService extends CloudsServiceImplBase {

    private final Random random;

    CloudsService(Random random) {
        this.random = random;
    }

    @Override
    public void getClouds(GetCloudsRequest request,
                          StreamObserver<CloudsResponse> responseObserver) {
        CloudsResponse.Builder response = CloudsResponse.newBuilder();
        IntStream.range(0, request.getLinesCount())
                .mapToObj(i -> generateCloudsLine(request.getLineWidth()))
                .forEach(response::addLines);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private CloudsLine generateCloudsLine(int lineWidth) {
        return CloudsLine.newBuilder()
                .addAllCloudPositions(generateClouds(lineWidth))
                .build();
    }

    private Collection<Integer> generateClouds(int lineWidth) {
        int objectsCount = random.nextInt(3);
        return random.ints(objectsCount, 0, lineWidth)
                .boxed()
                .collect(toSet());
    }

}
