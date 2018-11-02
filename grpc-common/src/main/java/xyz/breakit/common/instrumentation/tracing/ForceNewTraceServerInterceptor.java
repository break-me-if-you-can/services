package xyz.breakit.common.instrumentation.tracing;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * {@link io.grpc.ServerInterceptor} that forces new trace regardless of trace propagation.
 * It removes trace propagation headers.
 * Should be added before tracing interceptors.
 */
public final class ForceNewTraceServerInterceptor implements ServerInterceptor {

    private static final Metadata.Key<byte[]> GRPC_TRACE_BIN =
            Metadata.Key.of("grpc-trace-bin", Metadata.BINARY_BYTE_MARSHALLER);
    private static final Metadata.Key<byte[]> GRPC_TAGS_BIN =
            Metadata.Key.of("grpc-tags-bin", Metadata.BINARY_BYTE_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        metadata.removeAll(GRPC_TRACE_BIN);
        metadata.removeAll(GRPC_TAGS_BIN);

        return serverCallHandler.startCall(serverCall, metadata);
    }
}
