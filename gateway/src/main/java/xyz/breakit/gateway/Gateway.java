package xyz.breakit.gateway;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

/**
 * Gateway service entry point.
 */
public class Gateway {

    public static void main(String... args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080).addService(new FixtureService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
