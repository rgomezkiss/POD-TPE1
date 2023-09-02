package ar.edu.itba.pod.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class AdminClient {
    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class);

    private final static String[] actions = {"rides", "tickets", "slots"};

    private final static int MAX_YEAR = 365;
    private final static int MIN_YEAR = 1;

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g5 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");

        if (validateParameters(args)){
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
// Me comunico con el servidor e imprimo la respuesta
            } finally {
                channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            }
        }
    }

    private static boolean validateParameters(String[] args) {
        if(Arrays.stream(actions).noneMatch(action -> action.equals(args[1]))){
            System.out.println("Invalid action for admin-cli");
            return false;
        }

        if (args[1].equals(actions[2])) {
            int day = Integer.getInteger(args[4]);
            if (day < MIN_YEAR || day > MAX_YEAR) {
                System.out.println("Invalid day");
                return false;
            }
            int capacity = Integer.getInteger(args[5]);
            if (capacity < 0) {
                System.out.println("Invalid capacity");
                return false;
            }
            return true;
        }

        Path path = Paths.get(args[2]);

        if (path.toFile().exists()) {
            //Parsear el archivo según el comando
            return true;
        }
        else {
            System.out.println("Invalid filepath");
            return false;
        }
    }
}