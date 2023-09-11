package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import ar.edu.itba.pod.grpc.booking.GetAttractionsResponse;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttractionsAction implements Action<BookingParams> {

    private final static Logger logger = LoggerFactory.getLogger(AttractionsAction.class);

    @Override
    public void execute(final BookingParams params, final ManagedChannel channel) {
        final BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);
        final GetAttractionsResponse attractionsResponse = blockingStub.getAttractions(Empty.newBuilder().build());

        System.out.println("Attraction | Open time | Close time");
        attractionsResponse.getAttractionsList().forEach((attractionResponse) ->
                System.out.printf("%s %s %s%n",
                        attractionResponse.getAttractionName(),
                        attractionResponse.getOpeningTime(),
                        attractionResponse.getClosingTime()
                )
        );
    }
}
