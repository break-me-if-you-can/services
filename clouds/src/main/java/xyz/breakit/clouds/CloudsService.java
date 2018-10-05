package xyz.breakit.clouds;

import com.google.common.collect.ImmutableList;
import io.grpc.stub.StreamObserver;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceImplBase;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation of clouds service.
 * Generates lines with clouds.
 */
final class CloudsService extends CloudsServiceImplBase {

    private static final int MAX_CLOUDS_COUNT = 2;
    private final Random random;

    CloudsService(Random random) {
        this.random = random;
    }

    @Override
    public void getClouds(GetCloudsRequest request,
                          StreamObserver<CloudsResponse> responseObserver) {
        CloudsResponse.Builder response = CloudsResponse.newBuilder();
        IntStream.range(0, request.getLinesCount())
                .mapToObj(i -> generateCloudsLine(request.getLineWidth(), request.getCloudWidth()))
                .forEach(response::addLines);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private CloudsLine generateCloudsLine(int lineWidth, int cloudWidth) {

        checkArgument(lineWidth >= gooseWidth,
                "Goose width cannot exceed line width.");

        return CloudsLine.newBuilder()
                .addAllCloudPositions(generateClouds(lineWidth, cloudWidth))
                .build();
    }

    private Collection<Integer> generateClouds(int lineWidth, int cloudWidth) {
        ImmutableList.Builder<Integer> resultBuilder = ImmutableList.builder();

        int lastPosition = 0;
        for (int i = 0; i < MAX_CLOUDS_COUNT; i++) {
            int spaceLeft = lineWidth - cloudWidth - lastPosition;
            if (spaceLeft > 0) {
                int nextPosition = lastPosition + random.nextInt(spaceLeft);
                resultBuilder.add(nextPosition);
                lastPosition = nextPosition + cloudWidth;
            } else {
                break;
            }
        }
        return resultBuilder.build();
    }

}
