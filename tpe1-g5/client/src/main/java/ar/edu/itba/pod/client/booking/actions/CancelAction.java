package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.grpc.booking.BookResponse;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelAction implements Action {

    private final static Logger logger = LoggerFactory.getLogger(CancelAction.class);

    @Override
    public void execute(final AbstractParams params, final ManagedChannel channel) {
        final BookingParams bookingParams = (BookingParams) params;
        final BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);

        try {
            blockingStub.cancelBooking(BookRequest.newBuilder()
                    .setUUID(bookingParams.getVisitorId())
                    .setAttractionName(bookingParams.getRide())
                    .setDay(bookingParams.getDay())
                    .setTimeSlot(bookingParams.getSlot())
                    .build());
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        }
    }
}
