package xyz.breakit.common.instrumentation.failure;

import io.grpc.stub.StreamObserver;
import xyz.breakit.admin.AdminServiceGrpc.AdminServiceImplBase;
import xyz.breakit.admin.InjectFailureRequest;
import xyz.breakit.admin.InjectFailureResponse;

/**
 * Generic failure injection service.
 */
public class FailureInjectionAdminService extends AdminServiceImplBase {

    private final FailureInjectionService failureInjection;

    public FailureInjectionAdminService(FailureInjectionService failureInjection) {
        this.failureInjection = failureInjection;
    }

    @Override
    public void injectFailure(InjectFailureRequest request,
                              StreamObserver<InjectFailureResponse> responseObserver) {

        failureInjection.injectFailure(request);
        responseObserver.onNext(InjectFailureResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
