package xyz.breakit.geese;

import io.grpc.stub.StreamObserver;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceImplBase;

import java.util.Collection;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

/**
 * Implementation of geese service.
 * Generates lines with geese.
 */
final class GeeseService extends GeeseServiceImplBase {

    private final Random random;

    GeeseService(Random random) {
        this.random = random;
    }

    @Override
    public void getGeese(GetGeeseRequest request,
                         StreamObserver<GeeseResponse> responseObserver) {

        GeeseResponse.Builder response = GeeseResponse.newBuilder();
        IntStream.range(0, request.getLinesCount())
                .mapToObj(i -> generateGeeseLine(request.getLineWidth()))
                .forEach(response::addLines);

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private GeeseLine generateGeeseLine(int lineWidth) {
        return GeeseLine.newBuilder()
                .addAllGeesePositions(generateGeese(lineWidth))
                .build();
    }

    private Collection<Integer> generateGeese(int lineWidth) {
        int objectsCount = random.nextInt(3);
        return random.ints(objectsCount, 0, lineWidth)
                .boxed()
                .collect(toSet());
    }

}
