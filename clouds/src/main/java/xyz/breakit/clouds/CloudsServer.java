package xyz.breakit.clouds;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import xyz.breakit.common.healthcheck.CommonHealthcheckService;
import xyz.breakit.common.instrumentation.census.GrpcCensusReporter;
import xyz.breakit.common.instrumentation.failure.AddLatencyServerInterceptor;
import xyz.breakit.common.instrumentation.failure.FailureInjectionAdminService;
import xyz.breakit.common.instrumentation.failure.FailureInjectionService;
import xyz.breakit.common.instrumentation.failure.InjectedFailureProvider;

import java.io.IOException;
import java.util.Random;

/**
 * Clouds server entry point.
 */
public class CloudsServer {

    private static final int ZPAGES_PORT = 9080;

    public static void main(String... args) throws IOException, InterruptedException {

        InjectedFailureProvider failureProvider = new InjectedFailureProvider();
        AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(failureProvider);
        CloudsService cloudsService = new CloudsService(new Random());

        Server server = ServerBuilder.forPort(8100)
                .addService(
                        ServerInterceptors.intercept(cloudsService, latencyInterceptor))
                .addService(new FailureInjectionAdminService(new FailureInjectionService(failureProvider, failureProvider)))
                .addService(new CommonHealthcheckService("clouds", failureProvider, failureProvider))
                .build();
        server.start();

        GrpcCensusReporter.registerAndExportViews(ZPAGES_PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
