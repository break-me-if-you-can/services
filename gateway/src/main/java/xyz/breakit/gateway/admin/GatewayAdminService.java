package xyz.breakit.gateway.admin;

import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.AdminServiceGrpc.AdminServiceImplBase;
import xyz.breakit.admin.PartialDegradationRequest;
import xyz.breakit.admin.PartialDegradationResponse;
import xyz.breakit.gateway.flags.SettableFlags;

/**
 * Implements gateway admin service.
 */
public class GatewayAdminService extends AdminServiceImplBase {

    private final SettableFlags flags;

    public GatewayAdminService(SettableFlags flags) {
        this.flags = flags;
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
}
