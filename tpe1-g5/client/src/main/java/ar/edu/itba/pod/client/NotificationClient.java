package ar.edu.itba.pod.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NotificationClient {
    private final static Logger logger = LoggerFactory.getLogger(NotificationClient.class);

    private final static String[] actions = {"capacity", "confirmed"};
    private final static int MAX_YEAR = 365;
    private final static int MIN_YEAR = 1;

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g5 Notification Client Starting ...");
        logger.info("grpc-com-patterns Notification Client Starting ...");

        final String serverAddress = args[0];
        final String actionName = args[1];
        final String dayOfYear = args[2];
        final String rideName = args[3];
        final String visitorId = args[4];

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

        try {
            // TODO
            // Me comunico con el servidor e imprimo la respuesta
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private static boolean validateParameters() {
        //    Acá validamos primero que la acción tenga sentido y después vemos los parámetros
        return false;
    }
}
