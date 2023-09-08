package ar.edu.itba.pod.client.booking.actions;

import ar.edu.itba.pod.client.booking.utils.BookingParams;
import ar.edu.itba.pod.client.utils.AbstractParams;
import ar.edu.itba.pod.client.utils.Action;
import ar.edu.itba.pod.grpc.booking.BookRequest;
import ar.edu.itba.pod.grpc.booking.BookResponse;
import ar.edu.itba.pod.grpc.booking.BookingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class CancelAction implements Action {
    @Override
    public void execute(AbstractParams params, ManagedChannel channel) {
        BookingParams bookingParams = (BookingParams) params;
        BookingServiceGrpc.BookingServiceBlockingStub blockingStub = BookingServiceGrpc.newBlockingStub(channel);
        try {
            blockingStub.cancelBooking(BookRequest.newBuilder()
                    .setUUID(bookingParams.getVisitorId())
                    .setAttractionName(bookingParams.getRide())
                    .setDay(bookingParams.getDay())
                    .setTimeSlot(bookingParams.getSlot())
                    .build());
        } catch (StatusRuntimeException e) {
            //
        }
    }
}
