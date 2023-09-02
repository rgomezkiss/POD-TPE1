package ar.edu.itba.pod.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AdminClient {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private final static String[] actions = {"rides", "tickets", "slots"};

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g5 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");

        String serverAddress = args[0];
        String actionName = args[1];
        String fileName = args[2];
        String rideName = args[3];
        String dayOfYear = args[4];
        String capacity = args[5];

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();


        try {

        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private static boolean validateParameters() {
//    Acá validamos primero que la acción tenga sentido y después vemos los parámetros
    }
}
