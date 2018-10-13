package xyz.breakit.clouds;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import xyz.breakit.common.healthcheck.CommonHealthcheckService;
import xyz.breakit.common.instrumentation.failure.AddLatencyServerInterceptor;
import xyz.breakit.common.instrumentation.failure.FailureInjectionAdminService;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.common.instrumentation.failure.InMemoryAddedLatencyProvider;

import java.io.IOException;
import java.util.Random;

/**
 * Clouds server entry point.
 */
public class CloudsServer {

    public static void main(String... args) throws IOException, InterruptedException {

        InMemoryAddedLatencyProvider latencyProvider = new InMemoryAddedLatencyProvider();
        AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(latencyProvider);
        CloudsService cloudsService = new CloudsService(new Random());

        Server server = ServerBuilder.forPort(8100)
                .addService(
                        ServerInterceptors.intercept(cloudsService, latencyInterceptor))
                .addService(new FailureInjectionAdminService(new FailureInjectionService(latencyProvider)))
                .addService(new CommonHealthcheckService("clouds", latencyProvider))
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
