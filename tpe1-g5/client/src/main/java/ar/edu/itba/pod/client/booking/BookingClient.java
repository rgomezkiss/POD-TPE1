package ar.edu.itba.pod.client.booking;

import ar.edu.itba.pod.client.booking.actions.*;
import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.booking.utils.BookingParser;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class BookingClient {
    private final static Logger logger = LoggerFactory.getLogger(BookingClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Booking Client Starting ...");

        final BookingParams params = new BookingParser().parse(args);

        if (params == null) {
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder
                .forAddress(params.getServerAddress().getHost(), params.getServerAddress().getPort())
                .usePlaintext()
                .build();

        try {
            switch (BookingActions.valueOf(params.getAction())) {
                case ATTRACTIONS -> new AttractionsAction().execute(params, channel);
                case AVAILABILITY -> new AvailabilityAction().execute(params, channel);
                case BOOK -> new BookAction().execute(params, channel);
                case CONFIRM -> new ConfirmAction().execute(params, channel);
                case CANCEL -> new CancelAction().execute(params, channel);
            }
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
