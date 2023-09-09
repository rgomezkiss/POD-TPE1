package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.interceptor.GlobalExceptionHandlerInterceptor;
import ar.edu.itba.pod.server.services.BookingService;
import ar.edu.itba.pod.server.services.ConsultService;
import ar.edu.itba.pod.server.services.NotificationService;
import ar.edu.itba.pod.server.services.ParkAdminService;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);
    private final static Integer port = 50051;

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info("Server Starting ...");

        ParkData parkData = new ParkData();

        io.grpc.Server server = ServerBuilder
                .forPort(port)
                .addService(ServerInterceptors.intercept(new ParkAdminService(parkData), new GlobalExceptionHandlerInterceptor()))
                .addService(new ConsultService(parkData))
                .addService(new BookingService(parkData))
                .addService(new NotificationService(parkData))
                .build();

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
