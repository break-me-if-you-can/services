package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import xyz.breakit.gateway.FixtureServiceGrpc.FixtureServiceImplBase;

/**
 * Implementation of fixture service.
 */
public class FixtureService extends FixtureServiceImplBase {

    @Override
    public void getFixtureLine(GetFixtureLineRequest request, StreamObserver<FixtureLineResponse> responseObserver) {

        FixtureLineResponse response = FixtureLineResponse.newBuilder()
                .addGoosePositions(0)
                .addGoosePositions(4)
                .addCloudPositions(1)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
