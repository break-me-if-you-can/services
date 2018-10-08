package xyz.breakit.geese;

import com.google.common.collect.ImmutableList;
import io.grpc.stub.StreamObserver;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceImplBase;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation of geese service.
 * Generates lines with geese.
 */
final class GeeseService extends GeeseServiceImplBase {

    private static final int MAX_GEESE_COUNT = 2;
    private static final int DEFAULT_GOOSE_WIDTH = 1;
    private final Random random;

    GeeseService(Random random) {
        this.random = random;
    }

    @Override
    public void getGeese(GetGeeseRequest request,
                         StreamObserver<GeeseResponse> responseObserver) {

        GeeseResponse.Builder response = GeeseResponse.newBuilder();
        int gooseWidth = Integer.max(request.getGooseWidth(), DEFAULT_GOOSE_WIDTH);
        IntStream.range(0, request.getLinesCount())
                .mapToObj(i -> generateGeeseLine(request.getLineWidth(), gooseWidth))
                .forEach(response::addLines);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private GeeseLine generateGeeseLine(int lineWidth, int gooseWidth) {

        checkArgument(lineWidth >= gooseWidth,
                "Goose width cannot exceed line width.");

        return GeeseLine.newBuilder()
                .addAllGeesePositions(generateGeese(lineWidth, gooseWidth))
                .build();
    }

    private Collection<Integer> generateGeese(int lineWidth, int gooseWidth) {
        ImmutableList.Builder<Integer> resultBuilder = ImmutableList.builder();

        int lastPosition = 0;
        for (int i = 0; i < MAX_GEESE_COUNT; i++) {
            int spaceLeft = lineWidth - gooseWidth - lastPosition;
            if (spaceLeft > 0) {
                int nextPosition = lastPosition + random.nextInt(spaceLeft);
                resultBuilder.add(nextPosition);
                lastPosition = nextPosition + gooseWidth;
            } else {
                break;
            }
        }
        return resultBuilder.build();
    }

}
