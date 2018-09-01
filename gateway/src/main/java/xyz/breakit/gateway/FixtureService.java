package xyz.breakit.gateway;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;
import xyz.breakit.clouds.CloudsLine;
import xyz.breakit.clouds.CloudsResponse;
import xyz.breakit.clouds.CloudsServiceGrpc.CloudsServiceFutureStub;
import xyz.breakit.clouds.GetCloudsRequest;
import xyz.breakit.gateway.FixtureServiceGrpc.FixtureServiceImplBase;
import xyz.breakit.geese.GeeseLine;
import xyz.breakit.geese.GeeseResponse;
import xyz.breakit.geese.GeeseServiceGrpc.GeeseServiceFutureStub;
import xyz.breakit.geese.GetGeeseRequest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

/**
 * Implementation of fixture service.
 * Requests cloud and goose lines from remote services and merges their responses
 * into a fixture response.
 */
final class FixtureService extends FixtureServiceImplBase {

    private final GeeseServiceFutureStub geeseClient;
    private final CloudsServiceFutureStub cloudsClient;

    FixtureService(GeeseServiceFutureStub geeseClient,
                   CloudsServiceFutureStub cloudsClient) {
        this.geeseClient = geeseClient;
        this.cloudsClient = cloudsClient;
    }

    @Override
    public void getFixture(GetFixtureRequest request,
                           StreamObserver<FixtureResponse> responseObserver) {

        int requestedLinesCount = request.getLinesCount();
        int requestedLineWidth = request.getLineWidth();

        GetGeeseRequest geeseRequest = GetGeeseRequest.newBuilder()
                .setLinesCount(requestedLinesCount)
                .setLineWidth(requestedLineWidth)
                .build();
        ListenableFuture<GeeseResponse> geeseFuture = geeseClient.getGeese(geeseRequest);

        GetCloudsRequest cloudsRequest = GetCloudsRequest.newBuilder()
                .setLinesCount(requestedLinesCount)
                .setLineWidth(requestedLineWidth)
                .build();
        ListenableFuture<CloudsResponse> cloudsFuture = cloudsClient.getClouds(cloudsRequest);

        ListenableFuture<List<GeneratedMessageV3>> futures = Futures.allAsList(geeseFuture, cloudsFuture);

        Futures.addCallback(futures, new FutureCallback<List<GeneratedMessageV3>>() {
                    @Override
                    public void onSuccess(@Nullable List<GeneratedMessageV3> responses) {

                        GeeseResponse geese = (GeeseResponse) responses.get(0);
                        CloudsResponse clouds = (CloudsResponse) responses.get(1);

                        responseObserver.onNext(merge(geese, clouds, requestedLinesCount));
                        responseObserver.onCompleted();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        responseObserver.onError(throwable);
                    }
                },
                directExecutor());
    }

    private FixtureResponse merge(GeeseResponse geese, CloudsResponse clouds, int requestedLinesCount) {
        FixtureResponse.Builder responseBuilder = FixtureResponse.newBuilder();
        IntStream.range(0, requestedLinesCount)
                .mapToObj(i -> mergeLine(geese.getLines(i), clouds.getLines(i)))
                .forEach(responseBuilder::addLines);
        return responseBuilder.build();
    }

    private FixtureLine mergeLine(GeeseLine geese, CloudsLine clouds) {
        return FixtureLine.newBuilder()
                .addAllCloudPositions(clouds.getCloudPositionsList())
                .addAllGoosePositions(geese.getGeesePositionsList())
                .build();
    }

}
