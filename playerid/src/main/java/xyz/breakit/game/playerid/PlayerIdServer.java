package xyz.breakit.game.playerid;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;
import xyz.breakit.common.healthcheck.CommonHealthcheckService;
import xyz.breakit.common.instrumentation.failure.AddLatencyServerInterceptor;
import xyz.breakit.common.instrumentation.failure.FailureInjectionAdminService;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;

import java.io.IOException;

/**
 * Player ID server entry point.
 */
public class PlayerIdServer {

    public static void main(String... args) throws IOException, InterruptedException {

        InjectedFailureProvider failureProvider = new InjectedFailureProvider();
        AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(failureProvider);

        Server server = ServerBuilder.forPort(8110)
                .addService(
                        ServerInterceptors.intercept(new PlayerIdService(), latencyInterceptor))
                .addService(new FailureInjectionAdminService(new FailureInjectionService(failureProvider, failureProvider)))
                .addService(new CommonHealthcheckService("playerid", failureProvider, failureProvider))
                .addService(ProtoReflectionService.newInstance())
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
