package ar.edu.itba.pod.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingClient {
    private static Logger logger = LoggerFactory.getLogger(BookingClient.class);

    private final static int MAX_YEAR = 365;
    private final static int MIN_YEAR = 1;
    private final static String[] actions = {"attractions", "availability", "book", "confirm", "cancel"};

    private static List<String> rides = new ArrayList<>();

    private static List<String> slots = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g5 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");

        String serverAddress = args[0];
        String actionName = args[1];
        String dayOfYear = args[2];
        String rideName = args[3];
        String visitorId = args[4];
        String bookingSlotFrom = args[5];
        String bookingSlotTo = args[6];

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();


        try {

        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    private static boolean validateParameters(String[] args) {
//    Acá validamos primero que la acción tenga sentido y después vemos los parámetros

        if (Arrays.stream(actions).noneMatch(action -> action.equals(args[1]))) {
            System.out.println("Invalid action for admin-cli");
            return false;
        }
        if (args[1].equals(actions[0])) {

            //TODO imprimir detalle atractions

        } else if (args[1].equals(actions[1])) {
            int day = Integer.getInteger(args[3]);
            if (day < MIN_YEAR || day > MAX_YEAR) {
                System.out.println("Invalid day");
                return false;
            }

            String rideName = args[4];
            if (!rides.contains(rideName)) {
                System.out.println("Ride does not exist");
                return false;
            }

            String slotFrom = args[6];
            String slotTo = args[7];
            if (!slots.contains(slotFrom)) {
                System.out.println("Slot unavailable");
                return false;
            } else if (!slotTo.isEmpty()) {
                if (!slots.contains(slotFrom) && !slots.contains(slotTo)) {
                    System.out.println("Slot range dos not exist");
                    return false;
                }
                //TODO Retornar slots de la atraccion rideName en el rango

            }

            else if (rideName.isEmpty()){
                if (!slots.contains(slotFrom) && !slots.contains(slotTo)) {
                    System.out.println("Slot range dos not exist");
                    return false;
                }
                //TODO Retornar slots de todas las atracciones en el rango
            }

            return true;
        }
        return false;
    }
}
