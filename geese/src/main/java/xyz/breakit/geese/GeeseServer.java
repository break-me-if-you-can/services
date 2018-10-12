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
        Random random = new Random();
        Server server = ServerBuilder.forPort(8090)
                .addService(new GeeseService((min, max) -> min + random.nextInt(max - min + 1),
                        random::nextInt))
                .build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.awaitTermination();
    }

}
