package xyz.breakit.geese;

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
 * Geese server entry point.
 */
public class GeeseServer {

    public static void main(String... args) throws IOException, InterruptedException {

        Random random = new Random();
        InMemoryAddedLatencyProvider latencyProvider = new InMemoryAddedLatencyProvider();
        AddLatencyServerInterceptor latencyInterceptor = new AddLatencyServerInterceptor(latencyProvider);
        GeeseService geeseService =
                new GeeseService((min, max) -> min + random.nextInt(max - min + 1),
                        random::nextInt);

        Server server = ServerBuilder.forPort(8090)
                .addService(
                        ServerInterceptors.intercept(geeseService, latencyInterceptor))
                .addService(new FailureInjectionAdminService(new FailureInjectionService(latencyProvider)))
                .addService(new CommonHealthcheckService("geese", latencyProvider))
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
