package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.Servants.ParkAdminService;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);
    private final static Integer port = 50051;

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info("Server Starting ...");

        ParkData parkData = new ParkData();

        io.grpc.Server server = ServerBuilder.forPort(port).addService(new ParkAdminService(parkData)).build();
        server.start();
        logger.info("Server started, listening on " + port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }
}
