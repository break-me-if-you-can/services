package xyz.breakit.geese;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Random;

/**
 * Geese server entry point.
 */
public class GeeseServer {

    public static void main(String... args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8090)
                .addService(new GeeseService(new Random()))
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
