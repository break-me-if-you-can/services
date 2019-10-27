package xyz.breakit.game.gateway.admin;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.AdminServiceGrpc.AdminServiceImplBase;
import xyz.breakit.admin.AdminServiceGrpc.AdminServiceStub;
import xyz.breakit.admin.InjectFailureRequest;
import xyz.breakit.admin.InjectFailureResponse;
import xyz.breakit.admin.PartialDegradationRequest;
import xyz.breakit.admin.PartialDegradationResponse;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.game.gateway.flags.SettableFlags;

/**
 * Implements gateway admin service.
 */
public class GatewayAdminService extends AdminServiceImplBase {

    private final SettableFlags flags;
    private final AdminServiceStub geeseAdmin;
    private final AdminServiceStub cloudsAdmin;
    private final AdminServiceStub leaderboardAdmin;
    private final AdminServiceStub playerIdAdmin;
    private final FailureInjectionService failureInjectionService;

    public GatewayAdminService(SettableFlags flags,
                               AdminServiceStub geeseAdmin,
                               AdminServiceStub cloudsAdmin,
                               AdminServiceStub leaderboardAdmin,
                               AdminServiceStub playerIdAdmin,
                               FailureInjectionService failureInjectionService) {
        this.flags = flags;
        this.geeseAdmin = geeseAdmin;
        this.cloudsAdmin = cloudsAdmin;
        this.leaderboardAdmin = leaderboardAdmin;
        this.playerIdAdmin = playerIdAdmin;
        this.failureInjectionService = failureInjectionService;
    }

    @Override
    public void managePartialDegradation(PartialDegradationRequest request,
                                         StreamObserver<PartialDegradationResponse> responseObserver) {
        flags.setPartialDegradationEnabled(request.getEnable());

        PartialDegradationResponse response = PartialDegradationResponse.newBuilder()
                .setEnabled(flags.isPartialDegradationEnabled()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void injectFailure(InjectFailureRequest request,
                              StreamObserver<InjectFailureResponse> responseObserver) {
        if (ServiceNames.GEESE_SERVICE.equalsIgnoreCase(request.getServiceName())) {
            geeseAdmin.injectFailure(request, responseObserver);
        } else if (ServiceNames.CLOUDS_SERVICE.equalsIgnoreCase(request.getServiceName())) {
            cloudsAdmin.injectFailure(request, responseObserver);
        } else if (ServiceNames.LEADERBOARD_SERVICE.equalsIgnoreCase(request.getServiceName())) {
            leaderboardAdmin.injectFailure(request, responseObserver);
        } else if (ServiceNames.PLAYER_ID_SERVICE.equalsIgnoreCase(request.getServiceName())) {
            playerIdAdmin.injectFailure(request, responseObserver);
        }else if (ServiceNames.GATEWAY_SERVICE.equalsIgnoreCase(request.getServiceName())) {
            failureInjectionService.injectFailure(request);
            responseObserver.onNext(InjectFailureResponse.newBuilder().build());
            responseObserver.onCompleted();
        } else {
            StatusRuntimeException invalidArgumentException =
                    Status.INVALID_ARGUMENT
                            .withDescription(
                                    String.format(
                                            "Failure injection is not supported for '%s' service.",
                                            request.getServiceName()))
                            .asRuntimeException();
            responseObserver.onError(invalidArgumentException);
        }
    }

}
