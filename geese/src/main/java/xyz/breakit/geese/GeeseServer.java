package xyz.breakit.geese;

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
import java.util.Arrays;
import java.util.Random;

/**
 * Geese server entry point.
 */
public class GeeseServer {

    private static final int ZPAGES_PORT = 9080;

    public static void main(String... args) throws IOException, InterruptedException {

        Random random = new Random();
        InjectedFailureProvider failureProvider = new InjectedFailureProvider();
        AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(failureProvider);

        int maxGooseType = Arrays.stream(GooseType.values())
                .filter(g -> !g.equals(GooseType.UNRECOGNIZED))
                .mapToInt(GooseType::getNumber)
                .max().orElse(0);

        GeeseService geeseService =
                new GeeseService(
                        (min, max) -> min + random.nextInt(max - min + 1),
                        random::nextInt,
                        () -> GooseType.forNumber(random.nextInt(maxGooseType)+1),
                        failureProvider);

        Server server = ServerBuilder.forPort(8090)
                .addService(
                        ServerInterceptors.intercept(geeseService, latencyInterceptor))
                .addService(new FailureInjectionAdminService(new FailureInjectionService(failureProvider, failureProvider)))
                .addService(new CommonHealthcheckService("geese", failureProvider, failureProvider))
                .build();
        server.start();

        GrpcCensusReporter.registerAndExportViews(ZPAGES_PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
