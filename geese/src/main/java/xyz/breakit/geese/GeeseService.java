package xyz.breakit.geese;

import com.google.common.collect.ImmutableList;
import io.grpc.stub.StreamObserver;
import xyz.breakit.common.instrumentation.failure.FixtureFailureProvider;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceImplBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation of geese service.
 * Generates lines with geese.
 */
final class GeeseService extends GeeseServiceImplBase {

    private static final int MIN_GEESE_COUNT = 2;
    private static final int MAX_GEESE_COUNT = 3;

    private static final int DEFAULT_GOOSE_WIDTH = 1;
    private final BinaryOperator<Integer> numberOfGeeseGenerator;
    private final UnaryOperator<Integer> geeseGenerator;
    private final Supplier<GooseType> geeseTypeGenerator;
    private final FixtureFailureProvider fixtureFailureProvider;

    GeeseService(BinaryOperator<Integer> numberOfGeeseGenerator,
                 UnaryOperator<Integer> geeseGenerator,
                 Supplier<GooseType> geeseTypeGenerator,
                 FixtureFailureProvider fixtureFailureProvider) {
        this.numberOfGeeseGenerator = numberOfGeeseGenerator;
        this.geeseGenerator = geeseGenerator;
        this.geeseTypeGenerator = geeseTypeGenerator;
        this.fixtureFailureProvider = fixtureFailureProvider;
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

        Collection<Integer> geesePositions = generateGeese((int) lineWidth, (int) gooseWidth);
        Collection<GooseType> geeseTypes = generateGeeseTypes(geesePositions.size());
        return GeeseLine.newBuilder()
                .addAllGeesePositions(geesePositions)
                .addAllGeeseTypes(geeseTypes)
                .build();
    }

    private Collection<GooseType> generateGeeseTypes(int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> geeseTypeGenerator.get())
                .collect(Collectors.toList());
    }

    private Collection<Integer> generateGeese(int lineWidth, int gooseWidth) {

        Collection<Integer> geese;
        if (fixtureFailureProvider.isFullFixtureEnabled()) {
            geese = fullLineGeese(lineWidth, gooseWidth);
        } else {
            geese = geese(lineWidth, gooseWidth);
        }
        return geese;
    }

    private Collection<Integer> fullLineGeese(int lineWidth, int gooseWidth) {

        ImmutableList.Builder<Integer> line = ImmutableList.builder();
        int numberOfGeese = lineWidth / gooseWidth;

        IntStream.range(0, numberOfGeese + 1)
                .map(index -> index * gooseWidth)
                .forEach(line::add);

        return line.build();
    }

    private Collection<Integer> geese(int lineWidth, int gooseWidth) {
        int geeseCount = numberOfGeeseGenerator.apply(MIN_GEESE_COUNT, MAX_GEESE_COUNT);
        List<Integer> positions = new ArrayList<>(geeseCount);

        while (positions.size() < geeseCount) {
            int nextPosition = geeseGenerator.apply(lineWidth - gooseWidth);
            boolean overlap = false;
            for (int existingStart : positions) {
                int existingEnd = existingStart + gooseWidth - 1;
                if (overlap(nextPosition, existingStart, existingEnd)) {
                    overlap = true;
                    break;
                }

                if (overlap(nextPosition, existingStart - gooseWidth + 1, existingStart)) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) {
                positions.add(nextPosition);
            }
        }
        return positions;
    }

    private boolean overlap(int candidate, int existingStart, int existingEnd) {
        return candidate >= existingStart && candidate <= existingEnd;
    }

}
