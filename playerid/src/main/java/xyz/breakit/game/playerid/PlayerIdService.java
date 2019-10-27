package xyz.breakit.game.playerid;

import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Naive implementation of user id service.
 * Generates user ids based on a counter.
 */
final class PlayerIdService extends PlayerIdServiceGrpc.PlayerIdServiceImplBase {

    private static final String USER_ID_PREFIX = "Pilot_";
    private final AtomicLong idCounter = new AtomicLong();

    @Override
    public void generatePlayerId(GeneratePlayerIdRequest request,
                                 StreamObserver<GeneratePlayerIdResponse> responseObserver) {
        String userId = USER_ID_PREFIX + idCounter.incrementAndGet();
        responseObserver.onNext(GeneratePlayerIdResponse.newBuilder().setPlayerId(userId).build());
        responseObserver.onCompleted();
    }
}
