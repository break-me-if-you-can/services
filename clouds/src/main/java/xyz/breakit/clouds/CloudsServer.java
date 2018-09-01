package xyz.breakit.clouds;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Random;

/**
 * Clouds server entry point.
 */
public class CloudsServer {

    public static void main(String... args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8100)
                .addService(new CloudsService(new Random()))
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
