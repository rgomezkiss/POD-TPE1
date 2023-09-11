package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmAction implements Action<BookingParams> {

    private final static Logger logger = LoggerFactory.getLogger(ConfirmAction.class);

    @Override
    public void execute(final BookingParams params, final ManagedChannel channel) {
        final BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);

        try {
            blockingStub.confirmBooking(BookRequest.newBuilder()
                    .setUUID(params.getVisitorId())
                    .setAttractionName(params.getRide())
                    .setDay(params.getDay())
                    .setTimeSlot(params.getSlot())
                    .build());
        } catch (StatusRuntimeException e) {
            logger.error("{}: {}", e.getStatus().getCode().toString(), e.getMessage());
        }
    }
}