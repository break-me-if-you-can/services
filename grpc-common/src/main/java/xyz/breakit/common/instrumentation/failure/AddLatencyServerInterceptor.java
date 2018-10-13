package xyz.breakit.common.instrumentation.failure;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import java.util.Random;

/**
 * {@link ServerInterceptor} to add configurable latency to server calls.
 */
public final class AddLatencyServerInterceptor implements ServerInterceptor {

    private final AddedLatencyProvider addedLatencyProvider;

    private final Random random = new Random();

    /**
     * Created {@link AddLatencyServerInterceptor} will add {@link AddedLatencyProvider#duration()}
     * latency to a server call with a probability returned by
     * {@link AddedLatencyProvider#latencyProbability()} ()}.
     *
     * @param addedLatencyProvider provides probability and duration of adding latency to server call.
     */
    public AddLatencyServerInterceptor(AddedLatencyProvider addedLatencyProvider) {
        this.addedLatencyProvider = addedLatencyProvider;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        addLatencyIfEnabled();
        return next.startCall(call, headers);
    }

    private void addLatencyIfEnabled() {
        double probability = addedLatencyProvider.latencyProbability();
        double next = random.nextDouble();

        if (Double.compare(probability, next) > 0) {
            try {
                Thread.sleep(addedLatencyProvider.duration().toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
