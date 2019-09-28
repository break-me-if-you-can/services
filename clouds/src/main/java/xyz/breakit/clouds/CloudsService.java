package xyz.breakit.clouds;

import io.grpc.stub.StreamObserver;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceImplBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation of clouds service.
 * Generates lines with clouds.
 */
final class CloudsService extends CloudsServiceImplBase {

    private static final int MAX_CLOUDS_COUNT = 1;
    private static final int DEFAULT_CLOUD_WIDTH = 1;
    private final Random random;

    CloudsService(Random random) {
        this.random = random;
    }

    @Override
    public void getClouds(GetCloudsRequest request,
                          StreamObserver<CloudsResponse> responseObserver) {
        CloudsResponse.Builder response = CloudsResponse.newBuilder();
        int cloudWidth = Integer.max(request.getCloudWidth(), DEFAULT_CLOUD_WIDTH);

        IntStream.range(0, request.getLinesCount())
                .mapToObj(i -> generateCloudsLine(request.getLineWidth(), cloudWidth))
                .forEach(response::addLines);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private CloudsLine generateCloudsLine(int lineWidth, int cloudWidth) {

        checkArgument(lineWidth >= cloudWidth,
                "Cloud width cannot exceed line width.");

        return CloudsLine.newBuilder()
                .addAllCloudLocators(generateClouds(lineWidth, cloudWidth))
                .build();
    }

    private Collection<CloudLocator> generateClouds(int lineWidth, int cloudWidth) {
        int cloudsCount = random.nextInt(MAX_CLOUDS_COUNT + 1);
        List<Integer> positions = new ArrayList<>(cloudsCount);

        while (positions.size() < cloudsCount) {
            int nextPosition = random.nextInt(lineWidth - cloudsCount);
            boolean overlap = false;
            for (int existingStart : positions) {
                int existingEnd = existingStart + cloudsCount - 1;
                if (nextPosition >= existingStart && nextPosition <= existingEnd) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                positions.add(nextPosition);
            }
        }
        return positions.stream()
                .map(i -> CloudLocator.newBuilder().setCloudPosition(i).build())
                .collect(Collectors.toList());
    }

}
