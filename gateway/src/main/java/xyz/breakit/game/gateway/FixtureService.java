package xyz.breakit.game.gateway;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;
import xyz.breakit.clouds.CloudsResponse;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceFutureStub;
import xyz.breakit.clouds.GetCloudsRequest;
import xyz.breakit.game.gateway.FixtureServiceGrpc.FixtureServiceImplBase;
import xyz.breakit.game.gateway.flags.Flags;
import xyz.breakit.geese.*;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceFutureStub;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Implementation of fixture service.
 * Requests cloud and goose lines from remote services and merges their responses
 * into a fixture response.
 */
final class FixtureService extends FixtureServiceImplBase {

    private final Supplier<GeeseServiceFutureStub> geeseClient;
    private final Supplier<CloudsServiceFutureStub> cloudsClient;
    private final Flags flags;

    FixtureService(Supplier<GeeseServiceFutureStub> geeseClient,
                   Supplier<CloudsServiceFutureStub> cloudsClient,
                   Flags flags) {
        this.geeseClient = geeseClient;
        this.cloudsClient = cloudsClient;
        this.flags = flags;
    }

    @Override
    public void getFixture(GetFixtureRequest request,
                           StreamObserver<FixtureResponse> responseObserver) {

        ListenableFuture<GeeseResponse> geeseFuture =
                geeseClient.get().withDeadlineAfter(500, MILLISECONDS).getGeese(toGeeseRequest(request));
        ListenableFuture<CloudsResponse> cloudsFuture =
                cloudsClient.get().withDeadlineAfter(500, MILLISECONDS).getClouds(toCloudsRequest(request));

        ListenableFuture<List<GeneratedMessageV3>> geeseAndClouds = combine(geeseFuture, cloudsFuture);

        Futures.addCallback(geeseAndClouds, new FutureCallback<List<GeneratedMessageV3>>() {
                    @Override
                    public void onSuccess(@Nullable List<GeneratedMessageV3> responses) {

                        GeeseResponse geese = (GeeseResponse) responses.get(0);
                        if (!flags.isDiverseGeeseEnabled()) {
                            geese = convertToCanadaGeeseOnly(geese);
                        }
                        CloudsResponse clouds = (CloudsResponse) responses.get(1);

                        responseObserver.onNext(merge(geese, clouds, request.getLinesCount()));
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }
                },
                directExecutor());
    }

    private GeeseResponse convertToCanadaGeeseOnly(GeeseResponse geese) {
        List<GeeseLine> canadaGeeseLines = geese.getLinesList().stream()
                .map(line -> line.getGeeseLocatorsList().stream()
                        .map(goose -> GooseLocator.newBuilder(goose).setGooseType(GooseType.GOOSE_TYPE_CANADA_GOOSE).build())
                        .collect(Collectors.toList()))
                .map(line -> GeeseLine.newBuilder().addAllGeeseLocators(line).build())
                .collect(Collectors.toList());
        geese = GeeseResponse.newBuilder()
                .addAllLines(canadaGeeseLines)
                .build();
        return geese;
    }

    private ListenableFuture<List<GeneratedMessageV3>> combine(ListenableFuture<GeeseResponse> geeseFuture, ListenableFuture<CloudsResponse> cloudsFuture) {
        ListenableFuture<List<GeneratedMessageV3>> result;
        if (flags.isPartialDegradationEnabled()) {
            result = Futures.successfulAsList(geeseFuture, cloudsFuture);
        } else {
            result = Futures.allAsList(geeseFuture, cloudsFuture);
        }
        return result;
    }

    private GetCloudsRequest toCloudsRequest(GetFixtureRequest request) {
        return GetCloudsRequest.newBuilder()
                .setLinesCount(request.getLinesCount())
                .setLineWidth(request.getLineWidth())
                .setCloudWidth(request.getCloudWidth())
                .build();
    }

    private GetGeeseRequest toGeeseRequest(GetFixtureRequest request) {
        return GetGeeseRequest.newBuilder()
                .setLinesCount(request.getLinesCount())
                .setLineWidth(request.getLineWidth())
                .setGooseWidth(request.getGooseWidth())
                .build();
    }

    private FixtureResponse merge(GeeseResponse geese, CloudsResponse clouds, int requestedLinesCount) {
        FixtureResponse.Builder responseBuilder = FixtureResponse.newBuilder();
        IntStream.range(0, requestedLinesCount)
                .mapToObj(i -> mergeLine(i, geese, clouds))
                .forEach(responseBuilder::addLines);
        return responseBuilder.build();
    }

    private FixtureLine mergeLine(int index, @Nullable GeeseResponse geese, @Nullable CloudsResponse clouds) {
        FixtureLine.Builder builder = FixtureLine.newBuilder();
        if (geese != null) {
            builder.addAllGooseLocators(geese.getLines(index).getGeeseLocatorsList());
        }
        if (clouds != null) {
            builder.addAllCloudLocators(clouds.getLines(index).getCloudLocatorsList());
        }
        return builder.build();
    }

}
