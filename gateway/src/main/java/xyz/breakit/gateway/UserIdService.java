package xyz.breakit.gateway;

import io.grpc.stub.StreamObserver;
import xyz.breakit.gateway.UserIdServiceGrpc.UserIdServiceImplBase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Naive implementation of user id service.
 * Generates user ids based on a counter.
 */
final class UserIdService extends UserIdServiceImplBase {

    private static final String USER_ID_PREFIX = "Pilot_";
    private final AtomicLong idCounter = new AtomicLong();

    @Override
    public void generateUserId(GenerateUserRequest request, StreamObserver<GenerateUserResponse> responseObserver) {
        String userId = USER_ID_PREFIX + idCounter.incrementAndGet();
        responseObserver.onNext(GenerateUserResponse.newBuilder().setUserId(userId).build());
        responseObserver.onCompleted();
    }
}
