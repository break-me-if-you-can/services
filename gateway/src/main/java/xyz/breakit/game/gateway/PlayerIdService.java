package xyz.breakit.game.gateway;

import io.grpc.Context;
import io.grpc.Deadline;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.breakit.game.playerid.GeneratePlayerIdRequest;
import xyz.breakit.game.playerid.GeneratePlayerIdResponse;
import xyz.breakit.game.playerid.PlayerIdServiceGrpc.PlayerIdServiceImplBase;
import xyz.breakit.game.playerid.PlayerIdServiceGrpc.PlayerIdServiceStub;

import java.util.concurrent.TimeUnit;

/**
 * Calls remote Player ID service implementation to generate player ID.
 */
final class PlayerIdService extends PlayerIdServiceImplBase {

    private final Logger logger = LoggerFactory.getLogger(PlayerIdService.class);

    private final PlayerIdServiceStub remoteClient;

    PlayerIdService(PlayerIdServiceStub remoteClient) {
        this.remoteClient = remoteClient;
    }

    @Override
    public void generatePlayerId(GeneratePlayerIdRequest request,
                                 StreamObserver<GeneratePlayerIdResponse> responseObserver) {

        getRemoteClient().generatePlayerId(request,
                new StreamObserver<GeneratePlayerIdResponse>() {
                    @Override
                    public void onNext(GeneratePlayerIdResponse value) {
                        responseObserver.onNext(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseObserver.onError(t);
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onCompleted();
                    }
                });
    }

    private PlayerIdServiceStub getRemoteClient() {
        PlayerIdServiceStub client = remoteClient;

        Deadline deadline = Context.current().getDeadline();
        logger.trace("Deadline {}.", deadline);
        if (deadline != null) {
            // adjust for network latency
            client = client.withDeadline(deadline.offset(-200, TimeUnit.MILLISECONDS));
        }
        return client;
    }
}
